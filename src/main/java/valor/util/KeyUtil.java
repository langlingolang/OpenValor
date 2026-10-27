package valor.util;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class KeyUtil {
   public static String getKeyName(int keyCode) {
      if (keyCode < 0) {
         int mouseIndex = keyCode + 100;
         switch (mouseIndex) {
            case 0:
               return "LMB";
            case 1:
               return "RMB";
            case 2:
               return "MMB";
            case 3:
               return "MOUSE3";
            case 4:
               return "MOUSE4";
            case 5:
               return "MOUSE5";
            case 6:
               return "MOUSE6";
            case 7:
               return "MOUSE7";
            default:
               String buttonName = Mouse.getButtonName(mouseIndex);
               return buttonName != null ? buttonName : "MOUSE" + mouseIndex;
         }
      } else {
         return Keyboard.getKeyName(keyCode);
      }
   }

   public static void updateKeyBindState(int keyCode) {
      KeyBinding.setKeyBindState(keyCode, keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
   }

   public static boolean isKeyDown(int keyCode) {
      return keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
   }
}
