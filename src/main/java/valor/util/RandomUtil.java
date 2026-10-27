package valor.util;

import java.util.Random;

public final class RandomUtil {
   public static final Random RANDOM = new Random();

   public static double nextDouble(double min, double max) {
      return RANDOM.nextDouble() * (max - min) + min;
   }

   public static long nextLong(long min, long max) {
      return (long) nextDouble((double) min, (double) (max + 1L));
   }

   public static float nextFloat(float min, float max) {
      return RANDOM.nextFloat() * (max - min) + min;
   }
}
