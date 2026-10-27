package valor.setting;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class BooleanProperty extends Property<Boolean> {
   @Override
   public boolean parseValue(String stringValue) {
      if (stringValue == null) {
         return this.setValue(!this.getValue());
      } else {
         return !stringValue.equalsIgnoreCase("true") && !stringValue.equalsIgnoreCase("on") && !stringValue.equalsIgnoreCase("1")
            ? (stringValue.equalsIgnoreCase("false") || stringValue.equalsIgnoreCase("off") || stringValue.equalsIgnoreCase("0")) && this.setValue(Boolean.FALSE)
            : this.setValue(Boolean.TRUE);
      }
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }

   @Override
   public String getBoundsDescription() {
      return "true/false";
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.setValue(json.get(this.getName()).getAsBoolean());
   }

   @Override
   public String getFormattedValue() {
      return this.getValue() ? "&atrue" : "&cfalse";
   }

   public BooleanProperty(String name, Boolean defaultValue, BooleanSupplier visibility) {
      super(name, defaultValue, visibility);
   }

   public BooleanProperty(String name, Boolean defaultValue) {
      this(name, defaultValue, null);
   }
}
