package valor.ui.impl.clickgui.material.modal;

import java.awt.Color;
import java.util.List;
import valor.core.ConfigManager;
import valor.core.HudThemeManager;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.ui.impl.clickgui.material.ThemePreset;
import valor.util.render.GreenRender;

public class ThemesModal extends Modal {
   private static final int MODAL_W = 460;
   private static final int MODAL_H = 360;
   private static final int CARD_COLS = 4;
   private static final float CARD_W = 103.0F;
   private static final float CARD_H = 48.0F;
   private static final float CARD_GAP = 6.0F;
   private static final int[] PALETTE = new int[]{
      -1490880,
      -1476544,
      -1458112,
      -3086016,
      -8328896,
      -12523200,
      -12523136,
      -12523072,
      -12533527,
      -12549911,
      -12566295,
      -8371991,
      -4177687,
      -1490752,
      -1490816,
      -1490848,
      -1490784,
      -6274839,
      -12541719,
      -12523104,
      -6231744,
      -1466304,
      -986896,
      -14671832
   };
   private static final int PAL_COLS = 6;
   private int picking = 0;

   @Override
   protected boolean clickBody(int var1, int var2, int var3) {
      if (var3 != 0) {
         return true;
      } else {
         float var12 = this.bodyX();
         float var4 = this.bodyY();
         float var5 = this.bodyW();
         float var6 = this.bodyH();
         HudThemeManager var7 = HudThemeManager.getInstance();
         List var8 = ThemePreset.all();
         int var9 = this.currentThemeIndex(var7, var8);
         if (this.picking != 0) {
            float var27 = (float)(PALETTE.length / 6 * 22 + 12);
            var5 = var12 + (var5 - 144.0F) / 2.0F;
            var6 = var4 + var6 - var27 - 28.0F;
            if ((float)var1 >= var5 && (float)var1 <= var5 + 144.0F && (float)var2 >= var6 && (float)var2 <= var6 + var27) {
               for (int var28 = 0; var28 < PALETTE.length; var28++) {
                  int var30 = var28 % 6;
                  var3 = var28 / 6;
                  var4 = var5 + 6.0F + (float)(var30 * 22);
                  float var15 = var6 + 6.0F + (float)(var3 * 22);
                  if ((float)var1 >= var4 && (float)var1 <= var4 + 18.0F && (float)var2 >= var15 && (float)var2 <= var15 + 18.0F) {
                     this.applyPickedColor(PALETTE[var28]);
                     return true;
                  }
               }

               return true;
            } else {
               this.picking = 0;
               return true;
            }
         } else {
            float var10 = var12 + var5 - 64.0F - 12.0F;
            var5 = var4 + 300.0F;
            if ((float)var1 >= var10 && (float)var1 <= var10 + 64.0F && (float)var2 >= var5 && (float)var2 <= var5 + 18.0F) {
               if (var7 != null) {
                  var7.hudStyleProp.setValue(1);
                  var7.guiSizeProp.setValue(1);
                  var7.hudSizeProp.setValue(1);
                  var7.fontProp.setValue(0);
                  MaterialTheme.invalidate();
               }

               this.applyPreset((ThemePreset)var8.get(3));
               return true;
            } else {
               if (var7 != null) {
                  int var20;
                  if ((var20 = this.choiceAt(var1, var2, var12 + 12.0F, var4 + 218.0F, 3)) >= 0) {
                     var7.guiSizeProp.setValue(var20);
                     this.saveTheme();
                     return true;
                  }

                  int var24;
                  if ((var24 = this.choiceAt(var1, var2, var12 + 12.0F, var4 + 244.0F, 3)) >= 0) {
                     var7.hudSizeProp.setValue(var24);
                     this.saveTheme();
                     return true;
                  }

                  int var11;
                  if ((var11 = this.choiceAt(var1, var2, var12 + 12.0F, var4 + 270.0F, 4)) >= 0) {
                     var7.fontProp.setValue(var11);
                     this.saveTheme();
                     return true;
                  }
               }

               if (var9 >= 0 && ((ThemePreset)var8.get(var9)).editable && var7 != null) {
                  var6 = var12 + 12.0F;
                  var10 = var4 + 190.0F;
                  if ((float)var2 >= var10 && (float)var2 <= var10 + 20.0F) {
                     if ((Integer)var7.colorProp.getValue() == 2) {
                        if ((float)var1 >= var6 && (float)var1 <= var6 + 28.0F) {
                           this.picking = 1;
                           return true;
                        }

                        if ((float)var1 >= var6 + 28.0F + 8.0F && (float)var1 <= var6 + 56.0F + 8.0F) {
                           this.picking = 2;
                           return true;
                        }
                     } else if ((float)var1 >= var6 && (float)var1 <= var6 + 28.0F) {
                        this.picking = 3;
                        return true;
                     }
                  }
               }

               var6 = var12 + 12.0F;
               var10 = var4 + 24.0F;
               if ((float)var2 >= var10 && (float)var2 <= var10 + 162.0F - 6.0F && (float)var1 >= var6 && (float)var1 <= var6 + 412.0F + 18.0F) {
                  int var29 = (int)(((float)var1 - var6) / 109.0F);
                  int var16 = ((var3 = (int)(((float)var2 - var10) / 54.0F)) << 2) + var29;
                  if (var29 >= 0 && var29 < 4 && var3 >= 0 && var3 < 3 && var16 < var8.size()) {
                     this.applyPreset((ThemePreset)var8.get(var16));
                     return true;
                  }
               }

               return true;
            }
         }
      }
   }

