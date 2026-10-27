package valor.hook;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import valor.Valor;
import valor.module.movement.Jesus;

public class WorldHooks {
   public static IBlockState onGetBlockStateForRaytrace(World world, BlockPos pos) {
      return world.getBlockState(pos);
   }

   public static boolean onIsPushedByWater(Entity entity) {
      if (entity instanceof EntityPlayerSP && Valor.moduleManager != null) {
         Jesus jesus = (Jesus)Valor.moduleManager.modules.get(Jesus.class);
         if (jesus.isEnabled() && (Boolean)jesus.noPushProp.getValue()) {
            return false;
         }
      }
      return entity.isPushedByWater();
   }
}
