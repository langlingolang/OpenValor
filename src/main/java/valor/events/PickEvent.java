package valor.events;

import valor.event.events.Event;

public class PickEvent implements Event {
   private double range;

   public void setRange(double range) {
      this.range = range;
   }

   public double getRange() {
      return this.range;
   }

   public PickEvent(double range) {
      this.range = range;
   }
}
