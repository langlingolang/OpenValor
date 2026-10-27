package valor.ui.impl.clickgui.material.component;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.lwjgl.input.Mouse;
import valor.ui.dataset.Slider;
import valor.ui.impl.clickgui.material.Component;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.render.GreenRender;

public class SliderSetting extends Component {
   private final Slider slider;
   private boolean dragging;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      if (this.isVisible()) {
         if (this.dragging) {
            if (Mouse.isButtonDown(0)) {
               this.updateValue(var1);
            } else {
               this.dragging = false;
            }
         }

         String var9 = this.slider.getName();
         String var12 = this.slider.getValueString();
         GreenRender.drawStringSmall(var9, (float)(super.x + 4), (float)(super.y + 2), MaterialTheme.TEXT_MUTED);
         float var10 = GreenRender.strWSmall(var12);
         GreenRender.drawStringSmall(var12, (float)(super.x + super.width) - var10 - 4.0F, (float)(super.y + 2), MaterialTheme.TEXT);
         float var11 = (float)(super.x + 4);
         float var13 = (float)(super.y + super.height - 7);
         var3 = (float)(super.width - 8);
         GreenRender.fillRR(var11, var13, var3, 3.0F, 3.0F / 2.0F, MaterialTheme.TRACK);
         double var5;
         double var7 = (var5 = this.slider.getMax() - this.slider.getMin()) == 0.0 ? 0.0 : (this.slider.getInput() - this.slider.getMin()) / var5;
         var7 = Math.max(0.0, Math.min(1.0, var7));
         var3 = (float)((double)var3 * var7);
         Color var16 = hudAccent(var4);
         if (var3 > 0.5F) {
            GreenRender.fillRR(var11, var13, var3, 3.0F, 3.0F / 2.0F, var16);
         }

         GreenRender.fillRR(var11 + var3 - 2.5F, var13 - 1.5F, 5.0F, 6.0F, 2.5F, Color.WHITE);
      }
   }

   @Override
   public boolean isVisible() {
      return this.slider.isVisible();
   }

   private void updateValue(int var1) {
      float var2 = (float)(super.x + 4);
      float var3 = (float)(super.width - 8);
      double var4 = (double)(((float)var1 - var2) / var3);
      var4 = Math.max(0.0, Math.min(1.0, var4));
      double var6 = this.slider.getMin() + (this.slider.getMax() - this.slider.getMin()) * var4;
      double var8;
      if ((var8 = this.slider.getIncrement()) > 0.0) {
         var6 = (double)Math.round(var6 / var8) * var8;
      }

      BigDecimal var12 = BigDecimal.valueOf(var6).setScale(2, RoundingMode.HALF_UP);
      double var10 = Math.max(this.slider.getMin(), Math.min(this.slider.getMax(), var12.doubleValue()));
      this.slider.setValue(var10);
   }

   private static Color hudAccent(int var0) {
      return MaterialTheme.ACCENT_PURPLE;
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
      this.dragging = false;
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (!this.isVisible()) {
         return false;
      } else if (var3 == 0 && var1 >= super.x && var1 <= super.x + super.width && var2 >= super.y + super.height - 12 && var2 <= super.y + super.height) {
         this.dragging = true;
         this.updateValue(var1);
         return true;
      } else {
         return false;
      }
   }

   public SliderSetting(Slider var1, int var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.slider = var1;
   }
}
