package valor.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import valor.util.ValorPaths;

public final class AccountStore {
   public static Path ALTS_PATH = ValorPaths.ALTS_DIR.toPath();
   public static Path ACCOUNTS_FILE = ALTS_PATH.resolve("accounts.json");
   public static Path ACTIVE_ALT_FILE = ALTS_PATH.resolve("active_alt.txt");
   private static final Path LEGACY_ACCOUNTS_FILE = ValorPaths.CONFIG_DIR.toPath().resolve("accounts.json");
   private static final Path LEGACY_ACTIVE_ALT_FILE = ValorPaths.CONFIG_DIR.toPath().resolve("active_alt.txt");
   private static final byte[] ENCRYPTION_KEY = "valor-alt-manager/v1".getBytes(StandardCharsets.UTF_8);

   private static List<AltAccount> accounts = new ArrayList<>();
   private static AltAccount activeAccount;

   public static synchronized void removeAccount(int index) {
      if (index >= 0 && index < accounts.size()) {
         AltAccount removed = accounts.remove(index);
         if (activeAccount != null && removed != null && activeAccount.username.equalsIgnoreCase(removed.username)) {
            activeAccount = null;
            saveActiveAlt();
         }

         save();
      }
   }

   public static synchronized void setActiveAccount(AltAccount account) {
      activeAccount = account;
      saveActiveAlt();
   }

   public static synchronized void save() {
      try {
         if (!Files.exists(ALTS_PATH)) {
            Files.createDirectories(ALTS_PATH);
         }

         byte[] encrypted = cipher(AltAccount.toJson(accounts).getBytes(StandardCharsets.UTF_8));
         Files.write(ACCOUNTS_FILE, encrypted);
      } catch (Exception e) {
         System.out.println("[Valor] AccountStore.save failed: " + e);
      }
   }

   public static synchronized List<AltAccount> load() {
      try {
         if (!Files.exists(ACCOUNTS_FILE) && (Files.exists(LEGACY_ACCOUNTS_FILE) || Files.exists(LEGACY_ACTIVE_ALT_FILE))) {
            if (!Files.exists(ALTS_PATH)) {
               Files.createDirectories(ALTS_PATH);
            }

            if (Files.exists(LEGACY_ACCOUNTS_FILE)) {
               byte[] data = Files.readAllBytes(LEGACY_ACCOUNTS_FILE);
               Files.write(ACCOUNTS_FILE, data);
               Files.deleteIfExists(LEGACY_ACCOUNTS_FILE);
            }

            if (Files.exists(LEGACY_ACTIVE_ALT_FILE)) {
               byte[] data = Files.readAllBytes(LEGACY_ACTIVE_ALT_FILE);
               Files.write(ACTIVE_ALT_FILE, data);
               Files.deleteIfExists(LEGACY_ACTIVE_ALT_FILE);
            }
         }
      } catch (Throwable t) {
         System.out.println("[Valor] AccountStore migration failed: " + t);
      }

      if (!Files.exists(ACCOUNTS_FILE)) {
         accounts = new ArrayList<>();
         return accounts;
      } else {
         try {
            byte[] decrypted = cipher(Files.readAllBytes(ACCOUNTS_FILE));
            accounts = AltAccount.fromJson(new String(decrypted, StandardCharsets.UTF_8));
         } catch (Exception e) {
            System.out.println("[Valor] AccountStore.load failed: " + e);
            accounts = new ArrayList<>();
         }

         if (Files.exists(ACTIVE_ALT_FILE)) {
            try {
               List<String> lines = Files.readAllLines(ACTIVE_ALT_FILE, StandardCharsets.UTF_8);
               if (!lines.isEmpty()) {
                  String targetUsername = lines.get(0).trim();
                  for (AltAccount alt : accounts) {
                     if (alt.username.equalsIgnoreCase(targetUsername)) {
                        activeAccount = alt;
                        break;
                     }
                  }
               }
            } catch (Exception ignored) {
            }
         }

         return accounts;
      }
   }

   public static byte[] cipher(byte[] data) {
      byte[] result = new byte[data.length];

      for (int i = 0; i < data.length; i++) {
         int mask = ENCRYPTION_KEY[i % ENCRYPTION_KEY.length] & 255 ^ data.length + i & 0xFF;
         result[i] = (byte) (data[i] & 255 ^ mask);
      }

      return result;
   }

   public static synchronized AltAccount getActiveAccount() {
      return activeAccount;
   }

   public static synchronized AltAccount getAccount(int index) {
      return index >= 0 && index < accounts.size() ? accounts.get(index) : null;
   }

   public static synchronized void addOrUpdateAccount(AltAccount account) {
      for (int i = 0; i < accounts.size(); i++) {
         if (accounts.get(i).username.equalsIgnoreCase(account.username)) {
            accounts.set(i, account);
            save();
            return;
         }
      }

      accounts.add(account);
      save();
   }

   public static synchronized List<AltAccount> getAccounts() {
      return accounts;
   }

   public static void saveActiveAlt() {
      try {
         if (!Files.exists(ALTS_PATH)) {
            Files.createDirectories(ALTS_PATH);
         }

         String name = activeAccount != null ? activeAccount.username : "";
         Files.write(ACTIVE_ALT_FILE, name.getBytes(StandardCharsets.UTF_8));
      } catch (IOException ignored) {
      }
   }
}
