package valor.ui.impl.clickgui.material.component;

import java.awt.Color;
import valor.core.HudThemeManager;
import valor.setting.TextProperty;
import valor.ui.callback.GuiInput;
import valor.ui.impl.clickgui.material.Component;
import valor.ui.impl.clickgui.material.MaterialClickGui;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.render.GreenRender;

public class TextSetting extends Component {
   private final TextProperty property;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      if (this.isVisible()) {
         if (this.isHovered(var1, var2)) {
            GreenRender.fillRR(
               (float)(super.x + 2), (float)(super.y + 1), (float)(super.width - 4), (float)(super.height - 2), 5.0F, new Color(40, 42, 52, 120)
            );
         }

         String var7 = this.property.getName().replace("-", " ");
         String var8 = (String)this.property.getValue();
         GreenRender.drawStringSmall(var7, (float)(super.x + 6), (float)(super.y + 3), MaterialTheme.TEXT_MUTED);
         GreenRender.pushScissor((float)(super.x + 6), (float)(super.y + 14), (float)(super.width - 12), 12.0F);

         try {
            GreenRender.drawStringSmall(var8.isEmpty() ? "Set value" : var8, (float)(super.x + 6), (float)(super.y + 15), MaterialTheme.ACCENT_PURPLE);
         } finally {
            GreenRender.popScissor();
         }
      }
   }

   @Override
   public boolean isVisible() {
      return this.property.isVisible();
   }

   private static Color hudAccent(int var0) {
      HudThemeManager var1;
      return (var1 = HudThemeManager.getInstance()) != null ? var1.getColor(System.currentTimeMillis(), (long)var0 * 40L) : new Color(90, 140, 255);
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (!this.isVisible()) {
         return false;
      } else if (var3 == 0 && this.isHovered(var1, var2)) {
         GuiInput.prompt(this.property.getName().replace("-", " "), (String)this.property.getValue(), this.property::setValue, MaterialClickGui.getInstance());
         return true;
      } else {
         return false;
      }
   }

   public TextSetting(TextProperty var1, int var2, int var3, int var4, int var5) {
      super(var2, var3, var4, 28);
      this.property = var1;
   }

   public TextProperty getProperty() {
      return this.property;
   }
}
