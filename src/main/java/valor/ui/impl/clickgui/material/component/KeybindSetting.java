package valor.ui.impl.clickgui.material.component;

import valor.module.Module;
import valor.module.render.ClickGuiMod;
import valor.ui.impl.clickgui.material.Component;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.KeyUtil;
import valor.util.render.GreenRender;

public class KeybindSetting extends Component {
   private final Module module;
   private boolean binding;

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      String var5 = "Bind";
      String var7;
      if (this.binding) {
         var7 = "Press key";
      } else if (this.module.getKey() != 0 && this.module.getKey() != 0) {
         var7 = KeyUtil.getKeyName(this.module.getKey());
      } else {
         var7 = "None";
      }

      var3 = (float)super.y + ((float)super.height - GreenRender.fontHSmall()) / 2.0F + 0.5F;
      GreenRender.drawStringSmall(var5, (float)(super.x + 4), var3, MaterialTheme.TEXT_MUTED);
      float var6 = GreenRender.strWSmall(var7);
      GreenRender.drawStringSmall(var7, (float)(super.x + super.width) - var6 - 4.0F, var3, this.binding ? MaterialTheme.TEXT : MaterialTheme.TEXT_DISABLED);
   }

   @Override
   public boolean isBinding() {
      return this.binding;
   }

   @Override
   public void keyTyped(char var1, int var2) {
      if (this.binding) {
         if (var2 != 1 && var2 != 211 && var2 != 14 && var2 != 11 && var2 != 11) {
            this.module.setKey(var2);
         } else if (this.module instanceof ClickGuiMod) {
            this.module.setKey(210);
         } else {
            this.module.setKey(0);
         }

         this.binding = false;
      }
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (var3 == 0 && this.isHovered(var1, var2)) {
         this.binding = !this.binding;
         return true;
      } else {
         if (this.binding && !this.isHovered(var1, var2)) {
            this.binding = false;
         }

         return false;
      }
   }

   public KeybindSetting(Module var1, int var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.module = var1;
   }
}
