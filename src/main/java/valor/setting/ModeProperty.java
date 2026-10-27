package valor.setting;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class ModeProperty extends Property<Integer> {
   private final String[] modes;

   @Override
   public boolean parseValue(String stringValue) {
      stringValue = stringValue.replace("_", "");

      for (int i = 0; i < this.modes.length; i++) {
         if (stringValue.equalsIgnoreCase(this.modes[i].replace("_", ""))) {
            return this.setValue(i);
         }
      }

      return false;
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getModeName());
   }

   public String getModeName() {
      int idx = this.getValue();
      return idx >= 0 && idx < this.modes.length ? this.modes[idx] : "";
   }

   public void previousMode() {
      int idx = this.getValue() - 1;
      if (idx < 0) {
         idx = this.modes.length - 1;
      }

      this.setValue(idx);
   }

   public void nextMode() {
      int idx = this.getValue() + 1;
      if (idx >= this.modes.length) {
         idx = 0;
      }

      this.setValue(idx);
   }

   @Override
   public String getBoundsDescription() {
      return String.join(", ", this.modes);
   }

   public String[] getModes() {
      return this.modes;
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.parseValue(json.get(this.getName()).getAsString());
   }

   @Override
   public String getFormattedValue() {
      String mode = this.getModeName();
      return mode.isEmpty() ? "&4?" : String.format("&9%s", mode);
   }

   public ModeProperty(String name, Integer defaultValue, String[] modes) {
      this(name, defaultValue, modes, null);
   }

   public ModeProperty(String name, Integer defaultValue, String[] modes, BooleanSupplier visibility) {
      super(name, defaultValue, visibility);
      this.modes = modes;
   }
}
