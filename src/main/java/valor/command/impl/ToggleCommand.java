package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import valor.Valor;
import valor.command.Command;
import valor.core.HudThemeManager;
import valor.module.Module;
import valor.util.ChatUtil;
import valor.util.NotificationManager;

public final class ToggleCommand extends Command {
   @Override
   public void execute(ArrayList<String> var1) {
      String var10000;
      Object[] var10001;
      Object[] var10002;
      byte var10003;
      Object var10004;
      if (var1.size() < 2) {
         var10000 = "%sUsage: .%s <&omodule&r>&r";
         var10001 = new Object[]{Valor.CHAT_PREFIX, null};
         var10002 = var10001;
         var10003 = 1;
         var10004 = ((String)var1.get(0)).toLowerCase(Locale.ROOT);
      } else {
         Module var2;
         if ((var2 = Valor.moduleManager.getModule((String)var1.get(1))) == null) {
            var10000 = "%sModule not found (&o%s&r)&r";
            var10001 = new Object[]{Valor.CHAT_PREFIX, null};
            var10002 = var10001;
            var10003 = 1;
            var10004 = var1.get(1);
         } else {
            boolean var3 = true;
            if (var1.size() >= 3) {
               if (((String)var1.get(2)).equalsIgnoreCase("true")
                  || ((String)var1.get(2)).equalsIgnoreCase("on")
                  || ((String)var1.get(2)).equalsIgnoreCase("1")) {
                  var3 = !var2.isEnabled();
               } else if (((String)var1.get(2)).equalsIgnoreCase("false")
                  || ((String)var1.get(2)).equalsIgnoreCase("off")
                  || ((String)var1.get(2)).equalsIgnoreCase("0")) {
                  var3 = var2.isEnabled();
               }
            }

            if (!var3 || !var2.toggle()) {
               return;
            }

            NotificationManager.moduleToggle(var2.getName(), var2.isEnabled());
            HudThemeManager var4;
            if ((var4 = HudThemeManager.getInstance()) == null || !(Boolean)var4.toggleAlertsProp.getValue()) {
               return;
            }

            var10000 = "%s%s: %s&r";
            var10001 = new Object[]{Valor.CHAT_PREFIX, var2.getName(), null};
            var10002 = var10001;
            var10003 = 2;
            var10004 = var2.isEnabled() ? "&a&lON" : "&c&lOFF";
         }
      }

      var10002[var10003] = var10004;
      ChatUtil.addFormattedChatMessage(String.format(var10000, var10001));
   }

   public ToggleCommand() {
      super(new ArrayList<>(Arrays.asList("toggle", "t")));
   }
}
