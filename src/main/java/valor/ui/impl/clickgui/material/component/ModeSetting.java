package valor.ui.impl.clickgui.material.component;

import java.awt.Color;
import valor.setting.ModeProperty;
import valor.ui.impl.clickgui.material.Component;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.render.GreenRender;

public class ModeSetting extends Component {
   private final ModeProperty property;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      if (this.isVisible()) {
         if (this.isHovered(var1, var2)) {
            GreenRender.fillRR(
               (float)(super.x + 2), (float)(super.y + 1), (float)(super.width - 4), (float)(super.height - 2), 5.0F, new Color(40, 42, 52, 120)
            );
         }

         String var5 = this.property.getName().replace("-", " ");
         String var6 = this.property.getModeName().replace("_", " ");
         GreenRender.drawStringSmall(var5, (float)(super.x + 6), (float)(super.y + 3), MaterialTheme.TEXT_MUTED);
         GreenRender.drawStringSmall(var6, (float)(super.x + 6), (float)(super.y + 15), MaterialTheme.ACCENT_PURPLE);
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
      } else if (!this.isHovered(var1, var2)) {
         return false;
      } else if (var3 == 0) {
         this.property.nextMode();
         return true;
      } else if (var3 == 1) {
         this.property.previousMode();
         return true;
      } else {
         return false;
      }
   }

   public ModeSetting(ModeProperty var1, int var2, int var3, int var4, int var5) {
      super(var2, var3, var4, 28);
      this.property = var1;
   }

   public ModeProperty getProperty() {
      return this.property;
   }
}
