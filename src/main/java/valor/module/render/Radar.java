package valor.module.render;

import java.awt.Color;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.events.Render2DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.ColorProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.BotCheckUtil;
import valor.util.ChatColor;
import valor.util.DraggableHudElement;
import valor.util.HudDragManager;
import valor.util.Render3DUtil;

public class Radar extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public float hudWidth = 110.0F;
   public float hudHeight = 110.0F;
   public boolean hasAdjustedPosition = false;

   public final ModeProperty colorProp = new ModeProperty("color", 2, new String[]{"DEFAULT", "TEAMS", "THEME"});
   public final FloatProperty posXProp = new FloatProperty("pos-x", 60.0F, 0.0F, 4000.0F);
   public final FloatProperty posYProp = new FloatProperty("pos-y", 60.0F, 0.0F, 4000.0F);
   public final IntProperty radarRadiusProp = new IntProperty("radar-radius", 55, 10, 200);
   public final FloatProperty dotRadiusProp = new FloatProperty("dot-radius", 1.5F, 0.1F, 5.0F);
   public final BooleanProperty playersProp = new BooleanProperty("players", Boolean.TRUE);
   public final BooleanProperty friendsProp = new BooleanProperty("friends", Boolean.TRUE);
   public final BooleanProperty enemiesProp = new BooleanProperty("enemies", Boolean.TRUE);
   public final BooleanProperty botsProp = new BooleanProperty("bots", Boolean.FALSE);
   public final BooleanProperty showPvpProp = new BooleanProperty("show-pvp", Boolean.FALSE);
   public final ColorProperty fillColorProp = new ColorProperty("fill-color", Color.GRAY.getRGB());
   public final ColorProperty outlineColorProp = new ColorProperty("outline-color", Color.DARK_GRAY.getRGB());
   public final ColorProperty crossColorProp = new ColorProperty("cross-color", Color.LIGHT_GRAY.getRGB());

   @Override
   public float getWidth() {
      return this.hudWidth;
   }

   @Override
   public float getX() {
      return (Float) this.posXProp.getValue();
   }

   @Override
   public void setX(float x) {
      this.posXProp.setValue(x);
   }

   @Override
   public void setY(float y) {
      this.posYProp.setValue(y);
   }

   @Override
   public float getY() {
      return (Float) this.posYProp.getValue();
   }

   @Override
   public float getHeight() {
      return this.hudHeight;
   }

   @Override
   public boolean isHudVisible() {
      return this.isEnabled();
   }

   @EventTarget(4)
   public void onRender(Render2DEvent event) {
      if (!this.isEnabled() || mc.thePlayer == null) {
         return;
      }

      ScaledResolution sr = new ScaledResolution(mc);
      if (!this.hasAdjustedPosition) {
         if ((Float) this.posXProp.getValue() == 60.0F && (Float) this.posYProp.getValue() == 60.0F) {
            this.posXProp.setValue((float) sr.getScaledWidth() - (float) ((Integer) this.radarRadiusProp.getValue()).intValue() * 2.0F - 10.0F);
            this.posYProp.setValue(10.0F);
         }
         this.hasAdjustedPosition = true;
      }

      HudThemeManager theme = HudThemeManager.getInstance();
      float themeScale = theme != null ? (Float) theme.scaleProp.getValue() : 1.0F;
      float radius = (float) ((Integer) this.radarRadiusProp.getValue()).intValue();
      float centerX = (Float) this.posXProp.getValue() + radius * themeScale;
      float centerY = (Float) this.posYProp.getValue() + radius * themeScale;
      this.hudWidth = radius * 2.0F * themeScale + 16.0F;
      this.hudHeight = radius * 2.0F * themeScale + 16.0F;

      GlStateManager.pushMatrix();
      GlStateManager.translate(centerX, centerY, 0.0F);
      GlStateManager.scale(themeScale, themeScale, 1.0F);
      Render3DUtil.setupRender3D();

      float playerYaw = (float) Math.toRadians((double) mc.thePlayer.rotationYaw);
      if (mc.gameSettings.thirdPersonView != 2) {
         playerYaw += (float) Math.toRadians(180.0);
      }

      double cosYaw = Math.cos((double) playerYaw);
      double sinYaw = Math.sin((double) playerYaw);
      Color fillColor = new Color((Integer) this.fillColorProp.getValue());
      int fillRgb = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 100).getRGB();
      int outlineRgb = (Integer) this.outlineColorProp.getValue();
      int crossRgb = (Integer) this.crossColorProp.getValue();

      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

      if (fillRgb >>> 24 != 0) {
         Render3DUtil.color(fillRgb);
         GL11.glBegin(6);
         GL11.glVertex2d(0.0, 0.0);
         for (int i = 0; i <= 64; i++) {
            double angle = (double) i * ((Math.PI * 2) / 64.0);
            GL11.glVertex2d(Math.cos(angle) * (double) radius, Math.sin(angle) * (double) radius);
         }
         GL11.glEnd();
      }

      if (outlineRgb >>> 24 != 0) {
         Render3DUtil.color(outlineRgb);
         GL11.glLineWidth(2.0F);
         GL11.glBegin(2);
         for (int i = 0; i <= 64; i++) {
            double angle = (double) i * ((Math.PI * 2) / 64.0);
            GL11.glVertex2d(Math.cos(angle) * (double) radius, Math.sin(angle) * (double) radius);
         }
         GL11.glEnd();
      }

      if (crossRgb >>> 24 != 0) {
         Render3DUtil.color(crossRgb);
         GL11.glLineWidth(1.5F);
         GL11.glBegin(1);
         double sinCross = Math.sin((double) playerYaw);
         double cosCross = Math.cos((double) playerYaw);
         double sinCrossPerp = Math.sin((double) playerYaw + (Math.PI / 2));
         double cosCrossPerp = Math.cos((double) playerYaw + (Math.PI / 2));
         GL11.glVertex2d(-sinCross * (double) radius, -cosCross * (double) radius);
         GL11.glVertex2d(sinCross * (double) radius, cosCross * (double) radius);
         GL11.glVertex2d(-sinCrossPerp * (double) radius, -cosCrossPerp * (double) radius);
         GL11.glVertex2d(sinCrossPerp * (double) radius, cosCrossPerp * (double) radius);
         GL11.glEnd();

         GlStateManager.disableDepth();
         GlStateManager.enableBlend();
         GlStateManager.enableTexture2D();
         GlStateManager.blendFunc(770, 771);

         int dirColor = theme != null ? theme.getColor(System.currentTimeMillis(), 0L).getRGB() : Color.WHITE.getRGB();
         boolean shadow = theme == null || (Boolean) theme.shadowProp.getValue();

         if (theme != null) {
            HudThemeManager.drawString("N", (float) (-sinCross * ((double) radius + 5.0)) - HudThemeManager.getStringWidth("N") / 2.0F, (float) (-cosCross * ((double) radius + 5.0)) - HudThemeManager.getFontHeight() / 2.0F, dirColor, shadow);
            HudThemeManager.drawString("E", (float) (sinCrossPerp * ((double) radius + 5.0)) - HudThemeManager.getStringWidth("E") / 2.0F, (float) (cosCrossPerp * ((double) radius + 5.0)) - HudThemeManager.getFontHeight() / 2.0F, dirColor, shadow);
            HudThemeManager.drawString("S", (float) (sinCross * ((double) radius + 5.0)) - HudThemeManager.getStringWidth("S") / 2.0F, (float) (cosCross * ((double) radius + 5.0)) - HudThemeManager.getFontHeight() / 2.0F, dirColor, shadow);
            HudThemeManager.drawString("W", (float) (-sinCrossPerp * ((double) radius + 5.0)) - HudThemeManager.getStringWidth("W") / 2.0F, (float) (-cosCrossPerp * ((double) radius + 5.0)) - HudThemeManager.getFontHeight() / 2.0F, dirColor, shadow);
         } else {
            mc.fontRendererObj.drawString("N", (float) (-sinCross * ((double) radius + 5.0)) - (float) mc.fontRendererObj.getStringWidth("N") / 2.0F, (float) (-cosCross * ((double) radius + 5.0)) - (float) mc.fontRendererObj.FONT_HEIGHT / 2.0F, dirColor, shadow);
         }

         GlStateManager.disableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.enableDepth();
      }

      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.resetColor();

      for (EntityPlayer player : BotCheckUtil.getSortedLoadedEntities()
         .stream()
         .filter(entity -> {
            if (entity instanceof EntityPlayer) {
               EntityPlayer p = (EntityPlayer) entity;
               if (p.deathTime <= 0
                  && !(mc.getRenderViewEntity().getDistanceToEntity(p) > 512.0F)
                  && p != mc.thePlayer
                  && p != mc.getRenderViewEntity()) {
                  if (BotCheckUtil.isBot(p)) {
                     return (Boolean) this.botsProp.getValue();
                  } else if (BotCheckUtil.isFriend(p)) {
                     return (Boolean) this.friendsProp.getValue();
                  } else {
                     return BotCheckUtil.isTarget(p) ? (Boolean) this.enemiesProp.getValue() : (Boolean) this.playersProp.getValue();
                  }
               }
            }
            return false;
         })
         .map(EntityPlayer.class::cast)
         .collect(Collectors.toList())) {

         double diffX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks() - mc.thePlayer.posX;
         double diffZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks() - mc.thePlayer.posZ;
         double rotX = diffX * cosYaw + diffZ * sinYaw;
         double rotZ = diffZ * cosYaw - diffX * sinYaw;
         double dist = Math.sqrt(rotX * rotX + rotZ * rotZ);
         double scale = dist < (double) radius ? 1.0 : (double) radius / dist;
         double blipX = rotX * scale;
         double blipZ = rotZ * scale;
         double dotRadius = (double) ((Float) this.dotRadiusProp.getValue()).floatValue();

         Color dotColor;
         if (BotCheckUtil.isFriend(player)) {
            Color friendColor = Valor.friendManager.color;
            dotColor = new Color(friendColor.getRed(), friendColor.getGreen(), friendColor.getBlue(), 255);
         } else if (BotCheckUtil.isTarget(player)) {
            Color targetColor = Valor.targetManager.color;
            dotColor = new Color(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(), 255);
         } else {
            switch ((Integer) this.colorProp.getValue()) {
               case 0:
                  dotColor = BotCheckUtil.getTeamColor(player, 1.0F);
                  break;
               case 1:
                  int teamColor = BotCheckUtil.isTeammate(player) ? ChatColor.BLUE.rgb : ChatColor.RED.rgb;
                  dotColor = new Color(teamColor | 0xFF000000, true);
                  break;
               case 2:
                  int themeRgb = theme != null ? theme.getColor(System.currentTimeMillis(), 0L).getRGB() : Color.WHITE.getRGB();
                  dotColor = new Color(themeRgb | 0xFF000000, true);
                  break;
               default:
                  dotColor = Color.WHITE;
            }
         }

         Render3DUtil.drawFilledCircle(blipX, blipZ, dotRadius, dotColor.getRGB());
      }

      if ((Boolean) this.showPvpProp.getValue()) {
         double pvpDiffX = -mc.thePlayer.posX;
         double pvpDiffZ = -mc.thePlayer.posZ;
         double pvpRotX = pvpDiffX * cosYaw + pvpDiffZ * sinYaw;
         double pvpRotZ = pvpDiffZ * cosYaw - pvpDiffX * sinYaw;
         double pvpDist = Math.sqrt(pvpRotX * pvpRotX + pvpRotZ * pvpRotZ);
         double pvpScale = pvpDist < (double) (radius * 2.0F) ? 1.0 : (double) (radius * 2.0F) / pvpDist;
         double pvpBlipX = pvpRotX * pvpScale;
         double pvpBlipZ = pvpRotZ * pvpScale;

         GlStateManager.pushMatrix();
         GlStateManager.disableDepth();
         GlStateManager.enableBlend();
         GlStateManager.enableTexture2D();
         GlStateManager.blendFunc(770, 771);

         if (theme != null) {
            HudThemeManager.drawString(
               "PVP",
               (float) (pvpBlipX - (double) (HudThemeManager.getStringWidth("PVP") / 2.0F)),
               (float) (pvpBlipZ - (double) (HudThemeManager.getFontHeight() / 2.0F)),
               Color.WHITE.getRGB(),
               (Boolean) theme.shadowProp.getValue()
            );
         }

         GlStateManager.popMatrix();
         GlStateManager.disableBlend();
         GlStateManager.disableDepth();
      }

      Render3DUtil.setupRender2D();
      GlStateManager.popMatrix();
      HudDragManager.handleDragging(this);
   }

   public Radar() {
      super("Radar", false, ModuleCategory.VISUAL);
   }
}
