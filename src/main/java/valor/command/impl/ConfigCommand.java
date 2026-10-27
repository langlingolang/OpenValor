package valor.command.impl;

import java.awt.Desktop;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import valor.Valor;
import valor.command.Command;
import valor.core.ConfigManager;
import valor.util.ChatColor;
import valor.util.ChatUtil;

public final class ConfigCommand extends Command {
   @Override
   public void execute(ArrayList<String> var1) {
      if (var1.size() < 2) {
         String var10 = ((String)var1.get(0)).toLowerCase(Locale.ROOT);
         ChatUtil.addFormattedChatMessage(
            String.format(
               "%sUsage: .%s &oload&r/&osave&r <&oname&r> | .%s &olist&r | .%s &ofolder&r | .%s &odelete&r <&oname&r>&r", Valor.CHAT_PREFIX, var10, var10, var10, var10
            )
         );
      } else {
         String var2;
         if ((var2 = (String)var1.get(1)).equalsIgnoreCase("l")) {
            var2 = var1.size() < 3 ? "list" : "load";
         }

         var2 = var2.toLowerCase(Locale.ROOT);
         switch (var2) {
            case "load":
            case "reload":
               if (var1.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sMissing config name (use '&odefault&r' or '&o!&r' to load default config)&r", Valor.CHAT_PREFIX));
                  return;
               }

               new ConfigManager((String)var1.get(2), false).load();
               return;
            case "s":
            case "save":
               if (var1.size() < 3) {
                  new ConfigManager(ConfigManager.sanitizeConfigName(ConfigManager.currentConfig), true).save();
                  return;
               }

               new ConfigManager((String)var1.get(2), true).save();
               return;
            case "list":
               List<String> var5;
               if ((var5 = ConfigManager.getConfigList()).isEmpty()) {
                  ChatUtil.addFormattedChatMessage(String.format("%sNo configs found (&o%s&r)&r", Valor.CHAT_PREFIX, ConfigManager.CONFIG_DIR.getPath()));
                  return;
               }

               ChatUtil.addFormattedChatMessage(String.format("%sConfigs:&r", Valor.CHAT_PREFIX));

               for (String var12 : var5) {
                  var2 = var12;
                  String var11 = ChatColor.format(String.format("&7\u00bb&r &o%s&r", var2));
                  var2 = String.format(".config load %s", var2);
                  ChatUtil.addChatMessage(
                     new ChatComponentText(var11)
                        .setChatStyle(
                           new ChatStyle()
                              .setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, var2))
                              .setChatHoverEvent(new HoverEvent(net.minecraft.event.HoverEvent.Action.SHOW_TEXT, new ChatComponentText(var2)))
                        )
                  );
               }

               return;
            case "delete":
            case "del":
            case "remove":
               if (var1.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sMissing config name&r", Valor.CHAT_PREFIX));
                  return;
               }

               ConfigManager.deleteConfig((String)var1.get(2));
               return;
            case "f":
            case "folder":
            case "dir":
            case "directory":
               try {
                  Desktop.getDesktop().open(ConfigManager.CONFIG_DIR);
                  return;
               } catch (Exception var4) {
                  ChatUtil.addFormattedChatMessage(String.format("%sFailed to open (&o%s&r)&r", Valor.CHAT_PREFIX, ConfigManager.CONFIG_DIR.getPath()));
                  return;
               }
            default:
               ChatUtil.addFormattedChatMessage(String.format("%sInvalid argument (&o%s&r)&r", Valor.CHAT_PREFIX, var1.get(1)));
         }
      }
   }

   public ConfigCommand() {
      super(new ArrayList<>(Arrays.asList("config", "cfg", "c")));
   }
}
