package valor.module.combat;

import java.awt.Color;

import java.util.Queue;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;

import net.minecraft.entity.Entity;

import net.minecraft.entity.EntityLivingBase;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.network.INetHandler;

import net.minecraft.network.Packet;

import net.minecraft.network.play.client.C07PacketPlayerDigging;

import net.minecraft.network.play.client.C12PacketUpdateSign;

import net.minecraft.network.play.client.C19PacketResourcePackStatus;

import net.minecraft.network.play.server.S06PacketUpdateHealth;

import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import net.minecraft.network.play.server.S12PacketEntityVelocity;

import net.minecraft.network.play.server.S27PacketExplosion;

import net.minecraft.util.AxisAlignedBB;

import net.minecraft.util.MathHelper;

import valor.event.EventTarget;

import valor.event.types.EventType;

import valor.events.AttackEvent;

import valor.events.LoadWorldEvent;

import valor.events.PacketEvent;

import valor.events.Render3DEvent;

import valor.events.TickEvent;

import valor.module.Module;

import valor.module.ModuleCategory;

import valor.setting.BooleanProperty;

import valor.setting.FloatProperty;

import valor.setting.IntProperty;

import valor.setting.ModeProperty;

import valor.util.ReflectionUtil;

import valor.util.Render3DUtil;

public class TimerRange extends Module {

   private static final Minecraft mc = Minecraft.getMinecraft();

   private static final int MODE_NORMAL = 0;

   private static final int MODE_SMART = 1;

   private static final int MODE_MODERN = 2;

   public final ModeProperty modeProp = new ModeProperty("mode", 2, new String[]{"NORMAL", "SMART", "MODERN"});

   public final IntProperty normalTicksProp = new IntProperty("normal-ticks", 10, 1, 20, () -> this.isMode(0));

   public final FloatProperty normalBoostProp = new FloatProperty("normal-boost", 1.5F, 0.05F, 5.0F, () -> this.isMode(0));

   public final FloatProperty normalChargedProp = new FloatProperty("normal-charged", 0.45F, 0.05F, 2.0F, () -> this.isMode(0));

   public final FloatProperty normalBoostEndProp = new FloatProperty("normal-boost-end", 0.52F, 0.1F, 1.0F, () -> this.isMode(0));

   public final FloatProperty normalChargeEndProp = new FloatProperty("normal-charge-end", 0.84F, 0.1F, 1.0F, () -> this.isMode(0));

   public final FloatProperty normalRangeProp = new FloatProperty("normal-range", 3.5F, 1.0F, 5.0F, () -> this.isMode(0));

   public final IntProperty normalCooldownProp = new IntProperty("normal-cooldown", 10, 1, 50, () -> this.isMode(0));

   public final BooleanProperty normalForwardOnlyProp = new BooleanProperty("normal-forward-only", Boolean.TRUE, () -> this.isMode(0));

   public final BooleanProperty normalBlinkProp = new BooleanProperty("normal-blink", Boolean.FALSE, () -> this.isMode(0));

   public final IntProperty smartTicksProp = new IntProperty("smart-ticks", 10, 1, 20, () -> this.isMode(1));

   public final FloatProperty smartBoostProp = new FloatProperty("smart-boost", 1.5F, 0.05F, 5.0F, () -> this.isMode(1));

   public final FloatProperty smartChargedProp = new FloatProperty("smart-charged", 0.45F, 0.05F, 2.0F, () -> this.isMode(1));

   public final FloatProperty smartBoostEndProp = new FloatProperty("smart-boost-end", 0.52F, 0.1F, 1.0F, () -> this.isMode(1));

   public final FloatProperty smartChargeEndProp = new FloatProperty("smart-charge-end", 0.84F, 0.1F, 1.0F, () -> this.isMode(1));

   public final FloatProperty smartMinRangeProp = new FloatProperty("smart-min-range", 2.5F, 1.0F, 8.0F, () -> this.isMode(1));

   public final FloatProperty smartMaxRangeProp = new FloatProperty("smart-max-range", 3.0F, 1.0F, 8.0F, () -> this.isMode(1));

   public final IntProperty smartMinDelayProp = new IntProperty("smart-min-delay", 30, 1, 200, () -> this.isMode(1));

   public final IntProperty smartMaxDelayProp = new IntProperty("smart-max-delay", 60, 1, 200, () -> this.isMode(1));

   public final IntProperty smartClientPredictionProp = new IntProperty("smart-client-prediction", 2, 0, 5, () -> this.isMode(1));

