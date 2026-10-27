package valor.hook;

import valor.Valor;
import valor.module.render.ESP;

public class ItemStackHooks {
   public static Boolean onHasEffect() {
      return Valor.moduleManager != null && ((ESP)Valor.moduleManager.modules.get(ESP.class)).isEnabled() ? Boolean.FALSE : null;
   }
}
