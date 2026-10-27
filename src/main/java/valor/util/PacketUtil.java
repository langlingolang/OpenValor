package valor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

public final class PacketUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static void sendPacket(Packet<?> packet) {
      if (mc.getNetHandler() != null && mc.getNetHandler().getNetworkManager() != null) {
         mc.getNetHandler().getNetworkManager().sendPacket(packet);
      }
   }

   public static void sendPacketNoEvent(Packet<?> packet) {
      if (mc.getNetHandler() != null && mc.getNetHandler().getNetworkManager() != null) {
         mc.getNetHandler().getNetworkManager().sendPacket(packet, null);
      }
   }
}
