package valor.module;

import valor.Valor;
import valor.module.render.Notifications;
import valor.util.KeyUtil;

public abstract class Module {
   protected final String name;
   protected final ModuleCategory category;
   protected final boolean defaultEnabled;
   protected final int defaultKey;
   protected final boolean defaultHidden;
   protected boolean enabled;
   protected int key;
   protected boolean hidden;

   public void setKey(int key) {
      this.key = key;
   }

   public String getDescription() {
      return "";
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public String[] getSuffix() {
      return new String[0];
   }

   public void setHidden(boolean hidden) {
      this.hidden = hidden;
   }

   public void verifyValue(String value) {
   }

   public void onDisabled() {
   }

   public void onEnabled() {
   }

   public boolean toggle() {
      boolean newState = !this.enabled;
      this.setEnabled(newState);
      if (this.enabled == newState) {
         Module notifModule;
         if (Valor.moduleManager != null
            && (notifModule = Valor.moduleManager.getModule(Notifications.class)) instanceof Notifications
            && (Boolean) ((Notifications) notifModule).toggleSoundsProp.getValue()) {
            Valor.moduleManager.playToggleSound(this.enabled);
         }

         return true;
      } else {
         return false;
      }
   }

   public String formatModule() {
      return String.format("%s%s &r(%s&r)", this.key == 0 ? "" : String.format("&l[%s] &r", KeyUtil.getKeyName(this.key)), this.name, this.enabled ? "&a&lON" : "&c&lOFF");
   }

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (enabled) {
            this.onEnabled();
            return;
         }

         this.onDisabled();
      }
   }

   public String getName() {
      return this.name;
   }

   public Module(String name, boolean defaultEnabled, boolean defaultHidden, ModuleCategory category) {
      this.name = name;
      this.category = category;
      this.defaultEnabled = defaultEnabled;
      this.enabled = defaultEnabled;
      this.defaultKey = 0;
      this.key = 0;
      this.defaultHidden = defaultHidden;
      this.hidden = defaultHidden;
   }

   public Module(String name, boolean defaultEnabled, ModuleCategory category) {
      this(name, defaultEnabled, false, category);
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public int getKey() {
      return this.key;
   }

   public boolean isEnabled() {
      return this.enabled;
   }
}
