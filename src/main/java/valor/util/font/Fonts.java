package valor.util.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import valor.util.ResourceUtil;

public class Fonts {
   private static Fonts instance;
   public final TTFontRenderer comfortaa = new TTFontRenderer(this.loadFont("comfortaa.ttf", 20), true, true);
   public final TTFontRenderer comfortaaSmall = new TTFontRenderer(this.loadFont("comfortaa.ttf", 16), true, true);
   public final TTFontRenderer comfortaaCompact = new TTFontRenderer(this.loadFont("comfortaa.ttf", 14), true, true);
   public final TTFontRenderer comfortaaMedium = new TTFontRenderer(this.loadFont("comfortaa.ttf", 18), true, true);
   public final TTFontRenderer guiCategory = new TTFontRenderer(this.loadFont("poppins_bold.ttf", 18), true, true);
   public final TTFontRenderer guiModule = new TTFontRenderer(this.loadFont("poppins_bold.ttf", 12), true, true);
   public final TTFontRenderer guiSub = new TTFontRenderer(this.loadFont("poppins_bold.ttf", 10), true, true);
   public final TTFontRenderer interNormal = new TTFontRenderer(this.loadFont("Inter-Regular.ttf", 18), true, true);
   public final TTFontRenderer interHud = new TTFontRenderer(this.loadFont("Inter-Regular.ttf", 16), true, true);
   public final TTFontRenderer interSmall = new TTFontRenderer(this.loadFont("Inter-Regular.ttf", 14), true, true);
   public final TTFontRenderer interTiny = new TTFontRenderer(this.loadFont("Inter-Regular.ttf", 12), true, true);
   public final TTFontRenderer interBold = new TTFontRenderer(this.loadFont("Inter-Bold.ttf", 18), true, true);
   public final TTFontRenderer interLarge = new TTFontRenderer(this.loadFont("Inter-Regular.ttf", 20), true, true);
   public final TTFontRenderer interBoldCompact = new TTFontRenderer(this.loadFont("Inter-Bold.ttf", 16), true, true);
   public final TTFontRenderer interBoldLarge = new TTFontRenderer(this.loadFont("Inter-Bold.ttf", 20), true, true);
   public final TTFontRenderer sansNormal = new TTFontRenderer(this.loadFont("sans.ttf", 18), true, true);
   public final TTFontRenderer sansHud = new TTFontRenderer(this.loadFont("sans.ttf", 16), true, true);
   public final TTFontRenderer sansSmall = new TTFontRenderer(this.loadFont("sans.ttf", 14), true, true);
   public final TTFontRenderer sansLarge = new TTFontRenderer(this.loadFont("sans.ttf", 20), true, true);

   private Font loadFont(String var1, int var2) {
      try {
         InputStream var3;
         if ((var3 = ResourceUtil.getResourceStream("assets/valor/".concat(String.valueOf(var1)))) == null) {
            System.err.println("[Valor] Font not found: ".concat(String.valueOf(var1)));
            return new Font("Sans-serif", 0, var2);
         } else {
            return Font.createFont(0, var3).deriveFont(0, (float)var2);
         }
      } catch (IOException | FontFormatException var4) {
         System.err.println("[Valor] Failed to load font: " + var1 + " - " + var4.getMessage());
         return new Font("Sans-serif", 0, var2);
      }
   }

   public static boolean isReady() {
      return instance != null;
   }

   private Fonts() {
   }

   public static Fonts get() {
      return instance;
   }

   public static void init() {
      if (instance == null) {
         (instance = new Fonts()).comfortaa.generateTextures();
         instance.comfortaaSmall.generateTextures();
         instance.comfortaaCompact.generateTextures();
         instance.comfortaaMedium.generateTextures();
         instance.guiCategory.generateTextures();
         instance.guiModule.generateTextures();
         instance.guiSub.generateTextures();
         instance.interNormal.generateTextures();
         instance.interHud.generateTextures();
         instance.interSmall.generateTextures();
         instance.interTiny.generateTextures();
         instance.interBold.generateTextures();
         instance.interLarge.generateTextures();
         instance.interBoldCompact.generateTextures();
         instance.interBoldLarge.generateTextures();
         instance.sansNormal.generateTextures();
         instance.sansHud.generateTextures();
         instance.sansSmall.generateTextures();
         instance.sansLarge.generateTextures();
         System.out.println("[Valor] Custom fonts initialized");
      }
   }
}
