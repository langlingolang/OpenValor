#include <cstdio>
#include <iostream>
#include <string>
#include <fstream>
#include <Windows.h>
#include <jni.h>
#include <jvmti.h>

typedef jint(JNICALL* GetCreatedVMs)(JavaVM**, jsize, jsize*);
typedef jint(JNICALL* AgentOnAttach)(JavaVM*, char*, void*);

std::string GetTempDirPath() {
    char tempPath[MAX_PATH];
    if (GetTempPathA(MAX_PATH, tempPath) == 0) {
        return "C:\\Temp\\";
    }
    std::string path = tempPath;
    if (!path.empty() && path.back() != '\\' && path.back() != '/') {
        path += '\\';
    }
    return path;
}

void LogMessage(const std::string& msg) {
    std::string logFilePath = GetTempDirPath() + "valor_injector.log";
    std::ofstream file(logFilePath, std::ios::app);
    if (file.is_open()) {
        file << msg << "\n";
        file.close();
    }
}

std::string GetAgentPath() {

    std::string tempPathFile = GetTempDirPath() + "valor_agent.path";
    for (int retry = 0; retry < 30; retry++) {
        std::ifstream file(tempPathFile);
        if (file.is_open()) {
            std::string agentPath;
            if (std::getline(file, agentPath)) {
                while (!agentPath.empty() && (agentPath.back() == '\r' || agentPath.back() == '\n' || agentPath.back() == ' ' || agentPath.back() == '\"')) {
                    agentPath.pop_back();
                }
                while (!agentPath.empty() && (agentPath.front() == ' ' || agentPath.front() == '\"')) {
                    agentPath.erase(0, 1);
                }
                if (!agentPath.empty()) {
                    file.close();
                    DeleteFileA(tempPathFile.c_str());
                    return agentPath;
                }
            }
            file.close();
        }
        Sleep(100);
    }

    char appData[MAX_PATH];
    if (GetEnvironmentVariableA("APPDATA", appData, MAX_PATH) > 0) {
        std::string appDataDir = std::string(appData) + "\\.minecraft\\valor\\";
        std::string appDataPathFile = appDataDir + "valor_agent.path";
        std::ifstream file(appDataPathFile);
        if (file.is_open()) {
            std::string agentPath;
            if (std::getline(file, agentPath)) {
                while (!agentPath.empty() && (agentPath.back() == '\r' || agentPath.back() == '\n' || agentPath.back() == ' ' || agentPath.back() == '\"')) {
                    agentPath.pop_back();
                }
                while (!agentPath.empty() && (agentPath.front() == ' ' || agentPath.front() == '\"')) {
                    agentPath.erase(0, 1);
                }
                if (!agentPath.empty()) {
                    file.close();
                    DeleteFileA(appDataPathFile.c_str());
                    return agentPath;
                }
            }
            file.close();
        }

        std::string notchJar = appDataDir + "valor-notch.jar";
        if (GetFileAttributesA(notchJar.c_str()) != INVALID_FILE_ATTRIBUTES) {
            return notchJar;
        }
        std::string forgeJar = appDataDir + "valor-forge.jar";
        if (GetFileAttributesA(forgeJar.c_str()) != INVALID_FILE_ATTRIBUTES) {
            return forgeJar;
        }
    }

    return "valor-1.0.0-universal.jar";
}

DWORD WINAPI ForceLoadAgent(LPVOID lpParam) {
    HMODULE hModule = (HMODULE)lpParam;
    HMODULE hJvm = nullptr;
    GetCreatedVMs fGetVMs = nullptr;
    JavaVM* jvm = nullptr;
    jsize numVms = 0;
    JNIEnv* env = nullptr;
    HMODULE hInstrument = nullptr;
    AgentOnAttach fAgentOnAttach = nullptr;

    LogMessage("--- VALOR NATIVE ATTACH ---");

    hJvm = GetModuleHandleA("jvm.dll");
    if (!hJvm) {
        LogMessage("[!] jvm.dll not found in target process!");
        goto cleanup;
    }

    fGetVMs = (GetCreatedVMs)GetProcAddress(hJvm, "JNI_GetCreatedJavaVMs");
    if (!fGetVMs) {
        LogMessage("[!] JNI_GetCreatedJavaVMs export not found in jvm.dll!");
        goto cleanup;
    }

    if (fGetVMs(&jvm, 1, &numVms) != 0 || numVms == 0 || !jvm) {
        LogMessage("[!] No active Java VM found in target process!");
        goto cleanup;
    }

    if (jvm->AttachCurrentThreadAsDaemon((void**)&env, nullptr) != JNI_OK) {
        LogMessage("[!] Failed to attach current thread to JVM!");
        goto cleanup;
    }

    {
        std::string agentPath = GetAgentPath();
        LogMessage("[*] Target Agent Path: " + agentPath);

        hInstrument = GetModuleHandleA("instrument.dll");
        if (!hInstrument) {
            char jvmPath[MAX_PATH];
            if (GetModuleFileNameA(hJvm, jvmPath, MAX_PATH)) {
                std::string path(jvmPath);
                size_t lastSlash = path.find_last_of("\\/");
                if (lastSlash != std::string::npos) {
                    path = path.substr(0, lastSlash);
                    size_t parentSlash = path.find_last_of("\\/");
                    if (parentSlash != std::string::npos) {
                        std::string binPath = path.substr(0, parentSlash) + "\\instrument.dll";
                        hInstrument = LoadLibraryA(binPath.c_str());
                    }
                    if (!hInstrument) {
                        std::string sameDirPath = path + "\\instrument.dll";
                        hInstrument = LoadLibraryA(sameDirPath.c_str());
                    }
                }
            }
        }

        if (!hInstrument) {
            hInstrument = LoadLibraryA("instrument.dll");
        }

        if (hInstrument) {
            fAgentOnAttach = (AgentOnAttach)GetProcAddress(hInstrument, "Agent_OnAttach");
            if (fAgentOnAttach) {
                int attachResult = fAgentOnAttach(jvm, (char*)agentPath.c_str(), nullptr);
                if (attachResult == 0) {
                    LogMessage("[+] Native Agent Attached Successfully!");
                } else {
                    LogMessage("[-] FATAL: Agent_OnAttach failed with code: " + std::to_string(attachResult));
                }
            } else {
                LogMessage("[-] FATAL: Agent_OnAttach export not found in instrument.dll!");
            }
        } else {
            LogMessage("[-] FATAL: Failed to load instrument.dll!");
        }

        jvm->DetachCurrentThread();
    }

cleanup:
    Sleep(500);
    FreeLibraryAndExitThread(hModule, 0);
    return 0;
}

BOOL APIENTRY DllMain(HMODULE hModule, DWORD reason, LPVOID reserved) {
    if (reason == DLL_PROCESS_ATTACH) {
        DisableThreadLibraryCalls(hModule);
        HANDLE hThread = CreateThread(nullptr, 0, (LPTHREAD_START_ROUTINE)ForceLoadAgent, hModule, 0, nullptr);
        if (hThread) CloseHandle(hThread);
    }
    return TRUE;
}

}
