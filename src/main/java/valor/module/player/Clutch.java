package valor.module.player;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.SwapItemEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.util.BlockUtil;
import valor.util.ReflectionUtil;
import valor.util.RotationUtil;
public class Clutch extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final int MAX_CLUTCH_SLOTS = 7;
   private static final EnumFacing[] HORIZONTAL_FACINGS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
   public final BooleanProperty waterBucketProp = new BooleanProperty("water-bucket", Boolean.TRUE);
   public final BooleanProperty ladderProp = new BooleanProperty("ladder", Boolean.TRUE);
   public final FloatProperty minFallDistanceProp = new FloatProperty("min-fall-distance", 3.0F, 1.0F, 10.0F);
   public final IntProperty predictionTicksProp = new IntProperty("prediction-ticks", 2, 0, 4);
   public final BooleanProperty restoreSlotProp = new BooleanProperty("restore-slot", Boolean.TRUE);
   private ClutchPlacement currentPlacement;
   private int previousSlot = -1;
   private long lastPlaceTimeMs;
   private BlockPos placedWaterPos;
   private BlockPos targetPlacementPos;
   private EnumFacing targetFacing;
   private int tickCounter;
   @EventTarget(1)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled()) {
         if (mc.thePlayer != null && mc.theWorld != null && mc.currentScreen == null) {
            if (var1.getType() == EventType.POST) {
               float var44 = var1.getPitch();
               float var39 = var1.getYaw();
               if (this.currentPlacement != null && mc.currentScreen == null && !(mc.thePlayer.motionY >= -0.08)) {
                  if (System.currentTimeMillis() - this.lastPlaceTimeMs >= 150L) {
                     MovingObjectPosition var48;
                     if ((var48 = RotationUtil.rayTrace(var39, var44, (double)mc.playerController.getBlockReachDistance())) == null
                        || var48.typeOfHit != MovingObjectType.BLOCK
                        || !var48.getBlockPos().equals(this.currentPlacement.targetBlockPos)
                        || var48.sideHit != this.currentPlacement.facing) {
                        return;
                     }
                     ItemStack var52 = mc.thePlayer.inventory.getStackInSlot(this.currentPlacement.slot);
                     if (!isValidClutchItem(this.currentPlacement.type, var52)) {
                        this.currentPlacement = null;
                        return;
                     }
                     this.previousSlot = mc.thePlayer.inventory.currentItem;
                     mc.thePlayer.inventory.currentItem = this.currentPlacement.slot;
                     ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
                     boolean var40;
                     if (this.currentPlacement.type == ClutchType.WATER_BUCKET) {
                        float var49 = mc.thePlayer.rotationYaw;
                        float var58 = mc.thePlayer.rotationPitch;
                        try {
                           mc.thePlayer.rotationYaw = var39;
                           mc.thePlayer.rotationPitch = var44;
                           var40 = mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, var52);
                        } finally {
                           mc.thePlayer.rotationYaw = var49;
                           mc.thePlayer.rotationPitch = var58;
                        }
                     } else {
                        var40 = mc.playerController
                           .onPlayerRightClick(mc.thePlayer, mc.theWorld, var52, this.currentPlacement.targetBlockPos, this.currentPlacement.facing, var48.hitVec);
                     }
                     if (var40) {
                        mc.thePlayer.swingItem();
                        this.lastPlaceTimeMs = this.currentPlacement.type == ClutchType.BLOCK ? 0L : System.currentTimeMillis();
                        if (this.currentPlacement.type == ClutchType.BLOCK && this.currentPlacement.waterBlockPos != null) {
                           this.placedWaterPos = this.currentPlacement.waterBlockPos;
                           this.targetPlacementPos = this.currentPlacement.targetBlockPos.offset(this.currentPlacement.facing);
                           this.targetFacing = this.currentPlacement.ladderFacing;
                           this.tickCounter = 6;
                        } else if (this.currentPlacement.type == ClutchType.LADDER) {
                           this.resetWaterClutch();
                        }
                     }
                     if (!(Boolean)this.restoreSlotProp.getValue()) {
                        this.previousSlot = -1;
                     }
                     if (this.previousSlot != -1 && (Boolean)this.restoreSlotProp.getValue()) {
                        this.restoreSlot();
                     }
                     this.currentPlacement = null;
                  }
               }
            } else if (var1.getType() == EventType.PRE) {
               if (!mc.thePlayer.onGround && !(mc.thePlayer.motionY >= -0.08) && !(mc.thePlayer.fallDistance < (Float)this.minFallDistanceProp.getValue())) {
                  int var10001;
                  if ((Boolean)this.waterBucketProp.getValue()) {
                     int var7 = 0;
                     while (true) {
                        if (var7 >= 9) {
                           var10001 = -1;
                           break;
                        }
                        ItemStack var11;
                        if ((var11 = mc.thePlayer.inventory.getStackInSlot(var7)) != null && var11.getItem() == Items.water_bucket) {
                           var10001 = var7;
                           break;
                        }
                        var7++;
                     }
                  } else {
                     var10001 = -1;
                  }
                   ClutchPlacement var92 = this.findClutchPlacement(var10001);
                  this.currentPlacement = var92;
                  if (this.currentPlacement != null) {
                     float[] var35 = RotationUtil.applyGCD(var1.getYaw(), var1.getPitch(), this.currentPlacement.yaw, this.currentPlacement.pitch);
                     this.currentPlacement.yaw = var35[0];
                     this.currentPlacement.pitch = MathHelper.clamp_float(var35[1], -90.0F, 90.0F);
                     var1.setRotation(this.currentPlacement.yaw, this.currentPlacement.pitch, 7);
                     var1.setPervRotation(this.currentPlacement.yaw, 7);
                  }
               } else {
                  this.currentPlacement = null;
                  if (mc.thePlayer.onGround) {
                     this.resetWaterClutch();
                  }
               }
            }
         }
      }
   }
   public void resetWaterClutch() {
      this.placedWaterPos = null;
      this.targetPlacementPos = null;
      this.targetFacing = null;
      this.tickCounter = 0;
   }
   public void restoreSlot() {
      if (this.previousSlot != -1 && mc.thePlayer != null) {
         mc.thePlayer.inventory.currentItem = this.previousSlot;
         if (mc.playerController != null) {
            ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
         }
         this.previousSlot = -1;
      } else {
         this.previousSlot = -1;
      }
   }
   public static boolean isSolidBlock(BlockPos var0) {
      Block var1;
      return (var1 = mc.theWorld.getBlockState(var0).getBlock()).getMaterial().isSolid()
         && var1.getCollisionBoundingBox(mc.theWorld, var0, mc.theWorld.getBlockState(var0)) != null;
   }
   public static boolean isValidClutchItem(ClutchType var0, ItemStack var1) {
      if (var1 == null) {
         return false;
      } else if (var0 == ClutchType.WATER_BUCKET) {
         return var1.getItem() == Items.water_bucket;
      } else if (!(var1.getItem() instanceof ItemBlock)) {
         return false;
      } else {
         Block var2 = ((ItemBlock)var1.getItem()).getBlock();
         return var0 == ClutchType.LADDER ? var2 == Blocks.ladder : var2 != Blocks.ladder && !BlockUtil.isInteractableBlock(var2) && BlockUtil.isFullSolidBlock(var2);
      }
   }
   public static ClutchPlacement createPlacement(ClutchType var0, int var1, BlockPos var2, EnumFacing var3, Vec3 var4) {
      Vec3 var5 = mc.thePlayer.getPositionEyes(1.0F);
      double var6 = var4.xCoord - var5.xCoord;
      double var8 = var4.yCoord - var5.yCoord;
      double var10 = var4.zCoord - var5.zCoord;
      double var12 = Math.sqrt(var6 * var6 + var10 * var10);
      float var14 = MathHelper.wrapAngleTo180_float((float)(Math.toDegrees(Math.atan2(var10, var6)) - 90.0));
      float var15 = (float)(-Math.toDegrees(Math.atan2(var8, var12)));
      return new ClutchPlacement(var0, var1, var2, var3, var4, var14, var15);
   }
   public static Vec3 getFacingCenterVec(BlockPos var0, EnumFacing var1) {
      return new Vec3(
         (double)var0.getX() + 0.5 + (double)var1.getFrontOffsetX() * 0.5,
         (double)var0.getY() + 0.5 + (double)var1.getFrontOffsetY() * 0.5,
         (double)var0.getZ() + 0.5 + (double)var1.getFrontOffsetZ() * 0.5
      );
   }
   public double predictFallY() {
      double var1 = mc.thePlayer.posY;
      double var3 = mc.thePlayer.motionY;
      for (int var5 = 0; var5 < this.predictionTicksProp.getValue(); var5++) {
         var1 += var3;
         var3 = (var3 - 0.08) * 0.98;
      }
      return var1;
   }
   public static boolean canReachPlacement(ClutchPlacement var0) {
      MovingObjectPosition var1;
      return (var1 = RotationUtil.rayTrace(var0.yaw, var0.pitch, (double)mc.playerController.getBlockReachDistance())) != null
         && var1.typeOfHit == MovingObjectType.BLOCK
         && var1.getBlockPos().equals(var0.targetBlockPos)
         && var1.sideHit == var0.facing;
   }
   @Override
   public void onDisabled() {
      this.restoreSlot();
      this.currentPlacement = null;
      this.resetWaterClutch();
   }
   @Override
   public void onEnabled() {
      this.currentPlacement = null;
      this.previousSlot = -1;
      this.lastPlaceTimeMs = 0L;
      this.resetWaterClutch();
   }
   @EventTarget
   public void onSwap(SwapItemEvent var1) {
      if (this.isEnabled() && this.previousSlot != -1) {
         var1.setCancelled(true);
      }
   }
   public Clutch() {
      super("Clutch", false, ModuleCategory.PLAYER);
   }
   public static enum ClutchType {
      WATER_BUCKET,
      LADDER,
      BLOCK;
   }
   public static class ClutchPlacement {
      public ClutchType type;
      public int slot;
      public BlockPos targetBlockPos;
      public EnumFacing facing;
      public Vec3 hitVec;
      public float yaw;
      public float pitch;
      public BlockPos waterBlockPos;
      public EnumFacing ladderFacing;
      public ClutchPlacement(ClutchType type, int slot, BlockPos targetBlockPos, EnumFacing facing, Vec3 hitVec, float yaw, float pitch) {
         this.type = type;
         this.slot = slot;
         this.targetBlockPos = targetBlockPos;
         this.facing = facing;
         this.hitVec = hitVec;
         this.yaw = yaw;
         this.pitch = pitch;
      }
   }
   private ClutchPlacement findClutchPlacement(int waterSlot) {
      int var10001 = waterSlot;
                     int var3 = var10001;
                     if (var10001 != -1) {
                        int var53 = var3;
                        double var65 = mc.thePlayer.posX + mc.thePlayer.motionX * (double)((Integer)this.predictionTicksProp.getValue()).intValue();
                        double var13 = mc.thePlayer.posZ + mc.thePlayer.motionZ * (double)((Integer)this.predictionTicksProp.getValue()).intValue();
                        double var15 = this.predictFallY();
                        var3 = MathHelper.floor_double(var65);
                        int var4 = MathHelper.floor_double(var13);
                        int var17 = MathHelper.floor_double(var15) - 1;
                        ClutchPlacement var18 = null;
                        double var19 = Double.MAX_VALUE;
                        for (int var5 = -1; var5 <= 1; var5++) {
                           for (int var6 = -1; var6 <= 1; var6++) {
                              for (int var8 = var17; var8 >= var17 - 5; var8--) {
                                 BlockPos var9;
                                 if (isSolidBlock(var9 = new BlockPos(var3 + var5, var8, var4 + var6)) && BlockUtil.isReplaceable(var9.up())) {
                                    Vec3 var10 = new Vec3((double)var9.getX() + 0.5, (double)var9.getY() + 1.0, (double)var9.getZ() + 0.5);
                                    double var21;
                                    if (!(
                                          (var21 = mc.thePlayer.getPositionEyes(1.0F).distanceTo(var10))
                                             > (double)mc.playerController.getBlockReachDistance()
                                       )
                                       && !(var21 >= var19)) {
                                       var19 = var21;
                                       var18 = createPlacement(ClutchType.WATER_BUCKET, var53, var9, EnumFacing.UP, var10);
                                       break;
                                    }
                                 }
                              }
                           }
                        }
                        if (var18 != null) {
                           return var18;
                        }
                     }
                     if ((Boolean)this.ladderProp.getValue()) {
                        int var54 = 0;
                        while (true) {
                           if (var54 >= 9) {
                              var10001 = -1;
                              break;
                           }
                           ItemStack var66 = mc.thePlayer.inventory.getStackInSlot(var54);
                           if (isValidClutchItem(ClutchType.LADDER, var66)) {
                              var10001 = var54;
                              break;
                           }
                           var54++;
                        }
                     } else {
                        var10001 = -1;
                     }
                     int var41 = var10001;
                     if (var10001 == -1) {
                        return null;
                     } else {
                        ClutchPlacement var93 = null;
                        if (this.placedWaterPos != null && this.targetPlacementPos != null && this.targetFacing != null) {
                           if (--this.tickCounter < 0 || !BlockUtil.isReplaceable(this.placedWaterPos)) {
                              this.resetWaterClutch();
                           } else if (isSolidBlock(this.targetPlacementPos)) {
                              Vec3 var67 = getFacingCenterVec(this.targetPlacementPos, this.targetFacing);
                              if (mc.thePlayer.getPositionEyes(1.0F).distanceTo(var67) > (double)mc.playerController.getBlockReachDistance()) {
                                 this.resetWaterClutch();
                              } else {
                                 ClutchPlacement var12;
                                 if (canReachPlacement(var12 = createPlacement(ClutchType.LADDER, var41, this.targetPlacementPos, this.targetFacing, var67))) {
                                    var93 = var12;
                                 }
                              }
                           }
                           var93 = null;
                        } else {
                           var93 = null;
                        }
                        ClutchPlacement var45 = var93;
                        if (var93 != null) {
                           return var45;
                        } else if (this.placedWaterPos != null && this.tickCounter > 0) {
                           return null;
                        } else {
                           int var55 = var41;
                           double var68 = mc.thePlayer.posX + mc.thePlayer.motionX * (double)((Integer)this.predictionTicksProp.getValue()).intValue();
                           double var76 = mc.thePlayer.posZ + mc.thePlayer.motionZ * (double)((Integer)this.predictionTicksProp.getValue()).intValue();
                           int var79 = MathHelper.floor_double(var68);
                           int var16 = MathHelper.floor_double(var76);
                           var3 = MathHelper.floor_double(this.predictFallY());
                           ClutchPlacement var42 = null;
                           double var82 = Double.MAX_VALUE;
                           for (int var85 = 0; var85 >= -2; var85--) {
                              BlockPos var20;
                              if (BlockUtil.isReplaceable(var20 = new BlockPos(var79, var3 + var85, var16))) {
                                 for (EnumFacing var61 : HORIZONTAL_FACINGS) {
                                    BlockPos var63;
                                    if (isSolidBlock(var63 = var20.offset(var61))) {
                                       EnumFacing var87 = var61.getOpposite();
                                       Vec3 var22 = getFacingCenterVec(var63, var87);
                                       double var23;
                                       ClutchPlacement var69;
                                       if (!(
                                             (var23 = mc.thePlayer.getPositionEyes(1.0F).distanceTo(var22))
                                                > (double)mc.playerController.getBlockReachDistance()
                                          )
                                          && !(var23 >= var82)
                                          && canReachPlacement(var69 = createPlacement(ClutchType.LADDER, var55, var63, var87, var22))) {
                                          var82 = var23;
                                          var42 = var69;
                                       }
                                    }
                                 }
                              }
                           }
                           if (var42 != null) {
                              return var42;
                           } else {
                              var55 = -1;
                              int var70 = -1;
                              for (int var74 = 0; var74 < 9; var74++) {
                                 Block var14;
                                 ItemStack var77;
                                 if ((var77 = mc.thePlayer.inventory.getStackInSlot(var74)) != null
                                    && var77.stackSize > 0
                                    && var77.getItem() instanceof ItemBlock
                                    && (var14 = ((ItemBlock)var77.getItem()).getBlock()) != Blocks.ladder
                                    && !BlockUtil.isInteractableBlock(var14)
                                    && BlockUtil.isFullSolidBlock(var14)
                                    && var77.stackSize > var70) {
                                    var70 = var77.stackSize;
                                    var55 = var74;
                                 }
                              }
                              if (var55 == -1) {
                                 return null;
                              } else {
                                 var68 = mc.thePlayer.posX + mc.thePlayer.motionX * (double)((Integer)this.predictionTicksProp.getValue()).intValue();
                                 var76 = mc.thePlayer.posZ + mc.thePlayer.motionZ * (double)((Integer)this.predictionTicksProp.getValue()).intValue();
                                 var79 = MathHelper.floor_double(var68);
                                 var16 = MathHelper.floor_double(var76);
                                 var3 = MathHelper.floor_double(this.predictFallY());
                                 float var2;
                                 double var27 = (double)(-MathHelper.sin(var2 = mc.thePlayer.rotationYaw * (float) (Math.PI / 180.0)));
                                 double var29 = (double)MathHelper.cos(var2);
                                 EnumFacing var43 = Math.abs(var27) > Math.abs(var29)
                                    ? (var27 >= 0.0 ? EnumFacing.EAST : EnumFacing.WEST)
                                    : (var29 >= 0.0 ? EnumFacing.SOUTH : EnumFacing.NORTH);
                                 ClutchPlacement var83 = null;
                                 double var84 = Double.MAX_VALUE;
                                 for (int var86 = 0; var86 >= -3; var86--) {
                                    BlockPos var47;
                                    if (BlockUtil.isReplaceable(var47 = new BlockPos(var79, var3 + var86, var16))) {
                                       EnumFacing[] var51 = HORIZONTAL_FACINGS;
                                       int var60 = HORIZONTAL_FACINGS.length;
                                       for (int var62 = 0; var62 < var60; var62++) {
                                          EnumFacing var64;
                                          BlockPos var88;
                                          if ((var64 = var51[var62]) == var43 && BlockUtil.isReplaceable(var88 = var47.offset(var64))) {
                                             for (EnumFacing var72 : EnumFacing.VALUES) {
                                                BlockPos var33;
                                                if (isSolidBlock(var33 = var88.offset(var72))) {
                                                   EnumFacing var73 = var72.getOpposite();
                                                   Vec3 var75 = getFacingCenterVec(var33, var73);
                                                   double var25;
                                                   ClutchPlacement var34;
                                                   if (!(
                                                         (var25 = mc.thePlayer.getPositionEyes(1.0F).distanceTo(var75))
                                                            > (double)mc.playerController.getBlockReachDistance()
                                                      )
                                                      && !(var25 >= var84)
                                                      && canReachPlacement(var34 = createPlacement(ClutchType.BLOCK, var55, var33, var73, var75))) {
                                                      var34.waterBlockPos = var47;
                                                      var34.ladderFacing = var64.getOpposite();
                                                      var84 = var25;
                                                      var83 = var34;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                                 return var83;
                              }
                           }
                        }
                     }
   }
}
