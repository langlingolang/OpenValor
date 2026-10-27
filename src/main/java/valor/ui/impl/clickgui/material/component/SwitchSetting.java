package valor.ui.impl.clickgui.material.component;

import java.awt.Color;
import valor.setting.BooleanProperty;
import valor.ui.impl.clickgui.material.Component;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.FloatInterpolator;
import valor.util.render.GreenRender;

public class SwitchSetting extends Component {
   private final BooleanProperty property;
   private final FloatInterpolator toggleAnim;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      if (this.isVisible()) {
         long var5 = System.currentTimeMillis();
         this.toggleAnim.interpolateTo(this.property.getValue() ? 1.0F : 0.0F, 150L, var5);
         float var7 = this.toggleAnim.getValue(var5);
         GreenRender.drawStringSmall(
            this.property.getName().replace("-", " "),
            (float)(super.x + 4),
            (float)super.y + ((float)super.height - GreenRender.fontHSmall()) / 2.0F + 0.5F,
            MaterialTheme.TEXT
         );
         var2 = super.x + super.width - 20 - 4;
         int var9 = super.y + (super.height - 11) / 2;
         Color var10 = hudAccent(var4);
         Color var11 = GreenRender.lerp(MaterialTheme.SWITCH_OFF, var10, var7);
         GreenRender.fillRR((float)var2, (float)var9, 20.0F, 11.0F, 11.0F / 2.0F, var11);
         GreenRender.fillRR((float)(var2 + 1) + var7 * 9.0F, (float)(var9 + 1), 9.0F, 9.0F, 9.0F / 2.0F, Color.WHITE);
      }
   }

   @Override
   public boolean isVisible() {
      return this.property.isVisible();
   }

   private static Color hudAccent(int var0) {
      return MaterialTheme.ACCENT_PURPLE;
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (!this.isVisible()) {
         return false;
      } else if (var3 == 0 && this.isHovered(var1, var2)) {
         this.property.setValue(!(Boolean)this.property.getValue());
         return true;
      } else {
         return false;
      }
   }

   public SwitchSetting(BooleanProperty var1, int var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.property = var1;
      this.toggleAnim = new FloatInterpolator(var1.getValue() ? 1.0F : 0.0F);
   }

   public BooleanProperty getProperty() {
      return this.property;
   }
}
