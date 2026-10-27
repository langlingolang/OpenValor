package valor.module.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.opengl.GL11;
import valor.Valor;
import valor.core.MovementManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.MoveInputEvent;
import valor.events.Render2DEvent;
import valor.events.SwapItemEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.MoveUtil;
import valor.util.font.Fonts;

public class AutoBlockIn extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final Map<String, Integer> blockPriorityMap = new HashMap<>();
   private long lastPlaceTime = 0L;
   public final FloatProperty rangeProp = new FloatProperty("range", 4.5F, 3.0F, 6.0F);
   public final IntProperty speedProp = new IntProperty("speed", 20, 5, 100);
   public final IntProperty placeDelayProp = new IntProperty("place-delay", 50, 0, 200);
   public final IntProperty rotationToleranceProp = new IntProperty("rotation-tolerance", 25, 5, 100);
   public final BooleanProperty itemSpoofProp = new BooleanProperty("item-spoof", Boolean.TRUE);
   public final BooleanProperty showProgressProp = new BooleanProperty("show-progress", Boolean.TRUE);
   public final ModeProperty moveFixProp = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
   private float playerYaw;
   private float playerPitch;
   private float blockInProgress;
   private float targetYaw;
   private float targetPitch;
   private BlockPos targetBlockPos;
   private EnumFacing targetFacing;
   private Vec3 targetHitVec;
   public int blockSlot = -1;
   private static final int[][] SURROUND_OFFSETS = new int[][]{{1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 0, -1}};
   private static final double EDGE_OFFSET = 0.05;
   private static final double STEP_SIZE = 0.2;
   private static final double JITTER_RANGE = 0.020000000000000004;

   @EventTarget(1)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled()) {
         if (var1.getType() == EventType.PRE) {
            if (mc.thePlayer != null && mc.theWorld != null) {
               if (mc.currentScreen == null) {
                  this.playerYaw = var1.getYaw();
                  this.playerPitch = var1.getPitch();
                  Vec3 var3 = mc.thePlayer.getPositionVector();
                  BlockPos var4 = new BlockPos(var3.xCoord, var3.yCoord, var3.zCoord);
                  int var5 = 0;
                  if (!isAirOrReplaceable(var4.up(2))) {
                     var5++;
                  }

                  for (int[] var24 : SURROUND_OFFSETS) {
                     if (!isAirOrReplaceable(var4.add(var24[0], 0, var24[2]))) {
                        var5++;
                     }

                     if (!isAirOrReplaceable(var4.add(var24[0], 1, var24[2]))) {
                        var5++;
                     }
                  }

                  this.blockInProgress = (float)var5 / 9.0F;
                  AutoBlockIn var2 = this;
                  int var83 = -1;
                  int var91 = Integer.MAX_VALUE;

                  for (int var95 = 0; var95 <= 8; var95++) {
                     ItemStack var20;
                     if ((var20 = mc.thePlayer.inventory.getStackInSlot(var95)) != null
                        && var20.stackSize != 0
                        && var20.getItem() instanceof ItemBlock) {
                        String var126 = ((ItemBlock)var20.getItem()).getBlock().getUnlocalizedName().replace("tile.", "");
                        Integer var128;
                        if ((var128 = var2.blockPriorityMap.get(var126)) != null && var128 < var91) {
                           var91 = var128;
                           var83 = var95;
                           if (var128 == 0) {
                              break;
                           }
                        }
                     }
                  }

                  if (var83 != -1 && mc.thePlayer.inventory.currentItem != var83) {
                     mc.thePlayer.inventory.currentItem = var83;
                  }

                  ItemStack var77;
                  if ((var77 = mc.thePlayer.inventory.getCurrentItem()) == null || !(var77.getItem() instanceof ItemBlock)) {
                     this.targetBlockPos = null;
                     this.targetFacing = null;
                     this.targetHitVec = null;
                  } else {
                      Vec3 posVec = mc.thePlayer.getPositionVector();
                      BlockPos playerPos = new BlockPos(posVec.xCoord, posVec.yCoord, posVec.zCoord);
                      Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0F);
                      double reach = ((Float)this.rangeProp.getValue()).doubleValue();
                      this.searchBlockInTarget(posVec, playerPos, eyePos, reach);

                   if (this.targetBlockPos != null && this.targetFacing != null && this.targetHitVec != null) {
                        Vec3 var79 = mc.thePlayer.getPositionEyes(1.0F);
                        double var102 = this.targetHitVec.xCoord - var79.xCoord;
                        double var109 = this.targetHitVec.yCoord - var79.yCoord;
                        double var114 = this.targetHitVec.zCoord - var79.zCoord;
                        double var115 = Math.sqrt(var102 * var102 + var114 * var114);
                        float var80 = (float)Math.toDegrees(Math.atan2(var114, var102)) - 90.0F;
                        float var88 = (float)(-Math.toDegrees(Math.atan2(var109, var115)));
                        float var81 = MathHelper.wrapAngleTo180_float(MathHelper.wrapAngleTo180_float(var80) - this.playerYaw);
                        float var89 = var88 - this.playerPitch;
                        float var94 = ((Integer)this.speedProp.getValue()).floatValue();
                        float var82 = MathHelper.clamp_float(var81, -var94, var94);
                        float var90 = MathHelper.clamp_float(var89, -var94, var94);
                        this.targetYaw = this.playerYaw + var82;
                        this.targetPitch = MathHelper.clamp_float(this.playerPitch + var90, -90.0F, 90.0F);
                        var1.setRotation(this.targetYaw, this.targetPitch, 6);
                        var1.setPervRotation(this.moveFixProp.getValue() != 0 ? this.targetYaw : mc.thePlayer.rotationYaw, 6);
                     }
                  }
               }
            }
         }
      }
   }

   public static MovingObjectPosition rayTrace(float yaw, float pitch, double reach) {
      yaw = (float)Math.toRadians((double)yaw);
      pitch = (float)Math.toRadians((double)pitch);
      double var4 = -Math.sin((double)yaw) * Math.cos((double)pitch);
      double var6 = -Math.sin((double)pitch);
      double var8 = Math.cos((double)yaw) * Math.cos((double)pitch);
      Vec3 var11;
      Vec3 var13 = (var11 = mc.thePlayer.getPositionEyes(1.0F)).addVector(var4 * reach, var6 * reach, var8 * reach);
      return mc.theWorld.rayTraceBlocks(var11, var13);
   }

   public static float wrapAngleTo180(float angle) {
      float var1;
      return (var1 = (angle % 360.0F + 360.0F) % 360.0F) > 180.0F ? var1 - 360.0F : var1;
   }

   private static boolean isAirOrReplaceable(BlockPos var0) {
      Block var1;
      return (var1 = mc.theWorld.getBlockState(var0).getBlock()) == Blocks.air
         || var1 == Blocks.water
         || var1 == Blocks.flowing_water
         || var1 == Blocks.lava
         || var1 == Blocks.flowing_lava
         || var1 == Blocks.fire;
   }

   public static Vec3 getHitVec(BlockPos blockPos, EnumFacing facing, double offsetX, double offsetY) {
      double var6 = (double)blockPos.getX() + 0.5;
      double var8 = (double)blockPos.getY() + 0.5;
      double var10 = (double)blockPos.getZ() + 0.5;
      switch (facing) {
         case DOWN:
            var8 = (double)blockPos.getY() + 0.05;
            var6 = (double)blockPos.getX() + offsetX;
            var10 = (double)blockPos.getZ() + offsetY;
            break;
         case UP:
            var8 = (double)blockPos.getY() + 1.0 - 0.05;
            var6 = (double)blockPos.getX() + offsetX;
            var10 = (double)blockPos.getZ() + offsetY;
            break;
         case NORTH:
            var10 = (double)blockPos.getZ() + 0.05;
            var6 = (double)blockPos.getX() + offsetX;
            var8 = (double)blockPos.getY() + offsetY;
            break;
         case SOUTH:
            var10 = (double)blockPos.getZ() + 1.0 - 0.05;
            var6 = (double)blockPos.getX() + offsetX;
            var8 = (double)blockPos.getY() + offsetY;
            break;
         case WEST:
            var6 = (double)blockPos.getX() + 0.05;
            var10 = (double)blockPos.getZ() + offsetX;
            var8 = (double)blockPos.getY() + offsetY;
            break;
         case EAST:
            var6 = (double)blockPos.getX() + 1.0 - 0.05;
            var10 = (double)blockPos.getZ() + offsetX;
            var8 = (double)blockPos.getY() + offsetY;
      }

      return new Vec3(var6, var8, var10);
   }

   public static double clamp(double val, double min, double max) {
      if (val < min) {
         return min;
      } else {
         return val > max ? max : val;
      }
   }

   public static float[] calculateRotations(Vec3 eyes, double targetX, double targetY, double targetZ) {
      double var7 = targetX - eyes.xCoord;
      double var9 = targetY - eyes.yCoord;
      double var11 = targetZ - eyes.zCoord;
      double var13 = Math.sqrt(var7 * var7 + var11 * var11);
      float var15 = wrapAngleTo180((float)Math.toDegrees(Math.atan2(var11, var7)) - 90.0F);
      float var16 = (float)Math.toDegrees(-Math.atan2(var9, var13));
      return new float[]{var15, var16};
   }

   public boolean findFacingAndHitVec(BlockPos candidatePos, Vec3 eyes, double reach, BlockPos targetPos) {
      EnumFacing[] var6;
      for (EnumFacing var9 : var6 = EnumFacing.values()) {
         if (candidatePos.offset(var9).equals(targetPos)) {
            int var10 = (int)Math.round(5.0);

            for (int var11 = 0; var11 <= var10; var11++) {
               double var13;
               if ((var13 = (double)var11 * 0.2 + (Math.random() * 0.020000000000000004 * 2.0 - 0.020000000000000004)) < 0.0) {
                  var13 = 0.0;
               } else if (var13 > 1.0) {
                  var13 = 1.0;
               }

               for (int var12 = 0; var12 <= var10; var12++) {
                  double var16;
                  if ((var16 = (double)var12 * 0.2 + (Math.random() * 0.020000000000000004 * 2.0 - 0.020000000000000004)) < 0.0) {
                     var16 = 0.0;
                  } else if (var16 > 1.0) {
                     var16 = 1.0;
                  }

                  Vec3 var15 = getHitVec(candidatePos, var9, var16, var13);
                  float[] var18;
                  MovingObjectPosition var19;
                  if ((var19 = rayTrace((var18 = calculateRotations(eyes, var15.xCoord, var15.yCoord, var15.zCoord))[0], var18[1], reach)) != null
                     && var19.typeOfHit == MovingObjectType.BLOCK
                     && var19.getBlockPos().equals(candidatePos)
                     && var19.sideHit == var9) {
                     this.targetBlockPos = candidatePos;
                     this.targetFacing = var9;
                     this.targetHitVec = var19.hitVec;
                     this.targetYaw = var18[0];
                     this.targetPitch = var18[1];
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   @Override
   public void onDisabled() {
      if (this.blockSlot != -1 && mc.thePlayer != null && mc.thePlayer.inventory.currentItem != this.blockSlot) {
         mc.thePlayer.inventory.currentItem = this.blockSlot;
      }

      this.blockInProgress = 0.0F;
      this.targetBlockPos = null;
      this.targetFacing = null;
      this.targetHitVec = null;
   }

   @Override
   public void onEnabled() {
      if (mc.thePlayer != null) {
         this.playerYaw = mc.thePlayer.rotationYaw;
         this.playerPitch = mc.thePlayer.rotationPitch;
         this.targetYaw = this.playerYaw;
         this.targetPitch = this.playerPitch;
         this.blockInProgress = 0.0F;
         this.blockSlot = mc.thePlayer.inventory.currentItem;

         this.targetBlockPos = null;
         this.targetFacing = null;
         this.targetHitVec = null;
         this.lastPlaceTime = 0L;
      }
   }

   @EventTarget(1)
   public void onTick(TickEvent var1) {
      if (this.isEnabled()) {
         if (var1.getType() == EventType.PRE) {
            if (mc.thePlayer != null && mc.theWorld != null) {
               if (mc.currentScreen == null) {
                  if (this.targetBlockPos != null && this.targetFacing != null && this.targetHitVec != null) {
                     float var3 = this.targetPitch;
                     float var2 = this.targetYaw;
                     var2 = Math.abs(MathHelper.wrapAngleTo180_float(var2 - this.playerYaw));
                     var3 = Math.abs(MathHelper.wrapAngleTo180_float(var3 - this.playerPitch));
                     if (!(var2 <= (float)((Integer)this.rotationToleranceProp.getValue()).intValue()) || !(var3 <= (float)((Integer)this.rotationToleranceProp.getValue()).intValue())) {
                        return;
                     }

                     if (Valor.stateTracker.hasDug || Valor.stateTracker.hasPlacedBlock || Valor.stateTracker.hasUsedEntity) {
                        return;
                     }

                     long var6;
                     if ((var6 = System.currentTimeMillis()) - this.lastPlaceTime >= (long)((Integer)this.placeDelayProp.getValue()).intValue()) {
                        this.lastPlaceTime = var6;
                        MovingObjectPosition var4;
                        ItemStack var7;
                        if ((
                                 var4 = rayTrace(
                                    mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, (double)((Float)this.rangeProp.getValue()).floatValue()
                                 )
                              )
                              != null
                           && var4.typeOfHit == MovingObjectType.BLOCK
                           && var4.getBlockPos().equals(this.targetBlockPos)
                           && var4.sideHit == this.targetFacing
                           && (var7 = mc.thePlayer.inventory.getCurrentItem()) != null
                           && var7.getItem() instanceof ItemBlock) {
                           mc.playerController
                              .onPlayerRightClick(mc.thePlayer, mc.theWorld, var7, this.targetBlockPos, this.targetFacing, var4.hitVec);
                           mc.thePlayer.swingItem();
                           this.targetBlockPos = null;
                           this.targetFacing = null;
                           this.targetHitVec = null;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onMove(MoveInputEvent var1) {
      if (this.isEnabled() && (Integer)this.moveFixProp.getValue() == 1 && MovementManager.isRotated() && MovementManager.getPriority() == 6.0F && MoveUtil.isMoving()) {
         MoveUtil.fixMovement(MovementManager.getMovementYaw());
      }
   }

   @EventTarget
   public void onSwap(SwapItemEvent var1) {
      if (this.isEnabled()) {
         this.blockSlot = var1.setSlot(this.blockSlot);

         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent var1) {
      if (this.isEnabled() && mc.currentScreen == null) {
         if ((Boolean)this.showProgressProp.getValue()) {
            if (Fonts.isReady()) {
               String var5 = String.format("Blocking: %.0f%%", this.blockInProgress * 100.0F);
               GL11.glPushMatrix();
               GL11.glScaled(1.0, 1.0, 0.0);
               GlStateManager.disableDepth();
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
               ScaledResolution var2 = new ScaledResolution(mc);
               int var3 = (int)Fonts.get().comfortaaSmall.getWidth(var5);
               Color var4 = this.blockInProgress <= 0.33F ? new Color(255, 85, 85) : (this.blockInProgress <= 0.66F ? new Color(255, 255, 85) : new Color(85, 255, 85));
               Fonts.get()
                  .comfortaaSmall
                  .drawString(
                     var5,
                     (float)var2.getScaledWidth() / 2.0F - (float)var3 / 2.0F,
                     (float)var2.getScaledHeight() / 5.0F * 2.0F,
                     var4.getRGB() & 16777215 | -1090519040
                  );
               GlStateManager.disableBlend();
               GlStateManager.enableDepth();
               GL11.glPopMatrix();
            }
         }
      }
   }

   public AutoBlockIn() {
      super("AutoBlockIn", false, ModuleCategory.WORLD);
      this.blockPriorityMap.put("obsidian", 0);
      this.blockPriorityMap.put("end_stone", 1);
      this.blockPriorityMap.put("planks", 2);
      this.blockPriorityMap.put("log", 2);
      this.blockPriorityMap.put("glass", 3);
      this.blockPriorityMap.put("stained_glass", 3);
      this.blockPriorityMap.put("hardened_clay", 4);
      this.blockPriorityMap.put("stained_hardened_clay", 4);
      this.blockPriorityMap.put("cloth", 5);
   }

   public static class CandidateBlock {
      public BlockPos pos;
      public double distanceSq;

      public CandidateBlock(BlockPos pos, double distanceSq) {
         this.pos = pos;
         this.distanceSq = distanceSq;

      }
   }
   private void searchBlockInTarget(Vec3 playerPosVec, BlockPos playerPos, Vec3 eyePos, double maxReach) {
      double reachSq = maxReach * maxReach;
      double reachPlus1Sq = (maxReach + 1.0) * (maxReach + 1.0);
      BlockPos up2 = playerPos.up(2);
      AutoBlockIn var2 = this;
      Vec3 var96 = eyePos;
      double var125 = maxReach;
      BlockPos var4 = playerPos;
      BlockPos var85 = up2;

      if (isAirOrReplaceable(up2)) {
         ArrayList<AutoBlockIn.CandidateBlock> var6 = new ArrayList<>();
         int var7 = (int)Math.floor(eyePos.xCoord - maxReach);
         int var8 = (int)Math.floor(eyePos.xCoord + maxReach);
         int var9 = (int)Math.floor(eyePos.yCoord - 1.0);
         int var10 = (int)Math.floor(eyePos.yCoord + maxReach);
         int var11 = (int)Math.floor(eyePos.zCoord - maxReach);

         for (int var12 = (int)Math.floor(eyePos.zCoord + maxReach); var7 <= var8; var7++) {
            for (int var13 = var9; var13 <= var10; var13++) {
               for (int var14 = var11; var14 <= var12; var14++) {
                  BlockPos var15;
                  if (!isAirOrReplaceable(var15 = new BlockPos(var7, var13, var14))) {
                     double var26 = (double)var7 + 0.5 - eyePos.xCoord;
                     double var28 = (double)var13 + 0.5 - eyePos.yCoord;
                     double var30 = (double)var14 + 0.5 - eyePos.zCoord;
                     if (!(var26 * var26 + var28 * var28 + var30 * var30 > reachPlus1Sq)) {
                        double var34 = (double)var7;
                        double var36 = (double)(var7 + 1);
                        double var38 = (double)var13;
                        double var40 = (double)(var13 + 1);
                        double var42 = (double)var14;
                        double var44 = (double)(var14 + 1);
                        double var16 = eyePos.xCoord < var34 ? var34 - eyePos.xCoord : (eyePos.xCoord > var36 ? eyePos.xCoord - var36 : 0.0);
                        double var20 = eyePos.yCoord < var38 ? var38 - eyePos.yCoord : (eyePos.yCoord > var40 ? eyePos.yCoord - var40 : 0.0);
                        double var24 = eyePos.zCoord < var42 ? var42 - eyePos.zCoord : (eyePos.zCoord > var44 ? eyePos.zCoord - var44 : 0.0);
                        if (!(var16 * var16 + var20 * var20 + var24 * var24 > reachSq)) {
                           for (EnumFacing var23 : EnumFacing.values()) {
                              if (var15.offset(var23).equals(up2)) {
                                 var6.add(new AutoBlockIn.CandidateBlock(var15, var16 * var16 + var20 * var20 + var24 * var24));
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!var6.isEmpty()) {
            Collections.sort(var6, (c1, c2) -> Double.compare(c1.distanceSq, c2.distanceSq));
            for (AutoBlockIn.CandidateBlock candidate : var6) {
               if (this.findFacingAndHitVec(candidate.pos, eyePos, maxReach, up2)) {
                  return;
               }
            }
         }

         ArrayList<AutoBlockIn.CandidateBlock> var117 = new ArrayList<>();
         for (int[] var88 : SURROUND_OFFSETS) {
            BlockPos var89;
            if (isAirOrReplaceable(var89 = playerPos.add(var88[0], 2, var88[2]))) {
               for (EnumFacing var91 : EnumFacing.values()) {
                  BlockPos var92;
                  if (!isAirOrReplaceable(var92 = var89.offset(var91)) && !var92.equals(up2)) {
                     var117.add(new AutoBlockIn.CandidateBlock(var92, eyePos.squareDistanceTo(new Vec3((double)var92.getX() + 0.5, (double)var92.getY() + 0.5, (double)var92.getZ() + 0.5))));
                  }
               }
            }
         }

         if (!var117.isEmpty()) {
            Collections.sort(var117, (c1, c2) -> Double.compare(c1.distanceSq, c2.distanceSq));
            for (AutoBlockIn.CandidateBlock candidate : var117) {
               for (EnumFacing var120 : EnumFacing.values()) {
                  BlockPos var121 = candidate.pos.offset(var120);
                  for (int[] var122 : SURROUND_OFFSETS) {
                     if (var121.equals(playerPos.add(var122[0], 2, var122[2])) && this.findFacingAndHitVec(candidate.pos, eyePos, maxReach, var121)) {
                        return;
                     }
                  }
               }
            }
         }

         for (int[] var90 : SURROUND_OFFSETS) {
            BlockPos var138 = playerPos.add(var90[0], 2, var90[2]);
            if (isAirOrReplaceable(var138)) {
               for (int var18 = -1; var18 <= 1; var18++) {
                  for (int var19 = -1; var19 <= 1; var19++) {
                     for (int var148 = -1; var148 <= 1; var148++) {
                        BlockPos var33;
                        if (!isAirOrReplaceable(var33 = var138.add(var18, var19, var148))) {
                           if (var18 + var19 + var148 == 1 && this.findFacingAndHitVec(var33, eyePos, maxReach, var138)) {
                              return;
                           }
                        }
                     }
                  }
               }

               for (EnumFacing var123 : EnumFacing.values()) {
                  BlockPos var124;
                  if (!isAirOrReplaceable(var124 = var138.offset(var123)) && this.findFacingAndHitVec(var124, eyePos, maxReach, var138)) {
                     return;
                  }
               }
            }
         }
      }

      ArrayList<BlockPos> var97 = new ArrayList<>();
      for (int[] var110 : SURROUND_OFFSETS) {
         BlockPos var111;
         if (isAirOrReplaceable(var111 = playerPos.add(var110[0], 1, var110[2]))) {
            var97.add(var111);
         }
      }

      for (int[] var112 : SURROUND_OFFSETS) {
         BlockPos var113;
         if (isAirOrReplaceable(var113 = playerPos.add(var112[0], 0, var112[2]))) {
            var97.add(var113);
         }
      }

      for (BlockPos var98 : var97) {
         for (EnumFacing var66 : EnumFacing.values()) {
            BlockPos var67;
            if (!isAirOrReplaceable(var67 = var98.offset(var66))) {
               Vec3 var68 = new Vec3((double)var67.getX() + 0.5, (double)var67.getY() + 0.5, (double)var67.getZ() + 0.5);
               if (!(eyePos.distanceTo(var68) > maxReach)) {
                  int var149 = 5;
                  for (int var69 = 0; var69 <= var149; var69++) {
                     double var72 = (double)var69 * 0.2 + (Math.random() * 0.04 - 0.02);
                     var72 = MathHelper.clamp_double(var72, 0.0, 1.0);
                     for (int var70 = 0; var70 <= var149; var70++) {
                        double var75 = (double)var70 * 0.2 + (Math.random() * 0.04 - 0.02);
                        var75 = MathHelper.clamp_double(var75, 0.0, 1.0);
                        Vec3 var71 = getHitVec(var67, var66.getOpposite(), var75, var72);
                        float[] var150 = calculateRotations(eyePos, var71.xCoord, var71.yCoord, var71.zCoord);
                        MovingObjectPosition var74 = rayTrace(var150[0], var150[1], maxReach);
                        if (var74 != null && var74.typeOfHit == MovingObjectType.BLOCK
                           && var74.getBlockPos().equals(var67) && var74.sideHit == var66.getOpposite()) {
                           this.targetBlockPos = var67;
                           this.targetFacing = var66.getOpposite();
                           this.targetHitVec = var74.hitVec;
                           this.targetYaw = var150[0];
                           this.targetPitch = var150[1];
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

}
