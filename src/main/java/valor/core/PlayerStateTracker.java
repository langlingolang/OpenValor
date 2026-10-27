package valor.core;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;

public class PlayerStateTracker {
   public boolean hasUsedEntity = false;
   public boolean hasDug = false;
   public boolean hasPlacedBlock = false;
   public boolean hasHeldItemChanged = false;
   public boolean hasAnimated = false;

   public final void trackPacket(Packet<?> packet) {
      if (packet instanceof C02PacketUseEntity) {
         this.hasUsedEntity = true;
      }

      if (packet instanceof C07PacketPlayerDigging) {
         this.hasDug = true;
      }

      if (packet instanceof C08PacketPlayerBlockPlacement) {
         this.hasPlacedBlock = true;
      }

      if (packet instanceof C09PacketHeldItemChange) {
         this.hasHeldItemChanged = true;
      }

      if (packet instanceof C0APacketAnimation) {
         this.hasAnimated = true;
      }

      if (packet instanceof C03PacketPlayer) {
         this.hasUsedEntity = false;
         this.hasDug = false;
         this.hasPlacedBlock = false;
         this.hasHeldItemChanged = false;
         this.hasAnimated = false;
      }
   }
}
