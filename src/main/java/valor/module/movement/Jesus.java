package valor.module.movement;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;

public class Jesus extends Module {
   private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
   public final FloatProperty speedProp = new FloatProperty("speed", 2.5F, 0.0F, 3.0F);
   public final BooleanProperty noPushProp = new BooleanProperty("no-push", Boolean.TRUE);
   public final BooleanProperty groundOnlyProp = new BooleanProperty("ground-only", Boolean.TRUE);

   @Override
   public String[] getSuffix() {
      return new String[]{DECIMAL_FORMAT.format(this.speedProp.getValue())};
   }

   public Jesus() {
      super("Jesus", false, ModuleCategory.MOVEMENT);
   }
}
