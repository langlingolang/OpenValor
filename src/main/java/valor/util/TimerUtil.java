package valor.util;

public final class TimerUtil {
   public long lastTimeMs = 0L;

   public void reset() {
      this.lastTimeMs = System.currentTimeMillis();
   }

   public final long getElapsedTimeMs() {long last = this.lastTimeMs;
      return System.currentTimeMillis() - last;
   }

   public final boolean hasTimeElapsed(long ms) {
      return this.getElapsedTimeMs() >= ms;
   }
}
