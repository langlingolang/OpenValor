package valor.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import valor.Valor;
import valor.agent.NativeBridge;

public final class AssetExtractor {
   private static final Logger LOGGER = LogManager.getLogger("Valor");
   private static final String[] SOUND_PACKS = new String[]{"sigma", "rise", "quickmacro"};
   private static final String[] TOGGLE_STATES = new String[]{"enable", "disable"};
   private static boolean initialized;
   private static boolean failed;

   private static File findAgentJar() {
      for (String jarProp : new String[]{"valor.jar", "valor.forge.jar"}) {
         try {
            String propVal;
            File jarFile;
            if ((propVal = System.getProperty(jarProp)) != null && !propVal.isEmpty() && (jarFile = new File(propVal)).isFile() && containsValorSounds(jarFile)) {
               return jarFile;
            }
         } catch (Throwable ignored) {
         }
      }

      for (Class<?> clazz : new Class[]{Valor.class, NativeBridge.class}) {
         try {
            File codeSourceFile;
            URL codeSourceUrl;
            if (clazz.getProtectionDomain() != null
               && clazz.getProtectionDomain().getCodeSource() != null
               && (codeSourceUrl = clazz.getProtectionDomain().getCodeSource().getLocation()) != null
               && (codeSourceFile = new File(codeSourceUrl.toURI())).isFile()
               && containsValorSounds(codeSourceFile)) {
               return codeSourceFile;
            }
         } catch (Throwable ignored) {
         }
      }

      try {
         File[] tmpFiles;
         if ((tmpFiles = new File(System.getProperty("java.io.tmpdir")).listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jar") && (lower.contains("valor") || lower.startsWith("va"));
         })) != null) {
            File newestJar = null;
            long newestTime = -1L;

            for (File file : tmpFiles) {
               if (file.isFile() && file.lastModified() > newestTime && containsValorSounds(file)) {
                  newestJar = file;
                  newestTime = file.lastModified();
               }
            }

            if (newestJar != null) {
               return newestJar;
            }
         }
      } catch (Throwable ignored) {
      }

      return null;
   }

   private static boolean extractPack(File packDir) {
      try {
         File valorAssets = new File(packDir, "assets/valor");
         File toggleDir = new File(valorAssets, "sounds/toggle");
         if (!toggleDir.exists() && !toggleDir.mkdirs()) {
            LOGGER.warn("Could not create {}", new Object[]{toggleDir});
            return false;
         }

         String mcmeta = "{\n  \"pack\": {\n    \"pack_format\": 1,\n    \"description\": \"Valor Sounds\"\n  }\n}\n";
         Files.write(new File(packDir, "pack.mcmeta").toPath(), mcmeta.getBytes(StandardCharsets.UTF_8));
         Path soundsJsonPath = new File(valorAssets, "sounds.json").toPath();
         StringBuilder sb = new StringBuilder();
         sb.append("{\n");
         boolean first = true;

         for (String packName : SOUND_PACKS) {
            for (String state : TOGGLE_STATES) {
               if (!first) {
                  sb.append(",\n");
               }
               first = false;
               sb.append("  \"toggle.").append(packName).append(".").append(state).append("\": {\n");
               sb.append("    \"category\": \"player\",\n");
               sb.append("    \"sounds\": [\"valor:toggle/").append(packName).append("/").append(state).append("\"]\n");
               sb.append("  }");
            }
         }

         sb.append("\n}\n");
         Files.write(soundsJsonPath, sb.toString().getBytes(StandardCharsets.UTF_8));

         File agentJar = findAgentJar();
         if (agentJar == null) {
            LOGGER.warn("Could not locate agent JAR to extract sounds from");
            return false;
         }

         LOGGER.info("Extracting sounds from {}", new Object[]{agentJar.getAbsolutePath()});
         int extractedCount = 0;

         try (JarFile jar = new JarFile(agentJar)) {
            for (String packName : SOUND_PACKS) {
               File packSubDir = new File(toggleDir, packName);
               if (packSubDir.exists() || packSubDir.mkdirs()) {
                  for (String state : TOGGLE_STATES) {
                     File oggDest = new File(packSubDir, state + ".ogg");
                     if (!extractEntry(jar, "assets/valor/sounds/toggle/" + packName + "/" + state + ".ogg", oggDest)
                        && !extractEntry(jar, "assets/valor/sounds/" + packName + "/" + state + ".ogg", oggDest)
                        && !extractEntry(jar, "assets/valor/toggle/" + packName + "/" + state + ".ogg", oggDest)) {
                        LOGGER.warn("Missing sound in jar: {}/{}", new Object[]{packName, state});
                     } else {
                        extractedCount++;
                     }
                  }
               }
            }
         }

         if (extractedCount == 0) {
            LOGGER.warn("No toggle oggs extracted from agent JAR");
            return false;
         }

         LOGGER.info("Extracted {} toggle sound files", new Object[]{extractedCount});
         return true;
      } catch (Throwable t) {
         LOGGER.warn("extractPack failed", t);
         return false;
      }
   }

   public static synchronized void extractSounds() {
      if (!initialized && !failed) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc != null && mc.mcDataDir != null) {
            try {
               File packDir = new File(ValorPaths.VALOR_DIR, "resourcepack");
               if (!extractPack(packDir)) {
                  failed = true;
               } else {
                  List<IResourcePack> defaultPacks = getDefaultResourcePacks(mc);
                  if (defaultPacks == null) {
                     failed = true;
                     LOGGER.warn("Could not access Minecraft.defaultResourcePacks");
                  } else {
                     boolean alreadyAdded = false;
                     for (IResourcePack pack : defaultPacks) {
                        if (pack instanceof FolderResourcePack
                           && pack.getPackName() != null
                           && pack.getPackName().toLowerCase().contains("valor")
                           && pack.getPackName().toLowerCase().contains("resourcepack")) {
                           alreadyAdded = true;
                           break;
                        }
                     }

                     if (!alreadyAdded) {
                        defaultPacks.add(new FolderResourcePack(packDir));
                     }

                     mc.refreshResources();
                     initialized = true;
                     LOGGER.info("Valor sound pack ready ({})", new Object[]{packDir.getAbsolutePath()});
                  }
               }
            } catch (Throwable t) {
               failed = true;
               LOGGER.warn("Failed to install Valor sound pack", t);
            }
         }
      }
   }

   private static boolean extractEntry(JarFile jar, String entryName, File dest) {
      try {
         JarEntry entry = jar.getJarEntry(entryName);
         if (entry == null) {
            return false;
         }

         try (InputStream in = jar.getInputStream(entry);
              OutputStream out = Files.newOutputStream(dest.toPath())) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) >= 0) {
               out.write(buf, 0, read);
            }
         }

         return dest.isFile() && dest.length() > 0L;
      } catch (Throwable ignored) {
         return false;
      }
   }

   private static List<IResourcePack> getDefaultResourcePacks(Minecraft mc) {
      for (String fieldName : new String[]{"defaultResourcePacks", "field_110449_ao", "aA"}) {
         try {
            Field field = Minecraft.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object val = field.get(mc);
            if (val instanceof List) {
               return (List<IResourcePack>) val;
            }
         } catch (NoSuchFieldException ignored) {
         } catch (Throwable t) {
            LOGGER.warn("Reading {} failed: {}", new Object[]{fieldName, t.toString()});
         }
      }

      for (Field field : Minecraft.class.getDeclaredFields()) {
         if (List.class.isAssignableFrom(field.getType())) {
            List<IResourcePack> list;
            try {
               field.setAccessible(true);
               Object raw = field.get(mc);
               if (!(raw instanceof List)) {
                  continue;
               }
               list = (List<IResourcePack>) raw;
            } catch (Throwable ignored) {
               continue;
            }

            Iterator<?> it;
            try {
               it = list.iterator();
            } catch (Throwable ignored) {
               continue;
            }

            while (it.hasNext()) {
               try {
                  if (it.next() instanceof IResourcePack) {
                     return list;
                  }
               } catch (Throwable ignored) {
                  break;
               }
            }
         }
      }

      return null;
   }

   private static boolean containsValorSounds(File jarFile) {
      try (JarFile jar = new JarFile(jarFile)) {
         java.util.Enumeration<JarEntry> entries = jar.entries();
         while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (name.startsWith("assets/valor/sounds/") && name.endsWith(".ogg")) {
               return true;
            }
         }
      } catch (Throwable ignored) {
      }
      return false;
   }
}
