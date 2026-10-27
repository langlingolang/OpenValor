package valor.event.events.callables;

import valor.event.events.Cancellable;
import valor.event.events.Event;

public abstract class EventCancellable implements Cancellable, Event {
   private boolean cancelled;

   @Override
   public void setCancelled(boolean var1) {
      this.cancelled = var1;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }
}
