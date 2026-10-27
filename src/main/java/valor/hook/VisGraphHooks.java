package valor.hook;

import net.minecraft.client.renderer.chunk.SetVisibility;
import valor.Valor;
import valor.module.render.Chams;

public class VisGraphHooks {
   public static boolean shouldCancelFunc() {
      if (Valor.moduleManager == null) {
         return false;
      } else {
         Chams chams = (Chams)Valor.moduleManager.modules.get(Chams.class);
         return chams != null && chams.isEnabled();
      }
   }

   public static SetVisibility overrideComputeVisibility() {
      if (!shouldCancelFunc()) {
         return null;
      } else {
         SetVisibility visibility = new SetVisibility();
         visibility.setAllVisible(true);
         return visibility;
      }
   }
}
