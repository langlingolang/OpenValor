package valor.util;

import java.awt.Color;

public final class ColorUtil {
   public static final Color RED = new Color(255, 0, 0);
   public static final Color YELLOW = new Color(255, 255, 0);
   public static final Color GREEN = new Color(0, 255, 0);

   public static Color getHealthColor(float ratio) {
      if (ratio >= 0.9F) {
         return GREEN;
      } else if (ratio >= 0.55F) {
         return lerp((ratio - 0.55F) / 0.35F, YELLOW, GREEN);
      } else if (ratio >= 0.45F) {
         return YELLOW;
      } else {
         return ratio >= 0.1F ? lerp((ratio - 0.1F) / 0.35F, RED, YELLOW) : RED;
      }
   }

   public static Color withAlpha(Color color, int alpha) {
      return color == null
         ? new Color(255, 255, 255, Math.max(0, Math.min(255, alpha)))
         : new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
   }

   public static Color darken(Color color) {
      int alpha = color.getAlpha();
      return new Color(
         Math.min(Math.max((int) ((float) color.getRed() * 0.2F), 0), 255),
         Math.min(Math.max((int) ((float) color.getGreen() * 0.2F), 0), 255),
         Math.min(Math.max((int) ((float) color.getBlue() * 0.2F), 0), 255),
         alpha
      );
   }

   public static Color lerp(float t, Color c1, Color c2) {
      t = Math.min(Math.max(t, 0.0F), 1.0F);
      return new Color(
         (int) ((float) c1.getRed() + t * (float) (c2.getRed() - c1.getRed())),
         (int) ((float) c1.getGreen() + t * (float) (c2.getGreen() - c1.getGreen())),
         (int) ((float) c1.getBlue() + t * (float) (c2.getBlue() - c1.getBlue()))
      );
   }

   public static Color fromHSB(float hue, float saturation) {
      return new Color(Color.HSBtoRGB(hue, saturation, 1.0F));
   }
}
