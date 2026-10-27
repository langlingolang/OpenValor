package valor.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.play.client.C01PacketChatMessage;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.util.ChatUtil;

public final class CommandManager {
   public ArrayList<Command> commands = new ArrayList<>();

   @EventTarget(0)
   public final void onPacket(PacketEvent event) {
      if (event.getType() == EventType.SEND && event.getPacket() instanceof C01PacketChatMessage) {
         String message = ((C01PacketChatMessage) event.getPacket()).getMessage();
         if (isCommand(message)) {
            event.setCancelled(true);
            List<String> rawArgs = Arrays.asList(message.substring(1).trim().split("\\s+"));
            ArrayList<String> args = new ArrayList<>(rawArgs);
            if (rawArgs.get(0).isEmpty()) {
               ChatUtil.addFormattedChatMessage(String.format("%sUnknown command&r", Valor.CHAT_PREFIX).replace("&", "\u00a7"));
               return;
            }

            for (Command cmd : this.commands) {
               for (String alias : cmd.aliases) {
                  if (rawArgs.get(0).equalsIgnoreCase(alias)) {
                     cmd.execute(args);
                     return;
                  }
               }
            }

            ChatUtil.addFormattedChatMessage(String.format("%sUnknown command (&o%s&r)&r", Valor.CHAT_PREFIX, rawArgs.get(0)).replace("&", "\u00a7"));
         }
      }
   }

   public static boolean isCommand(String text) {
      return text != null && text.length() >= 2 && text.charAt(0) == '.' && Character.isLetterOrDigit(text.charAt(1));
   }
}