   @Override
   protected void renderBody(int var1, int var2, float var3) {
      var3 = this.bodyX();
      float var4 = this.bodyY();
      float var5 = this.bodyW();
      this.bodyH();
      HudThemeManager var6 = HudThemeManager.getInstance();
      List var7 = ThemePreset.all();
      int var8 = this.currentThemeIndex(var6, var7);
      GreenRender.drawStringSmall("COLOR THEME", var3 + 14.0F, var4 + 10.0F, MaterialTheme.TEXT_MUTED);
      float var9 = var3 + 12.0F;
      float var10 = var4 + 24.0F;

      for (int var11 = 0; var11 < var7.size(); var11++) {
         int var12 = var11 % 4;
         int var13 = var11 / 4;
         float var14 = var9 + (float)var12 * 109.0F;
         float var18 = var10 + (float)var13 * 54.0F;
         this.drawThemeCard((ThemePreset)var7.get(var11), var14, var18, var6, var8 == var11, var1, var2);
      }

      float var17 = var4 + 190.0F;
      this.drawColorEditor(var3 + 12.0F, var17, var5 - 24.0F, var6, var8, var7);
      this.drawChoiceRow("GUI SIZE", new String[]{"Compact", "Normal", "Large"}, (Integer)var6.guiSizeProp.getValue(), var3 + 12.0F, var4 + 218.0F, var1, var2);
      this.drawChoiceRow("HUD SIZE", new String[]{"Compact", "Normal", "Large"}, (Integer)var6.hudSizeProp.getValue(), var3 + 12.0F, var4 + 244.0F, var1, var2);
      this.drawChoiceRow("FONT", new String[]{"Inter", "Comfortaa", "Sans", "Vanilla"}, (Integer)var6.fontProp.getValue(), var3 + 12.0F, var4 + 270.0F, var1, var2);
      GreenRender.drawStringSmall("GUI size applies when you reopen the menu.", var3 + 12.0F, var4 + 302.0F, MaterialTheme.TEXT_MUTED);
      float var19 = var4 + 300.0F;
      float var20 = var3 + var5 - 64.0F - 12.0F;
      boolean var16 = (float)var1 >= var20 && (float)var1 <= var20 + 64.0F && (float)var2 >= var19 && (float)var2 <= var19 + 18.0F;
      GreenRender.fillRR(var20, var19, 64.0F, 18.0F, 5.0F, var16 ? MaterialTheme.MODAL_ROW_HOVER : MaterialTheme.MODAL_ROW);
      GreenRender.outlineRR(var20, var19, 64.0F, 18.0F, 5.0F, 1.0F, MaterialTheme.MODAL_BORDER);
      GreenRender.drawStringSmall(
         "Reset", var20 + (64.0F - GreenRender.strWSmall("Reset")) / 2.0F, var19 + (18.0F - GreenRender.fontHSmall()) / 2.0F + 0.5F, MaterialTheme.TEXT
      );
      if (this.picking != 0) {
         this.drawColorPopup(var1, var2);
      }
   }

   private int currentThemeIndex(HudThemeManager var1, List<ThemePreset> var2) {
      if (var1 == null) {
         return -1;
      } else {
         int var3 = (Integer)var1.colorProp.getValue();

         for (int var4 = 0; var4 < var2.size(); var4++) {
            ThemePreset var5;
            if (!(var5 = (ThemePreset)var2.get(var4)).editable && var5.colorMode == var3) {
               if (var3 == 2) {
                  if ((Integer)var1.customColor1Prop.getValue() == var5.custom1
                     && (Integer)var1.customColor2Prop.getValue() == var5.custom2
                     && Math.abs((Float)var1.colorSpeedProp.getValue() - var5.colorSpeed) < 0.01F) {
                     return var4;
                  }
               } else if (var3 == 6) {
                  if ((Integer)var1.staticColorProp.getValue() == var5.staticColor) {
                     return var4;
                  }
               } else if (Math.abs((Float)var1.colorSpeedProp.getValue() - var5.colorSpeed) < 0.01F) {
                  return var4;
               }
            }
         }

         for (int var6 = 0; var6 < var2.size(); var6++) {
            ThemePreset var7;
            if ((var7 = (ThemePreset)var2.get(var6)).editable && var7.colorMode == var3) {
               return var6;
            }
         }

         return -1;
      }
   }

