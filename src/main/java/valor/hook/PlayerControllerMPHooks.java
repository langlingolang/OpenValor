package valor.hook;

import net.minecraft.entity.Entity;
import valor.event.EventManager;
import valor.events.AttackEvent;
import valor.events.CancelUseEvent;
import valor.events.WindowClickEvent;

public class PlayerControllerMPHooks {
   public static boolean onWindowClick(int windowId, int slotId, int mouseButtonClicked, int mode) {
      WindowClickEvent event;
      EventManager.call(event = new WindowClickEvent(windowId, slotId, mouseButtonClicked, mode));
      return event.isCancelled();
   }

   public static void onAttackEntity(Entity target) {
      EventManager.call(new AttackEvent(target));
   }

   public static boolean onStoppedUsingItem() {
      CancelUseEvent event;
      EventManager.call(event = new CancelUseEvent());
      return event.isCancelled();
   }
}
