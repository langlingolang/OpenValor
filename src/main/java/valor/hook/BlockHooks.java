package valor.hook;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public class BlockHooks {
   public static EnumWorldBlockLayer onGetBlockLayer(Block block) {
      return null;
   }

   public static Boolean onShouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing facing, Block block) {
      return null;
   }
}
