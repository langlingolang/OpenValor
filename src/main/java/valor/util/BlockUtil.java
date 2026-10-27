package valor.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public final class BlockUtil {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static Vec3 getHitVec(BlockPos pos, EnumFacing facing) {
      Block block = mc.theWorld.getBlockState(pos).getBlock();
      Vec3 center = new Vec3(
         (double) pos.getX() + Math.min(Math.max(RandomUtil.nextDouble(0.0, 1.0), block.getBlockBoundsMinX()), block.getBlockBoundsMaxX()),
         (double) pos.getY() + Math.min(Math.max(RandomUtil.nextDouble(0.0, 1.0), block.getBlockBoundsMinY()), block.getBlockBoundsMaxY()),
         (double) pos.getZ() + Math.min(Math.max(RandomUtil.nextDouble(0.0, 1.0), block.getBlockBoundsMinZ()), block.getBlockBoundsMaxZ())
      );
      switch (facing) {
         case UP:
            return new Vec3(center.xCoord, (double) pos.getY() + block.getBlockBoundsMaxY(), center.zCoord);
         case NORTH:
            return new Vec3(center.xCoord, center.yCoord, (double) pos.getZ() + block.getBlockBoundsMinZ());
         case EAST:
            return new Vec3((double) pos.getX() + block.getBlockBoundsMaxX(), center.yCoord, center.zCoord);
         case SOUTH:
            return new Vec3(center.xCoord, center.yCoord, (double) pos.getZ() + block.getBlockBoundsMaxZ());
         case WEST:
            return new Vec3((double) pos.getX() + block.getBlockBoundsMinX(), center.yCoord, center.zCoord);
         default:
            return new Vec3(center.xCoord, (double) pos.getY() + block.getBlockBoundsMinY(), center.zCoord);
      }
   }

   public static boolean isInteractableBlock(Block block) {
      if (block instanceof BlockContainer) {
         return true;
      } else if (block instanceof BlockWorkbench) {
         return true;
      } else if (block instanceof BlockAnvil) {
         return true;
      } else if (block instanceof BlockBed) {
         return true;
      } else if (block instanceof BlockDoor && block.getMaterial() != Material.iron) {
         return true;
      } else if (block instanceof BlockTrapDoor) {
         return true;
      } else if (block instanceof BlockFenceGate) {
         return true;
      } else if (block instanceof BlockFence) {
         return true;
      } else if (block instanceof BlockButton) {
         return true;
      } else {
         return block instanceof BlockLever || block instanceof BlockJukebox;
      }
   }

   public static boolean isFullSolidBlock(Block block) {
      if (block instanceof BlockStairs) {
         return false;
      } else if (block instanceof BlockSlab) {
         return false;
      } else if (block instanceof BlockEndPortalFrame) {
         return false;
      } else if (block instanceof BlockEndPortal) {
         return false;
      } else if (block instanceof BlockVine) {
         return false;
      } else if (block instanceof BlockPumpkin) {
         return false;
      } else if (block instanceof BlockCactus) {
         return false;
      } else if (block instanceof BlockBush) {
         return false;
      } else if (block instanceof BlockFalling) {
         return false;
      } else if (block instanceof BlockWeb) {
         return false;
      } else if (block instanceof BlockPane) {
         return false;
      } else if (block instanceof BlockCarpet) {
         return false;
      } else if (block instanceof BlockSnow) {
         return false;
      } else if (block instanceof BlockFence) {
         return false;
      } else if (block instanceof BlockFenceGate) {
         return false;
      } else if (block instanceof BlockWall) {
         return false;
      } else if (block instanceof BlockLadder) {
         return false;
      } else if (block instanceof BlockTorch) {
         return false;
      } else if (block instanceof BlockRedstoneWire) {
         return false;
      } else if (block instanceof BlockRedstoneDiode) {
         return false;
      } else if (block instanceof BlockBasePressurePlate) {
         return false;
      } else if (block instanceof BlockTripWire) {
         return false;
      } else if (block instanceof BlockTripWireHook) {
         return false;
      } else if (block instanceof BlockRailBase) {
         return false;
      } else {
         return !(block instanceof BlockSlime) && !(block instanceof BlockTNT);
      }
   }

   public static boolean isReplaceable(BlockPos pos) {
      return isReplaceable(mc.theWorld.getBlockState(pos).getBlock());
   }

   public static boolean isReplaceable(Block block) {
      if (!block.getMaterial().isReplaceable()) {
         return false;
      } else {
         return !(block instanceof BlockSnow) || !(block.getBlockBoundsMaxY() > 0.125);
      }
   }

   public static Vec3 getHitVecOrRayTrace(BlockPos pos, EnumFacing facing, float yaw, float pitch) {
      MovingObjectPosition mop = RotationUtil.rayTrace(yaw, pitch, (double) mc.playerController.getBlockReachDistance());
      return mop != null
         && mop.typeOfHit == MovingObjectType.BLOCK
         && mop.getBlockPos().equals(pos)
         && mop.sideHit == facing
         ? mop.hitVec
         : getHitVec(pos, facing);
   }

   public static boolean isInteractableBlock(BlockPos pos) {
      return isInteractableBlock(mc.theWorld.getBlockState(pos).getBlock());
   }
}
