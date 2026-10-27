package valor.util.render;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.IntBuffer;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
public final class BlurPipeline {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final int DOWNSAMPLE = 2;
   private static final int BLUR_PASSES = 2;
   private static final String BLUR_VERT = "#version 120\nvoid main(){\n  gl_TexCoord[0]=gl_MultiTexCoord0;\n  gl_Position=ftransform();\n}\n";
   private static final String BLUR_FRAG = "#version 120\nuniform sampler2D tex;\nuniform vec2 direction;\nvoid main(){\n  vec2 uv=gl_TexCoord[0].st;\n  vec4 c=texture2D(tex,uv)*0.0798641732;\n  c+=(texture2D(tex,uv+direction*1.48500450)+texture2D(tex,uv-direction*1.48500450))*0.1520066804;\n  c+=(texture2D(tex,uv+direction*3.46505705)+texture2D(tex,uv-direction*3.46505705))*0.1247014573;\n  c+=(texture2D(tex,uv+direction*5.44522076)+texture2D(tex,uv-direction*5.44522076))*0.0873141362;\n  c+=(texture2D(tex,uv+direction*7.42555748)+texture2D(tex,uv-direction*7.42555748))*0.0521791297;\n  c+=(texture2D(tex,uv+direction*9.40612690)+texture2D(tex,uv-direction*9.40612690))*0.0266134565;\n  c+=(texture2D(tex,uv+direction*11.38698582)+texture2D(tex,uv-direction*11.38698582))*0.0115848079;\n  c+=(texture2D(tex,uv+direction*13.36818758)+texture2D(tex,uv-direction*13.36818758))*0.0043037644;\n  c+=(texture2D(tex,uv+direction*15.34978145)+texture2D(tex,uv-direction*15.34978145))*0.0013644810;\n  gl_FragColor=c;\n}\n";
   private static BlurPipeline.RawFramebuffer[] buffers;
   private static int captureTex;
   private static int captureW;
   private static int captureH;
   private static boolean failed;
   private static boolean failureLogged;
   private static boolean preparedThisFrame;
   private static int lastBw = -1;
   private static int lastBh = -1;
   private static int blurProg = -1;
   private static int uTex = -1;
   private static int uDirection = -1;
   private static boolean blurShaderFailed;
   private static int roundProg = -1;
   private static int uRoundTex = -1;
   private static int uRoundRect = -1;
   private static int uRoundCorners = -1;
   private static boolean roundShaderFailed;
   private static final String ROUND_VERT = "#version 120\nvarying vec2 vPos;\nvarying vec2 vUv;\nvoid main(){\n  vPos=gl_Vertex.xy;\n  vUv=gl_MultiTexCoord0.st;\n  gl_Position=ftransform();\n}\n";
   private static final String ROUND_FRAG = "#version 120\nuniform sampler2D tex;\nuniform vec4 uRect;\nuniform vec4 uCorners;\nvarying vec2 vPos;\nvarying vec2 vUv;\nfloat sdfCorners(vec2 p, vec2 b, vec4 r){\n  r.xy=(p.x>0.0)?r.xy:r.zw;\n  r.x=(p.y>0.0)?r.x:r.y;\n  vec2 d=abs(p)-b+vec2(r.x);\n  return length(max(d,0.0))+min(max(d.x,d.y),0.0)-r.x;\n}\nvoid main(){\n  vec2 center=(uRect.xy+uRect.zw)*0.5;\n  vec2 halfSize=(uRect.zw-uRect.xy)*0.5;\n  float dist=sdfCorners(vPos-center, halfSize, uCorners);\n  float a=1.0-smoothstep(-0.75,0.75,dist);\n  if(a<0.01) discard;\n  vec4 c=texture2D(tex,vUv);\n  gl_FragColor=vec4(c.rgb,c.a*a);\n}\n";
   public static void beginFrame() {
      preparedThisFrame = false;
   }
   private static void resetFixedPipeline() {
      GL20.glUseProgram(0);
      GL13.glActiveTexture(33984);
      GL11.glDisable(2896);
      GL11.glDisable(3008);
      GL11.glDisable(2884);
      GL11.glEnable(3553);
      GlStateManager.disableTexture2D();
      GlStateManager.enableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.disableAlpha();
      GlStateManager.disableCull();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.colorMask(true, true, true, true);
   }
   public static float[][] connectedProfile(float var0, float[] var1, float[] var2, float var3) {
      ArrayList<float[]> var4;
      (var4 = new ArrayList<>()).add(new float[]{var0 - var2[0], 0.0F});
      float var5 = 0.0F;
      for (int var6 = 1; var6 < var2.length; var6++) {
         var5 += var1[var6 - 1];
         float var7 = var0 - var2[var6 - 1];
         float var8 = var0 - var2[var6];
         float var9 = Math.max(0.0F, var8 - var7);
         float var10 = Math.min(var3, Math.min(var1[var6 - 1], var1[var6]) * 0.42F);
         float var11 = Math.min(var3, var9 * 0.5F);
         if (var9 < 0.05F) {
            var4.add(new float[]{var7, var5});
         } else {
            var4.add(new float[]{var7, var5 - var10});
            for (int var17 = 1; var17 <= 14; var17++) {
               double var12 = (Math.PI / 2) * (double)var17 / 14.0;
               var4.add(new float[]{var7 + var11 - var11 * (float)Math.cos(var12), var5 - var10 + var10 * (float)Math.sin(var12)});
            }
            var4.add(new float[]{var8 - var11, var5});
            for (int var18 = 1; var18 <= 14; var18++) {
               double var21 = (Math.PI / 2) * (double)var18 / 14.0;
               var4.add(new float[]{var8 - var11 + var11 * (float)Math.sin(var21), var5 + var10 - var10 * (float)Math.cos(var21)});
            }
         }
      }
      float var14 = var5 + var1[var1.length - 1];
      float var15 = var0 - var2[var2.length - 1];
      float var16 = Math.min(var3, Math.min(var1[var1.length - 1] * 0.42F, var2[var2.length - 1] * 0.25F));
      var4.add(new float[]{var15, var14 - var16});
      for (int var19 = 1; var19 <= 14; var19++) {
         double var20 = (Math.PI / 2) * (double)var19 / 14.0;
         var4.add(new float[]{var15 + var16 - var16 * (float)Math.cos(var20), var14 - var16 + var16 * (float)Math.sin(var20)});
      }
      return var4.toArray(new float[var4.size()][]);
   }
   public static void drawRegion(float var0, float var1, float var2, float var3) {
      drawRegionRounded(var0, var1, var2, var3, 0.0F, 0.0F, 0.0F, 0.0F);
   }
   public static void drawRegionRounded(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      if (!failed) {
         prepare();
         if (isAvailable()) {
            ScaledResolution var8;
            double var9 = (double)(var8 = new ScaledResolution(mc)).getScaledWidth();
            double var11 = (double)var8.getScaledHeight();
            if (!(var9 <= 0.0) && !(var11 <= 0.0) && !(var2 <= 0.0F) && !(var3 <= 0.0F)) {
               double var13 = (double)var0 / var9;
               double var15 = (double)(var0 + var2) / var9;
               double var17 = 1.0 - (double)(var1 + var3) / var11;
               double var19 = 1.0 - (double)var1 / var11;
               resetFixedPipeline();
               GlStateManager.disableDepth();
               GlStateManager.depthMask(false);
               GlStateManager.enableBlend();
               GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
               GlStateManager.disableAlpha();
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GL13.glActiveTexture(33984);
               GL11.glBindTexture(3553, buffers[0].framebufferTexture);
               GL11.glTexParameteri(3553, 10241, 9729);
               GL11.glTexParameteri(3553, 10240, 9729);
               GL11.glTexParameteri(3553, 10242, 33071);
               GL11.glTexParameteri(3553, 10243, 33071);
               boolean var23;
               if ((var23 = var4 > 0.01F || var5 > 0.01F || var6 > 0.01F || var7 > 0.01F) && ensureRoundShader()) {
                  GL20.glUseProgram(roundProg);
                  GL20.glUniform1i(uRoundTex, 0);
                  GL20.glUniform4f(uRoundRect, var0, var1, var0 + var2, var1 + var3);
                  GL20.glUniform4f(uRoundCorners, var6, var5, var7, var4);
               }
               Tessellator var21;
               WorldRenderer var22;
               (var22 = (var21 = Tessellator.getInstance()).getWorldRenderer()).begin(7, DefaultVertexFormats.POSITION_TEX);
               var22.pos((double)var0, (double)(var1 + var3), 0.0).tex(var13, var17).endVertex();
               var22.pos((double)(var0 + var2), (double)(var1 + var3), 0.0).tex(var15, var17).endVertex();
               var22.pos((double)(var0 + var2), (double)var1, 0.0).tex(var15, var19).endVertex();
               var22.pos((double)var0, (double)var1, 0.0).tex(var13, var19).endVertex();
               var21.draw();
               if (var23) {
                  GL20.glUseProgram(0);
               }
               GL11.glBindTexture(3553, 0);
               GlStateManager.bindTexture(0);
               GlStateManager.depthMask(false);
               GlStateManager.disableDepth();
               GlStateManager.enableBlend();
               GlStateManager.enableTexture2D();
               GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }
   }
   public static void drawRegionRounded(float var0, float var1, float var2, float var3, float var4) {
      drawRegionRounded(var0, var1, var2, var3, var4, var4, var4, var4);
   }
   private static void blit(int var0, float var1, float var2, BlurPipeline.RawFramebuffer var3, boolean var4, float var5, float var6) {
      OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, var3.framebufferObject);
      GlStateManager.viewport(0, 0, var3.framebufferWidth, var3.framebufferHeight);
      GlStateManager.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.clear(16384);
      resetFixedPipeline();
      GlStateManager.disableBlend();
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.matrixMode(5889);
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      GlStateManager.ortho(0.0, (double)var3.framebufferWidth, (double)var3.framebufferHeight, 0.0, 1000.0, 3000.0);
      GlStateManager.matrixMode(5888);
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      GlStateManager.translate(0.0F, 0.0F, -2000.0F);
      GL11.glBindTexture(3553, var0);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      if (var4 && blurProg > 0) {
         GL20.glUseProgram(blurProg);
         GL20.glUniform1i(uTex, 0);
         GL20.glUniform2f(uDirection, var5, var6);
      }
      float var7 = (float)var3.framebufferWidth;
      float var8 = (float)var3.framebufferHeight;
      Tessellator var9;
      WorldRenderer var10;
      (var10 = (var9 = Tessellator.getInstance()).getWorldRenderer()).begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var10.pos(0.0, (double)var8, 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex();
      var10.pos((double)var7, (double)var8, 0.0).tex((double)var1, 0.0).color(255, 255, 255, 255).endVertex();
      var10.pos((double)var7, 0.0, 0.0).tex((double)var1, (double)var2).color(255, 255, 255, 255).endVertex();
      var10.pos(0.0, 0.0, 0.0).tex(0.0, (double)var2).color(255, 255, 255, 255).endVertex();
      var9.draw();
      if (var4) {
         GL20.glUseProgram(0);
      }
      GL11.glBindTexture(3553, 0);
      GlStateManager.matrixMode(5889);
      GlStateManager.popMatrix();
      GlStateManager.matrixMode(5888);
      GlStateManager.popMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.enableBlend();
   }
   private static boolean ensureBlurShader() {
      if (blurProg > 0) {
         return true;
      } else if (blurShaderFailed) {
         return false;
      } else {
         try {
            int var0;
            GL20.glShaderSource(
               var0 = GL20.glCreateShader(35633), "#version 120\nvoid main(){\n  gl_TexCoord[0]=gl_MultiTexCoord0;\n  gl_Position=ftransform();\n}\n"
            );
            GL20.glCompileShader(var0);
            if (GL20.glGetShaderi(var0, 35713) == 0) {
               blurShaderFailed = true;
               return false;
            } else {
               int var1;
               GL20.glShaderSource(
                  var1 = GL20.glCreateShader(35632),
                  "#version 120\nuniform sampler2D tex;\nuniform vec2 direction;\nvoid main(){\n  vec2 uv=gl_TexCoord[0].st;\n  vec4 c=texture2D(tex,uv)*0.0798641732;\n  c+=(texture2D(tex,uv+direction*1.48500450)+texture2D(tex,uv-direction*1.48500450))*0.1520066804;\n  c+=(texture2D(tex,uv+direction*3.46505705)+texture2D(tex,uv-direction*3.46505705))*0.1247014573;\n  c+=(texture2D(tex,uv+direction*5.44522076)+texture2D(tex,uv-direction*5.44522076))*0.0873141362;\n  c+=(texture2D(tex,uv+direction*7.42555748)+texture2D(tex,uv-direction*7.42555748))*0.0521791297;\n  c+=(texture2D(tex,uv+direction*9.40612690)+texture2D(tex,uv-direction*9.40612690))*0.0266134565;\n  c+=(texture2D(tex,uv+direction*11.38698582)+texture2D(tex,uv-direction*11.38698582))*0.0115848079;\n  c+=(texture2D(tex,uv+direction*13.36818758)+texture2D(tex,uv-direction*13.36818758))*0.0043037644;\n  c+=(texture2D(tex,uv+direction*15.34978145)+texture2D(tex,uv-direction*15.34978145))*0.0013644810;\n  gl_FragColor=c;\n}\n"
               );
               GL20.glCompileShader(var1);
               if (GL20.glGetShaderi(var1, 35713) == 0) {
                  blurShaderFailed = true;
                  return false;
               } else {
                  GL20.glAttachShader(blurProg = GL20.glCreateProgram(), var0);
                  GL20.glAttachShader(blurProg, var1);
                  GL20.glLinkProgram(blurProg);
                  GL20.glDeleteShader(var0);
                  GL20.glDeleteShader(var1);
                  if (GL20.glGetProgrami(blurProg, 35714) == 0) {
                     blurProg = -1;
                     blurShaderFailed = true;
                     return false;
                  } else {
                     uTex = GL20.glGetUniformLocation(blurProg, "tex");
                     uDirection = GL20.glGetUniformLocation(blurProg, "direction");
                     return true;
                  }
               }
            }
         } catch (Throwable var2) {
            blurShaderFailed = true;
            blurProg = -1;
            return false;
         }
      }
   }
   private static void deleteBuffers() {
      if (buffers != null) {
         BlurPipeline.RawFramebuffer[] var0 = buffers;
         int var1 = buffers.length;
         for (int var2 = 0; var2 < var1; var2++) {
            BlurPipeline.RawFramebuffer var3;
            if ((var3 = var0[var2]) != null) {
               try {
                  var3.delete();
               } catch (Throwable var4) {
               }
            }
         }
         buffers = null;
      }
   }
   public static void drawConnectedList(float var0, float var1, float[] var2, float[] var3, float var4) {
      if (!failed && var3 != null && var2 != null && var3.length != 0 && var3.length == var2.length) {
         prepare();
         if (isAvailable()) {
            ScaledResolution var5;
            double var6 = (double)(var5 = new ScaledResolution(mc)).getScaledWidth();
            double var8 = (double)var5.getScaledHeight();
            if (!(var6 <= 0.0) && !(var8 <= 0.0)) {
               resetFixedPipeline();
               GlStateManager.disableDepth();
               GlStateManager.depthMask(false);
               GlStateManager.enableBlend();
               GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
               GlStateManager.disableAlpha();
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GL13.glActiveTexture(33984);
               GL11.glBindTexture(3553, buffers[0].framebufferTexture);
               GL11.glTexParameteri(3553, 10241, 9729);
               GL11.glTexParameteri(3553, 10240, 9729);
               GL11.glTexParameteri(3553, 10242, 33071);
               GL11.glTexParameteri(3553, 10243, 33071);
               float[][] var15 = connectedProfile(var0, var2, var3, var4);
               Tessellator var16;
               WorldRenderer var17;
               (var17 = (var16 = Tessellator.getInstance()).getWorldRenderer()).begin(5, DefaultVertexFormats.POSITION_TEX);
               int var18 = var15.length;
               for (int var10 = 0; var10 < var18; var10++) {
                  float[] var11;
                  float var12 = (var11 = var15[var10])[0];
                  float var19 = var11[1];
                  float var20 = var1 + var19;
                  double var13 = 1.0 - (double)var20 / var8;
                  var17.pos((double)var12, (double)var20, 0.0).tex((double)var12 / var6, var13).endVertex();
                  var17.pos((double)var0, (double)var20, 0.0).tex((double)var0 / var6, var13).endVertex();
               }
               var16.draw();
               GL11.glBindTexture(3553, 0);
               GlStateManager.bindTexture(0);
               GlStateManager.depthMask(false);
               GlStateManager.disableDepth();
               GlStateManager.enableBlend();
               GlStateManager.enableTexture2D();
               GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }
   }
   private static void ensureBuffers(int var0, int var1) {
      if (buffers == null || lastBw != var0 || lastBh != var1) {
         deleteBuffers();
         buffers = new BlurPipeline.RawFramebuffer[]{createFbo(var0, var1), createFbo(var0, var1), createFbo(Math.max(2, var0 / 2), Math.max(2, var1 / 2))};
         lastBw = var0;
         lastBh = var1;
      }
   }
   private static boolean ensureRoundShader() {
      if (roundProg > 0) {
         return true;
      } else if (roundShaderFailed) {
         return false;
      } else {
         try {
            int var0;
            GL20.glShaderSource(
               var0 = GL20.glCreateShader(35633),
               "#version 120\nvarying vec2 vPos;\nvarying vec2 vUv;\nvoid main(){\n  vPos=gl_Vertex.xy;\n  vUv=gl_MultiTexCoord0.st;\n  gl_Position=ftransform();\n}\n"
            );
            GL20.glCompileShader(var0);
            if (GL20.glGetShaderi(var0, 35713) == 0) {
               roundShaderFailed = true;
               return false;
            } else {
               int var1;
               GL20.glShaderSource(
                  var1 = GL20.glCreateShader(35632),
                  "#version 120\nuniform sampler2D tex;\nuniform vec4 uRect;\nuniform vec4 uCorners;\nvarying vec2 vPos;\nvarying vec2 vUv;\nfloat sdfCorners(vec2 p, vec2 b, vec4 r){\n  r.xy=(p.x>0.0)?r.xy:r.zw;\n  r.x=(p.y>0.0)?r.x:r.y;\n  vec2 d=abs(p)-b+vec2(r.x);\n  return length(max(d,0.0))+min(max(d.x,d.y),0.0)-r.x;\n}\nvoid main(){\n  vec2 center=(uRect.xy+uRect.zw)*0.5;\n  vec2 halfSize=(uRect.zw-uRect.xy)*0.5;\n  float dist=sdfCorners(vPos-center, halfSize, uCorners);\n  float a=1.0-smoothstep(-0.75,0.75,dist);\n  if(a<0.01) discard;\n  vec4 c=texture2D(tex,vUv);\n  gl_FragColor=vec4(c.rgb,c.a*a);\n}\n"
               );
               GL20.glCompileShader(var1);
               if (GL20.glGetShaderi(var1, 35713) == 0) {
                  roundShaderFailed = true;
                  return false;
               } else {
                  GL20.glAttachShader(roundProg = GL20.glCreateProgram(), var0);
                  GL20.glAttachShader(roundProg, var1);
                  GL20.glLinkProgram(roundProg);
                  GL20.glDeleteShader(var0);
                  GL20.glDeleteShader(var1);
                  if (GL20.glGetProgrami(roundProg, 35714) == 0) {
                     roundProg = -1;
                     roundShaderFailed = true;
                     return false;
                  } else {
                     uRoundTex = GL20.glGetUniformLocation(roundProg, "tex");
                     uRoundRect = GL20.glGetUniformLocation(roundProg, "uRect");
                     uRoundCorners = GL20.glGetUniformLocation(roundProg, "uCorners");
                     return true;
                  }
               }
            }
         } catch (Throwable var2) {
            roundShaderFailed = true;
            roundProg = -1;
            return false;
         }
      }
   }
   private static void reportFailure(Throwable var0) {
      if (!failureLogged) {
         failureLogged = true;
         System.err.println("[Valor] Blur disabled: ".concat(String.valueOf(var0)));
         var0.printStackTrace();
         try {
            File var1 = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\valor_runtime.log");
            PrintWriter var12 = new PrintWriter(new FileWriter(var1, true));
            Throwable var2 = null;
            boolean var7 = false ;
            try {
               var7 = true;
               var12.println("[Valor] Blur disabled: ".concat(String.valueOf(var0)));
               var0.printStackTrace(var12);
               var7 = false;
            } catch (Throwable var9) {
               var2 = var9;
               throw var9;
            } finally {
               if (var7) {
                  if (var2 != null) {
                     try {
                        var12.close();
                     } catch (Throwable var8) {
                        var2.addSuppressed(var8);
                     }
                  } else {
                     var12.close();
                  }
               }
            }
            var12.close();
         } catch (Throwable var11) {
         }
      }
   }
   private static void restoreFramebuffer(int var0, int var1, int var2, int var3, int var4, int var5) {
      try {
         GL20.glUseProgram(0);
         GL11.glBindTexture(3553, 0);
         GlStateManager.bindTexture(0);
         OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, var0);
         GL11.glReadBuffer(var1);
         GlStateManager.viewport(var2, var3, var4, var5);
         GlStateManager.colorMask(true, true, true, true);
         GlStateManager.depthMask(false);
         GlStateManager.disableDepth();
         GlStateManager.enableBlend();
         GlStateManager.enableTexture2D();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      } catch (Throwable var6) {
      }
   }
   private static BlurPipeline.RawFramebuffer createFbo(int var0, int var1) {
      BlurPipeline.RawFramebuffer var2 = new BlurPipeline.RawFramebuffer(var0, var1);
      GL11.glBindTexture(3553, var2.framebufferTexture);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      GL11.glBindTexture(3553, 0);
      return var2;
   }
   private static void ensureCaptureTex(int var0, int var1) {
      if (captureTex <= 0 || captureW != var0 || captureH != var1) {
         if (captureTex > 0) {
            GL11.glDeleteTextures(captureTex);
            captureTex = 0;
         }
         captureTex = GL11.glGenTextures();
         captureW = var0;
         captureH = var1;
         GL11.glBindTexture(3553, captureTex);
         GL11.glTexParameteri(3553, 10241, 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
         GL11.glTexParameteri(3553, 10242, 33071);
         GL11.glTexParameteri(3553, 10243, 33071);
         GL11.glTexImage2D(3553, 0, 32856, var0, var1, 0, 6408, 5121, (java.nio.ByteBuffer)null);
         GL11.glBindTexture(3553, 0);
      }
   }
   public static boolean isAvailable() {
      return !failed && buffers != null && preparedThisFrame && captureTex > 0;
   }
   private BlurPipeline() {
   }
   public static void prepare(float var0) {
      if (!failed) {
         if (!preparedThisFrame) {
            int var1 = GL11.glGetInteger(36006);
            int var2 = GL11.glGetInteger(3074);
            IntBuffer var3 = BufferUtils.createIntBuffer(16);
            GL11.glGetInteger(2978, var3);
            int var4 = var3.get(0);
            int var5 = var3.get(1);
            int var6 = var3.get(2);
            int var16 = var3.get(3);
            try {
               int var7 = Math.max(2, var6);
               int var8 = Math.max(2, var16);
               ensureCaptureTex(var7, var8);
               resetFixedPipeline();
               OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, var1);
               GL11.glReadBuffer(var1 == 0 ? var2 : '\u8ce0');
               GL13.glActiveTexture(33984);
               GL11.glBindTexture(3553, captureTex);
               GL11.glCopyTexSubImage2D(3553, 0, 0, 0, var4, var5, var7, var8);
               var7 = Math.max(2, var7 / 2);
               var8 = Math.max(2, var8 / 2);
               ensureBuffers(var7, var8);
               blit(captureTex, 1.0F, 1.0F, buffers[0], false, 0.0F, 0.0F);
               if (ensureBlurShader()) {
                  float var19 = 1.0F / (float)var7;
                  float var21 = 1.0F / (float)var8;
                  float var9 = var0 > 2.0F ? 1.15F : 1.0F;
                  int var15 = var0 > 1.5F ? 2 : 1;
                  for (int var10 = 0; var10 < var15; var10++) {
                     blit(buffers[0].framebufferTexture, 1.0F, 1.0F, buffers[1], true, var19 * var9, 0.0F);
                     blit(buffers[1].framebufferTexture, 1.0F, 1.0F, buffers[0], true, 0.0F, var21 * var9);
                  }
               } else {
                  for (int var18 = 0; var18 < 3; var18++) {
                     blit(buffers[0].framebufferTexture, 1.0F, 1.0F, buffers[2], false, 0.0F, 0.0F);
                     blit(buffers[2].framebufferTexture, 1.0F, 1.0F, buffers[0], false, 0.0F, 0.0F);
                  }
               }
               preparedThisFrame = true;
               return;
            } catch (Throwable var13) {
               failed = true;
               preparedThisFrame = false;
               reportFailure(var13);
            } finally {
               restoreFramebuffer(var1, var2, var4, var5, var6, var16);
            }
         }
      }
   }
   public static void prepare() {
      prepare(1.0F);
   }
   public static void reset() {
      failed = false;
      failureLogged = false;
      preparedThisFrame = false;
      blurShaderFailed = false;
      roundShaderFailed = false;
      if (blurProg > 0) {
         try {
            GL20.glDeleteProgram(blurProg);
         } catch (Throwable var1) {
         }
         blurProg = -1;
      }
      if (roundProg > 0) {
         try {
            GL20.glDeleteProgram(roundProg);
         } catch (Throwable var0) {
         }
         roundProg = -1;
      }
   }
   static final class RawFramebuffer {
      final int framebufferObject;
      final int framebufferTexture;
      final int framebufferWidth;
      final int framebufferHeight;
      RawFramebuffer(int var1, int var2) {
         this.framebufferWidth = var1;
         this.framebufferHeight = var2;
         this.framebufferTexture = GL11.glGenTextures();
         GL11.glBindTexture(3553, this.framebufferTexture);
         GL11.glTexParameteri(3553, 10241, 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
         GL11.glTexParameteri(3553, 10242, 33071);
         GL11.glTexParameteri(3553, 10243, 33071);
         GL11.glTexImage2D(3553, 0, 32856, var1, var2, 0, 6408, 5121, (java.nio.ByteBuffer)null);
         this.framebufferObject = OpenGlHelper.glGenFramebuffers();
         OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, this.framebufferObject);
         OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, 36064, 3553, this.framebufferTexture, 0);
         if ((var1 = OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER)) != 36053) {
            this.delete();
            throw new IllegalStateException("blur framebuffer incomplete: ".concat(String.valueOf(var1)));
         } else {
            GL11.glBindTexture(3553, 0);
         }
      }
      final void delete() {
         OpenGlHelper.glDeleteFramebuffers(this.framebufferObject);
         GL11.glDeleteTextures(this.framebufferTexture);
      }
   }
}
