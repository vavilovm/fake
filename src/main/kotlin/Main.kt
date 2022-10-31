import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    try {
        FakeTaskExecutor(args, File("fake.yaml").inputStream(), System.out).execute()
    } catch (e: FileNotFoundException) {
        System.err.println("File not found: ${e.message}")
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println(e.message)
        exitProcess(1)
    }
}
