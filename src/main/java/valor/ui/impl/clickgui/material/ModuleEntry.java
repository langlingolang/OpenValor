package valor.ui.impl.clickgui.material;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import valor.Valor;
import valor.module.Module;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.setting.Property;
import valor.setting.TextProperty;
import valor.ui.dataset.impl.FloatSlider;
import valor.ui.dataset.impl.IntSlider;
import valor.ui.dataset.impl.PercentageSlider;
import valor.ui.impl.clickgui.material.component.ColorSetting;
import valor.ui.impl.clickgui.material.component.KeybindSetting;
import valor.ui.impl.clickgui.material.component.ModeSetting;
import valor.ui.impl.clickgui.material.component.SliderSetting;
import valor.ui.impl.clickgui.material.component.SwitchSetting;
import valor.ui.impl.clickgui.material.component.TextSetting;
import valor.util.FloatInterpolator;
import valor.util.render.GreenRender;

public class ModuleEntry extends Component {
   private final Module module;
   private final List<Component> settings = new ArrayList<>();
   private boolean expanded;
   private int rowIndex;
   private boolean roundedBottom;
   private final FloatInterpolator expansion = new FloatInterpolator(0.0F);
   private final FloatInterpolator enableFill;
   private final FloatInterpolator hoverFill = new FloatInterpolator(0.0F);

   @Override
   public void render(int var1, int var2, float var3, int var4) {
      boolean var5 = this.module.isEnabled();
      long var6 = System.currentTimeMillis();
      this.enableFill.interpolateTo(var5 ? 1.0F : 0.0F, 150L, var6);
      float var12 = this.enableFill.getValue(var6);
      boolean var8 = var1 >= super.x && var1 <= super.x + super.width && var2 >= super.y && var2 <= super.y + MaterialTheme.MODULE_HEIGHT;
      this.hoverFill.interpolateTo(var8 ? 1.0F : 0.0F, 120L, var6);
      float var17 = this.hoverFill.getValue(var6);
      int var7 = this.getHeight() - MaterialTheme.MODULE_HEIGHT;
      float var20 = this.roundedBottom && var7 == 0 ? 7.0F : 0.0F;
      Color var9 = GreenRender.lerp(MaterialTheme.TEXT_DISABLED, MaterialTheme.TEXT, var12);
      if (var12 > 0.01F) {
         Color var13 = MaterialTheme.accent(MaterialTheme.enableAccent(this.module.getName()), (0.24F + 0.06F * var17) * var12);
         GreenRender.fillRRCorners((float)super.x, (float)super.y, (float)super.width, (float)MaterialTheme.MODULE_HEIGHT, 0.0F, 0.0F, var20, var20, var13);
      } else if (var17 > 0.01F) {
         Color var14 = MaterialTheme.ROW_HOVER;
         GreenRender.fillRRCorners(
            (float)super.x,
            (float)super.y,
            (float)super.width,
            (float)MaterialTheme.MODULE_HEIGHT,
            0.0F,
            0.0F,
            var20,
            var20,
            MaterialTheme.accent(var14, (float)var14.getAlpha() / 255.0F * var17)
         );
      }

      float var15 = (float)super.y + ((float)MaterialTheme.MODULE_HEIGHT - GreenRender.fontH()) / 2.0F + 0.5F;
      GreenRender.drawString(this.module.getName(), (float)(super.x + 7), var15, var9);
      if (var7 > 0) {
         if (var7 > 1) {
            GreenRender.fillRR(
               (float)(super.x + 2), (float)(super.y + MaterialTheme.MODULE_HEIGHT), (float)(super.width - 4), (float)var7, 6.0F, MaterialTheme.SETTINGS_BG
            );
         }

         GreenRender.pushScissor((float)super.x, (float)(super.y + MaterialTheme.MODULE_HEIGHT), (float)super.width, (float)var7);

         try {
            int var5y = super.y + MaterialTheme.MODULE_HEIGHT;
            Iterator var18 = this.settings.iterator();

            while (var18.hasNext()) {
               Component var19;
               if ((var19 = (Component)var18.next()).isVisible()) {
                  var19.setX(super.x + 2);
                  var19.setY(var5y);
                  var19.setWidth(super.width - 4);
                  var19.render(var1, var2, var3, var4++);
                  var5y += var19.getHeight();
               }
            }
         } finally {
            GreenRender.popScissor();
         }
      }
   }

   @Override
   public int getHeight() {
      long var1 = System.currentTimeMillis();
      this.expansion.interpolateTo(this.expanded ? 1.0F : 0.0F, 180L, var1);
      return MaterialTheme.MODULE_HEIGHT + Math.round((float)this.settingsHeight() * this.expansion.getValue(var1));
   }

