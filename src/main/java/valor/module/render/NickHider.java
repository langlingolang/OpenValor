package valor.module.render;

import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.TextProperty;
import valor.util.ChatColor;

public class NickHider extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public final TextProperty nameProp = new TextProperty("name", "You");
   public final BooleanProperty scoreboardProp = new BooleanProperty("scoreboard", Boolean.TRUE);
   public final BooleanProperty levelProp = new BooleanProperty("level", Boolean.TRUE);

   public final String replaceNick(String text) {
      if (text != null && mc.thePlayer != null) {
         if ((Boolean) this.scoreboardProp.getValue() && text.matches("\u00a77\\d{2}/\\d{2}/\\d{2}(?:\\d{2})?  ?\u00a78.*")) {
            text = text.replaceAll("\u00a78", "\u00a78\u00a7k").replaceAll("[^\\x00-\\x7F\u00a7]", "?");
         }

         return text.replaceAll(mc.thePlayer.getName(), Matcher.quoteReplacement(ChatColor.format((String) this.nameProp.getValue())));
      } else {
         return text;
      }
   }

   public NickHider() {
      super("NickHider", false, false, ModuleCategory.VISUAL);
   }
}
