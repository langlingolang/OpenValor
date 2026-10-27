package valor.module.movement;

import net.minecraft.client.Minecraft;
import valor.Valor;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.combat.KillAura;
import valor.setting.BooleanProperty;
import valor.setting.PercentageProperty;

public class KeepSprint extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final PercentageProperty slowdownProp = new PercentageProperty("slowdown", 0);
   public final BooleanProperty groundOnlyProp = new BooleanProperty("ground-only", Boolean.FALSE);
   public final BooleanProperty reachOnlyProp = new BooleanProperty("reach-only", Boolean.FALSE);

   public boolean shouldKeepSprint() {
      if (mc.thePlayer == null) {
         return false;
      } else {
         KillAura var1;
         if ((var1 = (KillAura)Valor.moduleManager.modules.get(KillAura.class)) != null && var1.isEnabled() && var1.isVanillaAutoBlock() && var1.isBlockingSword()) {
            return false;
         } else {
            return this.groundOnlyProp.getValue() && !mc.thePlayer.onGround
               ? false
               : !(Boolean)this.reachOnlyProp.getValue()
                  || mc.objectMouseOver != null
                     && mc.objectMouseOver.hitVec != null
                     && mc.getRenderViewEntity() != null
                     && mc.objectMouseOver.hitVec.distanceTo(mc.getRenderViewEntity().getPositionEyes(1.0F)) > 3.0;
         }
      }
   }

   public final double getSprintSlowdown(double var1) {
      double var3 = Math.max(0.0, Math.min(1.0, ((Integer)this.slowdownProp.getValue()).doubleValue() / 100.0));
      return 1.0 - (1.0 - var1) * var3;
   }

   public KeepSprint() {
      super("KeepSprint", false, ModuleCategory.MOVEMENT);
   }
}