   public final FloatProperty smartEnemyPredictionProp = new FloatProperty("smart-enemy-prediction", 1.5F, -1.0F, 2.0F, () -> this.isMode(1));

   public final BooleanProperty smartForwardOnlyProp = new BooleanProperty("smart-forward-only", Boolean.TRUE, () -> this.isMode(1));

   public final BooleanProperty smartBlinkProp = new BooleanProperty("smart-blink", Boolean.FALSE, () -> this.isMode(1));

   public final IntProperty modernTicksProp = new IntProperty("modern-ticks", 10, 1, 20, () -> this.isMode(2));

   public final FloatProperty modernBoostProp = new FloatProperty("modern-boost", 1.5F, 0.05F, 5.0F, () -> this.isMode(2));

   public final FloatProperty modernChargedProp = new FloatProperty("modern-charged", 0.45F, 0.05F, 2.0F, () -> this.isMode(2));

   public final FloatProperty modernBoostEndProp = new FloatProperty("modern-boost-end", 0.52F, 0.1F, 1.0F, () -> this.isMode(2));

   public final FloatProperty modernChargeEndProp = new FloatProperty("modern-charge-end", 0.84F, 0.1F, 1.0F, () -> this.isMode(2));

   public final FloatProperty modernMinRangeProp = new FloatProperty("modern-min-range", 2.5F, 1.0F, 8.0F, () -> this.isMode(2));

   public final FloatProperty modernMaxRangeProp = new FloatProperty("modern-max-range", 3.0F, 1.0F, 8.0F, () -> this.isMode(2));

   public final FloatProperty modernScanRangeProp = new FloatProperty("modern-scan-range", 8.0F, 2.0F, 12.0F, () -> this.isMode(2));

   public final IntProperty modernMinDelayProp = new IntProperty("modern-min-delay", 30, 1, 200, () -> this.isMode(2));

   public final IntProperty modernMaxDelayProp = new IntProperty("modern-max-delay", 60, 1, 200, () -> this.isMode(2));

   public final IntProperty modernClientPredictionProp = new IntProperty("modern-client-prediction", 2, 0, 5, () -> this.isMode(2));

   public final FloatProperty modernEnemyPredictionProp = new FloatProperty("modern-enemy-prediction", 1.5F, -1.0F, 2.0F, () -> this.isMode(2));

   public final FloatProperty modernMaxAngleProp = new FloatProperty("modern-max-angle", 5.0F, 5.0F, 90.0F, () -> this.isMode(2));

   public final BooleanProperty modernForwardOnlyProp = new BooleanProperty("modern-forward-only", Boolean.TRUE, () -> this.isMode(2));

   public final BooleanProperty modernBlinkProp = new BooleanProperty("modern-blink", Boolean.FALSE, () -> this.isMode(2));

   public final ModeProperty modernMarkProp = new ModeProperty("modern-mark", 0, new String[]{"OFF", "BOX", "PLATFORM"}, () -> this.isMode(2));

   public final BooleanProperty onLiquidProp = new BooleanProperty("on-liquid", Boolean.FALSE);

   public final BooleanProperty resetOnLagbackProp = new BooleanProperty("reset-on-lagback", Boolean.TRUE);

   public final BooleanProperty resetOnKnockbackProp = new BooleanProperty("reset-on-knockback", Boolean.TRUE);

private int activeMode = -1;

   private int boostTicksRemaining;

   private int totalBoostTicks;

   private int normalCooldownTimer;

   private int smartDelayTimer;

   private int smartTargetDelay;

   private float smartTargetRange;

   private int modernDelayTimer;

   private int modernTargetDelay;

   private float modernTargetRange;

   private final Queue<Packet<?>> blinkPacketQueue = new ConcurrentLinkedQueue<>();

   private boolean isBlinking;

   private void resetModernTarget() {

      this.modernDelayTimer = 0;

      this.modernTargetDelay = randomInt((Integer)this.modernMinDelayProp.getValue(), (Integer)this.modernMaxDelayProp.getValue());

      this.modernTargetRange = randomFloat((Float)this.modernMinRangeProp.getValue(), (Float)this.modernMaxRangeProp.getValue());

   }

   private boolean isBlinkEnabled() {

      switch ((Integer)this.modeProp.getValue()) {

         case 0:

            return (Boolean)this.normalBlinkProp.getValue();

         case 1:

            return (Boolean)this.smartBlinkProp.getValue();

         case 2:

            return (Boolean)this.modernBlinkProp.getValue();

         default:

            return false;

      }

   }

   @EventTarget

