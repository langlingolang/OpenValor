package valor.command.impl;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatColor;
import valor.util.ChatUtil;

public final class DenickCommand extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();

   @Override
   public void execute(ArrayList<String> var1) {
      if (var1.size() < 2) {
         ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s <&oname&r>&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
      } else {
         NetworkPlayerInfo var2;
         if ((var2 = mc.getNetHandler().getPlayerInfo(ChatColor.format((String)var1.get(1)))) != null) {
            Property var3;
            GameProfile var6;
            if ((var3 = Iterables.getFirst((var6 = var2.getGameProfile()).getProperties().get("textures"), null)) != null) {
               String var4;
               String var7 = (var4 = new String(Base64.getDecoder().decode(var3.getValue().getBytes(StandardCharsets.UTF_8)))).contains("profileName\" : \"")
                  ? var4.split("profileName\" : \"")[1].split("\"")[0]
                  : "?";
               String var5 = var4.contains("profileId\" : \"") ? var4.split("profileId\" : \"")[1].split("\"")[0] : "?";
               ChatUtil.addRawChatMessage(
                  String.format(ChatColor.format("%s%s&r -> %s (&o%s&r)&r"), ChatColor.format(Valor.CHAT_PREFIX), var6.getName().replace("\u00a7", "&"), var7, var5)
               );
               if (!var5.isEmpty() && !var5.equals("?")) {
                  Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(var5), null);
               }
            } else {
               ChatUtil.addRawChatMessage(String.format(ChatColor.format("%sNo textures for entity with name &o%s&r"), ChatColor.format(Valor.CHAT_PREFIX), var1.get(1)));
            }
         } else {
            ChatUtil.addRawChatMessage(String.format(ChatColor.format("%sNo entity with name &o%s&r"), ChatColor.format(Valor.CHAT_PREFIX), var1.get(1)));
         }
      }
   }

   public DenickCommand() {
      super(new ArrayList<>(Collections.singletonList("denick")));
   }
}
