package valor.module.render;

import java.awt.Color;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.events.Render2DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.util.BotCheckUtil;
import valor.util.ChatColor;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.RotationUtil;

public class Indicators extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public final FloatProperty scaleProp = new FloatProperty("scale", 1.0F, 0.5F, 1.5F);
   public final FloatProperty offsetProp = new FloatProperty("offset", 50.0F, 0.0F, 255.0F);
   public final BooleanProperty directionCheckProp = new BooleanProperty("direction-check", Boolean.TRUE);
   public final BooleanProperty fireballsProp = new BooleanProperty("fireballs", Boolean.TRUE);
   public final BooleanProperty pearlsProp = new BooleanProperty("pearls", Boolean.TRUE);
   public final BooleanProperty arrowsProp = new BooleanProperty("arrows", Boolean.TRUE);
   public final BooleanProperty eggProp = new BooleanProperty("egg", Boolean.TRUE);
   public final BooleanProperty snowballProp = new BooleanProperty("snowball", Boolean.TRUE);

   public boolean isValidProjectile(Entity entity) {
      if (mc.thePlayer == null) {
         return false;
      }
      double dotProduct = (entity.posX - entity.lastTickPosX) * (mc.thePlayer.posX - entity.posX)
         + (entity.posY - entity.lastTickPosY) * (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight() - entity.posY - (double) entity.height / 2.0)
         + (entity.posZ - entity.lastTickPosZ) * (mc.thePlayer.posZ - entity.posZ);

      if (dotProduct == 0.0) {
         return false;
      } else if (dotProduct < 0.0 && (Boolean) this.directionCheckProp.getValue()) {
         return false;
      } else if ((Boolean) this.fireballsProp.getValue() && entity instanceof EntityFireball) {
         return true;
      } else if ((Boolean) this.pearlsProp.getValue() && entity instanceof EntityEnderPearl) {
         return true;
      } else if ((Boolean) this.arrowsProp.getValue() && entity instanceof EntityArrow) {
         return true;
      } else {
         return (Boolean) this.eggProp.getValue() && entity instanceof EntityEgg ? true : (Boolean) this.snowballProp.getValue() && entity instanceof EntitySnowball;
      }
   }

   @EventTarget
   public void onRender(Render2DEvent event) {
      if (!this.isEnabled() || mc.thePlayer == null) {
         return;
      }

      for (Entity entity : BotCheckUtil.getSortedLoadedEntities().stream().filter(this::isValidProjectile).collect(Collectors.toList())) {
         float baseOffset = 10.0F + (Float) this.offsetProp.getValue();
         float angle = RotationUtil.getYawTo(
            Render3DUtil.interpolate(mc.thePlayer.posX, mc.thePlayer.prevPosX, (double) event.getPartialTicks()),
            Render3DUtil.interpolate(mc.thePlayer.posZ, mc.thePlayer.prevPosZ, (double) event.getPartialTicks()),
            Render3DUtil.interpolate(entity.posX, entity.prevPosX, (double) event.getPartialTicks()),
            Render3DUtil.interpolate(entity.posZ, entity.prevPosZ, (double) event.getPartialTicks())
         );
         if (mc.gameSettings.thirdPersonView == 2) {
            angle += 180.0F;
         }

         float sin = (float) Math.sin(Math.toRadians((double) angle));
         float cos = -((float) Math.cos(Math.toRadians((double) angle)));
         float scale = (Float) this.scaleProp.getValue();

         GlStateManager.pushMatrix();
         GlStateManager.disableDepth();
         GlStateManager.scale(scale, scale, 0.0F);
         GlStateManager.translate(
            (float) new ScaledResolution(mc).getScaledWidth() / 2.0F / scale,
            (float) new ScaledResolution(mc).getScaledHeight() / 2.0F / scale,
            0.0F
         );

         GlStateManager.pushMatrix();
         GlStateManager.translate(baseOffset * sin - 8.0F, baseOffset * cos - 8.0F, -300.0F);

         Item renderItem;
         if (entity instanceof EntityFireball) {
            renderItem = Items.fire_charge;
         } else if (entity instanceof EntityEnderPearl) {
            renderItem = Items.ender_pearl;
         } else if (entity instanceof EntityArrow) {
            renderItem = Items.arrow;
         } else if (entity instanceof EntityEgg) {
            renderItem = Items.egg;
         } else if (entity instanceof EntitySnowball) {
            renderItem = Items.snowball;
         } else {
            renderItem = new Item();
         }

         mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(renderItem), 0, 0);
         RenderHelper.disableStandardItemLighting();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();

         String distText = String.format("%dm", (int) mc.thePlayer.getDistanceToEntity(entity));
         GlStateManager.pushMatrix();
         GlStateManager.translate(baseOffset * sin - RenderUtil.getStringWidth(distText) / 2.0F + 1.0F, baseOffset * cos + 1.0F, -100.0F);

         HudThemeManager theme = HudThemeManager.getInstance();
         int textColor = theme != null
            ? theme.getColor(System.currentTimeMillis(), 0L).getRGB() & 16777215 | -1090519040
            : ChatColor.GRAY.rgb & 16777215 | -1090519040;
         RenderUtil.drawString(distText, 0.0F, 0.0F, new Color(textColor, true), true);
         GlStateManager.popMatrix();

         GlStateManager.pushMatrix();
         GlStateManager.translate((baseOffset + 15.0F) * sin + 1.0F, (baseOffset + 15.0F) * cos + 1.0F, -100.0F);
         Render3DUtil.setupRender3D();

         Color arrowColor;
         if (entity instanceof EntityFireball) {
            arrowColor = new Color(12676363);
         } else if (entity instanceof EntityEnderPearl) {
            arrowColor = new Color(2458740);
         } else if (entity instanceof EntityArrow) {
            arrowColor = new Color(9868950);
         } else {
            arrowColor = new Color(-1);
         }

         Render3DUtil.drawTracerArrow((float) (Math.atan2((double) cos, (double) sin) + Math.PI), arrowColor.getRGB());
         Render3DUtil.setupRender2D();
         GlStateManager.popMatrix();
         GlStateManager.enableDepth();
         GlStateManager.popMatrix();
      }
   }

   public Indicators() {
      super("Indicators", false, false, ModuleCategory.VISUAL);
   }
}
