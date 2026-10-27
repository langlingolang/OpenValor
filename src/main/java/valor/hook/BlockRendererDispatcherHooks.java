package valor.hook;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import valor.Valor;
import valor.module.render.BedESP;

public class BlockRendererDispatcherHooks {
   public static void onRenderBlock(IBlockState state, BlockPos pos) {
      if (Valor.moduleManager != null) {
         BedESP bedESP = (BedESP)Valor.moduleManager.modules.get(BedESP.class);
         if (bedESP != null && bedESP.isEnabled() && state.getBlock() instanceof BlockBed && state.getValue(BlockBed.PART) == EnumPartType.HEAD) {
            bedESP.beds.add(new BlockPos(pos));
         }
      }
   }
}
