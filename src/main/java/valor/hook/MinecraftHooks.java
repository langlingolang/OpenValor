package valor.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import valor.Valor;
import valor.event.EventManager;
import valor.event.types.EventType;
import valor.events.HitBlockEvent;
import valor.events.KeyEvent;
import valor.events.LeftClickMouseEvent;
import valor.events.LoadWorldEvent;
import valor.events.ResizeEvent;
import valor.events.RightClickMouseEvent;
import valor.events.SwapItemEvent;
import valor.events.TickEvent;
import valor.module.combat.NoHitDelay;
import valor.module.render.ClickGuiMod;
import valor.util.ReflectionUtil;
import valor.util.Win32KeyUtil;

public class MinecraftHooks {
   private static boolean initialized = false;
   private static boolean initializing = false;
   private static volatile boolean asmKeyHookActive = false;
   private static volatile boolean asmTickHookActive = false;
   private static final boolean[] KEY_STATES = new boolean[256];
   private static final boolean[] MOUSE_BUTTON_STATES = new boolean[16];
   private static final boolean[] KEY_OVERRIDDEN = new boolean[256];
   private static boolean insertWasDown;

   public static void pollGuiHotkey(Minecraft mc) {
      boolean kbInsert = false;

      try {
         kbInsert = Keyboard.isCreated() && Keyboard.isKeyDown(210);
      } catch (Throwable ignored) {
      }

      boolean winInsert = false;

      try {
         winInsert = Win32KeyUtil.isInsertPressed();
      } catch (Throwable ignored) {
      }

      boolean insertActive = kbInsert || winInsert;
      ClickGuiMod clickGui;
      if (insertActive
         && !insertWasDown
         && mc.currentScreen == null
         && Valor.moduleManager != null
         && (clickGui = (ClickGuiMod)Valor.moduleManager.modules.get(ClickGuiMod.class)) != null) {
         clickGui.setEnabled(true);
      }

      insertWasDown = insertActive;
   }

   public static boolean isAsmTickHookActive() {
      return asmTickHookActive;
   }

   public static boolean isAsmKeyHookActive() {
      return asmKeyHookActive;
   }

   public static boolean onChangeCurrentItem(InventoryPlayer inventory, int slot) {
      SwapItemEvent event;
      EventManager.call(event = new SwapItemEvent(-1, slot));
      if (!event.isCancelled()) {
         inventory.changeCurrentItem(slot);
      }

      return event.isCancelled();
   }

   public static boolean onSendClickBlockToController(Minecraft mc) {
      HitBlockEvent event;
      EventManager.call(event = new HitBlockEvent());
      if (event.isCancelled()) {
         mc.playerController.resetBlockRemoving();
         return true;
      } else {
         return false;
      }
   }

   private static synchronized void initClient() {
      if (Valor.moduleManager == null && !initializing) {
         initializing = true;

         try {
            new Valor();
         } catch (Throwable t) {
            initializing = false;
            System.out.println("[Valor] Client thread startup failed: ".concat(String.valueOf(t)));
            t.printStackTrace();
         }
      }
   }

   public static void onResize() {
      EventManager.call(new ResizeEvent());
   }

   public static boolean onClickMouse(Minecraft mc) {
      NoHitDelay noHitDelay;
      if (Valor.moduleManager != null && (noHitDelay = (NoHitDelay)Valor.moduleManager.modules.get(NoHitDelay.class)) != null && noHitDelay.isEnabled()) {
         try {
            ReflectionUtil.resetLeftClickCounter(mc);
         } catch (Exception ignored) {
         }
      }

      LeftClickMouseEvent event;
      EventManager.call(event = new LeftClickMouseEvent());
      return event.isCancelled();
   }

   public static boolean onRightClickMouse() {
      RightClickMouseEvent event;
      EventManager.call(event = new RightClickMouseEvent());
      return event.isCancelled();
   }

   public static void onRunTickPost(Minecraft mc) {
      if (mc.theWorld != null && mc.thePlayer != null) {
         EventManager.call(new TickEvent(EventType.POST));
      }

      if (Keyboard.isCreated()) {
         for (int keyCode = 1; keyCode < KEY_STATES.length; keyCode++) {
            boolean isDown = Keyboard.isKeyDown(keyCode);
            if (mc.currentScreen == null && isDown && !KEY_STATES[keyCode] && !KEY_OVERRIDDEN[keyCode]) {
               EventManager.call(new KeyEvent(keyCode));
            }

            KEY_STATES[keyCode] = isDown;
         }
      }

      if (Mouse.isCreated()) {
         for (int button = 0; button < MOUSE_BUTTON_STATES.length; button++) {
            boolean isDown = button < Mouse.getButtonCount() && Mouse.isButtonDown(button);
            int eventKey = button - 100;
            if (mc.currentScreen == null && isDown && !MOUSE_BUTTON_STATES[button]) {
               EventManager.call(new KeyEvent(eventKey));
            }

            MOUSE_BUTTON_STATES[button] = isDown;
         }
      }
   }

   public static void onLoadWorld() {
      EventManager.call(new LoadWorldEvent());
   }

   public static void onRunTickPre(Minecraft mc) {
      asmTickHookActive = true;

      for (int i = 0; i < KEY_OVERRIDDEN.length; i++) {
         KEY_OVERRIDDEN[i] = false;
      }

      initClient();
      if (mc.theWorld != null && mc.thePlayer != null) {
         EventManager.call(new TickEvent(EventType.PRE));
      }
   }

   public static void onSetKeyBindState(int keyCode, boolean pressed, Minecraft mc) {
      boolean wasDown = false;
      if (keyCode >= 0 && keyCode < KEY_STATES.length) {
         wasDown = KEY_STATES[keyCode];
      } else if (keyCode >= -100 && keyCode < -100 + MOUSE_BUTTON_STATES.length) {
         wasDown = MOUSE_BUTTON_STATES[keyCode + 100];
      }

      KeyBinding.setKeyBindState(keyCode, pressed);
      if (keyCode >= 0 && keyCode < KEY_OVERRIDDEN.length) {
         KEY_OVERRIDDEN[keyCode] = true;
         KEY_STATES[keyCode] = pressed;
      } else if (keyCode >= -100 && keyCode < -100 + MOUSE_BUTTON_STATES.length) {
         MOUSE_BUTTON_STATES[keyCode + 100] = pressed;
      }

      if (pressed && !wasDown && mc.currentScreen == null) {
         asmKeyHookActive = true;
         EventManager.call(new KeyEvent(keyCode));
      }
   }

   public static void onStartGame() {
      if (!initialized) {
         ;
      }
   }

   public static void postStartGame() {
      if (!initialized) {
         initialized = true;
         initClient();
      }
   }
}
