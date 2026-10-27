package valor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import valor.Valor;
import valor.core.MovementManager;
import valor.module.combat.TargetStrafe;

public final class MoveUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static int airTicks = 0;

   public static int getStrafeInput() {
      int strafe = 0;
      if (mc.gameSettings.keyBindLeft.isKeyDown()) {
         strafe++;
      }

      if (mc.gameSettings.keyBindRight.isKeyDown()) {
         strafe--;
      }

      return strafe;
   }

   public static float calculateMoveYaw(float yaw, float forward, float strafe) {
      TargetStrafe targetStrafe;
      if ((targetStrafe = (TargetStrafe)Valor.moduleManager.modules.get(TargetStrafe.class)).isEnabled() && !Float.isNaN(targetStrafe.strafeYaw)) {
         return targetStrafe.strafeYaw;
      } else {
         if (forward < 0.0F) {
            yaw += 180.0F;
         }

         if (strafe != 0.0F) {
            forward = forward == 0.0F ? 1.0F : 0.5F * Math.signum(forward);
            yaw += -90.0F * forward * Math.signum(strafe);
         }

         return MathHelper.wrapAngleTo180_float(yaw);
      }
   }

   public static void strafeAdd(double speed, float yaw) {
      mc.thePlayer.motionX = mc.thePlayer.motionX + -Math.sin(Math.toRadians((double)yaw)) * speed;
      mc.thePlayer.motionZ = mc.thePlayer.motionZ + Math.cos(Math.toRadians((double)yaw)) * speed;
   }

   public static void fixMovement(float yaw) {
      switch ((int)(MathHelper.wrapAngleTo180_float(calculateMoveYaw(mc.thePlayer.rotationYaw, (float)getForwardInput(), (float)getStrafeInput()) - yaw + 22.5F) + 180.0F) / 45 % 8) {
         case 0:
            mc.thePlayer.movementInput.moveForward = -1.0F;
            mc.thePlayer.movementInput.moveStrafe = 0.0F;
            break;
         case 1:
            mc.thePlayer.movementInput.moveForward = -1.0F;
            mc.thePlayer.movementInput.moveStrafe = 1.0F;
            break;
         case 2:
            mc.thePlayer.movementInput.moveForward = 0.0F;
            mc.thePlayer.movementInput.moveStrafe = 1.0F;
            break;
         case 3:
            mc.thePlayer.movementInput.moveForward = 1.0F;
            mc.thePlayer.movementInput.moveStrafe = 1.0F;
            break;
         case 4:
            mc.thePlayer.movementInput.moveForward = 1.0F;
            mc.thePlayer.movementInput.moveStrafe = 0.0F;
            break;
         case 5:
            mc.thePlayer.movementInput.moveForward = 1.0F;
            mc.thePlayer.movementInput.moveStrafe = -1.0F;
            break;
         case 6:
            mc.thePlayer.movementInput.moveForward = 0.0F;
            mc.thePlayer.movementInput.moveStrafe = -1.0F;
            break;
         case 7:
            mc.thePlayer.movementInput.moveForward = -1.0F;
            mc.thePlayer.movementInput.moveStrafe = -1.0F;
      }

      if (mc.thePlayer.movementInput.sneak) {
         mc.thePlayer.movementInput.moveForward *= 0.3F;
         mc.thePlayer.movementInput.moveStrafe *= 0.3F;
      }
   }

   public static int getSpeedEffectAmplifier() {
      int amp = 0;
      if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
         amp = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
      }

      return amp;
   }

   public static double getBaseSpeed() {
      int amp = 0;
      if ((mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() : 0) > 0) {
         amp = getSpeedEffectAmplifier();
      }

      double base = 0.452;
      if (amp == 1) {
         base = 0.49720000000000003;
      } else if (amp >= 2) {
         base = 0.5424;
      }

      return base;
   }

   public static void setSpeed(double speed) {
      strafe(speed, getMotionYaw());
   }

   public static void resetTimer() {
      if (mc.thePlayer != null) {
         ReflectionUtil.getTimer(mc).timerSpeed = 1.0F;
      }
   }

   public static void updateAirTicks() {
      if (mc.thePlayer == null) {
         airTicks = 0;
      } else if (mc.thePlayer.onGround) {
         airTicks = 0;
      } else {
         airTicks++;
      }
   }

   public static float getMotionYaw() {
      return getMovementSpeed() == 0.0
         ? MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)
         : MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90.0F);
   }

   public static void strafe(double speed, float yaw) {
      mc.thePlayer.motionX = -Math.sin(Math.toRadians((double)yaw)) * speed;
      mc.thePlayer.motionZ = Math.cos(Math.toRadians((double)yaw)) * speed;
   }

   public static double[] getForwardAndStrafeMotion() {
      float strafe = (float)getStrafeInput() * 0.98F;
      float forward = (float)getForwardInput() * 0.98F;
      float len;
      if ((len = strafe * strafe + forward * forward) >= 1.0E-4F) {
         if ((len = MathHelper.sqrt_float(len)) < 1.0F) {
            len = 1.0F;
         }

         float friction = mc.thePlayer
               .worldObj
               .getBlockState(
                  new BlockPos(
                     MathHelper.floor_double(mc.thePlayer.posX),
                     MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1,
                     MathHelper.floor_double(mc.thePlayer.posZ)
                  )
               )
               .getBlock()
               .slipperiness
            * 0.91F;
         len = mc.thePlayer.getAIMoveSpeed() * (0.16277136F / (friction * friction * friction)) / len;
         friction = MathHelper.sin(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
         float cos = MathHelper.cos(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
         strafe *= len;
         forward *= len;
         return new double[]{(double)(strafe * cos - forward * friction), (double)(forward * cos + strafe * friction)};
      } else {
         return new double[]{0.0, 0.0};
      }
   }

   public static float getMoveYaw() {
      return calculateMoveYaw(
         MovementManager.isRotated() ? MovementManager.getMovementYaw() : mc.thePlayer.rotationYaw,
         mc.thePlayer.movementInput.moveForward,
         mc.thePlayer.movementInput.moveStrafe
      );
   }

   public static double getMovementSpeed() {
      return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
   }

   public static int getForwardInput() {
      int forward = 0;
      if (mc.gameSettings.keyBindForward.isKeyDown()) {
         forward++;
      }

      if (mc.gameSettings.keyBindBack.isKeyDown()) {
         forward--;
      }

      return forward;
   }

   public static int getAirTicks() {
      return airTicks;
   }

   public static boolean isMoving() {
      return mc.gameSettings.keyBindForward.isKeyDown() != mc.gameSettings.keyBindBack.isKeyDown()
         ? true
         : mc.gameSettings.keyBindLeft.isKeyDown() != mc.gameSettings.keyBindRight.isKeyDown();
   }
}
