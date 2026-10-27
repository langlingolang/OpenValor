package valor.module.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Mouse;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.IntProperty;
import valor.util.ReflectionUtil;

public class InventoryClicker extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final IntProperty ticksProp = new IntProperty("ticks", 2, 0, 20);
   public int heldTicks;

   @Override
   public String[] getSuffix() {
      return new String[]{((Integer)this.ticksProp.getValue()).toString() + " ticks"};
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && mc.thePlayer != null && var1.getType() == EventType.PRE && mc.currentScreen instanceof GuiContainer) {
         GuiContainer var4 = (GuiContainer)mc.currentScreen;
         int var2 = Mouse.getEventX() * var4.width / mc.displayWidth;
         int var3 = var4.height - Mouse.getEventY() * var4.height / mc.displayHeight - 1;
         if (Mouse.isButtonDown(0)) {
            this.heldTicks++;
            if (this.heldTicks > (Integer)this.ticksProp.getValue()) {
               ReflectionUtil.mouseClicked(var4, var2, var3);
               return;
            }
         } else {
            this.heldTicks = 0;
         }
      }
   }

   public InventoryClicker() {
      super("InventoryClicker", false, ModuleCategory.PLAYER);
   }
}
