package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatColor;
import valor.util.ChatUtil;

public final class PlayerListCommand extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   @Override
   public void execute(ArrayList<String> var1) {
      var1 = new ArrayList();

      for (NetworkPlayerInfo var3 : mc.getNetHandler().getPlayerInfoMap()) {
         var1.add(var3.getGameProfile().getName().replace("\u00a7", "&"));
      }

      if (var1.isEmpty()) {
         ChatUtil.addFormattedChatMessage(String.format("%sNo players&r", Valor.CHAT_PREFIX));
      } else {
         ChatUtil.addRawChatMessage(String.format(ChatColor.format("%sPlayers:&r %s"), ChatColor.format(Valor.CHAT_PREFIX), String.join(", ", var1)));
      }
   }

   public PlayerListCommand() {
      super(new ArrayList<>(Arrays.asList("playerlist", "players")));
   }
}
