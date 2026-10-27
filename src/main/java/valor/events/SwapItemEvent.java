package valor.events;

import valor.event.events.callables.EventCancellable;

public class SwapItemEvent extends EventCancellable {
   private final int slot;
   private final int direction;

   public int setSlot(int currentSlot) {
      return this.slot >= 0 && this.slot <= 8 ? this.slot : Math.floorMod(currentSlot - this.direction, 9);
   }

   public SwapItemEvent(int slot, int direction) {
      this.slot = slot;
      this.direction = Math.min(Math.max(direction, -1), 1);
   }
}
