package valor.hook;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import valor.Valor;
import valor.core.LagManager;
import valor.core.LagType;
import valor.event.EventManager;
import valor.event.types.EventType;
import valor.events.PacketEvent;

public class NetworkManagerHooks {
   public static boolean onChannelRead(ChannelHandlerContext context, Packet<?> packet) {
      if (packet.getClass().getName().startsWith("net.minecraft.network.play.client")) {
         return false;
      } else {
         if (Valor.lagManager != null) {
            LagManager lagManager = Valor.lagManager;
            boolean intercept;
            if (Valor.lagManager.currentType == LagType.NONE) {
               intercept = false;
            } else if (packet instanceof S00PacketKeepAlive) {
               intercept = false;
            } else if (!(packet instanceof S01PacketJoinGame) && !(packet instanceof S07PacketRespawn)) {
               S19PacketEntityStatus statusPacket;
               Entity entity;
               if (!(packet instanceof S19PacketEntityStatus)
                  || (entity = (statusPacket = (S19PacketEntityStatus)packet).getEntity(LagManager.mc.theWorld)) == null
                  || entity.equals(LagManager.mc.thePlayer) && statusPacket.getOpCode() == 2) {
                  lagManager.packetQueue.offer((Packet<net.minecraft.network.play.INetHandlerPlayClient>)packet);
                  intercept = true;
               } else {
                  intercept = false;
               }
            } else {
               lagManager.setLagging(false, lagManager.currentType);
               intercept = false;
            }

            if (intercept) {
               return true;
            }
         }

         PacketEvent event;
         EventManager.call(event = new PacketEvent(EventType.RECEIVE, packet));
         return event.isCancelled();
      }
   }

   public static boolean onSendPacket(Packet<?> packet) {
      if (!packet.getClass().getName().startsWith("net.minecraft.network.play.server")) {
         PacketEvent event;
         EventManager.call(event = new PacketEvent(EventType.SEND, packet));
         if (event.isCancelled()) {
            return true;
         }

         if (Valor.stateTracker != null && Valor.blinkManager != null && Valor.packetBlinkManager != null && !Valor.packetBlinkManager.isFlushing) {
            Valor.stateTracker.trackPacket(packet);
            if (Valor.blinkManager.isBlinking && Valor.blinkManager.interceptPacket(packet)) {
               return true;
            }

            if (Valor.packetBlinkManager.queuePacket(packet)) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean onSendPacketWithListener(Packet<?> packet) {
      if (!packet.getClass().getName().startsWith("net.minecraft.network.play.server")
         && Valor.stateTracker != null
         && Valor.blinkManager != null
         && Valor.packetBlinkManager != null
         && !Valor.packetBlinkManager.isFlushing) {
         Valor.stateTracker.trackPacket(packet);
         if (Valor.blinkManager.isBlinking && Valor.blinkManager.interceptPacket(packet)) {
            return true;
         }

         if (Valor.packetBlinkManager.queuePacket(packet)) {
            return true;
         }
      }

      return false;
   }
}
