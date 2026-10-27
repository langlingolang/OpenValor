package valor.event.events;

public abstract class EventStoppable implements Event {
   private boolean stopped;

   public boolean isStopped() {
      return this.stopped;
   }

   protected EventStoppable() {
   }

   public void stop() {
      this.stopped = true;
   }
}
