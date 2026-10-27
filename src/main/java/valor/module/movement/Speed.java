package valor.module.movement;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;
import valor.Valor;
import valor.core.BlinkType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LivingUpdateEvent;
import valor.events.StrafeEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.world.Scaffold;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.MoveUtil;
import valor.util.ReflectionUtil;

public class Speed extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final int MODE_CUSTOM = 0;
   private static final int MODE_NCP = 1;
   private static final int MODE_HYPIXEL_LOWHOP = 2;
   private static final int MODE_LEGIT = 3;
   private static final int MODE_VULCAN_NEW = 4;
   private static final int MODE_TIMER = 5;

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"CUSTOM", "NCP", "HYPIXEL_LOWHOP", "LEGIT", "VULCAN_NEW", "TIMER"});
   public final FloatProperty multiplierProp = new FloatProperty("multiplier", 1.0F, 0.0F, 10.0F, () -> (Integer)this.modeProp.getValue() == 0);
   public final FloatProperty frictionProp = new FloatProperty("friction", 1.0F, 0.0F, 10.0F, () -> (Integer)this.modeProp.getValue() == 0);
   public final PercentageProperty strafeProp = new PercentageProperty("strafe", 0, () -> (Integer)this.modeProp.getValue() == 0);
   public final BooleanProperty rotateProp = new BooleanProperty("rotate", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 3);

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget(4)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         int var2 = (Integer)this.modeProp.getValue();
         if (!canApplySpeed()) {
            if (var2 == 5) {
               MoveUtil.resetTimer();
               Valor.blinkManager.setBlinking(false, BlinkType.DISABLER);
            }
         } else if (var2 == 5) {
            switch (MoveUtil.getAirTicks()) {
               case 0:
                  Valor.blinkManager.setBlinking(false, BlinkType.DISABLER);
               case 8:
                  ReflectionUtil.getTimer(mc).timerSpeed = 1.0F;
               default:
                  return;
               case 7:
                  Valor.blinkManager.setBlinking(true, BlinkType.DISABLER);
                  ReflectionUtil.getTimer(mc).timerSpeed = 2.0F;
            }
         } else {
            MoveUtil.resetTimer();
         }
      }
   }

   public static boolean hasSpeedPotion() {
      return mc.thePlayer.isPotionActive(Potion.moveSpeed);
   }

   public static boolean canApplySpeed() {
      return !((Scaffold)Valor.moduleManager.modules.get(Scaffold.class)).isEnabled()
         && MoveUtil.isMoving()
         && mc.thePlayer.getFoodStats().getFoodLevel() > 6
         && !mc.thePlayer.isSneaking()
         && !mc.thePlayer.isInWater()
         && !mc.thePlayer.isInLava()
         && !ReflectionUtil.isInWeb(mc.thePlayer);
   }

   @Override
   public void onDisabled() {
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
      MoveUtil.resetTimer();
      Valor.blinkManager.setBlinking(false, BlinkType.DISABLER);
   }

   @Override
   public void onEnabled() {
      MoveUtil.resetTimer();
   }

   @EventTarget(3)
   public void onLivingUpdate(LivingUpdateEvent var1) {
      if (this.isEnabled() && canApplySpeed()) {
         int var3;
         if ((var3 = (Integer)this.modeProp.getValue()) == 0 || var3 == 1 || var3 == 2) {
            mc.thePlayer.movementInput.jump = false;
         }

         if (var3 == 3 && mc.thePlayer.onGround) {
            if ((Boolean)this.rotateProp.getValue()) {
               float var2 = MoveUtil.calculateMoveYaw(mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe) + 45.0F;
               mc.thePlayer.rotationYaw = var2;
               mc.thePlayer.rotationYawHead = var2;
            }

            mc.thePlayer.movementInput.jump = true;
         }

         if (var3 == 1 || var3 == 2 || var3 == 5) {
            if (mc.thePlayer.onGround) {
               KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
               return;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
         }
      }
   }

   @EventTarget(3)
   public void onStrafe(StrafeEvent var1) {
      if (this.isEnabled() && canApplySpeed()) {
         int var2;
         if ((var2 = (Integer)this.modeProp.getValue()) != 3) {
            int var3 = MoveUtil.getAirTicks();
            switch (var2) {
               case 0:
                  if (mc.thePlayer.onGround) {
                     mc.thePlayer.motionY = 0.42F;
                     MoveUtil.strafe(MoveUtil.getBaseSpeed() * (double)((Float)this.multiplierProp.getValue()).floatValue(), MoveUtil.getMoveYaw());
                     return;
                  }

                  if ((Float)this.frictionProp.getValue() != 1.0F) {
                     var1.setFriction(var1.getFriction() * (Float)this.frictionProp.getValue());
                  }

                  if ((Integer)this.strafeProp.getValue() > 0) {
                     double var6;
                     MoveUtil.strafe((var6 = MoveUtil.getMovementSpeed()) * (double)((float)(100 - (Integer)this.strafeProp.getValue()) / 100.0F), MoveUtil.getMotionYaw());
                     MoveUtil.strafeAdd(var6 * (double)((Integer)this.strafeProp.getValue()).intValue() / 100.0, MoveUtil.getMoveYaw());
                     MoveUtil.setSpeed(var6);
                     return;
                  }
                  break;
               case 1:
                  if (mc.thePlayer.onGround) {
                     mc.thePlayer.motionY = 0.42F;
                     MoveUtil.strafe(hasSpeedPotion() ? 0.65 : 0.485, MoveUtil.getMoveYaw());
                     return;
                  }

                  if (var3 == 1) {
                     MoveUtil.strafe(0.31, MoveUtil.getMoveYaw());
                     return;
                  }

                  if (var3 == 4) {
                     mc.thePlayer.motionY -= 0.07;
                     return;
                  }
                  break;
               case 2:
                  if (mc.thePlayer.onGround) {
                     mc.thePlayer.motionY = 0.4F;
                     MoveUtil.strafe(hasSpeedPotion() ? 0.55 : 0.42, MoveUtil.getMoveYaw());
                     return;
                  }

                  if (var3 == 1) {
                     mc.thePlayer.motionY -= 0.08;
                     MoveUtil.strafe(0.28, MoveUtil.getMoveYaw());
                     return;
                  }
               case 3:
               default:
                  break;
               case 4:
                  if (mc.thePlayer.onGround) {
                     if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.thePlayer.jump();
                     }

                     double var4;
                     if ((var4 = MoveUtil.getMovementSpeed()) < 0.4) {
                        MoveUtil.strafe(0.4, MoveUtil.getMoveYaw());
                        return;
                     }

                     if (var4 > 0.49) {
                        MoveUtil.strafe(0.48, MoveUtil.getMoveYaw());
                     }

                     return;
                  }

                  if (mc.thePlayer.hurtTime <= 0 && MoveUtil.getMovementSpeed() < 0.222) {
                     MoveUtil.strafe(0.222, MoveUtil.getMoveYaw());
                  }
            }
         }
      }
   }

   public Speed() {
      super("Speed", false, ModuleCategory.MOVEMENT);
   }
}
