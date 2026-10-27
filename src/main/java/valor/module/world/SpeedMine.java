package valor.module.world;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.IntProperty;
import valor.setting.PercentageProperty;
import valor.util.ReflectionUtil;

public class SpeedMine extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final PercentageProperty speedProp = new PercentageProperty("speed", 15);
   public final IntProperty delayProp = new IntProperty("delay", 0, 0, 4);

   @Override
   public String[] getSuffix() {
      return new String[]{String.format("%d%%", this.speedProp.getValue())};
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled()
         && var1.getType() == EventType.PRE
         && !mc.playerController.isInCreativeMode()
         && mc.objectMouseOver != null
         && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
         ReflectionUtil.setBlockHitDelay(mc.playerController, Math.min(ReflectionUtil.getBlockHitDelay(mc.playerController), (Integer)this.delayProp.getValue() + 1));
         if (ReflectionUtil.isHittingBlock(mc.playerController)) {
            float var3 = ReflectionUtil.getCurBlockDamageMP(mc.playerController);
            float var2 = 0.3F * (((Integer)this.speedProp.getValue()).floatValue() / 100.0F);
            if (var3 < var2) {
               ReflectionUtil.setCurBlockDamageMP(mc.playerController, var2);
            }
         }
      }
   }

   public SpeedMine() {
      super("SpeedMine", false, ModuleCategory.WORLD);
   }
}
