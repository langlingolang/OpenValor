package valor.module.render;

import java.awt.Color;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import valor.Valor;
import valor.event.EventTarget;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;

public class ChestESP extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public final BooleanProperty themeColorsProp = new BooleanProperty("theme-colors", Boolean.TRUE);
   public final ColorProperty chestColorProp = new ColorProperty("chest", new Color(255, 170, 0).getRGB(), () -> !(Boolean)this.themeColorsProp.getValue());
   public final ColorProperty trappedChestColorProp = new ColorProperty("trapped-chest", new Color(255, 43, 0).getRGB(), () -> !(Boolean)this.themeColorsProp.getValue());
   public final ColorProperty enderChestColorProp = new ColorProperty("ender-chest", new Color(26, 17, 0).getRGB(), () -> !(Boolean)this.themeColorsProp.getValue());
   public final ModeProperty renderStyleProp = new ModeProperty("render-style", 0, new String[]{"POST", "VANILLA"});
   public final PercentageProperty fillOpacityProp = new PercentageProperty("fill-opacity", 14, () -> (Integer)this.renderStyleProp.getValue() == 0);
   public final BooleanProperty tracersProp = new BooleanProperty("tracers", Boolean.FALSE);

   public ChestESP() {
      super("ChestESP", false, ModuleCategory.VISUAL);
   }

   @EventTarget
   public void onRender(Render3DEvent event) {
      if (!this.isEnabled() || mc.theWorld == null) {
         return;
      }

      Render3DUtil.setupRender3D();

      for (TileEntity te : mc.theWorld.loadedTileEntityList.stream()
            .filter(tile -> tile instanceof TileEntityChest || tile instanceof TileEntityEnderChest)
            .collect(Collectors.toList())) {
         BlockPos pos = te.getPos();
         Block block = mc.theWorld.getBlockState(pos).getBlock();

         double minZOffset = 0.0625;
         double minXOffset = 0.0625;
         double maxZOffset = 0.9375;
         double maxXOffset = 0.9375;
         Color color;

         if (block instanceof BlockChest) {
            color = block.canProvidePower()
               ? ((Boolean)this.themeColorsProp.getValue() ? new Color(255, 105, 122) : new Color((Integer)this.trappedChestColorProp.getValue()))
               : ((Boolean)this.themeColorsProp.getValue() ? RenderUtil.getThemeColor() : new Color((Integer)this.chestColorProp.getValue()));

            EnumFacing facing = (EnumFacing)mc.theWorld.getBlockState(pos).getValue(BlockChest.FACING);
            if (facing == EnumFacing.NORTH) {
               if (mc.theWorld.getBlockState(pos.east()).getBlock() == block) continue;
               if (mc.theWorld.getBlockState(pos.west()).getBlock() == block) minXOffset = -0.9375;
            } else if (facing == EnumFacing.SOUTH) {
               if (mc.theWorld.getBlockState(pos.west()).getBlock() == block) continue;
               if (mc.theWorld.getBlockState(pos.east()).getBlock() == block) maxXOffset = 1.9375;
            } else if (facing == EnumFacing.WEST) {
               if (mc.theWorld.getBlockState(pos.north()).getBlock() == block) continue;
               if (mc.theWorld.getBlockState(pos.south()).getBlock() == block) maxZOffset = 1.9375;
            } else if (facing == EnumFacing.EAST) {
               if (mc.theWorld.getBlockState(pos.south()).getBlock() == block) continue;
               if (mc.theWorld.getBlockState(pos.north()).getBlock() == block) minZOffset = -0.9375;
            }
         } else {
            color = (Boolean)this.themeColorsProp.getValue()
               ? new Color(143, 126, 196)
               : new Color((Integer)this.enderChestColorProp.getValue());
         }

         RenderManager rm = mc.getRenderManager();
         AxisAlignedBB bb = new AxisAlignedBB(
            (double)pos.getX() + minXOffset,
            (double)pos.getY(),
            (double)pos.getZ() + minZOffset,
            (double)pos.getX() + maxXOffset,
            (double)pos.getY() + 0.875,
            (double)pos.getZ() + maxZOffset
         ).offset(-ReflectionUtil.getRenderPosX(rm), -ReflectionUtil.getRenderPosY(rm), -ReflectionUtil.getRenderPosZ(rm));

         if ((Integer)this.renderStyleProp.getValue() == 0) {
            int alpha = Math.max(0, Math.min(255, (int)((float)(Integer)this.fillOpacityProp.getValue() * 2.55F)));
            Render3DUtil.drawBox(bb, color, alpha, 1.5F, true);
         } else {
            Render3DUtil.drawOutlinedBox(bb, color.getRed(), color.getGreen(), color.getBlue(), 255, 1.5F);
         }

         if ((Boolean)this.tracersProp.getValue()) {
            float partialTicks = ReflectionUtil.getTimer(mc).renderPartialTicks;
            Vec3 eyeVec;
            if (mc.gameSettings.thirdPersonView == 0) {
               float pitch = Render3DUtil.lerpFloat(mc.getRenderViewEntity().rotationPitch, mc.getRenderViewEntity().prevRotationPitch, partialTicks);
               float yaw = Render3DUtil.lerpFloat(mc.getRenderViewEntity().rotationYaw, mc.getRenderViewEntity().prevRotationYaw, partialTicks);
               eyeVec = new Vec3(0.0, 0.0, 1.0)
                  .rotatePitch((float)(-Math.toRadians(pitch)))
                  .rotateYaw((float)(-Math.toRadians(yaw)));
            } else {
               float pitch = Render3DUtil.lerpFloat(mc.thePlayer.cameraPitch, mc.thePlayer.prevCameraPitch, partialTicks);
               float yaw = Render3DUtil.lerpFloat(mc.thePlayer.cameraYaw, mc.thePlayer.prevCameraYaw, partialTicks);
               eyeVec = new Vec3(0.0, 0.0, 0.0)
                  .rotatePitch((float)(-Math.toRadians(pitch)))
                  .rotateYaw((float)(-Math.toRadians(yaw)));
            }
            eyeVec = new Vec3(eyeVec.xCoord, eyeVec.yCoord + (double)mc.getRenderViewEntity().getEyeHeight(), eyeVec.zCoord);

            float tracerAlpha = 1.0F;
            Tracers tracersMod = (Tracers)Valor.moduleManager.getModule(Tracers.class);
            if (tracersMod != null) {
               tracerAlpha = (float)(Integer)tracersMod.opacityProp.getValue() / 100.0F;
            }

            Render3DUtil.drawTracerLine(
               eyeVec,
               (double)pos.getX() + 0.5,
               (double)pos.getY() + 0.5,
               (double)pos.getZ() + 0.5,
               (float)color.getRed() / 255.0F,
               (float)color.getGreen() / 255.0F,
               (float)color.getBlue() / 255.0F,
               tracerAlpha
            );
         }
      }

      Render3DUtil.endRender3D();
   }
}
