package valor.command.impl;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.StringUtils;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatUtil;

public final class NameCommand extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   @Override
   public void execute(ArrayList<String> var1) {
      Session var3;
      String var4;
      if ((var3 = mc.getSession()) != null && !StringUtils.isNullOrEmpty(var4 = var3.getUsername())) {
         try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(var4), null);
            ChatUtil.addFormattedChatMessage(String.format("%sYour username has been copied to the clipboard (&o%s&r)&r", Valor.CHAT_PREFIX, var4));
            return;
         } catch (Exception var2) {
            ChatUtil.addFormattedChatMessage(String.format("%sFailed to copy&r", Valor.CHAT_PREFIX));
         }
      }
   }

   public NameCommand() {
      super(new ArrayList<>(Arrays.asList("username", "name", "ign")));
   }
}
