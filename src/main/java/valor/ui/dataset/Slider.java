package valor.ui.dataset;

public abstract class Slider {
   public abstract boolean isVisible();

   public abstract double getIncrement();

   public abstract void setValueString(String var1);

   public abstract void stepping(boolean var1);

   public abstract String getValueColorString();

   public abstract double getInput();

   public abstract String getName();

   public abstract void setValue(double var1);

   public abstract String getValueString();

   public abstract double getMin();

   public abstract double getMax();
}
