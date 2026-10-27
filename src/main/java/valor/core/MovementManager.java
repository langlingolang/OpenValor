package valor.core;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class MovementManager {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static int rotationTicks = -1;
   private static float prevRenderYawOffset;
   private static float renderYawOffset;
   private static float prevRotationYawHead;
   private static float rotationYawHead;
   private static float prevRotationPitch;
   private static float rotationPitch;
   private static float movementYaw;
   private static int rotationPriority;

   public static float getRotationPitch() {
      return rotationPitch;
   }

   public static float getPriority() {
      return (float)rotationPriority;
   }

   public static float getRenderYawOffset() {
      return renderYawOffset;
   }

   public static float getMovementYaw() {
      return movementYaw;
   }

   public static float getPrevRotationPitch() {
      return prevRotationPitch;
   }

   public static boolean isRotated() {
      return isRotatedWithin(0);
   }

   public static float getPrevRotationYawHead() {
      return prevRotationYawHead;
   }

   public static void updateRotations(boolean rotated, float yaw, float pitch, float preYaw, int priority) {
      rotationTicks = rotated ? 0 : rotationTicks + 1;
      prevRenderYawOffset = renderYawOffset;
      float calculatedYaw;
      if (rotated) {
         float currentOffset = renderYawOffset;
         float targetYaw = currentOffset;
         double deltaX = mc.thePlayer.posX - mc.thePlayer.prevPosX;
         double deltaZ = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
         if ((float)(deltaX * deltaX + deltaZ * deltaZ) > 0.0025000002F) {
            targetYaw = (float)MathHelper.atan2(deltaZ, deltaX) * 180.0F / (float) Math.PI - 90.0F;
         }

         if (mc.thePlayer.swingProgress > 0.0F) {
            targetYaw = yaw;
         }

         targetYaw = MathHelper.wrapAngleTo180_float(targetYaw - currentOffset);
         if ((currentOffset = MathHelper.wrapAngleTo180_float(yaw - (currentOffset + targetYaw * 0.3F))) < -75.0F) {
            currentOffset = -75.0F;
         }

         if (currentOffset >= 75.0F) {
            currentOffset = 75.0F;
         }

         targetYaw = yaw - currentOffset;
         if (currentOffset * currentOffset > 2500.0F) {
            targetYaw += currentOffset * 0.2F;
         }

         calculatedYaw = targetYaw;
      } else {
         calculatedYaw = mc.thePlayer.renderYawOffset;
      }

      renderYawOffset = calculatedYaw;
      prevRotationYawHead = rotationYawHead;
      rotationYawHead = rotated ? yaw : mc.thePlayer.rotationYawHead;
      prevRotationPitch = rotationPitch;
      rotationPitch = rotated ? pitch : mc.thePlayer.rotationPitch;
      movementYaw = preYaw;
      rotationPriority = priority;
   }

   public static float getPrevRenderYawOffset() {
      return prevRenderYawOffset;
   }

   public static boolean isRotatedWithin(int thresholdTicks) {
      return rotationTicks >= 0 && rotationTicks <= thresholdTicks;
   }

   public static float getRotationYawHead() {
      return rotationYawHead;
   }
}
