package valor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public final class ValorPaths {
   public static final File MINECRAFT_DIR;
   public static final File CONFIG_DIR;
   public static final File ALTS_DIR;
   public static final File VALOR_DIR;

   static {
      String appdata = System.getenv("APPDATA");
      MINECRAFT_DIR = (appdata != null && !appdata.trim().isEmpty())
         ? new File(appdata, ".minecraft").getAbsoluteFile()
         : new File(System.getProperty("user.home"), "AppData/Roaming/.minecraft").getAbsoluteFile();
      CONFIG_DIR = new File(MINECRAFT_DIR, "config/Valor");
      ALTS_DIR = new File(CONFIG_DIR, "alts");
      VALOR_DIR = new File(MINECRAFT_DIR, "valor");

      CONFIG_DIR.mkdirs();
      ALTS_DIR.mkdirs();
      VALOR_DIR.mkdirs();
      File legacyConfig = new File("./config/Valor").getAbsoluteFile();

      try {
         if (!legacyConfig.getCanonicalFile().equals(CONFIG_DIR.getCanonicalFile()) && legacyConfig.isDirectory()) {
            Path srcPath = legacyConfig.toPath();
            Path destPath = CONFIG_DIR.toPath();

            try (Stream<Path> stream = Files.walk(srcPath)) {
               stream.forEach(p -> {
                  try {
                     Path rel = srcPath.relativize(p);
                     Path dest = destPath.resolve(rel);
                     if (Files.isDirectory(p)) {
                        Files.createDirectories(dest);
                     } else if (!Files.exists(dest)) {
                        Files.createDirectories(dest.getParent());
                        Files.copy(p, dest, StandardCopyOption.COPY_ATTRIBUTES);
                     }
                  } catch (IOException ignored) {
                  }
               });
            }
         }
      } catch (Throwable ignored) {
      }
   }
}
