package valor.core;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.TickEvent;

public class LagManager {
   public static Minecraft mc = Minecraft.getMinecraft();

   public LagType currentType = LagType.NONE;
   public long lagTicks = 0L;
   public Deque<Packet<INetHandlerPlayClient>> packetQueue = new ConcurrentLinkedDeque<>();

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof C00Handshake
         || event.getPacket() instanceof C00PacketLoginStart
         || event.getPacket() instanceof C00PacketServerQuery
         || event.getPacket() instanceof C01PacketPing
         || event.getPacket() instanceof C01PacketEncryptionResponse) {
         this.setLagging(false, this.currentType);
      }
   }

   public final boolean setLagging(boolean enable, LagType type) {
      if (enable) {
         this.lagTicks = 0L;
         this.currentType = type;
      } else {
         this.currentType = LagType.NONE;
         if (Minecraft.getMinecraft().getNetHandler() != null && this.packetQueue.isEmpty()) {
            return true;
         }

         Packet<INetHandlerPlayClient> packet;
         while ((packet = this.packetQueue.poll()) != null) {
            packet.processPacket(Minecraft.getMinecraft().getNetHandler());
         }

         this.packetQueue.clear();
      }

      return this.currentType != LagType.NONE;
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (event.getType() == EventType.POST) {
         if (mc.thePlayer != null && mc.thePlayer.isDead) {
            this.setLagging(false, this.currentType);
         }

         if (this.currentType != LagType.NONE) {
            this.lagTicks++;
         }
      }
   }
}
