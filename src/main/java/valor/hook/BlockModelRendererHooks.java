package valor.hook;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockModelRendererHooks {
   public static Boolean onRenderModel(
      BlockModelRenderer blockRenderer,
      IBlockAccess world,
      IBakedModel bakedModel,
      IBlockState state,
      BlockPos pos,
      WorldRenderer worldRenderer,
      boolean checkSides
   ) {
      return null;
   }
}
