package valor.core;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.Vec3;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.util.PacketUtil;

public class PacketBlinkManager {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public boolean isFlushing;
   public int delayTicks;
   public Vec3 startPos;
   private final Queue<PacketEntry> queue = new ConcurrentLinkedQueue<>();

   public boolean queuePacket(Packet<?> packet) {
      if (this.isFlushing || packet instanceof C00PacketKeepAlive || packet instanceof C01PacketChatMessage) {
         return false;
      }

      if (this.delayTicks > 0) {
         this.queue.add(new PacketEntry(packet));
         return true;
      }

      return false;
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (event.getType() == EventType.PRE) {
         if (mc.thePlayer != null) {
            if (this.queue.isEmpty()) {
               this.startPos = mc.thePlayer.getPositionVector();
            }
         }

         if (this.delayTicks <= 0 && !this.queue.isEmpty()) {
            this.flush();
         }
      }
   }

   public void flush() {
      if (this.isFlushing) {
         return;
      }

      this.isFlushing = true;
      try {
         while (!this.queue.isEmpty()) {
            PacketEntry entry = this.queue.poll();
            if (entry != null && entry.packet != null) {
               PacketUtil.sendPacketNoEvent(entry.packet);
            }
         }
      } finally {
         this.isFlushing = false;
      }
   }

   public static class PacketEntry {
      public final Packet<?> packet;
      public int delay;

      public PacketEntry(Packet<?> packet) {
         this.packet = packet;
         this.delay = 0;
      }
   }
}
