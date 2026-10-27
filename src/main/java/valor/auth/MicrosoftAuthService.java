package valor.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MicrosoftAuthService {
   public static final int[] LOCAL_PORTS = new int[]{
      59125, 59126, 59127, 59128, 59129, 59130, 59131, 59132, 59133, 59134, 59135,
      1234, 1235, 1236, 1237, 80, 8080, 19364, 19365, 19366, 27930, 27931, 27932, 27933, 27934, 42069
   };

   private static String urlEncode(String value) {
      try {
         return URLEncoder.encode(value, "UTF-8");
      } catch (IOException e) {
         return value;
      }
   }

   private static JsonObject postJson(String url, String jsonBody) throws AuthException {
      HttpURLConnection connection = null;
      JsonObject result;
      try {
         connection = (HttpURLConnection) new URL(url).openConnection();
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("User-Agent", "valor/1.0");
         connection.setConnectTimeout(15000);
         connection.setReadTimeout(20000);
         connection.setDoOutput(true);

         try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
         }

         int responseCode = connection.getResponseCode();
         String responseBody = readStream(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
         if (responseCode >= 400) {
            String errorMsg = responseBody;
            try {
               JsonObject errorJson = new JsonParser().parse(responseBody).getAsJsonObject();
               if (errorJson.has("error_description")) {
                  errorMsg = errorJson.get("error_description").getAsString();
               } else if (errorJson.has("error")) {
                  errorMsg = errorJson.get("error").getAsString();
               }
            } catch (Exception ignored) {
            }
            throw new AuthException("HTTP " + responseCode + " for " + url + ": " + errorMsg);
         }
         result = new JsonParser().parse(responseBody.isEmpty() ? "{}" : responseBody).getAsJsonObject();
      } catch (AuthException authEx) {
         throw authEx;
      } catch (Exception ex) {
         throw new AuthException("POST " + url + " failed: " + ex, ex);
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
      return result;
   }

   public static MinecraftProfile loginWithXblToken(String xblToken) throws AuthException {
      JsonObject xblJson = authXboxLive(xblToken);
      String token = xblJson.get("Token").getAsString();
      String userHash = extractXblUserHash(xblJson);

      JsonObject xstsProps = new JsonObject();
      xstsProps.addProperty("SandboxId", "RETAIL");
      JsonArray userTokens = new JsonArray();
      userTokens.add(new JsonPrimitive(token));
      xstsProps.add("UserTokens", userTokens);

      JsonObject xstsPayload = new JsonObject();
      xstsPayload.add("Properties", xstsProps);
      xstsPayload.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
      xstsPayload.addProperty("TokenType", "JWT");

      JsonObject xstsResponse = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", xstsPayload.toString());
      String xstsToken = xstsResponse.has("Token") ? xstsResponse.get("Token").getAsString() : null;
      String xstsHash = extractXblUserHash(xstsResponse);
      if (xstsToken == null) {
         throw new AuthException("XSTS: no Token in response");
      }

      String finalHash = !xstsHash.isEmpty() ? xstsHash : userHash;
      JsonObject mcPayload = new JsonObject();
      mcPayload.addProperty("identityToken", "XBL3.0 x=" + finalHash + ";" + xstsToken);

      JsonObject mcAuthResponse = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", mcPayload.toString());
      String accessToken = mcAuthResponse.has("access_token") ? mcAuthResponse.get("access_token").getAsString() : null;
      long expiresIn = mcAuthResponse.has("expires_in") ? mcAuthResponse.get("expires_in").getAsLong() : 86400L;

      if (accessToken == null) {
         throw new AuthException("Minecraft Services returned no access token.");
      }

      JsonObject profileJson = getJson("https://api.minecraftservices.com/minecraft/profile", accessToken);
      if (profileJson.has("name") && profileJson.has("id")) {
         String name = profileJson.get("name").getAsString();
         String uuid = profileJson.get("id").getAsString();
         long expiresAt = System.currentTimeMillis() + expiresIn * 1000L;
         return new MinecraftProfile(name, uuid, accessToken, expiresAt);
      } else {
         throw new AuthException("This Microsoft account has no Minecraft profile.");
      }
   }

   public static OAuthTokens loginWithCode(String pastedUrl) throws AuthException {
      String code = extractCodeParam(pastedUrl);
      if (code == null) {
         throw new AuthException("No 'code' parameter found in the pasted URL.");
      }
      return exchangeCode(code, "00000000402b5328", "https://login.live.com/oauth20_desktop.srf");
   }

   public static String cleanRefreshToken(String rawToken) {
      if (rawToken == null) {
         return "";
      }
      String token = rawToken.trim();
      if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
         token = token.substring(1, token.length() - 1).trim();
      }
      if (token.startsWith("refresh_token=")) {
         token = token.substring(14).trim();
      }
      int colonIdx = token.indexOf(':');
      if (colonIdx > 0 && colonIdx < token.length() - 1) {
         String sub = token.substring(colonIdx + 1).trim();
         if (sub.startsWith("M.")) {
            return sub;
         }
      }
      return token;
   }

   private static String extractCodeParam(String rawUrl) {
      if (rawUrl == null) {
         return null;
      }
      String url = rawUrl.replace("&amp;", "&");
      int queryIdx = url.indexOf('?');
      if (queryIdx < 0) {
         if (!url.contains("code=")) {
            return null;
         }
         queryIdx = url.indexOf("code=") - 1;
         if (queryIdx < 0) {
            return null;
         }
      }
      String[] params = url.substring(queryIdx + 1).split("&");
      for (String param : params) {
         int equalsIdx = param.indexOf('=');
         if (equalsIdx > 0 && "code".equals(param.substring(0, equalsIdx))) {
            String value = param.substring(equalsIdx + 1);
            int hashIdx = value.indexOf('#');
            if (hashIdx >= 0) {
               value = value.substring(0, hashIdx);
            }
            try {
               return URLDecoder.decode(value, "UTF-8");
            } catch (Exception ignored) {
               return value;
            }
         }
      }
      return null;
   }

   private static JsonObject getJson(String url, String bearerToken) throws AuthException {
      HttpURLConnection connection = null;
      JsonObject result;
      try {
         connection = (HttpURLConnection) new URL(url).openConnection();
         connection.setRequestMethod("GET");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
         connection.setRequestProperty("User-Agent", "valor/1.0");
         connection.setConnectTimeout(15000);
         connection.setReadTimeout(20000);

         int responseCode = connection.getResponseCode();
         String responseBody = readStream(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
         if (responseCode >= 400) {
            String errorMsg = responseBody;
            try {
               JsonObject errorJson = new JsonParser().parse(responseBody).getAsJsonObject();
               if (errorJson.has("error_description")) {
                  errorMsg = errorJson.get("error_description").getAsString();
               } else if (errorJson.has("error")) {
                  errorMsg = errorJson.get("error").getAsString();
               }
            } catch (Exception ignored) {
            }
            throw new AuthException("HTTP " + responseCode + " for " + url + ": " + errorMsg);
         }
         result = new JsonParser().parse(responseBody.isEmpty() ? "{}" : responseBody).getAsJsonObject();
      } catch (AuthException authEx) {
         throw authEx;
      } catch (Exception ex) {
         throw new AuthException("GET " + url + " failed: " + ex, ex);
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
      return result;
   }

   private static JsonObject authXboxLive(String rpsTicket) throws AuthException {
      AuthException lastException = null;
      for (String ticket : new String[]{rpsTicket, "d=" + rpsTicket}) {
         try {
            JsonObject props = new JsonObject();
            props.addProperty("AuthMethod", "RPS");
            props.addProperty("SiteName", "user.auth.xboxlive.com");
            props.addProperty("RpsTicket", ticket);

            JsonObject payload = new JsonObject();
            payload.add("Properties", props);
            payload.addProperty("RelyingParty", "http://auth.xboxlive.com");
            payload.addProperty("TokenType", "JWT");

            JsonObject response = postJson("https://user.auth.xboxlive.com/user/authenticate", payload.toString());
            if (response.has("Token")) {
               return response;
            }
            throw new AuthException("XBL: no Token in response");
         } catch (AuthException ex) {
            lastException = ex;
         }
      }
      throw lastException != null ? lastException : new AuthException("XBL authentication failed.");
   }

   private static String readStream(InputStream stream) {
      if (stream == null) {
         return "";
      }
      try (Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A")) {
         return scanner.hasNext() ? scanner.next() : "";
      }
   }

   private static int getXboxErrorCode(AuthException exception) {
      String message = exception.getMessage();
      if (message == null) {
         return 0;
      }
      String lower = message.toLowerCase(Locale.ROOT);
      if (lower.contains("compromised") || lower.contains("security interrupt") || lower.contains("collecting proof")) {
         return 100;
      } else if (lower.contains("expired") || lower.contains("revoked") || lower.contains("aadsts50173") || lower.contains("aadsts70008")) {
         return 80;
      } else if (!lower.contains("invalid_grant") && !lower.contains("aadsts70000")) {
         return !lower.contains("aadsts700016")
               && !lower.contains("was not found in the directory")
               && !lower.contains("aadsts7000218")
               && !lower.contains("invalid_scope")
            ? 30
            : 10;
      } else {
         return lower.contains("different client") ? 20 : 60;
      }
   }

   public static RefreshResult loginWithRefreshToken(String rawRefreshToken) throws AuthException {
      String refreshToken = cleanRefreshToken(rawRefreshToken);
      if (refreshToken.isEmpty()) {
         throw new AuthException("Empty refresh token.");
      }

      AuthException bestException = null;
      OAuthEndpoint[] endpoints = new OAuthEndpoint[]{
         new OAuthEndpoint("https://login.live.com/oauth20_token.srf", "00000000402b5328", "service::user.auth.xboxlive.com::MBI_SSL", "https://login.live.com/oauth20_desktop.srf"),
         new OAuthEndpoint("https://login.live.com/oauth20_token.srf", "00000000402b5328", "service::user.auth.xboxlive.com::MBI_SSL", null),
         new OAuthEndpoint("https://login.live.com/oauth20_token.srf", "000000004c12ae6f", "service::user.auth.xboxlive.com::MBI_SSL", "https://login.live.com/oauth20_desktop.srf"),
         new OAuthEndpoint("https://login.live.com/oauth20_token.srf", "000000004c12ae6f", "service::xboxlive.signin::MBI_SSL", "https://login.live.com/oauth20_desktop.srf"),
         new OAuthEndpoint("https://login.live.com/oauth20_token.srf", "000000004c12ae6f", "service::user.auth.xboxlive.com::MBI_SSL", null),
         new OAuthEndpoint("https://login.live.com/oauth20_token.srf", "54fd49e4-2103-4044-9603-2b028c814ec3", "XboxLive.signin XboxLive.offline_access", null),
         new OAuthEndpoint("https://login.live.com/oauth20_token.srf", "54fd49e4-2103-4044-9603-2b028c814ec3", "XboxLive.signin offline_access", null),
         new OAuthEndpoint("https://login.microsoftonline.com/consumers/oauth2/v2.0/token", "54fd49e4-2103-4044-9603-2b028c814ec3", "XboxLive.signin offline_access", null)
      };

      for (OAuthEndpoint endpoint : endpoints) {
         try {
            StringBuilder body = new StringBuilder();
            body.append("grant_type=refresh_token");
            body.append("&client_id=").append(urlEncode(endpoint.clientId));
            body.append("&refresh_token=").append(urlEncode(refreshToken));
            if (endpoint.scope != null && !endpoint.scope.isEmpty()) {
               body.append("&scope=").append(urlEncode(endpoint.scope));
            }
            if (endpoint.redirectUri != null && !endpoint.redirectUri.isEmpty()) {
               body.append("&redirect_uri=").append(urlEncode(endpoint.redirectUri));
            }

            JsonObject response = postJson(endpoint.tokenUrl, body.toString());
            if (!response.has("access_token")) {
               throw new AuthException("Token refresh returned no access token.");
            }

            OAuthTokens tokens = parseTokens(response);
            MinecraftProfile profile = loginWithXblToken(tokens.accessToken);
            String finalRefreshToken = tokens.refreshToken != null && !tokens.refreshToken.isEmpty() ? tokens.refreshToken : refreshToken;
            return new RefreshResult(new OAuthTokens(tokens.accessToken, finalRefreshToken, tokens.expiresAt), profile);
         } catch (AuthException ex) {
            if (bestException == null) {
               bestException = ex;
            } else {
               int currentScore = getXboxErrorCode(bestException);
               if (getXboxErrorCode(ex) > currentScore) {
                  bestException = ex;
               }
            }
         }
      }

      if (bestException == null) {
         throw new AuthException("Refresh failed.");
      }

      String message = bestException.getMessage();
      String resolvedMessage;
      if (message != null && !message.isEmpty()) {
         String lower = message.toLowerCase(Locale.ROOT);
         if (lower.contains("compromised") || lower.contains("security interrupt") || lower.contains("collecting proof")) {
            resolvedMessage = "Microsoft blocked this account (security interrupt). Sign in interactively via Browser and complete their proof challenge.";
         } else if (lower.contains("different client")) {
            resolvedMessage = "Refresh token belongs to a different Microsoft app. Use Browser login, or paste a token from the Minecraft launcher client.";
         } else if (lower.contains("aadsts700016") || lower.contains("was not found in the directory")) {
            resolvedMessage = "OAuth client/endpoint mismatch (internal). Try Browser login.";
         } else if (!lower.contains("expired") && !lower.contains("revoked") && !lower.contains("aadsts50173") && !lower.contains("aadsts70008")) {
            int traceIdx = message.indexOf("Trace ID:");
            if (traceIdx > 0) {
               message = message.substring(0, traceIdx).trim();
            }
            int corrIdx = message.indexOf("Correlation ID:");
            if (corrIdx > 0) {
               message = message.substring(0, corrIdx).trim();
            }
            resolvedMessage = message;
         } else {
            resolvedMessage = "Refresh token expired or revoked. Add the account again via Browser.";
         }
      } else {
         resolvedMessage = "Refresh failed.";
      }
      throw new AuthException(resolvedMessage, bestException);
   }

   private static String extractXblUserHash(JsonObject json) {
      if (!json.has("DisplayClaims")) {
         return "";
      }
      JsonObject displayClaims = json.getAsJsonObject("DisplayClaims");
      if (displayClaims.has("xui") && displayClaims.get("xui").isJsonArray()) {
         JsonArray xui = displayClaims.getAsJsonArray("xui");
         if (xui.size() > 0 && xui.get(0).isJsonObject()) {
            JsonObject first = xui.get(0).getAsJsonObject();
            if (first.has("uhs")) {
               return first.get("uhs").getAsString();
            }
         }
      }
      if (displayClaims.has("xdi") && displayClaims.get("xdi").isJsonObject()) {
         JsonObject xdi = displayClaims.getAsJsonObject("xdi");
         if (xdi.has("uhs")) {
            return xdi.get("uhs").getAsString();
         }
      }
      return "";
   }

   private static OAuthTokens parseTokens(JsonObject json) {
      String accessToken = json.has("access_token") ? json.get("access_token").getAsString() : "";
      String refreshToken = json.has("refresh_token") ? json.get("refresh_token").getAsString() : "";
      long expiresIn = json.has("expires_in") ? json.get("expires_in").getAsLong() : 3600L;
      long expiresAt = System.currentTimeMillis() + expiresIn * 1000L;
      return new OAuthTokens(accessToken, refreshToken, expiresAt);
   }

   private static JsonObject exchangeCode(String url, String postData) throws AuthException {
      HttpURLConnection connection = null;
      try {
         connection = (HttpURLConnection) new URL(url).openConnection();
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("User-Agent", "valor/1.0");
         connection.setConnectTimeout(15000);
         connection.setReadTimeout(20000);
         connection.setDoOutput(true);

         try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
         }

         int responseCode = connection.getResponseCode();
         String responseBody = readStream(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
         if (responseCode >= 400) {
            String errorMsg = responseBody;
            try {
               JsonObject errorJson = new JsonParser().parse(responseBody).getAsJsonObject();
               if (errorJson.has("error_description")) {
                  errorMsg = errorJson.get("error_description").getAsString();
               } else if (errorJson.has("error")) {
                  errorMsg = errorJson.get("error").getAsString();
               }
            } catch (Exception ignored) {
            }
            throw new AuthException("HTTP " + responseCode + " for " + url + ": " + errorMsg);
         }
         return new JsonParser().parse(responseBody.isEmpty() ? "{}" : responseBody).getAsJsonObject();
      } catch (AuthException authEx) {
         throw authEx;
      } catch (Exception ex) {
         throw new AuthException("POST " + url + " failed: " + ex, ex);
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private static String exchangeCode(HttpURLConnection connection) {
      try {
         InputStream is = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
         if (is == null) {
            return "";
         }
         try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
         }
      } catch (Throwable ignored) {
         return "";
      }
   }

   private static String loginWithCookiesToken(List<HttpCookie> initialCookies, String clientId, String redirectUri, String scope) throws AuthException {
      HttpURLConnection connection = null;
      try {
         ArrayList<HttpCookie> cookieJar = new ArrayList<>(initialCookies);
         String currentUrl = "https://login.live.com/oauth20_authorize.srf?client_id=" + urlEncode(clientId) + "&response_type=code&redirect_uri=" + urlEncode(redirectUri) + "&scope=" + urlEncode(scope);

         for (int redirectCount = 0; redirectCount < 20; redirectCount++) {
            if (connection != null) {
               connection.disconnect();
            }
            URL url = new URL(currentUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

            String host = url.getHost() == null ? "" : url.getHost().toLowerCase(Locale.ROOT);
            String path = url.getPath() != null && !url.getPath().isEmpty() ? url.getPath() : "/";
            boolean isHttps = "https".equalsIgnoreCase(url.getProtocol());

            StringBuilder cookieHeader = new StringBuilder();
            LinkedHashMap<String, String> matchedCookies = new LinkedHashMap<>();
            for (HttpCookie cookie : cookieJar) {
               if (!cookie.secure || isHttps) {
                  String cookieDomain = cookie.domain;
                  boolean domainMatches = false;
                  if (cookieDomain != null && !cookieDomain.isEmpty()) {
                     String cleanDomain = (cookieDomain.startsWith(".") ? cookieDomain.substring(1) : cookieDomain).toLowerCase(Locale.ROOT);
                     domainMatches = cookie.hostOnly ? host.equals(cleanDomain) : host.equals(cleanDomain) || host.endsWith("." + cleanDomain);
                  }
                  if (domainMatches) {
                     String cookiePath = cookie.path;
                     boolean pathMatches = (cookiePath == null || cookiePath.isEmpty() || "/".equals(cookiePath)) || path.equals(cookiePath) || path.startsWith(cookiePath.endsWith("/") ? cookiePath : cookiePath + "/");
                     if (pathMatches) {
                        matchedCookies.put(cookie.name, cookie.value);
                     }
                  }
               }
            }

            for (Entry<String, String> entry : matchedCookies.entrySet()) {
               if (cookieHeader.length() > 0) {
                  cookieHeader.append("; ");
               }
               cookieHeader.append(entry.getKey()).append('=').append(entry.getValue());
            }

            String cookieStr = cookieHeader.toString();
            if (!cookieStr.isEmpty()) {
               connection.setRequestProperty("Cookie", cookieStr);
            }
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);

            int responseCode = connection.getResponseCode();
            try {
               Map<String, List<String>> headers = connection.getHeaderFields();
               if (headers != null) {
                  for (Entry<String, List<String>> header : headers.entrySet()) {
                     if (header.getKey() != null && "Set-Cookie".equalsIgnoreCase(header.getKey()) && header.getValue() != null) {
                        for (String setCookieHeader : header.getValue()) {
                           if (setCookieHeader != null && !setCookieHeader.isEmpty()) {
                              String[] parts = setCookieHeader.split(";");
                              if (parts.length > 0) {
                                 int eqIdx = parts[0].indexOf('=');
                                 if (eqIdx > 0) {
                                    String cookieName = parts[0].substring(0, eqIdx).trim();
                                    String cookieValue = parts[0].substring(eqIdx + 1).trim();
                                    String cookieDomain = url.getHost();
                                    String cookiePath = "/";
                                    boolean cookieSecure = false;
                                    boolean cookieHostOnly = true;

                                    for (int i = 1; i < parts.length; i++) {
                                       String attr = parts[i].trim();
                                       int attrEq = attr.indexOf('=');
                                       String attrName = (attrEq < 0 ? attr : attr.substring(0, attrEq)).trim().toLowerCase(Locale.ROOT);
                                       String attrValue = attrEq < 0 ? "" : attr.substring(attrEq + 1).trim();

                                       if ("domain".equals(attrName) && !attrValue.isEmpty()) {
                                          cookieDomain = attrValue.startsWith(".") ? attrValue.substring(1) : attrValue;
                                          cookieHostOnly = false;
                                       } else if ("path".equals(attrName) && !attrValue.isEmpty()) {
                                          cookiePath = attrValue;
                                       } else if ("secure".equals(attrName)) {
                                          cookieSecure = true;
                                       }
                                    }
                                    cookieJar.add(new HttpCookie(cookieDomain, cookieHostOnly, cookiePath, cookieSecure, cookieName, cookieValue));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } catch (Throwable ignored) {
            }

            if (responseCode != 302 && responseCode != 301 && responseCode != 303 && responseCode != 307 && responseCode != 308) {
               if (responseCode != 200) {
                  throw new AuthException("Authorize HTTP " + responseCode);
               }
               String extractedCode = extractCodeParam(currentUrl);
               if (extractedCode != null) {
                  return extractedCode;
               }

               String responseBody = exchangeCode(connection);
               String authCode = null;
               if (responseBody != null && !responseBody.isEmpty()) {
                  Matcher m1 = Pattern.compile("(?i)(?:href|content|action|url)\\s*=\\s*[\"']([^\"']*code=[^\"']+)[\"']").matcher(responseBody);
                  while (m1.find()) {
                     String code = extractCodeParam(m1.group(1));
                     if (code != null) {
                        authCode = code;
                        break;
                     }
                  }
                  if (authCode == null) {
                     Matcher m2 = Pattern.compile("(?i)(https?://[^\\s\"'<>]*code=[^\\s\"'<>]+)").matcher(responseBody);
                     while (m2.find()) {
                        String code = extractCodeParam(m2.group(1));
                        if (code != null) {
                           authCode = code;
                           break;
                        }
                     }
                  }
               }

               if (authCode == null) {
                  String lowerBody = responseBody == null ? "" : responseBody.toLowerCase(Locale.ROOT);
                  if (!lowerBody.contains("name=\"loginfmt\"")
                     && !lowerBody.contains("name=\"passwd\"")
                     && !lowerBody.contains("id=\"i0116\"")
                     && !lowerBody.contains("id=\"i0118\"")) {
                     throw new AuthException("Authorize landed on 200 without code (cookies may be expired).");
                  }
                  throw new AuthException("Authorize returned the Microsoft login page (auth cookies missing or not sent).");
               }
               return authCode;
            }

            String location = connection.getHeaderField("Location");
            if (location == null) {
               throw new AuthException("Redirect without Location.");
            }

            String redirectTarget = location.startsWith("http") ? location : new URL(url, location).toString();
            String codeFromRedirect = extractCodeParam(redirectTarget);
            if (codeFromRedirect != null && (redirectTarget.startsWith(redirectUri) || redirectTarget.contains("oauth20_desktop.srf") || redirectTarget.contains("code="))) {
               return codeFromRedirect;
            }
            currentUrl = redirectTarget;
         }
         throw new AuthException("Too many redirects during authorize.");
      } catch (AuthException authEx) {
         throw authEx;
      } catch (Exception ex) {
         throw new AuthException("Cookie authorize failed: " + ex, ex);
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public static OAuthTokens exchangeCode(String code, String clientId, String redirectUri) throws AuthException {
      String postData = "client_id=" + urlEncode(clientId) + "&grant_type=authorization_code&code=" + urlEncode(code) + "&redirect_uri=" + urlEncode(redirectUri);
      JsonObject response = exchangeCode("https://login.live.com/oauth20_token.srf", postData);
      if (!response.has("access_token")) {
         throw new AuthException("Authorization-code exchange returned no access token.");
      }
      return parseTokens(response);
   }

   public static OAuthTokens loginWithCookies(String cookieData) throws AuthException {
      ArrayList<HttpCookie> cookies = new ArrayList<>();
      if (cookieData == null || cookieData.trim().isEmpty()) {
         throw new AuthException("Empty cookies.txt.");
      }

      String[] lines = cookieData.split("\\r?\\n");
      for (String line : lines) {
         String trimmed = line.trim();
         if (!trimmed.isEmpty()) {
            if (trimmed.startsWith("#HttpOnly_")) {
               trimmed = trimmed.substring(10);
            } else if (trimmed.startsWith("#")) {
               continue;
            }

            String[] tokens = trimmed.split("\\t");
            if (tokens.length < 7) {
               int eqIdx = trimmed.indexOf('=');
               if (eqIdx > 0 && !trimmed.contains(" ")) {
                  cookies.add(new HttpCookie("login.live.com", false, "/", true, trimmed.substring(0, eqIdx).trim(), trimmed.substring(eqIdx + 1).trim()));
               }
            } else {
               String domain = tokens[0].trim();
               boolean hostOnly = !"TRUE".equalsIgnoreCase(tokens[1].trim());
               String path = tokens[2].trim();
               boolean secure = "TRUE".equalsIgnoreCase(tokens[3].trim());
               String name = tokens[5].trim();
               String value = tokens[6];
               if (tokens.length > 7) {
                  StringBuilder fullValue = new StringBuilder(value);
                  for (int i = 7; i < tokens.length; i++) {
                     fullValue.append('\t').append(tokens[i]);
                  }
                  value = fullValue.toString();
               }
               if (domain.startsWith(".")) {
                  domain = domain.substring(1);
                  hostOnly = false;
               }
               if (!name.isEmpty()) {
                  String lowerDomain = domain.toLowerCase(Locale.ROOT);
                  if (lowerDomain.endsWith("live.com") || lowerDomain.endsWith("microsoft.com") || lowerDomain.endsWith("microsoftonline.com")) {
                     hostOnly = false;
                  }
                  cookies.add(new HttpCookie(domain, hostOnly, path, secure, name, value));
               }
            }
         }
      }

      if (cookies.isEmpty()) {
         throw new AuthException("No valid cookies parsed from cookies.txt.");
      }

      boolean foundAuthCookie = false;
      for (HttpCookie cookie : cookies) {
         String lowerName = cookie.name.toLowerCase(Locale.ROOT);
         if (lowerName.equals("mspauth")
            || lowerName.equals("mspprof")
            || lowerName.equals("__host-msaw0")
            || lowerName.equals("wlssc")
            || lowerName.equals("ppauth")) {
            foundAuthCookie = true;
            break;
         }
      }

      if (!foundAuthCookie) {
         throw new AuthException("No Microsoft auth cookies found (need MSPAuth/MSPProf). Export a full cookies.txt including HttpOnly cookies from login.live.com.");
      }

      for (String scope : new String[]{"service::user.auth.xboxlive.com::MBI_SSL", "service::xboxlive.signin::MBI_SSL"}) {
         try {
            return exchangeCode(
               loginWithCookiesToken(cookies, "00000000402b5328", "https://login.live.com/oauth20_desktop.srf", scope),
               "00000000402b5328",
               "https://login.live.com/oauth20_desktop.srf"
            );
         } catch (AuthException ignored) {
         }
      }

      try {
         return exchangeCode(
            loginWithCookiesToken(cookies, "000000004c12ae6f", "https://login.live.com/oauth20_desktop.srf", "service::user.auth.xboxlive.com::MBI_SSL"),
            "000000004c12ae6f",
            "https://login.live.com/oauth20_desktop.srf"
         );
      } catch (AuthException ex) {
         throw ex;
      }
   }

   public static String getAuthUrl(int port) {
      return "http://localhost:" + port + "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";
   }

   public static String getAuthUrl(int port, String state) {
      String redirectUri = getAuthUrl(port);
      return "https://login.live.com/oauth20_authorize.srf?client_id="
         + urlEncode("54fd49e4-2103-4044-9603-2b028c814ec3")
         + "&response_type=code&scope="
         + urlEncode("XboxLive.signin XboxLive.offline_access")
         + "&redirect_uri="
         + urlEncode(redirectUri)
         + "&prompt=select_account&state="
         + urlEncode(state);
   }

   public static MinecraftProfile loginWithAccessToken(String rawToken) throws AuthException {
      String token = cleanToken(rawToken);
      if (token.isEmpty()) {
         throw new AuthException("Empty access token.");
      }

      JsonObject profileJson;
      try {
         profileJson = getJson("https://api.minecraftservices.com/minecraft/profile", token);
      } catch (AuthException ex) {
         String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
         if (!msg.contains("401") && !msg.contains("403") && !msg.contains("unauthorized")) {
            throw ex;
         }
         throw new AuthException("Access token invalid or expired.", ex);
      }

      if (profileJson.has("name") && profileJson.has("id")) {
         String username = profileJson.get("name").getAsString();
         String uuid = profileJson.get("id").getAsString();
         long expiry = getJwtExpiry(token);
         if (expiry > 0L && System.currentTimeMillis() >= expiry) {
            throw new AuthException("Access token has expired.");
         }
         if (expiry <= 0L) {
            expiry = System.currentTimeMillis() + 86400000L;
         }
         return new MinecraftProfile(username, uuid, token, expiry);
      } else {
         throw new AuthException("Access token has no Minecraft profile.");
      }
   }

   private static long getJwtExpiry(String jwt) {
      try {
         String[] parts = jwt.split("\\.");
         if (parts.length < 2) {
            return 0L;
         }
         String payload = parts[1].replace('-', '+').replace('_', '/');
         int rem = payload.length() % 4;
         if (rem > 0) {
            StringBuilder sb = new StringBuilder(payload);
            for (int i = 0; i < 4 - rem; i++) {
               sb.append('=');
            }
            payload = sb.toString();
         }
         byte[] decoded = Base64.getDecoder().decode(payload);
         JsonObject json = new JsonParser().parse(new String(decoded, StandardCharsets.UTF_8)).getAsJsonObject();
         if (json.has("exp")) {
            return json.get("exp").getAsLong() * 1000L;
         }
      } catch (Throwable ignored) {
      }
      return 0L;
   }

   public static String cleanToken(String rawToken) {
      if (rawToken == null) {
         return "";
      }
      String token = rawToken.trim();
      if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
         token = token.substring(1, token.length() - 1).trim();
      }
      if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
         token = token.substring(7).trim();
      }
      if (token.startsWith("access_token=")) {
         token = token.substring(13).trim();
      }
      int colonIdx = token.indexOf(':');
      if (colonIdx > 0 && colonIdx < token.length() - 1) {
         String sub = token.substring(colonIdx + 1).trim();
         if (sub.indexOf('.') > 0 && sub.split("\\.").length >= 3) {
            return sub;
         }
      }
      return token;
   }

   public static void openUrlInBrowser(String url) {
      try {
         Desktop.getDesktop().browse(new URI(url));
      } catch (Throwable t) {
         try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
         } catch (Throwable ignored) {
         }
      }
   }

   public static final class AuthException extends Exception {
      public AuthException(String message) {
         super(message);
      }
      public AuthException(String message, Throwable cause) {
         super(message, cause);
      }
   }

   public static final class RefreshResult {
      public final OAuthTokens tokens;
      public final MinecraftProfile profile;
      public RefreshResult(OAuthTokens tokens, MinecraftProfile profile) {
         this.tokens = tokens;
         this.profile = profile;
      }
   }

   static final class OAuthEndpoint {
      final String tokenUrl;
      final String clientId;
      final String scope;
      final String redirectUri;
      OAuthEndpoint(String tokenUrl, String clientId, String scope, String redirectUri) {
         this.tokenUrl = tokenUrl;
         this.clientId = clientId;
         this.scope = scope;
         this.redirectUri = redirectUri;
      }
   }

   static final class HttpCookie {
      final String domain;
      final boolean hostOnly;
      final String path;
      final boolean secure;
      final String name;
      final String value;
      HttpCookie(String domain, boolean hostOnly, String path, boolean secure, String name, String value) {
         this.domain = domain;
         this.hostOnly = hostOnly;
         this.path = path != null && !path.isEmpty() ? path : "/";
         this.secure = secure;
         this.name = name;
         this.value = value;
      }
   }

   public static final class OAuthTokens {
      public final String accessToken;
      public final String refreshToken;
      public final long expiresAt;
      public OAuthTokens(String accessToken, String refreshToken, long expiresAt) {
         this.accessToken = accessToken;
         this.refreshToken = refreshToken;
         this.expiresAt = expiresAt;
      }
   }

   public static final class MinecraftProfile {
      public final String username;
      public final String uuid;
      public final String accessToken;
      public final long expiresAt;
      public MinecraftProfile(String username, String uuid, String accessToken, long expiresAt) {
         this.username = username;
         this.uuid = uuid;
         this.accessToken = accessToken;
         this.expiresAt = expiresAt;
      }
   }
}
