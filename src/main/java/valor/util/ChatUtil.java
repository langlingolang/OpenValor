package valor.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class ChatUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final List<String> PENDING_MESSAGES = new ArrayList<String>();

   public static void sendMessageToServer(String message) {
      if (mc != null && mc.thePlayer != null) {
         mc.thePlayer.sendChatMessage(message);
      }
   }

   public static void addChatMessage(IChatComponent component) {
      if (mc != null && mc.thePlayer != null) {
         mc.thePlayer.addChatMessage(component);
      }
   }

   public static void addRawChatMessage(String text) {
      addChatMessage(new ChatComponentText(text));
   }

   public static void addFormattedChatMessage(String text) {
      if (mc != null && mc.thePlayer != null) {
         addChatMessage(new ChatComponentText(ChatColor.format(text)));
      } else {
         synchronized (PENDING_MESSAGES) {
            PENDING_MESSAGES.add(text);
         }
      }
   }

   public static void flushPendingMessages() {
      if (mc != null && mc.thePlayer != null) {
         synchronized (PENDING_MESSAGES) {
            if (!PENDING_MESSAGES.isEmpty()) {
               for (String text : PENDING_MESSAGES) {
                  addChatMessage(new ChatComponentText(ChatColor.format(text)));
               }
               PENDING_MESSAGES.clear();
            }
         }
      }
   }
}
