package valor.module.combat;

import com.google.common.base.CaseFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0BPacketEntityAction.Action;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Keyboard;
import valor.Valor;
import valor.core.LagType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.KnockbackEvent;
import valor.events.LivingUpdateEvent;
import valor.events.LoadWorldEvent;
import valor.events.PacketEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.movement.LongJump;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.MoveUtil;
import valor.util.PacketUtil;
import valor.util.ReflectionUtil;
import valor.util.TimerUtil;

public class Velocity extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final Random RANDOM = new Random();
   private static final int MODE_CUSTOM = 0;
   private static final int MODE_JUMP = 1;
   private static final int MODE_DELAY = 2;
   private static final int MODE_REVERSE = 3;
   private static final int MODE_LEGIT = 4;
   private static final int MODE_HYPIXEL_BOOST = 5;
   private static final int MODE_GRIM_C07 = 6;
   private static final int MODE_BLOCKSMC = 7;
   private static final int MODE_MATRIX_SIMPLE = 8;
   private static final int MODE_INTAVE = 9;
   private static final int MODE_GRIM_REDUCE = 10;
   private static final int MODE_POLAR = 11;
   private static final int MODE_SENTINEL = 12;
   private static final int MODE_CANCEL = 13;
   private static final int MODE_VULCAN = 14;
   private static final int MODE_VULCAN_NEW = 15;
   private static final int MODE_DELAYED_PACKET = 16;
   private static final int MODE_TIMER_SYNC = 17;
   private static final int MODE_PREDICTION = 18;
   private int chanceAccumulator = 0;
   private int delayChanceAccumulator = 0;
   private boolean delayVelocityActive = false;
   private boolean fakeCheckPassed = true;
   private boolean jumpVelocityApplied = false;
   private boolean waitingForDelay = false;
   private boolean hypixelBoostJumped = false;
   private boolean hypixelBoostActive = false;
   private int hypixelBoostTicks = 0;
   private boolean grimReduceActive = false;
   private boolean blockSmcFlag = false;
   private boolean vulcanReduceActive = false;
   private boolean grimC07Active = false;
   private int grimReduceCount = 0;
   private int grimReduceTicks = 0;
   private int intaveReduceTicks = 0;
   private final TimerUtil grimPauseTimer = new TimerUtil();
   private final Map<Object, Long> delayedPacketsMap = new LinkedHashMap<>();
   private boolean sentinelVelocityActive = false;
   private boolean polarVelocityActive = false;
   private int polarTicks = 0;
   private double polarFallDistance = 0.0;
   private int predictionReduceTicks = -1;
   private int predictionTicksElapsed = 0;
   private int predictionBlendTicks = 0;
   private boolean predictionActive = false;
   private boolean predictionDelayWaiting = false;
   private int lastVelocityTick = -1;
   private boolean predictionJumpResetPending = false;
   public final ModeProperty modeProp = new ModeProperty(
      "mode",
      0,
      new String[]{
         "CUSTOM",
         "JUMP",
         "DELAY",
         "REVERSE",
         "LEGIT",
         "HYPIXEL_BOOST",
         "GRIM_C07",
         "BLOCKSMC",
         "MATRIX_SIMPLE",
         "INTAVE",
         "GRIM_REDUCE",
         "POLAR",
         "SENTINEL",
         "CANCEL",
         "VULCAN",
         "VULCAN_NEW",
         "DELAYED_PACKET",
         "TIMER_SYNC",
         "PREDICTION"
      }
   );
   public final IntProperty delayTicksProp = new IntProperty("delay-ticks", 3, 1, 20, () -> (Integer)this.modeProp.getValue() == 2);
   public final PercentageProperty delayChanceProp = new PercentageProperty("delay-chance", 100, () -> (Integer)this.modeProp.getValue() == 2);
   public final IntProperty delayedMsProp = new IntProperty("delayed-ms", 200, 50, 1000, () -> (Integer)this.modeProp.getValue() == 16);
   public final PercentageProperty chanceProp = new PercentageProperty("chance", 100, () -> (Integer)this.modeProp.getValue() == 0);
   public final PercentageProperty horizontalProp = new PercentageProperty("horizontal", 0, () -> (Integer)this.modeProp.getValue() == 0);
   public final PercentageProperty verticalProp = new PercentageProperty("vertical", 100, () -> (Integer)this.modeProp.getValue() == 0);
   public final PercentageProperty explosionsHorizontalProp = new PercentageProperty("explosions-horizontal", 100, () -> (Integer)this.modeProp.getValue() == 0);
   public final PercentageProperty explosionsVerticalProp = new PercentageProperty("explosions-vertical", 100, () -> (Integer)this.modeProp.getValue() == 0);
   public final BooleanProperty fakeCheckProp = new BooleanProperty("fake-check", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 0);
   public final BooleanProperty onlyGroundProp = new BooleanProperty("only-ground", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 0);
   public final BooleanProperty onlyCombatProp = new BooleanProperty("only-combat", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 0);
   public final BooleanProperty noFireProp = new BooleanProperty("no-fire", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 0);
   public final ModeProperty overrideDirectionProp = new ModeProperty("override-direction", 0, new String[]{"NONE", "HARD", "OFFSET"}, () -> (Integer)this.modeProp.getValue() == 0);
   public final FloatProperty overrideYawProp = new FloatProperty(
      "override-yaw", 0.0F, -180.0F, 180.0F, () -> (Integer)this.modeProp.getValue() == 0 && (Integer)this.overrideDirectionProp.getValue() != 0
   );
   public final IntProperty chanceProp2 = new IntProperty("chance", 80, 0, 100, () -> (Integer)this.modeProp.getValue() == 1);
   public final BooleanProperty requireMouseDownProp = new BooleanProperty("require-mouse-down", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 1);
   public final BooleanProperty requireForwardProp = new BooleanProperty("require-forward", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 1);
   public final BooleanProperty requireAimProp = new BooleanProperty("require-aim", Boolean.TRUE, () -> (Integer)this.modeProp.getValue() == 1);
   public final BooleanProperty cancelHorizontalProp = new BooleanProperty("cancel-horizontal", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 13);
   public final BooleanProperty cancelVerticalProp = new BooleanProperty("cancel-vertical", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 13);
   public final FloatProperty reduceAmountProp = new FloatProperty(
      "reduce-amount", 0.6F, 0.0F, 1.0F, () -> (Integer)this.modeProp.getValue() == 14 || (Integer)this.modeProp.getValue() == 15
   );
   public final IntProperty grimC07PauseProp = new IntProperty("grim-c07-pause", 50, 0, 5000, () -> (Integer)this.modeProp.getValue() == 6);
   public final PercentageProperty predictionHorizontalProp = new PercentageProperty("prediction-horizontal", 100, () -> (Integer)this.modeProp.getValue() == 18);
   public final PercentageProperty predictionVerticalProp = new PercentageProperty("prediction-vertical", 100, () -> (Integer)this.modeProp.getValue() == 18);
   public final IntProperty predictionBlendTicksProp = new IntProperty("prediction-blend-ticks", 6, 2, 10, () -> (Integer)this.modeProp.getValue() == 18);
   public final FloatProperty predictionReduceMotionProp = new FloatProperty("prediction-reduce-motion", 1.0F, 0.0F, 1.0F, () -> (Integer)this.modeProp.getValue() == 18);
   public final IntProperty predictionReduceTicksProp = new IntProperty("prediction-reduce-ticks", 0, 0, 5, () -> (Integer)this.modeProp.getValue() == 18);
   public final ModeProperty predictionReduceModeProp = new ModeProperty(
      "prediction-reduce-mode", 0, new String[]{"DISABLED", "NORMAL", "AIR_PUSH"}, () -> (Integer)this.modeProp.getValue() == 18
   );
   public final BooleanProperty predictionJumpResetProp = new BooleanProperty("prediction-jump-reset", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 18);
   public final BooleanProperty predictionDelayProp = new BooleanProperty("prediction-delay", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 18);
   public final IntProperty predictionDelayTicksProp = new IntProperty("prediction-delay-ticks", 4, 1, 15, () -> (Integer)this.modeProp.getValue() == 18 && (Boolean)this.predictionDelayProp.getValue());
   public final FloatProperty predictionDelayRangeProp = new FloatProperty(
      "prediction-delay-range", 3.0F, 1.0F, 4.0F, () -> (Integer)this.modeProp.getValue() == 18 && (Boolean)this.predictionDelayProp.getValue()
   );
   public final BooleanProperty predictionDelayUntilGroundProp = new BooleanProperty(
      "prediction-delay-until-ground", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 18 && (Boolean)this.predictionDelayProp.getValue()
   );
   public final BooleanProperty predictionReverseProp = new BooleanProperty("prediction-reverse", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 18);

@Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (var1.getType() == EventType.POST) {
         int var14;
         if ((var14 = (Integer)this.modeProp.getValue()) == 18) {
            boolean var4 = false;
            if (this.predictionActive) {
               this.predictionTicksElapsed++;
               int var5 = this.predictionDelayProp.getValue() ? (Integer)this.predictionDelayTicksProp.getValue() : 0;
               if (!this.predictionDelayWaiting && (Boolean)this.predictionDelayUntilGroundProp.getValue() && !mc.thePlayer.onGround) {
                  var5 = Math.max(var5, this.predictionTicksElapsed);
               }

               int var3 = (Integer)this.predictionBlendTicksProp.getValue();
               if (this.predictionTicksElapsed > var5 && this.predictionBlendTicks < var3) {
                  double var6 = Math.max(0.01, (double)((Integer)this.predictionHorizontalProp.getValue()).intValue() / 100.0);
                  double var8 = Math.max(0.01, (double)((Integer)this.predictionVerticalProp.getValue()).intValue() / 100.0);
                  if ((Boolean)this.predictionReverseProp.getValue()) {
                     var6 = Math.max(0.01, var6 * 0.75);
                  }

                  double var10 = Math.pow(var6, 1.0 / (double)var3);
                  double var12 = Math.pow(var8, 1.0 / (double)var3);
                  mc.thePlayer.motionX *= var10;
                  mc.thePlayer.motionZ *= var10;
                  if (mc.thePlayer.motionY > 0.0) {
                     mc.thePlayer.motionY *= var12;
                  }

                  this.predictionBlendTicks++;
                  if (this.predictionBlendTicks >= var3) {
                     this.predictionActive = false;
                     this.predictionDelayWaiting = false;
                     var4 = true;
                  }
               }
            }

            if (!this.predictionActive && !var4 && this.predictionReduceTicks >= 0 && this.predictionReduceTicks < (Integer)this.predictionReduceTicksProp.getValue() && mc.currentScreen == null) {
               this.predictionReduceTicks++;
               if ((Integer)this.predictionReduceModeProp.getValue() != 2 || !mc.thePlayer.onGround) {
                  mc.thePlayer.motionX = mc.thePlayer.motionX * (double)((Float)this.predictionReduceMotionProp.getValue()).floatValue();
                  mc.thePlayer.motionZ = mc.thePlayer.motionZ * (double)((Float)this.predictionReduceMotionProp.getValue()).floatValue();
               }
            }

            if (this.predictionJumpResetPending && mc.thePlayer.onGround) {
               this.predictionJumpResetPending = false;
               if (mc.thePlayer.isSprinting() && mc.thePlayer.movementInput.moveForward >= 0.8F && !hasBadEffects() && !isInLiquidOrWeb()) {
                  mc.thePlayer.movementInput.jump = true;
               }
            }
         }

         if (this.waitingForDelay && (isOnGroundNoSlow() || isInLiquidOrWeb() || Valor.lagManager.lagTicks >= (long)((Integer)this.delayTicksProp.getValue()).intValue())) {
            Valor.lagManager.setLagging(false, LagType.LAG_RANGE);
            this.waitingForDelay = false;
         }

         if (this.hypixelBoostJumped) {
            MoveUtil.strafe(MoveUtil.getMovementSpeed(), MoveUtil.getMoveYaw());
            this.hypixelBoostJumped = false;
         }

         if (var14 == 4) {
            int var2 = mc.thePlayer.hurtTime;
            if (mc.thePlayer.hurtTime >= 8) {
               if (this.hypixelBoostTicks <= 0) {
                  this.hypixelBoostActive = true;
                  this.hypixelBoostTicks = 2;
               }
            } else if (var2 <= 1) {
               this.hypixelBoostActive = false;
               this.hypixelBoostTicks = 0;
            }

            if (this.hypixelBoostActive && mc.thePlayer.onGround && this.hypixelBoostTicks <= 0) {
               mc.thePlayer.jump();
               this.hypixelBoostActive = false;
            }

            if (this.hypixelBoostTicks > 0) {
               this.hypixelBoostTicks--;
            }
         }

         BlockPos var15;
         if (var14 == 6 && !this.grimPauseTimer.hasTimeElapsed((long)((Integer)this.grimC07PauseProp.getValue()).intValue())
            || var14 == 6 && this.grimC07Active && (sendGrimDigPacket(var15 = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) || sendGrimDigPacket(var15.up()))) {
            this.grimC07Active = false;
         }

         if (var14 == 5 && mc.thePlayer.hurtTime == 8) {
            MoveUtil.strafe(MoveUtil.getMovementSpeed() * 0.7, MoveUtil.getMoveYaw());
         }

         if (var14 == 10 && this.grimReduceActive && mc.thePlayer.hurtTime == 2) {
            this.grimReduceTicks++;
            if (mc.thePlayer.onGround && this.grimReduceCount % 2 == 0 && this.grimReduceTicks <= 10) {
               mc.thePlayer.jump();
               this.grimReduceCount = 0;
            }

            this.grimReduceActive = false;
         }

         if (var14 == 9 && mc.thePlayer.hurtTime == 9 && ++this.intaveReduceTicks % 2 == 0 && mc.thePlayer.onGround && mc.thePlayer.isSprinting()) {
            mc.thePlayer.movementInput.jump = true;
            this.intaveReduceTicks = 0;
         }

         if (var14 == 14 && mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && this.vulcanReduceActive) {
            mc.thePlayer.motionX = mc.thePlayer.motionX * (double)((Float)this.reduceAmountProp.getValue()).floatValue();
            mc.thePlayer.motionZ = mc.thePlayer.motionZ * (double)((Float)this.reduceAmountProp.getValue()).floatValue();
         }

         if (var14 == 14 && this.vulcanReduceActive && (mc.thePlayer.hurtTime < 4 || mc.thePlayer.onGround)) {
            this.vulcanReduceActive = false;
         }

         if (var14 == 15 && mc.thePlayer.hurtTime > 1 && this.vulcanReduceActive) {
            mc.thePlayer.motionX = mc.thePlayer.motionX * (double)((Float)this.reduceAmountProp.getValue()).floatValue();
            mc.thePlayer.motionZ = mc.thePlayer.motionZ * (double)((Float)this.reduceAmountProp.getValue()).floatValue();
         }

         if (var14 == 15 && this.vulcanReduceActive && (mc.thePlayer.hurtTime < 5 || mc.thePlayer.onGround)) {
            this.vulcanReduceActive = false;
         }

         if (var14 == 11 && mc.thePlayer.hurtTime == 9) {
            mc.thePlayer.motionX *= 0.6;
            mc.thePlayer.motionZ *= 0.6;
         }

         if (var14 == 12 && mc.thePlayer.hurtTime == 9) {
            mc.thePlayer.motionX *= 0.5;
            mc.thePlayer.motionZ *= 0.5;
            mc.thePlayer.motionY *= 0.8;
         }

         if (var14 == 16) {
            long var17 = System.currentTimeMillis();
            Iterator var16 = this.delayedPacketsMap.entrySet().iterator();

            while (var16.hasNext()) {
               Entry var18;
               if ((Long)(var18 = (Entry)var16.next()).getValue() <= var17) {
                  Object var7;
                  if ((var7 = var18.getKey()) instanceof S12PacketEntityVelocity) {
                     ((S12PacketEntityVelocity)var7).processPacket((INetHandlerPlayClient)mc.getNetHandler());
                  } else if (var7 instanceof S32PacketConfirmTransaction) {
                     ((S32PacketConfirmTransaction)var7).processPacket((INetHandlerPlayClient)mc.getNetHandler());
                  }

                  var16.remove();
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled()) {
         int var2 = (Integer)this.modeProp.getValue();
         if (var1.getType() == EventType.SEND && var2 == 7 && this.blockSmcFlag) {
            if (var1.getPacket() instanceof C0BPacketEntityAction) {
               this.blockSmcFlag = false;
               var1.setCancelled(true);
            }
         } else if (var1.getType() == EventType.RECEIVE && !var1.isCancelled()) {
            if (var1.getPacket() instanceof S08PacketPlayerPosLook && var2 == 6) {
               this.grimPauseTimer.reset();
               this.grimC07Active = false;
            }

            S19PacketEntityStatus var3;
            Entity var4;
            if (var1.getPacket() instanceof S19PacketEntityStatus
               && (var4 = (var3 = (S19PacketEntityStatus)var1.getPacket()).getEntity(mc.theWorld)) != null
               && var4.equals(mc.thePlayer)
               && var3.getOpCode() == 2) {
               this.fakeCheckPassed = false;
            }

            if (var1.getPacket() instanceof S12PacketEntityVelocity) {
               S12PacketEntityVelocity var16;
               if ((var16 = (S12PacketEntityVelocity)var1.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                  if (this.shouldApplyVelocity()) {
                     LongJump var18 = (LongJump)Valor.moduleManager.modules.get(LongJump.class);
                     if (var2 == 2 && !this.waitingForDelay && !isOnGroundNoSlow() && !isInLiquidOrWeb() && !this.delayVelocityActive && (!this.fakeCheckPassed || !this.isFakeCheckEnabled()) && (!var18.isEnabled() || !var18.canTakeKnockback())) {
                        this.delayChanceAccumulator = this.delayChanceAccumulator % 100 + (Integer)this.delayChanceProp.getValue();
                        if (this.delayChanceAccumulator >= 100) {
                           Valor.lagManager.setLagging(true, LagType.LAG_RANGE);
                           Valor.lagManager.packetQueue.offer(var16);
                           var1.setCancelled(true);
                           this.waitingForDelay = true;
                           return;
                        }
                     }

                     int var7;
                     if (var2 == 0 && (var7 = (Integer)this.overrideDirectionProp.getValue()) != 0) {
                        double var8 = (double)var16.getMotionX() / 8000.0;
                        double var10 = (double)var16.getMotionZ() / 8000.0;
                        if (var7 == 1) {
                           float var5 = (float)Math.toRadians((double)((Float)this.overrideYawProp.getValue()).floatValue());
                           ReflectionUtil.setMotionX(var16, (int)(-Math.sin((double)var5) * Math.hypot(var8, var10) * 8000.0));
                           ReflectionUtil.setMotionZ(var16, (int)(Math.cos((double)var5) * Math.hypot(var8, var10) * 8000.0));
                        } else {
                           float var19 = (float)Math.toRadians((double)(mc.thePlayer.rotationYaw + (Float)this.overrideYawProp.getValue()));
                           ReflectionUtil.setMotionX(var16, (int)(-Math.sin((double)var19) * Math.hypot(var8, var10) * 8000.0));
                           ReflectionUtil.setMotionZ(var16, (int)(Math.cos((double)var19) * Math.hypot(var8, var10) * 8000.0));
                        }
                     }

                     switch (var2) {
                        case 6:
                           if (!this.grimPauseTimer.hasTimeElapsed((long)((Integer)this.grimC07PauseProp.getValue()).intValue())) {
                              this.grimC07Active = false;
                              return;
                           }

                           var1.setCancelled(true);
                           this.grimC07Active = true;
                           return;
                        case 7:
                           this.blockSmcFlag = true;
                           var1.setCancelled(true);
                           PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, Action.START_SNEAKING));
                           PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, Action.STOP_SNEAKING));
                           return;
                        case 8:
                           ReflectionUtil.setMotionX(var16, (int)((double)var16.getMotionX() * 0.36));
                           ReflectionUtil.setMotionZ(var16, (int)((double)var16.getMotionZ() * 0.36));
                           if (mc.thePlayer.onGround) {
                              ReflectionUtil.setMotionX(var16, (int)((double)var16.getMotionX() * 0.9));
                              ReflectionUtil.setMotionZ(var16, (int)((double)var16.getMotionZ() * 0.9));
                              return;
                           }
                           break;
                        case 9:
                        case 10:
                           this.grimReduceActive = true;
                           this.grimReduceCount++;
                           return;
                        case 11:
                        case 12:
                        case 17:
                        default:
                           break;
                        case 13:
                           var1.setCancelled(true);
                           if (!(Boolean)this.cancelVerticalProp.getValue()) {
                              mc.thePlayer.motionY = (double)var16.getMotionY() / 8000.0;
                           }

                           if (!(Boolean)this.cancelHorizontalProp.getValue()) {
                              mc.thePlayer.motionX = (double)var16.getMotionX() / 8000.0;
                              mc.thePlayer.motionZ = (double)var16.getMotionZ() / 8000.0;
                              return;
                           }
                           break;
                        case 14:
                           this.vulcanReduceActive = true;
                           ReflectionUtil.setMotionX(var16, (int)((float)var16.getMotionX() * (Float)this.reduceAmountProp.getValue()));
                           ReflectionUtil.setMotionZ(var16, (int)((float)var16.getMotionZ() * (Float)this.reduceAmountProp.getValue()));
                           return;
                        case 15:
                           this.vulcanReduceActive = true;
                           return;
                        case 16:
                           var1.setCancelled(true);
                           this.delayedPacketsMap.put(var16, System.currentTimeMillis() + (long)((Integer)this.delayedMsProp.getValue()).intValue());
                           return;
                        case 18:
                           if (var16.getMotionY() > 0 && !var18.isEnabled()) {
                              int var12 = mc.thePlayer.ticksExisted;
                              var2 = this.lastVelocityTick < 0 ? Integer.MAX_VALUE : var12 - this.lastVelocityTick;
                              int var17 = Math.max(10, (this.predictionDelayProp.getValue() ? (Integer)this.predictionDelayTicksProp.getValue() : 0) + (Integer)this.predictionBlendTicksProp.getValue());
                              boolean var14 = var2 >= 0 && var2 <= var17;
                              this.predictionActive = true;
                              this.predictionDelayWaiting = var14;
                              this.lastVelocityTick = var12;
                              this.predictionTicksElapsed = var14 ? (this.predictionDelayProp.getValue() ? (Integer)this.predictionDelayTicksProp.getValue() + 1 : 1) : 0;
                              this.predictionBlendTicks = 0;
                              if ((Integer)this.predictionReduceTicksProp.getValue() > 0) {
                                 this.predictionReduceTicks = 0;
                              }

                              if ((Boolean)this.predictionJumpResetProp.getValue() && mc.thePlayer.onGround && mc.thePlayer.isSprinting()) {
                                 this.predictionJumpResetPending = true;
                              }
                           }
                     }
                  }
               }
            } else {
               S27PacketExplosion var15;
               if (var1.getPacket() instanceof S27PacketExplosion
                  && ((var15 = (S27PacketExplosion)var1.getPacket()).func_149149_c() != 0.0F || var15.func_149144_d() != 0.0F || var15.func_149147_e() != 0.0F)
                  )
                {
                  if (var2 == 6 && this.grimPauseTimer.hasTimeElapsed((long)((Integer)this.grimC07PauseProp.getValue()).intValue())) {
                     var1.setCancelled(true);
                     this.grimC07Active = true;
                  } else if (var2 == 8) {
                     var1.setCancelled(true);
                     return;
                  }

                  this.delayVelocityActive = true;
                  if (var2 == 0 && ((Integer)this.explosionsHorizontalProp.getValue() == 0 || (Integer)this.explosionsVerticalProp.getValue() == 0)) {
                     var1.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   private static boolean sendGrimDigPacket(BlockPos var0) {
      if (!mc.theWorld.isAirBlock(var0)) {
         return false;
      } else {
         PacketUtil.sendPacket(new C07PacketPlayerDigging(net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, var0, EnumFacing.DOWN));
         return true;
      }
   }

   private static boolean isOnGroundNoSlow() {
      KillAura var0 = (KillAura)Valor.moduleManager.modules.get(KillAura.class);
      return mc.thePlayer.onGround && (!var0.isEnabled() || !var0.isAutoBlockNoSlow());
   }

   private boolean shouldApplyVelocity() {
      if (mc.thePlayer == null) {
         return false;
      } else if ((Integer)this.modeProp.getValue() == 0) {
         if ((Boolean)this.onlyGroundProp.getValue() && !mc.thePlayer.onGround) {
            return false;
         } else {
            if ((Boolean)this.onlyCombatProp.getValue()) {
               Iterator var1 = mc.theWorld.loadedEntityList.iterator();

               boolean var10000;
               while (true) {
                  if (!var1.hasNext()) {
                     var10000 = false;
                     break;
                  }

                  Entity var2;
                  if ((var2 = (Entity)var1.next()) instanceof EntityPlayer
                     && var2 != mc.thePlayer
                     && !var2.isDead
                     && mc.thePlayer.getDistanceToEntity(var2) <= 8.0F) {
                     var10000 = true;
                     break;
                  }
               }

               if (!var10000) {
                  return false;
               }
            }

            return this.noFireProp.getValue() && mc.thePlayer.isBurning() ? false : RANDOM.nextInt(100) < (Integer)this.chanceProp.getValue();
         }
      } else {
         return true;
      }
   }

   private static boolean isInLiquidOrWeb() {
      return mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || ReflectionUtil.isInWeb(mc.thePlayer);
   }

   private static boolean hasBadEffects() {
      PotionEffect var0 = mc.thePlayer.getActivePotionEffect(Potion.jump);
      PotionEffect var1 = mc.thePlayer.getActivePotionEffect(Potion.poison);
      PotionEffect var2 = mc.thePlayer.getActivePotionEffect(Potion.wither);
      return var0 != null || var1 != null || var2 != null;
   }

   private boolean isFakeCheckEnabled() {
      return (Integer)this.modeProp.getValue() == 0 && (Boolean)this.fakeCheckProp.getValue();
   }

   @Override
   public void onDisabled() {
      if (this.sentinelVelocityActive) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
         this.sentinelVelocityActive = false;
      }

      this.delayVelocityActive = false;
      this.fakeCheckPassed = true;
      this.hypixelBoostActive = false;
      this.hypixelBoostTicks = 0;
      this.grimReduceActive = false;
      this.blockSmcFlag = false;
      this.vulcanReduceActive = false;
      this.grimC07Active = false;
      this.grimReduceCount = 0;
      this.grimReduceTicks = 0;
      this.intaveReduceTicks = 0;
      this.polarVelocityActive = false;
      this.polarTicks = 0;
      this.polarFallDistance = 0.0;
      this.predictionReduceTicks = -1;
      this.predictionTicksElapsed = 0;
      this.predictionBlendTicks = 0;
      this.predictionActive = false;
      this.predictionDelayWaiting = false;
      this.lastVelocityTick = -1;
      this.predictionJumpResetPending = false;
      this.delayedPacketsMap.clear();
      Valor.lagManager.setLagging(false, LagType.LAG_RANGE);
   }

   @EventTarget
   public void onLivingUpdate(LivingUpdateEvent var1) {
      if (this.jumpVelocityApplied) {
         this.jumpVelocityApplied = false;
         if (mc.thePlayer.onGround && mc.thePlayer.isSprinting() && !mc.thePlayer.isPotionActive(Potion.jump) && !isInLiquidOrWeb()) {
            mc.thePlayer.movementInput.jump = true;
         }
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && (Integer)this.modeProp.getValue() == 1 && mc.thePlayer != null) {
         if (var1.getType() == EventType.PRE) {
            int var12 = mc.thePlayer.hurtTime;
            boolean var2 = mc.thePlayer.onGround;
            if (mc.thePlayer.onGround && this.polarFallDistance > 3.0 && !mc.thePlayer.capabilities.allowFlying) {
               this.polarVelocityActive = true;
            }

             if (var12 > this.polarTicks) {
                boolean var3 = mc.gameSettings.keyBindAttack.isKeyDown() || !(Boolean)this.requireMouseDownProp.getValue();
                boolean var4 = true;
                if ((Boolean)this.requireAimProp.getValue()) {
                   MovingObjectPosition var8 = mc.objectMouseOver;
                   if (var8 == null || var8.typeOfHit != MovingObjectType.ENTITY || !(var8.entityHit instanceof EntityPlayer)) {
                      var4 = false;
                   }
                }
               boolean var5 = mc.gameSettings.keyBindForward.isKeyDown() || !(Boolean)this.requireForwardProp.getValue();
               boolean var6 = (Integer)this.chanceProp2.getValue() == 100 || Math.random() * 100.0 < (double)((Integer)this.chanceProp2.getValue()).intValue();
               double var13 = mc.thePlayer.motionX;
               double var10 = mc.thePlayer.motionZ;
               boolean var7 = var13 * var13 + var10 * var10 < 1.0E-4
                  ? true
                  : Math.abs(MathHelper.wrapAngleTo180_float((float)(Math.toDegrees(Math.atan2(var10, var13)) - 90.0) - mc.thePlayer.rotationYaw)) <= 165.0F;
               if (!this.polarVelocityActive && !mc.thePlayer.isBurning() && var2 && var4 && var5 && var3 && var6 && !hasBadEffects() && var7) {
                  KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), this.sentinelVelocityActive = true);
               }

               this.polarVelocityActive = false;
            }

            this.polarTicks = var12;
            this.polarFallDistance = (double)mc.thePlayer.fallDistance;
         } else {
            if (var1.getType() == EventType.POST && this.sentinelVelocityActive && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
               KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), this.sentinelVelocityActive = false);
            }
         }
      }
   }

   @EventTarget
   public void onLoadWorld(LoadWorldEvent var1) {
      this.onDisabled();
   }

   @EventTarget
   public void onKnockback(KnockbackEvent var1) {
      if (this.isEnabled() && !var1.isCancelled()) {
         int var2;
         if ((var2 = (Integer)this.modeProp.getValue()) != 6 && var2 != 7 && var2 != 13 && var2 != 16 && var2 != 18) {
            if (!this.fakeCheckPassed || !this.isFakeCheckEnabled()) {
               this.fakeCheckPassed = true;
               if (this.delayVelocityActive && var2 == 0) {
                  this.delayVelocityActive = false;
                  if ((Integer)this.explosionsHorizontalProp.getValue() > 0) {
                     var1.setX(var1.getX() * (double)((Integer)this.explosionsHorizontalProp.getValue()).intValue() / 100.0);
                     var1.setZ(var1.getZ() * (double)((Integer)this.explosionsHorizontalProp.getValue()).intValue() / 100.0);
                  } else {
                     var1.setX(mc.thePlayer.motionX);
                     var1.setZ(mc.thePlayer.motionZ);
                  }

                  if ((Integer)this.explosionsVerticalProp.getValue() > 0) {
                     var1.setY(var1.getY() * (double)((Integer)this.explosionsVerticalProp.getValue()).intValue() / 100.0);
                     return;
                  }

                  var1.setY(mc.thePlayer.motionY);
                  return;
               }

               if (this.shouldApplyVelocity()) {
                  this.chanceAccumulator = this.chanceAccumulator % 100 + (Integer)this.chanceProp.getValue();
                  if (this.chanceAccumulator >= 100) {
                     this.jumpVelocityApplied = var2 == 2 && var1.getY() > 0.0;
                     this.hypixelBoostJumped = var2 == 3;
                     if (var2 == 0 || var2 == 2 || var2 == 3) {
                        if ((Integer)this.horizontalProp.getValue() > 0) {
                           var1.setX(var1.getX() * (double)((Integer)this.horizontalProp.getValue()).intValue() / 100.0);
                           var1.setZ(var1.getZ() * (double)((Integer)this.horizontalProp.getValue()).intValue() / 100.0);
                        } else {
                           var1.setX(mc.thePlayer.motionX);
                           var1.setZ(mc.thePlayer.motionZ);
                        }

                        if ((Integer)this.verticalProp.getValue() > 0) {
                           var1.setY(var1.getY() * (double)((Integer)this.verticalProp.getValue()).intValue() / 100.0);
                           return;
                        }

                        var1.setY(mc.thePlayer.motionY);
                     }
                  }
               }
            }
         }
      } else {
         this.delayVelocityActive = false;
         this.fakeCheckPassed = true;
      }
   }

   public Velocity() {
      super("Velocity", false, ModuleCategory.COMBAT);
   }
}
