package valor.setting;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class FloatProperty extends Property<Float> {
   private final Float min;
   private final Float max;

   @Override
   public boolean parseValue(String stringValue) {
      return this.setValue(Float.parseFloat(stringValue));
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }

   public Float getMax() {
      return this.max;
   }

   @Override
   public String getBoundsDescription() {
      return String.format("%s-%s", this.min, this.max);
   }

   public Float getMin() {
      return this.min;
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.setValue(json.get(this.getName()).getAsNumber().floatValue());
   }

   @Override
   public String getFormattedValue() {
      return String.format("&6%s", this.getValue());
   }

   public FloatProperty(String name, Float defaultValue, Float min, Float max, BooleanSupplier visibility) {
      super(name, defaultValue, val -> val >= min && val <= max, visibility);
      this.min = min;
      this.max = max;
   }

   public FloatProperty(String name, Float defaultValue, Float min, Float max) {
      this(name, defaultValue, min, max, null);
   }
}
