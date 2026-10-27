package valor.module.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.render.WorldOverlayStyle;

public class BedPlates extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final FloatProperty rangeProp = new FloatProperty("range", 20.0F, 10.0F, 50.0F);

   private final List<BlockPos> scannedBedPositions = new ArrayList<>();
   private int tickCounter = 0;
   public static final int[][] SURROUND_OFFSETS = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, 2, 0}};

   @EventTarget
   public void onRender3D(Render3DEvent var1) {
      if (this.isEnabled() && mc.thePlayer != null && mc.getRenderItem() != null) {
         double var2 = ReflectionUtil.getRenderPosX(mc.getRenderManager());
         double var4 = ReflectionUtil.getRenderPosY(mc.getRenderManager());
         double var6 = ReflectionUtil.getRenderPosZ(mc.getRenderManager());
         float var24 = -mc.getRenderManager().playerViewY;
         float var8 = mc.getRenderManager().playerViewX;
         Iterator<BlockPos> var9 = this.scannedBedPositions.iterator();

         while (var9.hasNext()) {
            BlockPos var10 = var9.next();
            BlockPos var11 = var10;
            LinkedHashMap<Block, Integer> var12 = new LinkedHashMap<>();

            for (int[] var16 : SURROUND_OFFSETS) {
               BlockPos var31 = var11.add(var16[0], var16[1], var16[2]);
               Block var17;
               if ((var17 = mc.theWorld.getBlockState(var31).getBlock()) != Blocks.air && !var17.isReplaceable(mc.theWorld, var31)) {
                  var12.merge(var17, 1, Integer::sum);
               }
            }

            if (!var12.isEmpty()) {
               float var27 = (float)(-(var12.size() - 1)) * 0.4F / 2.0F;
               int var28 = 0;

               for (Block var29 : var12.keySet()) {
                  ItemStack var30;
                  if ((var30 = new ItemStack(var29)).getItem() == null) {
                     var28++;
                  } else {
                     double var18 = (double)var10.getX() + 0.5 - var2 + (double)var27 + (double)((float)var28 * 0.4F);
                     double var20 = (double)var10.getY() + 1.8 - var4;
                     double var22 = (double)var10.getZ() + 0.5 - var6;
                     GlStateManager.pushMatrix();
                     GlStateManager.disableDepth();
                     GL11.glTranslated(var18, var20, var22);
                     GL11.glRotatef(var24, 0.0F, 1.0F, 0.0F);
                     GL11.glRotatef(var8, 1.0F, 0.0F, 0.0F);
                     GlStateManager.scale(-0.03F, -0.03F, 0.03F);
                     GlStateManager.disableLighting();
                     GlStateManager.disableColorMaterial();
                     Render3DUtil.setupRender3D();
                     WorldOverlayStyle.drawPill(-10.0F, -10.0F, 10.0F, 10.0F, 4.0F, RenderUtil.getThemeColor(), 215);
                     Render3DUtil.endRender3D();
                     GlStateManager.translate(0.0F, 0.0F, -100.0F);
                     GlStateManager.enableRescaleNormal();
                     GlStateManager.enableTexture2D();
                     RenderHelper.enableGUIStandardItemLighting();
                     mc.getRenderItem().renderItemIntoGUI(var30, -8, -8);
                     RenderHelper.disableStandardItemLighting();
                     GlStateManager.disableRescaleNormal();
                     GlStateManager.disableDepth();
                     GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                     GlStateManager.popMatrix();
                     var28++;
                  }
               }
            }
         }

         GlStateManager.enableDepth();
      }
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.POST) {
         this.tickCounter++;
         if (this.tickCounter >= 10) {
            this.tickCounter = 0;
            this.scannedBedPositions.clear();
            if (mc.theWorld != null && mc.thePlayer != null) {
               int var2 = (int)((Float)this.rangeProp.getValue()).floatValue();
               int var3 = (int)mc.thePlayer.posX;
               int var4 = (int)mc.thePlayer.posY;
               int var5 = (int)mc.thePlayer.posZ;
               int var6 = (var2 >> 4) + 1;
               int var7 = Math.max(0, var4 - 8);
               int var8 = Math.min(255, var4 + 8);

               for (int var9 = -var6; var9 <= var6; var9++) {
                  for (int var10 = -var6; var10 <= var6; var10++) {
                     Chunk var11;
                     if ((var11 = mc.theWorld.getChunkFromChunkCoords((var3 >> 4) + var9, (var5 >> 4) + var10)).isLoaded()) {
                        int var12 = (var3 >> 4) + var9 << 4;
                        int var13 = (var5 >> 4) + var10 << 4;

                        for (int var14 = var7; var14 <= var8; var14++) {
                           for (int var15 = 0; var15 < 16; var15++) {
                              for (int var16 = 0; var16 < 16; var16++) {
                                 BlockPos var17;
                                 if (var11.getBlock(var15, var14, var16) == Blocks.bed
                                    && (var11.getBlockMetadata(new BlockPos(var15, var14, var16)) & 8) != 0
                                    && (var17 = new BlockPos(var12 + var15, var14, var13 + var16)).distanceSq((double)var3, (double)var4, (double)var5)
                                       <= (double)((long)var2 * (long)var2)) {
                                    this.scannedBedPositions.add(var17);
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

   public BedPlates() {
      super("BedPlates", false, ModuleCategory.VISUAL);
   }
}
