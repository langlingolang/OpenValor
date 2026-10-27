package valor.module;

import java.util.LinkedHashMap;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.events.KeyEvent;
import valor.module.render.ClickGuiMod;
import valor.module.render.Notifications;
import valor.util.ChatUtil;
import valor.util.NotificationManager;
import valor.util.SoundUtil;

public class ModuleManager {
   public final LinkedHashMap<Class<?>, Module> modules = new LinkedHashMap<>();

   @EventTarget
   public void onKey(KeyEvent event) {
      for (Module module : this.modules.values()) {
         boolean matchesKey = module.getKey() == event.getKey()
               || (module instanceof ClickGuiMod && (event.getKey() == 54 || event.getKey() == 210));
         if (matchesKey && module.toggle() && !(module instanceof ClickGuiMod)) {
            NotificationManager.moduleToggle(module.getName(), module.isEnabled());
            HudThemeManager theme = HudThemeManager.getInstance();
            if (theme != null && (Boolean) theme.toggleAlertsProp.getValue()) {
               String status = module.isEnabled() ? "&a&lON" : "&c&lOFF";
               ChatUtil.addFormattedChatMessage(String.format("%s%s: %s&r", Valor.CHAT_PREFIX, module.getName(), status));
            }
         }
      }
   }

   public void playSound() {
      this.playToggleSound(true);
   }

   public void playToggleSound(boolean enabled) {
      int soundMode = 0;
      Module notif = this.getModule(Notifications.class);
      if (notif instanceof Notifications) {
         Notifications notifications = (Notifications) notif;
         if (!(Boolean) notifications.toggleSoundsProp.getValue()) {
            return;
         }

         soundMode = (Integer) notifications.soundProp.getValue();
      }

      SoundUtil.playToggleSound(soundMode, enabled);
   }

   public Module getModule(String name) {
      return this.modules.values().stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   public Module getModule(Class<?> clazz) {
      return this.modules.get(clazz);
   }
}
