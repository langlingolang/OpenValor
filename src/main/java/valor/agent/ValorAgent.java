package valor.agent;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.StringWriter;
import java.security.ProtectionDomain;
import java.lang.instrument.ClassFileTransformer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;
import sun.misc.Unsafe;
import valor.transformer.AbstractClientPlayerTransformer;
import valor.transformer.BlockModelRendererTransformer;
import valor.transformer.BlockRendererDispatcherTransformer;
import valor.transformer.BlockTransformer;
import valor.transformer.DisplayTransformer;
import valor.transformer.EntityLivingBaseTransformer;
import valor.transformer.EntityPlayerSPTransformer;
import valor.transformer.EntityPlayerTransformer;
import valor.transformer.EntityRendererTransformer;
import valor.transformer.EntityTransformer;
import valor.transformer.FontRendererTransformer;
import valor.transformer.GuiIngameForgeTransformer;
import valor.transformer.GuiIngameTransformer;
import valor.transformer.ItemStackTransformer;
import valor.transformer.KeyBindingTransformer;
import valor.transformer.MinecraftTransformer;
import valor.transformer.MovementInputFromOptionsTransformer;
import valor.transformer.NetworkManagerTransformer;
import valor.transformer.PlayerControllerMPTransformer;
import valor.transformer.RenderGlobalTransformer;
import valor.transformer.RenderManagerTransformer;
import valor.transformer.RenderPlayerTransformer;
import valor.transformer.RenderTransformer;
import valor.transformer.RendererLivingEntityTransformer;
import valor.transformer.VisGraphTransformer;
import valor.transformer.WorldRendererTransformer;
import valor.transformer.WorldTransformer;
public class ValorAgent {
    private static void logToFile(String msg) {
        System.out.println("[Valor] " + msg);
        try {
            String path = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\valor_runtime.log";
            FileWriter writer = new FileWriter(path, true);
            writer.write("[Valor] " + msg + "\n");
            writer.flush();
            writer.close();
        } catch (Exception ignored) {
        }
    }

    private static final Set<ClassLoader> INJECTED_CLASS_LOADERS = new HashSet<ClassLoader>();
    private static File agentJarFile;
    private static final String[] CLASSES_TO_RETRANSFORM = new String[]{
            "net.minecraft.client.Minecraft",
            "net.minecraft.entity.Entity",
            "net.minecraft.entity.EntityLivingBase",
            "net.minecraft.entity.player.EntityPlayer",
            "net.minecraft.client.entity.EntityPlayerSP",
            "net.minecraft.client.entity.AbstractClientPlayer",
            "net.minecraft.client.renderer.EntityRenderer",
            "net.minecraft.network.NetworkManager",
            "net.minecraft.network.play.client.C03PacketPlayer",
            "net.minecraft.client.multiplayer.PlayerControllerMP",
            "net.minecraft.client.settings.KeyBinding",
            "net.minecraft.client.renderer.entity.RendererLivingEntity",
            "net.minecraft.client.gui.GuiIngame",
            "net.minecraftforge.client.GuiIngameForge",
            "net.minecraft.client.renderer.entity.RenderManager",
            "net.minecraft.client.renderer.entity.Render",
            "net.minecraft.client.renderer.entity.RenderPlayer",
            "net.minecraft.client.gui.FontRenderer",
            "net.minecraft.block.Block",
            "net.minecraft.client.renderer.BlockModelRenderer",
            "net.minecraft.client.renderer.BlockRendererDispatcher",
            "net.minecraft.client.renderer.WorldRenderer",
            "net.minecraft.client.renderer.chunk.VisGraph",
            "net.minecraft.world.World",
            "net.minecraft.client.renderer.RenderGlobal",
            "net.minecraft.util.MovementInputFromOptions",
            "net.minecraft.item.ItemStack"
    };
    private static final String[] NOTCH_CLASSES_TO_RETRANSFORM = new String[]{
            "ave", "pk", "pr", "wn", "bew", "bet", "bfk", "ek", "ip",
            "bda", "avb", "bjl", "avo", "net.minecraftforge.client.GuiIngameForge",
            "biu", "biv", "bln", "avn", "afh", "bgf", "bgd", "bfd",
            "bhw", "adm", "zx", "org.lwjgl.opengl.Display"
    };

