package valor.asm;

import org.objectweb.asm.ClassWriter;
import valor.agent.NativeBridge;

public class SafeClassWriter extends ClassWriter {
   private final ClassLoader classLoader;

   public static volatile ClassLoader CURRENT_TRANSFORMER_LOADER = null;

   public SafeClassWriter(int flags) {
      this(flags, CURRENT_TRANSFORMER_LOADER != null ? CURRENT_TRANSFORMER_LOADER :
                  (NativeBridge.getGenesisClassLoader() != null ? NativeBridge.getGenesisClassLoader() :
                  Thread.currentThread().getContextClassLoader()));
   }

   public SafeClassWriter(int flags, ClassLoader classLoader) {
      super(flags);
      this.classLoader = classLoader != null ? classLoader :
                         (CURRENT_TRANSFORMER_LOADER != null ? CURRENT_TRANSFORMER_LOADER :
                         (NativeBridge.getGenesisClassLoader() != null ? NativeBridge.getGenesisClassLoader() :
                         SafeClassWriter.class.getClassLoader()));
   }

   @Override
   protected String getCommonSuperClass(String type1, String type2) {
      try {
         ClassLoader cl = this.classLoader != null ? this.classLoader :
                          (NativeBridge.getGenesisClassLoader() != null ? NativeBridge.getGenesisClassLoader() :
                          getClass().getClassLoader());
         Class<?> c, d;
         try {
            c = Class.forName(type1.replace('/', '.'), false, cl);
            d = Class.forName(type2.replace('/', '.'), false, cl);
         } catch (Throwable e) {
            return "java/lang/Object";
         }
         if (c.isAssignableFrom(d)) {
            return type1;
         }
         if (d.isAssignableFrom(c)) {
            return type2;
         }
         if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
         }
         do {
            c = c.getSuperclass();
         } while (c != null && !c.isAssignableFrom(d));
         return c == null ? "java/lang/Object" : c.getName().replace('.', '/');
      } catch (Throwable t) {
         return "java/lang/Object";
      }
   }
}
