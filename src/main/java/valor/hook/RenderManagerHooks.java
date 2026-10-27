package valor.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import valor.core.MovementManager;
import valor.event.EventManager;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.util.ReflectionUtil;

public class RenderManagerHooks {
   private static float origPrevRenderYawOffset;
   private static float origRenderYawOffset;
   private static float origPrevRotationYawHead;
   private static float origRotationYawHead;
   private static float origPrevRotationPitch;
   private static float origRotationPitch;
   private static float lastPartialTicks = -1.0F;
   private static int lastRenderInfoTicks = -1;

   public static void onPostRenderEntities(float partialTicks) {
      Minecraft mc;
      if ((mc = Minecraft.getMinecraft()) != null && mc.theWorld != null && mc.thePlayer != null) {
         try {
            ReflectionUtil.getTimer(mc).renderPartialTicks = partialTicks;
         } catch (Exception ignored) {
         }
      }
   }

   public static float getLastPartialTicks() {
      return lastPartialTicks;
   }

   public static void onCacheActiveRenderInfo(float partialTicks) {
      Minecraft mc;
      if ((mc = Minecraft.getMinecraft()) != null && mc.theWorld != null && mc.thePlayer != null) {
         int ticks = mc.thePlayer.ticksExisted;
         if (mc.thePlayer.ticksExisted != lastRenderInfoTicks) {
            lastRenderInfoTicks = ticks;

            try {
               EntityPlayerSPHooks.onUpdatePost(mc.thePlayer);
            } catch (Throwable ignored) {
            }

            try {
               EventManager.call(new TickEvent(EventType.POST));
               return;
            } catch (Throwable ignored) {
            }
         }
      }
   }

   public static void preRenderEntityStatic(Entity entity) {
      if (entity instanceof EntityPlayerSP && MovementManager.isRotatedWithin(1)) {
         EntityPlayerSP player = (EntityPlayerSP)entity;
         origPrevRenderYawOffset = player.prevRenderYawOffset;
         origRenderYawOffset = player.renderYawOffset;
         origPrevRotationYawHead = player.prevRotationYawHead;
         origRotationYawHead = player.rotationYawHead;
         origPrevRotationPitch = player.prevRotationPitch;
         origRotationPitch = player.rotationPitch;
         player.prevRenderYawOffset = MovementManager.getPrevRenderYawOffset();
         player.renderYawOffset = MovementManager.getRenderYawOffset();
         player.prevRotationYawHead = MovementManager.getPrevRotationYawHead();
         player.rotationYawHead = MovementManager.getRotationYawHead();
         player.prevRotationPitch = MovementManager.getPrevRotationPitch();
         player.rotationPitch = MovementManager.getRotationPitch();
      }

      if (entity instanceof EntityLivingBase) {
         try {
            RendererLivingEntityHooks.onDoRenderPre((EntityLivingBase)entity);
            return;
         } catch (Throwable ignored) {
         }
      }
   }

   public static void postRenderEntityStatic(Entity entity) {
      if (entity instanceof EntityPlayerSP && MovementManager.isRotatedWithin(1)) {
         EntityPlayerSP player = (EntityPlayerSP)entity;
         player.prevRenderYawOffset = origPrevRenderYawOffset;
         player.renderYawOffset = origRenderYawOffset;
         player.prevRotationYawHead = origPrevRotationYawHead;
         player.rotationYawHead = origRotationYawHead;
         player.prevRotationPitch = origPrevRotationPitch;
         player.rotationPitch = origRotationPitch;
      }

      if (entity instanceof EntityLivingBase) {
         try {
            RendererLivingEntityHooks.onDoRenderPost((EntityLivingBase)entity);
            return;
         } catch (Throwable ignored) {
         }
      }
   }
}
