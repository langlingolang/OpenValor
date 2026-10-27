package valor.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import valor.event.EventTarget;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.PercentageProperty;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;

public class Trajectories extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public final PercentageProperty opacityProp = new PercentageProperty("opacity", 100);
   public final BooleanProperty bowProp = new BooleanProperty("bow", Boolean.TRUE);
   public final BooleanProperty projectilesProp = new BooleanProperty("projectiles", Boolean.FALSE);
   public final BooleanProperty pearlsProp = new BooleanProperty("pearls", Boolean.TRUE);

   private static void addVertex(WorldRenderer wr, Vec3 vec) {
      wr.pos(vec.xCoord, vec.yCoord, vec.zCoord).endVertex();
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (!this.isEnabled()) {
         return;
      }
      if (mc.thePlayer == null || mc.thePlayer.getHeldItem() == null) {
         return;
      }
      if (mc.gameSettings.thirdPersonView != 0) {
         return;
      }

      Item heldItem = mc.thePlayer.getHeldItem().getItem();
      RenderManager renderManager = mc.getRenderManager();
      boolean isBow = false;
      float motionScale = 1.5F;
      float motionSlowdown = 0.99F;
      float gravity;
      float boundingBoxSize;

      if (heldItem instanceof ItemBow) {
         if (!(Boolean) this.bowProp.getValue()) {
            return;
         }
         if (!mc.thePlayer.isUsingItem()) {
            return;
         }
         isBow = true;
         gravity = 0.05F;
         boundingBoxSize = 0.3F;
         float duration = (float) mc.thePlayer.getItemInUseDuration() / 20.0F;
         duration = (duration * duration + duration * 2.0F) / 3.0F;
         if (duration < 0.1F) {
            return;
         }
         if (duration > 1.0F) {
            duration = 1.0F;
         }
         motionScale = duration * 3.0F;
      } else if (heldItem instanceof ItemFishingRod) {
         if (!(Boolean) this.projectilesProp.getValue()) {
            return;
         }
         gravity = 0.04F;
         boundingBoxSize = 0.25F;
         motionSlowdown = 0.92F;
      } else if (heldItem instanceof ItemSnowball || heldItem instanceof ItemEgg) {
         if (!(Boolean) this.projectilesProp.getValue()) {
            return;
         }
         gravity = 0.03F;
         boundingBoxSize = 0.25F;
      } else if (heldItem instanceof ItemEnderPearl) {
         if (!(Boolean) this.pearlsProp.getValue()) {
            return;
         }
         gravity = 0.03F;
         boundingBoxSize = 0.25F;
      } else {
         return;
      }

      float yaw = mc.thePlayer.rotationYaw;
      float pitch = mc.thePlayer.rotationPitch;

      double posX = ReflectionUtil.getRenderPosX(renderManager) - (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F);
      double posY = ReflectionUtil.getRenderPosY(renderManager) + (double) mc.thePlayer.getEyeHeight() - 0.10000000149011612;
      double posZ = ReflectionUtil.getRenderPosZ(renderManager) - (double) (MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F);

      double motionX = (double) (-MathHelper.sin(yaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0 : 0.4);
      double motionY = (double) (-MathHelper.sin(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0 : 0.4);
      double motionZ = (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0 : 0.4);

      float length = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
      motionX = (motionX / (double) length) * (double) motionScale;
      motionY = (motionY / (double) length) * (double) motionScale;
      motionZ = (motionZ / (double) length) * (double) motionScale;

      MovingObjectPosition landingPosition = null;
      boolean hasLanded = false;
      boolean hitEntity = false;

      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldRenderer = tessellator.getWorldRenderer();
      List<Vec3> points = new ArrayList<>();

      while (!hasLanded && posY > 0.0) {
         Vec3 start = new Vec3(posX, posY, posZ);
         Vec3 end = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
         landingPosition = mc.theWorld.rayTraceBlocks(start, end, false, true, false);

         start = new Vec3(posX, posY, posZ);
         end = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
         if (landingPosition != null) {
            hasLanded = true;
            end = new Vec3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
         }

         AxisAlignedBB chunkAABB = new AxisAlignedBB(
            posX - (double) boundingBoxSize, posY - (double) boundingBoxSize, posZ - (double) boundingBoxSize,
            posX + (double) boundingBoxSize, posY + (double) boundingBoxSize, posZ + (double) boundingBoxSize
         ).addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0);

         int minChunkX = MathHelper.floor_double((chunkAABB.minX - 2.0) / 16.0);
         int maxChunkX = MathHelper.floor_double((chunkAABB.maxX + 2.0) / 16.0);
         int minChunkZ = MathHelper.floor_double((chunkAABB.minZ - 2.0) / 16.0);
         int maxChunkZ = MathHelper.floor_double((chunkAABB.maxZ + 2.0) / 16.0);

         List<Entity> possibleEntities = new ArrayList<>();
         for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
               mc.theWorld.getChunkFromChunkCoords(cx, cz).getEntitiesWithinAABBForEntity(
                  mc.thePlayer, chunkAABB, possibleEntities, null
               );
            }
         }

         for (Entity entity : possibleEntities) {
            if (entity.canBeCollidedWith() && entity != mc.thePlayer) {
               AxisAlignedBB entityBox = entity.getEntityBoundingBox().expand(boundingBoxSize, boundingBoxSize, boundingBoxSize);
               MovingObjectPosition intercept = entityBox.calculateIntercept(start, end);
               if (intercept != null) {
                  hitEntity = true;
                  hasLanded = true;
                  landingPosition = intercept;
               }
            }
         }

         posX += motionX;
         posY += motionY;
         posZ += motionZ;

         BlockPos currentPos = new BlockPos(posX, posY, posZ);
         if (mc.theWorld.getBlockState(currentPos).getBlock().getMaterial() == Material.water) {
            motionX *= 0.6;
            motionY *= 0.6;
            motionZ *= 0.6;
         } else {
            motionX *= (double) motionSlowdown;
            motionY *= (double) motionSlowdown;
            motionZ *= (double) motionSlowdown;
         }

         motionY -= (double) gravity;

         points.add(new Vec3(
            posX - ReflectionUtil.getRenderPosX(renderManager),
            posY - ReflectionUtil.getRenderPosY(renderManager),
            posZ - ReflectionUtil.getRenderPosZ(renderManager)
         ));
      }

      if (points.size() <= 1) {
         return;
      }

      Color baseColor = hitEntity ? new Color(255, 105, 122) : RenderUtil.getThemeColor();
      int alpha = Math.round((float) ((Integer) this.opacityProp.getValue()).intValue() * 2.55F);
      Color color = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
      int rgba = color.getRGB();

      Render3DUtil.setupRender3D();
      Render3DUtil.color(rgba);
      GL11.glLineWidth(1.5F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);

      worldRenderer.begin(3, DefaultVertexFormats.POSITION);
      points.forEach(v -> addVertex(worldRenderer, v));
      tessellator.draw();

      GlStateManager.pushMatrix();
      GlStateManager.translate(
         posX - ReflectionUtil.getRenderPosX(renderManager),
         posY - ReflectionUtil.getRenderPosY(renderManager),
         posZ - ReflectionUtil.getRenderPosZ(renderManager)
      );

      if (landingPosition != null && landingPosition.sideHit != null) {
         EnumFacing.Axis axis = landingPosition.sideHit.getAxis();
         switch (axis) {
            case X:
               GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
               break;
            case Y:
               GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
               break;
            case Z:
               break;
         }
         Render3DUtil.drawVerticalLine(-0.25F, 0.25F, rgba);
         Render3DUtil.drawVerticalLine(0.25F, -0.25F, rgba);
      }

      GlStateManager.popMatrix();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
      Render3DUtil.endRender3D();
   }

   public Trajectories() {
      super("Trajectories", false, false, ModuleCategory.VISUAL);
   }
}
