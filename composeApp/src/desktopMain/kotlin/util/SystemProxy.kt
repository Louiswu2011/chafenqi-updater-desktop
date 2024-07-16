package util

import java.io.File
import java.util.concurrent.TimeUnit

object SystemProxy {
    fun setProxy() {
        setWindowsProxy()
    }

    fun disableProxy() {
        disableWindowsProxy()
    }

    private fun setWindowsProxy() {
        println("Setting windows proxy.")
        "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /f /t REG_DWORD /d 1"
            .runCommand()
        "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /f /t REG_SZ /d \"43.139.107.206:8999\""
            .runCommand()
    }

    private fun disableWindowsProxy() {
        println("Disabling windows proxy.")
        "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /f /t REG_DWORD /d 0"
            .runCommand()
        "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /f /t REG_SZ /d \"127.0.0.1:2333\""
            .runCommand()
    }
}

fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String? = runCatching {
    ProcessBuilder("\\s".toRegex().split(this))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().also { it.waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
}.onFailure { it.printStackTrace() }.getOrNull()