package valor.module.combat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Mouse;
import valor.Valor;
import valor.core.MovementManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.KeyEvent;
import valor.events.MoveInputEvent;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.hook.EntityRendererHooks;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.BotCheckUtil;
import valor.util.ItemUtil;
import valor.util.MoveUtil;
import valor.util.PlayerUtil;
import valor.util.RandomUtil;
import valor.util.RotationUtil;
import valor.util.TimerUtil;

public class AimAssist extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final TimerUtil timer = new TimerUtil();
   private float targetYaw = 0.0F;
   private float targetPitch = 0.0F;
   private int targetEntityId = -1;
   private EntityPlayer targetPlayer = null;
   private EntityPlayer hoveredPlayer = null;
   private long lastAimTime = 0L;
   public final FloatProperty horizontalSpeedProp = new FloatProperty("horizontal-speed", 3.0F, 0.0F, 10.0F);
   public final FloatProperty verticalSpeedProp = new FloatProperty("vertical-speed", 0.0F, 0.0F, 10.0F);
   public final PercentageProperty smoothingProp = new PercentageProperty("smoothing", 50);
   public final FloatProperty rangeProp = new FloatProperty("range", 4.5F, 3.0F, 8.0F);
   public final IntProperty fovProp = new IntProperty("fov", 90, 30, 360);
   public final BooleanProperty weaponsOnlyProp = new BooleanProperty("weapons-only", Boolean.TRUE);
   public final BooleanProperty allowToolsProp = new BooleanProperty("allow-tools", Boolean.FALSE, this.weaponsOnlyProp::getValue);
   public final BooleanProperty botCheckProp = new BooleanProperty("bot-check", Boolean.TRUE);
   public final BooleanProperty teamsProp = new BooleanProperty("teams", Boolean.TRUE);
   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"SMOOTH", "LOCK_IN", "SILENT"});
   public final FloatProperty lockSpeedProp = new FloatProperty("lock-speed", 30.0F, 0.1F, 30.0F, () -> (Integer)this.modeProp.getValue() == 1);
   public final IntProperty angleStepProp = new IntProperty("angle-step", 90, 30, 180, () -> (Integer)this.modeProp.getValue() == 2);
   public final PercentageProperty silentSmoothProp = new PercentageProperty("silent-smooth", 0, () -> (Integer)this.modeProp.getValue() == 2);

   @EventTarget
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && (Integer)this.modeProp.getValue() == 2 && var1.getType() == EventType.PRE) {
         if (this.hoveredPlayer != null && this.isValidTarget(this.hoveredPlayer)) {
            EntityPlayer var2 = this.hoveredPlayer;
            AxisAlignedBB var3 = this.hoveredPlayer.getEntityBoundingBox();
            double var4 = (double)var2.getCollisionBorderSize();
            float[] var6 = RotationUtil.getRotationsToBox(
               var3.expand(var4, var4, var4),
               var1.getYaw(),
               var1.getPitch(),
               (float)((Integer)this.angleStepProp.getValue()).intValue() + RandomUtil.nextFloat(-5.0F, 5.0F),
               (float)((Integer)this.silentSmoothProp.getValue()).intValue() / 100.0F
            );
            if (Mouse.isButtonDown(0)) {
               var1.setRotation(var6[0], var6[1], 1);
               var1.setPervRotation(var6[0], 1);
               Valor.rotationManager.setRotation(var6[0], var6[1], 1, false);
               EntityRendererHooks.setMouseOverLock(var6[0], var6[1], true);
            } else {
               EntityRendererHooks.setMouseOverLock(0.0F, 0.0F, false);
            }
         } else {
            this.hoveredPlayer = null;
            EntityRendererHooks.setMouseOverLock(0.0F, 0.0F, false);
         }
      }
   }

   private boolean isTargetInReach(EntityPlayer var1) {
      Reach var2;
      double var3 = (var2 = (Reach)Valor.moduleManager.modules.get(Reach.class)).isEnabled() ? (double)((Float)var2.rangeProp.getValue()).floatValue() : 3.0;
      return RotationUtil.getDistanceToTarget(var1) <= var3;
   }

   @EventTarget
   public void onRender3D(Render3DEvent var1) {
      if ((Integer)this.modeProp.getValue() == 1 && this.isEnabled() && this.targetPlayer != null && mc.currentScreen == null && mc.thePlayer != null) {
         if (!PlayerUtil.isAttackPressed()) {
            this.resetTarget();
         } else {
            EntityPlayer var2 = this.targetPlayer;
            if (!this.isValidTarget(var2)) {
               this.resetTarget();
            } else {
               float var20 = var1.getPartialTicks();
               double var4 = var2.lastTickPosX + (var2.posX - var2.lastTickPosX) * (double)var20;
               double var6 = var2.lastTickPosY + (var2.posY - var2.lastTickPosY) * (double)var20;
               double var8 = var2.lastTickPosZ + (var2.posZ - var2.lastTickPosZ) * (double)var20;
               Vec3 var21 = mc.thePlayer.getPositionEyes(var20);
               double var10 = Math.max(var6, Math.min(var21.yCoord, var6 + (double)var2.height));
               double var12 = var4 - var21.xCoord;
               double var14 = var10 - var21.yCoord;
               double var16 = var8 - var21.zCoord;
               double var18 = Math.sqrt(var12 * var12 + var16 * var16);
               float var22 = (float)(Math.toDegrees(Math.atan2(var16, var12)) - 90.0);
               float var24 = (float)(-Math.toDegrees(Math.atan2(var14, var18)));
               float var23 = MathHelper.wrapAngleTo180_float(var22 - mc.thePlayer.rotationYaw);
               float var25 = var24 - mc.thePlayer.rotationPitch;
               float var3;
               float var10000 = var3 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
               float var26;
               var3 = (var26 = var10000 * var10000 * var3 * 1.2F) > 0.0F ? var26 : 1.0E-4F;
               if (!(Math.abs(var23) < var3 * 0.5F) || !(Math.abs(var25) < var3 * 0.5F)) {
                  float var28 = (Float)this.lockSpeedProp.getValue();
                  float var5 = MathHelper.clamp_float(Math.abs(var23) / 20.0F, 0.2F, 1.0F);
                  float var29 = var28 * var5;
                  var5 = MathHelper.clamp_float(var23, -var29, var29);
                  float var30 = MathHelper.clamp_float(var25, -var29, var29);
                  mc.thePlayer.rotationYaw = mc.thePlayer.rotationYaw + smoothAngleStep(var5, var23, var3);
                  mc.thePlayer.rotationPitch = mc.thePlayer.rotationPitch + smoothAngleStep(var30, var25, var3);
                  mc.thePlayer.prevRotationYaw = mc.thePlayer.rotationYaw;
                  mc.thePlayer.prevRotationPitch = mc.thePlayer.rotationPitch;
               }
            }
         }
      }
   }

   private static float smoothAngleStep(float var0, float var1, float var2) {
      var0 = (float)Math.round(var0 / var2) * var2;
      if (var1 > 0.0F) {
         return Math.min(var0, var1);
      } else {
         return var1 < 0.0F ? Math.max(var0, var1) : 0.0F;
      }
   }

   private boolean isValidTarget(EntityPlayer var1) {
      if (var1 == mc.thePlayer || var1 == mc.thePlayer.ridingEntity) {
         return false;
      } else if (var1 == mc.getRenderViewEntity() || var1 == mc.getRenderViewEntity().ridingEntity) {
         return false;
      } else if (var1.deathTime > 0) {
         return false;
      } else if (RotationUtil.getDistanceToTarget(var1) > (double)((Float)this.rangeProp.getValue()).floatValue()) {
         return false;
      } else if (RotationUtil.getAngleToEntity(var1) > (float)((Integer)this.fovProp.getValue()).intValue()) {
         return false;
      } else if (RotationUtil.rayTraceToEntity(var1) != null) {
         return false;
      } else {
         return BotCheckUtil.isFriend(var1)
            ? false
            : (!(Boolean)this.teamsProp.getValue() || !BotCheckUtil.isTeammate(var1)) && (!(Boolean)this.botCheckProp.getValue() || !BotCheckUtil.isBot(var1));
      }
   }

   @Override
   public void onDisabled() {
      this.resetTarget();
      this.hoveredPlayer = null;
      EntityRendererHooks.setMouseOverLock(0.0F, 0.0F, false);
   }

   private void resetTarget() {
      this.targetPlayer = null;
      this.targetEntityId = -1;
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.POST && mc.currentScreen == null) {
         if ((Integer)this.modeProp.getValue() == 1 && this.targetPlayer != null && !this.isValidTarget(this.targetPlayer)) {
            this.resetTarget();
         }

         boolean var6;
         List<EntityPlayer> var7;
         if ((!(Boolean)this.weaponsOnlyProp.getValue() || ItemUtil.isHoldingWeapon() || (Boolean)this.allowToolsProp.getValue() && ItemUtil.isHoldingTool())
            && (!(var6 = PlayerUtil.isAttackPressed()) || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectType.BLOCK)
            && (var6 || !this.timer.hasTimeElapsed(350L))
            && !(var7 = mc.theWorld
                  .loadedEntityList
                  .stream()
                  .filter(var0 -> var0 instanceof EntityPlayer)
                  .map(var0 -> (EntityPlayer)var0)
                  .filter(this::isValidTarget)
                  .sorted(Comparator.comparingDouble(RotationUtil::getDistanceToTarget))
                  .collect(Collectors.toList()))
               .isEmpty()) {
            if (var7.stream().anyMatch(this::isTargetInReach)) {
               var7.removeIf(var1x -> !this.isTargetInReach(var1x));
            }

            EntityPlayer var8 = (EntityPlayer)var7.get(0);
            if ((Integer)this.modeProp.getValue() == 1) {
               if (this.targetPlayer == null) {
                  this.targetPlayer = var8;
                  this.targetEntityId = var8.getEntityId();
                  return;
               }
            } else {
               if ((Integer)this.modeProp.getValue() == 2) {
                  this.hoveredPlayer = var8;
                  return;
               }

               if (!(RotationUtil.getDistanceToTarget(var8) <= 0.0)) {
                  AxisAlignedBB var2 = var8.getEntityBoundingBox();
                  double var4 = (double)var8.getCollisionBorderSize();
                  float[] var9 = RotationUtil.getRotationsToBox(
                     var2.expand(var4, var4, var4),
                     mc.thePlayer.rotationYaw,
                     mc.thePlayer.rotationPitch,
                     180.0F,
                     (float)((Integer)this.smoothingProp.getValue()).intValue() / 100.0F
                  );
                  float var10 = Math.min(Math.abs((Float)this.horizontalSpeedProp.getValue()), 10.0F);
                  float var3 = Math.min(Math.abs((Float)this.verticalSpeedProp.getValue()), 10.0F);
                  Valor.rotationManager
                     .setRotation(
                        mc.thePlayer.rotationYaw + (var9[0] - mc.thePlayer.rotationYaw) * 0.1F * var10,
                        mc.thePlayer.rotationPitch + (var9[1] - mc.thePlayer.rotationPitch) * 0.1F * var3,
                        0,
                        false
                     );
               }
            }
         }
      }
   }

   @EventTarget
   public void onMove(MoveInputEvent var1) {
      if (this.isEnabled() && (Integer)this.modeProp.getValue() == 2 && MovementManager.isRotated() && MovementManager.getPriority() == 1.0F) {
         MoveUtil.fixMovement(MovementManager.getMovementYaw());
      }
   }

   @EventTarget
   public void onPress(KeyEvent var1) {
      if (var1.getKey() == mc.gameSettings.keyBindAttack.getKeyCode() && !Valor.moduleManager.modules.get(AutoClicker.class).isEnabled()) {
         this.timer.reset();
      }
   }

   public AimAssist() {
      super("AimAssist", false, ModuleCategory.COMBAT);
   }
}
