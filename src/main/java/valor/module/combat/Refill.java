package valor.module.combat;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.TimerUtil;

public class Refill extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final IntProperty delayProp = new IntProperty("delay", 1, 0, 20);
   public final ModeProperty modeProp = new ModeProperty("mode", 1, new String[]{"SOUP", "POT"});
   private final TimerUtil timer = new TimerUtil();

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget
   public void onUpdate(TickEvent var1) {
      if (this.isEnabled() && mc.thePlayer != null && var1.getType() == EventType.PRE) {
         if ((Integer)this.modeProp.getValue() == 0) {
            this.refillItem(Items.mushroom_stew);
            return;
         }

         if ((Integer)this.modeProp.getValue() == 1) {
            this.refillItem(ItemPotion.getItemById(373));
         }
      }
   }

   private static boolean isHotbarFull() {
      for (int var0 = 0; var0 <= 36; var0++) {
         if (mc.thePlayer.inventory.getStackInSlot(var0) == null) {
            return false;
         }
      }

      return true;
   }

   private void refillItem(Item var1) {
      if (mc.currentScreen instanceof GuiInventory && !isHotbarFull() && this.timer.hasTimeElapsed((long)((Integer)this.delayProp.getValue() * 50))) {
         for (int var2 = 9; var2 < 36; var2++) {
            ItemStack var3;
            if ((var3 = mc.thePlayer.inventoryContainer.getSlot(var2).getStack()) != null && var3.getItem() == var1) {
               mc.playerController.windowClick(0, var2, 0, 1, mc.thePlayer);
               break;
            }
         }

         this.timer.reset();
      }
   }

   public Refill() {
      super("Refill", false, ModuleCategory.COMBAT);
   }
}
