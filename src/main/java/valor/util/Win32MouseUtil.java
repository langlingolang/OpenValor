package valor.util;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public class Win32MouseUtil {
   private interface User32 extends StdCallLibrary {
      User32 INSTANCE = (User32)Native.loadLibrary("user32", User32.class);
      short GetAsyncKeyState(int vKey);
   }

   public static boolean isLeftMouseButtonDown() {
      try {
         return (User32.INSTANCE.GetAsyncKeyState(0x01) & 0x8000) != 0;
      } catch (Throwable t) {
         return false;
      }
   }
}
