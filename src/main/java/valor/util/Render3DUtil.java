package valor.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public final class Render3DUtil {
   private static Minecraft mc = Minecraft.getMinecraft();
   private static Frustum frustum = new Frustum();
   private static Map<Integer, Render3DUtil.EnchantmentInfo> enchantmentMap = new EnchantmentMap();
   private static boolean lightingWasEnabled = false;

   public static double interpolate(double current, double prev, double partialTicks) {
      return prev + (current - prev) * partialTicks;
   }

   public static void setupRender2D() {
      GL11.glDisable(2896);
      GlStateManager.disableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.disableDepth();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void drawFilledBox(AxisAlignedBB bb, int red, int green, int blue, int alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer renderer = tessellator.getWorldRenderer();
      renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      renderer.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
   }

   public static void drawOutlinedBox(AxisAlignedBB bb, int red, int green, int blue, int alpha, float lineWidth) {
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      RenderGlobal.drawOutlinedBoundingBox(bb, red, green, blue, alpha);
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
   }

   public static void drawFilledBox(AxisAlignedBB bb, int red, int green, int blue) {
      drawFilledBox(bb, red, green, blue, 63);
   }

   public static void drawCircleAroundEntity(Entity entity, double radius, int points, int color) {
      double x = interpolate(entity.posX, entity.lastTickPosX, (double)ReflectionUtil.getTimer(mc).renderPartialTicks)
         - ReflectionUtil.getRenderPosX(mc.getRenderManager());
      double y = interpolate(entity.posY, entity.lastTickPosY, (double)ReflectionUtil.getTimer(mc).renderPartialTicks)
         - ReflectionUtil.getRenderPosY(mc.getRenderManager());
      double z = interpolate(entity.posZ, entity.lastTickPosZ, (double)ReflectionUtil.getTimer(mc).renderPartialTicks)
         - ReflectionUtil.getRenderPosZ(mc.getRenderManager());
      color(color);
      GL11.glLineWidth(3.0F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(2);

      for (int i = 0; i <= points; i++) {
         double angle = (double)i * ((Math.PI * 2) / (double)points);
         GL11.glVertex3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
      }

      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
   }

   public static boolean isBoundingBoxInFrustum(AxisAlignedBB bb, double expand) {
      frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      return frustum.isBoundingBoxInFrustum(bb.expand(expand, expand, expand));
   }

   public static void drawBlockESP(BlockPos pos, double height, int red, int green, int blue) {
      drawFilledBox(
         new AxisAlignedBB(
               (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)pos.getX() + 1.0, (double)pos.getY() + height, (double)pos.getZ() + 1.0
            )
            .offset(
               -ReflectionUtil.getRenderPosX(mc.getRenderManager()),
               -ReflectionUtil.getRenderPosY(mc.getRenderManager()),
               -ReflectionUtil.getRenderPosZ(mc.getRenderManager())
            ),
         red,
         green,
         blue
      );
   }

   public static void drawEntityESP(Entity entity, int red, int green, int blue) {
      double x = interpolate(entity.posX, entity.lastTickPosX, (double)ReflectionUtil.getTimer(mc).renderPartialTicks);
      double y = interpolate(entity.posY, entity.lastTickPosY, (double)ReflectionUtil.getTimer(mc).renderPartialTicks);
      double z = interpolate(entity.posZ, entity.lastTickPosZ, (double)ReflectionUtil.getTimer(mc).renderPartialTicks);
      drawFilledBox(
         entity.getEntityBoundingBox()
            .expand(0.1F, 0.1F, 0.1F)
            .offset(x - entity.posX, y - entity.posY, z - entity.posZ)
            .offset(
               -ReflectionUtil.getRenderPosX(mc.getRenderManager()),
               -ReflectionUtil.getRenderPosY(mc.getRenderManager()),
               -ReflectionUtil.getRenderPosZ(mc.getRenderManager())
            ),
         red,
         green,
         blue
      );
   }

   public static void drawBox(AxisAlignedBB bb, Color color, int alpha, float lineWidth, boolean outline) {
      if (alpha > 0) {
         drawFilledBox(bb, color.getRed(), color.getGreen(), color.getBlue(), alpha);
      }

      if (outline) {
         drawOutlinedBox(bb, color.getRed(), color.getGreen(), color.getBlue(), 22, lineWidth + 7.0F);
         drawOutlinedBox(bb, color.getRed(), color.getGreen(), color.getBlue(), 44, lineWidth + 3.5F);
         drawOutlinedBox(bb, color.getRed(), color.getGreen(), color.getBlue(), 92, lineWidth + 1.5F);
         drawOutlinedBox(bb, color.getRed(), color.getGreen(), color.getBlue(), 245, lineWidth);
      }
   }

   public static void drawRectOutline(float top, float right, float bottom, int color) {
      drawRect(0.0F, 0.0F, right, 27.0F, 0);
      if (color != 0) {
         color(color);
         GL11.glLineWidth(1.5F);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glBegin(1);
         GL11.glVertex2f(2.0F, top);
         GL11.glVertex2f(2.0F, bottom);
         GL11.glVertex2f(right, bottom);
         GL11.glVertex2f(right, top);
         GL11.glVertex2f(2.0F, top);
         GL11.glVertex2f(right, top);
         GL11.glVertex2f(2.0F, bottom);
         GL11.glVertex2f(right, bottom);
         GL11.glEnd();
         GL11.glDisable(2848);
         GL11.glLineWidth(2.0F);
         GlStateManager.resetColor();
      }
   }

   public static void drawGradientRect(int left, int top, float right, int bottom, int startColor, int endColor) {
      float startAlpha = (float)(startColor >>> 24) / 255.0F;
      float startRed = (float)(startColor >> 16 & 0xFF) / 255.0F;
      float startGreen = (float)(startColor >> 8 & 0xFF) / 255.0F;
      float startBlue = (float)(startColor & 0xFF) / 255.0F;
      float endAlpha = (float)(endColor >>> 24) / 255.0F;
      float endRed = (float)(endColor >> 16 & 0xFF) / 255.0F;
      float endGreen = (float)(endColor >> 8 & 0xFF) / 255.0F;
      float endBlue = (float)(endColor & 0xFF) / 255.0F;
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glDisable(3008);
      GL11.glBlendFunc(770, 771);
      GL11.glShadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer renderer = tessellator.getWorldRenderer();
      renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      renderer.pos((double)right, (double)top, 0.0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
      renderer.pos((double)left, (double)top, 0.0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
      renderer.pos((double)left, (double)bottom, 0.0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
      renderer.pos((double)right, (double)bottom, 0.0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
      tessellator.draw();
      GL11.glShadeModel(7424);
      GL11.glDisable(3042);
      GL11.glEnable(3008);
      GL11.glEnable(3553);
   }

   public static void drawRect(float left, float top, float right, float bottom, int color) {
      if (color != 0) {
         setupRender3D();
         color(color);
         GL11.glBegin(7);
         GL11.glVertex2f(left, top);
         GL11.glVertex2f(left, bottom);
         GL11.glVertex2f(right, bottom);
         GL11.glVertex2f(right, top);
         GL11.glEnd();
         GlStateManager.resetColor();
         setupRender2D();
      }
   }

   public static void renderItemStack(ItemStack stack, int x, int y) {
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.clear(256);
      RenderHelper.enableGUIStandardItemLighting();
      GL11.glDisable(2896);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F, 1.0F, -0.01F);
      mc.getRenderItem().zLevel = -150.0F;
      mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
      mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
      mc.getRenderItem().zLevel = 0.0F;
      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.enableAlpha();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      GlStateManager.disableDepth();
      float posX = (float)x;
      float posY = (float)y;
      NBTTagList enchantList;
      if ((enchantList = stack.getItem() == Items.enchanted_book ? Items.enchanted_book.getEnchantments(stack) : stack.getEnchantmentTagList()) != null) {
         for (int i = 0; i < enchantList.tagCount(); i++) {
            Render3DUtil.EnchantmentInfo info;
            if ((info = enchantmentMap.get(enchantList.getCompoundTagAt(i).getInteger("id"))) != null) {
               short level = enchantList.getCompoundTagAt(i).getShort("lvl");
               int maxLevel = info.maxLevel;
               ChatColor levelColor;
               if (level > maxLevel) {
                  levelColor = ChatColor.LIGHT_PURPLE;
               } else if (level == maxLevel) {
                  levelColor = ChatColor.RED;
               } else {
                  switch (level) {
                     case 1:
                        levelColor = ChatColor.AQUA;
                        break;
                     case 2:
                        levelColor = ChatColor.GREEN;
                        break;
                     case 3:
                        levelColor = ChatColor.YELLOW;
                        break;
                     case 4:
                        levelColor = ChatColor.GOLD;
                        break;
                     default:
                        levelColor = ChatColor.GRAY;
                  }
               }

               String text = ChatColor.format(String.format("&r%s%s%d&r", info.shortName, levelColor, Integer.valueOf(level)));
               float renderX = posX * (1.0F / 0.5F);
               float renderY = (posY + (float)i * 4.0F) * (1.0F / 0.5F);
               String plainText = text.replaceAll("(?i)[\\da-f]", "");
               mc.fontRendererObj.drawString(plainText, renderX + 1.0F, renderY, 0, false);
               mc.fontRendererObj.drawString(plainText, renderX - 1.0F, renderY, 0, false);
               mc.fontRendererObj.drawString(plainText, renderX, renderY + 1.0F, 0, false);
               mc.fontRendererObj.drawString(plainText, renderX, renderY - 1.0F, 0, false);
               mc.fontRendererObj.drawString(text, renderX, renderY, -1, false);
            }
         }
      }

      GlStateManager.enableDepth();
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      GlStateManager.popMatrix();
   }

   public static void renderPotionIcon(PotionEffect effect, int x, int y) {
      int iconIndex = Potion.potionTypes[effect.getPotionID()].getStatusIconIndex();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.clear(256);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F, 1.0F, -0.01F);
      mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
      Gui.drawModalRectWithCustomSizedTexture(x, y, (float)(iconIndex % 8 * 18), (float)(198 + iconIndex / 8 * 18), 18, 18, 256.0F, 256.0F);
      GlStateManager.popMatrix();
      GlStateManager.enableAlpha();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }

   public static void drawFilledCircle(double x, double y, double radius, int color) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      color(color);
      GL11.glBegin(6);
      GL11.glVertex2d(x, y);

      for (int i = 0; i <= 12; i++) {
         double angle = (double)i * ((Math.PI * 2) / 12.0);
         GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
      }

      GL11.glEnd();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.resetColor();
   }

   public static void drawTracerArrow(float angle, int color) {
      float angle1 = angle + (float)Math.toRadians(45.0);
      float angle2 = angle - (float)Math.toRadians(45.0);
      color(color);
      GL11.glLineWidth(1.5F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(1);
      GL11.glVertex2f(0.0F, 0.0F);
      GL11.glVertex2f(0.0F + 7.5F * (float)Math.cos((double)angle1), 0.0F + 7.5F * (float)Math.sin((double)angle1));
      GL11.glVertex2f(0.0F, 0.0F);
      GL11.glVertex2f(0.0F + 7.5F * (float)Math.cos((double)angle2), 0.0F + 7.5F * (float)Math.sin((double)angle2));
      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
   }

   public static void drawTracerLine(Vec3 eyes, double targetX, double targetY, double targetZ, float red, float green, float blue, float alpha) {
      GlStateManager.pushMatrix();
      GlStateManager.color(red, green, blue, alpha);
      boolean viewBobbing = mc.gameSettings.viewBobbing;
      mc.gameSettings.viewBobbing = false;
      ReflectionUtil.setupCameraTransform(mc.entityRenderer, ReflectionUtil.getTimer(mc).renderPartialTicks, 2);
      mc.gameSettings.viewBobbing = viewBobbing;
      GL11.glLineWidth(1.5F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(1);
      GL11.glVertex3d(eyes.xCoord, eyes.yCoord, eyes.zCoord);
      GL11.glVertex3d(
         targetX - ReflectionUtil.getRenderPosX(mc.getRenderManager()),
         targetY - ReflectionUtil.getRenderPosY(mc.getRenderManager()),
         targetZ - ReflectionUtil.getRenderPosZ(mc.getRenderManager())
      );
      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
      GlStateManager.popMatrix();
   }

   public static void drawVerticalLine(float y1, float y2, int color) {
      color(color);
      GL11.glLineWidth(1.5F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(1);
      GL11.glVertex2f(-0.25F, y1);
      GL11.glVertex2f(0.25F, y2);
      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
   }

   public static void color(int color) {
      float alpha = (float)(color >>> 24) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      GlStateManager.color(red, green, blue, alpha);
   }

   public static void endRender3D() {
      if (lightingWasEnabled) {
         GL11.glEnable(2896);
      } else {
         GL11.glDisable(2896);
      }

      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.enableCull();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void setupRender3D() {
      lightingWasEnabled = GL11.glIsEnabled(2896);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableTexture2D();
      GlStateManager.disableCull();
      GlStateManager.disableAlpha();
      GlStateManager.disableDepth();
      GL11.glDisable(2896);
   }

   public static float lerpFloat(float current, float prev, float partialTicks) {
      return prev + (current - prev) * partialTicks;
   }

   static {
      GLAllocation.createDirectIntBuffer(16);
      GLAllocation.createDirectFloatBuffer(16);
      GLAllocation.createDirectFloatBuffer(16);
      GLAllocation.createDirectFloatBuffer(4);
   }

   public static final class EnchantmentInfo {
      public final String shortName;
      public final int maxLevel;

      public EnchantmentInfo(String shortName, int maxLevel) {
         this.shortName = shortName;
         this.maxLevel = maxLevel;
      }
   }

   static final class EnchantmentMap extends HashMap<Integer, Render3DUtil.EnchantmentInfo> {
      EnchantmentMap() {
         this.put(Integer.valueOf(0), new Render3DUtil.EnchantmentInfo("Pr", 4));
         this.put(Integer.valueOf(1), new Render3DUtil.EnchantmentInfo("Fp", 4));
         this.put(Integer.valueOf(2), new Render3DUtil.EnchantmentInfo("Ff", 4));
         this.put(Integer.valueOf(3), new Render3DUtil.EnchantmentInfo("Bp", 4));
         this.put(Integer.valueOf(4), new Render3DUtil.EnchantmentInfo("Pp", 4));
         this.put(Integer.valueOf(5), new Render3DUtil.EnchantmentInfo("Re", 3));
         this.put(Integer.valueOf(6), new Render3DUtil.EnchantmentInfo("Aq", 1));
         this.put(Integer.valueOf(7), new Render3DUtil.EnchantmentInfo("Th", 3));
         this.put(Integer.valueOf(8), new Render3DUtil.EnchantmentInfo("Ds", 3));
         this.put(Integer.valueOf(16), new Render3DUtil.EnchantmentInfo("Sh", 5));
         this.put(Integer.valueOf(17), new Render3DUtil.EnchantmentInfo("Sm", 5));
         this.put(Integer.valueOf(18), new Render3DUtil.EnchantmentInfo("BoA", 5));
         this.put(Integer.valueOf(19), new Render3DUtil.EnchantmentInfo("Kb", 2));
         this.put(Integer.valueOf(20), new Render3DUtil.EnchantmentInfo("Fa", 2));
         this.put(Integer.valueOf(21), new Render3DUtil.EnchantmentInfo("Lo", 3));
         this.put(Integer.valueOf(32), new Render3DUtil.EnchantmentInfo("Ef", 5));
         this.put(Integer.valueOf(33), new Render3DUtil.EnchantmentInfo("St", 1));
         this.put(Integer.valueOf(34), new Render3DUtil.EnchantmentInfo("Ub", 3));
         this.put(Integer.valueOf(35), new Render3DUtil.EnchantmentInfo("Fo", 3));
         this.put(Integer.valueOf(48), new Render3DUtil.EnchantmentInfo("Po", 5));
         this.put(Integer.valueOf(49), new Render3DUtil.EnchantmentInfo("Pu", 2));
         this.put(Integer.valueOf(50), new Render3DUtil.EnchantmentInfo("Fl", 1));
         this.put(Integer.valueOf(51), new Render3DUtil.EnchantmentInfo("Inf", 1));
         this.put(Integer.valueOf(61), new Render3DUtil.EnchantmentInfo("LoS", 3));
         this.put(Integer.valueOf(62), new Render3DUtil.EnchantmentInfo("Lu", 3));
      }
   }
}
