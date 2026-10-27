package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import valor.Valor;
import valor.command.Command;
import valor.module.Module;
import valor.util.ChatUtil;

public final class ShowCommand extends Command {
   @Override
   public void execute(ArrayList<String> var1) {
      if (var1.size() < 2) {
         ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s <&omodule&r>&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
      } else if (!((String)var1.get(1)).equals("*")) {
         Module var3;
         if ((var3 = Valor.moduleManager.getModule((String)var1.get(1))) == null) {
            ChatUtil.addFormattedChatMessage(String.format("%sModule &o%s&r not found&r", Valor.CHAT_PREFIX, var1.get(1)));
         } else if (!var3.isHidden()) {
            ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r is not hidden in HUD&r", Valor.CHAT_PREFIX, var3.getName()));
         } else {
            var3.setHidden(false);
            ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r is no longer hidden in HUD&r", Valor.CHAT_PREFIX, var3.getName()));
         }
      } else {
         Iterator var2 = Valor.moduleManager.modules.values().iterator();

         while (var2.hasNext()) {
            ((Module)var2.next()).setHidden(false);
         }

         ChatUtil.addFormattedChatMessage(String.format("%sAll modules are no longer hidden in HUD&r", Valor.CHAT_PREFIX));
      }
   }

   public ShowCommand() {
      super(new ArrayList<>(Arrays.asList("show", "s", "unhide")));
   }
}
