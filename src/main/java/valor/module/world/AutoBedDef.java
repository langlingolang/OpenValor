package valor.module.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0BPacketEntityAction.Action;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.opengl.GL11;
import valor.core.MovementManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.MoveInputEvent;
import valor.events.Render2DEvent;
import valor.events.SwapItemEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.MoveUtil;
import valor.util.ReflectionUtil;
import valor.util.RotationUtil;

public class AutoBedDef extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final FloatProperty rangeProp = new FloatProperty("range", 5.0F, 3.0F, 7.0F);
   public final IntProperty placeDelayProp = new IntProperty("place-delay", 80, 0, 300);
   public final IntProperty speedProp = new IntProperty("speed", 55, 30, 100);
   public final FloatProperty smoothingProp = new FloatProperty("smoothing", 0.25F, 0.1F, 0.8F);
   public final IntProperty rotToleranceProp = new IntProperty("rot-tolerance", 35, 5, 180);
   public final BooleanProperty swingProp = new BooleanProperty("swing", Boolean.TRUE);
   public final BooleanProperty showProgressProp = new BooleanProperty("show-progress", Boolean.TRUE);
   public final ModeProperty moveFixProp = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT"});
   private static final int ROTATION_PRIORITY = 6;
   private static final int SEARCH_RADIUS = 8;
   private float playerYaw;
   private float playerPitch;
   private float targetYaw;
   private float targetPitch;
   private float renderYaw;
   private float renderPitch;
   private BlockPos placementBlockPos;
   private BlockPos targetDefensePos;
   private EnumFacing placementFacing;
   private Vec3 placementHitVec;
   private long lastPlaceTime = 0L;
   private BlockPos lastPlacedPos;
   private long placeCooldownTime;
   private int originalHotbarSlot = -1;
   private float defenseProgress = 0.0F;
   private BlockPos bedHeadPos = null;
   private EnumFacing bedFacing = null;

   @EventTarget(1)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled()) {
         if (mc.thePlayer != null && mc.theWorld != null) {
            if (mc.currentScreen == null) {
               if (var1.getType() == EventType.POST) {
                  float var82 = var1.getPitch();
                  float var72 = var1.getYaw();
                  AutoBedDef var66 = this;
                  if (this.placementBlockPos != null && this.placementFacing != null && this.placementHitVec != null) {
                     if (this.targetDefensePos != null && canPlaceAt(this.targetDefensePos)) {
                        float var31 = this.targetPitch;
                        float var104 = this.targetYaw;
                        float var76 = Math.abs(MathHelper.wrapAngleTo180_float(var72 - var104));
                        float var77 = Math.abs(MathHelper.wrapAngleTo180_float(var82 - var31));
                        long var85;
                        if (var76 <= (float)((Integer)this.rotToleranceProp.getValue()).intValue()
                           && var77 <= (float)((Integer)this.rotToleranceProp.getValue()).intValue()
                           && (var85 = System.currentTimeMillis()) - this.lastPlaceTime >= (long)((Integer)this.placeDelayProp.getValue()).intValue()
                           && (!this.targetDefensePos.equals(this.lastPlacedPos) || var85 >= this.placeCooldownTime)) {
                           double var88 = (double)Math.min((Float)this.rangeProp.getValue(), mc.playerController.getBlockReachDistance());
                           MovingObjectPosition var91;
                           if ((var91 = rayTrace(var72, var82, var88)) == null
                              || var91.typeOfHit != MovingObjectType.BLOCK
                              || !var91.getBlockPos().equals(this.placementBlockPos)
                              || var91.sideHit != this.placementFacing) {
                              return;
                           }

                           ItemStack var93;
                           if ((var93 = mc.thePlayer.inventory.getCurrentItem()) == null || !(var93.getItem() instanceof ItemBlock)) {
                              return;
                           }

                           ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
                           boolean var96 = mc.thePlayer.isSneaking();
                           boolean var98 = mc.theWorld.getBlockState(this.placementBlockPos).getBlock() instanceof BlockBed;

                           boolean var75;
                           try {
                              if (var98 && !var96) {
                                 mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, Action.START_SNEAKING));
                                 mc.thePlayer.setSneaking(true);
                              }

                              var75 = mc.playerController
                                 .onPlayerRightClick(
                                    mc.thePlayer, mc.theWorld, var93, var66.placementBlockPos, var66.placementFacing, var66.placementHitVec
                                 );
                           } finally {
                              if (var98 && !var96) {
                                 mc.thePlayer.setSneaking(false);
                                 mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, Action.STOP_SNEAKING));
                              }
                           }

                           if (var75) {
                              if ((Boolean)this.swingProp.getValue()) {
                                 mc.thePlayer.swingItem();
                              } else {
                                 try {
                                    mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                 } catch (Exception var54) {
                                 }
                              }

                              this.lastPlaceTime = var85;
                              this.lastPlacedPos = this.targetDefensePos;
                              this.placeCooldownTime = var85 + 350L;
                           }
                        }
                     } else {
                        this.resetPlacementTarget();
                     }
                  }
               } else if (var1.getType() == EventType.PRE) {
                  this.playerYaw = var1.getYaw();
                  this.playerPitch = var1.getPitch();
                  AutoBedDef var10000;
                  float var10001;
                  if (this.bedHeadPos != null && this.bedFacing != null) {
                     List<BlockPos> var6 = this.getBedDefensePositions();
                     int var16 = 0;
                     Iterator<BlockPos> var17 = var6.iterator();

                     while (var17.hasNext()) {
                        if (!isReplaceable(var17.next())) {
                           var16++;
                        }
                     }

                     var10000 = this;
                     var10001 = var6.isEmpty() ? 0.0F : (float)var16 / (float)var6.size();
                  } else {
                     var10000 = this;
                     var10001 = 0.0F;
                  }

                  var10000.defenseProgress = var10001;
                  BlockPos var3 = this.bedHeadPos;
                  if (this.bedHeadPos == null ? true : !(mc.theWorld.getBlockState(var3).getBlock() instanceof BlockBed)) {
                     this.bedHeadPos = null;
                     this.bedFacing = null;
                     AutoBedDef var61 = this;
                     if (mc.thePlayer != null && mc.theWorld != null) {
                        BlockPos var68 = new BlockPos(
                           MathHelper.floor_double(mc.thePlayer.posX),
                           MathHelper.floor_double(mc.thePlayer.posY),
                           MathHelper.floor_double(mc.thePlayer.posZ)
                        );
                        double var78 = Double.MAX_VALUE;

                        for (int var18 = -8; var18 <= 8; var18++) {
                           for (int var19 = -2; var19 <= 2; var19++) {
                              for (int var20 = -8; var20 <= 8; var20++) {
                                 BlockPos var21;
                                 BlockPos var7 = var21 = var68.add(var18, var19, var20);
                                 Block var27;
                                 if ((var27 = mc.theWorld.getBlockState(var7).getBlock()) instanceof BlockBed
                                    && (var27.getMetaFromState(mc.theWorld.getBlockState(var7)) & 8) == 0) {
                                    EnumFacing var105;
                                    switch (mc.theWorld
                                          .getBlockState(var21)
                                          .getBlock()
                                          .getMetaFromState(mc.theWorld.getBlockState(var21))
                                       & 3) {
                                       case 0:
                                          var105 = EnumFacing.SOUTH;
                                          break;
                                       case 1:
                                          var105 = EnumFacing.WEST;
                                          break;
                                       case 2:
                                          var105 = EnumFacing.NORTH;
                                          break;
                                       case 3:
                                          var105 = EnumFacing.EAST;
                                          break;
                                       default:
                                          var105 = null;
                                    }

                                    EnumFacing var22 = var105;
                                    double var23;
                                    if (var105 != null
                                       && (
                                             var23 = mc.thePlayer
                                                .getDistanceSq((double)var21.getX() + 0.5, (double)var21.getY() + 0.5, (double)var21.getZ() + 0.5)
                                          )
                                          < var78) {
                                       var78 = var23;
                                       var61.bedHeadPos = var21;
                                       var61.bedFacing = var22;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (this.bedHeadPos != null) {
                     int var69 = 0;
                     int var106;

                     while (true) {
                        if (var69 >= 9) {
                           var106 = -1;
                           break;
                        }

                        ItemStack var79;
                        if ((var79 = mc.thePlayer.inventory.getStackInSlot(var69)) != null
                           && var79.stackSize != 0
                           && var79.getItem() instanceof ItemBlock
                           && ((ItemBlock)var79.getItem()).getBlock() == Blocks.wool) {
                           var106 = var69;
                           break;
                        }

                        var69++;
                     }

                     int var2 = var106;
                     if (var106 == -1) {
                        this.resetPlacementTarget();
                     } else {
                        if (mc.thePlayer.inventory.currentItem != var2) {
                           mc.thePlayer.inventory.currentItem = var2;
                        }

                        List<BlockPos> var56 = this.getBedDefensePositions();
                        if (this.lastPlacedPos != null && !isReplaceable(this.lastPlacedPos)) {
                           this.lastPlacedPos = null;
                        }

                        long var4 = System.currentTimeMillis();
                        boolean var108 = false;
                        if (this.targetDefensePos != null && var56.contains(this.targetDefensePos) && (!this.targetDefensePos.equals(this.lastPlacedPos) || var4 >= this.placeCooldownTime)) {
                           BlockPos var70 = this.targetDefensePos;
                           boolean var107 = false;
                           if (this.targetDefensePos != null && this.placementBlockPos != null && this.placementFacing != null && this.placementHitVec != null
                              && this.targetDefensePos.equals(var70) && canPlaceAt(var70) && !isReplaceable(this.placementBlockPos)
                              && this.placementBlockPos.offset(this.placementFacing).equals(var70)) {
                              Vec3 var80 = mc.thePlayer.getPositionEyes(1.0F);
                              double var83 = (double)Math.min((Float)this.rangeProp.getValue(), mc.playerController.getBlockReachDistance());
                              double var86 = this.placementHitVec.xCoord - var80.xCoord;
                              double var89 = this.placementHitVec.yCoord - var80.yCoord;
                              double var94 = this.placementHitVec.zCoord - var80.zCoord;
                              if (var86 * var86 + var89 * var89 + var94 * var94 <= var83 * var83) {
                                 float[] var73 = getRotations(var86, var89, var94);
                                 MovingObjectPosition var26 = rayTrace(var73[0], var73[1], var83);
                                 if (var26 != null && var26.typeOfHit == MovingObjectType.BLOCK
                                    && var26.getBlockPos().equals(this.placementBlockPos)
                                    && var26.sideHit == this.placementFacing) {
                                    this.targetYaw = var73[0];
                                    this.targetPitch = var73[1];
                                    var107 = true;
                                 }
                              }
                           }
                           var108 = var107;
                        }

                        if (!var108) {
                           this.resetPlacementTarget();
                           Iterator<BlockPos> var57 = var56.iterator();

                           while (var57.hasNext()) {
                              if (canPlaceAt(var3 = var57.next()) && (!var3.equals(this.lastPlacedPos) || var4 >= this.placeCooldownTime)) {
                                 BlockPos var71 = var3;
                                 AutoBedDef var63 = this;
                                 this.resetPlacementTarget();
                                 Vec3 var81 = mc.thePlayer.getPositionEyes(1.0F);
                                 double var84;
                                 double var109 = var84 = (double)Math.min((Float)this.rangeProp.getValue(), mc.playerController.getBlockReachDistance());
                                 double var87 = var109 * var109;

                                 EnumFacing[] var90;
                                 for (EnumFacing var24 : var90 = EnumFacing.values()) {
                                    BlockPos var74;
                                    if (!isReplaceable(var74 = var71.offset(var24))) {
                                       double var99 = (double)var74.getX() + 0.5 - var81.xCoord;
                                       double var28 = (double)var74.getY() + 0.5 - var81.yCoord;
                                       double var30 = (double)var74.getZ() + 0.5 - var81.zCoord;
                                       if (!(var99 * var99 + var28 * var28 + var30 * var30 > (var84 + 1.5) * (var84 + 1.5))) {
                                          EnumFacing var8 = var24.getOpposite();
                                          double[] var9 = new double[]{0.2, 0.4, 0.5, 0.6, 0.8};
                                          float var10 = Float.MAX_VALUE;
                                          float var11 = Float.NaN;
                                          float var12 = Float.NaN;
                                          Vec3 var13 = null;

                                          for (double var32 : var9) {
                                             for (double var34 : var9) {
                                                double var46 = (double)var74.getX();
                                                double var48 = (double)var74.getY();
                                                double var50 = (double)var74.getZ();
                                                Vec3 var112;
                                                switch (var8) {
                                                   case DOWN:
                                                      var112 = new Vec3(var46 + var32, var48, var50 + var34);
                                                      break;
                                                   case UP:
                                                      var112 = new Vec3(var46 + var32, var48 + 1.0, var50 + var34);
                                                      break;
                                                   case NORTH:
                                                      var112 = new Vec3(var46 + var32, var48 + var34, var50);
                                                      break;
                                                   case SOUTH:
                                                      var112 = new Vec3(var46 + var32, var48 + var34, var50 + 1.0);
                                                      break;
                                                   case WEST:
                                                      var112 = new Vec3(var46, var48 + var34, var50 + var32);
                                                      break;
                                                   case EAST:
                                                      var112 = new Vec3(var46 + 1.0, var48 + var34, var50 + var32);
                                                      break;
                                                   default:
                                                      var112 = new Vec3(var46 + 0.5, var48 + 0.5, var50 + 0.5);
                                                }

                                                Vec3 var100 = var112;
                                                double var36 = var112.xCoord - var81.xCoord;
                                                double var38 = var100.yCoord - var81.yCoord;
                                                double var40 = var100.zCoord - var81.zCoord;
                                                float[] var101;
                                                MovingObjectPosition var102;
                                                float var103;
                                                if (!(var36 * var36 + var38 * var38 + var40 * var40 > var87)
                                                   && (var102 = rayTrace((var101 = getRotations(var36, var38, var40))[0], var101[1], var84)) != null
                                                   && var102.typeOfHit == MovingObjectType.BLOCK
                                                   && var102.getBlockPos().equals(var74)
                                                   && var102.sideHit == var8
                                                   && (
                                                         var103 = Math.abs(MathHelper.wrapAngleTo180_float(var101[0] - var63.playerYaw))
                                                            + Math.abs(var101[1] - var63.playerPitch)
                                                      )
                                                      < var10) {
                                                   var10 = var103;
                                                   var11 = var101[0];
                                                   var12 = var101[1];
                                                   var13 = var102.hitVec;
                                                }
                                             }
                                          }

                                          if (var13 != null) {
                                             var63.targetYaw = var11;
                                             var63.targetPitch = var12;
                                             var63.placementBlockPos = var74;
                                             var63.targetDefensePos = var71;
                                             var63.placementFacing = var8;
                                             var63.placementHitVec = var13;
                                             break;
                                          }
                                       }
                                    }
                                 }

                                 if (this.placementBlockPos != null) {
                                    break;
                                 }
                              }
                           }
                        }

                        if (this.placementBlockPos != null) {
                           float var58 = MathHelper.wrapAngleTo180_float(this.targetYaw - this.playerYaw);
                           float var64 = this.targetPitch - this.playerPitch;
                           float var67 = (float)((Integer)this.speedProp.getValue()).intValue();
                           float var5 = (Float)this.smoothingProp.getValue();
                           float var59 = this.playerYaw + MathHelper.clamp_float(var58 * var5, -var67, var67);
                           float var65 = MathHelper.clamp_float(this.playerPitch + MathHelper.clamp_float(var64 * var5, -var67, var67), -90.0F, 90.0F);
                           float[] var60 = RotationUtil.applyGCD(this.playerYaw, this.playerPitch, var59, var65);
                           this.renderYaw = var60[0];
                           this.renderPitch = MathHelper.clamp_float(var60[1], -90.0F, 90.0F);
                           var1.setRotation(this.renderYaw, this.renderPitch, 6);
                           var1.setPervRotation(this.moveFixProp.getValue() != 0 ? this.renderYaw : mc.thePlayer.rotationYaw, 6);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void resetPlacementTarget() {
      this.placementBlockPos = null;
      this.targetDefensePos = null;
      this.placementFacing = null;
      this.placementHitVec = null;
   }

   public List<BlockPos> getBedDefensePositions() {
      ArrayList<BlockPos> var1 = new ArrayList<>();
      if (this.bedHeadPos != null && this.bedFacing != null) {
         BlockPos var2 = this.bedHeadPos;
         BlockPos var3 = this.bedHeadPos.offset(this.bedFacing);
         EnumFacing var4 = this.bedFacing;
         EnumFacing var5 = this.bedFacing.getOpposite();
         EnumFacing var6;
         EnumFacing var7 = (var6 = getPerpendicularFacing(var4)).getOpposite();
         var1.add(var2.offset(var6));
         var1.add(var2.offset(var7));
         var1.add(var3.offset(var6));
         var1.add(var3.offset(var7));
         var1.add(var2.offset(var5));
         var1.add(var3.offset(var4));
         var1.add(var2.up(1));
         var1.add(var3.up(1));
         return var1;
      } else {
         return var1;
      }
   }

   public static boolean isReplaceable(BlockPos var0) {
      Block var1;
      return (var1 = mc.theWorld.getBlockState(var0).getBlock()) == Blocks.air
         || var1 == Blocks.water
         || var1 == Blocks.flowing_water
         || var1 == Blocks.lava
         || var1 == Blocks.flowing_lava
         || var1 == Blocks.fire;
   }

   public static EnumFacing getPerpendicularFacing(EnumFacing facing) {
      switch (facing) {
         case NORTH:
            return EnumFacing.WEST;
         case SOUTH:
            return EnumFacing.EAST;
         case WEST:
            return EnumFacing.SOUTH;
         case EAST:
            return EnumFacing.NORTH;
         default:
            return EnumFacing.NORTH;
      }
   }

   public static MovingObjectPosition rayTrace(float yaw, float pitch, double reachDistance) {
      yaw = (float)Math.toRadians((double)yaw);
      pitch = (float)Math.toRadians((double)pitch);
      double var4 = -Math.sin((double)yaw) * Math.cos((double)pitch);
      double var6 = -Math.sin((double)pitch);
      double var8 = Math.cos((double)yaw) * Math.cos((double)pitch);
      Vec3 var11;
      Vec3 var13 = (var11 = mc.thePlayer.getPositionEyes(1.0F)).addVector(var4 * reachDistance, var6 * reachDistance, var8 * reachDistance);
      return mc.theWorld.rayTraceBlocks(var11, var13);
   }

   public static float[] getRotations(double dx, double dy, double dz) {
      double var6 = Math.sqrt(dx * dx + dz * dz);
      float var8 = MathHelper.wrapAngleTo180_float((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
      float var1 = (float)(-Math.toDegrees(Math.atan2(dy, var6)));
      return new float[]{var8, var1};
   }

   @Override
   public void onDisabled() {
      if (this.originalHotbarSlot != -1 && mc.thePlayer != null && mc.thePlayer.inventory.currentItem != this.originalHotbarSlot) {
         mc.thePlayer.inventory.currentItem = this.originalHotbarSlot;
      }

      this.defenseProgress = 0.0F;
      this.lastPlacedPos = null;
      this.resetPlacementTarget();
      this.bedHeadPos = null;
      this.bedFacing = null;
   }

   @Override
   public void onEnabled() {
      if (mc.thePlayer != null) {
         this.playerYaw = mc.thePlayer.rotationYaw;
         this.playerPitch = mc.thePlayer.rotationPitch;
         this.targetYaw = this.playerYaw;
         this.targetPitch = this.playerPitch;
         this.renderYaw = this.playerYaw;
         this.renderPitch = this.playerPitch;
         this.originalHotbarSlot = mc.thePlayer.inventory.currentItem;
      }

      this.defenseProgress = 0.0F;
      this.lastPlaceTime = 0L;
      this.lastPlacedPos = null;
      this.resetPlacementTarget();
      this.bedHeadPos = null;
      this.bedFacing = null;
   }

   public static boolean canPlaceAt(BlockPos var0) {
      if (!isReplaceable(var0)) {
         return false;
      } else {
         AxisAlignedBB var1 = new AxisAlignedBB(
            (double)var0.getX(), (double)var0.getY(), (double)var0.getZ(), (double)var0.getX() + 1.0, (double)var0.getY() + 1.0, (double)var0.getZ() + 1.0
         );
         return mc.theWorld.checkNoEntityCollision(var1);
      }
   }

   @EventTarget
   public void onMove(MoveInputEvent var1) {
      if (this.isEnabled()) {
         if ((Integer)this.moveFixProp.getValue() == 1 && MovementManager.isRotated() && MovementManager.getPriority() == 6.0F && MoveUtil.isMoving()) {
            MoveUtil.fixMovement(MovementManager.getMovementYaw());
         }
      }
   }

   @EventTarget
   public void onSwap(SwapItemEvent var1) {
      if (this.isEnabled()) {
         this.originalHotbarSlot = var1.setSlot(this.originalHotbarSlot);
         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent var1) {
      if (this.isEnabled() && mc.currentScreen == null) {
         if ((Boolean)this.showProgressProp.getValue()) {
            if (mc.fontRendererObj != null) {
               String var4 = String.format("%.0f%%", this.defenseProgress * 100.0F);
               GL11.glPushMatrix();
               GlStateManager.disableDepth();
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
               ScaledResolution var2 = new ScaledResolution(mc);
               int var3 = mc.fontRendererObj.getStringWidth(var4);
               mc.fontRendererObj
                  .drawString(
                     var4,
                     (float)var2.getScaledWidth() / 2.0F - (float)var3 / 2.0F,
                     (float)var2.getScaledHeight() / 5.0F * 2.0F,
                     (this.defenseProgress <= 0.33F ? new Color(255, 85, 85) : (this.defenseProgress <= 0.66F ? new Color(255, 255, 85) : new Color(85, 255, 85))).getRGB() & 16777215
                        | -1090519040,
                     true
                  );
               GlStateManager.disableBlend();
               GlStateManager.enableDepth();
               GL11.glPopMatrix();
            }
         }
      }
   }

   public AutoBedDef() {
      super("AutoBedDef", false, ModuleCategory.WORLD);
   }
}
