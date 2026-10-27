package valor.module;

public enum ModuleCategory {
   COMBAT("Combat"),
   MOVEMENT("Movement"),
   PLAYER("Player"),
   VISUAL("Visual"),
   WORLD("World");

public final String displayName;

   private ModuleCategory(String var3) {
      this.displayName = var3;
   }

   public String getDisplayName() {
      return this.displayName;
   }
}
