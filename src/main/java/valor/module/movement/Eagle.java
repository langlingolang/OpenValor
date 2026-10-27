package valor.module.movement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Keyboard;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.MoveInputEvent;
import valor.events.PacketEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.hook.EntityRendererHooks;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.util.MoveUtil;
import valor.util.PlayerSimulator;

public class Eagle extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final EnumFacing[] HORIZONTAL_FACINGS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
   public final FloatProperty edgeOffsetProp = new FloatProperty("edge-offset", 0.0F, 0.0F, 0.3F);
   public final IntProperty unsneakDelayProp = new IntProperty("unsneak-delay", 50, 50, 300);
   public final IntProperty sneakOnJumpProp = new IntProperty("sneak-on-jump", 0, 0, 500);
   public final BooleanProperty sneakKeyPressedProp = new BooleanProperty("sneak-key-pressed", Boolean.FALSE);
   public final BooleanProperty holdingBlocksProp = new BooleanProperty("holding-blocks", Boolean.FALSE);
   public final BooleanProperty lookingDownProp = new BooleanProperty("looking-down", Boolean.FALSE);
   public final BooleanProperty notMovingForwardProp = new BooleanProperty("not-moving-forward", Boolean.FALSE);
   public final BooleanProperty prePlaceProp = new BooleanProperty("pre-place", Boolean.FALSE);
   private boolean isSneaking;
   private boolean blockPlacedWhileSneaking;
   private boolean wasSneakKeyPressed;
   private int jumpSneakDelayTicks = -1;
   private int jumpSneakStartTick = -1;
   private int unsneakDelayTicks = -1;
   private int unsneakStartTick = -1;
   private float targetYaw;
   private float targetPitch;
   private MovingObjectPosition targetMouseOver;
   private boolean rotationSpoofed = false;

   @EventTarget(3)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (this.rotationSpoofed) {
            var1.setRotation(this.targetYaw, this.targetPitch, 2);
            this.rotationSpoofed = false;
         }

         if (this.targetMouseOver != null) {
            mc.objectMouseOver = this.targetMouseOver;
         }
      }
   }

   private void resetTimers() {
      this.unsneakStartTick = -1;
      this.jumpSneakStartTick = -1;
      this.jumpSneakDelayTicks = -1;
      this.unsneakDelayTicks = -1;
   }

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.SEND) {
         if (var1.getPacket() instanceof C08PacketPlayerBlockPlacement
            && ((C08PacketPlayerBlockPlacement)var1.getPacket()).getPlacedBlockDirection() != 255
            && this.isSneaking
            && (Boolean)this.sneakKeyPressedProp.getValue()) {
            this.blockPlacedWhileSneaking = true;
         }
      }
   }

   private void handleUnsneakDelay() {
      int var1 = mc.thePlayer.ticksExisted;
      if (this.unsneakStartTick == -1 && this.jumpSneakStartTick == -1) {
         this.unsneakStartTick = var1;
         double var3;
         int var2 = (int)(var3 = (double)((Integer)this.unsneakDelayProp.getValue() - 50) / 50.0);
         this.unsneakDelayTicks = var2 + (Math.random() < var3 - (double)var2 ? 1 : 0);
      }

      if (this.jumpSneakStartTick != -1 && var1 - this.jumpSneakStartTick < this.jumpSneakDelayTicks) {
         this.setSneaking(false);
      } else if (this.unsneakStartTick != -1 && var1 - this.unsneakStartTick < this.unsneakDelayTicks) {
         this.setSneaking(false);
      } else {
         this.unsneak();
      }
   }

   private void restoreSneakState() {
      if (this.wasSneakKeyPressed && isSneakKeyDown()) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
         mc.thePlayer.movementInput.sneak = true;
      }

      this.wasSneakKeyPressed = false;
   }

   private void setSneaking(boolean resetUnsneakTimer) {
      boolean var2 = mc.thePlayer.movementInput.sneak;
      if (!(Boolean)this.sneakKeyPressedProp.getValue()) {
         mc.thePlayer.movementInput.sneak = true;
         if (!var2) {
            mc.thePlayer.movementInput.moveStrafe *= 0.3F;
            mc.thePlayer.movementInput.moveForward *= 0.3F;
         }
      } else {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
         mc.thePlayer.movementInput.sneak = true;
      }

      this.isSneaking = true;
      if (resetUnsneakTimer) {
         this.unsneakStartTick = -1;
      }

      this.restoreSneakState();
   }

   private void unsneak() {
      if (!(Boolean)this.sneakKeyPressedProp.getValue()) {
         mc.thePlayer.movementInput.sneak = false;
      } else if (!this.isSneaking || !isSneakKeyDown() || !this.blockPlacedWhileSneaking && mc.thePlayer.onGround) {
         if (this.wasSneakKeyPressed) {
            mc.thePlayer.movementInput.sneak = false;
         }
      } else {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
         mc.thePlayer.movementInput.sneak = false;
         this.wasSneakKeyPressed = true;
      }

      this.isSneaking = false;
      this.blockPlacedWhileSneaking = false;
      this.resetTimers();
   }

   private void resetEagle() {
      this.isSneaking = false;
      this.resetTimers();
      if ((Boolean)this.sneakKeyPressedProp.getValue()) {
         this.restoreSneakState();
      }
   }

   private static AxisAlignedBB getPredictedBoundingBox() {
      try {
         PlayerSimulator var12;
         (var12 = PlayerSimulator.simulateTick(mc.thePlayer.movementInput)).movementInput.sneak = false;
         var12.handleWaterMovement();
         if (var12.world.isRemote) {
            var12.fire = 0;
         } else if (var12.fire > 0) {
            var12.fire--;
         }

         if (var12.isInLava()) {
            var12.setJumpTicks(15);
            var12.fallDistance *= 0.5F;
         }

         if (!(var12.posY < -64.0) && !var12.player.isRiding()) {
            var12.setSprinting(false);
            var12.pushOutOfBlocks(var12.posX - (double)var12.width * 0.35, var12.boundingBox.minY + 0.5, var12.posZ + (double)var12.width * 0.35);
            var12.pushOutOfBlocks(var12.posX - (double)var12.width * 0.35, var12.boundingBox.minY + 0.5, var12.posZ - (double)var12.width * 0.35);
            var12.pushOutOfBlocks(var12.posX + (double)var12.width * 0.35, var12.boundingBox.minY + 0.5, var12.posZ - (double)var12.width * 0.35);
            var12.pushOutOfBlocks(var12.posX + (double)var12.width * 0.35, var12.boundingBox.minY + 0.5, var12.posZ + (double)var12.width * 0.35);
            if (var12.player.isUsingItem() && var12.ridingEntity == null) {
               var12.movementInput.moveStrafe *= 0.2F;
               var12.movementInput.moveForward *= 0.2F;
            }

            int var3;
            boolean var13 = (var3 = var12.foodStats.getFoodLevel()) > 6 || var12.capabilities.allowFlying;
            boolean var5 = var12.isPotionActive(Potion.blindness);
            if (var12.isSprinting
               && !var12.movementInput.sneak
               && var12.onGround
               && !var12.isCollidedHorizontally
               && (var12.movementInput.moveForward >= 0.8F || var12.movementInput.moveStrafe != 0.0F)
               && var13
               && !var5) {
               var12.noClip = true;
            }

            if (var12.movementInput.sneak) {
               var12.noClip = false;
            }

            if (var12.noClip
               && (
                  var12.movementInput.moveForward <= 0.0F
                     || var12.movementInput.sneak
                     || var12.isCollidedHorizontally
                     || var3 <= 6 && !var12.capabilities.allowFlying
               )) {
               var12.noClip = false;
            }

            if (var12.capabilities.allowFlying && PlayerSimulator.mc.playerController.isSpectatorMode() && !var12.capabilities.isFlying) {
               var12.capabilities.isFlying = true;
            }

            if (var12.capabilities.isFlying) {
               if (var12.movementInput.sneak) {
                  var12.motionY = var12.motionY - (double)(var12.capabilities.getFlySpeed() * 3.0F);
               }

               if (var12.movementInput.jump) {
                  var12.motionY = var12.motionY + (double)(var12.capabilities.getFlySpeed() * 3.0F);
               }
            }

            if (var12.jumpTicks > 0) {
               var12.jumpTicks--;
            }

            if (Math.abs(var12.motionX) < 0.005) {
               var12.motionX = 0.0;
            }

            if (Math.abs(var12.motionY) < 0.005) {
               var12.motionY = 0.0;
            }

            if (Math.abs(var12.motionZ) < 0.005) {
               var12.motionZ = 0.0;
            }

            if (var12.player.getHealth() <= 0.0F || var12.player.isPlayerSleeping()) {
               var12.jumping = false;
               var12.moveForward = 0.0F;
               var12.moveStrafing = 0.0F;
            } else {
               var12.moveStrafing = var12.movementInput.moveForward;
               var12.moveForward = var12.movementInput.moveStrafe;
               var12.jumping = var12.movementInput.jump;
            }

            if (var12.jumping) {
               if (var12.inWater || var12.isInLava()) {
                  var12.motionY += 0.04F;
               } else if (var12.onGround && var12.jumpTicks == 0) {
                  var12.motionY = 0.42F;
                  if (var12.isPotionActive(Potion.jump)) {
                     Potion var8 = Potion.jump;
                     var12.motionY = var12.motionY + (double)((float)(var12.player.getActivePotionEffect(var8).getAmplifier() + 1) * 0.1F);
                  }

                  if (var12.noClip) {
                     float var14 = var12.rotationYaw * (float) (Math.PI / 180.0);
                     var12.motionX = var12.motionX - (double)(MathHelper.sin(var14) * 0.2F);
                     var12.motionZ = var12.motionZ + (double)(MathHelper.cos(var14) * 0.2F);
                  }

                  var12.isAirBorne = true;
               }
            } else {
               var12.jumpTicks = 0;
            }

            var12.moveForward *= 0.98F;
            var12.moveStrafing *= 0.98F;
            float var16 = var12.moveStrafing;
            float var15 = var12.moveForward;
            if (var12.capabilities.isFlying && var12.ridingEntity == null) {
               double var18 = var12.motionY;
               float var17 = var12.jumpMovementFactor;
               var12.jumpMovementFactor = var12.capabilities.getFlySpeed() * (float)(var12.noClip ? 2 : 1);
               var12.moveEntityWithHeading(var15, var16);
               var12.motionY = var18 * 0.6;
               var12.jumpMovementFactor = var17;
            } else {
               var12.moveEntityWithHeading(var15, var16);
            }

            var12.jumpMovementFactor = 0.02F;
            if (var12.noClip) {
               var12.jumpMovementFactor = (float)((double)var12.jumpMovementFactor + 0.005999999865889549);
            }

            if (var12.onGround && var12.capabilities.isFlying && !var12.isSpectator) {
               var12.capabilities.isFlying = false;
            }

            var12.setSprinting(true);
         }

         return var12.boundingBox;
      } catch (Throwable var10) {
         System.out.println("[Valor] Eagle SimulatedPlayer failed, using fallback: ".concat(String.valueOf(var10)));
         double[] var0 = MoveUtil.getForwardAndStrafeMotion();
         double var2 = mc.thePlayer.posX + mc.thePlayer.motionX + var0[0];
         double var4 = mc.thePlayer.posZ + mc.thePlayer.motionZ + var0[1];
         double var6 = mc.thePlayer.posY;
         float var11 = mc.thePlayer.width;
         float var1 = mc.thePlayer.height;
         return new AxisAlignedBB(
            var2 - (double)var11 * 0.5, var6, var4 - (double)var11 * 0.5, var2 + (double)var11 * 0.5, var6 + (double)var1, var4 + (double)var11 * 0.5
         );
      }
   }

   @Override
   public void onDisabled() {
      this.isSneaking = false;
      this.resetTimers();
      this.wasSneakKeyPressed = false;
      this.targetMouseOver = null;
      EntityRendererHooks.setMouseOverLock(0.0F, 0.0F, false);
   }

   private static boolean isSneakKeyDown() {
      return Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
   }

   @EventTarget
   public void onMoveInput(MoveInputEvent var1) {
      if (this.isEnabled()) {
         if (mc.thePlayer != null && mc.theWorld != null && mc.currentScreen == null && !mc.thePlayer.capabilities.isFlying) {
            boolean var25 = isSneakKeyDown();
            boolean var2 = (Boolean)this.sneakKeyPressedProp.getValue();
            if (var25 && !var2) {
               this.resetTimers();
            } else {
               float var3 = mc.thePlayer.movementInput.moveForward;
               float var4 = mc.thePlayer.movementInput.moveStrafe;
               boolean var5 = mc.thePlayer.movementInput.jump;
               if (!var2 || var25 && (var3 != 0.0F || var4 != 0.0F)) {
                  if ((Boolean)this.notMovingForwardProp.getValue() && var3 > 0.0F) {
                     this.resetEagle();
                  } else if ((Boolean)this.lookingDownProp.getValue() && mc.thePlayer.rotationPitch < 70.0F) {
                     this.resetEagle();
                  } else {
                     ItemStack var6;
                     if (!(Boolean)this.holdingBlocksProp.getValue() || (var6 = mc.thePlayer.getHeldItem()) != null && var6.getItem() instanceof ItemBlock) {
                        if (!var5 || !mc.thePlayer.onGround || var3 == 0.0F && var4 == 0.0F || (Integer)this.sneakOnJumpProp.getValue() <= 0 || var2 && !this.wasSneakKeyPressed) {
                           AxisAlignedBB var26 = getPredictedBoundingBox();
                           AxisAlignedBB var28 = new AxisAlignedBB(var26.minX, var26.minY - 0.01, var26.minZ, var26.maxX, var26.minY, var26.maxZ);
                           List var29;
                           double var10000;
                           if ((var29 = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, var28)).isEmpty()) {
                              var10000 = Double.NaN;
                           } else {
                              double var9 = (var26.minX + var26.maxX) / 2.0;
                              double var11 = (var26.minZ + var26.maxZ) / 2.0;
                              double var13 = Double.MAX_VALUE;
                              Iterator var27 = var29.iterator();

                              while (var27.hasNext()) {
                                 AxisAlignedBB var30;
                                 double var15 = Math.max((var30 = (AxisAlignedBB)var27.next()).minX, Math.min(var9, var30.maxX));
                                 double var17 = Math.max(var30.minZ, Math.min(var11, var30.maxZ));
                                 double var19 = Math.abs(var9 - var15);
                                 double var21 = Math.abs(var11 - var17);
                                 double var23 = Math.max(var19, var21);
                                 var13 = Math.min(var13, var23);
                              }

                              var10000 = var13;
                           }

                           double var7 = var10000;
                           if (!Double.isNaN(var10000)) {
                              if (var7 > (double)((Float)this.edgeOffsetProp.getValue()).floatValue()) {
                                 this.setSneaking(true);
                              } else {
                                 if (this.isSneaking) {
                                    this.handleUnsneakDelay();
                                 }
                              }
                           } else {
                              if (var5 && ((Integer)this.sneakOnJumpProp.getValue() <= 0 || var3 == 0.0F && var4 == 0.0F)) {
                                 if (this.isSneaking) {
                                    this.handleUnsneakDelay();
                                    return;
                                 }
                              } else {
                                 if (mc.thePlayer.onGround) {
                                    this.setSneaking(true);
                                    return;
                                 }

                                 if (this.isSneaking) {
                                    this.handleUnsneakDelay();
                                 }
                              }
                           }
                        } else {
                           this.jumpSneakStartTick = mc.thePlayer.ticksExisted;
                           double var31;
                           int var8 = (int)(var31 = (double)((Integer)this.sneakOnJumpProp.getValue()).intValue() / 50.0);
                           this.jumpSneakDelayTicks = var8 + (Math.random() < var31 - (double)var8 ? 1 : 0);
                           this.setSneaking(true);
                        }
                     } else {
                        this.resetEagle();
                     }
                  }
               } else {
                  if (!var25) {
                     this.resetTimers();
                  }

                  this.restoreSneakState();
               }
            }
         }
      }
   }

   @EventTarget
   public void onTickPre(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         EntityRendererHooks.setMouseOverLock(0.0F, 0.0F, false);
         this.rotationSpoofed = false;
         this.targetMouseOver = null;
         if ((Boolean)this.prePlaceProp.getValue()) {
            if (mc.thePlayer != null && mc.theWorld != null && mc.currentScreen == null && !mc.thePlayer.capabilities.isFlying) {
               ItemStack var24;
               if ((var24 = mc.thePlayer.getHeldItem()) != null && var24.getItem() instanceof ItemBlock) {
                  if (!(Boolean)this.lookingDownProp.getValue() || !(mc.thePlayer.rotationPitch < 70.0F)) {
                     if (!(Boolean)this.notMovingForwardProp.getValue() || !(mc.thePlayer.movementInput.moveForward > 0.0F)) {
                        float var25 = mc.thePlayer.rotationPitch;
                        double var4 = (double)mc.playerController.getBlockReachDistance();
                        double var15 = var4;
                        float var2 = var25;
                        float var5 = mc.thePlayer.rotationYaw;
                        AxisAlignedBB var6;
                        int var19 = MathHelper.floor_double((var6 = mc.thePlayer.getEntityBoundingBox()).minY) - 1;
                        int var20 = MathHelper.floor_double(var6.minX);
                        int var7 = MathHelper.floor_double(var6.maxX);
                        int var3 = MathHelper.floor_double(var6.minZ);
                        int var36 = MathHelper.floor_double(var6.maxZ);
                        ArrayList<Eagle.BlockCandidate> var48 = new ArrayList<>();

                        for (int var8 = var20; var8 <= var7; var8++) {
                           for (int var9 = var3; var9 <= var36; var9++) {
                              BlockPos var10 = new BlockPos(var8, var19, var9);
                              if (!mc.theWorld.getBlockState(var10).getBlock().isReplaceable(mc.theWorld, var10)) {
                                 for (EnumFacing var14 : HORIZONTAL_FACINGS) {
                                    BlockPos var17 = var10.offset(var14);
                                    if (mc.theWorld.getBlockState(var17).getBlock().isReplaceable(mc.theWorld, var17)) {
                                       var48.add(new Eagle.BlockCandidate(var10, var14));
                                    }
                                 }
                              }
                           }
                        }

                        Eagle.PrePlaceRotation var10000;
                        if (var48.isEmpty()) {
                           var10000 = null;
                        } else {
                           float var56 = Float.MAX_VALUE;
                           float var58 = Float.NaN;
                           BlockPos var60 = null;
                           EnumFacing var61 = null;
                           MovingObjectPosition var63 = null;
                           float var67 = 60.0F;

                           while (var67 <= 90.0F) {
                              float var70;
                              if ((var70 = 1.0F + (float)(Math.random() * 2.0 - 1.0) * 0.38000003F) < 0.4F) {
                                 var70 = 0.4F;
                              }

                              if (var70 > 1.8F) {
                                 var70 = 1.8F;
                              }

                              float var32 = Math.min(var67 += var70, 90.0F);
                              Vec3 var64 = mc.thePlayer.getPositionEyes(1.0F);
                              var70 = MathHelper.cos(-var5 * (float) (Math.PI / 180.0) - (float) Math.PI);
                              float var37 = MathHelper.sin(-var5 * (float) (Math.PI / 180.0) - (float) Math.PI);
                              float var18 = -MathHelper.cos(-var32 * (float) (Math.PI / 180.0));
                              float var51 = MathHelper.sin(-var32 * (float) (Math.PI / 180.0));
                              Vec3 var38 = new Vec3((double)(var37 * var18), (double)var51, (double)(var70 * var18));
                              Vec3 var39 = var64.addVector(var38.xCoord * var15, var38.yCoord * var15, var38.zCoord * var15);
                              MovingObjectPosition var40;
                              MovingObjectPosition var41;
                              if ((
                                    var41 = (var40 = mc.theWorld.rayTraceBlocks(var64, var39, false, false, false)) != null
                                          && var40.typeOfHit == MovingObjectType.BLOCK
                                       ? var40
                                       : null
                                 )
                                 != null) {
                                 EnumFacing var52 = var41.sideHit;
                                 if (var41.sideHit != EnumFacing.UP && var52 != EnumFacing.DOWN) {
                                    BlockPos var65 = var41.getBlockPos();

                                    for (Eagle.BlockCandidate var74 : var48) {
                                       if (var65.equals(var74.blockPos) && var52 == var74.facing) {
                                          float var53;
                                          if ((var53 = Math.abs(var32 - var2)) < var56) {
                                             var56 = var53;
                                             var58 = var32;
                                             var60 = var74.blockPos;
                                             var61 = var74.facing;
                                             var63 = var41;
                                          }
                                          break;
                                       }
                                    }

                                    if (var67 >= 90.0F) {
                                       break;
                                    }
                                 }
                              }
                           }

                           var10000 = var60 != null && var61 != null && !Float.isNaN(var58) && var63 != null
                              ? new Eagle.PrePlaceRotation(var5, var58, var60, var61, var63)
                              : null;
                        }

                        Eagle.PrePlaceRotation var27 = var10000;
                        if (var10000 == null) {
                           this.targetMouseOver = null;
                           EntityRendererHooks.setMouseOverLock(0.0F, 0.0F, false);
                        } else {
                           this.targetMouseOver = var27.mouseOver;
                           mc.objectMouseOver = var27.mouseOver;
                           float var33 = mc.thePlayer.rotationYaw;
                           float var42 = var27.pitch;
                           float var16 = var27.yaw;
                           var2 = mc.thePlayer.rotationYaw;
                           float var75 = MathHelper.wrapAngleTo180_float(var16 - var2);
                           float var77 = var42 - var25;
                           float var54;
                           float[] var79;
                           if ((var54 = (float)Math.sqrt((double)(var75 * var75 + var77 * var77))) < 0.001F) {
                              var79 = new float[]{var16, MathHelper.clamp_float(var42, -90.0F, 90.0F)};
                           } else {
                              float var80 = 15.0F / 30.0F;
                              float var81 = 20.0F / 100.0F;
                              float var82 = 0.120000005F / 2.0F;
                              float var57 = 0.94F + (float)(Math.random() * 0.120000005F);
                              float var43 = 45.0F * var57;
                              float var59 = (float)Math.pow((double)Math.min(1.0F, var54 / 180.0F), 0.7);
                              float var83 = 20.0F / 100.0F;
                              float var62 = Math.max(0.8F, 1.0F - 0.2F * (1.0F - var59));
                              float var66 = Math.min(var43 * var62, var54) / var54;
                              float var68 = var2 + var75 * var66;
                              float var73 = var25 + var77 * var66;
                              var79 = new float[]{var68, MathHelper.clamp_float(var73, -90.0F, 90.0F)};
                           }

                           float[] var29 = var79;
                           float var10002 = var29[0];
                           float var44 = var29[1];
                           var16 = var10002;
                           float var49;
                           float var84 = var49 = (mc.gameSettings.mouseSensitivity * 0.5F + 0.5F) * 0.6F + 0.2F;
                           double var76 = (double)(var84 * var84 * var49) * 1.2;
                           float var55 = var16 - var33;
                           float var34 = var44 - var25;
                           float var45 = var33 + (float)((double)Math.round((double)var55 / var76) * var76);
                           float var50 = var25 + (float)((double)Math.round((double)var34 / var76) * var76);
                           float[] var26 = new float[]{var45, MathHelper.clamp_float(var50, -90.0F, 90.0F)};
                           this.targetYaw = var26[0];
                           this.targetPitch = var26[1];
                           EntityRendererHooks.setMouseOverLock(this.targetYaw, this.targetPitch, true);
                           var2 = mc.thePlayer.rotationYaw;
                           float var35 = mc.thePlayer.rotationPitch;
                           float var46 = mc.thePlayer.prevRotationYaw;
                           var5 = mc.thePlayer.prevRotationPitch;
                           mc.thePlayer.rotationYaw = var26[0];
                           mc.thePlayer.rotationPitch = var26[1];
                           mc.thePlayer.prevRotationYaw = var26[0];
                           mc.thePlayer.prevRotationPitch = var26[1];

                           try {
                              mc.entityRenderer.getMouseOver(1.0F);
                           } catch (Exception var23) {
                           }

                           mc.thePlayer.rotationYaw = var2;
                           mc.thePlayer.rotationPitch = var35;
                           mc.thePlayer.prevRotationYaw = var46;
                           mc.thePlayer.prevRotationPitch = var5;
                           this.rotationSpoofed = true;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public Eagle() {
      super("Eagle", false, ModuleCategory.MOVEMENT);
   }

   public static class BlockCandidate {
      public final BlockPos blockPos;
      public final EnumFacing facing;

      public BlockCandidate(BlockPos var1, EnumFacing var2) {
         this.blockPos = var1;
         this.facing = var2;
      }
   }

   public static class PrePlaceRotation {
      public final float yaw;
      public final float pitch;
      public final BlockPos blockPos;
      public final EnumFacing facing;
      public final MovingObjectPosition mouseOver;

      public PrePlaceRotation(float var1, float var2, BlockPos var3, EnumFacing var4, MovingObjectPosition var5) {
         this.yaw = var1;
         this.pitch = var2;
         this.blockPos = var3;
         this.facing = var4;
         this.mouseOver = var5;
      }
   }
}
