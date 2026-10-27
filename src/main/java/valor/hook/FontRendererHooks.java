package valor.hook;

import valor.Valor;
import valor.module.render.ESP;
import valor.module.render.NickHider;

public class FontRendererHooks {
   public static char onCharAt(String text, int index) {
      char ch = text.charAt(index);
      return (ch < '0' || ch > '9') && (ch < 'a' || ch > 'f') && (ch < 'A' || ch > 'F') ? ch : 'r';
   }

   public static String onGetStringWidth(String text) {
      if (text == null) {
         return text;
      } else if (RendererLivingEntityHooks.isSuppressingName() || ESP.isRenderingOutline()) {
         return "";
      } else if (Valor.moduleManager == null) {
         return text;
      } else {
         NickHider nickHider;
         return (nickHider = (NickHider)Valor.moduleManager.modules.get(NickHider.class)) != null && nickHider.isEnabled() ? nickHider.replaceNick(text) : text;
      }
   }

   public static String onRenderString(String text) {
      if (text == null) {
         return text;
      } else if (RendererLivingEntityHooks.isSuppressingName() || ESP.isRenderingOutline()) {
         return "";
      } else if (Valor.moduleManager == null) {
         return text;
      } else {
         NickHider nickHider;
         return (nickHider = (NickHider)Valor.moduleManager.modules.get(NickHider.class)) != null && nickHider.isEnabled() ? nickHider.replaceNick(text) : text;
      }
   }
}
