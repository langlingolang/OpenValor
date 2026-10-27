package valor.module.combat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PickEvent;
import valor.events.RaytraceEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.setting.PercentageProperty;

public class Reach extends Module {
   private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.US));
   private final Random random = new Random();
   private boolean reachActive = true;
   public final FloatProperty rangeProp = new FloatProperty("range", 3.1F, 3.0F, 6.0F);
   public final PercentageProperty chanceProp = new PercentageProperty("chance", 100);

@Override
   public String[] getSuffix() {
      return new String[]{DECIMAL_FORMAT.format(this.rangeProp.getValue())};
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         this.reachActive = this.random.nextDouble() <= (double)((Integer)this.chanceProp.getValue()).intValue() / 100.0;
      }
   }

   @EventTarget
   public void onPick(PickEvent var1) {
      if (this.isEnabled() && this.reachActive) {
         var1.setRange(((Float)this.rangeProp.getValue()).doubleValue());
      }
   }

   @EventTarget
   public void onRaytrace(RaytraceEvent var1) {
      if (this.isEnabled() && this.reachActive) {
         var1.setRange(Math.max(var1.getRange(), ((Float)this.rangeProp.getValue()).doubleValue() + 0.5));
      }
   }

   public Reach() {
      super("Reach", false, ModuleCategory.COMBAT);
   }
}
