package valor.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

public final class AltAccount {
   public AccountType accountType;
   public String username;
   public String uuid;
   public String accessToken;
   public String refreshToken;
   public long expiresAt;
   public long addedAt;

   public static List<AltAccount> fromJson(String json) {
      ArrayList<AltAccount> list = new ArrayList<>();
      if (json.isEmpty()) {
         return list;
      } else {
         try {
            JsonElement parsed = new JsonParser().parse(json);
            if (!parsed.isJsonArray()) {
               return list;
            }

            for (JsonElement el : parsed.getAsJsonArray()) {
               if (el.isJsonObject()) {
                  list.add(fromJsonObject(el.getAsJsonObject()));
               }
            }
         } catch (Exception ignored) {
         }

         return list;
      }
   }

   public static String toJson(List<AltAccount> accounts) {
      JsonArray array = new JsonArray();

      for (AltAccount alt : accounts) {
         JsonObject obj = new JsonObject();
         obj.addProperty("type", alt.accountType != null ? alt.accountType.name() : "OFFLINE");
         obj.addProperty("username", alt.username != null ? alt.username : "");
         obj.addProperty("uuid", alt.uuid != null ? alt.uuid : "");
         if (alt.accessToken != null) {
            obj.addProperty("accessToken", alt.accessToken);
         }
         if (alt.refreshToken != null) {
            obj.addProperty("refreshToken", alt.refreshToken);
         }
         obj.addProperty("expiresAt", alt.expiresAt);
         obj.addProperty("addedAt", alt.addedAt);
         array.add(obj);
      }

      return array.toString();
   }

   public static AltAccount createMicrosoft(String username, String uuid, String accessToken, String refreshToken, long expiresAt) {
      AltAccount alt = new AltAccount();
      alt.accountType = AccountType.MICROSOFT;
      alt.username = username;
      alt.uuid = uuid;
      alt.accessToken = accessToken;
      alt.refreshToken = refreshToken;
      alt.expiresAt = expiresAt;
      alt.addedAt = System.currentTimeMillis();
      return alt;
   }

   public static AltAccount createOffline(String username) {
      AltAccount alt = new AltAccount();
      alt.accountType = AccountType.OFFLINE;
      alt.username = username;
      alt.uuid = "00000000-0000-0000-0000-000000000000";
      alt.addedAt = System.currentTimeMillis();
      return alt;
   }

   public static AltAccount fromJsonObject(JsonObject obj) {
      AltAccount alt = new AltAccount();
      String typeStr = obj.has("type") ? obj.get("type").getAsString() : "OFFLINE";

      try {
         alt.accountType = AccountType.valueOf(typeStr);
      } catch (Exception e) {
         alt.accountType = AccountType.OFFLINE;
      }

      alt.username = obj.has("username") ? obj.get("username").getAsString() : "";
      alt.uuid = obj.has("uuid") ? obj.get("uuid").getAsString() : "";
      alt.accessToken = obj.has("accessToken") ? obj.get("accessToken").getAsString() : null;
      alt.refreshToken = obj.has("refreshToken") ? obj.get("refreshToken").getAsString() : null;
      alt.expiresAt = obj.has("expiresAt") ? obj.get("expiresAt").getAsLong() : 0L;
      alt.addedAt = obj.has("addedAt") ? obj.get("addedAt").getAsLong() : System.currentTimeMillis();
      return alt;
   }

   public enum AccountType {
      MICROSOFT,
      OFFLINE
   }
}
