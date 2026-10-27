package valor.setting;

import com.google.gson.JsonObject;
import java.util.Locale;
import java.util.function.BooleanSupplier;

public class ColorProperty extends Property<Integer> {
   @Override
   public boolean parseValue(String stringValue) {
      if (stringValue == null) {
         return false;
      } else if ((stringValue = stringValue.trim().replaceFirst("^(#|0[xX])", "")).length() != 6 && stringValue.length() != 8) {
         return false;
      } else {
         try {
            return this.setValue(Integer.parseInt(stringValue.substring(stringValue.length() - 6), 16));
         } catch (NumberFormatException e) {
            return false;
         }
      }
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), String.format(Locale.ROOT, "%06X", this.getValue() & 16777215));
   }

   @Override
   public String getBoundsDescription() {
      return "RGB";
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.parseValue(json.get(this.getName()).getAsString());
   }

   @Override
   public boolean setValue(Integer val) {
      if (val != null) {
         return super.setValue(0xFF000000 | val & 16777215);
      }
      return false;
   }

   @Override
   public String getFormattedValue() {
      return String.format("#%06X", this.getValue() & 16777215);
   }

   public ColorProperty(String name, Integer defaultValue, BooleanSupplier visibility) {
      super(name, defaultValue, visibility);
   }

   public ColorProperty(String name, Integer defaultValue) {
      this(name, defaultValue, null);
   }
}
