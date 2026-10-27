package valor.util;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import valor.core.HudThemeManager;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.font.Fonts;
import valor.util.font.TTFontRenderer;
import valor.util.render.BlurPipeline;
import valor.util.render.GreenRender;

public final class RenderUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final Color DEFAULT_TEXT_COLOR = new Color(183, 181, 199, 255);

   private static TTFontRenderer getHudFont() {
      if (Fonts.isReady() && (Integer)HudThemeManager.getInstance().fontProp.getValue() != 3) {
         Fonts fonts = Fonts.get();
         int size = (Integer)HudThemeManager.getInstance().hudSizeProp.getValue();
         switch ((Integer)HudThemeManager.getInstance().fontProp.getValue()) {
            case 1:
               if (size == 0) {
                  return fonts.comfortaaCompact;
               } else {
                  return size == 2 ? fonts.comfortaaMedium : fonts.comfortaaSmall;
               }
            case 2:
               if (size == 0) {
                  return fonts.sansSmall;
               } else {
                  return size == 2 ? fonts.sansNormal : fonts.sansHud;
               }
            default:
               if (size == 0) {
                  return fonts.interSmall;
               } else {
                  return size == 2 ? fonts.interNormal : fonts.interHud;
               }
         }
      } else {
         return null;
      }
   }

   public static boolean preparePipeline() {
      return true;
   }

   public static void drawHudBackground(float x, float y, float width, float height) {
      drawPanel(x, y, width, height);
   }

   public static Color getSecondaryTextColor() {
      return new Color(160, 164, 178);
   }

   public static float getBoldStringWidth(String text) {
      TTFontRenderer font;
      return (font = getBoldHudFont()) != null ? font.getWidth(text) : getStringWidth(text);
   }

   private static Color getThemeColor(int index, int count, long time) {
      HudThemeManager theme;
      return (theme = HudThemeManager.getInstance()) == null ? MaterialTheme.hudAccent() : theme.getColor(index, count, time);
   }

   public static Color getAccentColor() {
      return getGradientColor(0, 2, System.currentTimeMillis());
   }

   public static void drawString(String text, float x, float y, Color color) {
      drawString(text, x, y, color, false);
   }

   private static void drawShadowUnderPanel(float x, float y, float width, float height, float radius) {
      GreenRender.fillRR(x - 1.0F, y, width + 2.0F, height + 2.0F, radius + 1.0F, new Color(0, 0, 0, 10));
      GreenRender.fillRR(x - 0.5F, y + 0.5F, width + 1.0F, height + 1.5F, radius + 0.5F, new Color(0, 0, 0, 18));
      GreenRender.fillRR(x + 0.5F, y + 1.25F, width, height, radius, new Color(0, 0, 0, 28));
   }

   public static Color getDarkerShade(Color color) {
      while (color == null) {
         color = MaterialTheme.ACCENT_PURPLE;
      }

      float factor = Math.min(1.0F, 0.07125F);
      return new Color(
         Math.min(255, (int)(9.475F + (float)color.getRed() * factor)),
         Math.min(255, (int)(15.16F + (float)color.getGreen() * factor)),
         Math.min(255, (int)(24.635F + (float)color.getBlue() * factor)),
         Math.max(0, Math.min(255, 126))
      );
   }

   public static void drawRoundedRect(float x, float y, float width, float height) {
      drawPanel(x, y, width, height);
   }

   public static float getFontHeight() {
      TTFontRenderer font;
      return (font = getHudFont()) != null ? font.getHeight() : (float)mc.fontRendererObj.FONT_HEIGHT * getVanillaFontScale();
   }

   public static Color getLightTextColor() {
      return new Color(245, 247, 252);
   }

   public static void drawPanel(float x, float y, float width, float height) {
      drawShadowUnderPanel(x, y, width, height, 7.0F);
      BlurPipeline.prepare();
      BlurPipeline.drawRegionRounded(x, y, width, height, 7.0F);
      GreenRender.fillRRGradient(x, y, width, height, 7.0F, new Color(27, 29, 44, 198), new Color(20, 22, 35, 210), false);
      Render3DUtil.setupRender2D();
   }

   private static Color withAlpha(Color color, int alpha) {
      if (color == null) {
         return new Color(0, 0, 0, Math.max(0, Math.min(255, alpha * 255 / 100)));
      } else {
         alpha = Math.max(0, Math.min(255, alpha <= 100 ? (int)((float)alpha * 2.55F) : alpha));
         return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
      }
   }

   public static Color getGradientColor(int index, int count, long time) {
      return GreenRender.lerp(getThemeColor(index, count, time), MaterialTheme.TEXT, 0.22F);
   }

   public static float getStringWidth(String text) {
      TTFontRenderer font;
      return (font = getHudFont()) != null ? font.getWidth(text) : (float)mc.fontRendererObj.getStringWidth(text) * getVanillaFontScale();
   }

   public static Color getDefaultTextColor() {
      return DEFAULT_TEXT_COLOR;
   }

   public static void drawString(String text, float x, float y, Color color, boolean shadow) {
      drawString(text, x, y, color, shadow, getHudFont());
   }

   private static void drawString(String text, float x, float y, Color color, boolean shadow, TTFontRenderer fontRenderer) {
      if (text != null && !text.isEmpty()) {
         Render3DUtil.setupRender2D();
         GL11.glEnable(3553);
         GlStateManager.enableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.disableLighting();
         GlStateManager.disableDepth();
         int rgb = color.getRGB();
         if (rgb >>> 24 == 0) {
            rgb |= -16777216;
         }

         if (fontRenderer != null) {
            if (shadow) {
               fontRenderer.drawStringWithShadow(text, x, y, rgb);
            } else {
               fontRenderer.drawString(text, x, y, rgb);
            }
         } else {
            GreenRender.prepareVanillaFont();
            float scale = getVanillaFontScale();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            if (shadow) {
               mc.fontRendererObj.drawStringWithShadow(text, 0.0F, 0.0F, rgb);
            } else {
               mc.fontRendererObj.drawString(text, 0.0F, 0.0F, rgb, false);
            }

            GlStateManager.popMatrix();
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static TTFontRenderer getBoldHudFont() {
      if (Fonts.isReady() && (Integer)HudThemeManager.getInstance().fontProp.getValue() == 0) {
         int size = (Integer)HudThemeManager.getInstance().hudSizeProp.getValue();
         Fonts fonts = Fonts.get();
         if (size == 0) {
            return fonts.interBoldCompact;
         } else {
            return size == 2 ? fonts.interBoldLarge : fonts.interBold;
         }
      } else {
         return null;
      }
   }

   private static Color getPanelBackgroundColor() {
      return new Color(23, 24, 39, 183);
   }

   public static void drawRoundedBox(float x, float y, float width, float height, Color color) {
      MaterialTheme.hudAccent();
      BlurPipeline.prepare();
      if (BlurPipeline.isAvailable()) {
         BlurPipeline.drawRegionRounded(x, y, width, height, 3.0F, 3.0F, 3.0F, 3.0F);
      }

      GreenRender.fillRRCornersHard(x, y, width, height, 3.0F, 3.0F, 3.0F, 3.0F, color);
      Render3DUtil.setupRender2D();
   }

   public static void drawConnectedProfile(float x, float y, float[] heights, float[] widths, Color[] colors) {
      if (widths.length != 0 && widths.length == heights.length) {
         int count = Math.min(widths.length, colors.length);
         float[][] profile = BlurPipeline.connectedProfile(x, heights, widths, 4.0F);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.disableTexture2D();
         GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.22745098F);
         GL11.glBegin(5);

         for (float[] pt : profile) {
            GL11.glVertex2f(pt[0] - 0.5F, y + pt[1] + 1.5F);
            GL11.glVertex2f(x + 0.5F, y + pt[1] + 1.5F);
         }

         GL11.glEnd();
         float totalH = 0.0F;

         for (float h : heights) {
            totalH += h;
         }

         GL11.glBegin(5);

         for (float[] pt : profile) {
            float vx = pt[0];
            float vy = pt[1];
            float pos = vy;
            float accum = 0.0F;
            int idx = 0;

            float mappedIdx;
            while (true) {
               if (idx >= count - 1) {
                  mappedIdx = (float)count - 1.0F;
                  break;
               }

               if (pos < accum + heights[idx]) {
                  mappedIdx = (float)idx + Math.max(0.0F, pos - accum) / Math.max(0.01F, heights[idx]);
                  break;
               }

               accum += heights[idx];
               idx++;
            }

            int baseIdx = Math.min(count - 1, (int)mappedIdx);
            int nextIdx = Math.min(count - 1, baseIdx + 1);
            float fract = mappedIdx - (float)baseIdx;
            Color c1 = colors[baseIdx];
            Color c2 = colors[nextIdx];
            Color blended = new Color(
               (int)((float)c1.getRed() + (float)(c2.getRed() - c1.getRed()) * fract),
               (int)((float)c1.getGreen() + (float)(c2.getGreen() - c1.getGreen()) * fract),
               (int)((float)c1.getBlue() + (float)(c2.getBlue() - c1.getBlue()) * fract)
            );
            float progress = Math.min(1.0F, vy / Math.max(1.0F, totalH));
            Color finalColor = new Color(
               Math.round(27.0F - 7.0F * progress + (float)blended.getRed() * 0.02F),
               Math.round(29.0F - 7.0F * progress + (float)blended.getGreen() * 0.02F),
               Math.round(44.0F - 9.0F * progress + (float)blended.getBlue() * 0.02F),
               Math.round(204.0F + 11.0F * progress)
            );
            GL11.glColor4f(
               (float)finalColor.getRed() / 255.0F,
               (float)finalColor.getGreen() / 255.0F,
               (float)finalColor.getBlue() / 255.0F,
               (float)finalColor.getAlpha() / 255.0F
            );
            GL11.glVertex2f(vx, y + vy);
            GL11.glVertex2f(x, y + vy);
         }

         GL11.glEnd();
         GlStateManager.enableTexture2D();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         Render3DUtil.setupRender2D();
      }
   }

   public static void drawProgressBar(float x, float y, float width, float height, float progress, Color color) {
      float radius = Math.min(width, height) / 2.0F;
      progress = Math.max(0.0F, Math.min(1.0F, progress));
      GreenRender.fillRR(x, y, width, height, radius, MaterialTheme.SETTINGS_BG);
      float fillWidth = Math.max(radius * 2.0F, width * progress);
      if (progress > 0.01F) {
         GreenRender.fillRR(x, y, fillWidth, height, radius, color);
      }

      Render3DUtil.setupRender2D();
   }

   public static void drawPanel(float x, float y, float width, float height, boolean shadow, int alpha) {
      float radius = Math.min(8.0F, Math.min(width, height) / 2.0F);
      if (shadow) {
         drawShadowUnderPanel(x, y, width, height, radius);
      }

      BlurPipeline.prepare();
      if (BlurPipeline.isAvailable()) {
         BlurPipeline.drawRegionRounded(x, y, width, height, radius);
      }

      Color bg = getPanelBackgroundColor();
      if (alpha >= 0) {
         bg = withAlpha(bg, alpha);
      }

      GreenRender.fillRR(x, y, width, height, radius, bg);
      Render3DUtil.setupRender2D();
   }

   public static void drawBoldString(String text, float x, float y, Color color) {
      TTFontRenderer boldFont;
      if ((boldFont = getBoldHudFont()) != null) {
         drawString(text, x, y, color, false, boldFont);
      } else {
         drawString(text, x, y, color);
      }
   }

   public static void drawShadowedPanel(float x, float y, float width, float height) {
      drawPanel(x, y, width, height, true, -1);
   }

   public static Color getThemeColor() {
      return MaterialTheme.hudAccent();
   }

   public static boolean isShadowDisabled() {
      return false;
   }

   private static float getVanillaFontScale() {
      if ((Integer)HudThemeManager.getInstance().fontProp.getValue() != 3) {
         return 1.0F;
      } else {
         int size = (Integer)HudThemeManager.getInstance().hudSizeProp.getValue();
         if (size == 0) {
            return 0.78F;
         } else {
            return size == 2 ? 1.08F : 0.92F;
         }
      }
   }
}
