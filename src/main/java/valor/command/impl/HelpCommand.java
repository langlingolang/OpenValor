package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatUtil;

public final class HelpCommand extends Command {
   @Override
   public void execute(ArrayList<String> var1) {
      if (!Valor.moduleManager.modules.isEmpty()) {
         ChatUtil.addFormattedChatMessage(String.format("%sCommands:&r", Valor.CHAT_PREFIX));
         Iterator var3 = Valor.commandManager.commands.iterator();

         while (var3.hasNext()) {
            Command var2;
            if (!((var2 = (Command)var3.next()) instanceof PropertyCommand)) {
               ChatUtil.addFormattedChatMessage(String.format("&7\u00bb&r .%s&r", String.join(" &7/&r .", var2.aliases)));
            }
         }
      }
   }

   public HelpCommand() {
      super(new ArrayList<>(Arrays.asList("help", "commands")));
   }
}
