package valor.module.render;

import java.awt.Color;
import java.util.Iterator;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.events.Render2DEvent;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.BotCheckUtil;
import valor.util.ChatColor;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;

public class Tracers extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static final float[] ARROW_X_OFFSETS = new float[]{7.0F, 6.0F, -4.8F, -5.7F, -6.2F, -6.2F, -5.7F, -4.8F, 6.0F};

   public static final float[] ARROW_Y_OFFSETS = new float[]{0.0F, 1.2F, 7.8F, 7.6F, 6.7F, -6.7F, -7.6F, -7.8F, -1.2F};

   public final ModeProperty colorProp = new ModeProperty("color", 2, new String[]{"DEFAULT", "TEAMS", "THEME"});
   public final BooleanProperty linesProp = new BooleanProperty("lines", Boolean.FALSE);
   public final BooleanProperty arrowsProp = new BooleanProperty("arrows", Boolean.TRUE);
   public final IntProperty arrowHideFovProp = new IntProperty("arrow-hide-fov", 0, 0, 180, () -> (Boolean) this.arrowsProp.getValue());
   public final PercentageProperty opacityProp = new PercentageProperty("opacity", 100);
   public final IntProperty distanceProp = new IntProperty("distance", 512, 0, 512);
   public final BooleanProperty playersProp = new BooleanProperty("players", Boolean.TRUE);
   public final BooleanProperty friendsProp = new BooleanProperty("friends", Boolean.TRUE);
   public final BooleanProperty enemiesProp = new BooleanProperty("enemies", Boolean.TRUE);
   public final BooleanProperty botsProp = new BooleanProperty("bots", Boolean.FALSE);

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.isEnabled() && (Boolean) this.linesProp.getValue()) {
         Render3DUtil.setupRender3D();
         Vec3 eyeOffset;
         if (mc.gameSettings.thirdPersonView == 0) {
            eyeOffset = new Vec3(0.0, 0.0, 1.0)
               .rotatePitch(
                  (float) -Math.toRadians(
                     (double) Render3DUtil.lerpFloat(
                        mc.getRenderViewEntity().rotationPitch,
                        mc.getRenderViewEntity().prevRotationPitch,
                        ReflectionUtil.getTimer(mc).renderPartialTicks
                     )
                  )
               )
               .rotateYaw(
                  (float) -Math.toRadians(
                     (double) Render3DUtil.lerpFloat(
                        mc.getRenderViewEntity().rotationYaw,
                        mc.getRenderViewEntity().prevRotationYaw,
                        ReflectionUtil.getTimer(mc).renderPartialTicks
                     )
                  )
               );
         } else {
            eyeOffset = new Vec3(0.0, 0.0, 0.0)
               .rotatePitch(
                  (float) -Math.toRadians(
                     (double) Render3DUtil.lerpFloat(
                        mc.thePlayer.cameraPitch,
                        mc.thePlayer.prevCameraPitch,
                        ReflectionUtil.getTimer(mc).renderPartialTicks
                     )
                  )
               )
               .rotateYaw(
                  (float) -Math.toRadians(
                     (double) Render3DUtil.lerpFloat(
                        mc.thePlayer.cameraYaw,
                        mc.thePlayer.prevCameraYaw,
                        ReflectionUtil.getTimer(mc).renderPartialTicks
                     )
                  )
               );
         }

         eyeOffset = new Vec3(eyeOffset.xCoord, eyeOffset.yCoord + (double) mc.getRenderViewEntity().getEyeHeight(), eyeOffset.zCoord);

         for (EntityPlayer player : BotCheckUtil.getSortedLoadedEntities()
            .stream()
            .filter(entity -> entity instanceof EntityPlayer && this.isValidPlayer((EntityPlayer) entity))
            .map(EntityPlayer.class::cast)
            .collect(Collectors.toList())) {

            Color tracerColor = this.getPlayerColor(player, (float) ((Integer) this.opacityProp.getValue()).intValue() / 100.0F);
            double renderX = Render3DUtil.interpolate(player.posX, player.lastTickPosX, (double) event.getPartialTicks());
            double renderY = Render3DUtil.interpolate(player.posY, player.lastTickPosY, (double) event.getPartialTicks()) - (player.isSneaking() ? 0.125 : 0.0);
            double renderZ = Render3DUtil.interpolate(player.posZ, player.lastTickPosZ, (double) event.getPartialTicks());

            Render3DUtil.drawTracerLine(
               eyeOffset,
               renderX,
               renderY + (double) player.getEyeHeight(),
               renderZ,
               (float) tracerColor.getRed() / 255.0F,
               (float) tracerColor.getGreen() / 255.0F,
               (float) tracerColor.getBlue() / 255.0F,
               (float) tracerColor.getAlpha() / 255.0F
            );
         }

         Render3DUtil.endRender3D();
      }
   }

   public boolean isValidPlayer(EntityPlayer player) {
      if (player.deathTime > 0) {
         return false;
      } else if (mc.getRenderViewEntity().getDistanceToEntity(player) > (float) ((Integer) this.distanceProp.getValue()).intValue()) {
         return false;
      } else if (player == mc.thePlayer || player == mc.getRenderViewEntity()) {
         return false;
      } else if (BotCheckUtil.isBot(player)) {
         return (Boolean) this.botsProp.getValue();
      } else if (BotCheckUtil.isFriend(player)) {
         return (Boolean) this.friendsProp.getValue();
      } else {
         return BotCheckUtil.isTarget(player) ? (Boolean) this.enemiesProp.getValue() : (Boolean) this.playersProp.getValue();
      }
   }

   public Color getPlayerColor(EntityPlayer player, float opacity) {
      if (BotCheckUtil.isFriend(player)) {
         Color friendColor = Valor.friendManager.color;
         return new Color((float) friendColor.getRed() / 255.0F, (float) friendColor.getGreen() / 255.0F, (float) friendColor.getBlue() / 255.0F, opacity);
      } else if (BotCheckUtil.isTarget(player)) {
         Color targetColor = Valor.targetManager.color;
         return new Color((float) targetColor.getRed() / 255.0F, (float) targetColor.getGreen() / 255.0F, (float) targetColor.getBlue() / 255.0F, opacity);
      } else {
         switch ((Integer) this.colorProp.getValue()) {
            case 0:
               return BotCheckUtil.getTeamColor(player, opacity);
            case 1:
               int teamColor = BotCheckUtil.isTeammate(player) ? ChatColor.BLUE.rgb : ChatColor.RED.rgb;
               return new Color(teamColor & Color.WHITE.getRGB() | (int) (opacity * 255.0F) << 24, true);
            case 2:
               int themeColor = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), 0L).getRGB();
               return new Color(themeColor & Color.WHITE.getRGB() | (int) (opacity * 255.0F) << 24, true);
            default:
               return new Color(1.0F, 1.0F, 1.0F, opacity);
         }
      }
   }

   @EventTarget
   public void onRender(Render2DEvent event) {
      if (this.isEnabled() && (Boolean) this.arrowsProp.getValue() && mc.thePlayer != null && mc.getRenderViewEntity() != null) {
         ScaledResolution sr = new ScaledResolution(mc);
         float centerX = (float) sr.getScaledWidth() / 2.0F;
         float centerY = (float) sr.getScaledHeight() / 2.0F;
         float themeScale = (Float) HudThemeManager.getInstance().scaleProp.getValue();
         float radius = Math.min(72.0F * themeScale, Math.min(centerX, centerY) * 0.48F);
         float partialTicks = event.getPartialTicks();
         float viewYaw = Render3DUtil.lerpFloat(mc.getRenderViewEntity().rotationYaw, mc.getRenderViewEntity().prevRotationYaw, partialTicks);
         if (mc.gameSettings.thirdPersonView == 2) {
            viewYaw += 180.0F;
         }

         double viewerX = Render3DUtil.interpolate(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().prevPosX, (double) partialTicks);
         double viewerZ = Render3DUtil.interpolate(mc.getRenderViewEntity().posZ, mc.getRenderViewEntity().prevPosZ, (double) partialTicks);
         GL11.glPushAttrib(1048575);

         try {
            Render3DUtil.setupRender3D();
            for (EntityPlayer player : BotCheckUtil.getSortedLoadedEntities()
               .stream()
               .filter(entity -> entity instanceof EntityPlayer && this.isValidPlayer((EntityPlayer) entity))
               .map(EntityPlayer.class::cast)
               .collect(Collectors.toList())) {

               double diffX = Render3DUtil.interpolate(player.posX, player.prevPosX, (double) partialTicks) - viewerX;
               double diffZ = Render3DUtil.interpolate(player.posZ, player.prevPosZ, (double) partialTicks) - viewerZ;
               float angle = MathHelper.wrapAngleTo180_float(
                  (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0) - viewYaw
               );

               if (!(Math.abs(angle) < (float) ((Integer) this.arrowHideFovProp.getValue()).intValue() / 2.0F)) {
                  float opacity = ((Integer) this.opacityProp.getValue()).floatValue() / 100.0F;
                  double rad = Math.toRadians((double) angle);
                  float sin = (float) Math.sin(rad);
                  float cos = (float) -Math.cos(rad);
                  float arrowPosX = centerX + radius * sin;
                  float arrowPosY = centerY + radius * cos;
                  int arrowRgb = this.getPlayerColor(player, opacity).getRGB();
                  float perpX = -cos;
                  float perpY = sin;

                  Render3DUtil.color(arrowRgb);
                  GL11.glBegin(9);
                  for (int i = 0; i < ARROW_X_OFFSETS.length; i++) {
                     GL11.glVertex2f(
                        arrowPosX + themeScale * (sin * ARROW_X_OFFSETS[i] + perpX * ARROW_Y_OFFSETS[i]),
                        arrowPosY + themeScale * (cos * ARROW_X_OFFSETS[i] + perpY * ARROW_Y_OFFSETS[i])
                     );
                  }
                  GL11.glEnd();

                  int outlineAlpha = Math.min(220, (arrowRgb >>> 24) * 220 / 255);
                  Render3DUtil.color(new Color(9, 12, 17, outlineAlpha).getRGB());
                  GL11.glLineWidth(1.6F);
                  GL11.glEnable(2848);
                  GL11.glHint(3154, 4354);
                  GL11.glBegin(2);
                  for (int i = 0; i < ARROW_X_OFFSETS.length; i++) {
                     GL11.glVertex2f(
                        arrowPosX + themeScale * (sin * ARROW_X_OFFSETS[i] + perpX * ARROW_Y_OFFSETS[i]),
                        arrowPosY + themeScale * (cos * ARROW_X_OFFSETS[i] + perpY * ARROW_Y_OFFSETS[i])
                     );
                  }
                  GL11.glEnd();
                  GL11.glDisable(2848);
                  GL11.glLineWidth(2.0F);
               }
            }
         } finally {
            GL11.glPopAttrib();
            Render3DUtil.setupRender2D();
         }
      }
   }

   public Tracers() {
      super("Tracers", false, ModuleCategory.VISUAL);
   }
}
