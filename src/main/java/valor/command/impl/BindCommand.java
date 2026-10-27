package valor.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import valor.Valor;
import valor.command.Command;
import valor.module.Module;
import valor.util.ChatUtil;
import valor.util.KeyUtil;

public final class BindCommand extends Command {
   private static int parseCustomKey(String var0) {
      if (var0.startsWith("MOUSE")) {
         try {
            int var1;
            if ((var1 = Integer.parseInt(var0.substring(5))) >= 0 && var1 < Mouse.getButtonCount()) {
               return var1;
            }
         } catch (StringIndexOutOfBoundsException | NumberFormatException var2) {
         }
      }

      int var4;
      if ((var4 = Mouse.getButtonIndex(var0)) != -1) {
         return var4;
      } else {
         switch (var0) {
            case "LBUTTON":
            case "LMB":
            case "LEFTCLICK":
               return 0;
            case "RBUTTON":
            case "RMB":
            case "RIGHTCLICK":
               return 1;
            case "MBUTTON":
            case "MMB":
            case "MIDDLECLICK":
            case "SCROLLCLICK":
               return 2;
            case "MOUSE3":
            case "XBUTTON1":
            case "SIDEBUTTON1":
            case "BOTTOMSIDE":
               return 3;
            case "MOUSE4":
            case "XBUTTON2":
            case "SIDEBUTTON2":
            case "TOPSIDE":
               return 4;
            case "MOUSE5":
               return 5;
            case "MOUSE6":
               return 6;
            case "MOUSE7":
               return 7;
            default:
               return -1;
         }
      }
   }

   @Override
   public void execute(ArrayList<String> var1) {
      if (var1.size() < 3) {
         if (var1.size() == 2 && (((String)var1.get(1)).equalsIgnoreCase("l") || ((String)var1.get(1)).equalsIgnoreCase("list"))) {
            List<Module> var7;
            if ((var7 = Valor.moduleManager.modules.values().stream().filter(var0 -> var0.getKey() != 0).collect(Collectors.toList())).isEmpty()) {
               ChatUtil.addFormattedChatMessage(String.format("%sNo binds&r", Valor.CHAT_PREFIX));
            } else {
               ChatUtil.addFormattedChatMessage(String.format("%sBinds:&r", Valor.CHAT_PREFIX));

               for (Module var8 : var7) {
                  ChatUtil.addFormattedChatMessage(String.format("%s\u00bb&r %s&r", var8.isHidden() ? "&8" : "&7", var8.formatModule()));
               }
            }
         } else {
            ChatUtil.addFormattedChatMessage(
               String.format(
                  "%sUsage: .%s <&omodule&r> <&okey&r>&r | .%s <&omodule&r> &onone&r | .%s &olist&r",
                  Valor.CHAT_PREFIX,
                  ((String)var1.get(0)).toLowerCase(Locale.ROOT),
                  ((String)var1.get(0)).toLowerCase(Locale.ROOT),
                  ((String)var1.get(0)).toLowerCase(Locale.ROOT)
               )
            );
         }
      } else {
         String var2;
         int var3;
         if (!(var2 = ((String)var1.get(2)).toUpperCase()).equalsIgnoreCase("NONE") && !var2.equalsIgnoreCase("NULL") && !var2.equalsIgnoreCase("0")) {
            int var4;
            if ((var3 = Keyboard.getKeyIndex(var2)) == 0 && (var4 = parseCustomKey(var2)) != -1) {
               var3 = var4 - 100;
            }
         } else {
            var3 = 0;
         }

         if (!((String)var1.get(1)).equals("*")) {
            Module var6;
            if ((var6 = Valor.moduleManager.getModule((String)var1.get(1))) == null) {
               ChatUtil.addFormattedChatMessage(String.format("%sModule not found (&o%s&r)&r", Valor.CHAT_PREFIX, var1.get(1)));
            } else {
               var6.setKey(var3);
               if (var3 == 0) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUnbind &o%s&r", Valor.CHAT_PREFIX, var6.getName()));
               } else {
                  ChatUtil.addFormattedChatMessage(String.format("%sBound &o%s&r to &l[%s]&r", Valor.CHAT_PREFIX, var6.getName(), KeyUtil.getKeyName(var3)));
               }
            }
         } else {
            Iterator var5 = Valor.moduleManager.modules.values().iterator();

            while (var5.hasNext()) {
               ((Module)var5.next()).setKey(var3);
            }

            if (var3 == 0) {
               ChatUtil.addFormattedChatMessage(String.format("%sUnbind all modules&r", Valor.CHAT_PREFIX));
            } else {
               ChatUtil.addFormattedChatMessage(String.format("%sBind all modules to &l[%s]&r", Valor.CHAT_PREFIX, KeyUtil.getKeyName(var3)));
            }
         }
      }
   }

   public BindCommand() {
      super(new ArrayList<>(Arrays.asList("bind", "b")));
   }
}
