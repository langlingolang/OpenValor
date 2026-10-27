package valor.module.render;

import valor.core.HudThemeManager;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;

public class Theme extends Module {
   public final ModeProperty hudStyleProp;
   public final ModeProperty colorProp;
   public final FloatProperty colorSpeedProp;
   public final FloatProperty colorDistProp;
   public final ColorProperty customColor1Prop;
   public final ColorProperty customColor2Prop;
   public final ColorProperty staticColorProp;
   public final FloatProperty scaleProp;
   public final BooleanProperty shadowProp;
   public final ModeProperty guiSizeProp;
   public final ModeProperty hudSizeProp;
   public final ModeProperty fontProp;
   public final BooleanProperty chatOutlineProp;
   public final BooleanProperty blinkTimerProp;
   public final BooleanProperty toggleAlertsProp;

   public Theme() {
      super("Theme", true, false, ModuleCategory.VISUAL);
      HudThemeManager tm = HudThemeManager.getInstance();
      this.hudStyleProp = tm.hudStyleProp;
      this.colorProp = tm.colorProp;
      this.colorSpeedProp = tm.colorSpeedProp;
      this.colorDistProp = tm.colorDistProp;
      this.customColor1Prop = tm.customColor1Prop;
      this.customColor2Prop = tm.customColor2Prop;
      this.staticColorProp = tm.staticColorProp;
      this.scaleProp = tm.scaleProp;
      this.shadowProp = tm.shadowProp;
      this.guiSizeProp = tm.guiSizeProp;
      this.hudSizeProp = tm.hudSizeProp;
      this.fontProp = tm.fontProp;
      this.chatOutlineProp = tm.chatOutlineProp;
      this.blinkTimerProp = tm.blinkTimerProp;
      this.toggleAlertsProp = tm.toggleAlertsProp;
   }
}

