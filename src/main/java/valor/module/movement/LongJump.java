package valor.module.movement;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import valor.Valor;
import valor.core.MovementManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.KeyEvent;
import valor.events.KnockbackEvent;
import valor.events.MoveInputEvent;
import valor.events.PacketEvent;
import valor.events.StrafeEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.ChatUtil;
import valor.util.MoveUtil;
import valor.util.PacketUtil;
import valor.util.RandomUtil;
import valor.util.ReflectionUtil;
import valor.util.RotationUtil;
import valor.util.TimerUtil;

public class LongJump extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final TimerUtil fireballTimer = new TimerUtil();
   private final TimerUtil timeoutTimer = new TimerUtil();
   public boolean isLongJumping = false;
   private int jumpTicks = 0;
   private int jumpStage = 0;
   private boolean isAiming = false;
   private boolean fireballThrown = false;
   private int originalSlot = -1;

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"FIREBALL", "FIREBALL_MANUAL", "FIREBALL_HIGH", "FIREBALL_FLAT"});
   public final FloatProperty motionProp = new FloatProperty("motion", 1.0F, 1.0F, 20.0F);
   public final FloatProperty speedMotionProp = new FloatProperty("speed-motion", 1.0F, 1.0F, 20.0F);
   public final PercentageProperty strafeProp = new PercentageProperty("strafe", 0);

   @EventTarget
   public void onKey(KeyEvent var1) {
      ItemStack var2;
      if (var1.getKey() == mc.gameSettings.keyBindUseItem.getKeyCode()
         && (var2 = mc.thePlayer.inventory.getCurrentItem()) != null
         && var2.getItem() instanceof ItemFireball) {
         this.fireballTimer.reset();
      }
   }

   @Override
   public String[] getSuffix() {
      String var1;
      return (var1 = this.modeProp.getModeName()).contains("FIREBALL")
         ? new String[]{"Fireball"}
         : new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, var1)};
   }

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (this.isFireballMode() && this.isLongJumping) {
             this.jumpTicks++;
             if (this.jumpTicks == 1) {
                int mode = (Integer)this.modeProp.getValue();
                if (mode == 0 || mode == 1) {
                   this.jumpStage = 0;
                } else if (mode == 3 && MoveUtil.isMoving()) {
                   this.jumpStage = 2;
                } else if (mode == 2 || mode == 3) {
                   this.jumpStage = 1;
                }
             }

            if (this.jumpTicks == 2 && MoveUtil.isMoving()) {
               MoveUtil.setSpeed(MoveUtil.getMovementSpeed() * (MoveUtil.getSpeedEffectAmplifier() > 0 ? (double)((Float)this.speedMotionProp.getValue()).floatValue() : (double)((Float)this.motionProp.getValue()).floatValue()));
            }

            if (this.jumpTicks > 0 && this.jumpTicks <= 30) {
               switch (this.jumpStage) {
                  case 1:
                     if (this.jumpTicks == 1) {
                        mc.thePlayer.motionY *= 0.75;
                     } else {
                        double var2;
                        if ((var2 = mc.thePlayer.motionY / 0.98F + 0.055) > 0.0) {
                           mc.thePlayer.motionY = var2;
                        }
                     }
                     break;
                  case 2:
                     if (this.jumpTicks == 1) {
                        mc.thePlayer.motionY *= 0.75;
                     } else {
                        mc.thePlayer.motionY = 0.01 + (double)this.jumpTicks * 0.003;
                     }
               }
            }

            if (this.jumpTicks >= 30) {
               this.isLongJumping = false;
               this.jumpTicks = 0;
               this.jumpStage = 0;
               if (this.isAutoMode()) {
                  this.setEnabled(false);
               }

               return;
            }
         }

         if (this.isAutoMode() && !this.isLongJumping) {
            if (this.timeoutTimer.hasTimeElapsed(1500L)) {
               this.setEnabled(false);
               return;
            }

            this.isAiming = true;
            float var4 = RotationUtil.roundRotation(mc.thePlayer.rotationYaw - 180.0F - RandomUtil.nextFloat(0.0F, 1.0F));
            float var3 = RotationUtil.roundRotation(89.0F + RandomUtil.nextFloat(-0.25F, 0.25F));
            var1.setRotation(var4, var3, 4);
            var1.setPervRotation(var4, 4);
         }
      }
   }

   @EventTarget(1)
   public void onPacket(PacketEvent var1) {
      if (var1.getType() == EventType.RECEIVE && !var1.isCancelled() && var1.getPacket() instanceof S08PacketPlayerPosLook) {
         this.isLongJumping = false;
         this.jumpTicks = 0;
         this.jumpStage = 0;
         if (this.isAutoMode()) {
            this.setEnabled(false);
         }
      }
   }

   private static int findFireballSlot() {
      if (mc.thePlayer == null) {
         return -1;
      } else {
         for (int var0 = 0; var0 < 9; var0++) {
            ItemStack var1;
            if ((var1 = mc.thePlayer.inventory.getStackInSlot(var0)) != null && var1.getItem() instanceof ItemFireball) {
               return var0;
            }
         }

         return -1;
      }
   }

   private boolean isFireballMode() {
      return this.isAutoMode() || this.isManualMode();
   }

   private boolean isManualMode() {
      return (Integer)this.modeProp.getValue() == 1;
   }

   public final boolean isAutoMode() {
      return (Integer)this.modeProp.getValue() == 0 || (Integer)this.modeProp.getValue() == 2 || (Integer)this.modeProp.getValue() == 3;
   }

   @Override
   public void onDisabled() {
      this.isLongJumping = false;
      this.jumpTicks = 0;
      this.jumpStage = 0;
      this.isAiming = false;
      this.fireballThrown = false;
   }

   @Override
   public void onEnabled() {
      this.timeoutTimer.reset();
      if (this.isAutoMode() && findFireballSlot() == -1) {
         this.setEnabled(false);
         ChatUtil.addFormattedChatMessage(String.format("%s%s: &cNo fireball found in your hotbar!&r", Valor.CHAT_PREFIX, this.getName()));
      }
   }

   public final boolean canTakeKnockback() {
      return !this.fireballTimer.hasTimeElapsed(1000L) && !this.isLongJumping;
   }

   @EventTarget(0)
   public void onTick(TickEvent var1) {
      if (this.isEnabled()) {
         switch (var1.getType()) {
            case PRE:
               if (this.isAutoMode() && !this.fireballThrown && this.isAiming) {
                  int var2;
                  if ((var2 = findFireballSlot()) != -1) {
                     this.originalSlot = mc.thePlayer.inventory.currentItem;
                     mc.thePlayer.inventory.currentItem = var2;
                     ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
                     PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                     this.fireballTimer.reset();
                     this.fireballThrown = true;
                  }

                  return;
               }
               break;
            case POST:
               if (this.originalSlot != -1) {
                  mc.thePlayer.inventory.currentItem = this.originalSlot;
                  this.originalSlot = -1;
               }
         }
      }
   }

   @EventTarget
   public void onMoveInput(MoveInputEvent var1) {
      if (this.isEnabled() && MovementManager.isRotated() && MovementManager.getPriority() == 4.0F && MoveUtil.isMoving()) {
         MoveUtil.fixMovement(MovementManager.getMovementYaw());
      }
   }

   @EventTarget
   public void onStrafe(StrafeEvent var1) {
      if (this.isEnabled() && this.isFireballMode() && this.isLongJumping && this.jumpTicks >= 5 && this.jumpTicks <= 30 && (Integer)this.strafeProp.getValue() > 0) {
         double var2;
         MoveUtil.strafe((var2 = MoveUtil.getMovementSpeed()) * (double)((float)(100 - (Integer)this.strafeProp.getValue()) / 100.0F), MoveUtil.getMotionYaw());
         MoveUtil.strafeAdd(var2 * (double)((float)((Integer)this.strafeProp.getValue()).intValue() / 100.0F), MoveUtil.getMoveYaw());
         MoveUtil.setSpeed(var2);
      }
   }

   @EventTarget(0)
   public void onKnockback(KnockbackEvent var1) {
      if (this.isEnabled() && !var1.isCancelled() && (this.isManualMode() || this.isAutoMode()) && this.canTakeKnockback()) {
         var1.setCancelled(true);
         this.isLongJumping = true;
         this.jumpTicks = 0;
      }
   }

   public LongJump() {
      super("LongJump", false, ModuleCategory.MOVEMENT);
   }
}
