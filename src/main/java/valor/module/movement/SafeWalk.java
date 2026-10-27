package valor.module.movement;

import net.minecraft.client.Minecraft;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.SafeWalkEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.world.Scaffold;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.util.ItemUtil;
import valor.util.MoveUtil;
import valor.util.PlayerUtil;

public class SafeWalk extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final FloatProperty motionProp = new FloatProperty("motion", 1.0F, 0.5F, 1.0F);
   public final FloatProperty speedMotionProp = new FloatProperty("speed-motion", 1.0F, 0.5F, 1.5F);
   public final BooleanProperty airProp = new BooleanProperty("air", Boolean.FALSE);
   public final BooleanProperty directionCheckProp = new BooleanProperty("direction-check", Boolean.TRUE);
   public final BooleanProperty pitchCheckProp = new BooleanProperty("pitch-check", Boolean.TRUE);
   public final BooleanProperty requirePressProp = new BooleanProperty("require-press", Boolean.FALSE);
   public final BooleanProperty blocksOnlyProp = new BooleanProperty("blocks-only", Boolean.TRUE);

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE && mc.thePlayer.onGround && MoveUtil.isMoving() && this.canSafeWalk()) {
         if (MoveUtil.getSpeedEffectAmplifier() <= 0) {
            if ((Float)this.motionProp.getValue() != 1.0F) {
               MoveUtil.setSpeed(MoveUtil.getMovementSpeed() * (double)((Float)this.motionProp.getValue()).floatValue());
               return;
            }
         } else if ((Float)this.speedMotionProp.getValue() != 1.0F) {
            MoveUtil.setSpeed(MoveUtil.getMovementSpeed() * (double)((Float)this.speedMotionProp.getValue()).floatValue());
         }
      }
   }

   private boolean canSafeWalk() {
      if (((Scaffold)Valor.moduleManager.modules.get(Scaffold.class)).isEnabled()) {
         return false;
      } else if ((Boolean)this.directionCheckProp.getValue() && mc.gameSettings.keyBindForward.isKeyDown()) {
         return false;
      } else if ((Boolean)this.pitchCheckProp.getValue() && mc.thePlayer.rotationPitch < 69.0F) {
         return false;
      } else {
         return this.blocksOnlyProp.getValue() && !ItemUtil.isHoldingValidBlock()
            ? false
            : (!(Boolean)this.requirePressProp.getValue() || mc.gameSettings.keyBindUseItem.isKeyDown())
               && (
                  mc.thePlayer.onGround && PlayerUtil.canMoveTo(mc.thePlayer.motionX, mc.thePlayer.motionZ, -1.0)
                     || (Boolean)this.airProp.getValue() && PlayerUtil.canMoveTo(mc.thePlayer.motionX, mc.thePlayer.motionZ, -2.0)
               );
      }
   }

   @EventTarget
   public void onMove(SafeWalkEvent var1) {
      if (this.isEnabled() && this.canSafeWalk()) {
         var1.setSafeWalk(true);
      }
   }

   public SafeWalk() {
      super("SafeWalk", false, ModuleCategory.MOVEMENT);
   }
}
