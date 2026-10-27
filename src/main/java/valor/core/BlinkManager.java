package valor.core;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.TickEvent;
import valor.util.PacketUtil;

public class BlinkManager {
   public static Minecraft mc = Minecraft.getMinecraft();

   public BlinkType currentType = BlinkType.NONE;
   public boolean isBlinking = false;
   public Deque<Packet<?>> packetQueue = new ConcurrentLinkedDeque<>();

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof C00Handshake
         || event.getPacket() instanceof C00PacketLoginStart
         || event.getPacket() instanceof C00PacketServerQuery
         || event.getPacket() instanceof C01PacketPing
         || event.getPacket() instanceof C01PacketEncryptionResponse) {
         this.setBlinking(false, this.currentType);
      }
   }

   public final long getPlayerPacketCount() {
      return this.packetQueue.stream().filter(p -> p instanceof C03PacketPlayer).count();
   }

   public final boolean setBlinking(boolean enable, BlinkType type) {
      if (type == BlinkType.NONE) {
         return false;
      } else {
         if (enable) {
            if (this.currentType == BlinkType.DISABLER && type == BlinkType.SCAFFOLD) {
               return true;
            }

            this.currentType = type;
            this.isBlinking = true;
         } else {
            if (this.currentType != type && this.currentType == BlinkType.DISABLER) {
               return true;
            }

            if (this.currentType != type) {
               return false;
            }

            this.isBlinking = false;
            if (Minecraft.getMinecraft().getNetHandler() != null && this.packetQueue.isEmpty()) {
               return true;
            }

            for (Packet<?> packet : this.packetQueue) {
               PacketUtil.sendPacketNoEvent(packet);
            }

            this.packetQueue.clear();
            this.currentType = BlinkType.NONE;
         }

         return true;
      }
   }

   public final boolean interceptPacket(Packet<?> packet) {
      if (this.currentType == BlinkType.NONE || packet instanceof C00PacketKeepAlive || packet instanceof C01PacketChatMessage) {
         return false;
      } else if (this.packetQueue.isEmpty() && packet instanceof C0FPacketConfirmTransaction) {
         return false;
      } else {
         this.packetQueue.offer(packet);
         return true;
      }
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (event.getType() == EventType.POST && mc.thePlayer != null && mc.thePlayer.isDead) {
         this.setBlinking(false, this.currentType);
      }
   }
}
