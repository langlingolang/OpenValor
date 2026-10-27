package valor.ui.dataset.impl;

import valor.setting.FloatProperty;
import valor.ui.dataset.Slider;
import valor.util.ChatColor;

public class FloatSlider extends Slider {
   private final FloatProperty property;

   @Override
   public boolean isVisible() {
      return this.property.isVisible();
   }

   @Override
   public double getIncrement() {
      return 0.1;
   }

   @Override
   public void setValueString(String var1) {
      try {
         this.property.setValue(Float.parseFloat(var1));
      } catch (Exception var2) {
      }
   }

   @Override
   public void stepping(boolean var1) {
      if (var1) {
         if (!((Float)this.property.getValue() >= this.property.getMax())) {
            this.property.setValue((float)Math.round((Float)this.property.getValue() * 10.0F + 1.0F) / 10.0F);
         }
      } else if (!((Float)this.property.getValue() <= this.property.getMin())) {
         this.property.setValue((float)Math.round((Float)this.property.getValue() * 10.0F - 1.0F) / 10.0F);
      }
   }

   @Override
   public String getValueColorString() {
      return ChatColor.format(this.property.getFormattedValue());
   }

   @Override
   public double getInput() {
      return (double)((Float)this.property.getValue()).floatValue();
   }

   @Override
   public String getName() {
      return this.property.getName().replace("-", " ");
   }

   public FloatSlider(FloatProperty var1) {
      this.property = var1;
   }

   @Override
   public void setValue(double var1) {
      this.property.setValue(Double.valueOf(var1).floatValue());
   }

   @Override
   public String getValueString() {
      return ((Float)this.property.getValue()).toString();
   }

   @Override
   public double getMin() {
      return (double)this.property.getMin().floatValue();
   }

   @Override
   public double getMax() {
      return (double)this.property.getMax().floatValue();
   }
}
