import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    try {
//        FakeTaskExecutor(args, File("fake.yaml").inputStream(), System.out).execute()
        FakeTaskExecutor(args, File("/Users/Mark.Vavilov/hse/fake/src/test/resources/testUpToDate/fake.yml").inputStream(), System.out, "/Users/Mark.Vavilov/hse/fake/src/test/resources/testUpToDate/").execute()
    } catch (e: FileNotFoundException) {
        System.err.println("File not found: ${e.message}")
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println(e.message)
        exitProcess(1)
    }
}
