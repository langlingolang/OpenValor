package valor.agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import valor.agent.BaseTransformer;
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

public class NativeBridge {
    private static final Map<String, BaseTransformer> transformers = new HashMap<String, BaseTransformer>();
    private static final Set<String> transformedClasses = new HashSet<String>();
    private static ClassLoader agentClassLoader;
    public static ClassLoader genesisClassLoader;
    private static Method customTransformMethod;
    public static File resolvedRuntimeJar;

    private static void log(String string) {
        try {
            String path = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\valor_inject.log";
            FileWriter writer = new FileWriter(path, true);
            writer.write("[Valor] " + string + "\n");
            writer.flush();
            writer.close();
        } catch (Exception ignored) {
        }
    }

    public static ClassLoader getClassLoader() {
        return agentClassLoader;
    }

    public static ClassLoader getGenesisClassLoader() {
        return genesisClassLoader;
    }

    public static void setGenesisClassLoader(ClassLoader classLoader) {
        genesisClassLoader = classLoader;
    }

    public static void init() {
        agentClassLoader = NativeBridge.class.getClassLoader();
        System.out.println("[Valor] NativeBridge.init() cl=" + agentClassLoader);
    }

    public static void injectIntoGenesis(ClassLoader genesisCL) {
        genesisClassLoader = genesisCL;
        System.out.println("[Valor] injectIntoGenesis() target=" + genesisCL);
        try {
            File agentJar = null;
            String runtimeProfile = System.getProperty("valor.runtime.profile");
            if (runtimeProfile == null || runtimeProfile.isEmpty()) {
                runtimeProfile = detectRuntimeProfile(genesisCL);
            }
            log("Resolved runtime profile: " + runtimeProfile);
            String valorJarProp = System.getProperty("valor.jar");
            if (valorJarProp != null && !valorJarProp.isEmpty()) {
                File file = new File(valorJarProp);
                if (file.isFile()) {
                    agentJar = file;
                }
            }
            if (agentJar == null) {
                String profileJarProp = System.getProperty("notch".equals(runtimeProfile) ? "valor.notch.jar" : "valor.forge.jar");
                if (profileJarProp != null && !profileJarProp.isEmpty()) {
                    File file = new File(profileJarProp);
                    if (file.isFile()) {
                        agentJar = file;
                    }
                }
            }
            if (agentJar == null && NativeBridge.class.getProtectionDomain() != null
                    && NativeBridge.class.getProtectionDomain().getCodeSource() != null) {
                URL loc = NativeBridge.class.getProtectionDomain().getCodeSource().getLocation();
                if (loc != null) {
                    agentJar = new File(loc.toURI());
                }
            }
            if (agentJar == null || !agentJar.isFile()) {
                log("Could not resolve the Valor agent JAR path");
                return;
            }

            URL jarUrl = agentJar.toURI().toURL();
            log("JAR URL: " + jarUrl);
            log("JAR file path: " + agentJar.getAbsolutePath());
            try {
                System.setProperty("valor.jar", agentJar.getAbsolutePath());
            } catch (Throwable ignored) {
            }

            if (!"mcp".equals(runtimeProfile)) {
                File runtimeJar = null;

                InputStream is = NativeBridge.class.getResourceAsStream("/runtimes/valor-" + runtimeProfile + ".jar");
                if (is == null) {
                    is = NativeBridge.class.getResourceAsStream("/runtimes/" + runtimeProfile + ".jar");
                }
                if (is != null) {
                    try {
                        File dir = new File(System.getProperty("user.home") + "/AppData/Roaming/.minecraft/valor");
                        dir.mkdirs();
                        File permanentJar = new File(dir, "valor-" + runtimeProfile + ".jar");
                        try (FileOutputStream fos = new FileOutputStream(permanentJar)) {
                            byte[] buf = new byte[8192];
                            int read;
                            while ((read = is.read(buf)) != -1) {
                                fos.write(buf, 0, read);
                            }
                        }
                        if (permanentJar.length() > 1000000L) {
                            runtimeJar = permanentJar;
                            log("Extracted embedded runtime JAR to: " + permanentJar.getAbsolutePath() + " (size: " + permanentJar.length() + ")");
                        }
                    } catch (Exception ex) {
                        log("Failed to extract embedded runtime JAR: " + ex);
                    } finally {
                        try {
                            is.close();
                        } catch (Exception ignored) {
                        }
                    }
                }

                if (runtimeJar == null) {
                    String profileJarProp = System.getProperty("notch".equals(runtimeProfile) ? "valor.notch.jar" : "valor.forge.jar");
                    if (profileJarProp != null && !profileJarProp.isEmpty()) {
                        File file = new File(profileJarProp);
                        if (file.isFile() && file.length() > 1000000L) {
                            runtimeJar = file;
                        }
                    }
                }
                if (runtimeJar == null) {
                    String[] suffixes = "notch".equals(runtimeProfile) ? new String[]{"-notch.jar"} : new String[]{"-forge-rt.jar", "-forge.jar"};
                    String jarPath = agentJar.getAbsolutePath();
                    for (String suffix : suffixes) {
                        if (jarPath.endsWith(".jar")) {
                            File candidate = new File(jarPath.substring(0, jarPath.length() - 4) + suffix);
                            if (candidate.isFile() && candidate.length() > 1000000L) {
                                runtimeJar = candidate;
                                break;
                            }
                        }
                        if (runtimeJar == null && agentJar.getParentFile() != null) {
                            File candidate = new File(agentJar.getParentFile(), "valor-1.0.0" + suffix);
                            if (candidate.isFile() && candidate.length() > 1000000L) {
                                runtimeJar = candidate;
                                break;
                            }
                        }
                    }
                }

                if (runtimeJar != null) {
                    resolvedRuntimeJar = runtimeJar;
                    URL runtimeJarUrl = runtimeJar.toURI().toURL();
                    log("Using " + runtimeProfile + " runtime JAR: " + runtimeJarUrl);
                    if ("notch".equals(runtimeProfile)) {
                        try {
                            URLClassLoader childClassLoader = new URLClassLoader(new URL[]{runtimeJarUrl}, NativeBridge.class.getClassLoader().getParent());
                            ValorAgent.openModuleForClassLoader(ValorAgent.INSTRUMENTATION, childClassLoader);
                            Class<?> bridgeClass = Class.forName("valor.agent.NativeBridge", true, childClassLoader);
                            bridgeClass.getMethod("setGenesisClassLoader", ClassLoader.class).invoke(null, genesisClassLoader);
                            customTransformMethod = bridgeClass.getMethod("transform", String.class, byte[].class);
                            log("Loaded " + runtimeProfile + " transformer bridge from " + runtimeJar.getName());
                        } catch (Exception ex) {
                            log("reloadTransformers failed for " + runtimeProfile + ": " + ex);
                        }
                    }
                } else {
                    log("Runtime JAR not found for profile " + runtimeProfile);
                }
            }

                        try {
                java.lang.reflect.Field invalidClassesField = genesisCL.getClass().getDeclaredField("invalidClasses");
                invalidClassesField.setAccessible(true);
                java.util.Set<?> invalidClasses = (java.util.Set<?>) invalidClassesField.get(genesisCL);
                if (invalidClasses != null) {
                    java.util.Iterator<?> it = invalidClasses.iterator();
                    while (it.hasNext()) {
                        Object item = it.next();
                        if (item != null && item.toString().startsWith("valor.")) {
                            it.remove();
                        }
                    }
                    log("Cleared valor classes from genesisCL invalidClasses");
                }
            } catch (Throwable ignored) {
            }

            try {
                java.lang.reflect.Field cachedClassesField = genesisCL.getClass().getDeclaredField("cachedClasses");
                cachedClassesField.setAccessible(true);
                java.util.Map<?, ?> cachedClasses = (java.util.Map<?, ?>) cachedClassesField.get(genesisCL);
                if (cachedClasses != null) {
                    java.util.Iterator<?> it = cachedClasses.keySet().iterator();
                    while (it.hasNext()) {
                        Object key = it.next();
                        if (key != null && key.toString().startsWith("valor.")) {
                            it.remove();
                        }
                    }
                    log("Cleared valor classes from genesisCL cachedClasses");
                }
            } catch (Throwable ignored) {
            }

            URL urlToAdd = (resolvedRuntimeJar != null) ? resolvedRuntimeJar.toURI().toURL() : jarUrl;
            try {
                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                addURL.invoke(genesisCL, urlToAdd);
                log("Added JAR URL to genesis CL: " + urlToAdd);
            } catch (Throwable t) {
                log("Could not add JAR URL to genesis CL: " + t.getMessage());
            }

            boolean hasAsm = isClassLoaded("org.objectweb.asm.ClassReader", genesisCL) || isClassLoaded("valor.o0O.lol", genesisCL);
            boolean hasJna = isClassLoaded("com.sun.jna.Native", genesisCL) || isClassLoaded("valor.o0O.Oil", genesisCL);
            if (hasAsm && hasJna) {
                log("Runtime dependencies already visible");
                return;
            }

            Method defineClassMethod;
            try {
                defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
                defineClassMethod.setAccessible(true);
            } catch (Throwable t) {
                log("Dependency fallback unavailable: " + t.getClass().getSimpleName());
                return;
            }

            Map<String, byte[]> fallbackClasses = new LinkedHashMap<String, byte[]>();
            try {
                JarFile jarFile = new JarFile(agentJar);
                try {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if ((!hasAsm && name.startsWith("org/objectweb/asm/")) || (!hasJna && name.startsWith("com/sun/jna/"))) {
                            if (name.endsWith(".class")) {
                                InputStream is = jarFile.getInputStream(entry);
                                try {
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    byte[] buf = new byte[8192];
                                    int read;
                                    while ((read = is.read(buf)) != -1) {
                                        baos.write(buf, 0, read);
                                    }
                                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                                    fallbackClasses.put(className, baos.toByteArray());
                                } finally {
                                    is.close();
                                }
                            }
                        }
                    }
                } finally {
                    jarFile.close();
                }
            } catch (Exception ex) {
                log("Could not read dependency fallback classes: " + ex);
                return;
            }

            int definedCount = 0;
            for (int round = 0; round < 6 && !fallbackClasses.isEmpty(); ++round) {
                int roundDefined = 0;
                Iterator<Map.Entry<String, byte[]>> it = fallbackClasses.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, byte[]> entry = it.next();
                    if (isClassLoaded(entry.getKey(), genesisCL)) {
                        it.remove();
                        continue;
                    }
                    try {
                        byte[] classBytes = entry.getValue();
                        Class<?> def = ValorAgent.defineClassDirect(genesisCL, entry.getKey(), classBytes); if (def == null && defineClassMethod != null) { defineClassMethod.invoke(genesisCL, entry.getKey(), classBytes, 0, classBytes.length); }
                        it.remove();
                        roundDefined++;
                        definedCount++;
                    } catch (Throwable ignored) {
                    }
                }
                if (roundDefined == 0) {
                    break;
                }
            }
            log("Dependency fallback defined " + definedCount + " classes, " + fallbackClasses.size() + " unresolved");
        } catch (Exception ex) {
            log("injectIntoGenesis failed: " + ex);
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            log(sw.toString());
        }
    }

    private static boolean isClassLoaded(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void startHex() {
        startValor();
    }

    public static void startValor() {
        log("startValor() called, genesisCL=" + genesisClassLoader);
        if (genesisClassLoader == null) {
            ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
            if (contextCL != null) {
                try {
                    Class.forName(getMinecraftClassName(), false, contextCL);
                    genesisClassLoader = contextCL;
                    log("Using context classloader as genesis: " + contextCL);
                    injectIntoGenesis(genesisClassLoader);
                } catch (ClassNotFoundException e) {
                    log("Context CL can't see MC, trying LaunchClassLoader");
                    genesisClassLoader = findLaunchClassLoader();
                    if (genesisClassLoader != null) {
                        log("Found Forge LaunchClassLoader: " + genesisClassLoader);
                        injectIntoGenesis(genesisClassLoader);
                    }
                }
            }
            if (genesisClassLoader == null) {
                log("No Minecraft classloader available; Valor startup deferred");
                return;
            }
        } else {
            injectIntoGenesis(genesisClassLoader);
        }

        final ClassLoader targetCL = genesisClassLoader;
        new Thread(() -> {
            try {
                Class<?> mcClass = Class.forName(getMinecraftClassName(), true, targetCL);
                log("MC class loaded: " + mcClass.getName() + " from " + mcClass.getClassLoader());

                Method getMcMethod = findMethod(mcClass, "getMinecraft", "func_71410_x", "A");
                log("getMc method: " + getMcMethod);
                if (getMcMethod == null) {
                    log("FATAL: Neither getMinecraft nor func_71410_x found!");
                    return;
                }

                Method addTaskMethod = findMethod(mcClass, "addScheduledTask", "func_152344_a", "a", Runnable.class);
                log("addTask method: " + addTaskMethod);
                if (addTaskMethod == null) {
                    log("FATAL: Neither addScheduledTask nor func_152344_a found!");
                    return;
                }

                Class<?> valorRunnableClass = Class.forName("valor.agent.ValorRunnable", true, targetCL);
                log("ValorRunnable class: " + valorRunnableClass.getName() + " from " + valorRunnableClass.getClassLoader());
                log("Polling for Minecraft instance...");

                Object mcInstance = null;
                for (int i = 0; i < 600 && mcInstance == null; ++i) {
                    try {
                        mcInstance = getMcMethod.invoke(null);
                    } catch (Exception ex) {
                        if (i == 0) {
                            log("getMc invoke error: " + ex.getMessage());
                        }
                    }
                    if (mcInstance != null) {
                        break;
                    }
                    Thread.sleep(100L);
                }

                if (mcInstance == null) {
                    log("Minecraft instance never appeared after 60s");
                    return;
                }

                log("Minecraft found: " + mcInstance + ", scheduling ValorRunnable");
                Object runnableInstance = valorRunnableClass.getConstructor().newInstance();
                addTaskMethod.invoke(mcInstance, runnableInstance);
                log("ValorRunnable scheduled successfully");
            } catch (Throwable t) {
                log("startValor scheduling FAILED: " + t);
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                log(sw.toString());
            }
        }, "Valor-Init").start();
    }

    private static Method findMethod(Class<?> clazz, String name1, String name2, String name3, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(name1, parameterTypes);
        } catch (NoSuchMethodException e1) {
            try {
                return clazz.getMethod(name2, parameterTypes);
            } catch (NoSuchMethodException e2) {
                try {
                    return clazz.getMethod(name3, parameterTypes);
                } catch (NoSuchMethodException e3) {
                    return null;
                }
            }
        }
    }

    private static String getMinecraftClassName() {
        if ("notch".equals(System.getProperty("valor.runtime.profile"))
                || (genesisClassLoader != null && isClassLoaded("ave", genesisClassLoader))) {
            return "ave";
        }
        return "net.minecraft.client.Minecraft";
    }

    private static String detectRuntimeProfile(ClassLoader cl) {
        if ("notch".equals(System.getProperty("valor.runtime.profile"))) {
            return "notch";
        }
        if ("forge".equals(System.getProperty("valor.runtime.profile"))) {
            return "forge";
        }
        if (cl != null && isClassLoaded("ave", cl)) {
            return "notch";
        }
        if (cl != null && (isClassLoaded("net.minecraftforge.common.MinecraftForge", cl)
                || isClassLoaded("net.minecraftforge.fml.common.FMLCommonHandler", cl)
                || isClassLoaded("net.minecraftforge.client.GuiIngameForge", cl)
                || isClassLoaded("net.minecraft.launchwrapper.Launch", cl))) {
            return "forge";
        }
        return "mcp";
    }

    public static ClassLoader findLaunchClassLoader() {
        try {
            Field field = Class.forName("net.minecraft.launchwrapper.Launch").getDeclaredField("classLoader");
            field.setAccessible(true);
            Object obj = field.get(null);
            if (obj instanceof ClassLoader) {
                return (ClassLoader) obj;
            }
        } catch (Throwable ignored) {
        }

        try {
            Class<?> launchClClass = Class.forName("net.minecraft.launchwrapper.LaunchClassLoader");
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                ClassLoader cl = thread.getContextClassLoader();
                if (cl != null && launchClClass.isInstance(cl)) {
                    return cl;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void registerTransformer(BaseTransformer transformer) {
        String target = transformer.getTargetClassInternal();
        transformers.put(target, transformer);
        System.out.println("[Valor] Registered transformer for: " + target);
    }

    public static byte[] transform(String className, byte[] classfileBuffer) {
        if (className == null) {
            return null;
        }
        try {
            if (genesisClassLoader == null) {
                ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
                if (contextCL != null) {
                    try {
                        Class.forName(getMinecraftClassName(), false, contextCL);
                        genesisClassLoader = contextCL;
                        log("Found genesis CL from transform thread context");
                        injectIntoGenesis(genesisClassLoader);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }

            BaseTransformer transformer = transformers.get(className);
            if (transformer == null && customTransformMethod == null) {
                return null;
            }

            ClassLoader originalContextCL = Thread.currentThread().getContextClassLoader();
            if (genesisClassLoader != null) {
                Thread.currentThread().setContextClassLoader(genesisClassLoader);
            }
            try {
                if (customTransformMethod != null) {
                    byte[] transformed = (byte[]) customTransformMethod.invoke(null, className, classfileBuffer);
                    logTransformationResult(className, transformed);
                    return transformed;
                }
                byte[] transformed = transformer.doTransform(classfileBuffer);
                logTransformationResult(className, transformed);
                return transformed;
            } finally {
                Thread.currentThread().setContextClassLoader(originalContextCL);
            }
        } catch (Throwable t) {
            try {
                String errorLogPath = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\valor_transform_errors.log";
                FileWriter fw = new FileWriter(errorLogPath, true);
                try {
                    PrintWriter pw = new PrintWriter(fw);
                    try {
                        fw.write("[Valor] Transform error for " + className + ": " + t + "\n");
                        t.printStackTrace(pw);
                    } finally {
                        pw.close();
                    }
                } finally {
                    fw.close();
                }
            } catch (Throwable ignored) {
            }
            return null;
        }
    }

    private static void logTransformationResult(String className, byte[] resultBytes) {
        synchronized (transformedClasses) {
            if (transformedClasses.add(className)) {
                log("transform " + className + " result=" + (resultBytes == null ? "unchanged" : "changed:" + resultBytes.length));
            }
        }
    }

    static {
        try {
            registerTransformer(new MinecraftTransformer());
            registerTransformer(new EntityTransformer());
            registerTransformer(new EntityLivingBaseTransformer());
            registerTransformer(new EntityPlayerTransformer());
            registerTransformer(new EntityPlayerSPTransformer());
            registerTransformer(new AbstractClientPlayerTransformer());
            registerTransformer(new EntityRendererTransformer());
            registerTransformer(new NetworkManagerTransformer());
            registerTransformer(new PlayerControllerMPTransformer());
            registerTransformer(new KeyBindingTransformer());
            registerTransformer(new RendererLivingEntityTransformer());
            registerTransformer(new GuiIngameTransformer());
            registerTransformer(new GuiIngameForgeTransformer());
            registerTransformer(new RenderManagerTransformer());
            registerTransformer(new RenderTransformer());
            registerTransformer(new RenderPlayerTransformer());
            registerTransformer(new FontRendererTransformer());
            registerTransformer(new BlockTransformer());
            registerTransformer(new BlockModelRendererTransformer());
            registerTransformer(new BlockRendererDispatcherTransformer());
            registerTransformer(new WorldRendererTransformer());
            registerTransformer(new VisGraphTransformer());
            registerTransformer(new WorldTransformer());
            registerTransformer(new ItemStackTransformer());
            registerTransformer(new DisplayTransformer());
            registerTransformer(new MovementInputFromOptionsTransformer());
            registerTransformer(new RenderGlobalTransformer());
            System.out.println("[Valor] NativeBridge initialized with 27 transformers");
        } catch (Throwable t) {
            System.err.println("[Valor] NativeBridge init failed: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
