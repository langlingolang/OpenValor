package valor.hook;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import valor.Valor;
import valor.core.MovementManager;
import valor.event.EventManager;
import valor.events.StrafeEvent;
import valor.module.movement.Jesus;

public class EntityLivingBaseHooks {
   public static float onDepthStrider(EntityLivingBase entity, float modifier) {
      if (entity instanceof EntityPlayerSP && modifier == (float)EnchantmentHelper.getDepthStriderModifier(entity)) {
         if (Valor.moduleManager == null) {
            return modifier;
         }

         Jesus jesus = (Jesus)Valor.moduleManager.modules.get(Jesus.class);
         if (jesus.isEnabled() && (!(Boolean)jesus.groundOnlyProp.getValue() || entity.onGround)) {
            return Math.max(modifier, (Float)jesus.speedProp.getValue());
         }
      }

      return modifier;
   }

   public static float onJump(EntityLivingBase entity, float yaw) {
      return (Entity)entity instanceof EntityPlayerSP && MovementManager.isRotated() ? MovementManager.getMovementYaw() * (float) (Math.PI / 180.0) : yaw;
   }

   public static void onMoveFlying(EntityLivingBase entity, float strafe, float forward, float friction) {
      if (entity instanceof EntityPlayerSP) {
         StrafeEvent event;
         EventManager.call(event = new StrafeEvent(strafe, forward, friction));
         strafe = event.getStrafe();
         forward = event.getForward();
         friction = event.getFriction();
         boolean isRotated = MovementManager.isRotated();
         float originalYaw = entity.rotationYaw;
         if (isRotated) {
            entity.rotationYaw = MovementManager.getMovementYaw();
         }

         entity.moveFlying(strafe, forward, friction);
         if (isRotated) {
            entity.rotationYaw = originalYaw;
         }
      } else {
         entity.moveFlying(strafe, forward, friction);
      }
   }
}
