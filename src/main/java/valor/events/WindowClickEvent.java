package valor.events;

import valor.event.events.callables.EventCancellable;

public class WindowClickEvent extends EventCancellable {
   private final int windowId;
   private final int slot;
   private final int mouseButton;
   private final int mode;

   public WindowClickEvent(int windowId, int slot, int mouseButton, int mode) {
      this.windowId = windowId;
      this.slot = slot;
      this.mouseButton = mouseButton;
      this.mode = mode;
   }
}
