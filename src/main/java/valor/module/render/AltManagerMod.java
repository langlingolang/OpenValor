package valor.module.render;

import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;

public class AltManagerMod extends Module {
   public final BooleanProperty confirmSwitchProp = new BooleanProperty("confirm-switch", Boolean.TRUE);
   public final BooleanProperty onDemandRefreshProp = new BooleanProperty("on-demand-refresh", Boolean.TRUE);

   @Override
   public String getDescription() {
      return "Settings for the Alt Manager (accounts list + Microsoft login).";
   }

   public AltManagerMod() {
      super("AltManager", false, true, ModuleCategory.VISUAL);
   }
}
