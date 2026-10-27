package valor.module.render;
import java.awt.Color;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.util.BotCheckUtil;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.render.OutlineShader;
public class ESP extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static volatile boolean isRenderingOutline;
   private Framebuffer outlineFramebuffer;
   private OutlineShader outlineShader;
   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"2D", "3D", "OUTLINE"});
   public final ModeProperty renderStyleProp = new ModeProperty("render-style", 0, new String[]{"POST", "VANILLA"});
   public final BooleanProperty hudColorProp = new BooleanProperty("hud-color", Boolean.TRUE);
   public final ColorProperty colorProp = new ColorProperty("color", new Color(255, 0, 0).getRGB(), () -> !(Boolean)this.hudColorProp.getValue());
   public final FloatProperty lineWidthProp = new FloatProperty("line-width", 1.5F, 0.5F, 5.0F);
   public final BooleanProperty healthBarProp = new BooleanProperty("health-bar", Boolean.TRUE);
   public final BooleanProperty invisibleProp = new BooleanProperty("invisible", Boolean.TRUE);
   public final BooleanProperty filledProp = new BooleanProperty("filled", Boolean.FALSE, () -> (Integer)this.modeProp.getValue() == 1);
   public final BooleanProperty smoothHealthProp = new BooleanProperty("smooth-health", Boolean.TRUE);
   public final BooleanProperty playersProp = new BooleanProperty("players", Boolean.TRUE);
   public final BooleanProperty friendsProp = new BooleanProperty("friends", Boolean.TRUE);
   public final BooleanProperty enemiesProp = new BooleanProperty("enemies", Boolean.TRUE);
   public final BooleanProperty selfProp = new BooleanProperty("self", Boolean.FALSE);
   public final BooleanProperty botsProp = new BooleanProperty("bots", Boolean.FALSE);
   public final BooleanProperty teamsProp = new BooleanProperty("teams", Boolean.TRUE);
   public static boolean isRenderingOutline() {
      return isRenderingOutline;
   }
   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (!this.isEnabled()) {
         return;
      }
      if (mc.theWorld == null || mc.getRenderManager() == null) {
         return;
      }
      if (this.isOutlineMode()) {
         float partialTicks = event.getPartialTicks();
         if (this.outlineFramebuffer == null
            || this.outlineFramebuffer.framebufferWidth != mc.displayWidth
            || this.outlineFramebuffer.framebufferHeight != mc.displayHeight) {
            if (this.outlineFramebuffer != null) {
               this.outlineFramebuffer.deleteFramebuffer();
            }
            this.outlineFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
            this.outlineFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
         }
         if (this.outlineShader == null) {
            this.outlineShader = new OutlineShader();
         }
         if (this.outlineFramebuffer != null && this.outlineShader != null) {
            for (Object obj : mc.theWorld.playerEntities) {
               EntityPlayer player = (EntityPlayer) obj;
               if ((player != mc.thePlayer || ((Boolean) this.selfProp.getValue() && mc.gameSettings.thirdPersonView != 0))
                  && !player.isDead
                  && player.deathTime <= 0
                  && (!player.isInvisible() || (Boolean) this.invisibleProp.getValue())
                  && this.isValidTarget(player)) {
                  Color entityColor = this.getEntityColor(player);
                  this.outlineFramebuffer.framebufferClear();
                  this.outlineFramebuffer.bindFramebuffer(true);
                  GL11.glPushAttrib(1048575);
                  boolean prevHideGui = mc.gameSettings.hideGUI;
                  try {
                     isRenderingOutline = true;
                     mc.gameSettings.hideGUI = true;
                     RenderManager renderManager = mc.getRenderManager();
                     Render<EntityPlayer> render = renderManager.getEntityRenderObject(player);
                     if (render != null) {
                        double renderX = interpolate(player.lastTickPosX, player.posX, partialTicks) - ReflectionUtil.getRenderPosX(renderManager);
                        double renderY = interpolate(player.lastTickPosY, player.posY, partialTicks) - ReflectionUtil.getRenderPosY(renderManager);
                        double renderZ = interpolate(player.lastTickPosZ, player.posZ, partialTicks) - ReflectionUtil.getRenderPosZ(renderManager);
                        float renderYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
                        boolean wasInvisible = player.isInvisible();
                        if (wasInvisible) {
                           player.setInvisible(false);
                        }
                        try {
                           render.doRender(player, renderX, renderY, renderZ, renderYaw, partialTicks);
                        } finally {
                           if (wasInvisible) {
                              player.setInvisible(true);
                           }
                        }
                     }
                  } finally {
                     mc.gameSettings.hideGUI = prevHideGui;
                     isRenderingOutline = false;
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
                     GL11.glBlendFunc(770, 771);
                     GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                     this.outlineShader.red = (float) entityColor.getRed() / 255.0F;
                     this.outlineShader.green = (float) entityColor.getGreen() / 255.0F;
                     this.outlineShader.blue = (float) entityColor.getBlue() / 255.0F;
                     this.outlineShader.alpha = 1.0F;
                     this.outlineShader.radius = Math.max(1.0F, (Float) this.lineWidthProp.getValue());
                     this.outlineShader.useShader();
                     GL11.glBindTexture(3553, this.outlineFramebuffer.framebufferTexture);
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
               }
            }
         }
      } else {
         this.setupGlState(true);
         try {
            for (Object obj : mc.theWorld.playerEntities) {
               EntityPlayer player = (EntityPlayer) obj;
               if ((player != mc.thePlayer || ((Boolean) this.selfProp.getValue() && mc.gameSettings.thirdPersonView != 0))
                  && !player.isDead
                  && (!player.isInvisible() || (Boolean) this.invisibleProp.getValue())
                  && this.isValidTarget(player)) {
                  Color entityColor = this.getEntityColor(player);
                  float red = (float) entityColor.getRed() / 255.0F;
                  float green = (float) entityColor.getGreen() / 255.0F;
                  float blue = (float) entityColor.getBlue() / 255.0F;
                  double posX = interpolate(player.lastTickPosX, player.posX, event.getPartialTicks()) - mc.getRenderManager().viewerPosX;
                  double posY = interpolate(player.lastTickPosY, player.posY, event.getPartialTicks()) - mc.getRenderManager().viewerPosY;
                  double posZ = interpolate(player.lastTickPosZ, player.posZ, event.getPartialTicks()) - mc.getRenderManager().viewerPosZ;
                  if ((Integer) this.modeProp.getValue() == 0) {
                     GlStateManager.pushMatrix();
                     GlStateManager.translate(posX, posY, posZ);
                     GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                     double halfWidth = (double) player.width / 2.0 + 0.15;
                     double height = (double) player.height + 0.15;
                     Tessellator tessellator = Tessellator.getInstance();
                     WorldRenderer wr = tessellator.getWorldRenderer();
                     if ((Integer) this.renderStyleProp.getValue() == 0) {
                        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        wr.pos(-halfWidth, height, 0.0).color(red, green, blue, 0.1F).endVertex();
                        wr.pos(halfWidth, height, 0.0).color(red, green, blue, 0.1F).endVertex();
                        wr.pos(halfWidth, 0.0, 0.0).color(red, green, blue, 0.1F).endVertex();
                        wr.pos(-halfWidth, 0.0, 0.0).color(red, green, blue, 0.1F).endVertex();
                        tessellator.draw();
                        draw2DBoxOutline(wr, tessellator, halfWidth, height, red, green, blue, 0.08F, (Float) this.lineWidthProp.getValue() + 7.0F);
                        draw2DBoxOutline(wr, tessellator, halfWidth, height, red, green, blue, 0.18F, (Float) this.lineWidthProp.getValue() + 3.5F);
                        draw2DBoxOutline(wr, tessellator, halfWidth, height, red, green, blue, 0.42F, (Float) this.lineWidthProp.getValue() + 1.5F);
                     } else {
                        draw2DBoxOutline(wr, tessellator, halfWidth, height, 0.0F, 0.0F, 0.0F, 1.0F, (Float) this.lineWidthProp.getValue() + 2.0F);
                     }
                     draw2DBoxOutline(wr, tessellator, halfWidth, height, red, green, blue, 1.0F, (Float) this.lineWidthProp.getValue());
                     if ((Boolean) this.healthBarProp.getValue()) {
                        float healthPercent = Math.max(0.0F, Math.min(1.0F, player.getHealth() / Math.max(1.0F, player.getMaxHealth())));
                        Color healthColor = Color.getHSBColor(healthPercent * 0.33F, 0.78F, 1.0F);
                        double barLeft = -halfWidth - 0.09;
                        double barRight = -halfWidth - 0.045;
                        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        wr.pos(barLeft - 0.012, height + 0.012, 0.0).color(0.01F, 0.02F, 0.03F, 0.92F).endVertex();
                        wr.pos(barRight + 0.012, height + 0.012, 0.0).color(0.01F, 0.02F, 0.03F, 0.92F).endVertex();
                        wr.pos(barRight + 0.012, -0.012, 0.0).color(0.01F, 0.02F, 0.03F, 0.92F).endVertex();
                        wr.pos(barLeft - 0.012, -0.012, 0.0).color(0.01F, 0.02F, 0.03F, 0.92F).endVertex();
                        tessellator.draw();
                        double healthHeight = height * (double) healthPercent;
                        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        wr.pos(barLeft, healthHeight, 0.0).color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 235).endVertex();
                        wr.pos(barRight, healthHeight, 0.0).color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 235).endVertex();
                        wr.pos(barRight, 0.0, 0.0).color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 235).endVertex();
                        wr.pos(barLeft, 0.0, 0.0).color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 235).endVertex();
                        tessellator.draw();
                     }
                     GlStateManager.popMatrix();
                  } else {
                     float partialTicks = event.getPartialTicks();
                     GlStateManager.pushMatrix();
                     GlStateManager.translate(posX, posY, posZ);
                     float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
                     GlStateManager.rotate(-yawHead, 0.0F, 1.0F, 0.0F);
                     double halfWidth = (double) player.width / 2.0;
                     double height = (double) player.height;
                     AxisAlignedBB bb = new AxisAlignedBB(-halfWidth, 0.0, -halfWidth, halfWidth, height, halfWidth);
                     if ((Integer) this.renderStyleProp.getValue() == 0) {
                        Render3DUtil.drawBox(bb, new Color(red, green, blue), (Boolean) this.filledProp.getValue() ? 38 : 0, (Float) this.lineWidthProp.getValue(), true);
                     } else if ((Boolean) this.filledProp.getValue()) {
                        GlStateManager.disableCull();
                        Tessellator tessellator = Tessellator.getInstance();
                        WorldRenderer wr = tessellator.getWorldRenderer();
                        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        wr.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.15F).endVertex();
                        wr.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, 0.15F).endVertex();
                        tessellator.draw();
                        GlStateManager.enableCull();
                     }
                     if ((Integer) this.renderStyleProp.getValue() != 0) {
                        GL11.glLineWidth((Float) this.lineWidthProp.getValue() + 2.0F);
                        drawOutlinedBoundingBox(bb, 0.01F, 0.02F, 0.03F, 0.9F);
                        GL11.glLineWidth((Float) this.lineWidthProp.getValue());
                        drawOutlinedBoundingBox(bb, red, green, blue, 1.0F);
                     }
                     GlStateManager.popMatrix();
                  }
               }
            }
         } finally {
            this.setupGlState(false);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
         }
      }
   }
   public void setupGlState(boolean enable) {
      if (enable) {
         GlStateManager.pushMatrix();
         GlStateManager.disableTexture2D();
         GlStateManager.disableDepth();
         GlStateManager.disableAlpha();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GL11.glDisable(2896);
         GL11.glEnable(2848);
         GL11.glLineWidth((Float) this.lineWidthProp.getValue());
      } else {
         GL11.glEnable(2896);
         GL11.glDisable(2848);
         GlStateManager.enableTexture2D();
         GlStateManager.enableDepth();
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }
   }
   public final boolean isOutlineMode() {
      return this.isEnabled() && (Integer) this.modeProp.getValue() == 2;
   }
   public static double interpolate(double prev, double current, float partialTicks) {
      return prev + (current - prev) * (double) partialTicks;
   }
   public static void drawOutlinedBoundingBox(AxisAlignedBB bb, float r, float g, float b, float a) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer wr = tessellator.getWorldRenderer();
      wr.begin(1, DefaultVertexFormats.POSITION_COLOR);
      wr.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
      wr.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
      tessellator.draw();
   }
   public Color getEntityColor(EntityPlayer player) {
      if (BotCheckUtil.isFriend(player)) {
         return new Color(98, 230, 164);
      } else if (BotCheckUtil.isTarget(player)) {
         return new Color(255, 105, 122);
      } else if ((Boolean) this.teamsProp.getValue() && BotCheckUtil.isTeammate(player)) {
         return new Color(95, 175, 255);
      } else {
         HudThemeManager theme;
         return (Boolean) this.hudColorProp.getValue() && (theme = HudThemeManager.getInstance()) != null
            ? theme.getColor(System.currentTimeMillis(), 0L)
            : new Color((Integer) this.colorProp.getValue(), true);
      }
   }
   public static void draw2DBoxOutline(WorldRenderer wr, Tessellator tess, double halfWidth, double height, float r, float g, float b, float a, float lineWidth) {
      GL11.glLineWidth(lineWidth);
      wr.begin(2, DefaultVertexFormats.POSITION_COLOR);
      wr.pos(-halfWidth, height, 0.0).color(r, g, b, a).endVertex();
      wr.pos(halfWidth, height, 0.0).color(r, g, b, a).endVertex();
      wr.pos(halfWidth, 0.0, 0.0).color(r, g, b, a).endVertex();
      wr.pos(-halfWidth, 0.0, 0.0).color(r, g, b, a).endVertex();
      tess.draw();
   }
   @Override
   public void onDisabled() {
      isRenderingOutline = false;
   }
   public boolean isValidTarget(EntityPlayer player) {
      if (player == mc.thePlayer) {
         return (Boolean) this.selfProp.getValue();
      } else if (player == mc.getRenderViewEntity() && player != mc.thePlayer) {
         return false;
      } else if (player.deathTime > 0) {
         return false;
      } else if (mc.getRenderViewEntity().getDistanceToEntity(player) > 512.0F) {
         return false;
      } else if (BotCheckUtil.isBot(player)) {
         return (Boolean) this.botsProp.getValue();
      } else if (BotCheckUtil.isFriend(player)) {
         return (Boolean) this.friendsProp.getValue();
      } else if (BotCheckUtil.isTarget(player)) {
         return (Boolean) this.enemiesProp.getValue();
      } else {
         return (Boolean) this.teamsProp.getValue() && BotCheckUtil.isTeammate(player) ? false : (Boolean) this.playersProp.getValue();
      }
   }
   public ESP() {
      super("ESP", false, ModuleCategory.VISUAL);
   }
}
