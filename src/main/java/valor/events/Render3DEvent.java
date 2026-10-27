package valor.events;

import valor.event.events.Event;

public class Render3DEvent implements Event {
   private final float partialTicks;

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public Render3DEvent(float partialTicks) {
      this.partialTicks = partialTicks;
   }
}
