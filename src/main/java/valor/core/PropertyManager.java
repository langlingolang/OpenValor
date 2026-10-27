package valor.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import valor.module.Module;
import valor.setting.Property;

public class PropertyManager {
   public LinkedHashMap<Class<?>, ArrayList<Property<?>>> propertiesByModule = new LinkedHashMap<>();

   public Property<?> getProperty(Module module, String name) {
      ArrayList<Property<?>> list = this.propertiesByModule.get(module.getClass());
      if (list == null) {
         return null;
      }

      for (Property<?> prop : list) {
         if (prop.getName().replace("-", "").equalsIgnoreCase(name.replace("-", ""))) {
            return prop;
         }
      }

      return null;
   }
}
