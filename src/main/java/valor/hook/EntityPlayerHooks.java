package valor.hook;

import net.minecraft.entity.player.EntityPlayer;
import valor.Valor;
import valor.module.movement.KeepSprint;

public class EntityPlayerHooks {
   public static boolean onSetSprinting(EntityPlayer player, boolean sprinting) {
      if (Valor.moduleManager != null) {
         KeepSprint keepSprint = (KeepSprint)Valor.moduleManager.modules.get(KeepSprint.class);
         if (!sprinting && keepSprint.isEnabled() && keepSprint.shouldKeepSprint()) {
            return false;
         }
      }

      player.setSprinting(sprinting);
      return sprinting;
   }

   public static double onAttackSpeed(double speed) {
      if (Valor.moduleManager == null) {
         return speed;
      } else {
         KeepSprint keepSprint = (KeepSprint)Valor.moduleManager.modules.get(KeepSprint.class);
         return keepSprint.isEnabled() && keepSprint.shouldKeepSprint() ? keepSprint.getSprintSlowdown(speed) : speed;
      }
   }
}
