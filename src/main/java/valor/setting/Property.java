package valor.setting;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import valor.module.Module;

public abstract class Property<T> {
   private final String name;
   private final T defaultValue;
   private final Predicate<T> validator;
   private final BooleanSupplier visibility;
   private T value;
   private Module parentModule;

   public boolean isVisible() {
      return this.visibility == null || this.visibility.getAsBoolean();
   }

   public abstract boolean parseValue(String stringValue);

   public T getValue() {
      return this.value;
   }

   public boolean setValue(T val) {
      if (this.validator != null && !this.validator.test(val)) {
         return false;
      } else {
         this.value = val;
         if (this.parentModule != null) {
            this.parentModule.verifyValue(this.name);
         }

         return true;
      }
   }

   public abstract void writeJson(JsonObject json);

   public void resetToDefault() {
      this.setValue(this.defaultValue);
   }

   public abstract String getBoundsDescription();

   public abstract boolean readJson(JsonObject json);

   public void setParentModule(Module module) {
      this.parentModule = module;
   }

   public abstract String getFormattedValue();

   public String getName() {
      return this.name;
   }

   public T getDefaultValue() {
      return this.defaultValue;
   }

   public Property(String name, T defaultValue, BooleanSupplier visibility) {
      this(name, defaultValue, null, visibility);
   }

   public Property(String name, T defaultValue, Predicate<T> validator, BooleanSupplier visibility) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.validator = validator;
      this.visibility = visibility;
      this.value = defaultValue;
      this.parentModule = null;
   }
}
