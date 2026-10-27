package valor.module.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import valor.event.EventTarget;
import valor.events.Render2DEvent;
import valor.hook.EntityPlayerSPHooks;
import valor.hook.EntityRendererHooks;
import valor.module.Module;
import valor.module.ModuleCategory;

public class FreeLook extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public int prevThirdPersonView = 0;

   @Override
   public void onDisabled() {
      mc.gameSettings.thirdPersonView = this.prevThirdPersonView;
      EntityPlayerSPHooks.freeLookBypass = false;
      EntityRendererHooks.stopFreeLook();
   }

   @Override
   public void onEnabled() {
      if (mc.thePlayer == null) {
         this.setEnabled(false);
      } else {
         this.prevThirdPersonView = mc.gameSettings.thirdPersonView;
         EntityPlayerSPHooks.freeLookBypass = true;
         EntityRendererHooks.startFreeLook(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (this.isEnabled()) {
         if (mc.thePlayer != null) {
            if (this.getKey() != 0 && !Keyboard.isKeyDown(this.getKey())) {
               this.setEnabled(false);
            } else {
               mc.gameSettings.thirdPersonView = 1;
            }
         }
      }
   }

   public FreeLook() {
      super("FreeLook", false, false, ModuleCategory.VISUAL);
   }
}
