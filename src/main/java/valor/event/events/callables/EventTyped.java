package valor.event.events.callables;

import valor.event.events.Event;
import valor.event.events.Typed;

public abstract class EventTyped implements Event, Typed {
   private final byte type;

   protected EventTyped(byte var1) {
      this.type = var1;
   }

   protected EventTyped() {
      this((byte)0);
   }

   @Override
   public byte getType() {
      return this.type;
   }
}
