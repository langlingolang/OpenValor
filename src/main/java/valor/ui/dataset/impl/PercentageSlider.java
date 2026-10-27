package valor.ui.dataset.impl;

import valor.setting.PercentageProperty;
import valor.ui.dataset.Slider;
import valor.util.ChatColor;

public class PercentageSlider extends Slider {
   private final PercentageProperty property;

   @Override
   public boolean isVisible() {
      return this.property.isVisible();
   }

   @Override
   public double getIncrement() {
      return 1.0;
   }

   @Override
   public void setValueString(String var1) {
      try {
         this.property.setValue(Integer.parseInt(var1));
      } catch (Exception var2) {
      }
   }

   @Override
   public void stepping(boolean var1) {
      if (var1) {
         if ((Integer)this.property.getValue() < this.property.getMax()) {
            this.property.setValue((Integer)this.property.getValue() + 1);
         }
      } else if ((Integer)this.property.getValue() > this.property.getMin()) {
         this.property.setValue((Integer)this.property.getValue() - 1);
      }
   }

   @Override
   public String getValueColorString() {
      return ChatColor.format(this.property.getFormattedValue());
   }

   @Override
   public double getInput() {
      return (double)((Integer)this.property.getValue()).intValue();
   }

   @Override
   public String getName() {
      return this.property.getName().replace("-", " ");
   }

   public PercentageSlider(PercentageProperty var1) {
      this.property = var1;
   }

   @Override
   public void setValue(double var1) {
      this.property.setValue(Double.valueOf(var1).intValue());
   }

   @Override
   public String getValueString() {
      return ((Integer)this.property.getValue()).toString();
   }

   @Override
   public double getMin() {
      return (double)this.property.getMin().intValue();
   }

   @Override
   public double getMax() {
      return (double)this.property.getMax().intValue();
   }
}
