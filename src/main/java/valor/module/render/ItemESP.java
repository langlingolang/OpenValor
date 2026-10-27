package valor.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import valor.event.EventTarget;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.PercentageProperty;
import valor.util.BotCheckUtil;
import valor.util.ChatColor;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.render.GlowShader;
import valor.util.render.WorldOverlayStyle;

public class ItemESP extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public Framebuffer glowFramebuffer;
   public GlowShader glowShader;

   public final BooleanProperty glowProp = new BooleanProperty("glow", Boolean.TRUE);
   public final PercentageProperty opacityProp = new PercentageProperty("opacity", 25, () -> !(Boolean) this.glowProp.getValue());
   public final BooleanProperty outlineProp = new BooleanProperty("outline", Boolean.FALSE);
   public final BooleanProperty itemCountProp = new BooleanProperty("item-count", Boolean.TRUE);
   public final BooleanProperty autoScaleProp = new BooleanProperty("auto-scale", Boolean.TRUE);
   public final BooleanProperty emeraldsProp = new BooleanProperty("emeralds", Boolean.TRUE);
   public final BooleanProperty diamondsProp = new BooleanProperty("diamonds", Boolean.TRUE);
   public final BooleanProperty goldProp = new BooleanProperty("gold", Boolean.TRUE);
   public final BooleanProperty ironProp = new BooleanProperty("iron", Boolean.TRUE);

   public static boolean isIronItem(int itemId) {
      Item item = Item.getItemById(itemId);
      Block block = Block.getBlockFromItem(item);
      return item == Items.iron_ingot || block == Blocks.iron_block || block == Blocks.iron_ore;
   }

   public static int getItemPriority(int itemId) {
      if (isEmeraldItem(itemId)) {
         return 4;
      } else if (isDiamondItem(itemId)) {
         return 3;
      } else if (isGoldItem(itemId)) {
         return 2;
      } else {
         return isIronItem(itemId) ? 1 : 0;
      }
   }

   public static boolean isGoldItem(int itemId) {
      Item item = Item.getItemById(itemId);
      Block block = Block.getBlockFromItem(item);
      return item == Items.gold_ingot || item == Items.gold_nugget || item == Items.golden_apple || block == Blocks.gold_block || block == Blocks.gold_ore;
   }

   public static boolean isEmeraldItem(int itemId) {
      Item item = Item.getItemById(itemId);
      Block block = Block.getBlockFromItem(item);
      return item == Items.emerald || block == Blocks.emerald_block || block == Blocks.emerald_ore;
   }

   public static boolean isDiamondItem(int itemId) {
      Item item = Item.getItemById(itemId);
      Block block = Block.getBlockFromItem(item);
      return item == Items.diamond
         || item == Items.diamond_sword
         || item == Items.diamond_pickaxe
         || item == Items.diamond_shovel
         || item == Items.diamond_axe
         || item == Items.diamond_hoe
         || item == Items.diamond_helmet
         || item == Items.diamond_chestplate
         || item == Items.diamond_leggings
         || item == Items.diamond_boots
         || block == Blocks.diamond_block
         || block == Blocks.diamond_ore;
   }

   public boolean renderGlowShader(List<EntityItem> items, float partialTicks) {
      if (items.isEmpty()) {
         return false;
      }

      if (this.glowFramebuffer == null || this.glowFramebuffer.framebufferWidth != mc.displayWidth || this.glowFramebuffer.framebufferHeight != mc.displayHeight) {
         if (this.glowFramebuffer != null) {
            this.glowFramebuffer.deleteFramebuffer();
         }

         this.glowFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
         this.glowFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
      }

      if (this.glowShader == null) {
         this.glowShader = new GlowShader();
      }

      if (!this.glowShader.isValid()) {
         return false;
      }

      this.glowFramebuffer.framebufferClear();
      this.glowFramebuffer.bindFramebuffer(true);
      GL11.glPushAttrib(1048575);

      try {
         RenderManager renderManager = mc.getRenderManager();
         for (EntityItem entityItem : items) {
            Render<EntityItem> render = renderManager.getEntityRenderObject(entityItem);
            if (render != null) {
               double renderX = Render3DUtil.interpolate(entityItem.posX, entityItem.lastTickPosX, (double) partialTicks) - ReflectionUtil.getRenderPosX(renderManager);
               double renderY = Render3DUtil.interpolate(entityItem.posY, entityItem.lastTickPosY, (double) partialTicks) - ReflectionUtil.getRenderPosY(renderManager);
               double renderZ = Render3DUtil.interpolate(entityItem.posZ, entityItem.lastTickPosZ, (double) partialTicks) - ReflectionUtil.getRenderPosZ(renderManager);
               render.doRender(entityItem, renderX, renderY, renderZ, entityItem.rotationYaw, partialTicks);
            }
         }
      } finally {
         GL11.glPopAttrib();
         mc.getFramebuffer().bindFramebuffer(true);
      }

      ScaledResolution sr = new ScaledResolution(mc);
      GL11.glPushAttrib(1048575);
      GL11.glMatrixMode(5889);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0, (double) sr.getScaledWidth(), (double) sr.getScaledHeight(), 0.0, -1.0, 1.0);
      GL11.glMatrixMode(5888);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();

      try {
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         GL11.glEnable(3553);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 1);
         Color themeColor = RenderUtil.getThemeColor();
         this.glowShader.glowColor = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 210);
         this.glowShader.useShader();
         GL11.glBindTexture(3553, this.glowFramebuffer.framebufferTexture);
         GL11.glTexParameteri(3553, 10241, 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
         GL11.glTexParameteri(3553, 10242, 33071);
         GL11.glTexParameteri(3553, 10243, 33071);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glBegin(7);
         GL11.glTexCoord2f(0.0F, 1.0F);
         GL11.glVertex2f(0.0F, 0.0F);
         GL11.glTexCoord2f(0.0F, 0.0F);
         GL11.glVertex2f(0.0F, (float) sr.getScaledHeight());
         GL11.glTexCoord2f(1.0F, 0.0F);
         GL11.glVertex2f((float) sr.getScaledWidth(), (float) sr.getScaledHeight());
         GL11.glTexCoord2f(1.0F, 1.0F);
         GL11.glVertex2f((float) sr.getScaledWidth(), 0.0F);
         GL11.glEnd();
      } finally {
         GL20.glUseProgram(0);
         GL11.glMatrixMode(5888);
         GL11.glPopMatrix();
         GL11.glMatrixMode(5889);
         GL11.glPopMatrix();
         GL11.glMatrixMode(5888);
         GL11.glPopAttrib();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

      return true;
   }

   @Override
   public void onDisabled() {
      if (this.glowFramebuffer != null) {
         this.glowFramebuffer.deleteFramebuffer();
         this.glowFramebuffer = null;
      }
   }

   @EventTarget
   public void onRender(Render3DEvent event) {
      if (!this.isEnabled()) {
         return;
      }

      LinkedHashMap<ItemCluster, Integer> clusterCountMap = new LinkedHashMap<>();
      ArrayList<EntityItem> renderItemList = new ArrayList<>();
      HashSet<ItemCluster> glowClusterSet = new HashSet<>();

      for (Entity entity : BotCheckUtil.getSortedLoadedEntities()) {
         if (entity.ticksExisted >= 3
            && (entity.ignoreFrustumCheck || Render3DUtil.isBoundingBoxInFrustum(entity.getEntityBoundingBox(), 0.125))
            && entity instanceof EntityItem) {
            EntityItem entityItem = (EntityItem) entity;
            ItemStack itemStack = entityItem.getEntityItem();
            if (itemStack != null && itemStack.stackSize > 0) {
               int itemId = Item.getIdFromItem(itemStack.getItem());
               if ((Boolean) this.emeraldsProp.getValue() && isEmeraldItem(itemId)
                  || (Boolean) this.diamondsProp.getValue() && isDiamondItem(itemId)
                  || (Boolean) this.goldProp.getValue() && isGoldItem(itemId)
                  || (Boolean) this.ironProp.getValue() && isIronItem(itemId)) {

                  double itemX = Render3DUtil.interpolate(entityItem.posX, entityItem.lastTickPosX, (double) event.getPartialTicks());
                  double itemY = Render3DUtil.interpolate(entityItem.posY, entityItem.lastTickPosY, (double) event.getPartialTicks());
                  double itemZ = Render3DUtil.interpolate(entityItem.posZ, entityItem.lastTickPosZ, (double) event.getPartialTicks());
                  ItemCluster cluster = new ItemCluster(itemId, itemX, itemY, itemZ);

                  if (renderItemList.size() < 24 && mc.getRenderViewEntity().getDistanceToEntity(entityItem) < 48.0F) {
                     renderItemList.add(entityItem);
                     glowClusterSet.add(cluster);
                  }

                  Integer existingCount = clusterCountMap.get(cluster);
                  clusterCountMap.put(cluster, itemStack.stackSize + (existingCount == null ? 0 : existingCount));
               }
            }
         }
      }

      boolean glowRendered = (Boolean) this.glowProp.getValue() && this.renderGlowShader(renderItemList, event.getPartialTicks());
      List<Entry<ItemCluster, Integer>> sortedEntries = clusterCountMap.entrySet().stream().sorted((e1, e2) -> {
         int p1 = getItemPriority(e1.getKey().itemId);
         int p2 = getItemPriority(e2.getKey().itemId);
         return Integer.compare(p1, p2);
      }).collect(Collectors.toList());

      for (Entry<ItemCluster, Integer> entry : sortedEntries) {
         ItemCluster cluster = entry.getKey();
         int itemId = cluster.itemId;
         Color clusterColor;
         if (isEmeraldItem(itemId)) {
            clusterColor = new Color(ChatColor.GREEN.rgb);
         } else if (isDiamondItem(itemId)) {
            clusterColor = new Color(ChatColor.AQUA.rgb);
         } else if (isGoldItem(itemId)) {
            clusterColor = new Color(ChatColor.YELLOW.rgb);
         } else if (isIronItem(itemId)) {
            clusterColor = new Color(ChatColor.WHITE.rgb);
         } else {
            clusterColor = new Color(ChatColor.GRAY.rgb);
         }

         double renderX = cluster.x - ReflectionUtil.getRenderPosX(mc.getRenderManager());
         double renderY = cluster.y - ReflectionUtil.getRenderPosY(mc.getRenderManager());
         double renderZ = cluster.z - ReflectionUtil.getRenderPosZ(mc.getRenderManager());
         double distance = mc.getRenderViewEntity().getDistance(cluster.x, cluster.y, cluster.z);
         double scaleFactor = 0.5 + 0.375 * ((Math.max(6.0, (Boolean) this.autoScaleProp.getValue() ? distance : 6.0) - 6.0) / 28.0);
         AxisAlignedBB bb = new AxisAlignedBB(
            renderX - scaleFactor * 0.5, renderY, renderZ - scaleFactor * 0.5,
            renderX + scaleFactor * 0.5, renderY + scaleFactor, renderZ + scaleFactor * 0.5
         );

         boolean isGlowing = glowRendered && glowClusterSet.contains(cluster);
         if (!isGlowing && (Integer) this.opacityProp.getValue() > 0 || (Boolean) this.outlineProp.getValue()) {
            Render3DUtil.setupRender3D();
            if (!isGlowing && (Integer) this.opacityProp.getValue() > 0) {
               Render3DUtil.drawFilledBox(bb, clusterColor.getRed(), clusterColor.getGreen(), clusterColor.getBlue());
               GlStateManager.resetColor();
            }

            if ((Boolean) this.outlineProp.getValue()) {
               Render3DUtil.drawOutlinedBox(bb, clusterColor.getRed(), clusterColor.getGreen(), clusterColor.getBlue(), 255, 1.5F);
               GlStateManager.resetColor();
            }

            Render3DUtil.endRender3D();
         }

         if ((Boolean) this.itemCountProp.getValue() && entry.getValue() > 1) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(renderX, renderY + scaleFactor * 0.5, renderZ);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            float viewPitch = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
            GlStateManager.rotate(mc.getRenderManager().playerViewX, viewPitch, 0.0F, 0.0F);

            double autoScaleDist = Math.max(6.0, (Boolean) this.autoScaleProp.getValue() ? distance : 6.0) - 6.0;
            double tagScale = -0.04375 - 0.0328125 * (autoScaleDist / 28.0);
            GlStateManager.scale(tagScale, tagScale, 1.0);
            GlStateManager.disableDepth();
            GL11.glDisable(2896);
            GlStateManager.disableFog();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            String countText = String.valueOf(entry.getValue());
            float textWidth = RenderUtil.getStringWidth(countText);
            float fontHeight = RenderUtil.getFontHeight();
            Render3DUtil.setupRender3D();
            WorldOverlayStyle.drawPill(-textWidth / 2.0F - 4.0F, -fontHeight / 2.0F - 3.0F, textWidth / 2.0F + 4.0F, fontHeight / 2.0F + 3.0F, 4.0F, RenderUtil.getThemeColor(), 220);
            Render3DUtil.setupRender2D();
            RenderUtil.drawString(countText, -textWidth / 2.0F, -fontHeight / 2.0F, RenderUtil.getLightTextColor(), false);
            GlStateManager.enableFog();
            GL11.glEnable(2903);
            GL11.glEnable(2896);
            GlStateManager.enableDepth();
            GlStateManager.resetColor();
            GlStateManager.popMatrix();
         }
      }
   }

   public ItemESP() {
      super("ItemESP", false, ModuleCategory.VISUAL);
   }

   public static class ItemCluster {
      public final int hash;
      public final int itemId;
      public final double x;
      public final double y;
      public final double z;

      public ItemCluster(int itemId, double x, double y, double z) {
         this.itemId = itemId;
         this.x = x;
         this.y = y;
         this.z = z;
         this.hash = Objects.hash(itemId, (int) x, (int) y, (int) z);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj != null && obj instanceof ItemCluster) {
            ItemCluster other = (ItemCluster) obj;
            return this.itemId == other.itemId && (int) this.x == (int) other.x && (int) this.y == (int) other.y && (int) this.z == (int) other.z;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.hash;
      }
   }
}
