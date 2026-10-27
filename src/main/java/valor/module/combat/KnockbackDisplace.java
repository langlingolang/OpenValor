package valor.module.combat;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;

public class KnockbackDisplace extends Module {
   public static final Minecraft mc = Minecraft.getMinecraft();
   public static final int SCAN_ANGLES_COUNT = 16;

   public final FloatProperty scanRangeProp = new FloatProperty("scan-range", 5.0F, 2.0F, 8.0F);
   public final IntProperty voidDepthProp = new IntProperty("void-depth", 6, 3, 20);
   public final BooleanProperty forceSprintProp = new BooleanProperty("force-sprint", Boolean.FALSE);
   public final FloatProperty turnSpeedProp = new FloatProperty("turn-speed", 22.0F, 5.0F, 45.0F);
   public final FloatProperty toleranceProp = new FloatProperty("tolerance", 3.0F, 1.0F, 10.0F);
   public final IntProperty maxPrepareTicksProp = new IntProperty("max-prepare-ticks", 4, 1, 8);

   EntityPlayer targetPlayer;
   float targetYaw;
   int prepareTicks;
   int lastTicksExisted = -100;

   final void reset() {
      this.targetPlayer = null;
      this.targetYaw = 0.0F;
      this.prepareTicks = 0;
      this.lastTicksExisted = -100;
   }

   public static float calculateAngle(double x, double z) {
      return MathHelper.wrapAngleTo180_float((float)(Math.toDegrees(Math.atan2(z, x)) - 90.0));
   }

   public static boolean isSolidBlock(BlockPos pos) {
      Block block;
      return (block = mc.theWorld.getBlockState(pos).getBlock()).getMaterial().isSolid()
         && block.getCollisionBoundingBox(mc.theWorld, pos, mc.theWorld.getBlockState(pos)) != null;
   }

   @Override
   public void onDisabled() {
      this.reset();
   }

   public KnockbackDisplace() {
      super("KnockbackDisplace", false, ModuleCategory.COMBAT);
   }

   public static final class DirectionOffset {
      public final double dirX;
      public final double dirZ;

      public DirectionOffset(double dirX, double dirZ) {
         this.dirX = dirX;
         this.dirZ = dirZ;
      }
   }
}