   public boolean hasSettings() {
      return !this.settings.isEmpty();
   }

   private void initSettings() {
      this.settings.add(new KeybindSetting(this.module, super.x, super.y, super.width, MaterialTheme.SETTING_HEIGHT));
      List var1;
      if ((var1 = Valor.propertyManager.propertiesByModule.get(this.module.getClass())) != null) {
         Iterator var3 = var1.iterator();

         while (var3.hasNext()) {
            Property var2;
            if ((var2 = (Property)var3.next()) instanceof BooleanProperty) {
               this.settings.add(new SwitchSetting((BooleanProperty)var2, super.x, super.y, super.width, MaterialTheme.SETTING_HEIGHT));
            } else if (var2 instanceof FloatProperty) {
               this.settings.add(new SliderSetting(new FloatSlider((FloatProperty)var2), super.x, super.y, super.width, MaterialTheme.SETTING_HEIGHT + 2));
            } else if (var2 instanceof IntProperty) {
               this.settings.add(new SliderSetting(new IntSlider((IntProperty)var2), super.x, super.y, super.width, MaterialTheme.SETTING_HEIGHT + 2));
            } else if (var2 instanceof PercentageProperty) {
               this.settings
                  .add(new SliderSetting(new PercentageSlider((PercentageProperty)var2), super.x, super.y, super.width, MaterialTheme.SETTING_HEIGHT + 2));
            } else if (var2 instanceof ModeProperty) {
               this.settings.add(new ModeSetting((ModeProperty)var2, super.x, super.y, super.width, MaterialTheme.SETTING_HEIGHT));
            } else if (var2 instanceof ColorProperty) {
               this.settings.add(new ColorSetting((ColorProperty)var2, super.x, super.y, super.width, 32));
            } else if (var2 instanceof TextProperty) {
               this.settings.add(new TextSetting((TextProperty)var2, super.x, super.y, super.width, MaterialTheme.SETTING_HEIGHT));
            }
         }
      }
   }

   private int settingsHeight() {
      int var1 = 0;
      Iterator var2 = this.settings.iterator();

      while (var2.hasNext()) {
         Component var3;
         if ((var3 = (Component)var2.next()).isVisible()) {
            var1 += var3.getHeight();
         }
      }

      return var1;
   }

   public boolean matchesSearch(String var1) {
      return var1 != null && !var1.isEmpty() ? this.module.getName().toLowerCase().contains(var1.toLowerCase()) : true;
   }

   public void setRowIndex(int var1) {
      this.rowIndex = var1;
   }

   public void setRoundedBottom(boolean var1) {
      this.roundedBottom = var1;
   }

   @Override
   public boolean isBinding() {
      if (!this.expanded) {
         return false;
      } else {
         Iterator var1 = this.settings.iterator();

         while (var1.hasNext()) {
            Component var2;
            if ((var2 = (Component)var1.next()).isVisible() && var2.isBinding()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void keyTyped(char var1, int var2) {
      if (this.expanded) {
         Iterator var3 = this.settings.iterator();

         while (var3.hasNext()) {
            Component var4;
            if ((var4 = (Component)var3.next()).isVisible()) {
               var4.keyTyped(var1, var2);
            }
         }
      }
   }

   @Override
   public void mouseReleased(int var1, int var2, int var3) {
      if (this.expanded) {
         Iterator var4 = this.settings.iterator();

         while (var4.hasNext()) {
            Component var5;
            if ((var5 = (Component)var4.next()).isVisible()) {
               var5.mouseReleased(var1, var2, var3);
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(int var1, int var2, int var3) {
      if (var1 >= super.x && var1 <= super.x + super.width && var2 >= super.y && var2 <= super.y + MaterialTheme.MODULE_HEIGHT) {
         if (var3 == 0) {
            this.module.toggle();
            return true;
         }

         if (var3 == 1 && this.hasSettings()) {
            this.expanded = !this.expanded;
            return true;
         }
      }

      if (this.expanded && var2 < super.y + this.getHeight()) {
         Iterator var4 = this.settings.iterator();

         while (var4.hasNext()) {
            Component var5;
            if ((var5 = (Component)var4.next()).isVisible() && var5.mouseClicked(var1, var2, var3)) {
               return true;
            }
         }
      }

      return false;
   }

   public ModuleEntry(Module var1, int var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.module = var1;
      this.enableFill = new FloatInterpolator(var1.isEnabled() ? 1.0F : 0.0F);
      this.initSettings();
   }

   public Module getModule() {
      return this.module;
   }
}
