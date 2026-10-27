package valor.module.player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.combat.KillAura;
import valor.setting.BooleanProperty;
import valor.setting.IntProperty;
import valor.util.BotCheckUtil;
import valor.util.ItemUtil;
import valor.util.KeyUtil;

public class AutoTool extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private int bestToolSlot = -1;
   private int previousSlot = -1;
   private int delayTicks = 0;
   public final IntProperty delayProp = new IntProperty("delay", 0, 0, 5);
   public final BooleanProperty switchBackProp = new BooleanProperty("switch-back", Boolean.TRUE);
   public final BooleanProperty sneakOnlyProp = new BooleanProperty("sneak-only", Boolean.TRUE);

   @Override
   public void onDisabled() {
      this.bestToolSlot = -1;
      this.previousSlot = -1;
      this.delayTicks = 0;
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (this.bestToolSlot != -1 && this.bestToolSlot != mc.thePlayer.inventory.currentItem) {
            this.bestToolSlot = -1;
            this.previousSlot = -1;
         }

         KillAura var2;
         if (mc.objectMouseOver != null
            && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK
            && mc.gameSettings.keyBindAttack.isKeyDown()
            && !mc.thePlayer.isUsingItem()
            && (!(var2 = (KillAura)Valor.moduleManager.modules.get(KillAura.class)).isEnabled() || !BotCheckUtil.isEntityLoaded((Entity)var2.getCurrentTargetEntity()) || !var2.canAttackWithItem())) {
            if (this.delayTicks >= (Integer)this.delayProp.getValue() && (!(Boolean)this.sneakOnlyProp.getValue() || KeyUtil.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()))) {
               int var3 = ItemUtil.findBestToolForBlock(mc.thePlayer.inventory.currentItem, mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock());
               if (mc.thePlayer.inventory.currentItem != var3) {
                  if (this.previousSlot == -1) {
                     this.previousSlot = mc.thePlayer.inventory.currentItem;
                  }

                  mc.thePlayer.inventory.currentItem = this.bestToolSlot = var3;
               }
            }

            this.delayTicks++;
            return;
         }

         if ((Boolean)this.switchBackProp.getValue() && this.previousSlot != -1) {
            mc.thePlayer.inventory.currentItem = this.previousSlot;
         }

         this.bestToolSlot = -1;
         this.previousSlot = -1;
         this.delayTicks = 0;
      }
   }

   public AutoTool() {
      super("AutoTool", false, ModuleCategory.PLAYER);
   }
}
