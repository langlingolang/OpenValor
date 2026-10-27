package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatColor;
import valor.util.ChatUtil;

public final class ItemCommand extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   @Override
   public void execute(ArrayList<String> var1) {
      ItemStack var4;
      if ((var4 = mc.thePlayer.inventory.getCurrentItem()) != null) {
         String var2 = var4.getDisplayName().replace('\u00a7', '&');
         String var3 = var4.getItem().getRegistryName();
         String var5 = var4.hasTagCompound() ? var4.getTagCompound().toString().replace('\u00a7', '&') : "";
         ChatUtil.addRawChatMessage(String.format("%s%s (%s) %s", ChatColor.format(Valor.CHAT_PREFIX), var2, var3, var5));
      }
   }

   public ItemCommand() {
      super(new ArrayList<>(Arrays.asList("itemname", "item")));
   }
}
