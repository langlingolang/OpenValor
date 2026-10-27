package valor.auth;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class SessionSwitcher {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static SwitchResult switchSession(AltAccount account) {
      if (account == null) {
         return SwitchResult.failure("No account.");
      } else {
         try {
            try {
               if (mc.theWorld != null) {
                  mc.theWorld.sendQuittingDisconnectingPacket();
                  mc.loadWorld(null);
               }
            } catch (Throwable t) {
               System.out.println("[Valor] SessionSwitcher.disconnectIfInWorld failed: " + t);
            }

            String username = account.username;
            String uuid = account.uuid != null && !account.uuid.isEmpty() ? account.uuid : "00000000-0000-0000-0000-000000000000";
            String accessToken = account.accessToken != null ? account.accessToken : "";
            String type = account.accountType == AltAccount.AccountType.MICROSOFT ? "mojang" : "legacy";
            Session session = createSession(username, uuid, accessToken, type);

            Field sessionField = null;
            for (Class<?> current = mc.getClass(); current != null; current = current.getSuperclass()) {
               for (Field field : current.getDeclaredFields()) {
                  if (Session.class.isAssignableFrom(field.getType())) {
                     sessionField = field;
                     break;
                  }
               }
               if (sessionField != null) {
                  break;
               }
            }

            if (sessionField == null) {
               throw new IllegalStateException("Minecraft.session field not found");
            } else {
               sessionField.setAccessible(true);

               try {
                  Field modifiersField = Field.class.getDeclaredField("modifiers");
                  modifiersField.setAccessible(true);
                  modifiersField.setInt(sessionField, sessionField.getModifiers() & -17);
               } catch (Throwable ignored) {
               }

               sessionField.set(mc, session);
               AccountStore.setActiveAccount(account);
               return SwitchResult.success("Now logged in as " + username);
            }
         } catch (Throwable t) {
            return SwitchResult.failure("Switch failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
         }
      }
   }

   private static Session createSession(String username, String uuid, String token, String type) throws Exception {
      try {
         Constructor<?> constructor = Session.class.getDeclaredConstructor(String.class, String.class, String.class, String.class);
         constructor.setAccessible(true);
         return (Session) constructor.newInstance(username, uuid, token, type);
      } catch (NoSuchMethodException e) {
         Class<?> sessionTypeClass = Class.forName("net.minecraft.util.Session$SessionType");
         Enum<?> sessionType = Enum.valueOf((Class<Enum>) sessionTypeClass, type.toUpperCase());
         Constructor<?> constructor = Session.class.getDeclaredConstructor(String.class, String.class, String.class, sessionTypeClass);
         constructor.setAccessible(true);
         return (Session) constructor.newInstance(username, uuid, token, sessionType);
      }
   }

   public static final class SwitchResult {
      public final boolean success;
      public final String message;

      public static SwitchResult failure(String message) {
         return new SwitchResult(false, message);
      }

      public static SwitchResult success(String message) {
         return new SwitchResult(true, message);
      }

      public SwitchResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }
   }
}
