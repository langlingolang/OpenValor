package valor.events;

import net.minecraft.entity.EntityLivingBase;
import valor.event.events.Event;
import valor.event.types.EventType;

public class RenderLivingEvent implements Event {
   private final EventType type;
   private final EntityLivingBase entity;

   public EntityLivingBase getEntity() {
      return this.entity;
   }

   public RenderLivingEvent(EventType type, EntityLivingBase entity) {
      this.type = type;
      this.entity = entity;
   }

   public EventType getType() {
      return this.type;
   }
}
