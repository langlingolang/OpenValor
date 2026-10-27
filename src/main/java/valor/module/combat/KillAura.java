package valor.module.combat;

import valor.module.player.AutoHeal;

import com.google.common.base.CaseFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings.GameType;
import valor.Valor;
import valor.core.BlinkType;
import valor.core.HudThemeManager;
import valor.core.MovementManager;
import valor.event.EventManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.AttackEvent;
import valor.events.CancelUseEvent;
import valor.events.HitBlockEvent;
import valor.events.LeftClickMouseEvent;
import valor.events.MoveInputEvent;
import valor.events.PacketEvent;
import valor.events.Render3DEvent;
import valor.events.RightClickMouseEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.movement.NoSlow;
import valor.module.world.Scaffold;
import valor.module.world.AutoBlockIn;
import valor.module.world.BedNuker;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.BotCheckUtil;
import valor.util.ItemUtil;
import valor.util.KeyUtil;
import valor.util.MoveUtil;
import valor.util.PacketUtil;
import valor.util.PlayerUtil;
import valor.util.RandomUtil;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RotationUtil;
import valor.util.TimerUtil;

public class KillAura extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
private final TimerUtil attackTimer = new TimerUtil();
   private KillAura.TargetInfo currentTarget = null;
   private int switchIndex = 0;
   private boolean shouldSwitch = false;
   private boolean clientSideBlocking = false;
   private boolean isAutoBlocking = false;
   private boolean autoBlockTargetFound = false;
   public boolean isRenderBlocking = false;
   private boolean unblockPending = false;
   private long attackDelayMs = 0L;
   private long lastAttackTimeMs = 0L;
   private int autoBlockStage = 0;
   private int blockPacketState = 0;
   private boolean wasBlocking = false;
   private long nextUnblockTimeMs = 0L;
   private boolean isInteracting = false;
   private boolean blockReleasePending = false;
   private float aimOffsetX = 0.5F;
   private float aimOffsetY = 0.72F;
   private float aimOffsetZ = 0.5F;
   private float targetAimOffsetX = 0.5F;
   private float targetAimOffsetY = 0.7F;
   private float targetAimOffsetZ = 0.5F;
   private long nextAimRandomizeTimeMs = 0L;
   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"SINGLE", "SWITCH"});
   public final ModeProperty sortProp = new ModeProperty("sort", 0, new String[]{"DISTANCE", "HEALTH", "HURT_TIME", "FOV"});
   public final ModeProperty autoBlockProp = new ModeProperty(
      "auto-block",
      6,
      new String[]{
         "NONE",
         "VANILLA",
         "SPOOF",
         "HYPIXEL",
         "SWAP",
         "FAKE",
         "GRIM",
         "VULCAN",
         "HYPIXEL2",
         "HYPIXEL3",
         "TICK",
         "INTERACT",
         "PREDICT",
         "PREDICT2",
         "PREDICT3",
         "PREDICTION"
      }
   );
   public final BooleanProperty autoBlockRequirePressProp = new BooleanProperty("auto-block-require-press", Boolean.FALSE);
   public final FloatProperty autoBlockMinApsProp = new FloatProperty("auto-block-min-aps", 8.0F, 1.0F, 20.0F);
   public final FloatProperty autoBlockMaxApsProp = new FloatProperty("auto-block-max-aps", 10.0F, 1.0F, 20.0F);
   public final FloatProperty autoBlockRangeProp = new FloatProperty("auto-block-range", 6.0F, 3.0F, 8.0F);
   public final BooleanProperty smartAutoBlockProp = new BooleanProperty("smart-auto-block", Boolean.FALSE, () -> (Integer)this.autoBlockProp.getValue() != 0 && !this.isAdvancedAutoBlockMode());
   public final BooleanProperty forceBlockWhenStillProp = new BooleanProperty("force-block-when-still", Boolean.TRUE, this.smartAutoBlockProp::getValue);
   public final BooleanProperty checkEnemyWeaponProp = new BooleanProperty("check-enemy-weapon", Boolean.TRUE, this::isSmartOrAdvancedAutoBlock);
   public final FloatProperty smartBlockRangeProp = new FloatProperty("smart-block-range", 3.0F, 0.0F, 8.0F, this::isSmartOrAdvancedAutoBlock);
   public final IntProperty maxOwnHurtTimeProp = new IntProperty("max-own-hurt-time", 3, 0, 10, this.smartAutoBlockProp::getValue);
   public final FloatProperty maxDirectionDiffProp = new FloatProperty("max-direction-diff", 60.0F, 30.0F, 180.0F, this::isSmartOrAdvancedAutoBlock);
   public final IntProperty maxSwingProgressProp = new IntProperty("max-swing-progress", 1, 0, 5, this::isSmartOrAdvancedAutoBlock);
   public final ModeProperty unblockModeProp = new ModeProperty(
      "unblock-mode",
      0,
      new String[]{"STOP", "SWITCH", "EMPTY"},
      () -> (Integer)this.autoBlockProp.getValue() != 0 && (Integer)this.autoBlockProp.getValue() != 5 && !this.isVanillaAutoBlock()
   );
   public final PercentageProperty blockRateProp = new PercentageProperty(
      "block-rate", 100, () -> (Integer)this.autoBlockProp.getValue() != 0 && (Integer)this.autoBlockProp.getValue() != 5 && !this.isVanillaAutoBlock()
   );
   public final BooleanProperty prioritizeBlockingProp = new BooleanProperty(
      "prioritize-blocking", Boolean.FALSE, () -> (Integer)this.autoBlockProp.getValue() != 0 && (Integer)this.autoBlockProp.getValue() != 5
   );
   public final FloatProperty swingRangeProp = new FloatProperty("swing-range", 3.5F, 3.0F, 6.0F);
   public final FloatProperty attackRangeProp = new FloatProperty("attack-range", 3.0F, 3.0F, 6.0F);
   public final IntProperty fovProp = new IntProperty("fov", 360, 30, 360);
   public final FloatProperty minApsProp = new FloatProperty("min-aps", 8.0F, 1.0F, 20.0F);
   public final FloatProperty maxApsProp = new FloatProperty("max-aps", 20.0F, 1.0F, 20.0F);
   public final IntProperty switchDelayProp = new IntProperty("switch-delay", 150, 0, 1000);
   public final ModeProperty rotationsProp = new ModeProperty("rotations", 2, new String[]{"NONE", "LEGIT", "SILENT", "LOCK_VIEW"});
   public final ModeProperty aimModeProp = new ModeProperty("aim-mode", 0, new String[]{"RANDOM", "TORSO", "SMOOTH_TRACK", "BOX_EDGE"});
   public final ModeProperty moveFixProp = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
   public final PercentageProperty smoothingProp = new PercentageProperty("smoothing", 0);
   public final IntProperty angleStepProp = new IntProperty("angle-step", 90, 30, 180);
   public final BooleanProperty throughWallsProp = new BooleanProperty("through-walls", Boolean.TRUE);
   public final BooleanProperty requirePressProp = new BooleanProperty("require-press", Boolean.FALSE);
   public final BooleanProperty allowMiningProp = new BooleanProperty("allow-mining", Boolean.TRUE);
   public final BooleanProperty weaponsOnlyProp = new BooleanProperty("weapons-only", Boolean.TRUE);
   public final BooleanProperty allowToolsProp = new BooleanProperty("allow-tools", Boolean.FALSE, this.weaponsOnlyProp::getValue);
   public final BooleanProperty inventoryCheckProp = new BooleanProperty("inventory-check", Boolean.TRUE);
   public final BooleanProperty botCheckProp = new BooleanProperty("bot-check", Boolean.TRUE);
   public final BooleanProperty playersProp = new BooleanProperty("players", Boolean.TRUE);
   public final BooleanProperty bossesProp = new BooleanProperty("bosses", Boolean.FALSE);
   public final BooleanProperty mobsProp = new BooleanProperty("mobs", Boolean.FALSE);
   public final BooleanProperty animalsProp = new BooleanProperty("animals", Boolean.FALSE);
   public final BooleanProperty golemsProp = new BooleanProperty("golems", Boolean.FALSE);
   public final BooleanProperty silverfishProp = new BooleanProperty("silverfish", Boolean.FALSE);
   public final BooleanProperty teamsProp = new BooleanProperty("teams", Boolean.TRUE);
   public final ModeProperty showTargetProp = new ModeProperty("show-target", 0, new String[]{"NONE", "DEFAULT", "THEME"});

@Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget(3)
   public void onUpdate(UpdateEvent var1) {
      if (var1.getType() == EventType.POST && this.unblockPending) {
         this.unblockPending = false;
         Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
         Valor.blinkManager.setBlinking(true, BlinkType.SCAFFOLD);
      }

      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (this.isVanillaAutoBlock() && !ItemUtil.isHoldingSword()) {
            Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
            this.clientSideBlocking = false;
            this.isAutoBlocking = false;
            this.isRenderBlocking = false;
            this.autoBlockStage = 0;
            this.blockPacketState = 0;
            this.blockReleasePending = false;
            this.wasBlocking = false;
            this.nextUnblockTimeMs = 0L;
         }

         if (this.currentTarget != null && BotCheckUtil.isEntityLoaded((Entity)this.currentTarget.entity)) {
            this.currentTarget = new KillAura.TargetInfo(this.currentTarget.entity);
         }

         long var2 = System.currentTimeMillis();
         long var4 = this.lastAttackTimeMs == 0L ? 0L : Math.max(0L, Math.min(250L, var2 - this.lastAttackTimeMs));
         this.lastAttackTimeMs = var2;
         if (this.attackDelayMs > 0L) {
            this.attackDelayMs -= var4;
         }

         BedNuker var58;
         boolean var59 = (var58 = (BedNuker)Valor.moduleManager.modules.get(BedNuker.class)).isEnabled() && var58.isReady() && !this.canAttackTarget();
         boolean var3 = this.currentTarget != null && this.canAuraRotateAndAttack();
         boolean var60 = this.isVanillaAutoBlock()
            && !var59
            && this.currentTarget != null
            && this.isWithinAutoBlockRange(this.currentTarget.entity)
            && this.isValidTarget(this.currentTarget.entity)
            && this.canAutoBlock();
         boolean var61;
         if (!(var61 = var3 && this.canAutoBlock() || var60)) {
            int var68;
            if (((var68 = (Integer)this.autoBlockProp.getValue()) == 6 || var68 == 8 || var68 == 10 || var68 == 11 || this.isAdvancedAutoBlockMode()) && this.isBlockingSword()) {
               this.unblock(false);
            }

            if (var68 == 3 && this.autoBlockTargetFound) {
               this.releaseUseItemKey();
            }

            Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
            this.isAutoBlocking = false;
            this.isRenderBlocking = false;
            this.autoBlockStage = 0;
            this.blockPacketState = 0;
         }

         if (var3 || var61) {
            boolean var69 = false;
            boolean var5 = false;
            boolean var6 = this.isVanillaAutoBlock() && this.blockPacketState == 2;
            if (var61) {
               switch (this.autoBlockProp.getValue()) {
                  case 0:
                     if (PlayerUtil.isUseItemPressed()) {
                        this.isAutoBlocking = true;
                        if (!this.isBlockingSword() && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                           var69 = true;
                        }
                     } else {
                        this.isAutoBlocking = false;
                        if (this.isBlockingSword() && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                           this.unblock(false);
                        }
                     }

                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     this.isRenderBlocking = false;
                     break;
                  case 1:
                     if (this.hasValidTargetInAutoBlockRange()) {
                        if (!this.isBlockingSword() && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                           var69 = true;
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = true;
                        this.isRenderBlocking = false;
                     } else {
                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                     }
                     break;
                  case 2:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        break;
                     }

                     int var66 = ReflectionUtil.getCurrentPlayerItem(mc.playerController);
                     if (!Valor.stateTracker.hasDug
                        && !Valor.stateTracker.hasPlacedBlock
                        && mc.thePlayer.inventory.currentItem == var66
                        && (!this.isBlockingSword() || this.autoBlockStage == 0)
                        && (this.attackDelayMs <= 0L || this.attackDelayMs > 50L)) {
                        int var71 = findSpoofHotbarSlot(var66);
                        PacketUtil.sendPacket(new C09PacketHeldItemChange(var71));
                        PacketUtil.sendPacket(new C09PacketHeldItemChange(var66));
                        var69 = true;
                        this.autoBlockStage = 1;
                     } else {
                        this.autoBlockStage = 0;
                        this.blockPacketState = 0;
                     }

                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     this.isAutoBlocking = true;
                     this.isRenderBlocking = false;
                     break;
                  case 3:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        break;
                     }

                     if (!Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                        switch (this.autoBlockStage) {
                           case 0:
                              if (!this.isBlockingSword()) {
                                 var69 = true;
                              }

                              var5 = true;
                              this.autoBlockStage = 1;
                              break;
                           case 1:
                              if (this.isBlockingSword()) {
                                 if (Valor.moduleManager.modules.get(NoSlow.class).isEnabled()) {
                                    int var65 = new Random().nextInt(9);

                                    while (var65 == mc.thePlayer.inventory.currentItem) {
                                       var65 = new Random().nextInt(9);
                                    }

                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(var65));
                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                 }

                                 this.unblock(false);
                                 var3 = false;
                              }

                              if (this.attackDelayMs > 50L) {
                                 break;
                              }
                           default:
                              this.autoBlockStage = 0;
                              this.blockPacketState = 0;
                        }
                     }

                     this.isAutoBlocking = true;
                     this.isRenderBlocking = true;
                     break;
                  case 4:
                     if (this.hasValidTargetInAutoBlockRange()) {
                        int var64 = ReflectionUtil.getCurrentPlayerItem(mc.playerController);
                        if (mc.thePlayer.inventory.currentItem == var64 && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                           switch (this.autoBlockStage) {
                              case 0:
                                 if (findSwordHotbarSlot(var64) != -1) {
                                    if (!this.isBlockingSword()) {
                                       var69 = true;
                                    }

                                    var3 = false;
                                    this.autoBlockStage = 1;
                                 }
                                 break;
                              case 1:
                                 int var76;
                                 if ((var76 = findSwordHotbarSlot(var64)) != -1) {
                                    if (!this.isBlockingSword()) {
                                       var69 = true;
                                       break;
                                    } else {
                                       if (this.attackDelayMs > 50L) {
                                          break;
                                       }

                                       PacketUtil.sendPacket(new C09PacketHeldItemChange(var76));
                                       ReflectionUtil.setCurrentPlayerItem(mc.playerController, var76);
                                       this.startBlockingItem(mc.thePlayer.inventory.getStackInSlot(var76));
                                    }
                                 }
                              default:
                                 this.autoBlockStage = 0;
                                 this.blockPacketState = 0;
                           }

                           Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                           this.isAutoBlocking = true;
                           this.isRenderBlocking = true;
                           break;
                        }
                     }

                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     this.isAutoBlocking = false;
                     this.isRenderBlocking = false;
                     break;
                  case 5:
                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     this.isAutoBlocking = false;
                     this.isRenderBlocking = this.hasValidTargetInAutoBlockRange();
                     if (PlayerUtil.isUseItemPressed() && !this.isBlockingSword() && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                        var69 = true;
                     }
                     break;
                  case 6:
                     this.isAutoBlocking = this.hasValidTargetInAutoBlockRange();
                     break;
                  case 7:
                     if (this.hasValidTargetInAutoBlockRange()) {
                        if (this.clientSideBlocking) {
                           this.autoBlockStage++;
                           if (this.autoBlockStage >= 2) {
                              this.unblock(false);
                              this.autoBlockStage = 0;
                              this.blockPacketState = 0;
                              var3 = false;
                           }
                        } else {
                           var69 = true;
                           this.autoBlockStage = 0;
                           this.blockPacketState = 0;
                        }

                        this.isAutoBlocking = true;
                     } else if (this.clientSideBlocking) {
                        this.unblock(false);
                        this.isAutoBlocking = false;
                     }
                     break;
                  case 8:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        if (this.clientSideBlocking) {
                           this.unblock(false);
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        this.autoBlockStage = 0;
                        break;
                     }

                     if (!Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                        switch (this.autoBlockStage) {
                           case 0:
                              if (!this.isBlockingSword()) {
                                 var69 = true;
                              }

                              this.autoBlockStage = 1;
                              break;
                           case 1:
                              if (this.isBlockingSword()) {
                                 this.unblock(false);
                                 var3 = false;
                              }

                              if (this.attackDelayMs > 50L) {
                                 break;
                              }
                           default:
                              this.autoBlockStage = 0;
                        }
                     }

                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     this.isAutoBlocking = true;
                     this.isRenderBlocking = true;
                     break;
                  case 9:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        if (this.clientSideBlocking) {
                           this.sendDigUnblockPacket();
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        this.blockPacketState = 0;
                        break;
                     }

                     if (!Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                        switch (this.blockPacketState) {
                           case 0:
                              Valor.blinkManager.setBlinking(true, BlinkType.SCAFFOLD);
                              int var63 = new Random().nextInt(9);

                              while (var63 == mc.thePlayer.inventory.currentItem) {
                                 var63 = new Random().nextInt(9);
                              }

                              PacketUtil.sendPacket(new C09PacketHeldItemChange(var63));
                              if (this.clientSideBlocking) {
                                 this.sendDigUnblockPacket();
                              }

                              this.blockPacketState = 1;
                              break;
                           case 1:
                              PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                              this.blockPacketState = 2;
                              break;
                           case 2:
                              this.blockPacketState = 3;
                              if (!this.isInteracting) {
                                 break;
                              }
                           case 3:
                              this.blockPacketState = 4;
                              if (!this.isInteracting) {
                                 break;
                              }
                           case 4:
                              PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                              this.clientSideBlocking = true;
                              Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                              this.isInteracting = !this.isInteracting;
                              this.blockPacketState = 0;
                        }
                     }

                     this.isAutoBlocking = true;
                     this.isRenderBlocking = true;
                     break;
                  case 10:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        if (this.clientSideBlocking) {
                           this.unblock(false);
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        this.blockPacketState = 0;
                        break;
                     }

                     if (!Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                        switch (this.blockPacketState) {
                           case 0:
                              if (this.clientSideBlocking || this.isBlockingSword()) {
                                 this.unblock(false);
                              }

                              var3 = false;
                              this.blockPacketState = 1;
                              break;
                           case 1:
                              if (this.attackDelayMs <= 0L) {
                                 this.blockPacketState = 2;
                              }
                              break;
                           case 2:
                              var3 = false;
                              if (!this.clientSideBlocking) {
                                 this.blockWithSword();
                              }

                              this.blockPacketState = 0;
                              break;
                           default:
                              this.blockPacketState = 0;
                        }
                     }

                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     this.isAutoBlocking = true;
                     this.isRenderBlocking = true;
                     break;
                  case 11:
                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     this.isAutoBlocking = this.hasValidTargetInAutoBlockRange();
                     this.isRenderBlocking = this.hasValidTargetInAutoBlockRange();
                     break;
                  case 12:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        this.autoBlockStage = 0;
                        this.blockPacketState = 0;
                     } else {
                        this.isRenderBlocking = true;
                        if (this.shouldAutoBlock() && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                           switch (this.autoBlockStage) {
                              case 0:
                                 if (!this.isBlockingSword()) {
                                    var69 = true;
                                 } else {
                                    var3 = false;
                                 }

                                 var5 = true;
                                 this.autoBlockStage = 1;
                                 break;
                              case 1:
                                 if (this.isBlockingSword()) {
                                    if (Valor.moduleManager.modules.get(NoSlow.class).isEnabled()) {
                                       int var70 = new Random().nextInt(9);

                                       while (var70 == mc.thePlayer.inventory.currentItem) {
                                          var70 = new Random().nextInt(9);
                                       }

                                       PacketUtil.sendPacket(new C09PacketHeldItemChange(var70));
                                       PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                    }

                                    this.unblock(false);
                                    var3 = false;
                                 }

                                 if (this.attackDelayMs > 50L) {
                                    break;
                                 }
                              default:
                                 this.autoBlockStage = 0;
                                 this.blockPacketState = 0;
                           }

                           this.isAutoBlocking = true;
                           break;
                        }

                        if (this.isBlockingSword()) {
                           this.unblock(false);
                           var3 = false;
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.autoBlockStage = 0;
                        this.blockPacketState = 0;
                     }
                     break;
                  case 13:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        if (this.isBlockingSword()) {
                           this.unblock(false);
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        this.blockPacketState = 0;
                     } else {
                        this.isRenderBlocking = true;
                        if (this.shouldAutoBlock() && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                           switch (this.blockPacketState) {
                              case 0:
                                 var3 = false;
                                 if (!this.isBlockingSword()) {
                                    var69 = true;
                                 }

                                 this.blockPacketState = 1;
                                 break;
                              case 1:
                                 var3 = false;
                                 if (this.isBlockingSword()) {
                                    this.unblock(false);
                                 }

                                 this.blockPacketState = 2;
                                 break;
                              case 2:
                                 if (this.isBlockingSword()) {
                                    var3 = false;
                                 }

                                 if (this.attackDelayMs > 0L || this.isBlockingSword()) {
                                    if (this.attackDelayMs <= 0L) {
                                       break;
                                    }

                                    var3 = false;
                                 }
                              default:
                                 this.blockPacketState = 0;
                           }

                           this.isAutoBlocking = true;
                           break;
                        }

                        if (this.isBlockingSword()) {
                           this.unblock(false);
                           var3 = false;
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.blockPacketState = 0;
                     }
                     break;
                  case 14:
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        if (this.isBlockingSword()) {
                           this.unblock(false);
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        this.blockPacketState = 0;
                     } else {
                        this.isRenderBlocking = true;
                        if (this.shouldAutoBlock() && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                           switch (this.blockPacketState) {
                              case 0:
                                 var3 = false;
                                 if (!this.isBlockingSword()) {
                                    var69 = true;
                                 }

                                 this.blockPacketState = 1;
                                 break;
                              case 1:
                                 var3 = false;
                                 this.blockPacketState = 2;
                                 break;
                              case 2:
                                 var3 = false;
                                 if (this.isBlockingSword()) {
                                    this.unblock(false);
                                 }

                                 this.blockPacketState = 3;
                                 break;
                              case 3:
                                 if (this.isBlockingSword()) {
                                    var3 = false;
                                 }

                                 if (this.attackDelayMs > 0L || this.isBlockingSword()) {
                                    if (this.attackDelayMs <= 0L) {
                                       break;
                                    }

                                    var3 = false;
                                 }
                              default:
                                 this.blockPacketState = 0;
                           }

                           this.isAutoBlocking = true;
                           break;
                        }

                        if (this.isBlockingSword()) {
                           this.unblock(false);
                           var3 = false;
                        }

                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        this.isAutoBlocking = false;
                        this.blockPacketState = 0;
                     }
                     break;
                  case 15:
                     Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                     if (!this.hasValidTargetInAutoBlockRange()) {
                        if (this.isBlockingSword()) {
                           this.unblock(false);
                        }

                        this.isAutoBlocking = false;
                        this.isRenderBlocking = false;
                        this.blockPacketState = 0;
                        this.wasBlocking = false;
                        this.nextUnblockTimeMs = 0L;
                     } else {
                         boolean var7 = false;
                         boolean shouldBlockNow = this.shouldAutoBlock();
                         long currentTimeMs = System.currentTimeMillis();
                         if (!shouldBlockNow) {
                            this.wasBlocking = false;
                            this.nextUnblockTimeMs = 0L;
                         } else {
                            if (!this.wasBlocking) {
                               this.wasBlocking = true;
                               long pingDelay = Math.min(100L, (long)getPlayerPing() / 2L);
                               this.nextUnblockTimeMs = currentTimeMs + 180L + pingDelay;
                            }

                            if (currentTimeMs < this.nextUnblockTimeMs) {
                               var7 = true;
                            }
                         }
                        switch (this.blockPacketState) {
                           case 0:
                              if (var7) {
                                 var3 = false;
                                 var69 = true;
                                 this.autoBlockStage = 0;
                                 this.blockPacketState = 1;
                              } else {
                                 this.blockPacketState = 4;
                              }
                              break;
                           case 1:
                              boolean var8 = this.currentTarget != null && (this.isWithinAttackRange(this.currentTarget.entity) || this.isBoundingBoxInAttackRange(this.currentTarget.boundingBox));
                              boolean var9 = var3 && var8 && this.attackDelayMs <= 15L;
                              var3 = false;
                              if (var9) {
                                 if (var7) {
                                    this.autoBlockStage = 1;
                                 } else {
                                    if (Valor.isBadlion) {
                                       this.sendDigUnblockPacket();
                                    } else {
                                       this.unblock(true);
                                    }

                                    this.attackDelayMs = 0L;
                                    this.autoBlockStage = 0;
                                    this.blockPacketState = 2;
                                 }
                              } else {
                                 if (Valor.isBadlion) {
                                    this.sendDigUnblockPacket();
                                 } else {
                                    this.unblock(true);
                                 }

                                 this.autoBlockStage = 0;
                                 this.blockPacketState = 4;
                              }
                              break;
                           case 2:
                              if (this.attackDelayMs > 0L) {
                                 var3 = false;
                                 this.blockPacketState = 3;
                              }
                              break;
                           case 3:
                              var3 = false;
                              this.autoBlockStage = 0;
                              if (var7) {
                                 var69 = true;
                                 this.blockPacketState = 1;
                              } else {
                                 this.blockPacketState = 4;
                              }
                              break;
                           case 4:
                              if (var7) {
                                 var3 = false;
                                 var69 = true;
                                 this.autoBlockStage = 0;
                                 this.blockPacketState = 1;
                              } else if (this.clientSideBlocking) {
                                 var3 = false;
                                 this.sendDigUnblockPacket();
                              }
                              break;
                           default:
                              this.blockPacketState = 0;
                        }

                        this.isAutoBlocking = true;
                        this.isRenderBlocking = true;
                     }
               }
            }

            boolean var122 = false;
            boolean var67 = false;
            boolean isSwingInRange = this.isWithinSwingRange(this.currentTarget.entity)
               || (RotationUtil.getDistanceToBoxEyes(this.currentTarget.boundingBox) <= (double)((Float)this.swingRangeProp.getValue()).floatValue());
            boolean var72 = isSwingInRange;
            boolean var77 = this.isWithinAttackRange(this.currentTarget.entity) || this.isBoundingBoxInAttackRange(this.currentTarget.boundingBox);
            if (var72 || var77) {
               if ((Integer)this.rotationsProp.getValue() > 0) {
                  float var98 = var1.getPitch();
                  float var79 = var1.getYaw();
                  this.updateAimOffsets(false);
                  AxisAlignedBB var16 = this.currentTarget.boundingBox;
                  double var104 = this.currentTarget.boundingBox.minX + (var16.maxX - var16.minX) * (double)this.aimOffsetX;
                  double var19 = var16.minY + (var16.maxY - var16.minY) * (double)this.aimOffsetY;
                  double var21 = var16.minZ + (var16.maxZ - var16.minZ) * (double)this.aimOffsetZ;
                  Vec3 var10 = mc.thePlayer.getPositionEyes(1.0F);
                  double var23;
                  if ((var23 = RotationUtil.getDistanceToBoxEyes(var16)) > (double)((Float)this.attackRangeProp.getValue()).floatValue() - 0.35) {
                     Vec3 var25;
                     var104 = (var25 = RotationUtil.getClosestPointOnAABB(var10, var16)).xCoord;
                     var19 = var25.yCoord;
                     var21 = var25.zCoord;
                  }

                  float[] var113 = RotationUtil.getSmoothRotations(
                     var104 - var10.xCoord,
                     var19 - var10.yCoord,
                     var21 - var10.zCoord,
                     var79,
                     var98,
                     (float)((Integer)this.angleStepProp.getValue()).intValue() + RandomUtil.nextFloat(-5.0F, 5.0F),
                     (float)((Integer)this.smoothingProp.getValue()).intValue() / 100.0F
                  );
                  float[] var11 = RotationUtil.applyGCD(var79, var98, var113[0], var113[1]);
                  float var12 = (Float)this.attackRangeProp.getValue() + 0.15F;
                  if (RotationUtil.calculateBoxIntercept(var16, var11[0], var11[1], (double)var12) == null && var23 <= (double)var12) {
                     Vec3 var87;
                     float[] var13 = RotationUtil.getSmoothRotations(
                     (var87 = RotationUtil.getClosestPointOnAABB(var10, var16)).xCoord - var10.xCoord,
                        var87.yCoord - var10.yCoord,
                        var87.zCoord - var10.zCoord,
                        var79,
                        var98,
                        (float)((Integer)this.angleStepProp.getValue()).intValue(),
                        0.0F
                     );
                     var11 = RotationUtil.applyGCD(var79, var98, var13[0], var13[1]);
                     if (RotationUtil.calculateBoxIntercept(var16, var11[0], var11[1], (double)var12) == null) {
                        double var26 = (var16.minX + var16.maxX) / 2.0;
                        double var28 = var16.minY + (var16.maxY - var16.minY) * 0.6;
                        double var30 = (var16.minZ + var16.maxZ) / 2.0;
                        float[] var73 = RotationUtil.getSmoothRotations(
                           var26 - var10.xCoord,
                           var28 - var10.yCoord,
                           var30 - var10.zCoord,
                           var79,
                           var98,
                           (float)((Integer)this.angleStepProp.getValue()).intValue(),
                           0.0F
                        );
                        var11 = RotationUtil.applyGCD(var79, var98, var73[0], var73[1]);
                     }
                  }

                  var1.setRotation(var11[0], var11[1], 1);
                  if ((Integer)this.rotationsProp.getValue() == 1 || (Integer)this.rotationsProp.getValue() == 3) {
                     Valor.rotationManager.setRotation(var11[0], var11[1], 1, true);
                  }

                  if ((Integer)this.moveFixProp.getValue() != 0 || (Integer)this.rotationsProp.getValue() == 1 || (Integer)this.rotationsProp.getValue() == 3) {
                     var1.setPervRotation(var11[0], 1);
                  }

                  KnockbackDisplace var99;
                  if ((var99 = (KnockbackDisplace)Valor.moduleManager.modules.get(KnockbackDisplace.class)) != null) {
                     EntityLivingBase var10001 = this.currentTarget.entity;
                     AxisAlignedBB var10002 = this.currentTarget.boundingBox;
                     float var20 = (Float)this.attackRangeProp.getValue();
                     UpdateEvent var108 = var1;
                     AxisAlignedBB var18 = var10002;
                     EntityLivingBase var105 = var10001;
                      if (var99.isEnabled()
                         && KnockbackDisplace.mc.thePlayer != null
                         && KnockbackDisplace.mc.theWorld != null
                         && var105 instanceof EntityPlayer
                         && var105 != KnockbackDisplace.mc.thePlayer
                         && var105.isEntityAlive()
                         && (KnockbackDisplace.mc.thePlayer.isSprinting() || (Boolean)var99.forceSprintProp.getValue())) {
                         if (!KnockbackDisplace.mc.thePlayer.isSprinting()) {
                            KnockbackDisplace.mc.thePlayer.setSprinting(true);
                         }

                        EntityPlayer var80;
                        EntityPlayer var109;
                        int var88 = MathHelper.floor_double((var80 = var109 = (EntityPlayer)var105).posX);
                        int var32 = MathHelper.floor_double(var80.posY) - 1;
                        int var33 = MathHelper.floor_double(var80.posZ);
                        int var94 = 0;

                        for (int var84 = -1; var84 <= 1; var84++) {
                           for (int var34 = -1; var34 <= 1; var34++) {
                              if (KnockbackDisplace.isSolidBlock(new BlockPos(var88 + var84, var32, var33 + var34))) {
                                 var94++;
                              }
                           }
                        }

                        if (var94 <= 3) {
                           var99.reset();
                        } else {
                           EntityPlayer var81 = var109;
                           KnockbackDisplace var74 = var99;
                           int var89 = MathHelper.floor_double(var109.posY) - 1;
                           double var118 = Double.MAX_VALUE;
                           KnockbackDisplace.DirectionOffset var95 = null;

                           for (int var85 = 0; var85 < 16; var85++) {
                              double var121;
                              double var36 = Math.cos(var121 = (Math.PI * 2) * (double)var85 / 16.0);
                              double var38 = Math.sin(var121);
                              double var48 = var38;
                              double var46 = var36;
                              int var100 = var89;
                              EntityPlayer var14 = var81;
                              KnockbackDisplace var92 = var74;
                              double var50 = (double)((Float)var74.scanRangeProp.getValue()).floatValue();
                              double var52 = 0.75;
                              double var124;

                               var124 = -1.0;
                               while (var52 <= var50) {
                                  double var54 = var14.posX + var46 * var52;
                                  double var56 = var14.posZ + var48 * var52;
                                  BlockPos var106 = new BlockPos(
                                     MathHelper.floor_double(var54), MathHelper.floor_double(var14.posY), MathHelper.floor_double(var56)
                                  );
                                  if (KnockbackDisplace.isSolidBlock(var106) || KnockbackDisplace.isSolidBlock(var106.up())) {
                                     var124 = -1.0;
                                     break;
                                  }

                                  int var24 = var106.getZ();
                                  int var22 = var106.getX();
                                  boolean hasSolidFloor = false;
                                  for (int step = 0; step < (Integer)var92.voidDepthProp.getValue(); step++) {
                                     if (KnockbackDisplace.isSolidBlock(new BlockPos(var22, var100 - step, var24))) {
                                        hasSolidFloor = true;
                                        break;
                                     }
                                  }

                                  if (!hasSolidFloor) {
                                     var124 = var52;
                                     break;
                                  }

                                  var52 += 0.5;
                               }

                              double var40 = var124;
                              if (!(var124 < 0.0)) {
                                 double var42 = !(Math.abs(var36) < 0.001)
                                       && !(Math.abs(var38) < 0.001)
                                       && !(Math.abs(Math.abs(var36) - Math.abs(var38)) < 0.001)
                                    ? 0.0
                                    : -0.01;
                                 double var44;
                                 if ((var44 = var40 + var42) < var118) {
                                    var118 = var44;
                                    var95 = new KnockbackDisplace.DirectionOffset(var36, var38);
                                 }
                              }
                           }

                           if (var95 == null) {
                              var99.reset();
                           } else {
                              float var86 = var1.getNewYaw();
                              float var111 = var1.getNewPitch();
                              double var112 = Math.max(0.1, (double)var20);
                              if (var18 != null && RotationUtil.calculateBoxIntercept(var18, var86, var111, var112) != null) {
                                 float var90;
                                 if (Math.abs(
                                       var12 = MathHelper.wrapAngleTo180_float(
                                          (var90 = KnockbackDisplace.calculateAngle(var95.dirX, var95.dirZ)) - var86
                                       )
                                    )
                                    > 18.0F) {
                                    var99.reset();
                                 } else {
                                     var32 = KnockbackDisplace.mc.thePlayer.ticksExisted;
                                     boolean resetTargetOrTimeout = (var99.targetPlayer != var109 || var32 - var99.lastTicksExisted > 6);
                                     boolean angleDiffTooHigh = !resetTargetOrTimeout
                                        && Math.abs(MathHelper.wrapAngleTo180_float(var90 - var99.targetYaw)) > 11.25F;
                                     if (!resetTargetOrTimeout && !angleDiffTooHigh) {
                                        if (var32 != var99.lastTicksExisted) {
                                           var99.prepareTicks++;
                                           var99.lastTicksExisted = var32;
                                        }

                                        var99.targetYaw = var90;
                                        var122 = (var99.prepareTicks >= 3);
                                     } else {
                                        var99.targetPlayer = var109;
                                        var99.targetYaw = var90;
                                        var99.prepareTicks = 1;
                                        var99.lastTicksExisted = var32;
                                        var122 = false;
                                     }
                                  }
                               }
                   }
                      }
                         }
                  } else {
                     var122 = this.tryAttack(var1.getNewYaw(), var1.getNewPitch());
                  }

                  var67 = var122;
               }
            }

            if ((Integer)this.autoBlockProp.getValue() == 6 && var3) {
               if (!this.hasValidTargetInAutoBlockRange()) {
                  if (this.clientSideBlocking) {
                     this.unblock(false);
                  }

                  this.isAutoBlocking = false;
               } else if (var77 && this.attackDelayMs <= 0L && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
                  if (this.clientSideBlocking) {
                     this.unblock(false);
                  }

                  float var101 = var1.getNewPitch();
                  float var82 = var1.getNewYaw();
                  if (this.currentTarget == null) {
                     var122 = false;
                  } else {
                     this.unblockIfItemReleased();
                     if (((Integer)this.rotationsProp.getValue() != 0 || !this.isWithinAttackRange(this.currentTarget.entity)) && !this.isLookingAtCurrentTarget(var82, var101)) {
                        var122 = false;
                     } else if (!this.isLookingAtCurrentTarget(var82, var101)) {
                        var122 = false;
                     } else {
                        mc.thePlayer.swingItem();
                        EventManager.call(new AttackEvent(this.currentTarget.entity));
                        ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
                        PacketUtil.sendPacket(new C02PacketUseEntity(this.currentTarget.entity, Action.ATTACK));
                        if (mc.playerController.getCurrentGameType() != GameType.SPECTATOR) {
                           PlayerUtil.attackEntity(this.currentTarget.entity);
                        }

                        this.shouldSwitch = true;
                        this.blockReleasePending = false;
                        var122 = true;
                     }
                  }

                  if (var122) {
                     this.attackDelayMs = this.attackDelayMs + this.calculateNextClickDelay();
                     var67 = true;
                  }

                  this.blockWithSword();
               } else if (!this.clientSideBlocking) {
                  this.blockWithSword();
               }
            }

            if ((Integer)this.autoBlockProp.getValue() == 11 && var3) {
               if (!this.hasValidTargetInAutoBlockRange()) {
                  if (this.isBlockingSword()) {
                     this.unblock(false);
                  }

                  this.isAutoBlocking = false;
                  this.isRenderBlocking = false;
                  this.blockPacketState = 0;
               } else {
                  boolean var83 = var77 && this.attackDelayMs <= 0L && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock;
                  if (this.isBlockingSword()) {
                     if (var83) {
                        this.unblock(false);
                        this.blockPacketState = 1;
                     }
                  } else if (var83 && this.tryAttack(var1.getNewYaw(), var1.getNewPitch())) {
                     var67 = true;
                     this.interactAndBlock(var1.getNewYaw(), var1.getNewPitch());
                     this.blockPacketState = 0;
                  }
               }
            }

            if (this.isVanillaAutoBlock() && this.blockPacketState == 2) {
               if (var67) {
                  this.autoBlockStage = 0;
                  this.blockPacketState = 3;
               } else if (var6) {
                  this.autoBlockStage = 0;
                  this.blockPacketState = 3;
               }
            }

            if (var69) {
               if (var67) {
                  this.interactAndBlock(var1.getNewYaw(), var1.getNewPitch());
               } else {
                  ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
                  this.startBlockingItem(mc.thePlayer.getHeldItem());
               }
            }

            if (var5) {
               Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
               Valor.blinkManager.setBlinking(true, BlinkType.SCAFFOLD);
            }
         }
      }
   }

   @EventTarget(4)
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled() && !var1.isCancelled() && mc.thePlayer != null && mc.theWorld != null) {
         if (var1.getPacket() instanceof C07PacketPlayerDigging
            && ((C07PacketPlayerDigging)var1.getPacket()).getStatus() == net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
            this.clientSideBlocking = false;
         }

         if (var1.getPacket() instanceof C09PacketHeldItemChange) {
            this.clientSideBlocking = false;
            if (this.isAutoBlocking) {
               mc.thePlayer.stopUsingItem();
            }
         }
      }
   }

   private static int findSpoofHotbarSlot(int var0) {
      for (int var1 = 0; var1 < 9; var1++) {
         if (var1 != var0 && mc.thePlayer.inventory.getStackInSlot(var1) == null) {
            return var1;
         }
      }

      for (int var3 = 0; var3 < 9; var3++) {
         ItemStack var2;
         if (var3 != var0 && (var2 = mc.thePlayer.inventory.getStackInSlot(var3)) != null && !var2.hasDisplayName()) {
            return var3;
         }
      }

      return Math.floorMod(var0 - 1, 9);
   }

   private boolean isFriend(EntityLivingBase var1) {
      return var1 instanceof EntityPlayer && BotCheckUtil.isTarget((EntityPlayer)var1);
   }

   private boolean isValidTarget(EntityLivingBase var1) {
      if (!mc.theWorld.loadedEntityList.contains(var1)) {
         return false;
      } else if (var1 == mc.thePlayer || var1 == mc.thePlayer.ridingEntity) {
         return false;
      } else if (var1 == mc.getRenderViewEntity() || var1 == mc.getRenderViewEntity().ridingEntity) {
         return false;
      } else if (var1.deathTime > 0) {
         return false;
      } else if (RotationUtil.getAngleToEntity(var1) > ((Integer)this.fovProp.getValue()).floatValue()) {
         return false;
      } else if (!(Boolean)this.throughWallsProp.getValue() && RotationUtil.rayTraceToEntity(var1) != null) {
         return false;
      } else if (var1 instanceof EntityOtherPlayerMP) {
         if (!(Boolean)this.playersProp.getValue()) {
            return false;
         } else {
            return BotCheckUtil.isFriend((EntityPlayer)var1)
               ? false
               : (!(Boolean)this.teamsProp.getValue() || !BotCheckUtil.isTeammate((EntityPlayer)var1))
                  && (!(Boolean)this.botCheckProp.getValue() || !BotCheckUtil.isBot((EntityPlayer)var1));
         }
      } else if (var1 instanceof EntityDragon || var1 instanceof EntityWither) {
         return (Boolean)this.bossesProp.getValue();
      } else if (!(var1 instanceof EntityMob) && !(var1 instanceof EntitySlime)) {
         if (var1 instanceof EntityAnimal || var1 instanceof EntityBat || var1 instanceof EntitySquid || var1 instanceof EntityVillager) {
            return (Boolean)this.animalsProp.getValue();
         } else {
            return !(var1 instanceof EntityIronGolem) ? false : (Boolean)this.golemsProp.getValue() && (!(Boolean)this.teamsProp.getValue() || !BotCheckUtil.isTeammateBedwars(var1));
         }
      } else {
         return !(var1 instanceof EntitySilverfish) ? (Boolean)this.mobsProp.getValue() : (Boolean)this.silverfishProp.getValue() && (!(Boolean)this.teamsProp.getValue() || !BotCheckUtil.isTeammateBedwars(var1));
      }
   }

   private boolean tryAttack(float var1, float var2) {
      if (this.currentTarget == null) {
         return false;
      } else if (!Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock) {
         if ((Boolean)this.prioritizeBlockingProp.getValue() && !this.blockReleasePending) {
            EntityLivingBase var4 = this.currentTarget.entity;
            if (var4 != null
               && getAngleDiff(
                                                    RotationUtil.getRotations(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + (double)mc.thePlayer.height / 2.0,
                        mc.thePlayer.posZ,
                        var4.rotationYaw,
                        var4.rotationPitch
                     )[0],
                     var4.rotationYaw
                  )
                  <= (Float)this.maxDirectionDiffProp.getValue()
               && RotationUtil.getDistanceToTarget(var4) <= (double)((Float)this.smartBlockRangeProp.getValue()).floatValue()) {
               return false;
            }
         }

         this.unblockIfItemReleased();
         if (this.isBlockingSword() && (Integer)this.autoBlockProp.getValue() != 1) {
            if (this.isAdvancedAutoBlockMode()) {
               if (!Valor.isBadlion || !this.isVanillaAutoBlock() || this.clientSideBlocking) {
                  return false;
               }
            } else {
               this.unblock(true);
               this.clientSideBlocking = false;
               if (mc.thePlayer.isUsingItem() && ItemUtil.isHoldingSword()) {
                  return false;
               }
            }
         }

         if (this.attackDelayMs > 0L) {
            return false;
         } else if (!this.isWithinAttackRange(this.currentTarget.entity) && !this.isLookingAtCurrentTarget(var1, var2)) {
            return false;
         } else if (!this.isLookingAtCurrentTarget(var1, var2)) {
            return false;
         } else {
            this.attackDelayMs = this.attackDelayMs + this.calculateNextClickDelay();
            mc.thePlayer.swingItem();
            EventManager.call(new AttackEvent(this.currentTarget.entity));
            ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
            PacketUtil.sendPacket(new C02PacketUseEntity(this.currentTarget.entity, Action.ATTACK));
            if (mc.playerController.getCurrentGameType() != GameType.SPECTATOR) {
               PlayerUtil.attackEntity(this.currentTarget.entity);
            }

            this.shouldSwitch = true;
            this.blockReleasePending = false;
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void verifyValue(String var1) {
      boolean var2 = (Integer)this.autoBlockProp.getValue() == 2
         || (Integer)this.autoBlockProp.getValue() == 3
         || (Integer)this.autoBlockProp.getValue() == 4
         || (Integer)this.autoBlockProp.getValue() == 6
         || (Integer)this.autoBlockProp.getValue() == 7
         || (Integer)this.autoBlockProp.getValue() == 8
         || (Integer)this.autoBlockProp.getValue() == 10
         || (Integer)this.autoBlockProp.getValue() == 11
         || this.isAdvancedAutoBlockMode()
         || this.isVanillaAutoBlock();
      if (!this.autoBlockProp.getName().equals(var1)) {
         if (this.swingRangeProp.getName().equals(var1)) {
            if ((Float)this.swingRangeProp.getValue() < (Float)this.attackRangeProp.getValue()) {
               this.attackRangeProp.setValue(this.swingRangeProp.getValue());
               return;
            }
         } else if (this.attackRangeProp.getName().equals(var1)) {
            if ((Float)this.swingRangeProp.getValue() < (Float)this.attackRangeProp.getValue()) {
               this.swingRangeProp.setValue(this.attackRangeProp.getValue());
               return;
            }
         } else if (this.minApsProp.getName().equals(var1)) {
            if ((Float)this.minApsProp.getValue() > (Float)this.maxApsProp.getValue()) {
               this.maxApsProp.setValue(this.minApsProp.getValue());
               return;
            }
         } else if (this.autoBlockMinApsProp.getName().equals(var1)) {
            if ((Float)this.autoBlockMinApsProp.getValue() > (Float)this.autoBlockMaxApsProp.getValue()) {
               this.autoBlockMaxApsProp.setValue(this.autoBlockMinApsProp.getValue());
            }

            if ((Float)this.autoBlockMinApsProp.getValue() > 10.0F && var2) {
               this.autoBlockMinApsProp.setValue(10.0F);
               return;
            }
         } else if (this.autoBlockMaxApsProp.getName().equals(var1)) {
            if ((Float)this.autoBlockMinApsProp.getValue() > (Float)this.autoBlockMaxApsProp.getValue()) {
               this.autoBlockMinApsProp.setValue(this.autoBlockMaxApsProp.getValue());
            }

            if ((Float)this.autoBlockMaxApsProp.getValue() > 10.0F && var2) {
               this.autoBlockMaxApsProp.setValue(10.0F);
               return;
            }
         } else if (this.maxApsProp.getName().equals(var1) && (Float)this.minApsProp.getValue() > (Float)this.maxApsProp.getValue()) {
            this.minApsProp.setValue(this.maxApsProp.getValue());
            return;
         }
      } else if (var2 && ((Float)this.autoBlockMinApsProp.getValue() > 10.0F || (Float)this.autoBlockMaxApsProp.getValue() > 10.0F)) {
         this.autoBlockMinApsProp.setValue(8.0F);
         this.autoBlockMaxApsProp.setValue(10.0F);
      }
   }

   private void sendDigUnblockPacket() {
      PacketUtil.sendPacket(
         new C07PacketPlayerDigging(net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
      );
      this.clientSideBlocking = false;
   }

   private void releaseUseItemKey() {
      if (this.autoBlockTargetFound) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
         this.autoBlockTargetFound = false;
      }
   }

   private static int findSwordHotbarSlot(int var0) {
      for (int var1 = 0; var1 < 9; var1++) {
         ItemStack var2;
         if (var1 != var0 && (var2 = mc.thePlayer.inventory.getStackInSlot(var1)) != null && var2.getItem() instanceof ItemSword) {
            return var1;
         }
      }

      return -1;
   }

   private boolean isWithinAttackRange(EntityLivingBase var1) {
      return RotationUtil.getDistanceToTarget(var1) <= (double)((Float)this.attackRangeProp.getValue()).floatValue();
   }

   private void unblock(boolean var1) {
      if (this.clientSideBlocking || this.isBlockingSword()) {
         int var2;
         if (!var1 && (Integer)this.unblockModeProp.getValue() == 2 && (var2 = findSpoofHotbarSlot(mc.thePlayer.inventory.currentItem)) >= 0 && var2 <= 8) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(var2));
            ReflectionUtil.setCurrentPlayerItem(mc.playerController, var2);
            mc.thePlayer.stopUsingItem();
            this.clientSideBlocking = false;
         } else if (!var1 && (Integer)this.unblockModeProp.getValue() == 1) {
            var2 = (mc.thePlayer.inventory.currentItem + 1) % 9;
            PacketUtil.sendPacket(new C09PacketHeldItemChange(var2));
            PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            mc.thePlayer.stopUsingItem();
            this.clientSideBlocking = false;
         } else {
            PacketUtil.sendPacket(
               new C07PacketPlayerDigging(net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
            );
            mc.thePlayer.stopUsingItem();
            this.clientSideBlocking = false;
         }
      }
   }

   private boolean isWithinAutoBlockRange(EntityLivingBase var1) {
      return RotationUtil.getDistanceToTarget(var1) <= (double)((Float)this.autoBlockRangeProp.getValue()).floatValue();
   }

   private boolean checkBacktrackRayTrace(float var1, float var2) {
      BackTrack var3;
      return (var3 = (BackTrack)Valor.moduleManager.modules.get(BackTrack.class)) == null
         || var3.isTargetInReach(this.currentTarget.entity, var1, var2, (Float)this.attackRangeProp.getValue());
   }

   public final boolean isVanillaAutoBlock() {
      return (Integer)this.autoBlockProp.getValue() == 15;
   }

   public final boolean isVanillaBlocking() {
      return this.isEnabled() && this.isVanillaAutoBlock() && mc.thePlayer != null && ItemUtil.isHoldingSword();
   }

   private boolean shouldAutoBlock() {
      for (Entity entity : mc.theWorld.loadedEntityList) {
         if (entity instanceof EntityLivingBase && this.isWithinAutoBlockRange((EntityLivingBase)entity)) {
            if (this.canAutoBlockTarget((EntityLivingBase)entity)) {
               return true;
            }
         }
      }
      return false;
   }

   private boolean canAutoBlockTarget(EntityLivingBase target) {
      if (target == null) return false;
      ItemStack heldItem = target.getHeldItem();
      if ((Boolean)this.checkEnemyWeaponProp.getValue()
         && (heldItem == null || !(heldItem.getItem() instanceof ItemSword) && !(heldItem.getItem() instanceof ItemAxe))) {
         return false;
      }

      double diffX = target.posX - mc.thePlayer.posX;
      double diffZ = target.posZ - mc.thePlayer.posZ;
      double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      double targetMotionX = target.posX - target.lastTickPosX;
      double targetMotionZ = target.posZ - target.lastTickPosZ;
      double playerMotionX = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
      double playerMotionZ = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
      double approachSpeed = 0.0;
      if (distXZ > 1.0E-4) {
         double relMotionX = targetMotionX - playerMotionX;
         double relMotionZ = targetMotionZ - playerMotionZ;
         approachSpeed = Math.max(0.0, -(diffX * relMotionX + diffZ * relMotionZ) / distXZ);
      }

      int pingTicks = Math.min(4, (getPlayerPing() + 49) / 50);
      double predictedDist = Math.max(0.0, RotationUtil.getDistanceToTarget(target) - approachSpeed * (double)(pingTicks + 1));
      if (predictedDist > (double)((Float)this.smartBlockRangeProp.getValue()).floatValue() + 0.15) {
         return false;
      }

      float[] rots = RotationUtil.getRotations(
         mc.thePlayer.posX,
         mc.thePlayer.posY + (double)mc.thePlayer.height / 2.0,
         mc.thePlayer.posZ,
         target.rotationYaw,
         target.rotationPitch
      );
      float maxDiff = (Float)this.maxDirectionDiffProp.getValue() + (predictedDist <= 2.25 ? 15.0F : 0.0F);
      if (getAngleDiff(rots[0], target.rotationYaw) > maxDiff) {
         return false;
      }

      boolean isSwinging = target.isSwingInProgress || target.swingProgressInt > 0;
      double closeThreshold = Math.min((double)((Float)this.smartBlockRangeProp.getValue()).floatValue(), 2.4);
      boolean isClosingIn = !isSwinging && approachSpeed >= 0.055 && predictedDist <= closeThreshold;
      boolean isPlayerHurt = !isSwinging && mc.thePlayer.hurtTime >= 7 && predictedDist <= closeThreshold;
      if (isSwinging || isClosingIn || isPlayerHurt) {
         if (!isSwinging) {
            return true;
         }
         int pingOffset = Math.min(2, (pingTicks + 1) / 2);
         if (target.swingProgressInt <= Math.max(1, (Integer)this.maxSwingProgressProp.getValue() + 2 + pingOffset)) {
            return true;
         }
      }

      return false;
   }

   private boolean shouldBlockChance() {
      return (Integer)this.blockRateProp.getValue() >= 100 || new Random().nextInt(100) < (Integer)this.blockRateProp.getValue();
   }

   private void startBlockingItem(ItemStack var1) {
      if (this.shouldBlockChance()) {
         ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
         PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(var1));
         mc.thePlayer.setItemInUse(var1, var1.getMaxItemUseDuration());
         this.clientSideBlocking = true;
         this.blockReleasePending = true;
      }
   }

   private boolean isLookingAtCurrentTarget(float var1, float var2) {
      if (this.currentTarget == null) {
         return false;
      } else {
         EntityLivingBase var3 = this.currentTarget.entity;
         AxisAlignedBB var4 = this.currentTarget.boundingBox;
         float var5 = (Float)this.attackRangeProp.getValue();
         if (RotationUtil.calculateBoxIntercept(var4, var1, var2, (double)var5) != null) {
            return true;
         } else {
            return RotationUtil.calculateBoxIntercept(var4, var1, var2, (double)(var5 + 0.25F)) != null ? true : this.isWithinAttackRange(var3);
         }
      }
   }

   public final EntityLivingBase getCurrentTargetEntity() {
      return this.currentTarget != null ? this.currentTarget.entity : null;
   }

   private void interactAndBlock(float var1, float var2) {
      MovingObjectPosition var3;
      if (this.currentTarget != null && (var3 = RotationUtil.calculateBoxIntercept(this.currentTarget.boundingBox, var1, var2, 8.0)) != null) {
         ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
         PacketUtil.sendPacket(
            new C02PacketUseEntity(
               this.currentTarget.entity,
               new Vec3(
                  var3.hitVec.xCoord - this.currentTarget.posX, var3.hitVec.yCoord - this.currentTarget.posY, var3.hitVec.zCoord - this.currentTarget.posZ
               )
            )
         );
         PacketUtil.sendPacket(new C02PacketUseEntity(this.currentTarget.entity, Action.INTERACT));
         PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
         mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(), mc.thePlayer.getHeldItem().getMaxItemUseDuration());
         this.clientSideBlocking = true;
      }
   }

   private static float getAngleDiff(float var0, float var1) {
      return Math.abs(MathHelper.wrapAngleTo180_float(var0 - var1));
   }

   private boolean isBoundingBoxInAttackRange(AxisAlignedBB var1) {
      return RotationUtil.getDistanceToBoxEyes(var1) <= (double)((Float)this.attackRangeProp.getValue()).floatValue();
   }

   @Override
   public void onDisabled() {
      this.releaseUseItemKey();
      if (this.clientSideBlocking) {
         this.unblock(false);
      }

      Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
      this.clientSideBlocking = false;
      this.isAutoBlocking = false;
      this.isRenderBlocking = false;
      this.blockReleasePending = false;
      this.wasBlocking = false;
      this.nextUnblockTimeMs = 0L;
      this.lastAttackTimeMs = 0L;
   }

   @Override
   public void onEnabled() {
      this.currentTarget = null;
      this.switchIndex = 0;
      this.shouldSwitch = false;
      this.attackDelayMs = 0L;
      this.lastAttackTimeMs = System.currentTimeMillis();
      this.autoBlockStage = 0;
      this.blockPacketState = 0;
      this.wasBlocking = false;
      this.nextUnblockTimeMs = 0L;
      this.blockReleasePending = false;
   }

   private void unblockIfItemReleased() {
      if ((Integer)this.autoBlockProp.getValue() != 1 && (Integer)this.autoBlockProp.getValue() != 11 && !this.isAdvancedAutoBlockMode() && this.isBlockingSword()) {
         if (this.clientSideBlocking && !mc.thePlayer.isUsingItem()) {
            this.sendDigUnblockPacket();
         } else {
            this.unblock(true);
         }

         if (!mc.thePlayer.isUsingItem()) {
            this.clientSideBlocking = false;
         }
      }
   }

   public final boolean isAutoBlockNoSlow() {
      return this.isBlockingSword() && this.isAutoBlocking
         ? !mc.thePlayer.isInWater()
            && !mc.thePlayer.isInLava()
            && (
               (Integer)this.autoBlockProp.getValue() == 3
                  || (Integer)this.autoBlockProp.getValue() == 4
                  || (Integer)this.autoBlockProp.getValue() == 6
                  || (Integer)this.autoBlockProp.getValue() == 7
                  || (Integer)this.autoBlockProp.getValue() == 8
                  || (Integer)this.autoBlockProp.getValue() == 10
                  || (Integer)this.autoBlockProp.getValue() == 11
                  || this.isAdvancedAutoBlockMode()
            )
         : false;
   }

   private boolean canAutoBlock() {
      if (!ItemUtil.isHoldingSword()) {
         return false;
      } else if ((Boolean)this.autoBlockRequirePressProp.getValue() && !PlayerUtil.isUseItemPressed()) {
         return false;
      } else {
         KillAura var1 = this;
         if (this.isVanillaAutoBlock()) {
            return true;
         } else if ((Boolean)this.smartAutoBlockProp.getValue() && !this.isAdvancedAutoBlockMode()) {
            Iterator var2 = mc.theWorld.loadedEntityList.iterator();

            while (var2.hasNext()) {
               Entity var3;
               if ((var3 = (Entity)var2.next()) instanceof EntityLivingBase && var1.isWithinAutoBlockRange((EntityLivingBase)var3) && var1.isValidTarget((EntityLivingBase)var3)) {
                  EntityLivingBase var4 = (EntityLivingBase)var3;
                  ItemStack var5;
                  if (!var1.smartAutoBlockProp.getValue() || var4 == null
                     ? true
                     : (
                        MoveUtil.isMoving() && var1.forceBlockWhenStillProp.getValue()
                           ? false
                           : (
                              !var1.checkEnemyWeaponProp.getValue()
                                    || (var5 = var4.getHeldItem()) != null && (var5.getItem() instanceof ItemSword || var5.getItem() instanceof ItemAxe)
                                 ? (
                                    mc.thePlayer.hurtTime > var1.maxOwnHurtTimeProp.getValue()
                                       ? false
                                       : (
                                          getAngleDiff(
                                                    RotationUtil.getRotations(
                                                      mc.thePlayer.posX,
                                                      mc.thePlayer.posY + (double)mc.thePlayer.height / 2.0,
                                                      mc.thePlayer.posZ,
                                                      var4.rotationYaw,
                                                      var4.rotationPitch
                                                   )[0],
                                                   var4.rotationYaw
                                                )
                                                > var1.maxDirectionDiffProp.getValue()
                                             ? false
                                             : (
                                                var4.swingProgressInt > var1.maxSwingProgressProp.getValue()
                                                   ? false
                                                   : !(RotationUtil.getDistanceToTarget(var4) > (double)((Float)var1.smartBlockRangeProp.getValue()).floatValue())
                                             )
                                       )
                                 )
                                 : false
                           )
                     )) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            return true;
         }
      }
   }

   public final boolean canAttackWithItem() {
      if (((Scaffold)Valor.moduleManager.modules.get(Scaffold.class)).isEnabled()) {
         return false;
      } else {
         return !this.weaponsOnlyProp.getValue() || ItemUtil.isHoldingWeapon() || this.allowToolsProp.getValue() && ItemUtil.isHoldingTool()
            ? !(Boolean)this.requirePressProp.getValue() || KeyUtil.isKeyDown(mc.gameSettings.keyBindAttack.getKeyCode())
            : false;
      }
   }

   private boolean isSmartOrAdvancedAutoBlock() {
      return (Boolean)this.smartAutoBlockProp.getValue() || this.isAdvancedAutoBlockMode();
   }

   private void blockWithSword() {
      ItemStack var1;
      if ((var1 = mc.thePlayer.getHeldItem()) != null && var1.getItem() instanceof ItemSword) {
         if (this.isVanillaAutoBlock() || this.shouldBlockChance()) {
            ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(var1));
            mc.thePlayer.setItemInUse(var1, var1.getMaxItemUseDuration());
            this.clientSideBlocking = true;
            this.blockReleasePending = true;
         }
      }
   }

   public final boolean isBlockingSword() {
      return (mc.thePlayer.isUsingItem() || this.clientSideBlocking) && ItemUtil.isHoldingSword();
   }

   private boolean isAdvancedAutoBlockMode() {
      int var1;
      return (var1 = (Integer)this.autoBlockProp.getValue()) >= 12 && var1 <= 15;
   }

   private boolean hasValidTargetInAutoBlockRange() {
      return mc.theWorld
         .loadedEntityList
         .stream()
         .anyMatch(var1 -> var1 instanceof EntityLivingBase && this.isWithinAutoBlockRange((EntityLivingBase)var1) && this.isValidTarget((EntityLivingBase)var1));
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled()) {
         switch (var1.getType()) {
            case PRE:
               if (this.currentTarget == null
                  || !this.isWithinAutoBlockRange(this.currentTarget.entity)
                  || !this.isWithinAttackRange(this.currentTarget.entity)
                  || !this.isWithinSwingRange(this.currentTarget.entity)
                  || this.attackTimer.hasTimeElapsed(((Integer)this.switchDelayProp.getValue()).longValue())) {
                  this.attackTimer.reset();
                  ArrayList<EntityLivingBase> var7 = new ArrayList<>();
                  Iterator var2 = mc.theWorld.loadedEntityList.iterator();

                  while (var2.hasNext()) {
                     Entity var3;
                     if ((var3 = (Entity)var2.next()) instanceof EntityLivingBase && this.isWithinAutoBlockRange((EntityLivingBase)var3)) {
                        EntityLivingBase var5 = (EntityLivingBase)var3;
                        if (this.isWithinAutoBlockRange(var5) || this.isWithinSwingRange(var5) || this.isWithinAttackRange(var5)) {
                           var7.add((EntityLivingBase)var3);
                        }
                     }
                  }

                  if (var7.isEmpty()) {
                     this.currentTarget = null;
                  } else {
                     if (var7.stream().anyMatch(this::isWithinSwingRange)) {
                        var7.removeIf(var1x -> !this.isWithinSwingRange(var1x));
                     }

                     if (var7.stream().anyMatch(this::isWithinAttackRange)) {
                        var7.removeIf(var1x -> !this.isWithinAttackRange(var1x));
                     }

                     if (var7.stream().anyMatch(this::isValidTarget)) {
                        var7.removeIf(var1x -> !this.isFriend(var1x));
                     }

                     var7.sort((var1x, var2x) -> {
                        int var3x = 0;
                        switch (this.sortProp.getValue()) {
                           case 1:
                              var3x = Float.compare(BotCheckUtil.getEffectiveHealth(var1x), BotCheckUtil.getEffectiveHealth(var2x));
                              break;
                           case 2:
                              var3x = Integer.compare(var1x.hurtResistantTime, var2x.hurtResistantTime);
                              break;
                           case 3:
                              var3x = Float.compare(RotationUtil.getAngleToEntity(var1x), RotationUtil.getAngleToEntity(var2x));
                        }

                        return var3x != 0 ? var3x : Double.compare(RotationUtil.getDistanceToTarget(var1x), RotationUtil.getDistanceToTarget(var2x));
                     });
                     if ((Integer)this.modeProp.getValue() == 1 && this.shouldSwitch) {
                        this.shouldSwitch = false;
                        this.switchIndex++;
                     }

                     if ((Integer)this.modeProp.getValue() == 0 || this.switchIndex >= var7.size()) {
                        this.switchIndex = 0;
                     }

                     EntityLivingBase var8 = (EntityLivingBase)var7.get(this.switchIndex);
                     if (this.currentTarget == null || this.currentTarget.entity != var8) {
                        this.updateAimOffsets(true);
                     }

                     this.currentTarget = new KillAura.TargetInfo(var8);
                  }
               }

               if (this.currentTarget != null) {
                  this.currentTarget = new KillAura.TargetInfo(this.currentTarget.entity);
                  return;
               }
               break;
            case POST:
               if (this.isBlockingSword() && !mc.thePlayer.isBlocking()) {
                  mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(), mc.thePlayer.getHeldItem().getMaxItemUseDuration());
               }

               ItemStack var6;
               if ((Integer)this.autoBlockProp.getValue() == 3
                  && this.autoBlockTargetFound
                  && ItemUtil.isHoldingSword()
                  && (var6 = mc.thePlayer.getHeldItem()) != null
                  && !mc.thePlayer.isBlocking()
                  && !mc.thePlayer.isUsingItem()) {
                  mc.thePlayer.setItemInUse(var6, var6.getMaxItemUseDuration());
               }
         }
      }
   }

   private void updateAimOffsets(boolean var1) {
      long var2 = System.currentTimeMillis();
      switch (this.aimModeProp.getValue()) {
         case 1:
            if (!var1 && var2 < this.nextAimRandomizeTimeMs) {
               return;
            }

            this.aimOffsetX = MathHelper.clamp_float(this.aimOffsetX + RandomUtil.nextFloat(-0.03F, 0.03F), 0.42F, 0.58F);
            this.aimOffsetY = MathHelper.clamp_float(this.aimOffsetY + RandomUtil.nextFloat(-0.02F, 0.02F), 0.62F, 0.78F);
            this.aimOffsetZ = MathHelper.clamp_float(this.aimOffsetZ + RandomUtil.nextFloat(-0.03F, 0.03F), 0.42F, 0.58F);
            this.nextAimRandomizeTimeMs = var2 + (long)RandomUtil.nextFloat(400.0F, 900.0F);
            return;
         case 2:
            if (var1 || var2 >= this.nextAimRandomizeTimeMs) {
               this.targetAimOffsetX = RandomUtil.nextFloat(0.35F, 0.65F);
               this.targetAimOffsetY = RandomUtil.nextFloat(0.6F, 0.8F);
               this.targetAimOffsetZ = RandomUtil.nextFloat(0.35F, 0.65F);
               this.nextAimRandomizeTimeMs = var2 + (long)RandomUtil.nextFloat(500.0F, 1000.0F);
            }

            this.aimOffsetX = this.aimOffsetX + (this.targetAimOffsetX - this.aimOffsetX) * 0.15F;
            this.aimOffsetY = this.aimOffsetY + (this.targetAimOffsetY - this.aimOffsetY) * 0.15F;
            this.aimOffsetZ = this.aimOffsetZ + (this.targetAimOffsetZ - this.aimOffsetZ) * 0.15F;
            return;
         case 3:
            if (!var1 && var2 < this.nextAimRandomizeTimeMs) {
               return;
            }

            this.aimOffsetX = RandomUtil.nextFloat(0.4F, 0.6F);
            this.aimOffsetY = RandomUtil.nextFloat(0.55F, 0.8F);
            this.aimOffsetZ = RandomUtil.nextFloat(0.4F, 0.6F);
            this.nextAimRandomizeTimeMs = var2 + (long)RandomUtil.nextFloat(200.0F, 450.0F);
            return;
         default:
            if (var1 || var2 >= this.nextAimRandomizeTimeMs) {
               this.aimOffsetX = RandomUtil.nextFloat(0.25F, 0.75F);
               this.aimOffsetY = RandomUtil.nextFloat(0.58F, 0.85F);
               this.aimOffsetZ = RandomUtil.nextFloat(0.25F, 0.75F);
               this.nextAimRandomizeTimeMs = var2 + (long)RandomUtil.nextFloat(180.0F, 420.0F);
            }
      }
   }

   private boolean isWithinSwingRange(EntityLivingBase var1) {
      return RotationUtil.getDistanceToTarget(var1) <= (double)((Float)this.swingRangeProp.getValue()).floatValue();
   }

   private long calculateNextClickDelay() {
      float var1 = this.isAutoBlocking ? (Float)this.autoBlockMinApsProp.getValue() : (Float)this.minApsProp.getValue();
      float var2 = this.isAutoBlocking ? (Float)this.autoBlockMaxApsProp.getValue() : (Float)this.maxApsProp.getValue();
      if (this.isVanillaAutoBlock()) {
         var1 = Math.max(6.0F, var1);
         var2 = Math.max(6.0F, var2);
      }

      if (var2 < var1) {
         var2 = var1;
      }

      var1 = MathHelper.clamp_float(RandomUtil.nextFloat(var1, var2) * RandomUtil.nextFloat(0.95F, 1.05F), var1, var2);
      long var3 = (long)(1000.0F / Math.max(var1, 0.1F));
      return this.isVanillaAutoBlock() ? Math.min(var3, 100L) : var3;
   }

   private static int getPlayerPing() {
      if (mc.thePlayer != null && mc.getNetHandler() != null) {
         NetworkPlayerInfo var0;
         return (var0 = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID())) == null ? 0 : Math.max(0, var0.getResponseTime());
      } else {
         return 0;
      }
   }

   @EventTarget
   public void onMove(MoveInputEvent var1) {
      if (this.isEnabled()) {
         if ((Integer)this.moveFixProp.getValue() == 1
            && (Integer)this.rotationsProp.getValue() != 3
            && MovementManager.isRotated()
            && MovementManager.getPriority() == 1.0F
            && MoveUtil.isMoving()) {
            MoveUtil.fixMovement(MovementManager.getMovementYaw());
         }

         if (this.isAutoBlockNoSlow()) {
            mc.thePlayer.movementInput.jump = false;
         }
      }
   }

   @EventTarget
   public void onRender(Render3DEvent var1) {
      if (this.isEnabled() && this.currentTarget != null && (Integer)this.showTargetProp.getValue() != 0 && BotCheckUtil.isEntityLoaded((Entity)this.currentTarget.entity) && this.canAttackWithItem()) {
         Color var2 = new Color(-1);
         switch (this.showTargetProp.getValue()) {
            case 1:
               if (this.currentTarget.entity.hurtTime > 0) {
                  var2 = new Color(16733525);
               } else {
                  var2 = new Color(5635925);
               }
               break;
            case 2:
               var2 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), 0L);
         }

         Render3DUtil.setupRender3D();
         Render3DUtil.drawEntityESP(this.currentTarget.entity, var2.getRed(), var2.getGreen(), var2.getBlue());
         Render3DUtil.endRender3D();
      }
   }

   @EventTarget
   public void onLeftClick(LeftClickMouseEvent var1) {
      if (this.isAutoBlocking) {
         var1.setCancelled(true);
      } else {
         if (this.isEnabled() && this.currentTarget != null && this.canAuraRotateAndAttack()) {
            var1.setCancelled(true);
         }
      }
   }

   @EventTarget
   public void onCancelUse(CancelUseEvent var1) {
      if (this.isAutoBlocking) {
         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onHitBlock(HitBlockEvent var1) {
      if (this.isAutoBlocking) {
         var1.setCancelled(true);
      } else {
         if (this.isEnabled() && this.currentTarget != null && this.canAuraRotateAndAttack()) {
            var1.setCancelled(true);
         }
      }
   }

   @EventTarget
   public void onRightClick(RightClickMouseEvent var1) {
      if (this.isAutoBlocking) {
         var1.setCancelled(true);
      } else {
         if (this.isEnabled() && this.currentTarget != null && this.canAuraRotateAndAttack()) {
            var1.setCancelled(true);
         }
      }
   }

public KillAura() {
      super("KillAura", false, ModuleCategory.COMBAT);
   }

   private boolean canAuraRotateAndAttack() {
      if ((Boolean)this.inventoryCheckProp.getValue() && mc.currentScreen instanceof GuiContainer) {
         return false;
      } else if (!(Boolean)this.weaponsOnlyProp.getValue() || ItemUtil.isHoldingWeapon() || (Boolean)this.allowToolsProp.getValue() && ItemUtil.isHoldingTool()) {
         if ((Boolean)this.allowMiningProp.getValue() && ReflectionUtil.isHittingBlock(mc.playerController)) {
            return false;
         } else if ((ItemUtil.isHoldingEdible() || ItemUtil.isHoldingBow()) && PlayerUtil.isUseItemPressed()) {
            return false;
         } else {
            AutoHeal var1;
            if ((var1 = (AutoHeal)Valor.moduleManager.modules.get(AutoHeal.class)).isEnabled() && var1.isHealing()) {
               return false;
            } else {
               BedNuker var3 = (BedNuker)Valor.moduleManager.modules.get(BedNuker.class);
               AutoBlockIn var2 = (AutoBlockIn)Valor.moduleManager.modules.get(AutoBlockIn.class);
               if (var3.isEnabled() && var3.isReady() && !this.canAttackTarget()) {
                  return false;
               } else if (Valor.moduleManager.modules.get(Scaffold.class).isEnabled()) {
                  return false;
               } else if (var2.isEnabled()) {
                  return false;
               } else {
                  return this.requirePressProp.getValue() ? PlayerUtil.isAttackPressed() : true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public final boolean canAttackTarget() {
      if (this.isEnabled() && this.currentTarget != null) {
         EntityLivingBase var1 = this.currentTarget.entity;
         Scaffold var2 = (Scaffold)Valor.moduleManager.modules.get(Scaffold.class);
         return BotCheckUtil.isEntityLoaded((Entity)var1)
            && this.isValidTarget(var1)
            && !var2.isEnabled()
            && (!(Boolean)this.requirePressProp.getValue() || KeyUtil.isKeyDown(mc.gameSettings.keyBindAttack.getKeyCode()))
            && (this.isWithinAttackRange(var1) || this.isBoundingBoxInAttackRange(this.currentTarget.boundingBox));
      } else {
         return false;
      }
   }

   public final boolean isClientSideBlocking() {
      return this.isVanillaBlocking() && this.clientSideBlocking;
   }

   public static class TargetInfo {
      public final EntityLivingBase entity;
      public final AxisAlignedBB boundingBox;
      public final double posX;
      public final double posY;
      public final double posZ;

      public TargetInfo(EntityLivingBase var1) {
         this.entity = var1;
         double var2 = (double)var1.getCollisionBorderSize();
         this.boundingBox = var1.getEntityBoundingBox().expand(var2, var2, var2);
         this.posX = var1.posX;
         this.posY = var1.posY;
         this.posZ = var1.posZ;
      }
   }
}
