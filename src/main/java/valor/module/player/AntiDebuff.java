package valor.module.player;

import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;

public class AntiDebuff extends Module {
   public final BooleanProperty blindnessProp = new BooleanProperty("blindness", Boolean.TRUE);
   public final BooleanProperty nauseaProp = new BooleanProperty("nausea", Boolean.TRUE);

public AntiDebuff() {
      super("AntiDebuff", false, ModuleCategory.PLAYER);
   }
}
