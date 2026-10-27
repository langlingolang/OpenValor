package valor.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import valor.Valor;

public final class ResourceUtil {
   public static InputStream getResourceStream(String resourcePath) {
      String path = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
      InputStream in;
      if ((in = Valor.class.getResourceAsStream("/".concat(String.valueOf(path)))) != null) {
         return in;
      } else {
         ClassLoader cl;
         if ((cl = Valor.class.getClassLoader()) != null && (in = cl.getResourceAsStream(path)) != null) {
            return in;
         } else {
            ClassLoader contextCl;
            if ((contextCl = Thread.currentThread().getContextClassLoader()) != null && contextCl != cl && (in = contextCl.getResourceAsStream(path)) != null) {
               return in;
            } else {
               for (String jarProp : new String[]{"valor.jar", "valor.notch.jar", "valor.forge.jar"}) {
                  try {
                     String jarPath;
                     InputStream jarStream;
                     if ((jarPath = System.getProperty(jarProp)) != null && !jarPath.isEmpty() && (jarStream = getResourceStreamFromJar(new File(jarPath), path)) != null) {
                        return jarStream;
                     }
                  } catch (Throwable ignored) {
                  }
               }

               return null;
            }
         }
      }
   }

   private static InputStream getResourceStreamFromJar(File file, String entryName) {
      if (!file.isFile()) {
         return null;
      }
      try (JarFile jar = new JarFile(file)) {
         JarEntry entry = jar.getJarEntry(entryName);
         if (entry == null) {
            return null;
         }
         try (InputStream in = jar.getInputStream(entry);
              ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
               out.write(buf, 0, read);
            }
            return new ByteArrayInputStream(out.toByteArray());
         }
      } catch (Throwable ignored) {
         return null;
      }
   }
}
