package valor.module.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.IntProperty;
import valor.util.ReflectionUtil;

public class NoJumpDelay extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final IntProperty delayProp = new IntProperty("delay", 3, 0, 8);

   @Override
   public String[] getSuffix() {
      return new String[]{((Integer)this.delayProp.getValue()).toString()};
   }

   @EventTarget(0)
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         ReflectionUtil.setJumpTicks((EntityLivingBase)mc.thePlayer, Math.min(ReflectionUtil.getJumpTicks((EntityLivingBase)mc.thePlayer), (Integer)this.delayProp.getValue() + 1));
      }
   }

   public NoJumpDelay() {
      super("NoJumpDelay", false, ModuleCategory.MOVEMENT);
   }
}
