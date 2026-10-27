package valor.command.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import valor.Valor;
import valor.command.Command;
import valor.util.ChatUtil;

public final class VClipCommand extends Command {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

   @Override
   public void execute(ArrayList<String> var1) {
      if (var1.size() >= 2) {
         double distance = 0.0;
         try {
            distance = Double.parseDouble((String)var1.get(1));
         } catch (NumberFormatException ignored) {
         }
         mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + distance, mc.thePlayer.posZ);
         ChatUtil.addFormattedChatMessage(String.format("%sClipped (%s blocks)", Valor.CHAT_PREFIX, DECIMAL_FORMAT.format(distance)));
         return;
      }

      ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s <&odistance&r>&r", Valor.CHAT_PREFIX, ((String)var1.get(0)).toLowerCase(Locale.ROOT)));
   }

   public VClipCommand() {
      super(new ArrayList<>(Collections.singletonList("vclip")));
   }
}
