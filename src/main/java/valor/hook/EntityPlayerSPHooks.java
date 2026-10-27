package valor.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import valor.Valor;
import valor.core.MovementManager;
import valor.event.EventManager;
import valor.event.types.EventType;
import valor.events.LivingUpdateEvent;
import valor.events.MoveInputEvent;
import valor.events.PlayerUpdateEvent;
import valor.events.UpdateEvent;
import valor.module.player.AntiDebuff;
import valor.module.movement.NoSlow;
import valor.util.MoveUtil;
import valor.util.ReflectionUtil;

public class EntityPlayerSPHooks {
   private static float targetYaw = Float.NaN;
   private static float targetPitch = Float.NaN;
   private static float originalYaw = Float.NaN;
   private static float originalPitch = Float.NaN;
   private static int lastPreUpdateTicks = -1;
   private static int lastPostUpdateTicks = -1;
   public static boolean freeLookBypass = false;
   private static int lastMotionUpdateTicks = -1;
   private static int lastLivingUpdateTicks = -1;
   private static int lastMoveInputTicks = -1;

   public static void applyOverrideRotation(EntityPlayerSP player) {
      if (!Float.isNaN(targetYaw) && !Float.isNaN(targetPitch)) {
         player.rotationYaw = targetYaw;
         player.rotationPitch = targetPitch;
      }
   }

   public static void onMotionUpdate() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer != null) {
         int ticks = mc.thePlayer.ticksExisted;
         if (ticks != lastMotionUpdateTicks) {
            lastMotionUpdateTicks = ticks;
            EventManager.call(new PlayerUpdateEvent());
         }
      }
   }

   public static boolean isAsmActive() {
      return true;
   }

   public static void onLivingUpdate() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer != null) {
         int ticks = mc.thePlayer.ticksExisted;
         if (ticks != lastLivingUpdateTicks) {
            lastLivingUpdateTicks = ticks;
            MoveUtil.updateAirTicks();
            EventManager.call(new LivingUpdateEvent());
         }
      }
   }

   public static boolean isUsingItem(EntityPlayerSP player) {
      if (Valor.moduleManager == null) {
         return player.isUsingItem();
      } else {
         NoSlow noSlow = (NoSlow)Valor.moduleManager.modules.get(NoSlow.class);
         return (!noSlow.isEnabled() || !noSlow.shouldPreventSlowdown() || NoSlow.isKillAuraActive() && noSlow.isBlockingSword()) && player.isUsingItem();
      }
   }

   public static void onMoveInput() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer != null) {
         int ticks = mc.thePlayer.ticksExisted;
         if (ticks != lastMoveInputTicks) {
            lastMoveInputTicks = ticks;
            EventManager.call(new MoveInputEvent());
         }
      }
   }

   public static void onUpdatePre(EntityPlayerSP player) {
      int ticks = player.ticksExisted;
      if (player.ticksExisted != lastPreUpdateTicks) {
         lastPreUpdateTicks = ticks;

         try {
            if (player.worldObj.isBlockLoaded(new BlockPos(player.posX, 0.0, player.posZ))) {
               float currentYaw = player.rotationYaw;
               float currentPitch = player.rotationPitch;
               float lastYaw = ReflectionUtil.getLastReportedYaw(player);
               float lastPitch = ReflectionUtil.getLastReportedPitch(player);
               UpdateEvent updateEvent;
               EventManager.call(updateEvent = new UpdateEvent(EventType.PRE, lastYaw, lastPitch, currentYaw, currentPitch));
               MovementManager.updateRotations(updateEvent.isRotated() && !player.isRiding(), updateEvent.getNewYaw(), updateEvent.getNewPitch(), updateEvent.getPreYaw(), updateEvent.isRotating());
               if (updateEvent.isRotated()) {
                  originalYaw = currentYaw;
                  originalPitch = currentPitch;
                  targetYaw = updateEvent.getNewYaw();
                  targetPitch = updateEvent.getNewPitch();
               } else {
                  originalYaw = Float.NaN;
                  originalPitch = Float.NaN;
                  targetYaw = Float.NaN;
                  targetPitch = Float.NaN;
               }

               if (freeLookBypass) {
                  originalYaw = Float.NaN;
                  originalPitch = Float.NaN;
                  targetYaw = Float.NaN;
                  targetPitch = Float.NaN;
               }
            }
         } catch (Throwable t) {
            System.out.println("[ValorRot] onUpdatePre ERROR: ".concat(String.valueOf(t)));
            t.printStackTrace();
         }
      }
   }

   public static void onWalkingPlayerPre(EntityPlayerSP player) {
      applyOverrideRotation(player);
      onMotionUpdate();
   }

   public static boolean checkPotion(EntityPlayerSP player, Potion potion) {
      if (potion == Potion.confusion && Valor.moduleManager != null) {
         AntiDebuff antiDebuff = (AntiDebuff)Valor.moduleManager.modules.get(AntiDebuff.class);
         if (antiDebuff.isEnabled() && antiDebuff.nauseaProp.getValue()) {
            return false;
         }
      }

      return ReflectionUtil.getActivePotionsMap(player).containsKey(potion.id);
   }

   public static void onUpdatePost(EntityPlayerSP player) {
      int ticks = player.ticksExisted;
      if (player.ticksExisted != lastPostUpdateTicks) {
         lastPostUpdateTicks = ticks;
         if (player.worldObj.isBlockLoaded(new BlockPos(player.posX, 0.0, player.posZ))) {
            if (!Float.isNaN(originalYaw) && !Float.isNaN(originalPitch)) {
               ReflectionUtil.setLastReportedYaw(player, player.rotationYaw);
               ReflectionUtil.setLastReportedPitch(player, player.rotationPitch);
               player.rotationYaw = player.rotationYaw + MathHelper.wrapAngleTo180_float(originalYaw - player.rotationYaw);
               player.rotationPitch = originalPitch;
               player.prevRotationYaw = player.rotationYaw;
               player.prevRotationPitch = player.rotationPitch;
               ReflectionUtil.setPrevRenderArmYaw(player, player.rotationYaw - (ReflectionUtil.getRenderArmYaw(player) - ReflectionUtil.getPrevRenderArmYaw(player)) * 2.0F);
               ReflectionUtil.setRenderArmYaw(player, player.rotationYaw);
               originalYaw = Float.NaN;
               originalPitch = Float.NaN;
               targetYaw = Float.NaN;
               targetPitch = Float.NaN;
            }

            EventManager.call(new UpdateEvent(EventType.POST, ReflectionUtil.getLastReportedYaw(player), ReflectionUtil.getLastReportedPitch(player), player.rotationYaw, player.rotationPitch));
         }
      }
   }
}
