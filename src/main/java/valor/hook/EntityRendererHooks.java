package valor.hook;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import valor.Valor;
import valor.event.EventManager;
import valor.events.PickEvent;
import valor.events.RaytraceEvent;
import valor.events.Render3DEvent;
import valor.module.player.AntiDebuff;
import valor.module.combat.AutoClicker;
import valor.module.combat.KillAura;
import valor.module.world.Scaffold;
import valor.module.world.AutoBlockIn;
import valor.module.player.GhostHand;
import valor.module.render.NoHurtCam;
import valor.util.ItemUtil;
import valor.util.KeyUtil;
import valor.util.ReflectionUtil;

public class EntityRendererHooks {
   private static Integer savedCurrentItem = null;
   private static ItemStack savedItemInUse = null;
   private static Integer savedItemInUseCount = null;
   private static boolean isItemInUseSaved = false;
   private static Float savedSwingProgress = null;
   private static Float savedPrevSwingProgress = null;
   private static boolean isSwingProgressSaved = false;
   private static boolean asmRender3DActive = false;
   private static boolean render3DFiredThisPass = false;
   private static boolean asmRender2DActive = false;
   private static float lastR3DPartialTicks = -1.0F;
   private static boolean mouseOverLockActive = false;
   private static float mouseOverLockYaw = 0.0F;
   private static float mouseOverLockPitch = 0.0F;
   private static float savedMouseOverYaw = 0.0F;
   private static float savedMouseOverPitch = 0.0F;
   private static float savedMouseOverPrevYaw = 0.0F;
   private static float savedMouseOverPrevPitch = 0.0F;
   private static boolean freeLookActive = false;
   private static float freeLookOrigYaw = 0.0F;
   private static float freeLookOrigPitch = 0.0F;
   private static float freeLookYaw = 0.0F;
   private static float freeLookPitch = 0.0F;

   public static void setMouseOverLock(float var0, float var1, boolean var2) {
      mouseOverLockActive = var2;
      mouseOverLockYaw = var0;
      mouseOverLockPitch = var1;
   }

   public static void startFreeLook(float var0, float var1) {
      freeLookActive = true;
      freeLookOrigYaw = var0;
      freeLookOrigPitch = var1;
      freeLookYaw = var0;
      freeLookPitch = var1;
   }

   public static void stopFreeLook() {
      freeLookActive = false;
   }

   public static boolean isAsmRender3DActive() {
      return asmRender3DActive;
   }

   public static boolean isAsmRender2DActive() {
      return asmRender2DActive;
   }

   public static void setLastR3DPartialTicks(float var0) {
      lastR3DPartialTicks = var0;
   }

   public static float getLastR3DPartialTicks() {
      return lastR3DPartialTicks;
   }

   public static void setAsmRender3DActive(boolean var0) {
      asmRender3DActive = var0;
   }

   public static void setAsmRender2DActive(boolean var0) {
      asmRender2DActive = var0;
   }

   public static double onRaytraceRange(double var0) {
      RaytraceEvent var2;
      EventManager.call(var2 = new RaytraceEvent(var0));
      return var2.getRange();
   }

