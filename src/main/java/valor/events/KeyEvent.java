package valor.events;

import valor.event.events.Event;

public class KeyEvent implements Event {
   private final int key;

   public KeyEvent(int key) {
      this.key = key;
   }

   public int getKey() {
      return this.key;
   }
}
