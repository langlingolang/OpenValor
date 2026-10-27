package valor.module.render;

import java.awt.Color;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import valor.Valor;
import valor.event.EventTarget;
import valor.events.Render2DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.util.DraggableHudElement;
import valor.util.FloatInterpolator;
import valor.util.GlScaleUtil;
import valor.util.HudDragManager;
import valor.util.RenderUtil;
import valor.util.render.BlurPipeline;

public class Watermark extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private float hudWidth = 60.0F;
   private float hudHeight = 18.0F;
   private final FloatInterpolator fadeInterpolator = new FloatInterpolator(0.0F);

   public final ModeProperty modeProp = new ModeProperty("mode", 1, new String[]{"NAME", "VERSION", "FULL"});
   public final BooleanProperty showPerformanceProp = new BooleanProperty("show-performance", Boolean.TRUE);
   public final FloatProperty posXProp = new FloatProperty("pos-x", 4.0F, 0.0F, 4000.0F);
   public final FloatProperty posYProp = new FloatProperty("pos-y", 4.0F, 0.0F, 4000.0F);

   public static String[] getPerformanceStats() {
      int fps = Minecraft.getDebugFPS();
      double speed = mc.thePlayer == null ? 0.0 : Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * 20.0;
      int ping = 0;

      try {
         NetworkPlayerInfo info;
         if ((info = mc.thePlayer == null ? null : mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID())) != null) {
            ping = info.getResponseTime();
         }
      } catch (Throwable ignored) {
      }

      return new String[]{String.valueOf(fps), String.format(Locale.US, "%.2f", speed), ping + " ms"};
   }

   @Override
   public float getWidth() {
      return this.hudWidth;
   }

   @Override
   public float getX() {
      return (Float)this.posXProp.getValue();
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
      return (Float)this.posYProp.getValue();
   }

   @Override
   public float getHeight() {
      return this.hudHeight;
   }

   @EventTarget
   public void onRender(Render2DEvent event) {
      if (mc.gameSettings.showDebugInfo) {
         this.fadeInterpolator.reset(0.0F);
      } else {
         long currentTime = System.currentTimeMillis();
         this.fadeInterpolator.interpolateTo(this.isEnabled() ? 1.0F : 0.0F, 170L, currentTime);
         float fadeAlpha = this.fadeInterpolator.getValue(currentTime);
         if (fadeAlpha > 0.01F) {
            String clientName = "valor";
            String version = Valor.version;
            version = Valor.version == null || version.isEmpty()
               ? "dev"
               : (version.equalsIgnoreCase("dev") ? "dev" : (!version.startsWith("v") && !version.startsWith("V") ? "v" + version : version));
            String playerInfo = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
            String serverIp = mc.isSingleplayer() ? "singleplayer" : (mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "menu");
            if (serverIp.length() > 22) {
               serverIp = serverIp.substring(0, 22);
            }

            playerInfo = this.modeProp.getValue() == 2 ? playerInfo + "  " + serverIp : serverIp;
            boolean showPerf = (Boolean)this.showPerformanceProp.getValue();
            String[] perfStats = showPerf ? getPerformanceStats() : null;
            float fontHeight = RenderUtil.getFontHeight();
            String label = this.modeProp.getValue() == 0 ? "" : (this.modeProp.getValue() == 1 ? version : playerInfo);
            if (showPerf) {
               label = (label.isEmpty() ? "" : label + "   ") + perfStats[0] + " fps   " + perfStats[2];
            }

            float nameWidth = RenderUtil.getBoldStringWidth(clientName);
            float totalWidth = nameWidth + 14.0F + (label.isEmpty() ? 0.0F : 9.0F + RenderUtil.getStringWidth(label));
            float totalHeight = fontHeight + 10.0F;
            float posX = (Float)this.posXProp.getValue();
            float posY = (Float)this.posYProp.getValue();
            BlurPipeline.prepare();
            GlScaleUtil.beginScale(posX, posY, totalWidth, totalHeight, fadeAlpha);

            try {
               Color brandColor = RenderUtil.getGradientColor(0, 1, currentTime);
               RenderUtil.drawPanel(posX, posY, totalWidth, totalHeight);
               float textY = posY + (totalHeight - fontHeight) / 2.0F;
               RenderUtil.drawBoldString(clientName, posX + 7.0F, textY, brandColor);
               if (!label.isEmpty()) {
                  RenderUtil.drawString(label, posX + 7.0F + nameWidth + 9.0F, textY, RenderUtil.getLightTextColor(), false);
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            } finally {
               GlScaleUtil.endScale();
            }

            this.hudWidth = totalWidth;
            this.hudHeight = totalHeight;
            HudDragManager.handleDragging(this);
         }
      }
   }

   @Override
   public boolean isHudVisible() {
      return this.isEnabled() && !mc.gameSettings.showDebugInfo;
   }

   public Watermark() {
      super("Watermark", true, false, ModuleCategory.VISUAL);
      this.setHidden(false);
   }
}
