package valor.module.render;

import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;

public class BedESP extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final int SCAN_INTERVAL = 10;
   private static final int SCAN_RADIUS_MAX = 64;
   private static final int SCAN_HEIGHT_RANGE = 32;

   public final CopyOnWriteArraySet<BlockPos> beds = new CopyOnWriteArraySet<>();

   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"DEFAULT", "FULL"});
   public final ModeProperty renderStyleProp = new ModeProperty("render-style", 0, new String[]{"POST", "VANILLA"});
   public final ModeProperty colorProp = new ModeProperty("color", 1, new String[]{"CUSTOM", "THEME"});
   public final ColorProperty customColorProp = new ColorProperty("custom-color", 16733525, () -> (Integer)this.colorProp.getValue() == 0);
   public final PercentageProperty opacityProp = new PercentageProperty("opacity", 25);
   public final BooleanProperty outlineProp = new BooleanProperty("outline", Boolean.FALSE);
   public final BooleanProperty obsidianProp = new BooleanProperty("obsidian", Boolean.TRUE);

   private World lastWorld;
   private int tickCounter;

   public final double getBedHeight() {
      return this.modeProp.getValue() == 1 ? 1.0 : 0.5625;
   }

   @EventTarget
   public void onRender3D(Render3DEvent var1) {
      if (this.isEnabled()) {
         Render3DUtil.setupRender3D();

         for (BlockPos var2 : this.beds) {
            IBlockState var3;
            if ((var3 = mc.theWorld.getBlockState(var2)).getBlock() instanceof BlockBed && var3.getValue(BlockBed.PART) == EnumPartType.HEAD) {
               BlockPos var10 = var2.offset(var3.getValue(BlockBed.FACING).getOpposite());
               IBlockState var4;
               if ((var4 = mc.theWorld.getBlockState(var10)).getBlock() instanceof BlockBed && var4.getValue(BlockBed.PART) == EnumPartType.FOOT) {
                  if ((Boolean)this.obsidianProp.getValue()) {
                     for (EnumFacing var5 : Arrays.asList(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST)) {
                        BlockPos var6 = var2.offset(var5);
                        BlockPos var13 = var10.offset(var5);
                        boolean var7 = mc.theWorld.getBlockState(var6).getBlock() instanceof BlockObsidian;
                        boolean var8 = mc.theWorld.getBlockState(var13).getBlock() instanceof BlockObsidian;
                        if (var7 && var8) {
                           this.renderObsidianBoundingBox(
                              new AxisAlignedBB(
                                    (double)Math.min(var6.getX(), var13.getX()),
                                    (double)var6.getY(),
                                    (double)Math.min(var6.getZ(), var13.getZ()),
                                    Math.max((double)var6.getX() + 1.0, (double)var13.getX() + 1.0),
                                    (double)var6.getY() + 1.0,
                                    Math.max((double)var6.getZ() + 1.0, (double)var13.getZ() + 1.0)
                                 )
                                 .offset(
                                    -ReflectionUtil.getRenderPosX(mc.getRenderManager()),
                                    -ReflectionUtil.getRenderPosY(mc.getRenderManager()),
                                    -ReflectionUtil.getRenderPosZ(mc.getRenderManager())
                                 )
                           );
                        } else if (var7) {
                           this.renderObsidianBlock(var6);
                        } else if (var8) {
                           this.renderObsidianBlock(var13);
                        }
                     }
                  }

                  AxisAlignedBB var12 = new AxisAlignedBB(
                        (double)Math.min(var2.getX(), var10.getX()),
                        (double)var2.getY(),
                        (double)Math.min(var2.getZ(), var10.getZ()),
                        Math.max((double)var2.getX() + 1.0, (double)var10.getX() + 1.0),
                        (double)var2.getY() + this.getBedHeight(),
                        Math.max((double)var2.getZ() + 1.0, (double)var10.getZ() + 1.0)
                     )
                     .offset(
                        -ReflectionUtil.getRenderPosX(mc.getRenderManager()), -ReflectionUtil.getRenderPosY(mc.getRenderManager()), -ReflectionUtil.getRenderPosZ(mc.getRenderManager())
                     );
                  Color var10000;
                  switch (this.colorProp.getValue()) {
                     case 0:
                        var10000 = new Color((Integer)this.customColorProp.getValue());
                        break;
                     case 1:
                        var10000 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), 0L);
                        break;
                     default:
                        var10000 = new Color(-1);
                  }

                  Color var14 = var10000;
                  int var15 = Math.max(0, Math.min(255, (int)((float)((Integer)this.opacityProp.getValue()).intValue() * 2.55F)));
                  if ((Integer)this.renderStyleProp.getValue() == 0) {
                     Render3DUtil.drawBox(var12, var14, var15, 1.5F, (Boolean)this.outlineProp.getValue());
                  } else {
                     if ((Boolean)this.outlineProp.getValue()) {
                        Render3DUtil.drawOutlinedBox(var12, var14.getRed(), var14.getGreen(), var14.getBlue(), 255, 1.5F);
                     }

                     Render3DUtil.drawFilledBox(var12, var14.getRed(), var14.getGreen(), var14.getBlue(), var15);
                  }
               }
            } else {
               this.beds.remove(var2);
            }
         }

         Render3DUtil.endRender3D();
      }
   }

   public void renderObsidianBoundingBox(AxisAlignedBB var1) {
      Color var2 = RenderUtil.getThemeColor();
      var2 = new Color(Math.round((float)var2.getRed() * 0.7F), Math.round((float)var2.getGreen() * 0.7F), Math.round((float)var2.getBlue() * 0.7F));
      if ((Integer)this.renderStyleProp.getValue() == 0) {
         Render3DUtil.drawBox(var1, var2, 34, 1.5F, (Boolean)this.outlineProp.getValue());
      } else {
         if ((Boolean)this.outlineProp.getValue()) {
            Render3DUtil.drawOutlinedBox(var1, var2.getRed(), var2.getGreen(), var2.getBlue(), 255, 1.5F);
         }

         Render3DUtil.drawFilledBox(var1, var2.getRed(), var2.getGreen(), var2.getBlue(), 63);
      }
   }

   public void renderObsidianBlock(BlockPos var1) {
      AxisAlignedBB var2 = new AxisAlignedBB(
            (double)var1.getX(), (double)var1.getY(), (double)var1.getZ(), (double)var1.getX() + 1.0, (double)var1.getY() + 1.0, (double)var1.getZ() + 1.0
         )
         .offset(-ReflectionUtil.getRenderPosX(mc.getRenderManager()), -ReflectionUtil.getRenderPosY(mc.getRenderManager()), -ReflectionUtil.getRenderPosZ(mc.getRenderManager()));
      this.renderObsidianBoundingBox(var2);
   }

   @Override
   public void onDisabled() {
      this.beds.clear();
      this.lastWorld = null;
      this.tickCounter = 0;
   }

   @Override
   public void onEnabled() {
      this.lastWorld = mc.theWorld;
      this.beds.clear();
      this.tickCounter = 10;
      if (mc.renderGlobal != null) {
         mc.renderGlobal.loadRenderers();
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.POST) {
         if (mc.theWorld != this.lastWorld) {
            this.lastWorld = mc.theWorld;
            this.beds.clear();
            this.tickCounter = 10;
         }

         if (mc.theWorld != null && mc.thePlayer != null && ++this.tickCounter >= 10) {
            this.tickCounter = 0;
            BedESP var26 = this;
            int var2 = (int)Math.floor(mc.thePlayer.posX);
            int var3 = (int)Math.floor(mc.thePlayer.posY);
            int var4 = (int)Math.floor(mc.thePlayer.posZ);
            int var5 = var2 >> 4;
            int var6 = var4 >> 4;
            int var7 = Math.max(0, var3 - 32);
            int var8 = Math.min(255, var3 + 32);

            for (int var9 = var5 - 5; var9 <= var5 + 5; var9++) {
               for (int var10 = var6 - 5; var10 <= var6 + 5; var10++) {
                  Chunk var11;
                  if ((var11 = mc.theWorld.getChunkFromChunkCoords(var9, var10)).isLoaded()) {
                     int var12 = var9 << 4;
                     int var13 = var10 << 4;

                     for (int var14 = var7; var14 <= var8; var14++) {
                        for (int var15 = 0; var15 < 16; var15++) {
                           int var16 = var12 + var15;

                           for (int var17 = 0; var17 < 16; var17++) {
                              if (var11.getBlock(var15, var14, var17) == Blocks.bed) {
                                 int var18 = var13 + var17;
                                 long var20 = (long)(var16 - var2);
                                 long var22 = (long)(var14 - var3);
                                 long var24 = (long)(var18 - var4);
                                 if (var20 * var20 + var22 * var22 + var24 * var24 <= 4096L) {
                                    BlockPos var27 = new BlockPos(var16, var14, var18);
                                    IBlockState var19;
                                    if ((var19 = mc.theWorld.getBlockState(var27)).getBlock() instanceof BlockBed
                                       && var19.getValue(BlockBed.PART) == EnumPartType.HEAD) {
                                       var26.beds.add(var27);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public BedESP() {
      super("BedESP", false, ModuleCategory.VISUAL);
   }
}
