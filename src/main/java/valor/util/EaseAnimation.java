package valor.util;

public final class EaseAnimation {
   private float startValue;
   private float targetValue;
   public float currentValue;
   private long startTime;
   private float durationMs;
   private Easing easing;
   public boolean finished = true;

   public final void reset() {
      this.currentValue = 0.0F;
      this.startValue = 0.0F;
      this.targetValue = 0.0F;
      this.finished = true;
   }

   private float applyEasing(float t) {
      switch (this.easing) {
         case LINEAR:
            return t;
         case OUT_QUAD:
            return 1.0F - (1.0F - t) * (1.0F - t);
         case OUT_CUBIC:
            return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
         case OUT_QUART:
            return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t) * (1.0F - t);
         case OUT_QUINT:
            return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t) * (1.0F - t) * (1.0F - t);
         case OUT_EXPO:
            if (t >= 1.0F) {
               return 1.0F;
            }
            return 1.0F - (float)Math.pow(2.0, -10.0 * (double)t);
         case IN_QUAD:
            return t * t;
         case IN_CUBIC:
            return t * t * t;
         case IN_OUT_CUBIC:
            if (t < 0.5F) {
               return 4.0F * t * t * t;
            }
            return 1.0F - (float)Math.pow((double)(-2.0F * t + 2.0F), 3.0) / 2.0F;
         case IN_OUT_QUART:
            if (t < 0.5F) {
               return 8.0F * t * t * t * t;
            }
            return 1.0F - (float)Math.pow((double)(-2.0F * t + 2.0F), 4.0) / 2.0F;
         case OUT_ELASTIC:
            if (t <= 0.0F) {
               return 0.0F;
            } else {
               if (t >= 1.0F) {
                  return 1.0F;
               }
               return (float)(Math.pow(2.0, -10.0 * (double)t) * Math.sin(((double)t * 10.0 - 0.75) * (float) (Math.PI * 2.0 / 3.0)) + 1.0);
            }
         default:
            return t;
      }
   }

   public final void start(float target, float duration, Easing easing) {
      if (!(Math.abs(target - this.targetValue) < 1.0E-4F)) {
         this.startValue = this.currentValue;
         this.targetValue = target;
         this.durationMs = duration;
         this.easing = easing;
         this.startTime = System.currentTimeMillis();
         this.finished = false;
      }
   }

   public EaseAnimation() {
      this.currentValue = 0.0F;
      this.startValue = 0.0F;
      this.targetValue = 0.0F;
   }

   public final float update() {
      if (this.finished) {
         return this.currentValue;
      } else {
         float progress = Math.min((float)(System.currentTimeMillis() - this.startTime) / this.durationMs, 1.0F);
         this.currentValue = this.startValue + (this.targetValue - this.startValue) * this.applyEasing(progress);
         if (progress >= 1.0F) {
            this.currentValue = this.targetValue;
            this.finished = true;
         }
         return this.currentValue;
      }
   }

   public float getValue() {
      return this.currentValue;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public static enum Easing {
      LINEAR,
      OUT_QUAD,
      OUT_CUBIC,
      OUT_QUART,
      OUT_QUINT,
      OUT_EXPO,
      IN_QUAD,
      IN_CUBIC,
      IN_OUT_CUBIC,
      IN_OUT_QUART,
      OUT_ELASTIC;
   }
}
