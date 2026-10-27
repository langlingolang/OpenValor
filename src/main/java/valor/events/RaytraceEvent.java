package valor.events;

import valor.event.events.Event;

public class RaytraceEvent implements Event {
   private double range;

   public void setRange(double range) {
      this.range = range;
   }

   public double getRange() {
      return this.range;
   }

   public RaytraceEvent(double range) {
      this.range = range;
   }
}
