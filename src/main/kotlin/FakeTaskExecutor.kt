import org.apache.commons.lang3.SystemUtils
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger
import kotlin.io.path.exists

/**
 * args                 -- array of tasks to execute
 * inputStream          -- Fakefile
 * outputStream         -- where to redirect output
 * workingDirectoryName -- working directory for execution
 * parseInput           -- function that parses inputStream yaml to a Map<String, FakeTask>
 *
 * fun execute()        -- Executes every given task or the first one if none is specified
 */
class FakeTaskExecutor(
    private val args: Array<String>,
    inputStream: InputStream,
    private val output: OutputStream,
    workingDirectoryName: String = "",
    parseInput: (InputStream) -> Map<String, FakeTask> = ::parseYaml
) {
    private val logger: Logger = Logger.getLogger(this.javaClass.name)
    private val workingDirectoryPath = Paths.get(workingDirectoryName).toAbsolutePath()
    private val workingDirectoryFile = workingDirectoryPath.toFile()
    private val tasks = parseInput(inputStream)

    fun execute() {
        args.forEach(::runWithDependencies)

        if (args.isEmpty()) {
            // run first task if none is specified
            tasks.keys.firstOrNull()?.let { runWithDependencies(it) }
        }
    }

    private fun runWithDependencies(task: String) {
        DependenciesSorter(task, tasks).dependenciesList.forEach(::executeTask)
    }

    private fun executeTask(taskName: String) {
        val task = tasks[taskName]

        if (task == null) {
            if (!workingDirectoryPath.resolve(taskName).exists()) {
                logger.severe { "DependencyUnresolved: $taskName" }
                throw DependencyUnresolved("There is no file or task with name $taskName")
            }
            return
        }

        if (task.upToDate()) {
            output.write("Task $taskName is up to date.\n".toByteArray())
            logger.info { "$taskName: up to date" }
            return
        }
        logger.info { "$taskName: ${task.run}" }

        val command = if (SystemUtils.IS_OS_WINDOWS) listOf("cmd.exe", "/C", task.run)
        else listOf("bash", "-c", task.run)

        val res = ProcessBuilder(command)
            .redirectErrorStream(true) // Merges stderr into stdout
            .directory(workingDirectoryFile).start()
            .run {
                inputStream.use {
                    it.transferTo(output)
                }
                waitFor()
            }
        logger.info { "returnCode=$res" }
        if (res != 0) throw NonZeroReturnCode("Return code is $res for task $taskName")
    }

    /**
     * true if all dependencies were modified before
     */
    private fun FakeTask.upToDate(): Boolean {
        val targetPath = target?.let {
            workingDirectoryPath.resolve(it)
        }?.takeIf { it.exists() } ?: return false

        val targetLastModifiedTime = Files.getLastModifiedTime(targetPath)

        return dependencies.map { tasks[it]?.target ?: it }.all {
            val dependencyLastModifiedTime = Files.getLastModifiedTime(workingDirectoryPath.resolve(it))
            dependencyLastModifiedTime <= targetLastModifiedTime
        }
    }
}

class DependencyUnresolved(message: String) : Exception(message)
class NonZeroReturnCode(message: String) : Exception(message)
