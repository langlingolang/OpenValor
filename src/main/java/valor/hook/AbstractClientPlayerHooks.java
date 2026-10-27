package valor.hook;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import valor.Valor;
import valor.module.movement.Sprint;
import valor.util.ReflectionUtil;

public class AbstractClientPlayerHooks {
   public static double getFovModifier(IAttributeInstance attribute, Object entity) {
      double fovModifier = attribute.getAttributeValue();
      if (entity instanceof EntityPlayerSP && Valor.moduleManager != null) {
         Sprint sprint = (Sprint)Valor.moduleManager.modules.get(Sprint.class);
         if (sprint.isEnabled() && (Boolean)sprint.fovFixProp.getValue()) {
            AttributeModifier modifier = ReflectionUtil.getSprintingSpeedBoostModifier(Sprint.mc.thePlayer);
            if (attribute.getModifier(modifier.getID()) == null && sprint.wasSprinting) {
               return fovModifier * 1.300000011920929;
            }
         }

         return fovModifier;
      } else {
         return fovModifier;
      }
   }
}
