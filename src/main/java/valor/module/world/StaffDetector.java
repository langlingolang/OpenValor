package valor.module.world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LoadWorldEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ModeProperty;
import valor.util.ChatUtil;
import valor.util.NotificationManager;
import valor.util.ResourceUtil;
import valor.util.SoundUtil;

public class StaffDetector extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final String[] STAFF_SERVERS = new String[]{
      "Hypixel",
      "BlocksMC",
      "Gamster",
      "GommeHD",
      "Pika",
      "Syuu",
      "Stardix",
      "MinemenClub",
      "MushMC",
      "Twerion",
      "BedwarsPractice",
      "QuickMacro",
      "Heypixel",
      "HylexMC",
      "Jartex",
      "Mineland"
   };
   public static final String STAFF_LIST_PATH = "/assets/valor/stafflists/";
   public static final List<Set<String>> SERVER_STAFF_SETS = new ArrayList<>();
   public static final Set<String> DETECTED_STAFF = Collections.synchronizedSet(new HashSet<>());
   private static boolean listsLoaded = false;

   public final ModeProperty stafflistProp = new ModeProperty("stafflist", 0, STAFF_SERVERS);
   public final ModeProperty detectionProp = new ModeProperty("detection", 2, new String[]{"TABLIST", "WORLD", "BOTH"});
   public final ModeProperty messageModeProp = new ModeProperty("message-mode", 0, new String[]{"CHAT", "NONE"});
   public final BooleanProperty autoLobbyProp = new BooleanProperty("auto-lobby", Boolean.FALSE);
   public final BooleanProperty alarmProp = new BooleanProperty("alarm", Boolean.FALSE);

   @Override
   public String getDescription() {
      return "Alerts when known staff appear in tab list or world.";
   }

   @Override
   public String[] getSuffix() {
      return new String[]{this.stafflistProp.getModeName()};
   }

   public void scanWorld(Set<String> staffSet) {
      if (mc.theWorld != null && mc.theWorld.playerEntities != null) {
         for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player != null) {
               String name = cleanName(player.getName());
               if (!name.isEmpty() && !DETECTED_STAFF.contains(name) && staffSet.contains(name)) {
                  this.notifyStaffDetected(name);
               }
            }
         }
      }
   }

   public static String cleanName(String name) {
      return name == null ? "" : name.replaceAll("\u00a7[0-9a-fk-or]", "").trim().toLowerCase();
   }

   public static synchronized void loadStaffLists() {
      if (!listsLoaded) {
         for (String serverName : STAFF_SERVERS) {
            HashSet<String> staffSet = new HashSet<>();
            String resourcePath = STAFF_LIST_PATH + serverName + ".txt";

            try (InputStream stream = ResourceUtil.getResourceStream(resourcePath)) {
               if (stream != null) {
                  try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                     String line;
                     while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                           staffSet.add(line.toLowerCase());
                        }
                     }
                  }
               } else {
                  System.out.println("[Valor] StaffDetector: missing staff list " + resourcePath);
               }
            } catch (Exception ex) {
               System.out.println("[Valor] StaffDetector: failed to load " + serverName + ": " + ex.getMessage());
            }

            SERVER_STAFF_SETS.add(staffSet);
         }

         listsLoaded = true;

      }
   }

   public void notifyStaffDetected(String staffName) {
      DETECTED_STAFF.add(staffName);
      NotificationManager.warn("Staff Detected", staffName);
      if ((Integer)this.messageModeProp.getValue() == 0) {
         ChatUtil.addFormattedChatMessage(Valor.CHAT_PREFIX + "&c&lStaff Detected: &r&f" + staffName);
      }

      if ((Boolean)this.autoLobbyProp.getValue()) {
         ChatUtil.sendMessageToServer("/lobby");
         if ((Integer)this.messageModeProp.getValue() == 0) {
            ChatUtil.addFormattedChatMessage(Valor.CHAT_PREFIX + "&7Returning to lobby...");
         }

         NotificationManager.warn("Staff Detected", "Returning to lobby...");
      }

      if ((Boolean)this.alarmProp.getValue()) {
         SoundUtil.playSound("note.pling");
      }
   }

   public void scanTabList(Set<String> staffSet) {
      if (mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfoMap() != null) {
         for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            if (info != null && info.getGameProfile() != null) {
               String name = cleanName(info.getGameProfile().getName());
               if (!name.isEmpty() && !DETECTED_STAFF.contains(name) && staffSet.contains(name)) {
                  this.notifyStaffDetected(name);
               }
            }
         }
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (mc.theWorld != null && mc.thePlayer != null && mc.getNetHandler() != null) {
            if (!SERVER_STAFF_SETS.isEmpty()) {
               Set<String> staffSet = SERVER_STAFF_SETS.get((Integer)this.stafflistProp.getValue());
               switch (this.detectionProp.getValue()) {
                  case 0:
                     this.scanTabList(staffSet);
                     return;
                  case 1:
                     this.scanWorld(staffSet);
                     return;
                  case 2:
                     this.scanTabList(staffSet);
                     this.scanWorld(staffSet);
               }
            }
         }
      }
   }

   @EventTarget
   public void onWorldLoad(LoadWorldEvent var1) {
      DETECTED_STAFF.clear();
   }

   public StaffDetector() {
      super("StaffDetector", false, ModuleCategory.WORLD);
      loadStaffLists();
   }
}
