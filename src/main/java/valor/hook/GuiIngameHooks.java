package valor.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import valor.Valor;
import valor.event.EventManager;
import valor.events.Render2DEvent;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.world.Scaffold;
import valor.module.world.AutoBlockIn;
import valor.util.HudDragManager;
import valor.util.ReflectionUtil;
import valor.util.RenderUtil;
import valor.util.render.BlurPipeline;
import valor.util.render.GreenRender;

public class GuiIngameHooks {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static boolean hudRenderedThisFrame = false;
   private static Scoreboard savedScoreboard;
   private static final int[] savedObjectiveSlots = new int[2];
   private static final ScoreObjective[] savedObjectives = new ScoreObjective[2];
   private static int savedObjectiveCount;

   public static void renderHudAtStandaloneDisplay() {

      renderHudAtDisplay();
   }

   public static void renderHud(float var0) {
      restoreVanillaScoreboard();
      if (!hudRenderedThisFrame) {
         if (mc.theWorld != null && mc.thePlayer != null) {
            if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
               hudRenderedThisFrame = true;
            } else {
               hudRenderedThisFrame = true;
               EntityRendererHooks.setAsmRender2DActive(true);

               try {
                  if (mc.entityRenderer != null) {
                     mc.entityRenderer.setupOverlayRendering();
                  }

                  resetFixedPipeline();
                  GlStateManager.pushMatrix();
                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  GlStateManager.disableLighting();
                  GlStateManager.disableDepth();
                  GlStateManager.depthMask(false);
                  GlStateManager.disableAlpha();
                  GlStateManager.disableCull();
                  GlStateManager.enableTexture2D();
                  GlStateManager.enableBlend();
                  GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                  BlurPipeline.beginFrame();
                  RenderUtil.preparePipeline();
                  BlurPipeline.prepare();
                  resetFixedPipeline();
                  if (mc.entityRenderer != null) {
                     mc.entityRenderer.setupOverlayRendering();
                  }

                  GlStateManager.disableDepth();
                  GlStateManager.depthMask(false);
                  GlStateManager.enableBlend();
                  GreenRender.beginBatch();

                  try {
                     EventManager.call(new Render2DEvent(var0));
                  } finally {
                     GreenRender.endBatch();
                  }

                  HudDragManager.updateMouseState();
                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  GlStateManager.popMatrix();
                  resetFixedPipeline();
                  GlStateManager.enableDepth();
                  GlStateManager.depthMask(true);
                  GlStateManager.enableAlpha();
                  GlStateManager.enableCull();
                  GlStateManager.disableBlend();
                  GlStateManager.enableTexture2D();
                  GlStateManager.alphaFunc(516, 0.1F);
               } catch (Throwable var6) {
                  System.out.println("[Valor] renderHud error: ".concat(String.valueOf(var6)));
                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                  try {
                     GreenRender.endBatch();
                  } catch (Throwable var4) {
                  }
               }
            }
         }
      }
   }

   private static void resetFixedPipeline() {
      try {
         GL20.glUseProgram(0);
         GL15.glBindBuffer(34962, 0);
         GL13.glActiveTexture(33984);
         GlStateManager.bindTexture(0);
         GlStateManager.disableLighting();
         GlStateManager.enableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.colorMask(true, true, true, true);
         GlStateManager.disableTexture2D();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.enableBlend();
      } catch (Throwable var0) {
      }
   }

   public static void restoreVanillaScoreboard() {
      if (savedScoreboard != null) {
         for (int var0 = 0; var0 < savedObjectiveCount; var0++) {
            if (savedScoreboard.getObjectiveInDisplaySlot(savedObjectiveSlots[var0]) == null) {
               savedScoreboard.setObjectiveInDisplaySlot(savedObjectiveSlots[var0], savedObjectives[var0]);
            }

            savedObjectives[var0] = null;
         }

         savedObjectiveCount = 0;
         savedScoreboard = null;
      }
   }

   public static void hideVanillaScoreboard() {
      if (shouldHideScoreboard() && mc.theWorld != null) {
         if (savedScoreboard == null) {
            Scoreboard var0;
            if ((var0 = mc.theWorld.getScoreboard()) != null) {
               savedScoreboard = var0;
               saveAndClearObjective(var0, 1);
               ScorePlayerTeam var1;
               int var2;
               if (mc.thePlayer != null
                  && (var1 = var0.getPlayersTeam(mc.thePlayer.getName())) != null
                  && var1.getChatFormat() != null
                  && (var2 = var1.getChatFormat().getColorIndex()) >= 0) {
                  saveAndClearObjective(var0, var2 + 3);
               }

               if (savedObjectiveCount == 0) {
                  savedScoreboard = null;
               }
            }
         }
      }
   }

   public static void postRenderOverlay(float var0) {
      restoreVanillaScoreboard();
      EntityRendererHooks.setAsmRender2DActive(true);
   }

   public static void preRenderOverlay(float var0) {
      hideVanillaScoreboard();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.enableTexture2D();
      GlStateManager.enableDepth();
      GlStateManager.depthMask(true);
      GlStateManager.disableLighting();
      GlStateManager.colorMask(true, true, true, true);
      GlStateManager.alphaFunc(516, 0.1F);
      Minecraft var1 = Minecraft.getMinecraft();
      if (!EntityRendererHooks.isAsmRender3DActive()) {
         EntityRendererHooks.setAsmRender3DActive(true);

         try {
            if (var1.theWorld != null && var1.thePlayer != null) {
               RenderManager var2 = var1.getRenderManager();
               Entity var3 = var1.getRenderViewEntity();
               if (var2 != null && var3 != null) {
                  double var4 = var3.lastTickPosX + (var3.posX - var3.lastTickPosX) * (double)var0;
                  double var6 = var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double)var0;
                  double var8 = var3.lastTickPosZ + (var3.posZ - var3.lastTickPosZ) * (double)var0;
                  ReflectionUtil.setRenderPosX(var2, var4);
                  ReflectionUtil.setRenderPosY(var2, var6);
                  ReflectionUtil.setRenderPosZ(var2, var8);

                  try {
                     ReflectionUtil.getTimer(var1).renderPartialTicks = var0;
                  } catch (Exception var14) {
                  }

                  EntityRenderer var16 = var1.entityRenderer;
                  int var17 = GL11.glGetInteger(2976);
                  GL11.glPushAttrib(1048575);
                  GL11.glMatrixMode(5889);
                  GL11.glPushMatrix();
                  GL11.glMatrixMode(5888);
                  GL11.glPushMatrix();

                  try {
                     ReflectionUtil.setupCameraTransform(var16, var0, 0);
                     EventManager.call(new Render3DEvent(var0));
                  } finally {
                     GL11.glMatrixMode(5888);
                     GL11.glPopMatrix();
                     GL11.glMatrixMode(5889);
                     GL11.glPopMatrix();
                     GL11.glMatrixMode(var17);
                     GL11.glPopAttrib();
                  }

                  resetFixedPipeline();
                  var16.setupOverlayRendering();
               }
            }
         } catch (Throwable var15) {
            System.out.println("[Valor] preRenderOverlay fallback error: ".concat(String.valueOf(var15)));
         }
      }
   }

   public static ItemStack onUpdateTick(InventoryPlayer var0) {
      if (Valor.moduleManager == null) {
         return null;
      } else {
         try {
            Scaffold var1;
            int var3;
            if ((var1 = (Scaffold)Valor.moduleManager.modules.get(Scaffold.class)) != null
               && var1.isEnabled()
               && var1.itemSpoofProp != null && Boolean.TRUE.equals(var1.itemSpoofProp.getValue())
               && (var3 = var1.blockSlot) >= 0) {
               return var0.getStackInSlot(var3);
            }

            AutoBlockIn var4;
            int var5;
            if ((var4 = (AutoBlockIn)Valor.moduleManager.modules.get(AutoBlockIn.class)) != null
               && (Boolean)var4.itemSpoofProp.getValue()
               && var4.isEnabled()
               && (var5 = var4.blockSlot) >= 0) {
               return var0.getStackInSlot(var5);
            }
         } catch (Throwable var2) {
            System.out.println("[Valor] onUpdateTick error: ".concat(String.valueOf(var2)));
         }

         return var0.getCurrentItem();
      }
   }

   public static void configureForgeScoreboard() {
      try {
         Class.forName("net.minecraftforge.client.GuiIngameForge").getField("renderObjective").setBoolean(null, !shouldHideScoreboard());
      } catch (Throwable var0) {
      }
   }

   public static boolean shouldHideScoreboard() {
      Module var0;
      return Valor.moduleManager == null ? false : (var0 = Valor.moduleManager.getModule(valor.module.render.Scoreboard.class)) != null && var0.isEnabled();
   }

   private static void saveAndClearObjective(Scoreboard var0, int var1) {
      ScoreObjective var2;
      if ((var2 = var0.getObjectiveInDisplaySlot(var1)) != null) {
         savedObjectiveSlots[savedObjectiveCount] = var1;
         savedObjectives[savedObjectiveCount++] = var2;
         var0.setObjectiveInDisplaySlot(var1, null);
      }
   }

   public static void beginHudFrame() {
      hudRenderedThisFrame = false;
   }

   public static void renderHudAtDisplay() {
      if (!hudRenderedThisFrame) {
         float var0;
         try {
            var0 = ReflectionUtil.getTimer(mc).renderPartialTicks;
         } catch (Throwable var1) {
            var0 = 0.0F;
         }

         renderHud(var0);
      }
   }
}
