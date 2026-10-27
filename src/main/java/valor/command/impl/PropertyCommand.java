package valor.command.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import valor.Valor;
import valor.command.Command;
import valor.module.Module;
import valor.setting.BooleanProperty;
import valor.setting.Property;
import valor.util.ChatUtil;

public final class PropertyCommand extends Command {
   @Override
   public void execute(ArrayList<String> var1) {
      Module var2 = Valor.moduleManager.getModule((String)var1.get(0));
      if (var1.size() >= 2) {
         Property var9;
         if ((var9 = Valor.propertyManager.getProperty(var2, (String)var1.get(1))) == null) {
            ChatUtil.addFormattedChatMessage(String.format("%s%s has no property &o%s&r", Valor.CHAT_PREFIX, var2.getName(), var1.get(1)));
         } else if (var1.size() < 3 && !(var9 instanceof BooleanProperty)) {
            ChatUtil.addFormattedChatMessage(String.format("%s%s: &o%s&r is set to %s&r (%s)&r", Valor.CHAT_PREFIX, var2.getName(), var9.getName(), var9.getFormattedValue(), var9.getBoundsDescription()));
         } else {
            String var7 = var1.size() < 3 ? null : String.join(" ", var1.subList(2, var1.size()));

            try {
               if (var9.parseValue(var7)) {
                  ChatUtil.addFormattedChatMessage(String.format("%s%s: &o%s&r has been set to %s&r", Valor.CHAT_PREFIX, var2.getName(), var9.getName(), var9.getFormattedValue()));
                  return;
               }
            } catch (Exception var4) {
            }

            ChatUtil.addFormattedChatMessage(String.format("%sInvalid value for property &o%s&r (%s)&r", Valor.CHAT_PREFIX, var9.getName(), var9.getBoundsDescription()));
         }
      } else {
         List<Property<?>> var3;
         List<Property<?>> var5;
         if ((var3 = Valor.propertyManager.propertiesByModule.get(var2.getClass())) != null
            && !(var5 = var3.stream().filter(Property::isVisible).collect(Collectors.toList())).isEmpty()) {
            ChatUtil.addFormattedChatMessage(String.format("%s%s:&r", Valor.CHAT_PREFIX, var2.formatModule()));

            for (Property var8 : var5) {
               ChatUtil.addFormattedChatMessage(String.format("&7\u00bb&r %s: %s&r", var8.getName(), var8.getFormattedValue()));
            }
         } else {
            ChatUtil.addFormattedChatMessage(String.format("%s%s has no properties&r", Valor.CHAT_PREFIX, var2.formatModule()));
         }
      }
   }

   public PropertyCommand() {
      super(new ArrayList<>(Valor.moduleManager.modules.values().stream().map(Module::getName).collect(Collectors.toList())));
   }
}
