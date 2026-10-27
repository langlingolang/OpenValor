package valor.module.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import valor.event.EventTarget;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.util.KeyUtil;

public class Sprint extends Module {
   public static final Minecraft mc = Minecraft.getMinecraft();
   public boolean wasSprinting = false;
   public final BooleanProperty fovFixProp = new BooleanProperty("fov-fix", Boolean.TRUE);

   @Override
   public void onDisabled() {
      this.wasSprinting = false;
      KeyUtil.updateKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode());
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled()) {
         switch (var1.getType()) {
            case PRE:
               KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
               return;
            case POST:
               this.wasSprinting = mc.thePlayer.isSprinting();
         }
      }
   }

   public Sprint() {
      super("Sprint", false, false, ModuleCategory.MOVEMENT);
   }
}
