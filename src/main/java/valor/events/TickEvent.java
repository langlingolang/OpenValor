package valor.events;

import valor.event.events.Event;
import valor.event.types.EventType;

public class TickEvent implements Event {
   private final EventType type;

   public TickEvent(EventType type) {
      this.type = type;
   }

   public EventType getType() {
      return this.type;
   }
}
