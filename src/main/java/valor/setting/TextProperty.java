package valor.setting;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class TextProperty extends Property<String> {
   @Override
   public boolean parseValue(String stringValue) {
      return this.setValue(stringValue);
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }

   @Override
   public String getBoundsDescription() {
      return "text";
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.parseValue(json.get(this.getName()).getAsString());
   }

   @Override
   public String getFormattedValue() {
      return String.format("&f%s", this.getValue());
   }

   public TextProperty(String name, String defaultValue, BooleanSupplier visibility) {
      super(name, defaultValue, visibility);
   }

   public TextProperty(String name, String defaultValue) {
      this(name, defaultValue, null);
   }
}
