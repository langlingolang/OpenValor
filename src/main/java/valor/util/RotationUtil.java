package valor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class RotationUtil {
   public static final Minecraft mc = Minecraft.getMinecraft();

   public static float clampAngle(float angle, float maxTurn) {
      maxTurn = Math.max(0.0F, Math.min(180.0F, maxTurn));
      if (angle > maxTurn) {
         angle = maxTurn;
      } else if (angle < -maxTurn) {
         angle = -maxTurn;
      }
      return angle;
   }

   public static float applySmoothing(float delta, float factor) {
      return delta * (0.5F + 0.5F * (1.0F - Math.max(0.0F, Math.min(1.0F, factor + RandomUtil.nextFloat(-0.1F, 0.1F)))));
   }

   public static float interpolateAngle(float current, float target) {
      return target + MathHelper.wrapAngleTo180_float(current - target);
   }

   public static MovingObjectPosition rayTrace(float yaw, float pitch, double distance) {
      Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
      Vec3 look = ReflectionUtil.getVectorForRotation(mc.thePlayer, pitch, yaw);
      Vec3 end = eyes.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);
      return mc.theWorld.rayTraceBlocks(eyes, end);
   }

   public static float getMouseGCD() {
      float sens = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
      return sens * sens * sens * 8.0F * 0.15F;
   }

   public static float getAngleToEntity(Entity entity) {
      Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
      float borderSize = entity.getCollisionBorderSize();
      if (entity.getEntityBoundingBox().expand((double) borderSize, (double) borderSize, (double) borderSize).isVecInside(eyes)) {
         return 0.0F;
      } else {
         double dx = entity.posX - eyes.xCoord;
         double dz = entity.posZ - eyes.zCoord;
         return Math.abs(
            MathHelper.wrapAngleTo180_float((float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F - mc.thePlayer.rotationYaw)
         ) * 2.0F;
      }
   }

   public static double getDistanceToBox(AxisAlignedBB bb, Vec3 eyes) {
      if (bb.isVecInside(eyes)) {
         return 0.0;
      } else {
         Vec3 closest = getClosestPointOnAABB(eyes, bb);
         double dx = closest.xCoord - eyes.xCoord;
         double dy = closest.yCoord - eyes.yCoord;
         double dz = closest.zCoord - eyes.zCoord;
         return Math.sqrt(dx * dx + dy * dy + dz * dz);
      }
   }

   public static double getDistanceToEntityBox(Entity entity, Vec3 eyes) {
      float borderSize = entity.getCollisionBorderSize();
      return getDistanceToBox(entity.getEntityBoundingBox().expand((double) borderSize, (double) borderSize, (double) borderSize), eyes);
   }

   public static double getDistanceToBoxEyes(AxisAlignedBB bb) {
      return getDistanceToBox(bb, mc.thePlayer.getPositionEyes(1.0F));
   }

   public static float getYawTo(double fromX, double fromZ, double toX, double toZ) {
      return MathHelper.wrapAngleTo180_float((float) (Math.atan2(toZ - fromZ, toX - fromX) * 180.0 / Math.PI) - 90.0F - mc.thePlayer.rotationYaw);
   }

   public static MovingObjectPosition rayTraceToEntity(Entity entity) {
      Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
      float borderSize = entity.getCollisionBorderSize();
      Vec3 target = getClosestPointOnAABB(eyes, entity.getEntityBoundingBox().expand((double) borderSize, (double) borderSize, (double) borderSize));
      return mc.theWorld.rayTraceBlocks(eyes, target);
   }

   public static MovingObjectPosition calculateBoxIntercept(AxisAlignedBB bb, float yaw, float pitch, double distance) {
      Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
      Vec3 look = ReflectionUtil.getVectorForRotation(mc.thePlayer, pitch, yaw);
      Vec3 end = eyes.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);
      return bb.calculateIntercept(eyes, end);
   }

   public static float[] applyGCD(float currentYaw, float currentPitch, float targetYaw, float targetPitch) {
      float gcd = getMouseGCD();
      float deltaYaw = targetYaw - currentYaw;
      float deltaPitch = targetPitch - currentPitch;
      return new float[]{
         currentYaw + (float) Math.round(deltaYaw / gcd) * gcd,
         currentPitch + (float) Math.round(deltaPitch / gcd) * gcd
      };
   }

   public static float[] getRotationsToBox(AxisAlignedBB bb, float currentYaw, float currentPitch, float maxTurn, float smoothing) {
      Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
      double minY = bb.minY + 0.05 * (bb.maxY - bb.minY);
      double maxY = bb.minY + 0.75 * (bb.maxY - bb.minY);
      double dx = (bb.minX + bb.maxX) / 2.0 - eyes.xCoord;
      double dy = eyes.yCoord >= maxY ? maxY - eyes.yCoord : (eyes.yCoord <= minY ? minY - eyes.yCoord : 0.0);
      double dz = (bb.minZ + bb.maxZ) / 2.0 - eyes.zCoord;
      return getSmoothRotations(dx, dy, dz, currentYaw, currentPitch, maxTurn, smoothing);
   }

   public static float[] getRotations(double dx, double dy, double dz, float currentYaw, float currentPitch) {
      return getSmoothRotations(dx, dy, dz, currentYaw, currentPitch, 180.0F, 0.0F);
   }

   public static double getDistanceToTarget(Entity entity) {
      float borderSize = entity.getCollisionBorderSize();
      return getDistanceToBoxEyes(entity.getEntityBoundingBox().expand((double) borderSize, (double) borderSize, (double) borderSize));
   }

   public static Vec3 getClosestPointOnAABB(Vec3 point, AxisAlignedBB bb) {
      double[] coords = new double[]{point.xCoord, point.yCoord, point.zCoord};
      double[] min = new double[]{bb.minX, bb.minY, bb.minZ};
      double[] max = new double[]{bb.maxX, bb.maxY, bb.maxZ};

      for (int i = 0; i < 3; i++) {
         if (coords[i] > max[i]) {
            coords[i] = max[i];
         } else if (coords[i] < min[i]) {
            coords[i] = min[i];
         }
      }

      return new Vec3(coords[0], coords[1], coords[2]);
   }

   public static float[] getSmoothRotations(double dx, double dy, double dz, float currentYaw, float currentPitch, float maxTurn, float smoothing) {
      double distXZ = Math.sqrt(dx * dx + dz * dz);
      float targetYaw = MathHelper.wrapAngleTo180_float((float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F - currentYaw);
      float targetPitch = MathHelper.wrapAngleTo180_float((float) (-Math.atan2(dy, distXZ) * 180.0 / Math.PI) - currentPitch);
      float smoothedYaw = Math.abs(targetYaw) <= 1.0F ? 0.0F : applySmoothing(clampAngle(targetYaw, maxTurn), smoothing);
      float smoothedPitch = Math.abs(targetPitch) <= 1.0F ? 0.0F : applySmoothing(clampAngle(targetPitch, maxTurn), smoothing);
      return new float[]{roundRotation(currentYaw + smoothedYaw), roundRotation(currentPitch + smoothedPitch)};
   }

   public static float roundRotation(float angle) {
      return (float) ((double) angle - (double) angle % 0.0096F);
   }
}
