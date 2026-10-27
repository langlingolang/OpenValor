package valor.util;

public final class FloatInterpolator {
   private float startValue;
   private float targetValue;
   private long startTimeMs;
   private long durationMs;

   public final void reset(float value) {
      this.startValue = value;
      this.targetValue = value;
      this.durationMs = 0L;
   }

   public final float getValue(long currentTimeMs) {
      if (this.durationMs <= 0L) {
         return this.targetValue;
      } else {
         float t = Math.max(0.0F, Math.min(1.0F, (float)(currentTimeMs - this.startTimeMs) / (float)this.durationMs));
         float smooth = t * t * (3.0F - t * 2.0F);
         return this.startValue + (this.targetValue - this.startValue) * smooth;
      }
   }

   public final boolean isFinished(long currentTimeMs) {
      return this.durationMs <= 0L || currentTimeMs - this.startTimeMs >= this.durationMs;
   }

   public final void interpolateTo(float target, long durationMs, long currentTimeMs) {
      if (Float.compare(this.targetValue, target) != 0) {
         this.startValue = this.getValue(currentTimeMs);
         this.targetValue = target;
         this.startTimeMs = currentTimeMs;
         this.durationMs = Math.max(0L, durationMs);
      }
   }

   public FloatInterpolator(float initialValue) {
      this.startValue = initialValue;
      this.targetValue = initialValue;
   }
}
