package org.nixos.idea.format

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.provider.LocalEelDescriptor
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApi
import com.intellij.platform.eel.environmentVariables
import com.intellij.platform.eel.spawnProcess
import java.io.IOException

@Suppress("UnstableApiUsage")
internal object NixProcessHandlerFactory {
    @JvmStatic
    @Throws(ExecutionException::class)
    fun create(project: Project, commandLine: GeneralCommandLine): OSProcessHandler {
        val eelDescriptor = project.getEelDescriptor()
        if (eelDescriptor is LocalEelDescriptor) {
            return OSProcessHandler(commandLine)
        }

        val process = try {
            runBlockingMaybeCancellable {
                val eelApi = eelDescriptor.toEelApi()
                val environment = eelApi.exec.environmentVariables().eelIt().await() + commandLine.environment
                val processBuilder = eelApi.exec.spawnProcess(commandLine.exePath)
                    .args(commandLine.parametersList.list)
                    .env(environment)

                commandLine.workingDirectory?.let { workingDirectory ->
                    processBuilder.workingDirectory(workingDirectory.asEelPath())
                }

                processBuilder.eelIt()
            }
        } catch (e: IOException) {
            throw ExecutionException(e.message, e)
        }

        return OSProcessHandler(
            process.convertToJavaProcess(),
            commandLine.commandLineString,
            commandLine.charset
        )
    }
}
