package valor.events;

import valor.event.events.Event;

public class Render2DEvent implements Event {
   private final float partialTicks;

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public Render2DEvent(float partialTicks) {
      this.partialTicks = partialTicks;
   }
}
