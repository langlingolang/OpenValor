package valor.module.combat;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.Render3DEvent;
import valor.events.StrafeEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.combat.KillAura;
import valor.module.movement.Speed;
import valor.module.movement.LongJump;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.BotCheckUtil;
import valor.util.ColorUtil;
import valor.util.MoveUtil;
import valor.util.PlayerUtil;
import valor.util.Render3DUtil;
import valor.util.RotationUtil;

public class TargetStrafe extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private EntityLivingBase targetEntity = null;
   public float strafeYaw = Float.NaN;
   private int strafeDirection = 1;

   public final FloatProperty radiusProp = new FloatProperty("radius", 1.0F, 0.0F, 6.0F);
   public final IntProperty pointsProp = new IntProperty("points", 6, 3, 24);
   public final BooleanProperty requirePressProp = new BooleanProperty("require-press", Boolean.TRUE);
   public final BooleanProperty speedOnlyProp = new BooleanProperty("speed-only", Boolean.TRUE);
   public final ModeProperty showTargetProp = new ModeProperty("show-target", 1, new String[]{"NONE", "DEFAULT", "THEME"});

   @EventTarget(0)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         boolean var2 = PlayerUtil.isLeftPressed();
         int var3;
         if (var2 ^ PlayerUtil.isRightPressed()) {
            this.strafeDirection = var2 ? 1 : -1;
         }

          boolean canStrafe = true;
          if ((Boolean)this.speedOnlyProp.getValue()) {
             Speed speedMod = (Speed)Valor.moduleManager.modules.get(Speed.class);
             LongJump longJumpMod = (LongJump)Valor.moduleManager.modules.get(LongJump.class);
             if (!speedMod.isEnabled() && (!longJumpMod.isEnabled() || !longJumpMod.isLongJumping)) {
                canStrafe = false;
             }
          }
          if (canStrafe && (Boolean)this.requirePressProp.getValue() && !PlayerUtil.isJumpPressed()) {
             canStrafe = false;
          }

          if (!canStrafe) {
            this.targetEntity = null;
            this.strafeYaw = Float.NaN;
            return;
         }

         KillAura var27;
         EntityLivingBase var28;
         this.targetEntity = (var27 = (KillAura)Valor.moduleManager.modules.get(KillAura.class)).isEnabled()
               && var27.canAttackWithItem()
               && BotCheckUtil.isEntityLoaded((Entity)(var28 = var27.getCurrentTargetEntity()))
            ? var28
            : null;
         if (this.targetEntity == null) {
            this.strafeYaw = Float.NaN;
            return;
         }

         ArrayList<TargetStrafe.Point> var19 = new ArrayList<>();

         for (int var5 = 0; var5 < (Integer)this.pointsProp.getValue(); var5++) {
            var19.add(
               new TargetStrafe.Point(
                  (double)((Float)this.radiusProp.getValue()).floatValue() * Math.cos((double)var5 * ((Math.PI * 2) / (double)((Integer)this.pointsProp.getValue()).intValue())),
                  (double)((Float)this.radiusProp.getValue()).floatValue() * Math.sin((double)var5 * ((Math.PI * 2) / (double)((Integer)this.pointsProp.getValue()).intValue()))
               )
            );
         }

         if (var19.isEmpty()) {
            this.targetEntity = null;
            this.strafeYaw = Float.NaN;
            return;
         }

         double var25 = 0.0;
         var3 = -1;

         for (int var4 = 0; var4 < var19.size(); var4++) {
            double var7 = mc.thePlayer
               .getDistance(this.targetEntity.posX + var19.get(var4).x, mc.thePlayer.posY, this.targetEntity.posZ + var19.get(var4).z);
            if (var3 == -1 || var7 < var25) {
               var25 = var7;
               var3 = var4;
            }
         }

         if (mc.thePlayer.isCollidedHorizontally) {
            this.strafeDirection = -this.strafeDirection;
         }

         int var23 = wrapIndex(var3 + this.strafeDirection, var19.size());
         double var26 = this.targetEntity.posX + var19.get(var23).x;
         double var9 = this.targetEntity.posZ + var19.get(var23).z;
         if (PlayerUtil.isOverVoid(
            new AxisAlignedBB(var26 - 0.015, mc.thePlayer.posY, var9 - 0.015, var26 + 0.015, mc.thePlayer.posY + (double)mc.thePlayer.height, var9 + 0.015)
         )) {
            this.strafeDirection = -this.strafeDirection;
            var23 = wrapIndex(var3 + this.strafeDirection, var19.size());
            var26 = this.targetEntity.posX + var19.get(var23).x;
            var9 = this.targetEntity.posZ + var19.get(var23).z;
         }

         double var11 = var26 - mc.thePlayer.posX;
         double var13 = var9 - mc.thePlayer.posZ;
         float var20 = var1.getPitch();
         float var22 = var1.getYaw();
         this.strafeYaw = RotationUtil.getRotations(var11, 0.0, var13, var22, var20)[0];
         var1.setPervRotation(this.strafeYaw, 10);
      }
   }

   public static int wrapIndex(int var0, int var1) {
      if (var0 < 0) {
         return var1 - 1;
      } else {
         return var0 >= var1 ? 0 : var0;
      }
   }

   @Override
   public void onDisabled() {
      this.targetEntity = null;
      this.strafeYaw = Float.NaN;
   }

   @EventTarget
   public void onRender(Render3DEvent var1) {
      if (this.isEnabled() && BotCheckUtil.isEntityLoaded((Entity)this.targetEntity) && (Integer)this.showTargetProp.getValue() != 0) {
          Color var4;
          EntityLivingBase var2 = this.targetEntity;
          if (var2 instanceof EntityPlayer && BotCheckUtil.isFriend((EntityPlayer)var2)) {
             var4 = Valor.friendManager.color;
          } else if (var2 instanceof EntityPlayer && BotCheckUtil.isTarget((EntityPlayer)var2)) {
             var4 = Valor.targetManager.color;
          } else {
             switch ((Integer)this.showTargetProp.getValue()) {
                case 1:
                   var4 = !(var2 instanceof EntityPlayer) ? Color.WHITE : BotCheckUtil.getTeamColor((EntityPlayer)var2, 1.0F);
                   break;
                case 2:
                   int var3 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), 0L).getRGB();
                   var4 = new Color(var3);
                   break;
                default:
                   var4 = new Color(-1);
             }
          }
         Render3DUtil.setupRender3D();
         Render3DUtil.drawCircleAroundEntity(this.targetEntity, (double)((Float)this.radiusProp.getValue()).floatValue(), (Integer)this.pointsProp.getValue(), ColorUtil.darken(var4).getRGB());
         Render3DUtil.drawCircleAroundEntity(this.targetEntity, (double)((Float)this.radiusProp.getValue()).floatValue(), (Integer)this.pointsProp.getValue(), var4.getRGB());
         Render3DUtil.endRender3D();
      }
   }

   @EventTarget
   public void onStrafe(StrafeEvent var1) {
      if (this.isEnabled() && !Float.isNaN(this.strafeYaw) && MoveUtil.isMoving()) {
         var1.setStrafe(0.0F);
         var1.setForward(1.0F);
      }
   }

   public TargetStrafe() {
      super("TargetStrafe", false, ModuleCategory.COMBAT);
   }

   public static class Point {
      public final double x;
      public final double z;

      public Point(double var1, double var3) {
         this.x = var1;
         this.z = var3;
      }
   }
}
