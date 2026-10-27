package valor.ui.impl.clickgui.material.component;

import java.awt.Color;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;
import valor.setting.ColorProperty;
import valor.ui.impl.clickgui.material.Component;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.Render3DUtil;
import valor.util.render.GreenRender;

public class ColorSetting extends Component {
   private final ColorProperty property;
   private boolean draggingHue;
   private boolean draggingSat;
   private boolean draggingBri;
   private float hue;
   private float saturation;
   private float brightness;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      if (this.isVisible()) {
         if (!Mouse.isButtonDown(0)) {
            this.draggingHue = this.draggingSat = this.draggingBri = false;
         }

         if (!this.draggingHue && !this.draggingSat && !this.draggingBri) {
            this.syncFromProperty();
         } else {
            var2 = super.x + 4;
            int var11 = super.width - 8;
            if (this.draggingHue) {
               this.hue = clamp((float)(var1 - var2) / (float)var11);
               this.applyColor();
            } else if (this.draggingSat) {
               this.saturation = clamp((float)(var1 - var2) / (float)var11);
               this.applyColor();
            } else if (this.draggingBri) {
               this.brightness = clamp((float)(var1 - var2) / (float)var11);
               this.applyColor();
            }
         }

         GreenRender.drawStringSmall(this.property.getName().replace("-", " "), (float)(super.x + 4), (float)(super.y + 2), MaterialTheme.TEXT_MUTED);
         int var12 = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
         Gui.drawRect(super.x + super.width - 12, super.y + 2, super.x + super.width - 4, super.y + 10, var12 | 0xFF000000);
         var1 = super.x + 4;
         var2 = super.width - 8;
         int var13;
         int var5 = (var4 = (var13 = super.y + 12) + 6) + 6;

         for (int var6 = 0; var6 < var2; var6++) {
            float var7 = (float)var6 / (float)var2;
            Gui.drawRect(var1 + var6, var13, var1 + var6 + 1, var13 + 4, Color.HSBtoRGB(var7, 1.0F, 1.0F) | 0xFF000000);
         }

         Render3DUtil.drawGradientRect(var1, var4, (float)(var1 + var2), var4 + 4, Color.WHITE.getRGB(), Color.getHSBColor(this.hue, 1.0F, 1.0F).getRGB());
         Render3DUtil.drawGradientRect(var1, var5, (float)(var1 + var2), var5 + 4, Color.BLACK.getRGB(), Color.getHSBColor(this.hue, this.saturation, 1.0F).getRGB());
         this.drawPointer(var1, var13, var2, this.hue);
         this.drawPointer(var1, var4, var2, this.saturation);
         this.drawPointer(var1, var5, var2, this.brightness);
      }
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   public boolean isVisible() {
      return this.property.isVisible();
   }

   private boolean overBar(int var1, int var2, int var3, int var4, int var5) {
      return var1 >= var3 && var1 <= var3 + var4 && var2 >= var5 && var2 <= var5 + 4;
   }

   private void syncFromProperty() {
      Color var1;
      float[] var2 = Color.RGBtoHSB((var1 = new Color((Integer)this.property.getValue())).getRed(), var1.getGreen(), var1.getBlue(), null);
      this.hue = var2[0];
      this.saturation = var2[1];
      this.brightness = var2[2];
   }

   private void drawPointer(int var1, int var2, int var3, float var4) {
      int var5;
      Gui.drawRect((var5 = var1 + (int)((float)var3 * var4)) - 1, var2 - 1, var5 + 1, var2 + 5, -1);
   }

   private static float clamp(float var0) {
      return Math.max(0.0F, Math.min(1.0F, var0));
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
      this.draggingHue = this.draggingSat = this.draggingBri = false;
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (this.isVisible() && var3 == 0) {
         var3 = super.x + 4;
         int var4 = super.width - 8;
         int var5;
         int var6;
         int var7 = (var6 = (var5 = super.y + 12) + 6) + 6;
         if (this.overBar(var1, var2, var3, var4, var5)) {
            this.draggingHue = true;
            this.hue = clamp((float)(var1 - var3) / (float)var4);
            this.applyColor();
            return true;
         } else if (this.overBar(var1, var2, var3, var4, var6)) {
            this.draggingSat = true;
            this.saturation = clamp((float)(var1 - var3) / (float)var4);
            this.applyColor();
            return true;
         } else if (this.overBar(var1, var2, var3, var4, var7)) {
            this.draggingBri = true;
            this.brightness = clamp((float)(var1 - var3) / (float)var4);
            this.applyColor();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void applyColor() {
      int var1 = Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215;
      this.property.setValue(var1);
   }

   public ColorSetting(ColorProperty var1, int var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.property = var1;
      this.syncFromProperty();
   }

   public ColorProperty getProperty() {
      return this.property;
   }
}
