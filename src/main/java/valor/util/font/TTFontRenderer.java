package valor.util.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class TTFontRenderer {
   private static final char COLOR_INVOKER = '\u00a7';
   private static final Random RANDOM = new Random();
   private static final int RASTER_SCALE = 2;
   private static final float DRAW_SCALE = 0.25F;
   private final Font font;
   private final TTFontRenderer.CharacterData[] charData = new TTFontRenderer.CharacterData[65536];
   private final int[] colorCodes = new int[32];
   private final int margin = 12;
   private final boolean antiAlias;
   private final boolean fracMetrics;
   private boolean initialized = false;

   public float getHeight(String var1) {
      float var2 = 0.0F;
      int var3 = var1.length();

      for (int var4 = 0; var4 < var3; var4++) {
         char var5 = var1.charAt(var4);
         if ((var4 > 0 ? var1.charAt(var4 - 1) : 46) != 167 && var5 != 167 && this.isValid(var5)) {
            this.ensureGlyph(var5);
            TTFontRenderer.CharacterData var6;
            if ((var6 = this.charData[var5]) != null) {
               var2 = Math.max(var2, var6.height);
            }
         }
      }

      return (var2 - 12.0F) * 0.25F;
   }

   public float getHeight() {
      return (this.charData[65] != null ? this.charData[65].height : 60.0F) * 0.25F - 1.0F;
   }

   public float getWidth(String var1) {
      if (var1 != null && var1.length() != 0) {
         float var2 = 0.0F;
         int var3 = var1.length();

         for (int var4 = 0; var4 < var3; var4++) {
            char var5;
            if ((var5 = var1.charAt(var4)) != 167 && (var4 > 0 ? var1.charAt(var4 - 1) : 46) != 167 && this.isValid(var5)) {
               this.ensureGlyph(var5);
               TTFontRenderer.CharacterData var6;
               if ((var6 = this.charData[var5]) != null) {
                  var2 += var6.advance * 0.25F;
               }
            }
         }

         return var2;
      } else {
         return 0.0F;
      }
   }

   public void drawStringWithShadow(String var1, float var2, float var3, int var4) {
      GL11.glTranslated(0.5, 0.5, 0.0);
      this.renderString(var1, var2, var3, var4, true);
      GL11.glTranslated(-0.5, -0.5, 0.0);
      this.renderString(var1, var2, var3, var4, false);
   }

   private void renderString(CharSequence var1, float var2, float var3, int var4, boolean var5) {
      if (var1 != null && var1.length() != 0 && this.initialized) {
         GL11.glPushMatrix();
         if ((var4 & -67108864) == 0) {
            var4 |= -16777216;
         }

         if (var4 == 553648127) {
            var4 = -5263441;
         }

         GL11.glScalef(0.25F, 0.25F, 1.0F);
         var2 -= 3.0F;
         var3 -= 2.0F;
         var2 /= 0.25F;
         var3 /= 0.25F;
         boolean var6 = false;
         boolean var7 = false;
         boolean var8 = false;
         int var9 = var1.length();
         float var10 = (float)(var5 ? 4 : 1);
         float var11 = (float)(var4 >>> 24) / 255.0F;
         float var12 = (float)(var4 >> 16 & 0xFF) / 255.0F;
         float var13 = (float)(var4 >> 8 & 0xFF) / 255.0F;
         float var18 = (float)(var4 & 0xFF) / 255.0F;
         GlStateManager.enableBlend();
         GlStateManager.enableTexture2D();
         GlStateManager.blendFunc(770, 771);
         GlStateManager.color(var12 / var10, var13 / var10, var18 / var10, var11);

         for (int var19 = 0; var19 < var9; var19++) {
            char var20 = var1.charAt(var19);
            if ((var19 > 0 ? var1.charAt(var19 - 1) : 46) != 167) {
               if (var20 == 167) {
                  if (var19 + 1 >= var9) {
                     break;
                  }

                  int var21;
                  if ((var21 = "0123456789ABCDEFKLMNOR".indexOf(var1.charAt(var19 + 1))) < 16) {
                     var8 = false;
                     var7 = false;
                     var6 = false;
                     if (var21 < 0) {
                        var21 = 15;
                     }

                     if (var5) {
                        var21 += 16;
                     }

                     int var22;
                     GlStateManager.color(
                        (float)((var22 = this.colorCodes[var21]) >> 16) / 255.0F, (float)(var22 >> 8 & 0xFF) / 255.0F, (float)(var22 & 0xFF) / 255.0F, var11
                     );
                  } else if (var21 == 16) {
                     var8 = true;
                  } else if (var21 == 18) {
                     var7 = true;
                  } else if (var21 == 19) {
                     var6 = true;
                  } else {
                     var8 = false;
                     var7 = false;
                     var6 = false;
                     GlStateManager.color(1.0F / var10, 1.0F / var10, 1.0F / var10, var11);
                  }
               } else if (this.isValid(var20)) {
                  this.ensureGlyph(var20);
                  if (var8) {
                     var20 = (char)RANDOM.nextInt(256);
                  }

                  TTFontRenderer.CharacterData var23;
                  if ((var23 = this.charData[var20]) != null) {
                     this.drawChar(var23, var2, var3);
                     if (var7) {
                        this.drawLine(var2, var3 + var23.height / 2.0F, var2 + var23.width, var3 + var23.height / 2.0F, 3.0F);
                     }

                     if (var6) {
                        this.drawLine(var2, var3 + var23.height - 15.0F, var2 + var23.width, var3 + var23.height - 15.0F, 3.0F);
                     }

                     var2 += var23.advance;
                  }
               }
            }
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPopMatrix();
      }
   }

   public void generateTextures() {
      for (int var1 = 0; var1 < 256; var1++) {
         char var2 = (char)var1;
         if (this.isValid(var2)) {
            this.setup(var2);
         }
      }

      this.initialized = true;
   }

   private void ensureGlyph(char var1) {
      if (this.initialized && this.charData[var1] == null) {
         this.setup(var1);
      }
   }

   private void drawChar(TTFontRenderer.CharacterData var1, float var2, float var3) {
      var1.bind();
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2d((double)var2, (double)var3);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2d((double)var2, (double)(var3 + var1.height));
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2d((double)(var2 + var1.width), (double)(var3 + var1.height));
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2d((double)(var2 + var1.width), (double)var3);
      GL11.glEnd();
   }

   private void generateColors() {
      for (int var1 = 0; var1 < 32; var1++) {
         int var2 = (var1 >> 3 & 1) * 85;
         int var3 = (var1 >> 2 & 1) * 170 + var2;
         int var4 = (var1 >> 1 & 1) * 170 + var2;
         var2 = (var1 & 1) * 170 + var2;
         if (var1 == 6) {
            var3 += 85;
         }

         if (var1 >= 16) {
            var3 /= 4;
            var4 /= 4;
            var2 /= 4;
         }

         this.colorCodes[var1] = (var3 & 0xFF) << 16 | (var4 & 0xFF) << 8 | var2 & 0xFF;
      }
   }

   public void drawString(String var1, float var2, float var3, int var4) {
      this.renderString(var1, var2, var3, var4, false);
   }

   public void drawCenteredString(String var1, float var2, float var3, int var4) {
      this.drawString(var1, var2 - this.getWidth(var1) / 2.0F, var3, var4);
   }

   public void drawCenteredStringWithShadow(String var1, float var2, float var3, int var4) {
      this.drawStringWithShadow(var1, var2 - this.getWidth(var1) / 2.0F, var3, var4);
   }

   private void createTexture(int var1, BufferedImage var2) {
      int[] var3 = new int[var2.getWidth() * var2.getHeight()];
      var2.getRGB(0, 0, var2.getWidth(), var2.getHeight(), var3, 0, var2.getWidth());
      ByteBuffer var4 = BufferUtils.createByteBuffer(var2.getWidth() * var2.getHeight() << 2);

      for (int var5 = 0; var5 < var2.getHeight(); var5++) {
         for (int var6 = 0; var6 < var2.getWidth(); var6++) {
            int var7 = var3[var5 * var2.getWidth() + var6];
            var4.put((byte)(var7 >> 16));
            var4.put((byte)(var7 >> 8));
            var4.put((byte)var7);
            var4.put((byte)(var7 >>> 24));
         }
      }

      ((Buffer)var4).flip();
      GL11.glBindTexture(3553, var1);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexImage2D(3553, 0, 6408, var2.getWidth(), var2.getHeight(), 0, 6408, 5121, var4);
   }

   private void drawLine(float var1, float var2, float var3, float var4, float var5) {
      GlStateManager.disableTexture2D();
      GL11.glLineWidth(var5);
      GL11.glBegin(1);
      GL11.glVertex2f(var1, var2);
      GL11.glVertex2f(var3, var4);
      GL11.glEnd();
      GlStateManager.enableTexture2D();
   }

   public TTFontRenderer(Font var1, boolean var2, boolean var3) {
      this.generateColors();
      this.font = var1.deriveFont(var1.getSize2D() * 2.0F);
      this.antiAlias = var2;
      this.fracMetrics = var3;
   }

   private void setup(char var1) {
      if (this.charData[var1] == null) {
         Font var2 = this.font.canDisplay(var1) ? this.font : new Font("Dialog", 0, this.font.getSize());
         Graphics2D var3;
         (var3 = (Graphics2D)new BufferedImage(1, 1, 2).getGraphics()).setFont(var2);
         var3.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS, this.fracMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF
         );
         FontMetrics var4;
         Rectangle2D var5 = (var4 = var3.getFontMetrics()).getStringBounds(String.valueOf(var1), var3);
         BufferedImage var6;
         Graphics2D var7;
         (var7 = (Graphics2D)(var6 = new BufferedImage((int)StrictMath.ceil(var5.getWidth() + 24.0), (int)StrictMath.ceil(var5.getHeight()), 2)).getGraphics())
            .setFont(var2);
         var7.setColor(new Color(255, 255, 255, 0));
         var7.fillRect(0, 0, var6.getWidth(), var6.getHeight());
         var7.setColor(Color.WHITE);
         if (this.antiAlias) {
            var7.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         }

         if (this.fracMetrics) {
            var7.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         }

         var7.drawString(String.valueOf(var1), 12, var4.getAscent());
         float var8 = (float)var5.getWidth();
         var7.dispose();
         var3.dispose();
         int var9 = GL11.glGenTextures();
         this.createTexture(var9, var6);
         this.charData[var1] = new TTFontRenderer.CharacterData((float)var6.getWidth(), (float)var6.getHeight(), var8, var9);
      }
   }

   private boolean isValid(char var1) {
      return var1 > '\n' && var1 < 256 && var1 != 127
         ? true
         : var1 == 9745 || var1 == 9746 || var1 == 9989 || var1 >= 10003 && var1 <= 10008 || var1 == 10060 || var1 == 10062;
   }

   public static class CharacterData {
      private final int textureId;
      public final float width;
      public final float height;
      public final float advance;

      private CharacterData(float var1, float var2, float var3, int var4) {
         this.width = var1;
         this.height = var2;
         this.advance = var3;
         this.textureId = var4;
      }

      public void bind() {
         GlStateManager.enableTexture2D();
         GL11.glBindTexture(3553, this.textureId);
         GlStateManager.bindTexture(this.textureId);
      }
   }
}
