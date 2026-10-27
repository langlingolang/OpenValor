package valor.util;

import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import valor.Valor;
import valor.module.movement.KeepSprint;

public final class PlayerUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static boolean isBlockAirBelow(int depth) {
      if (!mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.disableDamage) {
         int floorY = MathHelper.floor_double(mc.thePlayer.posY);

         int currentY;
         for (int i = 0; i <= depth && (currentY = floorY - i) >= 0; i++) {
            if (!(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, (double)currentY, mc.thePlayer.posZ)).getBlock() instanceof BlockAir)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean willTakeFallDamage(float damageDistance) {
      if (!mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.disableDamage) {
         PotionEffect jumpEffect;
         float jumpModifier = (jumpEffect = mc.thePlayer.getActivePotionEffect(Potion.jump)) != null ? (float)(jumpEffect.getAmplifier() + 1) : 0.0F;
         float fallDist = mc.thePlayer.fallDistance;
         if (mc.thePlayer.motionY < -0.67 || !isBlockBelow()) {
            fallDist -= (float)mc.thePlayer.motionY;
         }

         return MathHelper.ceiling_float_int(fallDist - damageDistance - jumpModifier) > 0;
      } else {
         return false;
      }
   }

   public static boolean isOverVoid(AxisAlignedBB bb) {
      if (!mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()) {
         int minY;
         if ((minY = MathHelper.floor_double(bb.minY)) < 0) {
            return true;
         } else {
            int minX = MathHelper.floor_double(bb.minX);
            int maxX = MathHelper.floor_double(bb.maxX + 1.0);
            int minZ = MathHelper.floor_double(bb.minZ);

            for (int maxZ = MathHelper.floor_double(bb.maxZ + 1.0); minX < maxX; minX++) {
               for (int z = minZ; z < maxZ; z++) {
                  for (int y = minY; y >= 0; y--) {
                     if (!BlockUtil.isReplaceable(new BlockPos(minX, y, z))) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean canMoveTo(double offsetX, double offsetZ, double offsetY) {
      AxisAlignedBB offsetBB = mc.thePlayer.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ);
      return mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, offsetBB).isEmpty();
   }

   public static boolean isBlockInRange(BlockPos pos, double reach) {
      return isBlockInRange(pos, mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ, reach);
   }

   public static boolean isBlockInRange(BlockPos pos, double x, double y, double z, double reach) {
      return pos.distanceSqToCenter(x, y, z) < Math.pow(reach, 2.0);
   }

   public static void attackEntity(Entity target) {
      if (target.canAttackWithItem() && !target.hitByEntity(mc.thePlayer)) {
         float baseDamage = (float)mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
         float enchantDamage = EnchantmentHelper.getModifierForCreature(
            mc.thePlayer.getHeldItem(), target instanceof EntityLivingBase ? ((EntityLivingBase)target).getCreatureAttribute() : EnumCreatureAttribute.UNDEFINED
         );
         int knockbackModifier = EnchantmentHelper.getKnockbackModifier(mc.thePlayer);
         if (mc.thePlayer.isSprinting()) {
            knockbackModifier++;
         }

         if (baseDamage > 0.0F || enchantDamage > 0.0F) {
            boolean isCritical = mc.thePlayer.fallDistance > 0.0F
               && !mc.thePlayer.onGround
               && !mc.thePlayer.isOnLadder()
               && !mc.thePlayer.isInWater()
               && !mc.thePlayer.isPotionActive(Potion.blindness)
               && mc.thePlayer.ridingEntity == null
               && baseDamage > 0.0F;
            if (isCritical) {
               baseDamage *= 1.5F;
            }

            baseDamage += enchantDamage;
            boolean setFire = false;
            int fireAspect = EnchantmentHelper.getFireAspectModifier(mc.thePlayer);
            if (target instanceof EntityLivingBase && fireAspect > 0 && !target.isBurning()) {
               setFire = true;
               target.setFire(1);
            }

            double motionX = target.motionX;
            double motionY = target.motionY;
            double motionZ = target.motionZ;
            if (target.attackEntityFrom(DamageSource.causePlayerDamage(mc.thePlayer), baseDamage)) {
               if (knockbackModifier > 0) {
                  target.addVelocity(
                     (double)(-MathHelper.sin(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0F) * (float)knockbackModifier * 0.5F),
                     0.1,
                     (double)(MathHelper.cos(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0F) * (float)knockbackModifier * 0.5F)
                  );
                  KeepSprint keepSprint;
                  if ((keepSprint = (KeepSprint)Valor.moduleManager.modules.get(KeepSprint.class)).isEnabled() && keepSprint.shouldKeepSprint()) {
                     double slowdown = keepSprint.getSprintSlowdown(0.6);
                     mc.thePlayer.motionX *= slowdown;
                     mc.thePlayer.motionZ *= slowdown;
                     if (!keepSprint.shouldKeepSprint()) {
                        mc.thePlayer.setSprinting(false);
                     }
                  } else {
                     mc.thePlayer.motionX *= 0.6;
                     mc.thePlayer.motionZ *= 0.6;
                     mc.thePlayer.setSprinting(false);
                  }
               }

               if (target instanceof EntityPlayerMP && target.velocityChanged) {
                  ((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
                  target.velocityChanged = false;
                  target.motionX = motionX;
                  target.motionY = motionY;
                  target.motionZ = motionZ;
               }

               if (isCritical) {
                  mc.thePlayer.onCriticalHit(target);
               }

               if (enchantDamage > 0.0F) {
                  mc.thePlayer.onEnchantmentCritical(target);
               }

               if (baseDamage >= 18.0F) {
                  mc.thePlayer.triggerAchievement(AchievementList.overkill);
               }

               mc.thePlayer.setLastAttacker(target);
               if (target instanceof EntityLivingBase) {
                  EnchantmentHelper.applyThornEnchantments((EntityLivingBase)target, mc.thePlayer);
               }

               EnchantmentHelper.applyArthropodEnchantments(mc.thePlayer, target);
               if (target instanceof EntityLivingBase) {
                  mc.thePlayer.addStat(StatList.damageDealtStat, Math.round(baseDamage * 10.0F));
                  if (fireAspect > 0) {
                     target.setFire(fireAspect << 2);
                  }
               }

               mc.thePlayer.addExhaustion(0.3F);
               return;
            }

            if (setFire) {
               target.extinguish();
            }
         }
      }
   }

   public static boolean isSneakPressed() {
      return mc.currentScreen == null && KeyUtil.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
   }

   public static boolean isAttackPressed() {
      return mc.currentScreen == null && KeyUtil.isKeyDown(mc.gameSettings.keyBindAttack.getKeyCode());
   }

   public static boolean isJumpPressed() {
      return mc.currentScreen == null && KeyUtil.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
   }

   public static boolean isUseItemPressed() {
      return mc.currentScreen == null && KeyUtil.isKeyDown(mc.gameSettings.keyBindUseItem.getKeyCode());
   }

   public static boolean isRightPressed() {
      return mc.currentScreen == null && KeyUtil.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
   }

   public static boolean isOverVoid() {
      return isOverVoid(mc.thePlayer.getEntityBoundingBox().expand(-1.0E-6, 0.0, -1.0E-6));
   }

   public static boolean isLeftPressed() {
      return mc.currentScreen == null && KeyUtil.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
   }

   public static boolean isBlockBelow() {
      AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0, -1.0, 0.0);
      return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty();
   }

   public static boolean isBlockAbove() {
      AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0);
      return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty();
   }
}
