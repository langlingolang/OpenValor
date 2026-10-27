package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatColor;
import valor.util.ChatUtil;

public final class TargetCommand extends Command {
   @Override
   public void execute(ArrayList<String> var1) {
      if (var1.size() >= 2) {
         String var2 = ((String)var1.get(1)).toLowerCase(Locale.ROOT);
         switch (var2) {
            case "add":
               if (var1.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s add <&oname&r>&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
                  return;
               }

               if ((var2 = Valor.targetManager.add((String)var1.get(2))) == null) {
                  ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r is already in your enemy list&r", Valor.CHAT_PREFIX, var1.get(2)));
                  return;
               }

               ChatUtil.addFormattedChatMessage(String.format("%sAdded &o%s&r to your enemy list&r", Valor.CHAT_PREFIX, var2));
               return;
            case "remove":
               if (var1.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s remove <&oname&r>&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
                  return;
               }

               if ((var2 = Valor.targetManager.remove((String)var1.get(2))) == null) {
                  ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r is not in your enemy list&r", Valor.CHAT_PREFIX, var1.get(2)));
                  return;
               }

               ChatUtil.addFormattedChatMessage(String.format("%sRemoved &o%s&r from your enemy list&r", Valor.CHAT_PREFIX, var2));
               return;
            case "list":
               var1 = Valor.targetManager.names;
               if (Valor.targetManager.names.isEmpty()) {
                  ChatUtil.addFormattedChatMessage(String.format("%sNo enemies&r", Valor.CHAT_PREFIX));
                  return;
               }

               ChatUtil.addFormattedChatMessage(String.format("%sEnemies:&r", Valor.CHAT_PREFIX));

               for (String var6 : var1) {
                  ChatUtil.addRawChatMessage(String.format(ChatColor.format("   &o%s&r"), var6));
               }

               return;
            case "clear":
               Valor.targetManager.clear();
               ChatUtil.addFormattedChatMessage(String.format("%sCleared your enemy list&r", Valor.CHAT_PREFIX));
               return;
         }
      }

      ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s <&oadd&r/&oremove&r/&olist&r/&oclear&r>&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
   }

   public TargetCommand() {
      super(new ArrayList<>(Arrays.asList("enemy", "e", "target")));
   }
}
