package valor.agent;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NativeInjector {

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

    public static final int PROCESS_TERMINATE = 0x0001;
    public static final int PROCESS_CREATE_THREAD = 0x0002;
    public static final int PROCESS_VM_OPERATION = 0x0008;
    public static final int PROCESS_VM_READ = 0x0010;
    public static final int PROCESS_VM_WRITE = 0x0020;
    public static final int PROCESS_QUERY_INFORMATION = 0x0400;

    public static final int MEM_COMMIT = 0x1000;
    public static final int MEM_RESERVE = 0x2000;
    public static final int MEM_RELEASE = 0x8000;
    public static final int PAGE_READWRITE = 0x04;
    public static final int PAGE_EXECUTE_READWRITE = 0x40;

    public static final int INFINITE = 0xFFFFFFFF;
    public static final int WAIT_OBJECT_0 = 0x00000000;
    public static final int WAIT_TIMEOUT = 0x00000102;
    public static final int WAIT_FAILED = 0xFFFFFFFF;

    public interface Win32Kernel32 extends StdCallLibrary {
        Win32Kernel32 INSTANCE = (Win32Kernel32) Native.loadLibrary("kernel32", Win32Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

        Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);

        Pointer VirtualAllocEx(Pointer hProcess, Pointer lpAddress, BaseTSD.SIZE_T dwSize, int flAllocationType, int flProtect);

        boolean VirtualFreeEx(Pointer hProcess, Pointer lpAddress, BaseTSD.SIZE_T dwSize, int dwFreeType);

        boolean WriteProcessMemory(Pointer hProcess, Pointer lpBaseAddress, Pointer lpBuffer, BaseTSD.SIZE_T nSize, Pointer lpNumberOfBytesWritten);

        Pointer CreateRemoteThread(
                Pointer hProcess,
                Pointer lpThreadAttributes,
                BaseTSD.SIZE_T dwStackSize,
                Pointer lpStartAddress,
                Pointer lpParameter,
                int dwCreationFlags,
                Pointer lpThreadId
        );

        int WaitForSingleObject(Pointer hHandle, int dwMilliseconds);

        boolean GetExitCodeThread(Pointer hThread, IntByReference lpExitCode);

        boolean CloseHandle(Pointer hObject);

        int GetLastError();
    }

    public static File resolveDll(String dllPath) {
        if (dllPath == null || dllPath.trim().isEmpty()) {
            dllPath = "valor_injector.dll";
        }

        File direct = new File(dllPath);
        if (direct.isAbsolute() && direct.exists()) {
            return direct;
        }

        File userDir = new File(System.getProperty("user.dir", "."));
        File[] candidates = new File[]{
                direct,
                new File(userDir, dllPath),
                new File(userDir, "native/build/Release/" + direct.getName()),
                new File(userDir, "native/build/" + direct.getName()),
                new File(userDir, "build/libs/" + direct.getName()),
                new File(userDir, "../native/build/Release/" + direct.getName()),
                new File("valor/reconstructed_src/native/build/Release/" + direct.getName()),
                new File(System.getProperty("java.io.tmpdir"), direct.getName())
        };

        for (File f : candidates) {
            if (f.exists() && f.isFile()) {
                return f;
            }
        }

        try (InputStream in = NativeInjector.class.getResourceAsStream("/" + direct.getName())) {
            if (in != null) {
                File tempDll = new File(System.getProperty("java.io.tmpdir"), direct.getName());
                try (FileOutputStream fos = new FileOutputStream(tempDll)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                }
                return tempDll;
            }
        } catch (Exception ignored) {
        }

        return direct;
    }

    public static boolean inject(int pid, String agentJarPath, String dllPath) {
        cleanTempFiles();
        try {
            File agentJar = new File(agentJarPath);
            String absoluteAgentPath = agentJar.getAbsolutePath();

            File tempFile = new File(System.getProperty("java.io.tmpdir"), "valor_agent.path");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(absoluteAgentPath.getBytes(StandardCharsets.UTF_8));
            }
            try {
                File appDataDir = new File(System.getProperty("user.home") + "/AppData/Roaming/.minecraft/valor");
                appDataDir.mkdirs();
                File appDataPathFile = new File(appDataDir, "valor_agent.path");
                try (FileOutputStream fos = new FileOutputStream(appDataPathFile)) {
                    fos.write(absoluteAgentPath.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Throwable ignored) {
            }

            File prevLog = new File(System.getProperty("java.io.tmpdir"), "valor_injector.log");
            if (prevLog.exists()) {
                prevLog.delete();
            }

            File resolvedDll = resolveDll(dllPath);
            if (!resolvedDll.exists()) {
                System.out.println("[NativeInjector] Error: DLL not found at: " + dllPath);
                return false;
            }

            String absoluteDllPath = resolvedDll.getAbsolutePath();
            System.out.println("[NativeInjector] Injector DLL: " + absoluteDllPath);
            System.out.println("[NativeInjector] Agent JAR: " + absoluteAgentPath);

            int desiredAccess = PROCESS_CREATE_THREAD |
                    PROCESS_QUERY_INFORMATION |
                    PROCESS_VM_OPERATION |
                    PROCESS_VM_WRITE |
                    PROCESS_VM_READ;

            Pointer hProcess = Win32Kernel32.INSTANCE.OpenProcess(desiredAccess, false, pid);
            if (hProcess == null || Pointer.nativeValue(hProcess) == 0) {
                int err = Win32Kernel32.INSTANCE.GetLastError();
                System.out.println("[NativeInjector] Failed to open process PID " + pid + " (Error code: " + err + ")");
                return false;
            }

            byte[] dllPathBytes = (absoluteDllPath + "\0").getBytes(StandardCharsets.UTF_8);
            BaseTSD.SIZE_T allocSize = new BaseTSD.SIZE_T(dllPathBytes.length);

            Pointer pDllPathRemote = Win32Kernel32.INSTANCE.VirtualAllocEx(
                    hProcess,
                    null,
                    allocSize,
                    MEM_COMMIT | MEM_RESERVE,
                    PAGE_READWRITE
            );

            if (pDllPathRemote == null || Pointer.nativeValue(pDllPathRemote) == 0) {
                int err = Win32Kernel32.INSTANCE.GetLastError();
                System.out.println("[NativeInjector] VirtualAllocEx failed in target process (Error code: " + err + ")");
                Win32Kernel32.INSTANCE.CloseHandle(hProcess);
                return false;
            }

            Memory mem = new Memory(dllPathBytes.length);
            mem.write(0, dllPathBytes, 0, dllPathBytes.length);

            boolean writtenOk = Win32Kernel32.INSTANCE.WriteProcessMemory(
                    hProcess,
                    pDllPathRemote,
                    mem,
                    allocSize,
                    null
            );

            if (!writtenOk) {
                int err = Win32Kernel32.INSTANCE.GetLastError();
                System.out.println("[NativeInjector] WriteProcessMemory failed (Error code: " + err + ")");
                Win32Kernel32.INSTANCE.VirtualFreeEx(hProcess, pDllPathRemote, new BaseTSD.SIZE_T(0), MEM_RELEASE);
                Win32Kernel32.INSTANCE.CloseHandle(hProcess);
                return false;
            }

            Pointer pLoadLibrary = null;
            try {
                pLoadLibrary = NativeLibrary.getInstance("kernel32").getFunction("LoadLibraryA");
            } catch (Throwable t) {
                WinDef.HMODULE hKernel32 = Kernel32.INSTANCE.GetModuleHandle("kernel32.dll");
                if (hKernel32 != null) {
                    Function getProc = NativeLibrary.getInstance("kernel32").getFunction("GetProcAddress");
                    pLoadLibrary = (Pointer) getProc.invoke(Pointer.class, new Object[]{ hKernel32.getPointer(), "LoadLibraryA" });
                }
            }

            if (pLoadLibrary == null || Pointer.nativeValue(pLoadLibrary) == 0) {
                System.out.println("[NativeInjector] Failed to get address of LoadLibraryA");
                Win32Kernel32.INSTANCE.VirtualFreeEx(hProcess, pDllPathRemote, new BaseTSD.SIZE_T(0), MEM_RELEASE);
                Win32Kernel32.INSTANCE.CloseHandle(hProcess);
                return false;
            }

            System.out.println("[NativeInjector] Resolved LoadLibraryA address: " + pLoadLibrary);

            Pointer hThread = Win32Kernel32.INSTANCE.CreateRemoteThread(
                    hProcess,
                    null,
                    new BaseTSD.SIZE_T(0),
                    pLoadLibrary,
                    pDllPathRemote,
                    0,
                    null
            );

            if (hThread == null || Pointer.nativeValue(hThread) == 0) {
                int err = Win32Kernel32.INSTANCE.GetLastError();
                System.out.println("[NativeInjector] CreateRemoteThread failed (Error code: " + err + ")");
                Win32Kernel32.INSTANCE.VirtualFreeEx(hProcess, pDllPathRemote, new BaseTSD.SIZE_T(0), MEM_RELEASE);
                Win32Kernel32.INSTANCE.CloseHandle(hProcess);
                return false;
            }

            int wait = Win32Kernel32.INSTANCE.WaitForSingleObject(hThread, 8000);
            if (wait == WAIT_TIMEOUT) {
                System.out.println("[NativeInjector] Warning: Remote LoadLibraryA thread wait timed out.");
            }

            IntByReference exitCode = new IntByReference();
            boolean gotExitCode = Win32Kernel32.INSTANCE.GetExitCodeThread(hThread, exitCode);

            Win32Kernel32.INSTANCE.VirtualFreeEx(hProcess, pDllPathRemote, new BaseTSD.SIZE_T(0), MEM_RELEASE);
            Win32Kernel32.INSTANCE.CloseHandle(hThread);
            Win32Kernel32.INSTANCE.CloseHandle(hProcess);

            if (gotExitCode && exitCode.getValue() == 0) {
                System.out.println("[NativeInjector] Error: LoadLibraryA returned NULL in target process (exitCode=0)");
                return false;
            }

            System.out.println("[NativeInjector] Remote LoadLibraryA executed successfully (HMODULE: 0x" + Integer.toHexString(exitCode.getValue()) + ")!");

            File logFile = new File(System.getProperty("java.io.tmpdir"), "valor_injector.log");
            long waitStart = System.currentTimeMillis();
            boolean sawFinished = false;
            while (System.currentTimeMillis() - waitStart < 4000) {
                if (logFile.exists() && logFile.length() > 0) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("Native Agent Attached Successfully!") || line.contains("FATAL:")) {
                                sawFinished = true;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    if (sawFinished) {
                        break;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            if (logFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Native DLL] " + line);
                    }
                } catch (Exception ignored) {
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

