package valor.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import valor.event.EventManager;
import valor.events.SwapItemEvent;

public class KeyBindingHooks {
   public static int onIsPressed(int pressTime, KeyBinding keyBinding) {
      if (pressTime != 0) {
         Minecraft mc = Minecraft.getMinecraft();

         for (int slot = 0; slot < 9; slot++) {
            if (mc.gameSettings.keyBindsHotbar[slot].getKeyDescription().equals(keyBinding.getKeyDescription())) {
               SwapItemEvent event;
               EventManager.call(event = new SwapItemEvent(slot, 0));
               if (event.isCancelled()) {
                  return 0;
               }
            }
         }
      }

      return pressTime;
   }
}
