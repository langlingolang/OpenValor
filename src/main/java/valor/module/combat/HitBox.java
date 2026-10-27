package valor.module.combat;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import valor.Valor;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LeftClickMouseEvent;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.util.BotCheckUtil;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;

public class HitBox extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private MovingObjectPosition expandedMouseOver = null;
   public final FloatProperty multiplierProp = new FloatProperty("multiplier", 1.2F, 1.0F, 5.0F);
   public final ModeProperty showHitboxProp = new ModeProperty("show-hitbox", 0, new String[]{"NONE", "PLAYERS", "MOBS", "ANIMALS", "ALL"});
   public final ColorProperty colorProp = new ColorProperty("color", new Color(255, 255, 255).getRGB(), () -> (Integer)this.showHitboxProp.getValue() != 0);
   public final BooleanProperty teamsProp = new BooleanProperty("teams", Boolean.TRUE, () -> (Integer)this.showHitboxProp.getValue() == 1 || (Integer)this.showHitboxProp.getValue() == 4);
   public final BooleanProperty botCheckProp = new BooleanProperty("bot-check", Boolean.TRUE, () -> (Integer)this.showHitboxProp.getValue() == 1 || (Integer)this.showHitboxProp.getValue() == 4);

@Override
   public String[] getSuffix() {
      return new String[]{String.format("%.1fx", this.multiplierProp.getValue())};
   }

   private boolean isValidTarget(EntityLivingBase var1) {
      if (var1 == mc.thePlayer) {
         return false;
      } else if (var1.deathTime <= 0 && !(var1 instanceof EntityArmorStand) && !var1.isInvisible()) {
         if (mc.getRenderViewEntity().getDistanceToEntity(var1) > 128.0F) {
            return false;
         } else if (!var1.ignoreFrustumCheck && !Render3DUtil.isBoundingBoxInFrustum(var1.getEntityBoundingBox(), 0.1F)) {
            return false;
         } else {
            switch (this.showHitboxProp.getValue()) {
               case 0:
                  return false;
               case 1:
                  if (var1 instanceof EntityPlayer) {
                     EntityPlayer var3;
                     if (BotCheckUtil.isFriend(var3 = (EntityPlayer)var1)) {
                        return false;
                     }

                     if ((Boolean)this.teamsProp.getValue() && BotCheckUtil.isTeammate(var3)) {
                        return false;
                     }

                     if ((Boolean)this.botCheckProp.getValue() && BotCheckUtil.isBot(var3)) {
                        return false;
                     }

                     return true;
                  }

                  return false;
               case 2:
                  if (!(var1 instanceof EntityDragon) && !(var1 instanceof EntityWither)) {
                     if (!(var1 instanceof EntityMob) && !(var1 instanceof EntitySlime)) {
                        return false;
                     }

                     if (!(var1 instanceof EntitySilverfish)) {
                        return true;
                     }

                     return false;
                  }

                  return true;
               case 3:
                  if (!(var1 instanceof EntityAnimal)
                     && !(var1 instanceof EntityBat)
                     && !(var1 instanceof EntitySquid)
                     && !(var1 instanceof EntityVillager)
                     && !(var1 instanceof EntityIronGolem)) {
                     return false;
                  }

                  return true;
               case 4:
                  if (var1 instanceof EntityPlayer) {
                     EntityPlayer var2;
                     if (BotCheckUtil.isFriend(var2 = (EntityPlayer)var1)) {
                        return false;
                     }

                     if ((Boolean)this.teamsProp.getValue() && BotCheckUtil.isTeammate(var2)) {
                        return false;
                     }

                     if ((Boolean)this.botCheckProp.getValue() && BotCheckUtil.isBot(var2)) {
                        return false;
                     }
                  }

                  return true;
               default:
                  return false;
            }
         }
      } else {
         return false;
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE && mc.getRenderViewEntity() != null && mc.theWorld != null) {
         mc.pointedEntity = null;
         Entity var2 = null;
         this.expandedMouseOver = mc.getRenderViewEntity().rayTrace(3.0, 1.0F);
         double var5 = 3.0;
         Vec3 var3 = mc.getRenderViewEntity().getPositionEyes(1.0F);
         if (this.expandedMouseOver != null) {
            var5 = this.expandedMouseOver.hitVec.distanceTo(var3);
         }

         Vec3 var4 = mc.getRenderViewEntity().getLook(1.0F);
         Vec3 var7 = var3.addVector(var4.xCoord * 3.0, var4.yCoord * 3.0, var4.zCoord * 3.0);
         Vec3 var8 = null;
         List var16 = mc.theWorld
            .getEntitiesWithinAABBExcludingEntity(
               mc.getRenderViewEntity(),
               mc.getRenderViewEntity().getEntityBoundingBox().addCoord(var4.xCoord * 3.0, var4.yCoord * 3.0, var4.zCoord * 3.0).expand(1.0, 1.0, 1.0)
            );
         double var12 = var5;
         Iterator var17 = var16.iterator();

         while (var17.hasNext()) {
            Entity var9;
            if ((var9 = (Entity)var17.next()).canBeCollidedWith()) {
               HitBox var11;
               float var10 = (float)(
                  (double)var9.getCollisionBorderSize()
                     * (double)(
                        (var11 = (HitBox)Valor.moduleManager.modules.get(HitBox.class)) != null && var11.isEnabled() && var9 instanceof EntityLivingBase
                           ? (Float)var11.multiplierProp.getValue()
                           : 1.0F
                     )
               );
               AxisAlignedBB var18;
               MovingObjectPosition var19 = (var18 = var9.getEntityBoundingBox().expand((double)var10, (double)var10, (double)var10))
                  .calculateIntercept(var3, var7);
               if (var18.isVecInside(var3)) {
                  if (0.0 < var12 || var12 == 0.0) {
                     var2 = var9;
                     var8 = var19 == null ? var3 : var19.hitVec;
                     var12 = 0.0;
                  }
               } else {
                  double var14;
                  if (var19 != null && ((var14 = var3.distanceTo(var19.hitVec)) < var12 || var12 == 0.0)) {
                     if (var9 != mc.getRenderViewEntity().ridingEntity || var9.canRiderInteract()) {
                        var2 = var9;
                        var8 = var19.hitVec;
                        var12 = var14;
                     } else if (var12 == 0.0) {
                        var2 = var9;
                        var8 = var19.hitVec;
                     }
                  }
               }
            }
         }

         if (var2 != null && (var12 < var5 || this.expandedMouseOver == null)) {
            this.expandedMouseOver = new MovingObjectPosition(var2, var8);
            if (var2 instanceof EntityLivingBase || var2 instanceof EntityItemFrame) {
               mc.pointedEntity = var2;
            }
         }
      }
   }

   @EventTarget
   public void onRender(Render3DEvent var1) {
      List var2;
      if (this.isEnabled()
         && (Integer)this.showHitboxProp.getValue() != 0
         && !(var2 = mc.theWorld
               .loadedEntityList
               .stream()
               .filter(var0 -> var0 instanceof EntityLivingBase)
               .map(var0 -> (EntityLivingBase)var0)
               .filter(this::isValidTarget)
               .collect(Collectors.toList()))
            .isEmpty()) {
         Render3DUtil.setupRender3D();
         Color var3 = new Color((Integer)this.colorProp.getValue());
         Iterator var6 = var2.iterator();

         while (var6.hasNext()) {
            EntityLivingBase var4;
            float var5 = (float)((double)(var4 = (EntityLivingBase)var6.next()).getCollisionBorderSize() * (double)((Float)this.multiplierProp.getValue()).floatValue());
            AxisAlignedBB var7 = var4.getEntityBoundingBox().expand((double)var5, (double)var5, (double)var5);
            Render3DUtil.drawOutlinedBox(
               new AxisAlignedBB(
                  var7.minX
                     - var4.posX
                     + (Render3DUtil.interpolate(var4.posX, var4.lastTickPosX, (double)var1.getPartialTicks()) - ReflectionUtil.getRenderPosX(mc.getRenderManager())),
                  var7.minY
                     - var4.posY
                     + (Render3DUtil.interpolate(var4.posY, var4.lastTickPosY, (double)var1.getPartialTicks()) - ReflectionUtil.getRenderPosY(mc.getRenderManager())),
                  var7.minZ
                     - var4.posZ
                     + (Render3DUtil.interpolate(var4.posZ, var4.lastTickPosZ, (double)var1.getPartialTicks()) - ReflectionUtil.getRenderPosZ(mc.getRenderManager())),
                  var7.maxX
                     - var4.posX
                     + (Render3DUtil.interpolate(var4.posX, var4.lastTickPosX, (double)var1.getPartialTicks()) - ReflectionUtil.getRenderPosX(mc.getRenderManager())),
                  var7.maxY
                     - var4.posY
                     + (Render3DUtil.interpolate(var4.posY, var4.lastTickPosY, (double)var1.getPartialTicks()) - ReflectionUtil.getRenderPosY(mc.getRenderManager())),
                  var7.maxZ
                     - var4.posZ
                     + (Render3DUtil.interpolate(var4.posZ, var4.lastTickPosZ, (double)var1.getPartialTicks()) - ReflectionUtil.getRenderPosZ(mc.getRenderManager()))
               ),
               var3.getRed(),
               var3.getGreen(),
               var3.getBlue(),
               150,
               1.5F
            );
         }

         Render3DUtil.endRender3D();
      }
   }

   @EventTarget(1)
   public void onLeftClick(LeftClickMouseEvent var1) {
      if (this.isEnabled() && !var1.isCancelled() && this.expandedMouseOver != null) {
         mc.objectMouseOver = this.expandedMouseOver;
      }
   }

   public HitBox() {
      super("HitBox", false, ModuleCategory.COMBAT);
   }
}
