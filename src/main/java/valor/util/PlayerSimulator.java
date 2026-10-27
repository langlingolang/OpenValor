package valor.util;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.Vec3;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public final class PlayerSimulator {
   public static final Minecraft mc = Minecraft.getMinecraft();
   public final EntityPlayerSP player;
   public AxisAlignedBB boundingBox;
   public final MovementInput movementInput;
   public int jumpTicks;
   public double motionZ;
   public double motionY;
   public double motionX;
   public boolean inWater;
   public boolean onGround;
   public boolean isAirBorne;
   public float rotationYaw;
   private float rotationPitch;
   public double posX;
   public double posY;
   public double posZ;
   public final PlayerCapabilities capabilities;
   public final Entity ridingEntity;
   public float jumpMovementFactor;
   public final World world;
   public boolean isCollidedHorizontally;
   private boolean isCollidedVertically;
   private final WorldBorder worldBorder;
   private final IChunkProvider chunkProvider;
   private boolean isCollidedVerticallyPrev;
   private final Entity riddenByEntity;
   private BaseAttributeMap attributeMap;
   public final boolean isSpectator;
   public float fallDistance;
   private final float stepHeight;
   private boolean isCollided;
   public int fire;
   private float distanceWalkedModified;
   private float distanceWalkedOnStepModified;
   private int nextStepDistance;
   private float height;
   public final float width;
   private final int fireResistance;
   private boolean isInWeb;
   private boolean isDead;
   public boolean noClip;
   public boolean isSprinting;
   public final FoodStats foodStats;
   public float moveStrafing = 0.0F;
   public float moveForward = 0.0F;
   public boolean jumping = false;
   private boolean sneaking = false;

   private float getAIMoveSpeed() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
   }

   private boolean isRainingAt(BlockPos var1) {
      if ((double)this.world.getRainStrength(1.0F) <= 0.2) {
         return false;
      } else if (!this.canSeeSky(var1)) {
         return false;
      } else if (this.world.getPrecipitationHeight(var1).getY() > var1.getY()) {
         return false;
      } else {
         BiomeGenBase var2;
         if ((var2 = this.world.getBiomeGenForCoords(var1)).getEnableSnow()) {
            return false;
         } else {
            return this.world.canSnowAt(var1, false) ? false : var2.canRain();
         }
      }
   }

   public final void moveEntityWithHeading(float var1, float var2) {
      if (this.inWater && !this.capabilities.isFlying) {
         double var11 = this.posY;
         float var12 = 0.8F;
         float var7 = 0.02F;
         float var5;
         if ((var5 = (float)EnchantmentHelper.getDepthStriderModifier(this.player)) > 3.0F) {
            var5 = 3.0F;
         }

         if (!this.onGround) {
            var5 *= 0.5F;
         }

         if (var5 > 0.0F) {
            var12 = 0.8F + -0.25399995F * var5 / 3.0F;
            var7 = 0.02F + (this.getAIMoveSpeed() - 0.02F) * var5 / 3.0F;
         }

         this.moveFlying(var1, var2, var7);
         this.setEntityBoundingBoxPosition(this.motionX, this.motionY, this.motionZ);
         this.motionX *= (double)var12;
         this.motionY *= 0.8F;
         this.motionZ *= (double)var12;
         this.motionY -= 0.02;
         if (this.isCollidedHorizontally && this.isOffsetColliding(this.motionX, this.motionY + 0.6F - this.posY + var11, this.motionZ)) {
            this.motionY = 0.3F;
         }
      } else {
         if (!this.isInLava() || this.capabilities.isFlying) {
            float var8 = 0.91F;
            if (this.onGround) {
               var8 = this.world
                     .getBlockState(
                        new BlockPos(
                           MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)
                        )
                     )
                     .getBlock()
                     .slipperiness
                  * 0.91F;
            }

            var8 = 0.16277136F / (var8 * var8 * var8);
            float var6 = this.onGround ? this.getAIMoveSpeed() * var8 : this.jumpMovementFactor;
            this.moveFlying(var1, var2, var6);
            var8 = 0.91F;
            if (this.onGround) {
               var8 = this.world
                     .getBlockState(
                        new BlockPos(
                           MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)
                        )
                     )
                     .getBlock()
                     .slipperiness
                  * 0.91F;
            }

            if (this.isOnLadder()) {
               this.motionX = MathHelper.clamp_double(this.motionX, -0.15F, 0.15F);
               this.motionZ = MathHelper.clamp_double(this.motionZ, -0.15F, 0.15F);
               this.fallDistance = 0.0F;
               if (this.motionY < -0.15) {
                  this.motionY = -0.15;
               }

               if (this.isSneaking() && this.motionY < 0.0) {
                  this.motionY = 0.0;
               }
            }

            this.setEntityBoundingBoxPosition(this.motionX, this.motionY, this.motionZ);
            if (this.isCollidedHorizontally && this.isOnLadder()) {
               this.motionY = 0.2;
            }

            if (this.world.isRemote
               && (
                  !this.world.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ))
                     || !this.world.getChunkFromBlockCoords(new BlockPos(this.posX, 0.0, this.posZ)).isLoaded()
               )) {
               this.motionY = this.posY > 0.0 ? -0.1 : 0.0;
            } else {
               this.motionY -= 0.08;
            }

            this.motionY *= 0.98F;
            this.motionX *= (double)var8;
            this.motionZ *= (double)var8;
            return;
         }

         double var3 = this.posY;
         this.moveFlying(var1, var2, 0.02F);
         this.setEntityBoundingBoxPosition(this.motionX, this.motionY, this.motionZ);
         this.motionX *= 0.5;
         this.motionY *= 0.5;
         this.motionZ *= 0.5;
         this.motionY -= 0.02;
         if (this.isCollidedHorizontally && this.isOffsetColliding(this.motionX, this.motionY + 0.6F - this.posY + var3, this.motionZ)) {
            this.motionY = 0.3F;
            return;
         }
      }
   }

   private void setEntityBoundingBoxPosition(double var1, double var3, double var5) {
      double var7 = var1;
      double var9 = var3;
      double var11 = var5;
      if (this.isDead) {
         this.boundingBox = this.boundingBox.offset(var1, var3, var5);
         this.resetPositionToBB();
      } else {
         double var13 = this.posX;
         double var15 = this.posY;
         double var17 = this.posZ;
         if (this.isInWeb) {
            this.isInWeb = false;
            var7 = var1 * 0.25;
            var9 = var3 * 0.05F;
            var11 = var5 * 0.25;
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
         }

         double var19 = var7;
         double var21 = var9;
         double var23 = var11;
         boolean var52;
         if (var52 = this.onGround && this.isSneaking()) {
            double[] var2;
            var19 = (var2 = this.calculateMotion(var7, var11))[0];
            var23 = var2[1];
         }

         List var53 = this.world.getCollidingBoundingBoxes(this.player, this.boundingBox.addCoord(var7, var9, var11));
         AxisAlignedBB var60 = this.boundingBox;
         Iterator var4 = var53.iterator();

         while (var4.hasNext()) {
            var9 = ((AxisAlignedBB)var4.next()).calculateYOffset(this.boundingBox, var9);
         }

         this.boundingBox = this.boundingBox.offset(0.0, var9, 0.0);
         boolean var61 = this.onGround || var21 != var9 && var21 < 0.0;
         Iterator var25 = var53.iterator();

         while (var25.hasNext()) {
            var7 = ((AxisAlignedBB)var25.next()).calculateXOffset(this.boundingBox, var7);
         }

         this.boundingBox = this.boundingBox.offset(var7, 0.0, 0.0);
         var25 = var53.iterator();

         while (var25.hasNext()) {
            var11 = ((AxisAlignedBB)var25.next()).calculateZOffset(this.boundingBox, var11);
         }

         this.boundingBox = this.boundingBox.offset(0.0, 0.0, var11);
         if (this.stepHeight > 0.0F && var61 && (var19 != var7 || var23 != var11)) {
            double var64 = var7;
            double var27 = var9;
            double var29 = var11;
            AxisAlignedBB var31 = this.boundingBox;
            this.boundingBox = var60;
            var9 = (double)this.stepHeight;
            List var32 = this.world.getCollidingBoundingBoxes(this.player, this.boundingBox.addCoord(var19, var9, var23));
            AxisAlignedBB var33 = this.boundingBox;
            AxisAlignedBB var34 = this.boundingBox.addCoord(var19, 0.0, var23);
            double var35 = var9;
            Iterator var37 = var32.iterator();

            while (var37.hasNext()) {
               var35 = ((AxisAlignedBB)var37.next()).calculateYOffset(var34, var35);
            }

            var33 = var33.offset(0.0, var35, 0.0);
            double var75 = var19;
            Iterator var39 = var32.iterator();

            while (var39.hasNext()) {
               var75 = ((AxisAlignedBB)var39.next()).calculateXOffset(var33, var75);
            }

            var33 = var33.offset(var75, 0.0, 0.0);
            double var76 = var23;
            Iterator var54 = var32.iterator();

            while (var54.hasNext()) {
               var76 = ((AxisAlignedBB)var54.next()).calculateZOffset(var33, var76);
            }

            var33 = var33.offset(0.0, 0.0, var76);
            AxisAlignedBB var55 = this.boundingBox;
            double var41 = var9;
            Iterator var43 = var32.iterator();

            while (var43.hasNext()) {
               var41 = ((AxisAlignedBB)var43.next()).calculateYOffset(var55, var41);
            }

            AxisAlignedBB var56 = var55.offset(0.0, var41, 0.0);
            double var77 = var19;
            Iterator var45 = var32.iterator();

            while (var45.hasNext()) {
               var77 = ((AxisAlignedBB)var45.next()).calculateXOffset(var56, var77);
            }

            AxisAlignedBB var57 = var56.offset(var77, 0.0, 0.0);
            double var78 = var23;
            Iterator var47 = var32.iterator();

            while (var47.hasNext()) {
               var78 = ((AxisAlignedBB)var47.next()).calculateZOffset(var57, var78);
            }

            AxisAlignedBB var58 = var57.offset(0.0, 0.0, var78);
            double var79 = var75 * var75 + var76 * var76;
            double var49 = var77 * var77 + var78 * var78;
            if (var79 > var49) {
               var7 = var75;
               var11 = var76;
               var9 = -var35;
               this.boundingBox = var33;
            } else {
               var7 = var77;
               var11 = var78;
               var9 = -var41;
               this.boundingBox = var58;
            }

            Iterator var59 = var32.iterator();

            while (var59.hasNext()) {
               var9 = ((AxisAlignedBB)var59.next()).calculateYOffset(this.boundingBox, var9);
            }

            this.boundingBox = this.boundingBox.offset(0.0, var9, 0.0);
            if (var64 * var64 + var29 * var29 >= var7 * var7 + var11 * var11) {
               var7 = var64;
               var9 = var27;
               var11 = var29;
               this.boundingBox = var31;
            }
         }

         this.resetPositionToBB();
         this.isCollidedHorizontally = var19 != var7 || var23 != var11;
         this.isCollidedVertically = var21 != var9;
         this.onGround = this.isCollidedVertically && var21 < 0.0;
         this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
         int var65 = MathHelper.floor_double(this.posX);
         int var26 = MathHelper.floor_double(this.posY - 0.2F);
         int var66 = MathHelper.floor_double(this.posZ);
         BlockPos var28 = new BlockPos(var65, var26, var66);
         Block var30;
         Block var67;
         if ((var67 = this.world.getBlockState(var28).getBlock()).getMaterial() == Material.air
            && (
               (var30 = this.world.getBlockState(var28.down()).getBlock()) instanceof BlockFence
                  || var30 instanceof BlockWall
                  || var30 instanceof BlockFenceGate
            )) {
            var67 = var30;
         }

         this.updateFallState(var9, this.onGround);
         if (var19 != var7) {
            this.motionX = 0.0;
         }

         if (var23 != var11) {
            this.motionZ = 0.0;
         }

         if (var21 != var9) {
            this.handleSlimeBlockBounce(var67);
         }

         if (this.canTriggerWalking() && !var52 && this.ridingEntity == null) {
            double var68 = this.posX - var13;
            double var70 = this.posY - var15;
            double var74 = this.posZ - var17;
            if (var67 != Blocks.ladder) {
               var70 = 0.0;
            }

            if (var67 != null && this.onGround) {
               this.playStepSound(var67);
            }

            this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt_double(var68 * var68 + var74 * var74) * 0.6);
            this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt_double(var68 * var68 + var70 * var70 + var74 * var74) * 0.6);
            if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && var67.getMaterial() != Material.air) {
               this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;
            }
         }

         try {
            this.doBlockCollisions();
         } catch (Throwable var51) {
            var51.printStackTrace();
         }

         boolean var69 = this.isWet();
         if (this.world.isFlammableWithin(this.boundingBox.contract(0.001, 0.001, 0.001))) {
            if (!var69) {
               this.fire++;
               if (this.fire == 0) {
                  this.setJumpTicks(8);
               }
            }
         } else if (this.fire <= 0) {
            this.fire = -this.fireResistance;
         }

         if (var69 && this.fire > 0) {
            this.fire = -this.fireResistance;
         }
      }
   }

   private void moveFlying(float var1, float var2, float var3) {
      float var4;
      if ((var4 = var1 * var1 + var2 * var2) >= 1.0E-4F) {
         if ((var4 = MathHelper.sqrt_float(var4)) < 1.0F) {
            var4 = 1.0F;
         }

         var4 = var3 / var4;
         var1 *= var4;
         var2 *= var4;
         var3 = MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F);
         var4 = MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F);
         this.motionX += (double)(var1 * var4 - var2 * var3);
         this.motionZ += (double)(var2 * var4 + var1 * var3);
      }
   }

   private boolean checkNoEntityCollision(AxisAlignedBB var1) {
      return this.world.getCollidingBoundingBoxes(this.player, var1).isEmpty() && !this.world.isAnyLiquid(var1);
   }

   private boolean isOffsetColliding(double var1, double var3, double var5) {
      return this.checkNoEntityCollision(this.boundingBox.offset(var1, var3, var5));
   }

   private void handleSlimeBlockBounce(Block var1) {
      if (var1 instanceof BlockSlime && !this.isSneaking()) {
         if (this.motionY < 0.0) {
            this.motionY = -this.motionY;
            return;
         }
      } else {
         this.motionY = 0.0;
      }
   }

   public final boolean handleWaterMovement() {
      if (this.isMaterialInBB(this.boundingBox.expand(0.0, -0.4F, 0.0).contract(0.001, 0.001, 0.001), Material.water)) {
         this.fallDistance = 0.0F;
         this.inWater = true;
         this.fire = 0;
      } else {
         this.inWater = false;
      }

      return this.inWater;
   }

   private boolean isWet() {
      return this.inWater || this.isRainingAt(new BlockPos(this.posX, this.posY, this.posZ)) || this.isRainingAt(new BlockPos(this.posX, this.posY + (double)this.height, this.posZ));
   }

   private boolean canTriggerWalking() {
      return !this.capabilities.isFlying;
   }

   private void resetPositionToBB() {
      this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0;
      this.posY = this.boundingBox.minY;
      this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0;
   }

   private void doBlockCollisions() {
      BlockPos var1 = new BlockPos(this.boundingBox.minX + 0.001, this.boundingBox.minY + 0.001, this.boundingBox.minZ + 0.001);
      BlockPos var2 = new BlockPos(this.boundingBox.maxX - 0.001, this.boundingBox.maxY - 0.001, this.boundingBox.maxZ - 0.001);
      if (this.isAreaLoaded(var1.getX(), var1.getY(), var1.getZ(), var2.getX(), var2.getY(), var2.getZ())) {
         for (int var3 = var1.getX(); var3 <= var2.getX(); var3++) {
            for (int var4 = var1.getY(); var4 <= var2.getY(); var4++) {
               for (int var5 = var1.getZ(); var5 <= var2.getZ(); var5++) {
                  BlockPos var6 = new BlockPos(var3, var4, var5);
                  IBlockState var8 = this.world.getBlockState(var6);

                  try {
                     Block var9;
                     if ((var9 = var8.getBlock()) instanceof BlockWeb) {
                        this.isInWeb = true;
                     } else if (var9 instanceof BlockSoulSand) {
                        this.motionX *= 0.4;
                        this.motionZ *= 0.4;
                     }
                  } catch (Throwable var7) {
                  }
               }
            }
         }
      }
   }

   private boolean isBlockNormalCube(BlockPos var1) {
      return this.getBlockState(var1).getBlock().isNormalCube();
   }

   private boolean canSeeSky(BlockPos var1) {
      return this.getChunkFromBlockCoords(var1).canSeeSky(var1);
   }

   private BaseAttributeMap getAttributeMap() {
      if (this.attributeMap == null) {
         this.attributeMap = new ServersideAttributeMap();
      }

      return this.attributeMap;
   }

   private IAttributeInstance getEntityAttribute(IAttribute var1) {
      return this.getAttributeMap().getAttributeInstance(var1);
   }

   public final boolean isPotionActive(Potion var1) {
      return this.player.getActivePotionEffect(var1) != null;
   }

   private IBlockState getBlockState(BlockPos var1) {
      return this.world.getBlockState(var1);
   }

   private Chunk getChunkFromBlockCoords(BlockPos var1) {
      int var2 = var1.getZ() >> 4;
      int var3 = var1.getX() >> 4;
      return this.chunkProvider.provideChunk(var3, var2);
   }

   private boolean isChunkLoaded(int var1, int var2) {
      return this.chunkProvider.chunkExists(var1, var2);
   }

   private boolean isAreaLoaded(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var5 >= 0 && var2 < 256) {
         var1 >>= 4;
         var3 >>= 4;
         var4 >>= 4;

         for (int var11 = var6 >> 4; var1 <= var4; var1++) {
            for (int var8 = var3; var8 <= var11; var8++) {
               if (!this.isChunkLoaded(var1, var8)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isMaterialInBB(AxisAlignedBB var1, Material var2) {
      int var3 = MathHelper.floor_double(var1.minX);
      int var4 = MathHelper.floor_double(var1.maxX + 1.0);
      int var5 = MathHelper.floor_double(var1.minY);
      int var6 = MathHelper.floor_double(var1.maxY + 1.0);
      int var7 = MathHelper.floor_double(var1.minZ);
      int var17 = MathHelper.floor_double(var1.maxZ + 1.0);
      if (!this.isAreaLoaded(var3, var5, var7, var4, var6, var17)) {
         return false;
      } else {
         boolean var8 = false;
         Vec3 var9 = new Vec3(0.0, 0.0, 0.0);

         for (MutableBlockPos var10 = new MutableBlockPos(); var3 < var4; var3++) {
            for (int var11 = var5; var11 < var6; var11++) {
               for (int var12 = var7; var12 < var17; var12++) {
                  var10.set(var3, var11, var12);
                  IBlockState var13;
                  Block var14;
                  if ((var13 = this.getBlockState(var10)) != null && (var14 = var13.getBlock()) != null && var14.getMaterial() == var2) {
                     double var15 = (double)((float)(var11 + 1) - BlockLiquid.getLiquidHeightPercent(var13.getValue(BlockLiquid.LEVEL)));
                     if ((double)var6 >= var15) {
                        var8 = true;
                        var9 = var14.modifyAcceleration(this.world, var10, this.player, var9);
                     }
                  }
               }
            }
         }

         if (var9.lengthVector() > 0.0 && this.isSprinting()) {
            var9 = var9.normalize();
            this.motionX = this.motionX + var9.xCoord * 0.014;
            this.motionY = this.motionY + var9.yCoord * 0.014;
            this.motionZ = this.motionZ + var9.zCoord * 0.014;
         }

         return var8;
      }
   }

   private void updateFallState(double var1, boolean var3) {
      if (!this.inWater) {
         this.handleWaterMovement();
      }

      if (var3) {
         if (this.fallDistance > 0.0F) {
            this.fallDistance = 0.0F;
            return;
         }
      } else if (var1 < 0.0) {
         this.fallDistance = (float)((double)this.fallDistance - var1);
      }
   }

   private void moveEntity(double var1, double var3, double var5) {
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      float var7 = this.width / 2.0F;
      this.boundingBox = new AxisAlignedBB(
         var1 - (double)var7, var3, var5 - (double)var7, var1 + (double)var7, var3 + (double)this.height, var5 + (double)var7
      );
   }

   public final boolean pushOutOfBlocks(double var1, double var3, double var5) {
      if (this.isDead) {
         return false;
      } else {
         BlockPos var14 = new BlockPos(var1, var3, var5);
         double var7 = var1 - (double)var14.getX();
         double var9 = var5 - (double)var14.getZ();
         int var13 = (int)Math.ceil((double)this.height);
         if (!this.isBlockAbove(var14, var13)) {
            byte var2 = -1;
            double var11 = 9999.0;
            if (this.isBlockAbove(var14.west(), var13) && var7 < 9999.0) {
               var11 = var7;
               var2 = 0;
            }

            if (this.isBlockAbove(var14.east(), var13) && 1.0 - var7 < var11) {
               var11 = 1.0 - var7;
               var2 = 1;
            }

            if (this.isBlockAbove(var14.north(), var13) && var9 < var11) {
               var2 = 4;
            }

            if (this.isBlockAbove(var14.south(), var13)) {
               var2 = 5;
            }

            if (var2 == 0) {
               this.motionX = -0.1F;
            }

            if (var2 == 1) {
               this.motionX = 0.1F;
            }

            if (var2 == 4) {
               this.motionZ = -0.1F;
            }

            if (var2 == 5) {
               this.motionZ = 0.1F;
            }
         }

         return false;
      }
   }

   private boolean isBlockAbove(BlockPos var1, int var2) {
      for (int var3 = 0; var3 < var2; var3++) {
         if (!this.isBlockNormalCube(var1.add(0, var3, 0))) {
            return false;
         }
      }

      return true;
   }

   public static PlayerSimulator simulateTick(MovementInput var0) {
      EntityPlayerSP var1 = mc.thePlayer;
      EntityPlayerSP var3 = mc.thePlayer;
      NBTTagCompound var4 = new NBTTagCompound();
      PlayerCapabilities var5 = new PlayerCapabilities();
      var3.capabilities.writeCapabilitiesToNBT(var4);
      var5.readCapabilitiesFromNBT(var4);
      var5.isFlying = false;
      var5.allowFlying = false;
      var4 = new NBTTagCompound();
      FoodStats var8 = new FoodStats();
      var1.getFoodStats().writeNBT(var4);
      var8.readNBT(var4);
      MovementInput var7;
      (var7 = new MovementInput()).jump = var0.jump;
      var7.moveForward = var0.moveForward;
      var7.moveStrafe = var0.moveStrafe;
      var7.sneak = var0.sneak;
      return new PlayerSimulator(
         var1,
         var1.getEntityBoundingBox(),
         var7,
         ReflectionUtil.getJumpTicks((EntityLivingBase)var1),
         var1.motionZ,
         var1.motionY,
         var1.motionX,
         var1.isInWater(),
         var1.onGround,
         var1.isAirBorne,
         var1.rotationYaw,
         var1.rotationPitch,
         var1.posX,
         var1.posY,
         var1.posZ,
         var5,
         var1.ridingEntity,
         var1.jumpMovementFactor,
         var1.worldObj,
         var1.isCollidedHorizontally,
         var1.isCollidedVertically,
         var1.worldObj.getWorldBorder(),
         var1.worldObj.getChunkProvider(),
         var1.riddenByEntity,
         var1.getAttributeMap(),
         var1.isSpectator(),
         var1.fallDistance,
         var1.stepHeight,
         var1.isCollided,
         ReflectionUtil.getFire((Entity)var1),
         var1.distanceWalkedModified,
         var1.distanceWalkedOnStepModified,
         ReflectionUtil.getNextStepDistance((Entity)var1),
         var1.height,
         var1.width,
         var1.fireResistance,
         ReflectionUtil.isInWeb(var1),
         var1.noClip,
         var1.isSprinting(),
         var8
      );
   }

   private double[] calculateMotion(double var1, double var3) {
      EntityPlayerSP var5 = mc.thePlayer;
      World var6 = mc.thePlayer.worldObj;
      double var7 = var1;

      double var9;
      for (var9 = var3; var1 != 0.0 && var6.getCollidingBoundingBoxes(var5, this.boundingBox.offset(var1, -1.0, 0.0)).isEmpty(); var7 = var1) {
         if (var1 < 0.05 && var1 >= -0.05) {
            var1 = 0.0;
         } else if (var1 > 0.0) {
            var1 -= 0.05;
         } else {
            var1 += 0.05;
         }
      }

      for (; var3 != 0.0 && var6.getCollidingBoundingBoxes(var5, this.boundingBox.offset(0.0, -1.0, var3)).isEmpty(); var9 = var3) {
         if (var3 < 0.05 && var3 >= -0.05) {
            var3 = 0.0;
         } else if (var3 > 0.0) {
            var3 -= 0.05;
         } else {
            var3 += 0.05;
         }
      }

      for (; var1 != 0.0 && var3 != 0.0 && var6.getCollidingBoundingBoxes(var5, this.boundingBox.offset(var1, -1.0, var3)).isEmpty(); var9 = var3) {
         if (var1 < 0.05 && var1 >= -0.05) {
            var1 = 0.0;
         } else if (var1 > 0.0) {
            var1 -= 0.05;
         } else {
            var1 += 0.05;
         }

         var7 = var1;
         if (var3 < 0.05 && var3 >= -0.05) {
            var3 = 0.0;
         } else if (var3 > 0.0) {
            var3 -= 0.05;
         } else {
            var3 += 0.05;
         }
      }

      return new double[]{var7, var9};
   }

   private void playStepSound(Block var1) {
      if (var1 instanceof BlockSlime && Math.abs(this.motionY) < 0.1 && !this.isSneaking()) {
         double var2 = 0.4 + Math.abs(this.motionY) * 0.2;
         this.motionX *= var2;
         this.motionZ *= var2;
      }
   }

   private boolean isOnLadder() {
      int var1 = MathHelper.floor_double(this.posX);
      int var2 = MathHelper.floor_double(this.boundingBox.minY);
      int var3 = MathHelper.floor_double(this.posZ);
      Block var4;
      return ((var4 = this.world.getBlockState(new BlockPos(var1, var2, var3)).getBlock()) == Blocks.ladder || var4 == Blocks.vine) && !this.isSpectator;
   }

   public final boolean isInLava() {
      return this.world.isMaterialInBB(this.boundingBox.expand(-0.1F, -0.4F, -0.1F), Material.lava);
   }

   private void jump() {
      double var1 = MathHelper.clamp_double(this.posX, -2.9999999E7, 2.9999999E7);
      double var3 = MathHelper.clamp_double(this.posZ, -2.9999999E7, 2.9999999E7);
      if (var1 != this.posX || var3 != this.posZ) {
         this.moveEntity(var1, this.posY, var3);
      }
   }

   public final void setSprinting(boolean var1) {
      if (!var1) {
         this.isDead = this.isSpectator;
         if (this.isSpectator) {
            this.onGround = false;
            return;
         }
      } else {
         this.jump();
      }
   }

   public final void setJumpTicks(int var1) {
      var1 *= 20;
      var1 = EnchantmentProtection.getFireTimeForEntity(this.player, var1);
      if (this.fire < var1) {
         this.fire = var1;
      }
   }

   private boolean isSneaking() {
      return this.movementInput.sneak && !this.player.isPlayerSleeping();
   }

   private boolean isSprinting() {
      return !this.capabilities.isFlying;
   }

   private PlayerSimulator(
      EntityPlayerSP var1,
      AxisAlignedBB var2,
      MovementInput var3,
      int var4,
      double var5,
      double var7,
      double var9,
      boolean var11,
      boolean var12,
      boolean var13,
      float var14,
      float var15,
      double var16,
      double var18,
      double var20,
      PlayerCapabilities var22,
      Entity var23,
      float var24,
      World var25,
      boolean var26,
      boolean var27,
      WorldBorder var28,
      IChunkProvider var29,
      Entity var30,
      BaseAttributeMap var31,
      boolean var32,
      float var33,
      float var34,
      boolean var35,
      int var36,
      float var37,
      float var38,
      int var39,
      float var40,
      float var41,
      int var42,
      boolean var43,
      boolean var44,
      boolean var45,
      FoodStats var46
   ) {
      this.player = var1;
      this.boundingBox = var2;
      this.movementInput = var3;
      this.jumpTicks = var4;
      this.motionZ = var5;
      this.motionY = var7;
      this.motionX = var9;
      this.inWater = var11;
      this.onGround = var12;
      this.isAirBorne = var13;
      this.rotationYaw = var14;
      this.rotationPitch = var15;
      this.posX = var16;
      this.posY = var18;
      this.posZ = var20;
      this.capabilities = var22;
      this.ridingEntity = var23;
      this.jumpMovementFactor = var24;
      this.world = var25;
      this.isCollidedHorizontally = var26;
      this.isCollidedVertically = var27;
      this.worldBorder = var28;
      this.chunkProvider = var29;
      this.isCollidedVerticallyPrev = false;
      this.riddenByEntity = var30;
      this.attributeMap = var31;
      this.isSpectator = var32;
      this.fallDistance = var33;
      this.stepHeight = var34;
      this.isCollided = var35;
      this.fire = var36;
      this.distanceWalkedModified = var37;
      this.distanceWalkedOnStepModified = var38;
      this.nextStepDistance = var39;
      this.height = var40;
      this.width = var41;
      this.fireResistance = var42;
      this.isInWeb = var43;
      this.isDead = var44;
      this.noClip = var45;
      this.isSprinting = var45;
      this.foodStats = var46;
   }
}