   private int choiceAt(int var1, int var2, float var3, float var4, int var5) {
      var3 += 86.0F;
      float var6 = var5 == 4 ? 79.0F : 106.0F;

      for (int var7 = 0; var7 < var5; var7++) {
         float var8 = var3 + (float)var7 * (var6 + 4.0F);
         if ((float)var1 >= var8 && (float)var1 <= var8 + var6 && (float)var2 >= var4 && (float)var2 <= var4 + 22.0F) {
            return var7;
         }
      }

      return -1;
   }

   private void applyPickedColor(int var1) {
      HudThemeManager var2;
      if ((var2 = HudThemeManager.getInstance()) == null) {
         this.picking = 0;
      } else {
         switch (this.picking) {
            case 1:
               var2.customColor1Prop.setValue(var1);
               break;
            case 2:
               var2.customColor2Prop.setValue(var1);
               break;
            case 3:
               var2.staticColorProp.setValue(var1);
         }

         this.picking = 0;
         this.saveTheme();
      }
   }

   private void applyPreset(ThemePreset var1) {
      HudThemeManager var2;
      if ((var2 = HudThemeManager.getInstance()) != null) {
         var1.apply(var2);
         this.saveTheme();
      }
   }

   private void drawChoiceRow(String var1, String[] var2, int var3, float var4, float var5, int var6, int var7) {
      GreenRender.drawStringSmall(var1, var4, var5 + 5.0F, MaterialTheme.TEXT_MUTED);
      float var12 = var4 + 86.0F;
      var4 = var2.length == 4 ? 79.0F : 106.0F;

      for (int var8 = 0; var8 < var2.length; var8++) {
         float var9 = var12 + (float)var8 * (var4 + 4.0F);
         boolean var10 = (float)var6 >= var9 && (float)var6 <= var9 + var4 && (float)var7 >= var5 && (float)var7 <= var5 + 22.0F;
         Color var11 = MaterialTheme.hudAccent();
         GreenRender.fillRR(
            var9,
            var5,
            var4,
            22.0F,
            5.0F,
            var8 == var3 ? MaterialTheme.accent(var11, 0.22F) : (var10 ? MaterialTheme.MODAL_ROW_HOVER : MaterialTheme.MODAL_ROW)
         );
         if (var8 == var3) {
            GreenRender.outlineRR(var9, var5, var4, 22.0F, 5.0F, 1.0F, MaterialTheme.accent(var11, 0.52F));
         }

         GreenRender.drawStringSmall(
            var2[var8], var9 + (var4 - GreenRender.strWSmall(var2[var8])) / 2.0F, var5 + (22.0F - GreenRender.fontHSmall()) / 2.0F, MaterialTheme.TEXT
         );
      }
   }

   private void drawColorEditor(float var1, float var2, float var3, HudThemeManager var4, int var5, List<ThemePreset> var6) {
      if (var4 != null) {
         if (var5 >= 0 && ((ThemePreset)var6.get(var5)).editable) {
            if ((Integer)var4.colorProp.getValue() == 2) {
               this.drawColorSlot(var1, var2, 28.0F, 20.0F, (Integer)var4.customColor1Prop.getValue(), "1", 1);
               this.drawColorSlot(var1 + 28.0F + 8.0F, var2, 28.0F, 20.0F, (Integer)var4.customColor2Prop.getValue(), "2", 2);
            } else {
               this.drawColorSlot(var1, var2, 28.0F, 20.0F, (Integer)var4.staticColorProp.getValue(), "S", 3);
            }
         }
      }
   }

