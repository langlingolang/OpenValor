package valor.util.render;

import java.awt.Color;
import org.lwjgl.opengl.GL11;

public final class WorldOverlayStyle {
   public static void drawPill(float var0, float var1, float var2, float var3, float var4, Color var5, int var6) {
      drawRounded(var0 + 0.5F, var1 + 1.5F, var2 + 0.5F, var3 + 1.5F, var4, new Color(0, 0, 0, 58));
      int var7 = Math.min(255, 27 + var5.getRed() / 35);
      int var8 = Math.min(255, 29 + var5.getGreen() / 35);
      int var9 = Math.min(255, 44 + var5.getBlue() / 35);
      drawRounded(var0, var1, var2, var3, var4, new Color(var7, var8, var9, Math.max(0, Math.min(255, var6))));
   }

   private static void drawRounded(float var0, float var1, float var2, float var3, float var4, Color var5) {
      var4 = Math.min(var4, Math.min(var2 - var0, var3 - var1) / 2.0F);
      GL11.glColor4f((float)var5.getRed() / 255.0F, (float)var5.getGreen() / 255.0F, (float)var5.getBlue() / 255.0F, (float)var5.getAlpha() / 255.0F);
      GL11.glBegin(6);
      GL11.glVertex2f((var0 + var2) / 2.0F, (var1 + var3) / 2.0F);
      float[] var9 = new float[]{var2 - var4, var2 - var4, var0 + var4, var0 + var4};
      float[] var10 = new float[]{var1 + var4, var3 - var4, var3 - var4, var1 + var4};

      for (int var12 = 0; var12 < 4; var12++) {
         for (int var6 = 0; var6 <= 6; var6++) {
            double var7 = Math.toRadians((double)(-90 + var12 * 90 + var6 * 15));
            GL11.glVertex2f(var9[var12] + var4 * (float)Math.cos(var7), var10[var12] + var4 * (float)Math.sin(var7));
         }
      }

      GL11.glVertex2f(var2 - var4, var1);
      GL11.glEnd();
   }

   private WorldOverlayStyle() {
   }
}
