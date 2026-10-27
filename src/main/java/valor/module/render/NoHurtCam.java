package valor.module.render;

import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.PercentageProperty;

public class NoHurtCam extends Module {
   public final PercentageProperty multiplierProp = new PercentageProperty("multiplier", 0);

   public NoHurtCam() {
      super("NoHurtCam", false, false, ModuleCategory.VISUAL);
   }
}