   private void drawColorPopup(int var1, int var2) {
      float var3 = (float)(PALETTE.length / 6 * 22 + 12);
      float var4 = this.bodyX() + (this.bodyW() - 144.0F) / 2.0F;
      float var5 = this.bodyY() + this.bodyH() - var3 - 28.0F;
      GreenRender.fillRR(var4 - 2.0F, var5 - 2.0F, 148.0F, var3 + 4.0F, 8.0F, new Color(0, 0, 0, 140));
      GreenRender.fillRR(var4, var5, 144.0F, var3, 8.0F, MaterialTheme.MODAL_BG);
      GreenRender.outlineRR(var4, var5, 144.0F, var3, 8.0F, 1.2F, MaterialTheme.hudAccent());

      for (int var9 = 0; var9 < PALETTE.length; var9++) {
         int var6 = var9 % 6;
         int var7 = var9 / 6;
         float var10 = var4 + 6.0F + (float)(var6 * 22);
         float var11 = var5 + 6.0F + (float)(var7 * 22);
         boolean var8 = (float)var1 >= var10 && (float)var1 <= var10 + 18.0F && (float)var2 >= var11 && (float)var2 <= var11 + 18.0F;
         GreenRender.fillRR(var10, var11, 18.0F, 18.0F, 4.0F, new Color(PALETTE[var9], true));
         if (var8) {
            GreenRender.outlineRR(var10, var11, 18.0F, 18.0F, 4.0F, 1.4F, Color.WHITE);
         }
      }
   }

   private void saveTheme() {
      new ConfigManager(ConfigManager.currentConfig, false).saveQuietly();
   }

   private void drawThemeCard(ThemePreset var1, float var2, float var3, HudThemeManager var4, boolean var5, int var6, int var7) {
      boolean var14 = (float)var6 >= var2 && (float)var6 <= var2 + 103.0F && (float)var7 >= var3 && (float)var7 <= var3 + 48.0F;
      Color var15 = MaterialTheme.hudAccent();
      Color var8 = var5 ? MaterialTheme.accent(var15, 0.62F) : (var14 ? MaterialTheme.TEXT_MUTED : MaterialTheme.MODAL_BORDER);
      GreenRender.fillRR(
         var2, var3, 103.0F, 48.0F, 7.0F, var5 ? MaterialTheme.accent(var15, 0.1F) : (var14 ? MaterialTheme.MODAL_ROW_HOVER : MaterialTheme.MODAL_ROW)
      );
      GreenRender.outlineRR(var2, var3, 103.0F, 48.0F, 7.0F, 1.3F, var8);
      Color[] var11 = this.swatchColors(var1, var4);
      GreenRender.fillRRGradient(var2 + 8.0F, var3 + 6.0F, 87.0F, 13.0F, 4.0F, var11[0], var11[1], true);
      Color var12 = var5 ? MaterialTheme.TEXT : MaterialTheme.TEXT_MUTED;
      GreenRender.drawStringSmall(var1.name, var2 + (103.0F - GreenRender.strWSmall(var1.name)) / 2.0F, var3 + 23.0F, var12);
      if (var1.editable) {
         String var9 = "editable";
         float var13 = GreenRender.strWSmall("editable");
         var2 = var2 + 103.0F - var13 - 8.0F;
         GreenRender.drawStringSmall(var9, var2, var3 + 48.0F - 13.0F, MaterialTheme.TEXT_MUTED);
      }
   }

   private void drawColorSlot(float var1, float var2, float var3, float var4, int var5, String var6, int var7) {
      boolean var8 = this.picking == var7;
      GreenRender.fillRR(var1, var2, var3, var4, 4.0F, new Color(var5, true));
      GreenRender.outlineRR(var1, var2, var3, var4, 4.0F, 1.0F, var8 ? MaterialTheme.hudAccent() : MaterialTheme.MODAL_BORDER);
      GreenRender.drawStringSmall(var6, var1 + var3 / 2.0F - GreenRender.strWSmall(var6) / 2.0F, var2 + var4 + 1.0F, MaterialTheme.TEXT_MUTED);
   }

   private Color[] swatchColors(ThemePreset var1, HudThemeManager var2) {
      switch (var1.colorMode) {
         case 0:
            return new Color[]{new Color(255, 80, 80), new Color(80, 80, 255)};
         case 1:
            return new Color[]{new Color(255, 140, 180), new Color(180, 80, 200)};
         case 2:
         default:
            int var5 = var1.editable ? (Integer)var2.customColor1Prop.getValue() : var1.custom1;
            int var4 = var1.editable ? (Integer)var2.customColor2Prop.getValue() : var1.custom2;
            return new Color[]{new Color(var5, true), new Color(var4, true)};
         case 3:
            return new Color[]{new Color(255, 90, 90), new Color(255, 140, 100)};
         case 4:
            return new Color[]{new Color(255, 80, 180), new Color(220, 40, 140)};
         case 5:
            return new Color[]{new Color(80, 220, 255), new Color(40, 160, 230)};
         case 6:
            int var3 = var1.editable ? (Integer)var2.staticColorProp.getValue() : var1.staticColor;
            return new Color[]{new Color(var3, true), new Color(var3, true)};
      }
   }

   public ThemesModal() {
      super("Themes", 460, 360);
   }
}
