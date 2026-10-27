package valor.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import valor.Valor;
import valor.module.Module;

public final class NotificationManager {
   public static final NotificationManager INSTANCE = new NotificationManager();

   public final List<Notification> notifications = new ArrayList<>();
   public long defaultDurationMs = 3000L;
   public int maxActiveNotifications = 5;

   public final synchronized void update() {
      long currentTime = System.currentTimeMillis();
      Iterator<Notification> it = this.notifications.iterator();

      while (it.hasNext()) {
         Notification notif = it.next();
         if (!notif.isFadingOut && !notif.isExpired && currentTime - notif.createTimeMs >= notif.durationMs) {
            notif.fadeOut();
         }

         notif.fadeAnim.update();
         if (notif.isFadingOut && notif.fadeAnim.isFinished() && notif.fadeAnim.getValue() <= 0.001F) {
            notif.isExpired = true;
         }

         if (notif.isExpired) {
            it.remove();
         }
      }

      int activeCount = 0;
      for (Notification notif : this.notifications) {
         if (!notif.isFadingOut) {
            activeCount++;
         }
      }

      if (activeCount > this.maxActiveNotifications) {
         int excess = activeCount - this.maxActiveNotifications;
         for (Notification notif : this.notifications) {
            if (excess <= 0) {
               break;
            }
            if (!notif.isFadingOut) {
               notif.fadeOut();
               excess--;
            }
         }
      }
   }

   public final synchronized List<Notification> getNotifications() {
      return new ArrayList<>(this.notifications);
   }

   public static void info(String title, String message) {
      addNotification(title, message, NotificationType.INFO, INSTANCE.defaultDurationMs);
   }

   public static void warn(String title, String message) {
      addNotification(title, message, NotificationType.WARNING, INSTANCE.defaultDurationMs);
   }

   public static void addNotification(String title, String message, NotificationType type, long durationMs) {
      if (isNotificationsEnabled() && Minecraft.getMinecraft() != null) {
         INSTANCE.add(new Notification(title, message, type, durationMs));
      }
   }

   public synchronized void add(Notification notification) {
      this.notifications.add(notification);
      while (this.notifications.size() > this.maxActiveNotifications) {
         Notification first = this.notifications.get(0);
         if (!first.isFadingOut) {
            first.fadeOut();
         }

         if (this.notifications.size() <= this.maxActiveNotifications + 2) {
            break;
         }

         this.notifications.remove(0);
      }
   }

   public static void moduleToggle(String moduleName, boolean enabled) {
      addNotification(moduleName, enabled ? "ON" : "OFF", enabled ? NotificationType.TOGGLE_ON : NotificationType.TOGGLE_OFF, INSTANCE.defaultDurationMs);
   }

   public static NotificationManager getInstance() {
      return INSTANCE;
   }

   public static boolean isNotificationsEnabled() {
      Module notifMod;
      return Valor.moduleManager != null && (notifMod = Valor.moduleManager.getModule("Notifications")) != null && notifMod.isEnabled();
   }

   private NotificationManager() {
   }

   public static class Notification {
      public final String title;
      public final String message;
      public final NotificationType notificationType;
      public final long createTimeMs;
      public final long durationMs;
      public final EaseAnimation fadeAnim;
      public final EaseAnimation yAnim;
      public boolean isFadingOut;
      public boolean isExpired;

      public void fadeOut() {
         if (!this.isFadingOut && !this.isExpired) {
            this.isFadingOut = true;
            this.fadeAnim.start(0.0F, 280.0F, EaseAnimation.Easing.IN_CUBIC);
         }
      }

      public Notification(String title, String message, NotificationType type, long durationMs) {
         this.title = title == null ? "" : title;
         this.message = message == null ? "" : message;
         this.notificationType = type == null ? NotificationType.INFO : type;
         this.createTimeMs = System.currentTimeMillis();
         this.durationMs = Math.max(500L, durationMs);
         this.fadeAnim = new EaseAnimation();
         this.fadeAnim.start(1.0F, 350.0F, EaseAnimation.Easing.OUT_EXPO);
         this.yAnim = new EaseAnimation();
         this.isFadingOut = false;
         this.isExpired = false;
      }
   }

   public enum NotificationType {
      INFO,
      SUCCESS,
      WARNING,
      TOGGLE_ON,
      TOGGLE_OFF;
   }
}
