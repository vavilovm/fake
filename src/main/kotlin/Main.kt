import java.io.File
import java.io.FileNotFoundException
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Logger.getLogger("").apply { level = Level.OFF }
    try {
        FakeTaskExecutor(args, File("fake.yaml").inputStream(), System.out).execute()
    } catch (e: FileNotFoundException) {
        System.err.println("File not found: ${e.message}")
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println(e.message)
        exitProcess(2)
    }
}
