package valor.module.movement;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LivingUpdateEvent;
import valor.events.PacketEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.util.MoveUtil;

public class Fly extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final int MODE_VANILLA = 0;
   private static final int MODE_MOTION = 1;
   private static final int MODE_GLIDE = 2;
   private static final int MODE_PACKET = 3;
   private static final int MODE_HYPIXEL = 4;

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"VANILLA", "MOTION", "GLIDE", "PACKET", "HYPIXEL"});
   public final FloatProperty hSpeedProp = new FloatProperty(
      "h-speed", 1.0F, 0.1F, 5.0F, () -> (Integer)this.modeProp.getValue() == 0 || (Integer)this.modeProp.getValue() == 1 || (Integer)this.modeProp.getValue() == 3
   );
   public final FloatProperty vSpeedProp = new FloatProperty("v-speed", 0.42F, 0.05F, 2.0F, () -> (Integer)this.modeProp.getValue() == 1 || (Integer)this.modeProp.getValue() == 3);
   public final FloatProperty glideSpeedProp = new FloatProperty(
      "glide-speed", 0.08F, 0.01F, 0.5F, () -> (Integer)this.modeProp.getValue() == 2 || (Integer)this.modeProp.getValue() == 4
   );
   public final BooleanProperty antiKickProp = new BooleanProperty("anti-kick", Boolean.TRUE);
   public final BooleanProperty disableOnDamageProp = new BooleanProperty("disable-on-damage", Boolean.FALSE);

   private boolean originalAllowFlying;
   private boolean originalIsFlying;

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget(3)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && canFly() && var1.getType() == EventType.PRE) {
         if ((Integer)this.modeProp.getValue() == 1 || (Integer)this.modeProp.getValue() == 2) {
            mc.thePlayer.movementInput.jump = false;
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.RECEIVE && (Boolean)this.antiKickProp.getValue()) {
         if (var1.getPacket() instanceof S08PacketPlayerPosLook) {
            this.setEnabled(false);
         }
      }
   }

   private static void setMotionH(double speed) {
      if (MoveUtil.isMoving()) {
         MoveUtil.strafe(speed, MoveUtil.getMoveYaw());
      } else {
         mc.thePlayer.motionX = 0.0;
         mc.thePlayer.motionZ = 0.0;
      }
   }

   @Override
   public void verifyValue(String var1) {
      if (this.isEnabled() && (Integer)this.modeProp.getValue() == 0) {
         this.restoreFlightCapabilities();
         if (mc.thePlayer != null) {
            this.originalAllowFlying = mc.thePlayer.capabilities.allowFlying;
            this.originalIsFlying = mc.thePlayer.capabilities.isFlying;
         }
      }
   }

   private static boolean isSneakKeyDown() {
      return mc.gameSettings.keyBindSneak.isKeyDown();
   }

   private static void setMotionV(double upSpeed, double downSpeed) {
      if (isJumpKeyDown()) {
         mc.thePlayer.motionY = upSpeed;
      } else if (isSneakKeyDown()) {
         mc.thePlayer.motionY = -downSpeed;
      } else {
         mc.thePlayer.motionY = 0.0;
      }
   }

   @Override
   public void onDisabled() {
      this.restoreFlightCapabilities();
   }

   @Override
   public void onEnabled() {
      if (mc.thePlayer != null) {
         this.originalAllowFlying = mc.thePlayer.capabilities.allowFlying;
         this.originalIsFlying = mc.thePlayer.capabilities.isFlying;
      }
   }

   @EventTarget(3)
   public void onLivingUpdate(LivingUpdateEvent var1) {
      if (this.isEnabled() && canFly()) {
         if ((Boolean)this.disableOnDamageProp.getValue() && mc.thePlayer.hurtTime > 0) {
            this.setEnabled(false);
         } else {
            switch ((Integer)this.modeProp.getValue()) {
               case 0:
                  mc.thePlayer.capabilities.allowFlying = true;
                  mc.thePlayer.capabilities.isFlying = true;
                  mc.thePlayer.capabilities.setFlySpeed(0.05F * (Float)this.hSpeedProp.getValue());
                  mc.thePlayer.fallDistance = 0.0F;
                  return;
               case 1:
                  mc.thePlayer.fallDistance = 0.0F;
                  double var8 = 0.28 * (double)((Float)this.hSpeedProp.getValue()).floatValue();
                  double var9 = (double)((Float)this.vSpeedProp.getValue()).floatValue();
                  setMotionH(var8);
                  setMotionV(var9, var9);
                  return;
               case 2:
                  mc.thePlayer.fallDistance = 0.0F;
                  double var7 = (double)((Float)this.glideSpeedProp.getValue()).floatValue();
                  if (isJumpKeyDown()) {
                     mc.thePlayer.motionY = var7;
                  } else if (isSneakKeyDown()) {
                     mc.thePlayer.motionY = -var7 * 2.0;
                  } else if (!mc.thePlayer.onGround) {
                     mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY, -var7);
                  }

                  if (MoveUtil.isMoving()) {
                     MoveUtil.strafe(0.2, MoveUtil.getMoveYaw());
                  }

                  return;
               case 3:
                  mc.thePlayer.fallDistance = 0.0F;
                  mc.thePlayer.onGround = true;
                  double var6 = 0.26 * (double)((Float)this.hSpeedProp.getValue()).floatValue();
                  double var4 = (double)((Float)this.vSpeedProp.getValue()).floatValue();
                  setMotionH(var6);
                  setMotionV(var4, var4);
                  return;
               case 4:
                  mc.thePlayer.fallDistance = 0.0F;
                  double var2 = (double)((Float)this.glideSpeedProp.getValue()).floatValue();
                  if (!mc.thePlayer.onGround) {
                     if (isJumpKeyDown()) {
                        mc.thePlayer.motionY = var2 * 0.75;
                     } else if (isSneakKeyDown()) {
                        mc.thePlayer.motionY = -var2 * 1.5;
                     } else {
                        mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY, -var2 * 0.5);
                     }
                  }

                  if (MoveUtil.isMoving() && !mc.thePlayer.onGround) {
                     MoveUtil.strafe(Math.min(MoveUtil.getMovementSpeed(), 0.22), MoveUtil.getMoveYaw());
                  }
            }
         }
      }
   }

   private void restoreFlightCapabilities() {
      if (mc.thePlayer != null && !mc.thePlayer.capabilities.isCreativeMode) {
         mc.thePlayer.capabilities.allowFlying = this.originalAllowFlying;
         mc.thePlayer.capabilities.isFlying = this.originalIsFlying;
         mc.thePlayer.capabilities.setFlySpeed(0.05F);
      }
   }

   private static boolean isJumpKeyDown() {
      return mc.gameSettings.keyBindJump.isKeyDown();
   }

   private static boolean canFly() {
      return mc.thePlayer != null && mc.theWorld != null && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava();
   }

   public Fly() {
      super("Fly", false, ModuleCategory.MOVEMENT);
   }
}
