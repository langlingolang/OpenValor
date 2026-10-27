package valor.ui.impl.clickgui.material;

import java.awt.Color;
import valor.core.HudThemeManager;
import valor.util.render.BlurPipeline;
import valor.util.render.GreenRender;

public final class MaterialTheme {
   public static final float CORNER_RADIUS = 7.0F;
   public static final float MODULE_RADIUS = 5.0F;
   public static int FRAME_WIDTH = 110;
   public static int HEADER_HEIGHT = 22;
   public static int MODULE_HEIGHT = 16;
   public static int SETTING_HEIGHT = 18;
   public static int SEARCH_WIDTH = 242;
   public static int SEARCH_HEIGHT = 25;
   public static final Color ACCENT_PURPLE = new Color(174, 157, 218);
   public static final Color ACCENT_BLUE = new Color(70, 110, 210);
   public static final Color ACCENT_MAGENTA = new Color(160, 70, 170);
   public static final Color ACCENT_INDIGO = new Color(90, 95, 200);
   public static Color PANEL_BG = new Color(24, 26, 43, 197);
   public static Color PANEL_BG_BOTTOM = new Color(20, 23, 38, 207);
   public static Color HEADER_BG = new Color(13, 23, 37, 188);
   public static Color HEADER_BG_END = new Color(9, 18, 30, 198);
   public static final Color CATEGORY_HEADER_BG = new Color(22, 22, 38, 158);
   public static Color SETTINGS_BG = new Color(4, 8, 15, 154);
   public static Color OVERLAY = new Color(12, 15, 28, 20);
   public static Color OVERLAY_TINT = new Color(84, 88, 134, 8);
   public static Color TEXT = new Color(244, 247, 250);
   public static Color TEXT_MUTED = new Color(164, 165, 184);
   public static Color TEXT_DISABLED = new Color(196, 197, 211);
   public static Color SEARCH_BG = new Color(23, 24, 41, 214);
   public static Color TRACK = new Color(33, 43, 57, 210);
   public static Color SWITCH_OFF = new Color(48, 57, 69);
   public static Color SHADOW = new Color(0, 0, 0, 76);
   public static final Color GLASS_EDGE = new Color(190, 214, 232, 0);
   public static final Color GLASS_EDGE_SOFT = new Color(190, 214, 232, 0);
   public static final Color ROW_HOVER = new Color(120, 150, 176, 24);
   public static Color MODAL_OVERLAY = new Color(0, 0, 0, 130);
   public static Color MODAL_BG = new Color(7, 13, 22, 226);
   public static Color MODAL_BG_BOTTOM = new Color(4, 8, 15, 232);
   public static Color MODAL_HEADER = new Color(14, 24, 37, 240);
   public static Color MODAL_ROW = new Color(18, 27, 39, 188);
   public static Color MODAL_ROW_HOVER = new Color(27, 39, 54, 224);
   public static Color MODAL_ROW_ACTIVE = new Color(40, 49, 69, 238);
   public static Color MODAL_DANGER = new Color(200, 70, 80);
   public static Color MODAL_OK = new Color(90, 170, 100);
   public static Color MODAL_BADGE_MSA = new Color(90, 130, 220);
   public static Color MODAL_BADGE_OFFLINE = new Color(140, 140, 150);
   public static Color MODAL_BORDER = new Color(158, 186, 208, 40);
   public static Color MODAL_TOPBAR_BG = new Color(7, 13, 22, 196);
   public static Color MODAL_TOPBAR_HOVER = new Color(24, 36, 50, 230);

   public static void applySize() {
      int var0;
      FRAME_WIDTH = (var0 = (Integer)HudThemeManager.getInstance().guiSizeProp.getValue()) == 0 ? 104 : (var0 == 2 ? 128 : 110);
      HEADER_HEIGHT = var0 == 0 ? 20 : (var0 == 2 ? 26 : 22);
      MODULE_HEIGHT = var0 == 0 ? 15 : (var0 == 2 ? 20 : 16);
      SETTING_HEIGHT = var0 == 0 ? 17 : (var0 == 2 ? 22 : 18);
      SEARCH_WIDTH = var0 == 0 ? 224 : (var0 == 2 ? 264 : 242);
      SEARCH_HEIGHT = var0 == 0 ? 23 : (var0 == 2 ? 29 : 25);
   }

   public static Color accent(Color var0, float var1) {
      return new Color(var0.getRed(), var0.getGreen(), var0.getBlue(), Math.max(0, Math.min(255, (int)(var1 * 255.0F))));
   }

   public static Color enableAccent(String var0) {
      return ACCENT_PURPLE;
   }

   public static Color hudAccent() {
      HudThemeManager var0;
      return (var0 = HudThemeManager.getInstance()) != null ? var0.getColor(System.currentTimeMillis(), 0L) : ACCENT_PURPLE;
   }

   public static boolean isLight() {
      return false;
   }

   public static Color lighten(Color var0, float var1) {
      return new Color(
         Math.min(255, (int)((float)var0.getRed() + (float)(255 - var0.getRed()) * var1)),
         Math.min(255, (int)((float)var0.getGreen() + (float)(255 - var0.getGreen()) * var1)),
         Math.min(255, (int)((float)var0.getBlue() + (float)(255 - var0.getBlue()) * var1)),
         var0.getAlpha()
      );
   }

   public static int withAlpha(Color var0, int var1) {
      return new Color(var0.getRed(), var0.getGreen(), var0.getBlue(), Math.max(0, Math.min(255, var1))).getRGB();
   }

   public static void drawGlass(float var0, float var1, float var2, float var3, float var4) {
      BlurPipeline.drawRegionRounded(var0, var1, var2, var3, var4);
      GreenRender.fillRRGradient(var0, var1, var2, var3, var4, PANEL_BG, PANEL_BG_BOTTOM, false);
   }

   public static void drawOverflowFade(float var0, float var1, float var2, int var3) {
      if (var3 > 0) {
         var3 = Math.round(215.0F * Math.min(1.0F, (float)var3 / 28.0F));
         GreenRender.fillRRGradient(var0 + 1.0F, var1 - 28.0F, var2 - 2.0F, 28.0F, 6.0F, new Color(5, 10, 18, 0), new Color(5, 10, 18, var3), false);
      }
   }

   public static Color darken(Color var0, float var1) {
      return new Color(
         Math.max(0, (int)((float)var0.getRed() * (1.0F - var1))),
         Math.max(0, (int)((float)var0.getGreen() * (1.0F - var1))),
         Math.max(0, (int)((float)var0.getBlue() * (1.0F - var1))),
         var0.getAlpha()
      );
   }

   public static void refresh() {
   }

   public static void invalidate() {
   }

   private MaterialTheme() {
   }
}
