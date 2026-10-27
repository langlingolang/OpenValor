package valor;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import valor.auth.AccountStore;
import valor.command.CommandManager;
import valor.command.impl.AccountCommand;
import valor.command.impl.BindCommand;
import valor.command.impl.ConfigCommand;
import valor.command.impl.DenickCommand;
import valor.command.impl.FriendCommand;
import valor.command.impl.HelpCommand;
import valor.command.impl.HideCommand;
import valor.command.impl.ItemCommand;
import valor.command.impl.NameCommand;
import valor.command.impl.PlayerListCommand;
import valor.command.impl.PropertyCommand;
import valor.command.impl.ShowCommand;
import valor.command.impl.TargetCommand;
import valor.command.impl.ToggleCommand;
import valor.command.impl.VClipCommand;
import valor.core.BlinkManager;
import valor.core.ConfigManager;
import valor.core.FriendManager;
import valor.core.HudThemeManager;
import valor.core.LagManager;
import valor.core.NoSlowManager;
import valor.core.PacketBlinkManager;
import valor.core.PlayerStateTracker;
import valor.core.PropertyManager;
import valor.core.RotationManager;
import valor.core.TargetManager;
import valor.event.EventManager;
import valor.internal.RuntimeVerificationService;
import valor.module.Module;
import valor.module.ModuleManager;
import valor.module.combat.AimAssist;
import valor.module.player.AntiDebuff;
import valor.module.combat.AutoClicker;
import valor.module.player.AutoHeal;
import valor.module.combat.BackTrack;
import valor.module.combat.BlockHit;
import valor.module.combat.HitBox;
import valor.module.combat.HitSelect;
import valor.module.combat.KillAura;
import valor.module.combat.KnockbackDisplace;
import valor.module.combat.LagRange;
import valor.module.combat.NoHitDelay;
import valor.module.combat.Reach;
import valor.module.combat.TimerRange;
import valor.module.combat.Velocity;
import valor.module.combat.WTap;
import valor.module.world.Disabler;
import valor.module.movement.AntiVoid;
import valor.module.movement.Blink;
import valor.module.movement.Eagle;
import valor.module.movement.Fly;
import valor.module.player.InvWalk;
import valor.module.movement.Jesus;
import valor.module.movement.KeepSprint;
import valor.module.movement.LongJump;
import valor.module.movement.NoFall;
import valor.module.movement.NoSlow;
import valor.module.movement.SafeWalk;
import valor.module.world.Scaffold;
import valor.module.movement.Speed;
import valor.module.movement.Sprint;
import valor.module.combat.TargetStrafe;
import valor.module.render.AltManagerMod;
import valor.module.render.SessionStats;
import valor.module.world.StaffDetector;
import valor.module.world.AutoBedDef;
import valor.module.world.AutoBlockIn;
import valor.module.player.AutoTool;
import valor.module.world.BedNuker;
import valor.module.player.ChestStealer;
import valor.module.player.Clutch;
import valor.module.player.FastPlace;
import valor.module.player.GhostHand;
import valor.module.player.InvManager;
import valor.module.player.InventoryClicker;
import valor.module.movement.NoJumpDelay;
import valor.module.combat.Refill;
import valor.module.world.SpeedMine;
import valor.module.render.ArrayListMod;
import valor.module.render.BedESP;
import valor.module.render.BedPlates;
import valor.module.render.BreakProgress;
import valor.module.render.Chams;
import valor.module.render.ChestESP;
import valor.module.render.ClickGuiMod;
import valor.module.render.ESP;
import valor.module.render.FreeLook;
import valor.module.render.Freecam;
import valor.module.render.Fullbright;
import valor.module.render.Indicators;
import valor.module.render.ItemESP;
import valor.module.render.NameTags;
import valor.module.render.NickHider;
import valor.module.render.NoHurtCam;
import valor.module.render.Notifications;
import valor.module.render.Radar;
import valor.module.render.Scoreboard;
import valor.module.render.TNTTimer;
import valor.module.render.TargetHUD;
import valor.module.render.Tracers;
import valor.module.render.Trajectories;
import valor.module.render.Watermark;
import valor.module.render.Theme;
import valor.setting.Property;
import valor.util.AssetExtractor;
import valor.util.font.Fonts;
public class Valor {
   public static String CHAT_PREFIX = "&7[&5Valor&7]&r ";
   public static String version;
   public static boolean isSrg = "srg".equals(System.getProperty("valor.runtime.profile"));
   public static boolean isBadlion = "badlion".equals(System.getProperty("valor.runtime.launcher"));
   public static RotationManager rotationManager;
   public static NoSlowManager noSlowManager;
   public static BlinkManager blinkManager;
   public static LagManager lagManager;
   public static PacketBlinkManager packetBlinkManager;
   public static PlayerStateTracker stateTracker;
   public static FriendManager friendManager;
   public static TargetManager targetManager;
   public static PropertyManager propertyManager;
   public static ModuleManager moduleManager;
   public static CommandManager commandManager;
   private static boolean initialized = false;
   public Valor() {
      this.init();
   }
   public void init() {
      if (!initialized) {
         initialized = true;
         if (!RuntimeVerificationService.verifyRuntime().isSuccess()) {
            initialized = false;
         } else {
            rotationManager = new RotationManager();
            noSlowManager = new NoSlowManager();
            blinkManager = new BlinkManager();
            lagManager = new LagManager();
            packetBlinkManager = new PacketBlinkManager();
            stateTracker = new PlayerStateTracker();
            friendManager = new FriendManager();
            targetManager = new TargetManager();
            propertyManager = new PropertyManager();
            moduleManager = new ModuleManager();
            commandManager = new CommandManager();
            commandManager.commands.add(new ConfigCommand());
            commandManager.commands.add(new FriendCommand());
            commandManager.commands.add(new TargetCommand());
            commandManager.commands.add(new HelpCommand());
            commandManager.commands.add(new PropertyCommand());
            commandManager.commands.add(new ToggleCommand());
            commandManager.commands.add(new BindCommand());
            commandManager.commands.add(new HideCommand());
            commandManager.commands.add(new ShowCommand());
            commandManager.commands.add(new PlayerListCommand());
            commandManager.commands.add(new NameCommand());
            commandManager.commands.add(new DenickCommand());
            commandManager.commands.add(new VClipCommand());
            commandManager.commands.add(new ItemCommand());
            commandManager.commands.add(new AccountCommand());
            EventManager.register(rotationManager);
            EventManager.register(noSlowManager);
            EventManager.register(blinkManager);
            EventManager.register(lagManager);
            EventManager.register(packetBlinkManager);
            EventManager.register(moduleManager);
            EventManager.register(commandManager);
            Class<?>[] moduleClasses = new Class<?>[]{
               AimAssist.class,
               AntiDebuff.class,
               AntiVoid.class,
               AutoClicker.class,
               AutoHeal.class,
               AutoTool.class,
               BedNuker.class,
               BedESP.class,
               BackTrack.class,
               Blink.class,
               Chams.class,
               ChestESP.class,
               ChestStealer.class,
               Disabler.class,
               Eagle.class,
               ESP.class,
               FastPlace.class,
               Fullbright.class,
               GhostHand.class,
               ClickGuiMod.class,
               HitSelect.class,
               Watermark.class,
               Notifications.class,
               SessionStats.class,
               Scoreboard.class,
               Indicators.class,
               AltManagerMod.class,
               InventoryClicker.class,
               InvManager.class,
               InvWalk.class,
               ItemESP.class,
               Jesus.class,
               KeepSprint.class,
               HitBox.class,
               KillAura.class,
               Fly.class,
               LagRange.class,
               LongJump.class,
               NameTags.class,
               NickHider.class,
               NoFall.class,
               NoHitDelay.class,
               NoHurtCam.class,
               NoJumpDelay.class,
               NoSlow.class,
               Radar.class,
               Reach.class,
               Refill.class,
               SafeWalk.class,
               Scaffold.class,
               AutoBlockIn.class,
               BlockHit.class,
               Speed.class,
               SpeedMine.class,
               StaffDetector.class,
               Sprint.class,
               TargetHUD.class,
               TargetStrafe.class,
               Tracers.class,
               Trajectories.class,
               Velocity.class,
               WTap.class,
               KnockbackDisplace.class,
               AutoBedDef.class,
               BreakProgress.class,
               TimerRange.class,
               Clutch.class,
               Freecam.class,
               TNTTimer.class,
               FreeLook.class,
               BedPlates.class,
               ArrayListMod.class,

               Theme.class

            };
            for (Class<?> modClass : moduleClasses) {
               try {
                  Module instance = (Module) modClass.getDeclaredConstructor().newInstance();
                  moduleManager.modules.put(modClass, instance);
               } catch (Throwable t) {
                  System.out.println("[Valor]   FAILED to register " + modClass.getSimpleName() + ": " + t);
               }
            }
            for (Module mod : moduleManager.modules.values()) {
               ArrayList<Property<?>> properties = new ArrayList<>();
               Field[] fields = mod.getClass().getDeclaredFields();
               for (Field field : fields) {
                  field.setAccessible(true);
                  Object val;
                  try {
                     val = field.get(mod);
                  } catch (IllegalAccessException e) {
                     throw new RuntimeException(e);
                  }
                  if (val instanceof Property && !properties.contains(val)) {
                     Property<?> prop = (Property<?>) val;
                     prop.setParentModule(mod);
                     properties.add(prop);
                  }
               }
               propertyManager.propertiesByModule.put(mod.getClass(), properties);
               EventManager.register(mod);
            }
            EventManager.register(HudThemeManager.getInstance());
            ConfigManager defaultConfig = new ConfigManager("default", true);
            defaultConfig.load();
            if (friendManager.file.exists()) {
               friendManager.load();
            }
            if (targetManager.file.exists()) {
               targetManager.load();
            }
            try {
               AccountStore.load();
            } catch (Throwable t) {
               System.out.println("[Valor] AccountStore.load failed: " + t);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
               new ConfigManager(ConfigManager.currentConfig != null ? ConfigManager.currentConfig : "default", false).saveQuietly();
               try {
                  AccountStore.save();
               } catch (Throwable ignored) {
               }
            }));
            try {
               InputStream in = Valor.class.getResourceAsStream("/version.json");
               if (in != null) {
                  try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                     version = new JsonParser().parse(reader).getAsJsonObject().get("version").getAsString();
                    }
               } else {
                  version = "dev";
               }
            } catch (Exception ignored) {
               version = "dev";
            }
            try {
               Fonts.init();
            } catch (Throwable t) {
               System.out.println("[Valor] Font init failed: " + t.getMessage());
            }
            AssetExtractor.extractSounds();
            System.out.println("[Valor] Initialization complete");
         }
      }
   }
}