   public void onPacket(PacketEvent var1) {

      if (this.isEnabled() && mc.thePlayer != null && !mc.thePlayer.isDead) {

         Packet<?> var2 = var1.getPacket();

         if (this.isBlinkEnabled()) {

            if (this.boostTicksRemaining > 0 && !this.isBlinking) {

               this.isBlinking = true;

            }

            if (this.isBlinking) {

               boolean shouldFlush = false;

               if (var1.getType() == EventType.RECEIVE) {

                  if (var2 instanceof S08PacketPlayerPosLook) {

                     shouldFlush = true;

                  } else if (var2 instanceof S27PacketExplosion) {

                     S27PacketExplosion var3 = (S27PacketExplosion)var2;

                     if (var3.func_149149_c() != 0.0F || var3.func_149144_d() != 0.0F || var3.func_149147_e() != 0.0F) {

                        this.flushBlinkPackets();

                        return;

                     }

                     var1.setCancelled(true);

                     this.blinkPacketQueue.add(var2);

                     return;

                  } else if (var2 instanceof S06PacketUpdateHealth && ((S06PacketUpdateHealth)var2).getHealth() < mc.thePlayer.getHealth()) {

                     shouldFlush = true;

                  } else {

                     var1.setCancelled(true);

                     this.blinkPacketQueue.add(var2);

                     return;

                  }

               } else if (var1.getType() == EventType.SEND) {

                  if (var2 instanceof C07PacketPlayerDigging

                     || var2 instanceof C12PacketUpdateSign

                     || var2 instanceof C19PacketResourcePackStatus) {

                     shouldFlush = true;

                  } else {

                     return;

                  }

               }

               if (shouldFlush) {

                  this.flushBlinkPackets();

               }

            }

         }

         if (var1.getType() == EventType.RECEIVE) {

            if ((Boolean)this.resetOnLagbackProp.getValue() && var2 instanceof S08PacketPlayerPosLook) {

               this.resetTimerState(true);

            } else if ((Boolean)this.resetOnKnockbackProp.getValue()

               && var2 instanceof S12PacketEntityVelocity

               && ((S12PacketEntityVelocity)var2).getEntityID() == mc.thePlayer.getEntityId()) {

               this.resetTimerState(true);

            }

         }

      }

   }

   private void resetTimerState(boolean flushPackets) {

      setClientTimerSpeed(1.0F);

      this.boostTicksRemaining = 0;

      this.totalBoostTicks = 0;

      this.normalCooldownTimer = 0;

      this.resetSmartTarget();

      this.resetModernTarget();

      if (flushPackets) {

         this.flushBlinkPackets();

      } else {

         this.isBlinking = false;

      }

   }

   private static float randomFloat(float var0, float var1) {

      return var1 <= var0 ? var0 : var0 + (float)(Math.random() * (double)(var1 - var0));

   }

   private static void setClientTimerSpeed(float var0) {

      try {

         ReflectionUtil.getTimer(mc).timerSpeed = var0;

      } catch (Exception var2) {

      }

   }

   private boolean isMode(int var1) {

      return (Integer)this.modeProp.getValue() == var1;

   }

