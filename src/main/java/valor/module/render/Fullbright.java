package valor.module.render;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.ModeProperty;

public class Fullbright extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public float previousGamma = Float.NaN;
   public boolean addedNightVision = false;

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"GAMMA", "EFFECT"});

   @Override
   public void verifyValue(String value) {
      if (this.isEnabled()) {
         this.onDisabled();
         this.onEnabled();
      }
   }

   @Override
   public void onDisabled() {
      if (!Float.isNaN(this.previousGamma)) {
         mc.gameSettings.gammaSetting = this.previousGamma;
         this.previousGamma = Float.NaN;
      }

      if (this.addedNightVision) {
         if (mc.thePlayer != null) {
            mc.thePlayer.removePotionEffectClient(Potion.nightVision.id);
         }
         this.addedNightVision = false;
      }
   }

   @Override
   public void onEnabled() {
      switch ((Integer) this.modeProp.getValue()) {
         case 0:
            this.previousGamma = mc.gameSettings.gammaSetting;
            return;
         case 1:
            this.addedNightVision = true;
      }
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (this.isEnabled() && event.getType() == EventType.POST) {
         switch ((Integer) this.modeProp.getValue()) {
            case 0:
               mc.gameSettings.gammaSetting = 1000.0F;
               return;
            case 1:
               if (mc.thePlayer != null) {
                  mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 25940, 0));
               }
         }
      }
   }

   public Fullbright() {
      super("Fullbright", true, false, ModuleCategory.VISUAL);
   }
}
