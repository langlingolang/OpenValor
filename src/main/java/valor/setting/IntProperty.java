package valor.setting;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class IntProperty extends Property<Integer> {
   private final Integer min;
   private final Integer max;

   @Override
   public boolean parseValue(String stringValue) {
      return this.setValue(Integer.parseInt(stringValue));
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }

   public Integer getMax() {
      return this.max;
   }

   @Override
   public String getBoundsDescription() {
      return String.format("%d-%d", this.min, this.max);
   }

   public Integer getMin() {
      return this.min;
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.setValue(json.get(this.getName()).getAsNumber().intValue());
   }

   @Override
   public String getFormattedValue() {
      return String.format("&e%s", this.getValue());
   }

   public IntProperty(String name, Integer defaultValue, Integer min, Integer max, BooleanSupplier visibility) {
      super(name, defaultValue, val -> val >= min && val <= max, visibility);
      this.min = min;
      this.max = max;
   }

   public IntProperty(String name, Integer defaultValue, Integer min, Integer max) {
      this(name, defaultValue, min, max, null);
   }
}
