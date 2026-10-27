package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatColor;
import valor.util.ChatUtil;

public final class FriendCommand extends Command {
   @Override
   public void execute(ArrayList<String> var1) {
      while (var1.size() >= 2) {
         String var2 = ((String)var1.get(1)).toLowerCase(Locale.ROOT);
         switch (var2) {
            case "a":
            case "add":
               if (var1.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s add <&oname&r> [&oname&r] ...&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
                  return;
               }

               for (String var9 : var1.subList(2, var1.size())) {
                  String var12;
                  if ((var12 = Valor.friendManager.add(var9)) == null) {
                     ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r is already in your friend list&r", Valor.CHAT_PREFIX, var9));
                  } else {
                     ChatUtil.addFormattedChatMessage(String.format("%sAdded &o%s&r to your friend list&r", Valor.CHAT_PREFIX, var12));
                  }
               }

               return;
            case "r":
            case "remove":
               if (var1.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s remove <&oname&r> [&oname&r] ...&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
                  return;
               }

               for (String var8 : var1.subList(2, var1.size())) {
                  String var11;
                  if ((var11 = Valor.friendManager.remove(var8)) == null) {
                     ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r is not in your friend list&r", Valor.CHAT_PREFIX, var8));
                  } else {
                     ChatUtil.addFormattedChatMessage(String.format("%sRemoved &o%s&r from your friend list&r", Valor.CHAT_PREFIX, var11));
                  }
               }

               return;
            case "l":
            case "list":
               var1 = Valor.friendManager.names;
               if (Valor.friendManager.names.isEmpty()) {
                  ChatUtil.addFormattedChatMessage(String.format("%sNo friends&r", Valor.CHAT_PREFIX));
                  return;
               }

               ChatUtil.addFormattedChatMessage(String.format("%sFriends:&r", Valor.CHAT_PREFIX));

               for (String var10 : var1) {
                  ChatUtil.addRawChatMessage(String.format(ChatColor.format("   &o%s&r"), var10));
               }

               return;
            case "c":
            case "clear":
               Valor.friendManager.clear();
               ChatUtil.addFormattedChatMessage(String.format("%sCleared your friend list&r", Valor.CHAT_PREFIX));
               return;
            default:
               if (var1.size() == 2) {
                  if (Valor.friendManager.contains((String)var1.get(1))) {
                     this.execute(new ArrayList<>(Arrays.asList((String)var1.get(0), "remove", (String)var1.get(1))));
                     return;
                  }

                  var1 = new ArrayList<>(Arrays.asList((String)var1.get(0), "add", (String)var1.get(1)));
                   continue;
               }
               break;
         }
      }

      ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s <&oa(dd)&r/&or(emove)&r/&ol(ist)&r/&oc(lear)&r>&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
   }

   public FriendCommand() {
      super(new ArrayList<>(Arrays.asList("friend", "f")));
   }
}
