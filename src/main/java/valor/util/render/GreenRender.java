package valor.util.render;

import java.awt.Color;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import valor.core.HudThemeManager;
import valor.util.font.Fonts;
import valor.util.font.TTFontRenderer;

public final class GreenRender {
   private static final ResourceLocation VANILLA_FONT_TEXTURE = new ResourceLocation("textures/font/ascii.png");
   private static final Deque<int[]> scissors = new ArrayDeque<>();
   public static final Minecraft mc = Minecraft.getMinecraft();
   private static int prog = -1;
   private static int vbo = -1;
   private static int uResolution;
   private static int uRect;
   private static int uRadius;
   private static int uColor;
   private static int uStrokeColor;
   private static int uStrokeWidth;
   private static int uGradientColor;
   private static int uGradientEnabled;
   private static int uGradientHorizontal;
   private static int uGlowColor;
   private static int uGlowWidth;
   private static int uSoftness;
   private static int uCorners;
   private static int aPos = -1;
   private static ScaledResolution cachedSR;
   private static int cachedDisplayW;
   private static int cachedDisplayH;
   private static final String VERT = "#version 120\nattribute vec2 aPos;\nvarying vec2 vPos;\nvoid main() {\n    vPos = aPos;\n    gl_Position = gl_ModelViewProjectionMatrix * vec4(aPos, 0.0, 1.0);\n}\n";
   private static final String FRAG = "#version 120\nvarying vec2 vPos;\nuniform vec2  uResolution;\nuniform vec4  uRect;\nuniform float uRadius;\nuniform vec4  uCorners;\nuniform vec4  uColor;\nuniform vec4  uStrokeColor;\nuniform float uStrokeWidth;\nuniform vec4  uGradientColor;\nuniform int   uGradientEnabled;\nuniform int   uGradientHorizontal;\nuniform vec4  uGlowColor;\nuniform float uGlowWidth;\nuniform float uSoftness;\nfloat sdf(vec2 p, vec2 b, float r) {\n    vec2 d = abs(p) - b + vec2(r);\n    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - r;\n}\nfloat sdfCorners(vec2 p, vec2 b, vec4 r) {\n    r.xy = (p.x > 0.0) ? r.xy : r.zw;\n    r.x  = (p.y > 0.0) ? r.x  : r.y;\n    vec2 d = abs(p) - b + vec2(r.x);\n    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - r.x;\n}\nvoid main() {\n    vec2 center = (uRect.xy + uRect.zw) * 0.5;\n    vec2 halfSize = (uRect.zw - uRect.xy) * 0.5;\n    vec2 local = vPos - center;\n    float dist = (uCorners.x >= 0.0)\n        ? sdfCorners(local, halfSize, uCorners)\n        : sdf(local, halfSize, uRadius);\n    float soft = max(uSoftness, 0.5);\n    vec4 fill = uColor;\n    if (uGradientEnabled == 1) {\n        float t;\n        if (uGradientHorizontal == 1) t = (vPos.x - uRect.x) / (uRect.z - uRect.x);\n        else t = (vPos.y - uRect.y) / (uRect.w - uRect.y);\n        t = clamp(t, 0.0, 1.0);\n        fill = mix(uColor, uGradientColor, t);\n    }\n    float fillAlpha   = fill.a   * (1.0 - smoothstep(-soft, soft, dist));\n    float strokeAlpha = uStrokeColor.a * (1.0 - smoothstep(-soft, soft, abs(dist) - uStrokeWidth * 0.5));\n    float glowAlpha   = uGlowColor.a   * smoothstep(-soft, 0.0, dist) * (1.0 - smoothstep(0.0, uGlowWidth, dist));\n    vec4 result = vec4(0.0);\n    if (glowAlpha > 0.0) result = vec4(uGlowColor.rgb, glowAlpha);\n    if (fillAlpha > 0.0) {\n        result.rgb = result.a > 0.0 ? mix(result.rgb, fill.rgb, fillAlpha) : fill.rgb;\n        result.a = max(result.a, fillAlpha);\n    }\n    if (strokeAlpha > 0.0) {\n        result.rgb = result.a > 0.0 ? mix(result.rgb, uStrokeColor.rgb, strokeAlpha) : uStrokeColor.rgb;\n        result.a = max(result.a, strokeAlpha);\n    }\n    if (result.a < 0.01) discard;\n    gl_FragColor = result;\n}\n";
   private static final FloatBuffer quadBuffer = BufferUtils.createFloatBuffer(8);
   private static int batchDepth = 0;
   private static boolean batchDepthWasOn;
   private static boolean batchAlphaWasOn;
   private static boolean batchCullWasOn;
   private static boolean batchTexWasOn;
   private static boolean batchBlendWasOn;
   private static final Deque<float[]> scissorTransforms = new ArrayDeque<>();