    public static void cleanTempFiles() {
        try {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File[] oldFiles = tmpDir.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return (lower.startsWith("valor") || lower.startsWith("va")) && lower.endsWith(".jar");
            });
            if (oldFiles != null) {
                for (File f : oldFiles) {
                    try {
                        f.delete();
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public ValorAgent() {
    }
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        registerTransformers(inst);
    }
    public static volatile Instrumentation INSTRUMENTATION = null;
    public static File resolvedRuntimeJar = null;

    public static File resolveAgentJarFile() {
        if (agentJarFile != null && agentJarFile.exists()) {
            return agentJarFile;
        }
        try {
            if (ValorAgent.class.getProtectionDomain() != null &&
                ValorAgent.class.getProtectionDomain().getCodeSource() != null &&
                ValorAgent.class.getProtectionDomain().getCodeSource().getLocation() != null) {
                File f = new File(ValorAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                if (f.exists()) {
                    agentJarFile = f;
                    return f;
                }
            }
        } catch (Throwable ignored) {
        }
        String valorJarProp = System.getProperty("valor.jar");
        if (valorJarProp != null && !valorJarProp.isEmpty()) {
            File f = new File(valorJarProp);
            if (f.exists()) {
                agentJarFile = f;
                return f;
            }
        }
        File[] candidates = new File[]{
            new File("build/libs/valor-1.0.0-universal.jar"),
            new File("valor-1.0.0-universal.jar"),
            new File(System.getProperty("user.dir"), "build/libs/valor-1.0.0-universal.jar"),
            new File(System.getProperty("user.dir"), "valor/reconstructed_src/build/libs/valor-1.0.0-universal.jar"),
            new File(System.getProperty("user.home") + "\\AppData\\Local\\Temp\\valor-1.0.0-universal.jar")
        };
        for (File candidate : candidates) {
            if (candidate.exists()) {
                agentJarFile = candidate;
                return candidate;
            }
        }
        return agentJarFile;
    }

    public static void openAllBootModules(Instrumentation inst) {
        if (inst == null) return;
        try {
            Method getModuleMethod = Class.class.getMethod("getModule");
            Object ourModule = getModuleMethod.invoke(ValorAgent.class);

            Class<?> moduleClass = Class.forName("java.lang.Module");
            Class<?> moduleLayerClass = Class.forName("java.lang.ModuleLayer");
            Method getPackagesMethod = moduleClass.getMethod("getPackages");
            Method redefineModule = Instrumentation.class.getMethod(
                "redefineModule", moduleClass, Set.class, Map.class, Map.class, Set.class, Map.class
            );

            Set<Object> targetModules = new HashSet<Object>();
            if (ourModule != null) {
                targetModules.add(ourModule);
            }
            try {
                Method getUnnamedModule = ClassLoader.class.getMethod("getUnnamedModule");
                Object sysUnnamed = getUnnamedModule.invoke(ClassLoader.getSystemClassLoader());
                if (sysUnnamed != null) targetModules.add(sysUnnamed);
                ClassLoader ctx = Thread.currentThread().getContextClassLoader();
                if (ctx != null) {
                    Object ctxUnnamed = getUnnamedModule.invoke(ctx);
                    if (ctxUnnamed != null) targetModules.add(ctxUnnamed);
                }
            } catch (Throwable ignored) {
            }

            try {
                for (Class<?> clazz : inst.getAllLoadedClasses()) {
                    try {
                        Object m = getModuleMethod.invoke(clazz);
                        if (m != null) targetModules.add(m);
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }

            Object bootLayer = moduleLayerClass.getMethod("boot").invoke(null);
            @SuppressWarnings("unchecked")
            Set<Object> bootModules = (Set<Object>) moduleLayerClass.getMethod("modules").invoke(bootLayer);

            for (Object mod : bootModules) {
                try {
                    @SuppressWarnings("unchecked")
                    Set<String> pkgs = (Set<String>) getPackagesMethod.invoke(mod);
                    if (pkgs == null || pkgs.isEmpty()) continue;

                    Map<String, Set<Object>> extraOpens = new HashMap<String, Set<Object>>();
                    Map<String, Set<Object>> extraExports = new HashMap<String, Set<Object>>();
                    for (String pkg : pkgs) {
                        extraOpens.put(pkg, targetModules);
                        extraExports.put(pkg, targetModules);
                    }

                    redefineModule.invoke(inst, mod, Collections.emptySet(), extraExports, extraOpens, Collections.emptySet(), Collections.emptyMap());
                } catch (Throwable ignored) {
                }
            }
            System.out.println("[Valor] Successfully opened " + bootModules.size() + " boot modules to all modules via Instrumentation.redefineModule");
        } catch (NoSuchMethodException e) {

        } catch (Throwable t) {
            System.err.println("[Valor] Note: openAllBootModules: " + t.getMessage());
        }
    }

    public static void openModuleForClassLoader(Instrumentation inst, ClassLoader loader) {
        if (inst == null || loader == null) return;
        try {
            Class<?> moduleClass = Class.forName("java.lang.Module");
            Class<?> moduleLayerClass = Class.forName("java.lang.ModuleLayer");
            Method getModuleMethod = Class.class.getMethod("getModule");
            Method getUnnamedModule = ClassLoader.class.getMethod("getUnnamedModule");
            Object loaderModule = getUnnamedModule.invoke(loader);
            if (loaderModule == null) return;

            Method getPackagesMethod = moduleClass.getMethod("getPackages");
            Method redefineModule = Instrumentation.class.getMethod(
                "redefineModule", moduleClass, Set.class, Map.class, Map.class, Set.class, Map.class
            );

            Set<Object> targetModules = Collections.singleton(loaderModule);
            Object bootLayer = moduleLayerClass.getMethod("boot").invoke(null);
            @SuppressWarnings("unchecked")
            Set<Object> bootModules = (Set<Object>) moduleLayerClass.getMethod("modules").invoke(bootLayer);

            for (Object mod : bootModules) {
                try {
                    @SuppressWarnings("unchecked")
                    Set<String> pkgs = (Set<String>) getPackagesMethod.invoke(mod);
                    if (pkgs == null || pkgs.isEmpty()) continue;

                    Map<String, Set<Object>> extraOpens = new HashMap<String, Set<Object>>();
                    Map<String, Set<Object>> extraExports = new HashMap<String, Set<Object>>();
                    for (String pkg : pkgs) {
                        extraOpens.put(pkg, targetModules);
                        extraExports.put(pkg, targetModules);
                    }

                    redefineModule.invoke(inst, mod, Collections.emptySet(), extraExports, extraOpens, Collections.emptySet(), Collections.emptyMap());
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static Class<?> defineClassDirect(ClassLoader loader, String className, byte[] bytes) {
        if (loader == null || bytes == null) return null;
        try {
            Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
            defineClassMethod.setAccessible(true);
            return (Class<?>) defineClassMethod.invoke(loader, className, bytes, 0, bytes.length);
        } catch (Throwable ignored) {
            return null;
        }
    }
    public static File extractRuntimeJar(String profile) {
        File dir = new File(System.getProperty("user.home") + "/AppData/Roaming/.minecraft/valor");
        dir.mkdirs();
        File permanentJar = new File(dir, "valor-" + profile + ".jar");

        InputStream is = ValorAgent.class.getResourceAsStream("/runtimes/valor-" + profile + ".jar");
        if (is == null) {
            is = ValorAgent.class.getResourceAsStream("/runtimes/" + profile + ".jar");
        }
        if (is != null) {
            try {
                try (FileOutputStream fos = new FileOutputStream(permanentJar)) {
                    byte[] buf = new byte[8192];
                    int read;
                    while ((read = is.read(buf)) != -1) {
                        fos.write(buf, 0, read);
                    }
                }
                if (permanentJar.length() > 500000L) {
                    System.out.println("[Valor] Extracted embedded runtime JAR for " + profile + ": " + permanentJar.getAbsolutePath() + " (" + permanentJar.length() + " bytes)");
                    return permanentJar;
                }
            } catch (Exception ex) {
                System.err.println("[Valor] Failed to extract embedded runtime JAR: " + ex.getMessage());
            } finally {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
        if (permanentJar.exists() && permanentJar.length() > 500000L) {
            return permanentJar;
        }

        File[] candidates = new File[]{
                new File("runtimes/valor-" + profile + ".jar"),
                new File("build/libs/valor-1.0.0-" + profile + ".jar"),
                new File("build/libs/valor-1.0.0-" + profile + "-rt.jar"),
                new File("valor/reconstructed_src/build/libs/valor-1.0.0-" + profile + ".jar"),
                new File("valor/reconstructed_src/build/libs/valor-1.0.0-" + profile + "-rt.jar"),
                new File("src/main/resources/runtimes/valor-" + profile + ".jar"),
                new File("valor/reconstructed_src/src/main/resources/runtimes/valor-" + profile + ".jar"),
                new File("valor_agent_" + profile + "_8732_2353906.jar"),
                new File(System.getProperty("user.dir"), "valor_agent_" + profile + "_8732_2353906.jar"),
                new File(System.getProperty("user.dir"), "valor/reconstructed_src/src/main/resources/runtimes/valor-" + profile + ".jar"),
                new File(System.getProperty("user.dir"), "valor/reconstructed_src/build/libs/valor-1.0.0-" + profile + "-rt.jar")
        };
        for (File candidate : candidates) {
            if (candidate.isFile() && candidate.length() > 500000L) {
                System.out.println("[Valor] Found candidate runtime JAR for " + profile + ": " + candidate.getAbsolutePath());
                return candidate;
            }
        }
        return null;
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        logToFile("=== agentmain INVOKED (args: '" + agentArgs + "') ===");
        cleanTempFiles();
        try {
            doAgentMain(agentArgs, inst);
        } catch (Throwable t) {
            logToFile("CRITICAL agentmain UNHANDLED ERROR: " + t);
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            logToFile(sw.toString());
            System.err.println("[Valor] agentmain fatal: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static void doAgentMain(String agentArgs, Instrumentation inst) throws Exception {
        INSTRUMENTATION = inst;
        resolveAgentJarFile();
        openAllBootModules(inst);

        Class<?> aveClass = null;
        boolean isForge = false;
        ClassLoader forgeClassLoader = null;
        Class<?> mcClass = null;

        for (Class<?> loadedClass : inst.getAllLoadedClasses()) {
            String name = loadedClass.getName();
            if ("ave".equals(name)) {
                aveClass = loadedClass;
            } else if ("net.minecraftforge.common.MinecraftForge".equals(name) ||
                       "net.minecraftforge.fml.common.FMLCommonHandler".equals(name) ||
                       "net.minecraftforge.client.GuiIngameForge".equals(name) ||
                       "net.minecraft.launchwrapper.Launch".equals(name)) {
                isForge = true;
                if (forgeClassLoader == null && loadedClass.getClassLoader() != null) {
                    forgeClassLoader = loadedClass.getClassLoader();
                }
            } else if ("net.minecraft.client.Minecraft".equals(name)) {
                mcClass = loadedClass;
            }
        }

        if (mcClass != null && !isForge && aveClass == null) {
            boolean hasGetMinecraft = false;
            for (Method m : mcClass.getDeclaredMethods()) {
                if ("getMinecraft".equals(m.getName())) {
                    hasGetMinecraft = true;
                    break;
                }
            }
            if (!hasGetMinecraft) {
                isForge = true;
                if (forgeClassLoader == null) {
                    forgeClassLoader = mcClass.getClassLoader();
                }
            }
        }

        if (aveClass != null) {
            logToFile("Notch runtime detected (ave.class loaded). Loading Notch agent runtime...");
            System.setProperty("valor.runtime.profile", "notch");
            System.setProperty("valor.runtime.launcher", "badlion");

            File notchJar = extractRuntimeJar("notch");
            if (notchJar == null || !notchJar.isFile()) {
                if (agentJarFile != null && agentJarFile.isFile() && agentJarFile.getName().contains("notch")) {
                    notchJar = agentJarFile;
                }
            }
            if (notchJar == null || !notchJar.isFile()) {
                notchJar = agentJarFile;
            }

            if (notchJar != null && notchJar.isFile()) {
                resolvedRuntimeJar = notchJar;
                try {
                    java.lang.reflect.Field f = NativeBridge.class.getField("resolvedRuntimeJar");
                    f.set(null, notchJar);
                } catch (Throwable ignored) {
                }
                try {
                    inst.appendToSystemClassLoaderSearch(new JarFile(notchJar));
                    logToFile("Appended valor-notch.jar to system class loader search: " + notchJar.getName());
                } catch (Throwable t) {
                    logToFile("Note: appendToSystemClassLoaderSearch: " + t.getMessage());
                }
            }

            ClassLoader mcClassLoader = aveClass.getClassLoader();
            if (mcClassLoader == null) {
                mcClassLoader = ClassLoader.getSystemClassLoader();
            }
            NativeBridge.setGenesisClassLoader(mcClassLoader);
            openModuleForClassLoader(inst, mcClassLoader);
            try {
                ensureInjected(mcClassLoader);
            } catch (Throwable t) {
                logToFile("Note: ensureInjected: " + t.getMessage());
            }

            try {
                NativeBridge.injectIntoGenesis(mcClassLoader);
            } catch (Throwable t) {
                logToFile("Note: injectIntoGenesis: " + t.getMessage());
            }

            inst.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain pd, byte[] classfileBuffer) {
                    return NativeBridge.transform(className, classfileBuffer);
                }
            }, true);
            logToFile("Registered Notch delegating transformer with Instrumentation");

            try {
                NativeBridge.startValor();
                logToFile("NativeBridge.startValor() called for Notch runtime");
            } catch (Throwable t) {
                logToFile("startValor error: " + t.getMessage());
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                logToFile(sw.toString());
            }

            retransformClasses(inst, NOTCH_CLASSES_TO_RETRANSFORM);
            return;
        }

        if (isForge) {
            logToFile("Minecraft Forge runtime detected (SRG mappings). Loading Forge agent runtime...");
            System.setProperty("valor.runtime.profile", "forge");
            System.setProperty("valor.runtime.launcher", "forge");

            File forgeJar = extractRuntimeJar("forge");
            if (forgeJar == null || !forgeJar.isFile()) {
                if (agentJarFile != null && agentJarFile.isFile() && (agentJarFile.getName().contains("forge") || agentJarFile.getName().contains("universal"))) {
                    if (agentJarFile.getName().contains("forge")) {
                        forgeJar = agentJarFile;
                    }
                }
            }
            if (forgeJar == null || !forgeJar.isFile()) {
                forgeJar = agentJarFile;
            }

            if (forgeJar != null && forgeJar.isFile()) {
                resolvedRuntimeJar = forgeJar;
                try {
                    java.lang.reflect.Field f = NativeBridge.class.getField("resolvedRuntimeJar");
                    f.set(null, forgeJar);
                } catch (Throwable ignored) {
                }
                try {
                    inst.appendToSystemClassLoaderSearch(new JarFile(forgeJar));
                    logToFile("Appended valor-forge.jar to system class loader search: " + forgeJar.getName());
                } catch (Throwable t) {
                    logToFile("Note: appendToSystemClassLoaderSearch: " + t.getMessage());
                }
            }

            if (forgeClassLoader == null) {
                forgeClassLoader = NativeBridge.findLaunchClassLoader();
            }
            if (forgeClassLoader == null && mcClass != null) {
                forgeClassLoader = mcClass.getClassLoader();
            }
            if (forgeClassLoader == null) {
                forgeClassLoader = ClassLoader.getSystemClassLoader();
            }
            NativeBridge.setGenesisClassLoader(forgeClassLoader);
            openModuleForClassLoader(inst, forgeClassLoader);

            try {
                ensureInjected(forgeClassLoader);
            } catch (Throwable t) {
                logToFile("Note: ensureInjected: " + t.getMessage());
            }

            try {
                NativeBridge.injectIntoGenesis(forgeClassLoader);
            } catch (Throwable t) {
                logToFile("Note: injectIntoGenesis: " + t.getMessage());
            }

            registerTransformers(inst);

            try {
                NativeBridge.startValor();
                logToFile("NativeBridge.startValor() called for Forge runtime");
            } catch (Throwable t) {
                logToFile("startValor error: " + t.getMessage());
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                logToFile(sw.toString());
            }

            retransformClasses(inst, CLASSES_TO_RETRANSFORM);
            return;
        }

        logToFile("Standard MCP / Lunar runtime detected.");
        System.setProperty("valor.runtime.profile", "mcp");
        System.setProperty("valor.runtime.launcher", "lunar");

        File mcpJar = extractRuntimeJar("mcp");
        if (mcpJar == null || !mcpJar.isFile()) {
            mcpJar = agentJarFile;
        }
        if (mcpJar != null && mcpJar.isFile()) {
            resolvedRuntimeJar = mcpJar;
            try {
                java.lang.reflect.Field f = NativeBridge.class.getField("resolvedRuntimeJar");
                f.set(null, mcpJar);
            } catch (Throwable ignored) {
            }
            try {
                inst.appendToSystemClassLoaderSearch(new JarFile(mcpJar));
                logToFile("Appended MCP agent JAR to system class loader search: " + mcpJar.getName());
            } catch (Throwable t) {
                logToFile("Note: appendToSystemClassLoaderSearch: " + t.getMessage());
            }
        }

        ClassLoader mcClassLoader = (mcClass != null && mcClass.getClassLoader() != null) ? mcClass.getClassLoader() : Thread.currentThread().getContextClassLoader();
        if (mcClassLoader == null) {
            mcClassLoader = ClassLoader.getSystemClassLoader();
        }
        NativeBridge.setGenesisClassLoader(mcClassLoader);
        openModuleForClassLoader(inst, mcClassLoader);
        try {
            ensureInjected(mcClassLoader);
        } catch (Throwable ignored) {
        }
        try {
            NativeBridge.injectIntoGenesis(mcClassLoader);
        } catch (Throwable ignored) {
        }

        registerTransformers(inst);
        try {
            NativeBridge.startValor();
        } catch (Throwable t) {
            logToFile("startValor failed: " + t.getMessage());
            t.printStackTrace();
        }
        retransformClasses(inst, CLASSES_TO_RETRANSFORM);
    }

    private static void retransformClasses(Instrumentation inst, String[] classNames) {
        Set<String> targetNames = new HashSet<String>(Arrays.asList(classNames));
        List<Class<?>> classesToRetransform = new ArrayList<Class<?>>();
        for (Class<?> loadedClass : inst.getAllLoadedClasses()) {
            if (targetNames.contains(loadedClass.getName()) && inst.isModifiableClass(loadedClass)) {
                classesToRetransform.add(loadedClass);
            }
        }
        int successCount = 0;
        for (Class<?> loadedClass : classesToRetransform) {
            try {
                inst.retransformClasses(new Class<?>[]{loadedClass});
                successCount++;
                logToFile("Successfully retransformed " + loadedClass.getName());
            } catch (Throwable t) {
                logToFile("Retransform failed for " + loadedClass.getName() + ": " + t);
            }
        }
        logToFile("Retransformed " + successCount + "/" + classesToRetransform.size() + " classes");
    }

    private static void registerTransformers(Instrumentation inst) throws Exception {
        try {
            agentJarFile = new File(ValorAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            agentJarFile = null;
        }
        inst.addTransformer(new MinecraftTransformer(), true);
        inst.addTransformer(new EntityTransformer(), true);
        inst.addTransformer(new EntityLivingBaseTransformer(), true);
        inst.addTransformer(new EntityPlayerTransformer(), true);
        inst.addTransformer(new EntityPlayerSPTransformer(), true);
        inst.addTransformer(new AbstractClientPlayerTransformer(), true);
        inst.addTransformer(new EntityRendererTransformer(), true);
        inst.addTransformer(new NetworkManagerTransformer(), true);
        inst.addTransformer(new PlayerControllerMPTransformer(), true);
        inst.addTransformer(new KeyBindingTransformer(), true);
        inst.addTransformer(new RendererLivingEntityTransformer(), true);
        inst.addTransformer(new GuiIngameTransformer(), true);
        inst.addTransformer(new GuiIngameForgeTransformer(), true);
        inst.addTransformer(new RenderManagerTransformer(), true);
        inst.addTransformer(new RenderTransformer(), true);
        inst.addTransformer(new RenderPlayerTransformer(), true);
        inst.addTransformer(new RenderGlobalTransformer(), true);
        inst.addTransformer(new MovementInputFromOptionsTransformer(), true);
        inst.addTransformer(new FontRendererTransformer(), true);
        inst.addTransformer(new BlockTransformer(), true);
        inst.addTransformer(new BlockModelRendererTransformer(), true);
        inst.addTransformer(new BlockRendererDispatcherTransformer(), true);
        inst.addTransformer(new WorldRendererTransformer(), true);
        inst.addTransformer(new VisGraphTransformer(), true);
        inst.addTransformer(new WorldTransformer(), true);
        inst.addTransformer(new ItemStackTransformer(), true);
        inst.addTransformer(new DisplayTransformer(), true);
        System.out.println("[Valor] Agent loaded with 27 transformers");
    }
    public static boolean isInjected(ClassLoader classLoader) {
        return classLoader == null || INJECTED_CLASS_LOADERS.contains(classLoader);
    }
    public static synchronized void ensureInjected(ClassLoader targetClassLoader) {
        if (targetClassLoader == null || INJECTED_CLASS_LOADERS.contains(targetClassLoader)) {
            return;
        }
        resolveAgentJarFile();
        if (INSTRUMENTATION != null) {
            openAllBootModules(INSTRUMENTATION);
            openModuleForClassLoader(INSTRUMENTATION, targetClassLoader);
        }
        File jarToInject = resolvedRuntimeJar != null ? resolvedRuntimeJar : agentJarFile;
        if (targetClassLoader instanceof URLClassLoader) {
            try {
                Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
                if (jarToInject != null) {
                    addUrlMethod.invoke(targetClassLoader, jarToInject.toURI().toURL());
                    System.out.println("[Valor] Injected into URLClassLoader: " + targetClassLoader + " (using " + jarToInject.getName() + ")");

                }
            } catch (Exception e) {
                System.err.println("[Valor] URLClassLoader addURL failed, falling back to defineClass");
            }
        }
        if (jarToInject == null) {
            System.err.println("[Valor] Cannot inject: agent JAR path unknown");
            return;
        }
        try (JarFile jarFile = new JarFile(jarToInject)) {
            List<String> valorClassEntries = new ArrayList<String>();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("valor/") && name.endsWith(".class")) {
                    valorClassEntries.add(name);
                }
            }
            Method defineClassMethod = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE
            );
            defineClassMethod.setAccessible(true);
            Map<String, byte[]> pendingClasses = new LinkedHashMap<String, byte[]>();
            for (String entryPath : valorClassEntries) {
                String className = entryPath.replace('/', '.').replace(".class", "");
                try {
                    Class.forName(className, false, targetClassLoader);
                } catch (Throwable e) {
                    JarEntry classEntry = jarFile.getJarEntry(entryPath);
                    try (InputStream is = jarFile.getInputStream(classEntry)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, read);
                        }
                        byte[] classBytes = baos.toByteArray();
                        try {
                            pendingClasses.put(className, classBytes);
                        } catch (Exception defineEx) {
                            System.err.println("[Valor] Failed to define " + className + ": " + defineEx.getMessage());
                        }
                    }
                }
            }
            int totalDefined = 0;
            for (int pass = 0; pass < 10 && !pendingClasses.isEmpty(); pass++) {
                int definedThisPass = 0;
                Iterator<Map.Entry<String, byte[]>> it = pendingClasses.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, byte[]> entry = it.next();
                    String cName = entry.getKey();
                    byte[] cBytes = entry.getValue();
                    try {
                        defineClassMethod.invoke(targetClassLoader, cName, cBytes, 0, cBytes.length);
                        it.remove();
                        definedThisPass++;
                        totalDefined++;
                    } catch (InvocationTargetException ite) {
                        Throwable cause = ite.getCause();
                        if (cause instanceof LinkageError) {
                            it.remove();
                        }
                    } catch (Throwable ignored) {
                    }
                }
                if (definedThisPass == 0) {
                    break;
                }
            }
            System.out.println("[Valor] Successfully injected " + totalDefined + " classes directly into " + targetClassLoader.getClass().getName() + " (" + pendingClasses.size() + " remaining)");
            if (!pendingClasses.isEmpty()) {
                System.err.println("[Valor] Warning: could not define: " + pendingClasses.keySet());
            }
            INJECTED_CLASS_LOADERS.add(targetClassLoader);
        } catch (Exception e) {
            System.err.println("[Valor] Injection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void nativeInject() throws Exception {
        logNative("[Valor] ========== nativeInject() START ==========");
        String jarPath;
        try {
            jarPath = new File(ValorAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (Exception e) {
            jarPath = "C:\\Users\\begly\\Documents\\OpenMyau\\build\\libs\\Valor-1.0.0.jar";
        }
        logNative("[Valor] Agent JAR: " + jarPath);
        logNative("[Valor] Calling loadAgent0 via Unsafe method dispatch...");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        Method loadAgentMethod = classLoader.loadClass("sun.instrument.InstrumentationImpl").getDeclaredMethod("loadAgent", String.class);
        Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafeField.get(null);
        Field overrideField = AccessibleObject.class.getDeclaredField("override");
        long overrideOffset = unsafe.objectFieldOffset(overrideField);
        unsafe.putBoolean(loadAgentMethod, overrideOffset, true);
        logNative("[Valor] Overriding access, calling loadAgent...");
        loadAgentMethod.invoke(null, jarPath);
        logNative("[Valor] loadAgent completed!");
        logNative("[Valor] ========== nativeInject() END ==========");
    }
    private static void logNative(String message) {
        System.out.println(message);
        try (PrintWriter pw = new PrintWriter(new FileWriter("valor_native_log.txt", true))) {
            pw.println("[" + System.currentTimeMillis() + "] " + message);
        } catch (Exception ignored) {
        }
    }
    public static class MinecraftProcess {
        public final int pid;
        public final String clientType;
        public final String username;
        public final String version;
        public final String commandLine;

        public MinecraftProcess(int pid, String clientType, String username, String version, String commandLine) {
            this.pid = pid;
            this.clientType = clientType;
            this.username = username;
            this.version = version;
            this.commandLine = commandLine;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PID ").append(pid).append(" - ").append(clientType);
            if (username != null && !username.isEmpty()) {
                sb.append(" (Player: ").append(username).append(")");
            }
            if (version != null && !version.isEmpty()) {
                sb.append(" [").append(version).append("]");
            }
            return sb.toString();
        }
    }

    public static List<MinecraftProcess> findMinecraftProcesses() {
        List<MinecraftProcess> list = new ArrayList<MinecraftProcess>();
        try {
            String psCommand = "Get-CimInstance Win32_Process | Where-Object {$_.Name -eq 'java.exe' -or $_.Name -eq 'javaw.exe'} | ForEach-Object { $_.ProcessId.ToString() + '|||' + $_.CommandLine }";
            ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-Command", psCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            List<String[]> rawList = new ArrayList<String[]>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    int separatorIndex = line.indexOf("|||");
                    if (!line.isEmpty() && separatorIndex > 0) {
                        String pidStr = line.substring(0, separatorIndex).trim();
                        String cmd = line.substring(separatorIndex + 3);
                        rawList.add(new String[]{pidStr, cmd});
                    }
                }
            }
            process.waitFor();

            for (String[] proc : rawList) {
                int pid;
                try {
                    pid = Integer.parseInt(proc[0]);
                } catch (NumberFormatException ignored) {
                    continue;
                }
                String cmd = proc[1];
                String cmdLower = cmd.toLowerCase();

                boolean isMc = cmdLower.contains("minecraft")
                        || cmdLower.contains("lunar")
                        || cmdLower.contains("--gamedir")
                        || cmdLower.contains("--username")
                        || cmdLower.contains("net.minecraft")
                        || cmdLower.contains("launchwrapper")
                        || cmdLower.contains("feather")
                        || cmdLower.contains("badlion");

                if (!isMc) {
                    continue;
                }

                String type = "Minecraft Java Instance";
                if (cmdLower.contains("badlion") || cmdLower.contains("blclient")) {
                    type = "Badlion Client";
                } else if (cmdLower.contains("feather")) {
                    type = "Feather Client";
                } else if (cmdLower.contains("lunar")) {
                    type = "Lunar Client";
                } else if (cmdLower.contains("fmltweaker") || cmdLower.contains("minecraftforge") || cmdLower.contains("forge")) {
                    type = "Minecraft Forge";
                } else if (cmdLower.contains("optifine")) {
                    type = "Minecraft (OptiFine)";
                } else if (cmdLower.contains("net.minecraft.client.main.main")) {
                    type = "Vanilla / Official Launcher";
                }

                String username = parseArg(cmd, "--username");
                String version = parseArg(cmd, "--version");

                list.add(new MinecraftProcess(pid, type, username, version, cmd));
            }
        } catch (Exception e) {
            System.err.println("[Valor] Error finding processes: " + e.getMessage());
        }
        return list;
    }

    private static String parseArg(String cmd, String flag) {
        int idx = cmd.toLowerCase().indexOf(flag.toLowerCase());
        if (idx == -1) return null;
        String rest = cmd.substring(idx + flag.length()).trim();
        if (rest.isEmpty()) return null;
        if (rest.charAt(0) == '\"') {
            int endQuote = rest.indexOf('"', 1);
            if (endQuote != -1) {
                return rest.substring(1, endQuote);
            }
        }
        int space = rest.indexOf(' ');
        if (space != -1) {
            return rest.substring(0, space);
        }
        return rest;
    }

    private static int findMinecraftProcessId() {
        List<MinecraftProcess> list = findMinecraftProcesses();
        if (list.isEmpty()) {
            return -1;
        }
        for (MinecraftProcess p : list) {
            if ("Lunar Client".equals(p.clientType) || "Badlion Client".equals(p.clientType)) {
                return p.pid;
            }
        }
        for (MinecraftProcess p : list) {
            if ("Minecraft Forge".equals(p.clientType)) {
                return p.pid;
            }
        }
        return list.get(0).pid;
    }

    public static boolean inject(int pid, String jarPath) {
        System.out.println("[Valor] Attaching to PID " + pid + "...");
        String targetJarPath = jarPath;
        try {
            List<MinecraftProcess> procs = findMinecraftProcesses();
            for (MinecraftProcess p : procs) {
                if (p.pid == pid) {
                    if ("Badlion Client".equals(p.clientType)) {
                        System.out.println("[Valor] Target PID " + pid + " is Badlion Client! Selecting Notch agent runtime...");
                        File notchJar = extractRuntimeJar("notch");
                        if (notchJar != null && notchJar.isFile()) {
                            targetJarPath = notchJar.getAbsolutePath();
                            System.out.println("[Valor] Extracted Notch agent: " + targetJarPath);
                        }
                    } else if ("Minecraft Forge".equals(p.clientType)) {
                        System.out.println("[Valor] Target PID " + pid + " is Minecraft Forge! Selecting Forge agent runtime...");
                        File forgeJar = extractRuntimeJar("forge");
                        if (forgeJar != null && forgeJar.isFile()) {
                            targetJarPath = forgeJar.getAbsolutePath();
                            System.out.println("[Valor] Extracted Forge agent: " + targetJarPath);
                        }
                    } else if ("Lunar Client".equals(p.clientType)) {
                        System.out.println("[Valor] Target PID " + pid + " is Lunar Client! Selecting MCP agent runtime...");
                        File mcpJar = extractRuntimeJar("mcp");
                        if (mcpJar != null && mcpJar.isFile()) {
                            targetJarPath = mcpJar.getAbsolutePath();
                            System.out.println("[Valor] Extracted MCP agent: " + targetJarPath);
                        }
                    }
                    break;
                }
            }
        } catch (Throwable t) {
            System.err.println("[Valor] Note during process check: " + t.getMessage());
        }
        boolean attached = false;
        try {
            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            Object vmInstance = vmClass.getMethod("attach", String.class).invoke(null, String.valueOf(pid));
            try {
                vmClass.getMethod("loadAgent", String.class).invoke(vmInstance, targetJarPath);
                attached = true;
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause != null && cause.getMessage() != null &&
                    (cause.getMessage().contains(": 0") || cause.getMessage().trim().equals("0"))) {
                    System.out.println("[Valor] Note: Target VM confirmed agent loaded (code 0).");
                    attached = true;
                } else {
                    throw ite;
                }
            }
            try {
                vmClass.getMethod("detach").invoke(vmInstance);
            } catch (Exception ignored) {
            }
        } catch (Throwable attachEx) {
            Throwable cause = attachEx.getCause() != null ? attachEx.getCause() : attachEx;
            System.out.println("[Valor] Standard attach failed (" + cause.getMessage() + ").");
        }

        if (attached) {
            System.out.println("[Valor] Successfully injected via standard attach!");
            return true;
        }

        System.out.println("[Valor] Going straight to Native DLL Injection...");
        try {
            return NativeInjector.inject(pid, targetJarPath, "valor_injector.dll");
        } catch (Throwable t) {
            System.err.println("[Valor] Native DLL injection fallback error: " + t.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("  _    _       ");
        System.out.println(" | |  | |      ");
        System.out.println(" | |__| | __ _  ___  ___  __ _ _   _  __ _ ");
        System.out.println(" |  __  |/ _` |/ __|/ _ \\/ _` | | | |/ _` |");
        System.out.println(" | |  | | (_| | (__| (_) | (_| | |_| | (_| |");
        System.out.println(" |_|  |_|\\__,_|\\___|\\___/ \\__, |\\__,_|\\__,_|");
        System.out.println("                           __/ |           ");
        System.out.println("                          |___/            ");
        System.out.println();
        String jarPath;
        try {
            jarPath = new File(ValorAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (Exception e) {
            System.err.println("[Valor] Cannot find own JAR path");
            return;
        }
        System.out.println("[Valor] Agent: " + jarPath);
        cleanTempFiles();

        int targetPid = -1;
        boolean forceGui = false;
        boolean forceCli = false;

        String forceProfile = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if ("--profile".equalsIgnoreCase(arg) && i + 1 < args.length) {
                forceProfile = args[++i].trim();
            } else if ("--gui".equalsIgnoreCase(arg)) {
                forceGui = true;
            } else if ("--cli".equalsIgnoreCase(arg)) {
                forceCli = true;
            } else if ("--pid".equalsIgnoreCase(arg) && i + 1 < args.length) {
                try {
                    targetPid = Integer.parseInt(args[++i].trim());
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    targetPid = Integer.parseInt(arg);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if ("notch".equalsIgnoreCase(forceProfile)) {
            File nJar = extractRuntimeJar("notch");
            if (nJar != null && nJar.isFile()) {
                jarPath = nJar.getAbsolutePath();
                System.out.println("[Valor] Force-selected Notch runtime JAR: " + jarPath);
            }
        } else if ("forge".equalsIgnoreCase(forceProfile)) {
            File fJar = extractRuntimeJar("forge");
            if (fJar != null && fJar.isFile()) {
                jarPath = fJar.getAbsolutePath();
                System.out.println("[Valor] Force-selected Forge runtime JAR: " + jarPath);
            }
        } else if ("mcp".equalsIgnoreCase(forceProfile) || "lunar".equalsIgnoreCase(forceProfile)) {
            File mJar = extractRuntimeJar("mcp");
            if (mJar != null && mJar.isFile()) {
                jarPath = mJar.getAbsolutePath();
                System.out.println("[Valor] Force-selected MCP runtime JAR: " + jarPath);
            }
        }
        if (targetPid != -1) {
            System.out.println("[Valor] Target PID specified: " + targetPid);
            inject(targetPid, jarPath);
            return;
        }

        if (!forceCli && !GraphicsEnvironment.isHeadless()) {
            System.out.println("[Valor] Launching GUI launcher...");
            ValorLauncher.showGui(jarPath);
            return;
        }

        List<MinecraftProcess> processes = findMinecraftProcesses();
        if (processes.isEmpty()) {
            System.err.println("[Valor] No Minecraft/Lunar process found. Start the game first.");
            return;
        }

        int selectedPid = -1;
        if (processes.size() == 1) {
            MinecraftProcess p = processes.get(0);
            System.out.println("[Valor] Found 1 running Minecraft instance: " + p);
            selectedPid = p.pid;
        } else {
            System.out.println("[Valor] Multiple Minecraft instances detected:");
            for (int i = 0; i < processes.size(); i++) {
                System.out.println("  [" + (i + 1) + "] " + processes.get(i));
            }
            System.out.print("[Valor] Select an instance [1-" + processes.size() + "] or 'q' to quit: ");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String input = reader.readLine();
                if (input == null || "q".equalsIgnoreCase(input.trim())) {
                    System.out.println("[Valor] Cancelled.");
                    return;

                }
                int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= processes.size()) {
                    selectedPid = processes.get(choice - 1).pid;
                } else {
                    System.err.println("[Valor] Invalid selection.");
                    return;

                }
            } catch (Exception e) {
                System.err.println("[Valor] Input error: " + e.getMessage());
                return;
            }
        }

        if (selectedPid != -1) {
            inject(selectedPid, jarPath);
        }
    }
}
