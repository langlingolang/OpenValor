package valor.auth;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OAuthLocalServer {
   private static final Pattern CALLBACK_PATTERN = Pattern.compile("^code=([^&]*)&state=([^&]*)$");
   private final OAuthCallback callback;
   private final HttpServer httpServer;
   private final String stateToken;
   private final AtomicBoolean cancelled = new AtomicBoolean(false);
   private final AtomicBoolean requestHandled = new AtomicBoolean(false);
   private int boundPort;

   private static String generateRandomState() {
      try {
         SecureRandom rand = SecureRandom.getInstanceStrong();
         int len = 96 + rand.nextInt(32);
         StringBuilder sb = new StringBuilder(len);
         for (int i = 0; i < len; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_".charAt(rand.nextInt(65)));
         }
         return sb.toString();
      } catch (Throwable t) {
         SecureRandom rand = new SecureRandom();
         int len = 96 + rand.nextInt(32);
         StringBuilder sb = new StringBuilder(len);
         for (int i = 0; i < len; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_".charAt(rand.nextInt(65)));
         }
         return sb.toString();
      }
   }

   private void handleEndContext(HttpExchange exchange) {
      try {
         if (!exchange.getRemoteAddress().getAddress().isLoopbackAddress()) {
            exchange.close();
         } else {
            byte[] html = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Valor</title><style>body{font-family:Segoe UI,Arial,sans-serif;background:#1a1c26;color:#e9eef6;display:flex;align-items:center;justify-content:center;height:100vh;margin:0}.c{text-align:center;padding:32px 40px;background:#222433;border-radius:14px;box-shadow:0 8px 28px rgba(0,0,0,.45);max-width:420px}h1{margin:0 0 8px;font-size:20px;font-weight:600}p{margin:0;color:#9aa0ad;font-size:13px;line-height:1.5}.ok{color:#6ad27a;font-size:30px;margin-bottom:10px}</style></head><body><div class=\"c\"><div class=\"ok\">&#10003;</div><h1>Authenticated</h1><p>You can close this tab and return to Valor.</p></div></body></html>"
               .getBytes(StandardCharsets.UTF_8);
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "text/html; charset=UTF-8");
            headers.add("Content-Length", Integer.toString(html.length));
            exchange.sendResponseHeaders(200, (long) html.length);

            try (OutputStream out = exchange.getResponseBody()) {
               out.write(html);
            }

            exchange.close();
         }
      } catch (Throwable ignored) {
         try {
            exchange.close();
         } catch (Throwable ignored2) {
         }
      }
   }

   public final String start() throws Exception {
      this.httpServer.createContext("/", this::handleRootContext);
      this.httpServer.createContext("/end", this::handleEndContext);
      Exception lastEx = new Exception("Unable to bind to any IAS port.");
      int[] ports = MicrosoftAuthService.LOCAL_PORTS;

      for (int port : ports) {
         try {
            this.httpServer.bind(new InetSocketAddress(port), 0);
            this.boundPort = port;
            this.httpServer.start();
            return MicrosoftAuthService.getAuthUrl(this.boundPort, this.stateToken);
         } catch (Throwable t) {
            lastEx.addSuppressed(new Exception("port " + port + ": " + t.getMessage(), t));
         }
      }

      throw lastEx;
   }

   private void handleRootContext(HttpExchange exchange) {
      try {
         if (this.requestHandled.getAndSet(true)) {
            exchange.close();
         } else if (!exchange.getRemoteAddress().getAddress().isLoopbackAddress()) {
            exchange.close();
         } else {
            URI uri = exchange.getRequestURI();
            byte[] html = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Valor</title><style>body{font-family:Segoe UI,Arial,sans-serif;background:#1a1c26;color:#e9eef6;display:flex;align-items:center;justify-content:center;height:100vh;margin:0}.c{text-align:center;padding:32px 40px;background:#222433;border-radius:14px;box-shadow:0 8px 28px rgba(0,0,0,.45);max-width:420px}h1{margin:0 0 8px;font-size:20px;font-weight:600}p{margin:0;color:#9aa0ad;font-size:13px;line-height:1.5}.ok{color:#6ad27a;font-size:30px;margin-bottom:10px}</style></head><body><div class=\"c\"><div class=\"ok\">&#10003;</div><h1>Authenticated</h1><p>You can close this tab and return to Valor.</p></div></body></html>"
               .getBytes(StandardCharsets.UTF_8);
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "text/html; charset=UTF-8");
            headers.add("Content-Length", Integer.toString(html.length));
            headers.add("Location", "http://localhost:" + this.boundPort + "/end");
            exchange.sendResponseHeaders(302, (long) html.length);

            try (OutputStream out = exchange.getResponseBody()) {
               out.write(html);
            }

            exchange.close();
            new Thread(() -> {
               try {
                  if (this.cancelled.get()) {
                     this.callback.onCancelled();
                     return;
                  }

                  this.callback.onStatus("Exchanging browser auth code...", true);
                  String query = uri.getQuery();
                  if (query == null) {
                     this.callback.onStatus("Browser auth failed: empty callback query.", false);
                     return;
                  }

                  if (query.toLowerCase().contains("access_denied")) {
                     this.callback.onStatus("Authentication cancelled in browser.", false);
                     return;
                  }

                  Matcher matcher = CALLBACK_PATTERN.matcher(query);
                  if (!matcher.matches()) {
                     this.callback.onStatus("Browser auth failed: invalid callback query.", false);
                     return;
                  }

                  String state = matcher.group(2);
                  if (this.stateToken.equals(state)) {
                     String code = matcher.group(1);
                     this.callback.onStatus("Exchanging code for Microsoft token...", true);
                     MicrosoftAuthService.OAuthTokens tokens = MicrosoftAuthService.exchangeCode(code, "54fd49e4-2103-4044-9603-2b028c814ec3", MicrosoftAuthService.getAuthUrl(this.boundPort));
                     this.callback.onStatus("Authenticating with Xbox Live / Minecraft...", true);
                     MicrosoftAuthService.MinecraftProfile profile = MicrosoftAuthService.loginWithXblToken(tokens.accessToken);
                     this.callback.onStatus("Logged in as " + profile.username, true);
                     this.callback.onSuccess(profile, tokens.refreshToken);
                     return;
                  }

                  this.callback.onStatus("Browser auth failed: state mismatch.", false);
               } catch (Throwable t) {
                  this.callback.onStatus("Browser login failed: " + t.getMessage(), false);
               } finally {
                  new Thread(() -> {
                     try {
                        Thread.sleep(2000L);
                     } catch (InterruptedException ignored) {
                     }
                     this.stop();
                  }, "valor-alt-browserserver-stop").start();
               }
            }, "valor-alt-browserserver").start();
         }
      } catch (Throwable t) {
         try {
            exchange.close();
         } catch (Throwable ignored) {
         }

         try {
            this.stop();
         } catch (Throwable ignored) {
         }

         this.callback.onStatus("Browser auth server error: " + t.getMessage(), false);
      }
   }

   public OAuthLocalServer(OAuthCallback callback) throws Exception {
      this.callback = callback;
      this.httpServer = HttpServer.create();
      this.stateToken = generateRandomState();
   }

   public final void stop() {
      try {
         this.httpServer.stop(0);
      } catch (Throwable ignored) {
      }
   }

   public interface OAuthCallback {
      void onSuccess(MicrosoftAuthService.MinecraftProfile profile, String refreshToken);

      void onCancelled();

      void onStatus(String status, boolean ok);
   }
}
