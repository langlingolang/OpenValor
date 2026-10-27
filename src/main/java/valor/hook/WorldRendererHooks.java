package valor.hook;

import java.nio.IntBuffer;

public class WorldRendererHooks {
   public static IntBuffer onPutColorMultiplier(IntBuffer buffer, int index, int color) {
      return buffer.put(index, color);
   }
}
