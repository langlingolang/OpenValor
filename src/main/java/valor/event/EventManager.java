package valor.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import valor.event.events.Event;
import valor.event.events.EventStoppable;
import valor.event.types.Priority;

public final class EventManager {
   private static final HashMap<Class<? extends Event>, List<EventManager.MethodData>> REGISTRY_MAP = new HashMap<>();
   private static final Set<String> LOGGED_FAILURES = ConcurrentHashMap.newKeySet();

   private static void sortMethodsByPriority(Class<? extends Event> var0) {
      CopyOnWriteArrayList var1 = new CopyOnWriteArrayList();

      for (byte var5 : Priority.VALUE_ARRAY) {
         Iterator var6 = REGISTRY_MAP.get(var0).iterator();

         while (var6.hasNext()) {
            EventManager.MethodData var7;
            if ((var7 = (EventManager.MethodData)var6.next()).getPriority() == var5) {
               var1.add(var7);
            }
         }
      }

      REGISTRY_MAP.put(var0, var1);
   }

   private static boolean isInvalidMethod(Method var0) {
      return var0.getParameterTypes().length != 1 || !var0.isAnnotationPresent(EventTarget.class);
   }

   private static void invokeHandler(EventManager.MethodData var0, Event var1) {
      try {
         var0.getTarget().invoke(var0.getSource(), var1);
      } catch (Throwable var2) {
         Throwable var4 = var2 instanceof InvocationTargetException && var2.getCause() != null ? var2.getCause() : var2;
         String var3 = var0.getSource().getClass().getName() + "." + var0.getTarget().getName() + ":" + var4.getClass().getName() + ":" + var4.getMessage();
         if (LOGGED_FAILURES.add(var3)) {
            System.err.println("[Valor] Event handler failed: ".concat(String.valueOf(var3)));
            var4.printStackTrace();
         }
      }
   }

   private static void registerMethod(Method var0, Object var1) {
      Class var2 = var0.getParameterTypes()[0];
      EventManager.MethodData var3;
      if (!(var3 = new EventManager.MethodData(var1, var0, var0.getAnnotation(EventTarget.class).value())).getTarget().isAccessible()) {
         var3.getTarget().setAccessible(true);
      }

      if (REGISTRY_MAP.containsKey(var2)) {
         if (!REGISTRY_MAP.get(var2).contains(var3)) {
            REGISTRY_MAP.get(var2).add(var3);
            sortMethodsByPriority(var2);
            return;
         }
      } else {
         List<EventManager.MethodData> var4 = new CopyOnWriteArrayList<>();
         var4.add(var3);
         REGISTRY_MAP.put(var2, var4);
      }
   }

   public static void cleanMap(boolean var0) {
      Iterator var1 = REGISTRY_MAP.entrySet().iterator();

      while (var1.hasNext()) {
         if (!var0 || ((List)((Entry)var1.next()).getValue()).isEmpty()) {
            var1.remove();
         }
      }
   }

   public static Event call(Event var0) {
      List var1;
      if ((var1 = REGISTRY_MAP.get(var0.getClass())) != null) {
         if (var0 instanceof EventStoppable) {
            EventStoppable var2 = (EventStoppable)var0;
            Iterator var3 = var1.iterator();

            while (var3.hasNext()) {
               invokeHandler((EventManager.MethodData)var3.next(), var0);
               if (var2.isStopped()) {
                  break;
               }
            }
         } else {
            Iterator var4 = var1.iterator();

            while (var4.hasNext()) {
               invokeHandler((EventManager.MethodData)var4.next(), var0);
            }
         }
      }

      return var0;
   }

   private EventManager() {
   }

   public static void register(Object var0) {
      Method[] var1;
      int var2 = (var1 = var0.getClass().getDeclaredMethods()).length;

      for (int var3 = 0; var3 < var2; var3++) {
         Method var4;
         if (!isInvalidMethod(var4 = var1[var3])) {
            registerMethod(var4, var0);
         }
      }
   }

   public static void register(Object var0, Class<? extends Event> var1) {
      Method[] var2;
      int var3 = (var2 = var0.getClass().getDeclaredMethods()).length;

      for (int var4 = 0; var4 < var3; var4++) {
         Method var5;
         Method var6;
         if (!isInvalidMethod(var6 = var5 = var2[var4]) && var6.getParameterTypes()[0].equals(var1)) {
            registerMethod(var5, var0);
         }
      }
   }

   public static void unregister(Object var0) {
      Iterator var1 = REGISTRY_MAP.values().iterator();

      while (var1.hasNext()) {
         List var2;
         Iterator var3 = (var2 = (List)var1.next()).iterator();

         while (var3.hasNext()) {
            EventManager.MethodData var4;
            if ((var4 = (EventManager.MethodData)var3.next()).getSource().equals(var0)) {
               var2.remove(var4);
            }
         }
      }

      cleanMap(true);
   }

   public static void unregister(Object var0, Class<? extends Event> var1) {
      if (REGISTRY_MAP.containsKey(var1)) {
         Iterator var2 = REGISTRY_MAP.get(var1).iterator();

         while (var2.hasNext()) {
            EventManager.MethodData var3;
            if ((var3 = (EventManager.MethodData)var2.next()).getSource().equals(var0)) {
               REGISTRY_MAP.get(var1).remove(var3);
            }
         }

         cleanMap(true);
      }
   }

   public static void removeEntry(Class<? extends Event> var0) {
      Iterator var1 = REGISTRY_MAP.entrySet().iterator();

      while (var1.hasNext()) {
         if (((Class)((Entry)var1.next()).getKey()).equals(var0)) {
            var1.remove();
            break;
         }
      }
   }

   static final class MethodData {
      private final Object source;
      private final Method target;
      private final byte priority;

      public MethodData(Object var1, Method var2, byte var3) {
         this.source = var1;
         this.target = var2;
         this.priority = var3;
      }

      public final byte getPriority() {
         return this.priority;
      }

      public final Method getTarget() {
         return this.target;
      }

      public final Object getSource() {
         return this.source;
      }
   }
}
