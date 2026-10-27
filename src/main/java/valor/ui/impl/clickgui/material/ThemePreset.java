package valor.ui.impl.clickgui.material;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import valor.core.HudThemeManager;

public final class ThemePreset {
   public final String name;
   public final int colorMode;
   public final int custom1;
   public final int custom2;
   public final int staticColor;
   public final float colorSpeed;
   public final float colorDistance;
   public final boolean editable;

   private static ThemePreset custom(String var0, int var1, int var2, float var3) {
      return new ThemePreset(var0, 2, var1, var2, 0, var3, 10.0F, false);
   }

   private static ThemePreset preset(String var0, int var1, float var2) {
      return new ThemePreset(var0, var1, 0, 0, 0, var2, 10.0F, false);
   }

   private static ThemePreset stat(String var0, int var1, float var2) {
      return new ThemePreset(var0, 6, 0, 0, var1, var2, 10.0F, false);
   }

   private ThemePreset(String var1, int var2, int var3, int var4, int var5, float var6, float var7, boolean var8) {
      this.name = var1;
      this.colorMode = var2;
      this.custom1 = var3;
      this.custom2 = var4;
      this.staticColor = var5;
      this.colorSpeed = var6;
      this.colorDistance = var7;
      this.editable = var8;
   }

   public final void apply(HudThemeManager var1) {
      if (var1 != null) {
         var1.colorProp.setValue(this.colorMode);
         if (!this.editable) {
            var1.customColor1Prop.setValue(this.custom1);
            var1.customColor2Prop.setValue(this.custom2);
            var1.staticColorProp.setValue(this.staticColor);
            var1.colorSpeedProp.setValue(this.colorSpeed);
            var1.colorDistProp.setValue(this.colorDistance);
         }
      }
   }

   public static List<ThemePreset> all() {
      ArrayList var0;
      (var0 = new ArrayList()).add(preset("Rainbow", 0, 0.6F));
      var0.add(preset("Astolfo", 1, 0.45F));
      var0.add(preset("Rise", 3, 0.45F));
      var0.add(preset("Raven", 4, 0.45F));
      var0.add(preset("Slinky", 5, 0.45F));
      var0.add(custom("Sunset", new Color(255, 140, 60).getRGB(), new Color(255, 80, 160).getRGB(), 0.5F));
      var0.add(custom("Ocean", new Color(80, 220, 255).getRGB(), new Color(40, 100, 200).getRGB(), 0.5F));
      var0.add(custom("Candy", new Color(255, 120, 220).getRGB(), new Color(160, 100, 255).getRGB(), 0.55F));
      var0.add(stat("Toxic", new Color(120, 255, 80).getRGB(), 0.45F));
      var0.add(stat("Mono", new Color(235, 235, 240).getRGB(), 0.45F));
      var0.add(new ThemePreset("Custom", 2, new Color(85, 255, 255).getRGB(), new Color(255, 85, 255).getRGB(), 0, 0.45F, 10.0F, true));
      var0.add(new ThemePreset("Solid", 6, 0, 0, new Color(255, 85, 255).getRGB(), 0.45F, 10.0F, true));
      return var0;
   }
}