   private static void render(float var0, float var1, float var2, float var3) {
      if (batchDepth > 0) {
         GlStateManager.disableTexture2D();
         GlStateManager.disableDepth();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         drawQuad(var0 - 1.0F, var1 - 1.0F, var2 + 2.0F, var3 + 2.0F);
         GL20.glUseProgram(0);
         GlStateManager.enableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         ScaledResolution var4 = getSR();
         boolean var5 = GL11.glIsEnabled(2929);
         boolean var6 = GL11.glIsEnabled(3008);
         boolean var7 = GL11.glIsEnabled(2884);
         boolean var8 = GL11.glIsEnabled(3553);
         boolean var9 = GL11.glIsEnabled(3042);
         GlStateManager.matrixMode(5889);
         GlStateManager.pushMatrix();
         GlStateManager.loadIdentity();
         GlStateManager.ortho(0.0, (double)var4.getScaledWidth(), (double)var4.getScaledHeight(), 0.0, -1.0, 1.0);
         GlStateManager.matrixMode(5888);
         GlStateManager.pushMatrix();
         GlStateManager.loadIdentity();
         GlStateManager.disableDepth();
         GlStateManager.depthMask(false);
         GlStateManager.disableAlpha();
         GlStateManager.disableCull();
         GlStateManager.disableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         drawQuad(var0 - 1.0F, var1 - 1.0F, var2 + 2.0F, var3 + 2.0F);
         GL20.glUseProgram(0);
         GL15.glBindBuffer(34962, 0);
         GlStateManager.matrixMode(5889);
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5888);
         GlStateManager.popMatrix();
         if (var5) {
            GlStateManager.enableDepth();
         } else {
            GlStateManager.disableDepth();
         }

         GlStateManager.depthMask(true);
         if (var6) {
            GlStateManager.enableAlpha();
         } else {
            GlStateManager.disableAlpha();
         }

         if (var7) {
            GlStateManager.enableCull();
         } else {
            GlStateManager.disableCull();
         }

         if (var8) {
            GlStateManager.enableTexture2D();
         } else {
            GlStateManager.disableTexture2D();
         }

         if (var9) {
            GlStateManager.enableBlend();
         } else {
            GlStateManager.disableBlend();
         }

         restoreTextPipeline();
      }
   }

   private static TTFontRenderer normal() {
      if (Fonts.isReady() && (Integer)HudThemeManager.getInstance().fontProp.getValue() != 3) {
         Fonts var0 = Fonts.get();
         int var1 = (Integer)HudThemeManager.getInstance().fontProp.getValue();
         int var2 = (Integer)HudThemeManager.getInstance().guiSizeProp.getValue();
         if (var1 == 1) {
            if (var2 == 0) {
               return var0.comfortaaSmall;
            } else {
               return var2 == 2 ? var0.comfortaa : var0.comfortaaMedium;
            }
         } else if (var1 == 2) {
            if (var2 == 0) {
               return var0.sansHud;
            } else {
               return var2 == 2 ? var0.sansLarge : var0.sansNormal;
            }
         } else if (var2 == 0) {
            return var0.interHud;
         } else {
            return var2 == 2 ? var0.interLarge : var0.interNormal;
         }
      } else {
         return null;
      }
   }

   public static void dropShadow(float var0, float var1, float var2, float var3, float var4) {
      glowRR(var0, var1, var2, var3, var4, 14.0F, new Color(0, 0, 0, 110));
      glowRR(var0 + 1.0F, var1 + 2.0F, var2 - 2.0F, var3 - 2.0F, var4, 6.0F, new Color(0, 0, 0, 60));
   }

   public static void beginBatch() {
      if (batchDepth > 0) {
         batchDepth++;
      } else if (ensureShader()) {
         batchDepth = 1;
         batchDepthWasOn = GL11.glIsEnabled(2929);
         batchAlphaWasOn = GL11.glIsEnabled(3008);
         batchCullWasOn = GL11.glIsEnabled(2884);
         batchTexWasOn = GL11.glIsEnabled(3553);
         batchBlendWasOn = GL11.glIsEnabled(3042);
         GlStateManager.disableDepth();
         GlStateManager.depthMask(false);
         GlStateManager.disableAlpha();
         GlStateManager.disableCull();
         GlStateManager.disableLighting();
         GlStateManager.disableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      }
   }

   public static void prepareVanillaFont() {
      restoreTextPipeline();
      GlStateManager.enableAlpha();
      GL11.glBindTexture(3553, 0);
      GlStateManager.bindTexture(0);
      mc.getTextureManager().bindTexture(VANILLA_FONT_TEXTURE);
   }

   public static void fillRRCornersHard(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8) {
      fillRRCornersHard(var0, var1, var2, var3, var4, var5, var6, var7, new Color(var8, true));
   }

   public static void fillRRCornersHard(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, Color var8) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, 0.0F);
         GL20.glUniform1f(uSoftness, 0.5F);
         applyColor(uColor, var8);
         GL20.glUniform4f(uCorners, var6, var5, var7, var4);
         render(var0, var1, var2, var3);
      }
   }

   public static void drawString(String var0, float var1, float var2, int var3) {
      drawString(var0, var1, var2, new Color(var3, true));
   }

   public static void drawString(String var0, float var1, float var2, Color var3) {
      drawGuiText(var0, var1, var2, var3, normal(), false);
   }

   public static void outlineRR(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
      outlineRR(var0, var1, var2, var3, var4, var5, new Color(var6, true));
   }

   public static void outlineRR(float var0, float var1, float var2, float var3, float var4, float var5, Color var6) {
      strokeRR(var0, var1, var2, var3, var4, var5, new Color(0, 0, 0, 0), var6);
   }

   private static void restoreTextPipeline() {
      GL20.glUseProgram(0);
      if (aPos >= 0) {
         GL20.glDisableVertexAttribArray(aPos);
      }

      GL15.glBindBuffer(34962, 0);
      GL13.glActiveTexture(33984);
      GL11.glDisable(2896);
      GL11.glEnable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GlStateManager.disableTexture2D();
      GlStateManager.enableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void drawStringSmall(String var0, float var1, float var2, Color var3) {
      drawGuiText(var0, var1, var2, var3, small(), true);
   }

   public static void drawStringSmall(String var0, float var1, float var2, int var3) {
      drawStringSmall(var0, var1, var2, new Color(var3, true));
   }

   public static void fillRR(float var0, float var1, float var2, float var3, float var4, int var5) {
      fillRR(var0, var1, var2, var3, var4, new Color(var5, true));
   }

   public static void fillRR(float var0, float var1, float var2, float var3, float var4, Color var5) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, var4);
         applyColor(uColor, var5);
         render(var0, var1, var2, var3);
      }
   }

   public static float fontHSmall() {
      TTFontRenderer var0;
      return (var0 = small()) != null ? var0.getHeight() : (float)mc.fontRendererObj.FONT_HEIGHT * vanillaScale(true);
   }

   public static float strW(String var0) {
      TTFontRenderer var1;
      return (var1 = normal()) != null ? var1.getWidth(var0) : (float)mc.fontRendererObj.getStringWidth(var0) * vanillaScale(false);
   }

   public static float strWSmall(String var0) {
      TTFontRenderer var1;
      return (var1 = small()) != null ? var1.getWidth(var0) : (float)mc.fontRendererObj.getStringWidth(var0) * vanillaScale(true);
   }

   public static void fillRRGradient(float var0, float var1, float var2, float var3, float var4, Color var5, Color var6, boolean var7) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, var4);
         applyColor(uColor, var5);
         applyColor(uGradientColor, var6);
         GL20.glUniform1i(uGradientEnabled, 1);
         GL20.glUniform1i(uGradientHorizontal, var7 ? 1 : 0);
         render(var0, var1, var2, var3);
      }
   }

   public static Color withAlpha(Color var0, float var1) {
      return withAlpha(var0, (int)(var1 * 255.0F));
   }

   public static Color withAlpha(Color var0, int var1) {
      return new Color(var0.getRed(), var0.getGreen(), var0.getBlue(), Math.max(0, Math.min(255, var1)));
   }

   public static void drawStringBold(String var0, float var1, float var2, int var3) {
      drawStringBold(var0, var1, var2, new Color(var3, true));
   }

   public static void drawStringBold(String var0, float var1, float var2, Color var3) {
      drawGuiText(var0, var1, var2, var3, bold(), false);
   }

   public static void fillRRCorners(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8) {
      fillRRCorners(var0, var1, var2, var3, var4, var5, var6, var7, new Color(var8, true));
   }

   public static void fillRRCorners(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, Color var8) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, 0.0F);
         applyColor(uColor, var8);
         GL20.glUniform4f(uCorners, var6, var5, var7, var4);
         render(var0, var1, var2, var3);
      }
   }

   public static float fontH() {
      TTFontRenderer var0;
      return (var0 = normal()) != null ? var0.getHeight() : (float)mc.fontRendererObj.FONT_HEIGHT * vanillaScale(false);
   }

   public static void glowRR(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
      glowRR(var0, var1, var2, var3, var4, var5, new Color(var6, true));
   }

   public static void glowRR(float var0, float var1, float var2, float var3, float var4, float var5, Color var6) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, var4);
         applyColor(uColor, new Color(0, 0, 0, 0));
         applyColor(uGlowColor, var6);
         GL20.glUniform1f(uGlowWidth, var5);
         GL20.glUniform1f(uSoftness, var5);
         render(var0 - var5, var1 - var5, var2 + var5 * 2.0F, var3 + var5 * 2.0F);
      }
   }

   public static void popScissor() {
      if (!scissors.isEmpty()) {
         scissors.pop();
      }

      if (scissors.isEmpty()) {
         GL11.glDisable(3089);
      } else {
         int[] var0;
         GL11.glScissor((var0 = scissors.peek())[0], var0[1], var0[2], var0[3]);
      }
   }

   public static void pushScissor(float var0, float var1, float var2, float var3) {
      if (!scissorTransforms.isEmpty()) {
         float[] var4 = scissorTransforms.peek();
         var0 = var0 * var4[0] + var4[1];
         var1 = var1 * var4[0] + var4[2];
         var2 *= var4[0];
         var3 *= var4[0];
      }

      ScaledResolution var12;
      int var5 = (var12 = new ScaledResolution(mc)).getScaleFactor();
      int var6 = (int)Math.floor((double)(var0 * (float)var5));
      int var11 = (int)Math.floor((double)(((float)var12.getScaledHeight() - var1 - var3) * (float)var5));
      int var7 = (int)Math.ceil((double)((var0 + var2) * (float)var5));
      int var8 = (int)Math.ceil((double)(((float)var12.getScaledHeight() - var1) * (float)var5));
      if (!scissors.isEmpty()) {
         int[] var9 = scissors.peek();
         var6 = Math.max(var6, var9[0]);
         var11 = Math.max(var11, var9[1]);
         var7 = Math.min(var7, var9[0] + var9[2]);
         var8 = Math.min(var8, var9[1] + var9[3]);
      }

      int[] var10 = new int[]{var6, var11, Math.max(0, var7 - var6), Math.max(0, var8 - var11)};
      scissors.push(var10);
      GL11.glEnable(3089);
      GL11.glScissor(var10[0], var10[1], var10[2], var10[3]);
   }

   public static float strWBold(String var0) {
      TTFontRenderer var1;
      return (var1 = bold()) != null ? var1.getWidth(var0) : (float)mc.fontRendererObj.getStringWidth(var0) * vanillaScale(false);
   }

   private static TTFontRenderer small() {
      if (Fonts.isReady() && (Integer)HudThemeManager.getInstance().fontProp.getValue() != 3) {
         Fonts var0 = Fonts.get();
         int var1 = (Integer)HudThemeManager.getInstance().fontProp.getValue();
         int var2 = (Integer)HudThemeManager.getInstance().guiSizeProp.getValue();
         if (var1 == 1) {
            if (var2 == 0) {
               return var0.comfortaaCompact;
            } else {
               return var2 == 2 ? var0.comfortaaMedium : var0.comfortaaSmall;
            }
         } else if (var1 == 2) {
            if (var2 == 0) {
               return var0.sansSmall;
            } else {
               return var2 == 2 ? var0.sansNormal : var0.sansHud;
            }
         } else if (var2 == 0) {
            return var0.interTiny;
         } else {
            return var2 == 2 ? var0.interHud : var0.interSmall;
         }
      } else {
         return null;
      }
   }

   private static TTFontRenderer bold() {
      if (Fonts.isReady() && (Integer)HudThemeManager.getInstance().fontProp.getValue() != 3) {
         Fonts var0 = Fonts.get();
         int var1 = (Integer)HudThemeManager.getInstance().fontProp.getValue();
         int var2 = (Integer)HudThemeManager.getInstance().guiSizeProp.getValue();
         if (var1 == 1) {
            if (var2 == 0) {
               return var0.comfortaaSmall;
            } else {
               return var2 == 2 ? var0.comfortaa : var0.comfortaaMedium;
            }
         } else if (var1 == 2) {
            if (var2 == 0) {
               return var0.sansHud;
            } else {
               return var2 == 2 ? var0.sansLarge : var0.sansNormal;
            }
         } else if (var2 == 0) {
            return var0.interBoldCompact;
         } else {
            return var2 == 2 ? var0.interBoldLarge : var0.interBold;
         }
      } else {
         return null;
      }
   }

   public static void fillRect(float var0, float var1, float var2, float var3, int var4) {
      fillRR(var0, var1, var2, var3, 0.0F, var4);
   }

   public static void fillRect(float var0, float var1, float var2, float var3, Color var4) {
      fillRR(var0, var1, var2, var3, 0.0F, var4);
   }

   private static void drawQuad(float var0, float var1, float var2, float var3) {
      var2 = var0 + var2;
      var3 = var1 + var3;
      ((Buffer)quadBuffer).clear();
      ((Buffer)quadBuffer.put(var0).put(var1).put(var2).put(var1).put(var2).put(var3).put(var0).put(var3)).flip();
      GL15.glBindBuffer(34962, vbo);
      GL15.glBufferData(34962, quadBuffer, 35048);
      GL20.glEnableVertexAttribArray(aPos);
      GL20.glVertexAttribPointer(aPos, 2, 5126, false, 0, 0L);
      GL11.glDrawArrays(7, 0, 4);
      GL20.glDisableVertexAttribArray(aPos);
      GL15.glBindBuffer(34962, 0);
   }

   public static void endBatch() {
      if (batchDepth > 0) {
         if (--batchDepth <= 0) {
            GL20.glUseProgram(0);
            if (batchDepthWasOn) {
               GlStateManager.enableDepth();
            } else {
               GlStateManager.disableDepth();
            }

            GlStateManager.depthMask(true);
            if (batchAlphaWasOn) {
               GlStateManager.enableAlpha();
            } else {
               GlStateManager.disableAlpha();
            }

            if (batchCullWasOn) {
               GlStateManager.enableCull();
            } else {
               GlStateManager.disableCull();
            }

            if (batchTexWasOn) {
               GlStateManager.enableTexture2D();
            } else {
               GlStateManager.disableTexture2D();
            }

            if (batchBlendWasOn) {
               GlStateManager.enableBlend();
            } else {
               GlStateManager.disableBlend();
            }

            restoreTextPipeline();
         }
      }
   }

   public static Color lerp(Color var0, Color var1, float var2) {
      var2 = Math.max(0.0F, Math.min(1.0F, var2));
      return new Color(
         (int)((float)var0.getRed() + (float)(var1.getRed() - var0.getRed()) * var2),
         (int)((float)var0.getGreen() + (float)(var1.getGreen() - var0.getGreen()) * var2),
         (int)((float)var0.getBlue() + (float)(var1.getBlue() - var0.getBlue()) * var2),
         (int)((float)var0.getAlpha() + (float)(var1.getAlpha() - var0.getAlpha()) * var2)
      );
   }

   public static float smooth(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   public static void pushScissorTransform(float var0, float var1, float var2) {
      float[] var3;
      float var4 = (var3 = scissorTransforms.peek()) == null ? 1.0F : var3[0];
      float var5 = var3 == null ? 0.0F : var3[1];
      float var6 = var3 == null ? 0.0F : var3[2];
      scissorTransforms.push(new float[]{var4 * var2, var5 + var4 * var0 * (1.0F - var2), var6 + var4 * var1 * (1.0F - var2)});
   }

   public static void popScissorTransform() {
      if (!scissorTransforms.isEmpty()) {
         scissorTransforms.pop();
      }
   }

   public static float lerpFloat(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * Math.max(0.0F, Math.min(1.0F, var2));
   }

   private static void applyColor(int var0, Color var1) {
      GL20.glUniform4f(var0, (float)var1.getRed() / 255.0F, (float)var1.getGreen() / 255.0F, (float)var1.getBlue() / 255.0F, (float)var1.getAlpha() / 255.0F);
   }

   public static void dropShadowCorners(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      glowRRCorners(var0, var1, var2, var3, var4, var5, var6, var7, 14.0F, new Color(0, 0, 0, 110));
      glowRRCorners(var0 + 1.0F, var1 + 2.0F, var2 - 2.0F, var3 - 2.0F, var4, var5, var6, var7, 6.0F, new Color(0, 0, 0, 60));
   }

   public static void fillCircle(float var0, float var1, float var2, Color var3) {
      fillRR(var0 - var2, var1 - var2, var2 * 2.0F, var2 * 2.0F, var2, var3);
   }

   public static void fillCircle(float var0, float var1, float var2, int var3) {
      fillCircle(var0, var1, var2, new Color(var3, true));
   }

   public static void fillFlat(float var0, float var1, float var2, float var3, Color var4) {
      if (!(var2 <= 0.0F) && !(var3 <= 0.0F) && var4 != null) {
         boolean var5 = GL11.glIsEnabled(3553);
         boolean var6 = GL11.glIsEnabled(3042);
         boolean var7 = GL11.glIsEnabled(2929);
         GlStateManager.disableTexture2D();
         GlStateManager.disableDepth();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         Tessellator var8;
         WorldRenderer var9;
         (var9 = (var8 = Tessellator.getInstance()).getWorldRenderer()).begin(7, DefaultVertexFormats.POSITION_COLOR);
         int var10 = var4.getRed();
         int var11 = var4.getGreen();
         int var12 = var4.getBlue();
         int var13 = var4.getAlpha();
         var9.pos((double)var0, (double)var1, 0.0).color(var10, var11, var12, var13).endVertex();
         var9.pos((double)var0, (double)(var1 + var3), 0.0).color(var10, var11, var12, var13).endVertex();
         var9.pos((double)(var0 + var2), (double)(var1 + var3), 0.0).color(var10, var11, var12, var13).endVertex();
         var9.pos((double)(var0 + var2), (double)var1, 0.0).color(var10, var11, var12, var13).endVertex();
         var8.draw();
         if (var5) {
            GlStateManager.enableTexture2D();
         } else {
            GlStateManager.disableTexture2D();
         }

         if (!var6) {
            GlStateManager.disableBlend();
         }

         if (var7) {
            GlStateManager.enableDepth();
         } else {
            GlStateManager.disableDepth();
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static void drawGuiText(String var0, float var1, float var2, Color var3, TTFontRenderer var4, boolean var5) {
      prepText();
      if (var4 != null) {
         var4.drawString(var0, var1, var2, var3.getRGB());
      } else {
         prepareVanillaFont();
         float var6 = vanillaScale(var5);
         GlStateManager.pushMatrix();
         GlStateManager.translate(var1, var2, 0.0F);
         GlStateManager.scale(var6, var6, 1.0F);
         mc.fontRendererObj.drawStringWithShadow(var0, 0.0F, 0.0F, var3.getRGB());
         GlStateManager.popMatrix();
      }
   }

   private static boolean ensureShader() {
      if (prog != -1) {
         return true;
      } else {
         try {
            int var0;
            GL20.glShaderSource(
               var0 = GL20.glCreateShader(35633),
               "#version 120\nattribute vec2 aPos;\nvarying vec2 vPos;\nvoid main() {\n    vPos = aPos;\n    gl_Position = gl_ModelViewProjectionMatrix * vec4(aPos, 0.0, 1.0);\n}\n"
            );
            GL20.glCompileShader(var0);
            int var1;
            GL20.glShaderSource(
               var1 = GL20.glCreateShader(35632),
               "#version 120\nvarying vec2 vPos;\nuniform vec2  uResolution;\nuniform vec4  uRect;\nuniform float uRadius;\nuniform vec4  uCorners;\nuniform vec4  uColor;\nuniform vec4  uStrokeColor;\nuniform float uStrokeWidth;\nuniform vec4  uGradientColor;\nuniform int   uGradientEnabled;\nuniform int   uGradientHorizontal;\nuniform vec4  uGlowColor;\nuniform float uGlowWidth;\nuniform float uSoftness;\nfloat sdf(vec2 p, vec2 b, float r) {\n    vec2 d = abs(p) - b + vec2(r);\n    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - r;\n}\nfloat sdfCorners(vec2 p, vec2 b, vec4 r) {\n    r.xy = (p.x > 0.0) ? r.xy : r.zw;\n    r.x  = (p.y > 0.0) ? r.x  : r.y;\n    vec2 d = abs(p) - b + vec2(r.x);\n    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - r.x;\n}\nvoid main() {\n    vec2 center = (uRect.xy + uRect.zw) * 0.5;\n    vec2 halfSize = (uRect.zw - uRect.xy) * 0.5;\n    vec2 local = vPos - center;\n    float dist = (uCorners.x >= 0.0)\n        ? sdfCorners(local, halfSize, uCorners)\n        : sdf(local, halfSize, uRadius);\n    float soft = max(uSoftness, 0.5);\n    vec4 fill = uColor;\n    if (uGradientEnabled == 1) {\n        float t;\n        if (uGradientHorizontal == 1) t = (vPos.x - uRect.x) / (uRect.z - uRect.x);\n        else t = (vPos.y - uRect.y) / (uRect.w - uRect.y);\n        t = clamp(t, 0.0, 1.0);\n        fill = mix(uColor, uGradientColor, t);\n    }\n    float fillAlpha   = fill.a   * (1.0 - smoothstep(-soft, soft, dist));\n    float strokeAlpha = uStrokeColor.a * (1.0 - smoothstep(-soft, soft, abs(dist) - uStrokeWidth * 0.5));\n    float glowAlpha   = uGlowColor.a   * smoothstep(-soft, 0.0, dist) * (1.0 - smoothstep(0.0, uGlowWidth, dist));\n    vec4 result = vec4(0.0);\n    if (glowAlpha > 0.0) result = vec4(uGlowColor.rgb, glowAlpha);\n    if (fillAlpha > 0.0) {\n        result.rgb = result.a > 0.0 ? mix(result.rgb, fill.rgb, fillAlpha) : fill.rgb;\n        result.a = max(result.a, fillAlpha);\n    }\n    if (strokeAlpha > 0.0) {\n        result.rgb = result.a > 0.0 ? mix(result.rgb, uStrokeColor.rgb, strokeAlpha) : uStrokeColor.rgb;\n        result.a = max(result.a, strokeAlpha);\n    }\n    if (result.a < 0.01) discard;\n    gl_FragColor = result;\n}\n"
            );
            GL20.glCompileShader(var1);
            GL20.glAttachShader(prog = GL20.glCreateProgram(), var0);
            GL20.glAttachShader(prog, var1);
            GL20.glLinkProgram(prog);
            GL20.glDeleteShader(var0);
            GL20.glDeleteShader(var1);
            uResolution = GL20.glGetUniformLocation(prog, "uResolution");
            uRect = GL20.glGetUniformLocation(prog, "uRect");
            uRadius = GL20.glGetUniformLocation(prog, "uRadius");
            uColor = GL20.glGetUniformLocation(prog, "uColor");
            uStrokeColor = GL20.glGetUniformLocation(prog, "uStrokeColor");
            uStrokeWidth = GL20.glGetUniformLocation(prog, "uStrokeWidth");
            uGradientColor = GL20.glGetUniformLocation(prog, "uGradientColor");
            uGradientEnabled = GL20.glGetUniformLocation(prog, "uGradientEnabled");
            uGradientHorizontal = GL20.glGetUniformLocation(prog, "uGradientHorizontal");
            uGlowColor = GL20.glGetUniformLocation(prog, "uGlowColor");
            uGlowWidth = GL20.glGetUniformLocation(prog, "uGlowWidth");
            uSoftness = GL20.glGetUniformLocation(prog, "uSoftness");
            uCorners = GL20.glGetUniformLocation(prog, "uCorners");
            vbo = GL15.glGenBuffers();
            aPos = GL20.glGetAttribLocation(prog, "aPos");
            return true;
         } catch (Exception var2) {
            prog = -1;
            return false;
         }
      }
   }

   public static void fillRRCornersCrisp(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, Color var8) {
      if (!ensureShader()) {
         fillFlat(var0, var1, var2, var3, var8);
      } else {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, 0.0F);
         GL20.glUniform1f(uSoftness, 0.12F);
         applyColor(uColor, var8);
         GL20.glUniform4f(uCorners, var6, var5, var7, var4);
         render(var0, var1, var2, var3);
      }
   }

   private static ScaledResolution getSR() {
      int var0 = mc.displayWidth;
      int var1 = mc.displayHeight;
      if (cachedSR == null || cachedDisplayW != var0 || cachedDisplayH != var1) {
         cachedSR = new ScaledResolution(mc);
         cachedDisplayW = var0;
         cachedDisplayH = var1;
      }

      return cachedSR;
   }

   public static void outlineRRCorners(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, Color var9) {
      strokeRRCorners(var0, var1, var2, var3, var4, var5, var6, var7, var8, new Color(0, 0, 0, 0), var9);
   }

   private static void prepText() {
      restoreTextPipeline();
   }

   private static float vanillaScale(boolean var0) {
      int var1 = (Integer)HudThemeManager.getInstance().guiSizeProp.getValue();
      if (var0) {
         if (var1 == 0) {
            return 0.72F;
         }

         if (var1 != 2) {
            return 0.86F;
         }
      } else {
         if (var1 == 0) {
            return 0.88F;
         }

         if (var1 == 2) {
            return 1.16F;
         }
      }

      return 1.0F;
   }

   public static void glowRRCorners(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, Color var9) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, 0.0F);
         GL20.glUniform4f(uCorners, var6, var5, var7, var4);
         applyColor(uColor, new Color(0, 0, 0, 0));
         applyColor(uGlowColor, var9);
         GL20.glUniform1f(uGlowWidth, var8);
         GL20.glUniform1f(uSoftness, var8);
         render(var0 - var8, var1 - var8, var2 + var8 * 2.0F, var3 + var8 * 2.0F);
      }
   }

   private static void setDefaults(float var0, float var1, float var2, float var3, float var4) {
      ScaledResolution var5 = getSR();
      GL20.glUniform2f(uResolution, (float)var5.getScaledWidth(), (float)var5.getScaledHeight());
      GL20.glUniform4f(uRect, var0, var1, var0 + var2, var1 + var3);
      GL20.glUniform1f(uRadius, var4);
      GL20.glUniform1f(uSoftness, 1.0F);
      GL20.glUniform4f(uCorners, -1.0F, -1.0F, -1.0F, -1.0F);
      GL20.glUniform1f(uStrokeWidth, 0.0F);
      GL20.glUniform4f(uStrokeColor, 0.0F, 0.0F, 0.0F, 0.0F);
      GL20.glUniform1i(uGradientEnabled, 0);
      GL20.glUniform1i(uGradientHorizontal, 0);
      GL20.glUniform4f(uGradientColor, 0.0F, 0.0F, 0.0F, 0.0F);
      GL20.glUniform4f(uGlowColor, 0.0F, 0.0F, 0.0F, 0.0F);
      GL20.glUniform1f(uGlowWidth, 0.0F);
   }

   public static int lerpARGB(int var0, int var1, float var2) {
      return lerp(new Color(var0, true), new Color(var1, true), var2).getRGB();
   }

   public static void strokeRRCorners(
      float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, Color var9, Color var10
   ) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, 0.0F);
         GL20.glUniform4f(uCorners, var6, var5, var7, var4);
         applyColor(uColor, var9);
         applyColor(uStrokeColor, var10);
         GL20.glUniform1f(uStrokeWidth, var8);
         render(var0, var1, var2, var3);
      }
   }

   public static void strokeRR(float var0, float var1, float var2, float var3, float var4, float var5, Color var6, Color var7) {
      if (ensureShader()) {
         GL20.glUseProgram(prog);
         setDefaults(var0, var1, var2, var3, var4);
         applyColor(uColor, var6);
         applyColor(uStrokeColor, var7);
         GL20.glUniform1f(uStrokeWidth, var5);
         render(var0, var1, var2, var3);
      }
   }

   public static void strokeRR(float var0, float var1, float var2, float var3, float var4, float var5, int var6, int var7) {
      strokeRR(var0, var1, var2, var3, var4, var5, new Color(var6, true), new Color(var7, true));
   }

   public static int withAlphaARGB(int var0, float var1) {
      return withAlpha(new Color(var0, true), var1).getRGB();
   }

   private GreenRender() {
   }

   public static void destroy() {
      if (prog != -1) {
         GL20.glDeleteProgram(prog);
         prog = -1;
      }

      if (vbo != -1) {
         GL15.glDeleteBuffers(vbo);
         vbo = -1;
      }
   }
}
