package valor.module.movement;

import valor.Valor;
import valor.core.BlinkType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LoadWorldEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;

public class Blink extends Module {
   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"DEFAULT", "PULSE"});
   public final IntProperty ticksProp = new IntProperty("ticks", 20, 0, 1200);

   public Blink() {
      super("Blink", false, ModuleCategory.MOVEMENT);
   }

   @EventTarget(4)
   public void onTick(TickEvent tickEvent) {
      if (this.isEnabled() && tickEvent.getType() == EventType.POST) {
         if (Valor.blinkManager == null || Valor.blinkManager.currentType != BlinkType.MANUAL) {
            this.setEnabled(false);
            return;
         }
         if ((Integer)this.ticksProp.getValue() > 0 && Valor.blinkManager.getPlayerPacketCount() > (long)((Integer)this.ticksProp.getValue()).intValue()) {
            switch ((Integer)this.modeProp.getValue()) {
               case 0: {
                  this.setEnabled(false);
                  return;
               }
               case 1: {
                  Valor.blinkManager.setBlinking(false, BlinkType.MANUAL);
                  Valor.blinkManager.setBlinking(true, BlinkType.MANUAL);
               }
            }
         }
      }
   }

   @EventTarget
   public void onWorldLoad(LoadWorldEvent loadWorldEvent) {
      this.setEnabled(false);
   }

   @Override
   public void onEnabled() {
      if (Valor.blinkManager != null) {
         Valor.blinkManager.setBlinking(false, Valor.blinkManager.currentType);
         Valor.blinkManager.setBlinking(true, BlinkType.MANUAL);
      }
   }

   @Override
   public void onDisabled() {
      if (Valor.blinkManager != null) {
         Valor.blinkManager.setBlinking(false, BlinkType.MANUAL);
      }
   }
}