   private boolean isInLiquid() {

      return mc.thePlayer != null && !(Boolean)this.onLiquidProp.getValue() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava());

   }

   private void resetSmartTarget() {

      this.smartDelayTimer = 0;

      this.smartTargetDelay = randomInt((Integer)this.smartMinDelayProp.getValue(), (Integer)this.smartMaxDelayProp.getValue());

      this.smartTargetRange = randomFloat((Float)this.smartMinRangeProp.getValue(), (Float)this.smartMaxRangeProp.getValue());

   }

   private void flushBlinkPackets() {

      Packet<?> var1;

      while ((var1 = this.blinkPacketQueue.poll()) != null) {

         try {

            ((Packet)var1).processPacket(mc.getNetHandler());

         } catch (Exception var3) {

         }

      }

      this.isBlinking = false;

   }

   @EventTarget

   public void onRender3D(Render3DEvent var1) {

      EntityLivingBase var2;

      if (this.isEnabled()

         && mc.thePlayer != null

         && mc.getRenderManager() != null

         && this.isMode(2)

         && (Integer)this.modernMarkProp.getValue() != 0

         && (var2 = findNearestTarget((Float)this.modernScanRangeProp.getValue())) != null) {

         Color var3 = isLookingAtEntity(var2, (Float)this.modernMaxAngleProp.getValue()) ? new Color(37, 126, 255, 70) : new Color(210, 60, 60, 70);

         float var4 = var1.getPartialTicks();

         double var5 = var2.lastTickPosX + (var2.posX - var2.lastTickPosX) * (double)var4 - ReflectionUtil.getRenderPosX(mc.getRenderManager());

         double var7 = var2.lastTickPosY + (var2.posY - var2.lastTickPosY) * (double)var4 - ReflectionUtil.getRenderPosY(mc.getRenderManager());

         double var9 = var2.lastTickPosZ + (var2.posZ - var2.lastTickPosZ) * (double)var4 - ReflectionUtil.getRenderPosZ(mc.getRenderManager());

         float var11 = var2.width / 2.0F;

         double var12 = (Integer)this.modernMarkProp.getValue() == 1 ? var7 + (double)var2.height : var7 + 0.1;

         AxisAlignedBB var14 = new AxisAlignedBB(var5 - (double)var11, var7, var9 - (double)var11, var5 + (double)var11, var12, var9 + (double)var11);

         Render3DUtil.setupRender3D();

         Render3DUtil.drawOutlinedBox(var14, var3.getRed(), var3.getGreen(), var3.getBlue(), var3.getAlpha(), 1.8F);

         Render3DUtil.endRender3D();

      }

   }

   private void startTimerBoost(int var1) {

      this.boostTicksRemaining = var1;

      this.totalBoostTicks = var1;

   }

   private boolean isMovingForward(boolean var1) {

      if (mc.thePlayer == null || this.isInLiquid()) {

         return false;

      } else {

         return var1 ? mc.thePlayer.moveForward != 0.0F && mc.thePlayer.moveStrafing == 0.0F : mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F;

      }

   }

   private static boolean isLookingAtEntity(Entity var0, float var1) {

      double var2 = var0.posX - mc.thePlayer.posX;

      float var4 = (float)(Math.atan2(var0.posZ - mc.thePlayer.posZ, var2) * 180.0 / Math.PI) - 90.0F;

      return Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - var4)) <= var1;

   }

   private static EntityLivingBase findNearestTarget(float var0) {

      if (mc.theWorld != null && mc.thePlayer != null) {

         EntityLivingBase var1 = null;

         double var2 = Double.MAX_VALUE;

         for (Entity var5 : mc.theWorld.loadedEntityList) {

            double var6;

            if (var5 instanceof EntityPlayer && var5 != mc.thePlayer && !var5.isDead && (var6 = (double)mc.thePlayer.getDistanceToEntity(var5)) <= (double)var0 && var6 < var2) {

               var1 = (EntityLivingBase)var5;

               var2 = var6;

            }

         }

         return var1;

      } else {

         return null;

      }

   }

   private static boolean isTargetWithinPredictedRange(Entity var0, float var1, int var2, float var3) {

      double var4 = 2.0 + (double)var3;

      AxisAlignedBB var6 = var0.getEntityBoundingBox().offset((var0.posX - var0.prevPosX) * var4, (var0.posY - var0.prevPosY) * var4, (var0.posZ - var0.prevPosZ) * var4);

      double var7 = mc.thePlayer.posX + mc.thePlayer.motionX * (double)var2;

      double var9 = mc.thePlayer.posY + mc.thePlayer.motionY * (double)var2;

      double var11 = mc.thePlayer.posZ + mc.thePlayer.motionZ * (double)var2;

      double var13 = Math.max(var6.minX - var7, Math.max(0.0, var7 - var6.maxX));

      double var15 = Math.max(var6.minY - var9, Math.max(0.0, var9 - var6.maxY));

      double var17 = Math.max(var6.minZ - var11, Math.max(0.0, var11 - var6.maxZ));

      return Math.sqrt(var13 * var13 + var15 * var15 + var17 * var17) <= (double)var1;

   }

   private void updateTimerSpeed(float var1, float var2, float var3, float var4) {

      if (this.boostTicksRemaining > 0 && !this.isInLiquid()) {

         double var5 = 1.0 - (double)this.boostTicksRemaining / (double)Math.max(this.totalBoostTicks, 1);

         setClientTimerSpeed(Math.max(var5 < (double)var3 ? var1 : (var5 < (double)var4 ? var2 : 1.0F), 0.05F));

         this.boostTicksRemaining--;

      } else {

         this.boostTicksRemaining = 0;

         this.totalBoostTicks = 0;

         setClientTimerSpeed(1.0F);

         if (this.isBlinking) {

            this.flushBlinkPackets();

         }

      }

   }

   @Override

   public void onDisabled() {

      this.resetTimerState(true);

      this.activeMode = -1;

   }

   @Override

   public void onEnabled() {

      this.activeMode = (Integer)this.modeProp.getValue();

      this.resetTimerState(true);

   }

   @EventTarget

   public void onTick(TickEvent var1) {

      if (this.isEnabled()) {

         if (var1.getType() == EventType.PRE) {

            if (mc.thePlayer != null && mc.theWorld != null) {

               int var2 = (Integer)this.modeProp.getValue();

               if (this.activeMode != var2) {

                  this.resetTimerState(true);

                  this.activeMode = var2;

               }

               switch (var2) {

                  case 0:

                     this.normalCooldownTimer++;

                     this.updateTimerSpeed((Float)this.normalBoostProp.getValue(), (Float)this.normalChargedProp.getValue(), (Float)this.normalBoostEndProp.getValue(), (Float)this.normalChargeEndProp.getValue());

                     return;

                  case 1:

                     this.smartDelayTimer = this.isMovingForward((Boolean)this.smartForwardOnlyProp.getValue()) ? this.smartDelayTimer + 1 : 0;

                     this.updateTimerSpeed((Float)this.smartBoostProp.getValue(), (Float)this.smartChargedProp.getValue(), (Float)this.smartBoostEndProp.getValue(), (Float)this.smartChargeEndProp.getValue());

                     return;

                  case 2:

                     if (!this.isMovingForward((Boolean)this.modernForwardOnlyProp.getValue())) {

                        this.modernDelayTimer = 0;

                     } else {

                        EntityLivingBase var3;

                        if ((var3 = findNearestTarget((Float)this.modernScanRangeProp.getValue())) != null && isLookingAtEntity(var3, (Float)this.modernMaxAngleProp.getValue())) {

                           this.modernDelayTimer++;

                           if (this.modernDelayTimer >= this.modernTargetDelay && this.boostTicksRemaining <= 0) {

                              float var4 = mc.thePlayer.getDistanceToEntity(var3);

                              if (var4 >= this.modernTargetRange

                                 && var4 <= (Float)this.modernMaxRangeProp.getValue()

                                 && isTargetWithinPredictedRange(var3, this.modernTargetRange, (Integer)this.modernClientPredictionProp.getValue(), (Float)this.modernEnemyPredictionProp.getValue())) {

                                 this.startTimerBoost((Integer)this.modernTicksProp.getValue());

                                 this.resetModernTarget();

                              }

                           }

                        }

                     }

                     this.updateTimerSpeed(

                        (Float)this.modernBoostProp.getValue(), (Float)this.modernChargedProp.getValue(), (Float)this.modernBoostEndProp.getValue(), (Float)this.modernChargeEndProp.getValue()

                     );

                     return;

               }

               setClientTimerSpeed(1.0F);

            } else {

               this.resetTimerState(true);

            }

         } else if (var1.getType() == EventType.POST && this.isBlinkEnabled() && this.isBlinking) {

            this.flushBlinkPackets();

         }

      }

   }

   private static int randomInt(int var0, int var1) {

      return var1 <= var0 ? var0 : var0 + (int)(Math.random() * (double)(var1 - var0 + 1));

   }

   @EventTarget

   public void onLoadWorld(LoadWorldEvent var1) {

      this.resetTimerState(false);

      this.blinkPacketQueue.clear();

   }

   @EventTarget

   public void onAttack(AttackEvent var1) {

      if (this.isEnabled() && mc.thePlayer != null && var1.getTarget() instanceof EntityLivingBase) {

         EntityLivingBase var2 = (EntityLivingBase)var1.getTarget();

         switch (this.modeProp.getValue()) {

            case 0:

               if (this.isMovingForward((Boolean)this.normalForwardOnlyProp.getValue())

                  && this.normalCooldownTimer >= (Integer)this.normalCooldownProp.getValue()

                  && mc.thePlayer.getDistanceToEntity(var2) <= (Float)this.normalRangeProp.getValue()) {

                  this.startTimerBoost((Integer)this.normalTicksProp.getValue());

                  this.normalCooldownTimer = 0;

                  return;

               }

               break;

            case 1:

               if (this.isMovingForward((Boolean)this.smartForwardOnlyProp.getValue())

                  && this.smartDelayTimer >= this.smartTargetDelay

                  && isTargetWithinPredictedRange(var2, this.smartTargetRange, (Integer)this.smartClientPredictionProp.getValue(), (Float)this.smartEnemyPredictionProp.getValue())) {

                  this.startTimerBoost((Integer)this.smartTicksProp.getValue());

                  this.resetSmartTarget();

               }

         }

      }

   }

public TimerRange() {

      super("TimerRange", false, ModuleCategory.COMBAT);

   }

}

