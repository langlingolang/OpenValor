package valor.setting;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class PercentageProperty extends Property<Integer> {
   private final Integer min;
   private final Integer max;

   @Override
   public boolean parseValue(String stringValue) {
      return this.setValue(Integer.parseInt(stringValue.replace("%", "")));
   }

   public Integer getMax() {
      return this.max;
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }

   @Override
   public String getBoundsDescription() {
      return String.format("%d-%d%%", this.min, this.max);
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
      return String.format("&b%d%%", this.getValue());
   }

   public PercentageProperty(String name, Integer defaultValue, BooleanSupplier visibility) {
      this(name, defaultValue, 0, 100, visibility);
   }

   public PercentageProperty(String name, Integer defaultValue) {
      this(name, defaultValue, null);
   }

   public PercentageProperty(String name, Integer defaultValue, Integer min, Integer max, BooleanSupplier visibility) {
      super(name, defaultValue, val -> val >= min && val <= max, visibility);
      this.min = min;
      this.max = max;
   }

   public PercentageProperty(String name, Integer defaultValue, Integer min, Integer max) {
      this(name, defaultValue, min, max, null);
   }
}
