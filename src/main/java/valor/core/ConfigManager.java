package valor.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import valor.Valor;
import valor.module.Module;
import valor.module.render.Watermark;
import valor.setting.Property;
import valor.util.ChatUtil;
import valor.util.ReflectionUtil;
import valor.util.ValorPaths;

public final class ConfigManager {
   private static Minecraft mc = Minecraft.getMinecraft();
   private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   public static final File CONFIG_DIR = ValorPaths.CONFIG_DIR;
   private String configName;
   public File configFile;
   public static String currentConfig;

   public static String sanitizeConfigName(String name) {
      if (name == null) {
         return "default";
      } else if (!(name = name.trim()).isEmpty() && !name.equals("!") && !name.equalsIgnoreCase("default")) {
         StringBuilder sb = new StringBuilder(Math.min(name.length(), 64));

         for (int i = 0; i < name.length() && sb.length() < 64; i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != ' ' && c != '_' && c != '-') {
               sb.append('_');
            } else {
               sb.append(c);
            }
         }

         String sanitized = sb.toString().trim();
         if (sanitized.isEmpty()) {
            return "default";
         } else {
            if (sanitized.toUpperCase(Locale.ROOT).matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) {
               sanitized = "_".concat(String.valueOf(sanitized));
            }

            return sanitized;
         }
      } else {
         return "default";
      }
   }

   public static List<String> getConfigList() {
      File[] files = CONFIG_DIR.listFiles((dir, fileName) -> {
         String lower = fileName.toLowerCase(Locale.ROOT);
         return lower.endsWith(".json") && !lower.equals("cloud_meta.json") && !lower.equals("accounts.json");
      });

      if (files != null && files.length != 0) {
         ArrayList<String> list = new ArrayList<>(files.length);
         for (File f : files) {
            String fName = f.getName();
            int dotIdx = fName.lastIndexOf('.');
            String baseName = dotIdx > 0 ? fName.substring(0, dotIdx) : fName;
            if (sanitizeConfigName(baseName).equals(baseName)) {
               list.add(baseName);
            }
         }

         list.sort(String.CASE_INSENSITIVE_ORDER);
         return list;
      } else {
         return Collections.emptyList();
      }
   }

   public final void saveQuietly() {
      try {
         if (!Files.isSymbolicLink(this.configFile.toPath())) {
            if (this.configFile.getParentFile().exists() || this.configFile.getParentFile().mkdirs() || this.configFile.getParentFile().exists()) {
               JsonObject json = toJson();
               try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.configFile))) {
                  writer.write(GSON.toJson((JsonElement) json));
                  writer.newLine();
               }
            }
         }
      } catch (IOException e) {
         ReflectionUtil.getLogger(mc).error("Error saving config: " + e.getMessage());
      }
   }

   private static JsonObject toJson() {
      JsonObject root = new JsonObject();

      for (Module mod : Valor.moduleManager.modules.values()) {
         JsonObject modObj = new JsonObject();
         modObj.addProperty("toggled", mod.isEnabled());
         modObj.addProperty("key", mod.getKey());
         modObj.addProperty("hidden", mod.isHidden());
         ArrayList<Property<?>> properties = Valor.propertyManager.propertiesByModule.get(mod.getClass());
         if (properties != null) {
            for (Property<?> prop : properties) {
               try {
                  prop.writeJson(modObj);
               } catch (Exception e) {
                  ReflectionUtil.getLogger(mc).warn(String.format("Failed to save property %s for module %s", prop.getName(), mod.getName()));
               }
            }
         }

         root.add(mod.getName(), modObj);
      }

      JsonObject themeObj = new JsonObject();
      for (Property<?> prop : HudThemeManager.getInstance().getProperties()) {
         try {
            prop.writeJson(themeObj);
         } catch (Exception e) {
            ReflectionUtil.getLogger(mc).warn(String.format("Failed to save theme property %s", prop.getName()));
         }
      }

      themeObj.addProperty("hud-style-rev", 5);
      root.add("Theme", themeObj);
      return root;
   }

   private static void migratePropertyKey(JsonObject obj, String oldKey, String newKey) {
      if (obj.has(oldKey) && !obj.has(newKey)) {
         obj.add(newKey, obj.get(oldKey));
      }
   }

   private static void fromJson(JsonObject root) {
      for (Module mod : Valor.moduleManager.modules.values()) {
         JsonElement modElement = root.get(mod.getName());
         if ((modElement == null || !modElement.isJsonObject()) && "Theme".equals(mod.getName())) {
            modElement = root.get("HUD");
         }

         if (modElement != null && modElement.isJsonObject()) {
            JsonObject modObj = modElement.getAsJsonObject();
            if ("TimerRange".equals(mod.getName()) && modObj.has("mode")) {
               String mode = modObj.get("mode").getAsString().toLowerCase(Locale.ROOT);
               if (mode.equals("prediction")) {
                  mode = "modern";
                  modObj.addProperty("mode", "MODERN");
               }

               if (mode.equals("normal") || mode.equals("smart") || mode.equals("modern")) {
                  migratePropertyKey(modObj, "ticks", mode + "-ticks");
                  migratePropertyKey(modObj, "timer-boost", mode + "-boost");
                  migratePropertyKey(modObj, "timer-charged", mode + "-charged");
                  migratePropertyKey(modObj, "boost-delay-max", mode + "-boost-end");
                  migratePropertyKey(modObj, "charged-delay-max", mode + "-charge-end");
                  migratePropertyKey(modObj, "blink", mode + "-blink");
                  migratePropertyKey(modObj, "on-forward-only", mode + "-forward-only");
                  if (mode.equals("normal")) {
                     migratePropertyKey(modObj, "range", "normal-range");
                     migratePropertyKey(modObj, "cooldown-tick", "normal-cooldown");
                  } else {
                     migratePropertyKey(modObj, "min-range", mode + "-min-range");
                     migratePropertyKey(modObj, "max-range", mode + "-max-range");
                     migratePropertyKey(modObj, "min-tick-delay", mode + "-min-delay");
                     migratePropertyKey(modObj, "max-tick-delay", mode + "-max-delay");
                     migratePropertyKey(modObj, "predict-client-movement", mode + "-client-prediction");
                     migratePropertyKey(modObj, "predict-enemy-position", mode + "-enemy-prediction");
                  }

                  if (mode.equals("modern")) {
                     migratePropertyKey(modObj, "scan-range", "modern-scan-range");
                     migratePropertyKey(modObj, "max-angle-difference", "modern-max-angle");
                     migratePropertyKey(modObj, "mark", "modern-mark");
                  }
               }
            }

            if ("Velocity".equals(mod.getName())) {
               if (modObj.has("mode") && "TEST".equalsIgnoreCase(modObj.get("mode").getAsString())) {
                  modObj.addProperty("mode", "PREDICTION");
               }

               String[][] migrations = new String[][]{
                  {"test-horizontal", "prediction-horizontal"},
                  {"test-vertical", "prediction-vertical"},
                  {"test-reduce-motion", "prediction-reduce-motion"},
                  {"test-reduce-ticks", "prediction-reduce-ticks"},
                  {"test-reduce-mode", "prediction-reduce-mode"},
                  {"test-jump-reset", "prediction-jump-reset"},
                  {"test-delay", "prediction-delay"},
                  {"test-delay-ticks", "prediction-delay-ticks"},
                  {"test-delay-range", "prediction-delay-range"},
                  {"test-delay-until-ground", "prediction-delay-until-ground"},
                  {"test-reverse", "prediction-reverse"}
               };

               for (String[] pair : migrations) {
                  migratePropertyKey(modObj, pair[0], pair[1]);
               }
            }

            ArrayList<Property<?>> properties = Valor.propertyManager.propertiesByModule.get(mod.getClass());
            if (properties != null) {
               for (Property<?> prop : properties) {
                  if (modObj.has(prop.getName())) {
                     try {
                        if ("KillAura".equals(mod.getName()) && "auto-block".equals(prop.getName())) {
                           String val = modObj.get(prop.getName()).getAsString();
                           if ("COVER".equalsIgnoreCase(val) || "COVER_SWAP".equalsIgnoreCase(val)) {
                              modObj.addProperty(prop.getName(), "PREDICTION");
                           }
                        }

                        if ("Speed".equals(mod.getName()) && "mode".equals(prop.getName())) {
                           String val = modObj.get(prop.getName()).getAsString();
                           if ("PREDICT_TIMER".equalsIgnoreCase(val)
                              || "PREDICT_HOP".equalsIgnoreCase(val)
                              || "PREDICT_FLOW".equalsIgnoreCase(val)
                              || "PREDICTION".equalsIgnoreCase(val)
                              || "PREDICTION_FLOW".equalsIgnoreCase(val)) {
                              modObj.addProperty(prop.getName(), "CUSTOM");
                           }
                        }

                        prop.readJson(modObj);
                     } catch (Exception e) {
                        ReflectionUtil.getLogger(mc).warn(String.format("Failed to load property %s for module %s", prop.getName(), mod.getName()));
                     }
                  }
               }
            }

            JsonElement valEl;
            if (modObj.has("toggled") && (valEl = modObj.get("toggled")) != null && valEl.isJsonPrimitive()) {
               mod.setEnabled(valEl.getAsBoolean());
            }

            if (modObj.has("key") && (valEl = modObj.get("key")) != null && valEl.isJsonPrimitive()) {
               mod.setKey(valEl.getAsInt());
            }

            if (modObj.has("hidden") && (valEl = modObj.get("hidden")) != null && valEl.isJsonPrimitive()) {
               mod.setHidden(valEl.getAsBoolean());
            }
         }
      }

      JsonElement wmEl = root.get("Watermark");
      boolean hasShowPerf = wmEl != null && wmEl.isJsonObject() && wmEl.getAsJsonObject().has("show-performance");
      JsonElement infoHudEl = root.get("InfoHUD");
      Module wmMod;
      if (!hasShowPerf
         && infoHudEl != null
         && infoHudEl.isJsonObject()
         && infoHudEl.getAsJsonObject().has("toggled")
         && infoHudEl.getAsJsonObject().get("toggled").isJsonPrimitive()
         && (wmMod = Valor.moduleManager.getModule(Watermark.class)) instanceof Watermark) {
         ((Watermark) wmMod).showPerformanceProp.setValue(infoHudEl.getAsJsonObject().get("toggled").getAsBoolean());
      }

      JsonElement themeEl = root.get("Theme");
      if (themeEl == null || !themeEl.isJsonObject()) {
         themeEl = root.get("HUD");
      }

      if (themeEl != null && themeEl.isJsonObject()) {
         JsonObject themeObj = themeEl.getAsJsonObject();
         if ((!themeObj.has("hud-style-rev") || themeObj.get("hud-style-rev").getAsInt() < 5) && themeObj.has("hud-style")) {
            String style = themeObj.get("hud-style").getAsString();
            if ("SIMPLE".equalsIgnoreCase(style)) {
               themeObj.addProperty("hud-style", "VALOR");
            }
         }

         for (Property<?> prop : HudThemeManager.getInstance().getProperties()) {
            if (themeObj.has(prop.getName())) {
               try {
                  prop.readJson(themeObj);
               } catch (Exception e) {
                  ReflectionUtil.getLogger(mc).warn(String.format("Failed to load theme property %s", prop.getName()));
               }
            }
         }
      }
   }

   public final void save() {
      try {
         if (Files.isSymbolicLink(this.configFile.toPath())) {
            throw new IOException("Refusing to overwrite a symbolic link");
         } else if (!this.configFile.getParentFile().exists() && !this.configFile.getParentFile().mkdirs() && !this.configFile.getParentFile().exists()) {
            throw new IOException("Could not create config directory");
         } else {
            JsonObject root = toJson();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.configFile))) {
               writer.write(GSON.toJson((JsonElement) root));
               writer.newLine();
            }

            ChatUtil.addFormattedChatMessage(String.format("%sConfig has been saved (&a&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
         }
      } catch (IOException e) {
         ReflectionUtil.getLogger(mc).error("Error saving config: " + e.getMessage());
         ChatUtil.addFormattedChatMessage(String.format("%sConfig couldn't be saved (&c&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
      }
   }

   public static boolean deleteConfig(String name) {
      if (name != null && !name.trim().isEmpty()) {
         name = sanitizeConfigName(name);
         File file = new File(CONFIG_DIR, name + ".json");
         if (!file.exists()) {
            return false;
         } else {
            boolean deleted = file.delete();
            if (deleted) {
               ChatUtil.addFormattedChatMessage(String.format("%sConfig deleted (&a&o%s&r)&r", Valor.CHAT_PREFIX, file.getName()));
            } else {
               ChatUtil.addFormattedChatMessage(String.format("%sFailed to delete (&c&o%s&r)&r", Valor.CHAT_PREFIX, file.getName()));
            }

            return deleted;
         }
      } else {
         return false;
      }
   }

   public final void load() {
      try {
         if (!this.configFile.exists()) {
            ChatUtil.addFormattedChatMessage(String.format("%sConfig file not found (&c&o%s&r). Creating default config...&r", Valor.CHAT_PREFIX, this.configFile.getName()));
            this.save();
         } else if (!Files.isSymbolicLink(this.configFile.toPath()) && this.configFile.length() <= 2097152L) {
            JsonElement parsed;
            try (BufferedReader reader = new BufferedReader(new FileReader(this.configFile))) {
               parsed = new JsonParser().parse(reader);
            }

            if (parsed != null && parsed.isJsonObject()) {
               fromJson(parsed.getAsJsonObject());
               ChatUtil.addFormattedChatMessage(String.format("%sConfig has been loaded (&a&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
            } else {
               ChatUtil.addFormattedChatMessage(String.format("%sInvalid config format (&c&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
            }
         } else {
            ChatUtil.addFormattedChatMessage(String.format("%sConfig was rejected (&c&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
         }
      } catch (FileNotFoundException e) {
         ChatUtil.addFormattedChatMessage(String.format("%sConfig file not found (&c&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
      } catch (JsonSyntaxException e) {
         ChatUtil.addFormattedChatMessage(String.format("%sConfig has invalid JSON syntax (&c&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
         ReflectionUtil.getLogger(mc).error("JSON Syntax Error: " + e.getMessage());
      } catch (Exception e) {
         ReflectionUtil.getLogger(mc).error("Error loading config: " + e.getMessage());
         ChatUtil.addFormattedChatMessage(String.format("%sConfig couldn't be loaded (&c&o%s&r)&r", Valor.CHAT_PREFIX, this.configFile.getName()));
      }
   }

   public ConfigManager(String name, boolean notifyOnCreate) {
      this.configName = sanitizeConfigName(name);
      currentConfig = this.configName;
      this.configFile = new File(CONFIG_DIR, String.format("%s.json", this.configName));

      try {
         this.configFile.getParentFile().mkdirs();
         Logger logger;
         if (notifyOnCreate && (logger = ReflectionUtil.getLogger(mc)) != null) {
            logger.info(String.format("Created: %s", this.configFile.getName()));
         }
      } catch (Exception e) {
         Logger logger;
         if ((logger = ReflectionUtil.getLogger(mc)) != null) {
            logger.error(e.getMessage());
         }
      }
   }
}
