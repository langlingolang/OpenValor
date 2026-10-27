package valor.agent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import valor.Valor;
import valor.event.EventManager;
import valor.event.types.EventType;
import valor.events.KeyEvent;
import valor.events.TickEvent;
import valor.hook.EntityPlayerSPHooks;
import valor.hook.MinecraftHooks;
import valor.util.ChatUtil;
public class ValorRunnable implements Runnable {
    static final boolean[] KEY_STATES = new boolean[256];
    static final AtomicBoolean TASK_SCHEDULED = new AtomicBoolean(false);
    public ValorRunnable() {
    }
    @Override
    public void run() {
        try {
            String mcDir = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft";
            new File(mcDir).mkdirs();
            File logFile = new File(mcDir + "\\valor_runtime.log");
            boolean append = logFile.length() < 8388608L;
            PrintStream printStream = new PrintStream(new FileOutputStream(logFile, append), true);
            System.setOut(printStream);
            System.setErr(printStream);
            System.out.println("[Valor] === ValorRunnable started at " + new Date() + " on " + Thread.currentThread().getName() + " ===");
        } catch (Exception ignored) {
        }
        try {
            Class.forName("valor.Valor").getConstructor().newInstance();
        } catch (Throwable t) {
            System.out.println("[Valor] Valor init failed: " + t);
            t.printStackTrace();
        }
        Thread pollThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(20L);
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc != null && TASK_SCHEDULED.compareAndSet(false, true)) {
                        mc.addScheduledTask(new TickHookTask());
                    }
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable t) {
                System.out.println("[Valor] Poll thread FATAL: " + t);
                t.printStackTrace(System.out);
            }
        }, "Valor-Poll");
        pollThread.setDaemon(true);
        pollThread.start();
    }
    public static class TickHookTask implements Runnable {
        private static int lastTicksExisted = -1;
        public TickHookTask() {
        }
        @Override
        public void run() {
            try {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc == null || Valor.moduleManager == null) {
                    return;
                }
                boolean inWorld = mc.theWorld != null && mc.thePlayer != null;
                if (inWorld) {
                    int currentTicks = mc.thePlayer.ticksExisted;
                    if (currentTicks != lastTicksExisted) {
                        lastTicksExisted = currentTicks;
                        ChatUtil.flushPendingMessages();
                        if (!MinecraftHooks.isAsmTickHookActive()) {
                            try {
                                EventManager.call(new TickEvent(EventType.PRE));
                            } catch (Throwable ignored) {
                            }
                            try {
                                EntityPlayerSPHooks.onUpdatePre(mc.thePlayer);
                            } catch (Throwable ignored) {
                            }
                            try {
                                EntityPlayerSPHooks.onMotionUpdate();
                            } catch (Throwable ignored) {
                            }
                            try {
                                EntityPlayerSPHooks.onLivingUpdate();
                            } catch (Throwable ignored) {
                            }
                            try {
                                EntityPlayerSPHooks.onMoveInput();
                            } catch (Throwable ignored) {
                            }
                            try {
                                EntityPlayerSPHooks.onUpdatePost(mc.thePlayer);
                            } catch (Throwable ignored) {
                            }
                            try {
                                EventManager.call(new TickEvent(EventType.POST));
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
                try {
                    if (!MinecraftHooks.isAsmTickHookActive()) {
                        if (mc.currentScreen == null) {
                            for (int key = 0; key < 256; ++key) {
                                boolean isDown = Keyboard.isKeyDown(key);
                                if (isDown && !KEY_STATES[key]) {
                                    EventManager.call(new KeyEvent(key));
                                }
                                KEY_STATES[key] = isDown;
                            }
                        } else {
                            for (int key = 0; key < 256; ++key) {
                                KEY_STATES[key] = false;
                            }
                        }
                    } else {
                        for (int key = 0; key < 256; ++key) {
                            KEY_STATES[key] = false;
                        }
                    }
                } catch (Throwable ignored) {
                }
            } finally {
                TASK_SCHEDULED.set(false);
            }
        }
    }
}
