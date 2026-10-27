package valor.events;

import net.minecraft.network.Packet;
import valor.event.events.callables.EventCancellable;
import valor.event.types.EventType;

public class PacketEvent extends EventCancellable {
   private final EventType type;
   private final Packet<?> packet;

   public Packet<?> getPacket() {
      return this.packet;
   }

   public PacketEvent(EventType var1, Packet<?> var2) {
      this.type = var1;
      this.packet = var2;
   }

   public EventType getType() {
      return this.type;
   }
}