   public static void onRenderWorldPass(int var0, float var1) {
      if (!Valor.isBadlion && var0 == 2 && !render3DFiredThisPass) {
         render3DFiredThisPass = true;
         asmRender3DActive = true;
         boolean var12 = !Valor.isSrg;
         boolean var2 = GL11.glIsEnabled(2929);
         boolean var3 = GL11.glIsEnabled(3042);
         boolean var4 = GL11.glIsEnabled(3553);
         boolean var5 = GL11.glIsEnabled(2884);
         boolean var6 = GL11.glIsEnabled(3008);
         boolean var7 = GL11.glIsEnabled(2896);
         boolean var8 = GL11.glGetBoolean(2930);
         int var9 = GL11.glGetInteger(2976);
         if (var12) {
            GL11.glPushAttrib(1048575);
         }

         GL11.glMatrixMode(5889);
         GL11.glPushMatrix();
         GL11.glMatrixMode(5888);
         GL11.glPushMatrix();

         try {
            GL11.glDisable(32823);
            GL11.glPolygonOffset(1.0F, 0.0F);
            EventManager.call(new Render3DEvent(var1));
         } finally {
            GuiIngameHooks.hideVanillaScoreboard();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glMatrixMode(5888);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5889);
            GL11.glPopMatrix();
            GL11.glMatrixMode(var9);
            if (var12) {
               GL11.glPopAttrib();
            }
         }

         if (var12) {
            if (var2) {
               GlStateManager.enableDepth();
            } else {
               GlStateManager.disableDepth();
            }

            if (var3) {
               GlStateManager.enableBlend();
            } else {
               GlStateManager.disableBlend();
            }

            if (var4) {
               GlStateManager.enableTexture2D();
            } else {
               GlStateManager.disableTexture2D();
            }

            if (var5) {
               GlStateManager.enableCull();
            } else {
               GlStateManager.disableCull();
            }

            if (var6) {
               GlStateManager.enableAlpha();
            } else {
               GlStateManager.disableAlpha();
            }

            if (var7) {
               GlStateManager.enableLighting();
            } else {
               GlStateManager.disableLighting();
            }

            GlStateManager.depthMask(var8);
         } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.disableLighting();
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableCull();
            GlStateManager.enableAlpha();
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDepthFunc(515);
            GL11.glDisable(32823);
            if (Valor.isSrg || Valor.isBadlion) {
               GlStateManager.disableTexture2D();
               GlStateManager.enableTexture2D();
               GlStateManager.disableDepth();
               GlStateManager.enableDepth();
               GlStateManager.enableBlend();
               GlStateManager.disableBlend();
               GlStateManager.enableLighting();
               GlStateManager.disableLighting();
               GlStateManager.disableCull();
               GlStateManager.enableCull();
               GlStateManager.disableAlpha();
               GlStateManager.enableAlpha();
               GlStateManager.depthMask(false);
               GlStateManager.depthMask(true);
            }
         }
      }
   }

   public static Material onSetupFogBlock(Block var0) {
      return var0.getMaterial();
   }

   public static void postGetMouseOver(Minecraft var0) {
      if (mouseOverLockActive && var0 != null && var0.thePlayer != null) {
         var0.thePlayer.rotationYaw = savedMouseOverYaw;
         var0.thePlayer.rotationPitch = savedMouseOverPitch;
         var0.thePlayer.prevRotationYaw = savedMouseOverPrevYaw;
         var0.thePlayer.prevRotationPitch = savedMouseOverPrevPitch;
      }
   }

   public static void postUpdateRenderer(Minecraft var0) {
      if (savedCurrentItem != null && var0 != null && var0.thePlayer != null && var0.thePlayer.inventory != null) {
         var0.thePlayer.inventory.currentItem = savedCurrentItem;
         savedCurrentItem = null;
      }
   }

   public static void preUpdateRenderer(Minecraft var0) {
      if (var0 != null && var0.thePlayer != null && var0.thePlayer.inventory != null && Valor.moduleManager != null && Valor.moduleManager.modules != null) {
         Scaffold var1 = (Scaffold)Valor.moduleManager.modules.get(Scaffold.class);
         if (var1 != null && var1.isEnabled()) {
            int var2;
            if (var1.itemSpoofProp != null && Boolean.TRUE.equals(var1.itemSpoofProp.getValue()) && (var2 = var1.blockSlot) >= 0) {
               savedCurrentItem = var0.thePlayer.inventory.currentItem;
               var0.thePlayer.inventory.currentItem = var2;
            }
         }

         AutoBlockIn var3 = (AutoBlockIn)Valor.moduleManager.modules.get(AutoBlockIn.class);
         if (var3 != null && var3.isEnabled()) {
            int var4;
            if (var3.itemSpoofProp != null && Boolean.TRUE.equals(var3.itemSpoofProp.getValue()) && (var4 = var3.blockSlot) >= 0) {
               savedCurrentItem = var0.thePlayer.inventory.currentItem;
               var0.thePlayer.inventory.currentItem = var4;
            }
         }
      }
   }

   public static void preGetMouseOver(Minecraft var0) {
      if (mouseOverLockActive && var0 != null && var0.thePlayer != null) {
         savedMouseOverYaw = var0.thePlayer.rotationYaw;
         savedMouseOverPitch = var0.thePlayer.rotationPitch;
         savedMouseOverPrevYaw = var0.thePlayer.prevRotationYaw;
         savedMouseOverPrevPitch = var0.thePlayer.prevRotationPitch;
         var0.thePlayer.rotationYaw = mouseOverLockYaw;
         var0.thePlayer.rotationPitch = mouseOverLockPitch;
         var0.thePlayer.prevRotationYaw = mouseOverLockYaw;
         var0.thePlayer.prevRotationPitch = mouseOverLockPitch;
      }
   }

   public static void onGetMouseOverEntities(List<Entity> var0) {
      GhostHand var1;
      if (Valor.moduleManager != null && (var1 = (GhostHand)Valor.moduleManager.modules.get(GhostHand.class)).isEnabled()) {
         var0.removeIf(var1::shouldIgnoreEntity);
      }
   }

   public static void postUpdateCameraAndRender(Minecraft var0) {
      GuiIngameHooks.restoreVanillaScoreboard();
      if (freeLookActive && var0.thePlayer != null) {
         var0.thePlayer.rotationYaw = freeLookOrigYaw;
         var0.thePlayer.rotationPitch = freeLookOrigPitch;
      }

      if (savedCurrentItem != null) {
         var0.thePlayer.inventory.currentItem = savedCurrentItem;
         savedCurrentItem = null;
      }

      if (isItemInUseSaved) {
         ReflectionUtil.setItemInUse(var0.thePlayer, savedItemInUse);
         ReflectionUtil.setItemInUseCount((EntityPlayer)var0.thePlayer, savedItemInUseCount != null ? savedItemInUseCount : 0);
         savedItemInUse = null;
         savedItemInUseCount = null;
         isItemInUseSaved = false;
      }

      if (isSwingProgressSaved) {
         ReflectionUtil.setSwingProgress((EntityLivingBase)var0.thePlayer, savedSwingProgress != null ? savedSwingProgress : 0.0F);
         ReflectionUtil.setPrevSwingProgress((EntityLivingBase)var0.thePlayer, savedPrevSwingProgress != null ? savedPrevSwingProgress : 0.0F);
         savedSwingProgress = null;
         savedPrevSwingProgress = null;
         isSwingProgressSaved = false;
      }

      if (!Valor.isBadlion && var0.theWorld != null && var0.thePlayer != null) {
         try {
            GuiIngameHooks.renderHud(ReflectionUtil.getTimer(var0).renderPartialTicks);
         } catch (Throwable var1) {
            System.out.println("[Valor] Render2D end-of-frame error: ".concat(String.valueOf(var1)));
         }
      }

      asmRender2DActive = false;
   }

   public static void preUpdateCameraAndRender(Minecraft var0) {
      GuiIngameHooks.hideVanillaScoreboard();
      MinecraftHooks.pollGuiHotkey(var0);
      asmRender3DActive = false;
      render3DFiredThisPass = false;
      asmRender2DActive = false;
      GuiIngameHooks.beginHudFrame();
      if (freeLookActive && var0.thePlayer != null) {
         float var1 = MathHelper.wrapAngleTo180_float(var0.thePlayer.rotationYaw - freeLookOrigYaw);
         float var2 = var0.thePlayer.rotationPitch - freeLookOrigPitch;
         freeLookYaw += var1;
         freeLookPitch = MathHelper.clamp_float(freeLookPitch + var2, -90.0F, 90.0F);
         var0.thePlayer.rotationYaw = freeLookYaw;
         var0.thePlayer.rotationPitch = freeLookPitch;
      }

      if (var0 != null && var0.thePlayer != null && var0.thePlayer.inventory != null && Valor.moduleManager != null && Valor.moduleManager.modules != null) {
         Scaffold var3 = (Scaffold)Valor.moduleManager.modules.get(Scaffold.class);
         if (var3 != null && var3.isEnabled() && var3.itemSpoofProp != null && Boolean.TRUE.equals(var3.itemSpoofProp.getValue())) {
            int var5 = var3.blockSlot;
            if (var5 >= 0) {
               savedCurrentItem = var0.thePlayer.inventory.currentItem;
               var0.thePlayer.inventory.currentItem = var5;
            }
         }

         KillAura var6;
         if ((var6 = (KillAura)Valor.moduleManager.modules.get(KillAura.class)).isEnabled() && var6.isRenderBlocking && ItemUtil.isHoldingSword()) {
            if (!isItemInUseSaved) {
               savedItemInUse = ReflectionUtil.getItemInUse(var0.thePlayer);
               savedItemInUseCount = ReflectionUtil.getItemInUseCount((EntityPlayer)var0.thePlayer);
               isItemInUseSaved = true;
            }

            ReflectionUtil.setItemInUse(var0.thePlayer, var0.thePlayer.inventory.getCurrentItem());
            ReflectionUtil.setItemInUseCount((EntityPlayer)var0.thePlayer, 69000);
         }

         AutoClicker var4;
         if ((var4 = (AutoClicker)Valor.moduleManager.modules.get(AutoClicker.class)).isEnabled() && var4.forceBlockAnimProp.getValue() && ItemUtil.isHoldingSword()
            ? KeyUtil.isKeyDown(AutoClicker.mc.gameSettings.keyBindAttack.getKeyCode())
            : false) {
            if (!isItemInUseSaved) {
               savedItemInUse = ReflectionUtil.getItemInUse(var0.thePlayer);
               savedItemInUseCount = ReflectionUtil.getItemInUseCount((EntityPlayer)var0.thePlayer);
               isItemInUseSaved = true;
            }

            ReflectionUtil.setItemInUse(var0.thePlayer, var0.thePlayer.inventory.getCurrentItem());
            ReflectionUtil.setItemInUseCount((EntityPlayer)var0.thePlayer, 69000);
            if (!isSwingProgressSaved) {
               savedSwingProgress = ReflectionUtil.getSwingProgress(var0.thePlayer);
               savedPrevSwingProgress = ReflectionUtil.getPrevSwingProgress((EntityLivingBase)var0.thePlayer);
               isSwingProgressSaved = true;
            }

            ReflectionUtil.setSwingProgress((EntityLivingBase)var0.thePlayer, 0.0F);
            ReflectionUtil.setPrevSwingProgress((EntityLivingBase)var0.thePlayer, 0.0F);
         }
      }
   }

   public static double onOrientCamera(Vec3 var0, Vec3 var1, float var2) {
      return var0.distanceTo(var1);
   }

   public static double onPickRange(double var0) {
      PickEvent var2;
      EventManager.call(var2 = new PickEvent(var0));
      return var2.getRange();
   }

   public static float onHurtCam(float var0) {
      if (Valor.moduleManager == null) {
         return var0;
      } else {
         NoHurtCam var1;
         return (var1 = (NoHurtCam)Valor.moduleManager.modules.get(NoHurtCam.class)).isEnabled()
            ? var0 * (float)((Integer)var1.multiplierProp.getValue()).intValue() / 100.0F
            : var0;
      }
   }

   public static boolean onNauseaCheck(EntityPlayerSP var0, Potion var1) {
      AntiDebuff var2;
      return var1 == Potion.confusion
            && Valor.moduleManager != null
            && (var2 = (AntiDebuff)Valor.moduleManager.modules.get(AntiDebuff.class)).isEnabled()
            && var2.nauseaProp.getValue()
         ? false
         : ReflectionUtil.getActivePotionsMap(var0).containsKey(var1.id);
   }

   public static boolean onBlindnessCheck(EntityLivingBase var0, Potion var1) {
      AntiDebuff var2;
      return var1 == Potion.blindness
            && Valor.moduleManager != null
            && (var2 = (AntiDebuff)Valor.moduleManager.modules.get(AntiDebuff.class)).isEnabled()
            && var2.blindnessProp.getValue()
         ? false
         : ReflectionUtil.getActivePotionsMap(var0).containsKey(var1.id);
   }

   static {
      System.currentTimeMillis();
   }
}
