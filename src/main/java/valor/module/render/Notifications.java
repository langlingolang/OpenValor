package valor.module.render;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import valor.Valor;
import valor.event.EventTarget;
import valor.events.LoadWorldEvent;
import valor.events.Render2DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.ColorUtil;
import valor.util.DraggableHudElement;
import valor.util.EaseAnimation;
import valor.util.HudDragManager;
import valor.util.NotificationManager;
import valor.util.RenderUtil;

public class Notifications extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final float SPACING_Y = 3.0F;
   public static final float PADDING_X = 8.0F;
   public static final float PADDING_Y = 8.0F;
   public static final float ANIMATION_OFFSET_X = 36.0F;
   public static final Color SUCCESS_COLOR = new Color(85, 255, 130, 255);
   public static final Color ERROR_COLOR = new Color(255, 95, 95, 255);

   private float hudWidth = 72.0F;
   private float hudHeight = 26.0F;
   private boolean hasInitializedPos;
   private boolean hasShownWelcome;
   private int lastVerticalMode = -1;

   public final FloatProperty posXProp = new FloatProperty("pos-x", 0.0F, 0.0F, 4000.0F);
   public final FloatProperty posYProp = new FloatProperty("pos-y", 0.0F, 0.0F, 4000.0F);
   public final FloatProperty durationProp = new FloatProperty("duration", 3.0F, 1.0F, 10.0F);
   public final IntProperty maxProp = new IntProperty("max", 5, 1, 12);
   public final ModeProperty horizontalProp = new ModeProperty("horizontal", 1, new String[]{"LEFT", "RIGHT"});
   public final ModeProperty verticalProp = new ModeProperty("vertical", 0, new String[]{"DOWN", "UP"});
   public final BooleanProperty toggleSoundsProp = new BooleanProperty("toggle-sounds", Boolean.TRUE);
   public final ModeProperty soundProp = new ModeProperty("sound", 0, new String[]{"CLICK", "SIGMA", "RISE", "QUICKMACRO"}, () -> (Boolean)this.toggleSoundsProp.getValue());

   @Override
   public String getDescription() {
      return "Animated on-screen toasts for toggles and alerts.";
   }

   @Override
   public float getWidth() {
      return this.hudWidth;
   }

   public void updateVerticalPosition() {
      if (this.lastVerticalMode >= 0 && this.lastVerticalMode != (Integer)this.verticalProp.getValue()) {
         this.posYProp.setValue((Float)this.posYProp.getValue() + (this.verticalProp.getValue() == 1 ? this.hudHeight : -this.hudHeight));
      }

      this.lastVerticalMode = (Integer)this.verticalProp.getValue();
   }

   @Override
   public float getX() {
      return (Float)this.posXProp.getValue();
   }

   @Override
   public void setX(float x) {
      this.posXProp.setValue(x);
      this.hasInitializedPos = true;
   }

   public static float getNotificationWidth(NotificationManager.Notification notification) {
      return 16.0F + RenderUtil.getStringWidth(notification.title) + 8.0F + RenderUtil.getStringWidth(notification.message);
   }

   @Override
   public void setY(float y) {
      this.posYProp.setValue(this.verticalProp.getValue() == 1 ? y + this.hudHeight : y);
      this.hasInitializedPos = true;
   }

   @Override
   public float getY() {
      return this.verticalProp.getValue() == 1 ? (Float)this.posYProp.getValue() - this.hudHeight : (Float)this.posYProp.getValue();
   }

   @Override
   public float getHeight() {
      return this.hudHeight;
   }

   @EventTarget
   public void onRender(Render2DEvent event) {
      if (this.isEnabled() && !mc.gameSettings.showDebugInfo) {
         ScaledResolution sr = new ScaledResolution(mc);
         if (!this.hasInitializedPos && (Float)this.posXProp.getValue() == 0.0F && (Float)this.posYProp.getValue() == 0.0F) {
            this.posXProp.setValue((float)sr.getScaledWidth() - 80.0F);
            this.posYProp.setValue(8.0F);
            this.hasInitializedPos = true;
         }

         NotificationManager nm = NotificationManager.getInstance();
         long durationMs = (long)((Float)this.durationProp.getValue() * 1000.0F);
         int maxNotifications = (Integer)this.maxProp.getValue();
         nm.defaultDurationMs = Math.max(500L, durationMs);
         nm.maxActiveNotifications = Math.max(1, maxNotifications);
         nm.update();
         List<NotificationManager.Notification> notifList = nm.getNotifications();
         boolean isDragging = HudDragManager.isChatGuiOpen();
         float posX = (Float)this.posXProp.getValue();
         float notifHeight = RenderUtil.getFontHeight() + 1.0F + 16.0F;

         if (notifList.isEmpty()) {
            this.hudHeight = notifHeight;
            this.updateVerticalPosition();
            float posY = this.getY();
            if (isDragging) {
               String label = "Notifications";
               float width = 16.0F + RenderUtil.getStringWidth(label);
               RenderUtil.drawRoundedRect(posX, posY, width, notifHeight);
               RenderUtil.drawString(label, posX + 8.0F, posY + (notifHeight - RenderUtil.getFontHeight()) / 2.0F, RenderUtil.getSecondaryTextColor(), false);
               this.hudWidth = width;
               this.hudHeight = notifHeight;
               HudDragManager.handleDragging(this);
            } else {
               this.hudWidth = 72.0F;
               this.hudHeight = notifHeight;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            float maxWidth = 0.0F;
            float totalHeight = 0.0F;

            for (int i = notifList.size() - 1; i >= 0; i--) {
               NotificationManager.Notification notif = notifList.get(i);
               float width = getNotificationWidth(notif);
               maxWidth = Math.max(maxWidth, width);
               notif.yAnim.start(totalHeight, 320.0F, EaseAnimation.Easing.OUT_QUART);
               notif.yAnim.update();
               totalHeight += notifHeight + 3.0F;
            }

            float finalHeight = Math.max(notifHeight, totalHeight - 3.0F);
            this.hudHeight = finalHeight;
            this.updateVerticalPosition();
            float posY = (Float)this.posYProp.getValue();
            long time = System.currentTimeMillis();
            int count = notifList.size();

            for (int i = 0; i < count; i++) {
               NotificationManager.Notification notif = notifList.get(i);
               float alpha = Math.max(0.0F, Math.min(1.0F, notif.fadeAnim.getValue()));
               if (!(alpha <= 0.02F)) {
                  float width = getNotificationWidth(notif);
                  float animOffset = (1.0F - alpha) * 36.0F;
                  float drawX = this.horizontalProp.getValue() == 1 ? posX + maxWidth - width + animOffset : posX - animOffset;
                  float drawY = this.verticalProp.getValue() == 1 ? posY - notifHeight - notif.yAnim.getValue() : posY + notif.yAnim.getValue();
                  RenderUtil.drawRoundedRect(drawX, drawY, width, notifHeight);
                  int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
                  Color titleColor = ColorUtil.withAlpha(RenderUtil.getGradientColor(i, count, time), alphaByte);
                  NotificationManager.NotificationType type = notif.notificationType;
                  Color typeColor;
                  switch (type) {
                     case TOGGLE_ON:
                     case SUCCESS:
                        typeColor = SUCCESS_COLOR;
                        break;
                     case TOGGLE_OFF:
                     case WARNING:
                        typeColor = ERROR_COLOR;
                        break;
                     default:
                        typeColor = RenderUtil.getDefaultTextColor();
                  }

                  Color finalTypeColor = ColorUtil.withAlpha(typeColor, alphaByte);
                  drawY += (notifHeight - RenderUtil.getFontHeight()) / 2.0F;
                  RenderUtil.drawString(notif.title, drawX + 8.0F, drawY, titleColor, false);
                  float messageWidth = RenderUtil.getStringWidth(notif.message);
                  RenderUtil.drawString(notif.message, drawX + width - 8.0F - messageWidth, drawY, finalTypeColor, false);
               }
            }

            this.hudWidth = maxWidth;
            this.hudHeight = finalHeight;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            HudDragManager.handleDragging(this);
         }
      }
   }

   @EventTarget
   public void onWorldLoad(LoadWorldEvent event) {
      if (this.isEnabled() && !this.hasShownWelcome) {
         this.hasShownWelcome = true;
         String version = Valor.version != null && !Valor.version.isEmpty() ? Valor.version : "dev";
         if (!version.equalsIgnoreCase("dev") && !version.startsWith("v") && !version.startsWith("V")) {
            version = "v" + version;
         }

         NotificationManager.info("Valor", "loaded " + version);
      }
   }

   @Override
   public boolean isHudVisible() {
      return this.isEnabled() && !mc.gameSettings.showDebugInfo;
   }

   public Notifications() {
      super("Notifications", true, false, ModuleCategory.VISUAL);
      this.setHidden(false);
   }
}
