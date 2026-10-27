package valor.hook;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import valor.Valor;
import valor.event.EventManager;
import valor.events.KnockbackEvent;
import valor.events.SafeWalkEvent;

public class EntityHooks {
   public static boolean onMoveEntitySafeWalk(Entity entity, boolean isSafeWalk) {
      if (entity instanceof EntityPlayerSP) {
         SafeWalkEvent event;
         EventManager.call(event = new SafeWalkEvent(isSafeWalk));
         return event.isSafeWalk();
      } else {
         return isSafeWalk;
      }
   }

   public static boolean onSetAngles(Entity entity) {
      return entity instanceof EntityPlayerSP && Valor.rotationManager != null && Valor.rotationManager.silent;
   }

   public static boolean onSetVelocity(Entity entity, double motionX, double motionY, double motionZ) {
      if (entity instanceof EntityPlayerSP) {
         KnockbackEvent event;
         EventManager.call(event = new KnockbackEvent(motionX, motionY, motionZ));
         if (event.isCancelled()) {
            entity.motionX = event.getX();
            entity.motionY = event.getY();
            entity.motionZ = event.getZ();
            return true;
         }
      }

      return false;
   }
}
