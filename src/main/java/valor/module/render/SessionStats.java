package valor.module.render;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.EnumChatFormatting;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.LoadWorldEvent;
import valor.events.PacketEvent;
import valor.events.Render2DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.FloatProperty;
import valor.util.DraggableHudElement;
import valor.util.FloatInterpolator;
import valor.util.GlScaleUtil;
import valor.util.HudDragManager;
import valor.util.RenderUtil;
import valor.util.render.BlurPipeline;
import valor.util.render.GreenRender;

public class SessionStats extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private float hudWidth = 72.0F;
   private float hudHeight = 52.0F;
   private final FloatInterpolator fadeInterpolator = new FloatInterpolator(0.0F);
   private boolean hasAdjustedPosition;
   private long sessionStartTimeMs = System.currentTimeMillis();
   private int killsCount = 0;
   private int winsCount = 0;
   private String lastTargetPlayerName = null;
   private long lastAttackTimeMs = 0L;

   public final FloatProperty posXProp = new FloatProperty("pos-x", 4.0F, 0.0F, 4000.0F);
   public final FloatProperty posYProp = new FloatProperty("pos-y", 112.0F, 0.0F, 4000.0F);

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (this.isEnabled()) {
         if (event.getType() == EventType.SEND
            && event.getPacket() instanceof C02PacketUseEntity
            && ((C02PacketUseEntity)event.getPacket()).getAction() == Action.ATTACK
            && mc.theWorld != null) {
            Entity target = ((C02PacketUseEntity)event.getPacket()).getEntityFromWorld(mc.theWorld);
            if (target instanceof EntityPlayer) {
               this.lastTargetPlayerName = target.getName();
               this.lastAttackTimeMs = System.currentTimeMillis();
            }
         }

         if (event.getType() == EventType.RECEIVE && event.getPacket() instanceof S02PacketChat) {
            String text = EnumChatFormatting.getTextWithoutFormattingCodes(((S02PacketChat)event.getPacket()).getChatComponent().getUnformattedText());
            if (text == null) {
               return;
            }

            String lower = text.toLowerCase(Locale.ROOT);
            if (this.lastTargetPlayerName != null
               && System.currentTimeMillis() - this.lastAttackTimeMs < 8000L
               && (
                  lower.contains(this.lastTargetPlayerName.toLowerCase(Locale.ROOT) + " was killed")
                     || lower.contains("killed by " + (mc.thePlayer != null ? mc.thePlayer.getName().toLowerCase(Locale.ROOT) : ""))
                     || lower.contains(this.lastTargetPlayerName.toLowerCase(Locale.ROOT)) && lower.contains("killed")
               )) {
               this.killsCount++;
               this.lastTargetPlayerName = null;
            }

            if (lower.contains("you won")
               || lower.contains("victory")
               || lower.contains("winner!")
               || mc.thePlayer != null && lower.contains(mc.thePlayer.getName().toLowerCase(Locale.ROOT) + " won")) {
               this.winsCount++;
            }
         }
      }
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
            if (!this.hasAdjustedPosition) {
               this.hasAdjustedPosition = true;
               if ((Float)this.posXProp.getValue() == 4.0F && (Float)this.posYProp.getValue() == 100.0F) {
                  this.posYProp.setValue(112.0F);
               }
            }

            long elapsedSec = Math.max(0L, (System.currentTimeMillis() - this.sessionStartTimeMs) / 1000L);
            long minutes = elapsedSec / 60L;
            long seconds = elapsedSec % 60L;
            String timeStr;
            if (minutes >= 60L) {
               long hours = minutes / 60L;
               minutes %= 60L;
               timeStr = hours + "h " + minutes + "m " + seconds + "s";
            } else {
               timeStr = minutes + "m " + seconds + "s";
            }

            float width = Math.max(138.0F, 70.0F + RenderUtil.getStringWidth(timeStr));
            float posX = (Float)this.posXProp.getValue();
            float posY = (Float)this.posYProp.getValue();
            String killsStr = String.valueOf(this.killsCount);
            String winsStr = String.valueOf(this.winsCount);

            BlurPipeline.prepare();
            GlScaleUtil.beginScale(posX, posY, width, 66.0F, fadeAlpha);

            try {
               RenderUtil.drawPanel(posX, posY, width, 66.0F);
               RenderUtil.drawBoldString("Session", posX + 10.0F, posY + 9.0F, RenderUtil.getLightTextColor());
               RenderUtil.drawString(timeStr, posX + width - 10.0F - RenderUtil.getStringWidth(timeStr), posY + 10.0F, RenderUtil.getSecondaryTextColor(), false);
               String[] statValues = new String[]{killsStr, winsStr};
               String[] statLabels = new String[]{"Kills", "Wins"};

               for (int i = 0; i < 2; i++) {
                  float colCenterX = posX + width * (i == 0 ? 0.25F : 0.75F);
                  GlStateManager.pushMatrix();
                  GlStateManager.translate(colCenterX - GreenRender.strWBold(statValues[i]) * 0.75F, posY + 29.0F, 0.0F);
                  GlStateManager.scale(1.5F, 1.5F, 1.0F);
                  RenderUtil.drawBoldString(statValues[i], 0.0F, 0.0F, RenderUtil.getLightTextColor());
                  GlStateManager.popMatrix();
                  RenderUtil.drawString(statLabels[i], colCenterX - RenderUtil.getStringWidth(statLabels[i]) / 2.0F, posY + 47.0F, RenderUtil.getSecondaryTextColor(), false);
               }
            } finally {
               GlScaleUtil.endScale();
            }

            this.hudWidth = width;
            this.hudHeight = 66.0F;
            HudDragManager.handleDragging(this);
         }
      }
   }

   @EventTarget
   public void onWorldLoad(LoadWorldEvent event) {
      if (this.sessionStartTimeMs <= 0L) {
         this.sessionStartTimeMs = System.currentTimeMillis();
      }
   }

   @Override
   public boolean isHudVisible() {
      return this.isEnabled() && !mc.gameSettings.showDebugInfo;
   }

   public SessionStats() {
      super("SessionStats", true, false, ModuleCategory.VISUAL);
      this.setHidden(false);
   }
}
