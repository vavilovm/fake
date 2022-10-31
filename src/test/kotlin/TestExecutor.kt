import com.charleskorn.kaml.Yaml
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.apache.commons.lang3.SystemUtils
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class TestExecutor {
    private fun testCommand(
        input: InputStream, outputStream: OutputStream? = null, act: (InputStream, OutputStream) -> Unit
    ): String {
        outputStream?.let { output ->
            return testCommandWithOutput(input, act, output)
        }

        ByteArrayOutputStream().use { output ->
            return testCommandWithOutput(input, act, output)
        }
    }

    private fun testCommandWithOutput(
        inputStream: InputStream, act: (InputStream, OutputStream) -> Unit, output: OutputStream
    ): String {
        inputStream.use { input ->
            act(input, output)
            return output.toString().removeSuffix(System.lineSeparator())
        }
    }

    private fun testFile(args: Array<String>, fileName: String): String {
        val inputStream = javaClass.getResourceAsStream(fileName)!!
        val dirPath = File(javaClass.getResource(fileName).path).parent!!
        return testCommand(inputStream) { input, output ->
            FakeTaskExecutor(args, input, output, dirPath).execute()
        }
    }

    private fun testMap(args: Array<String>, map: Map<String, FakeTask>, outputStream: OutputStream? = null): String {
        val str = Yaml.default.encodeToString(MapSerializer(String.serializer(), FakeTask.serializer()), map)
        return testCommand(str.byteInputStream(), outputStream) { inputStream, out ->
            FakeTaskExecutor(args, inputStream, out).execute()
        }
    }


    @Test
    fun testPartUpToDate() {
        if (SystemUtils.IS_OS_WINDOWS) return

        val fileName = "/testUpToDate/fake.yml"

        val res1 = testFile(arrayOf("compile"), fileName)
        val res2 = testFile(arrayOf("build"), fileName)
        val res3 = testFile(arrayOf("build"), fileName)
        assertEquals("compile", res1)
        assertEquals("Task compile is up to date.\nbuild", res2)
        assertEquals("Task compile is up to date.\nTask build is up to date.", res3)

        javaClass.getResource("/testUpToDate/main")?.let { url ->
            File(url.path).takeIf { it.exists() }?.delete()
        }
        javaClass.getResource("/testUpToDate/main.o")?.let { url ->
            File(url.path).takeIf { it.exists() }?.delete()
        }
    }

    @Test
    fun testDefaultRunFirst() {
        val res = testFile(
            arrayOf(), "/fake3.yml"
        )
        assertEquals(
            "5", res
        )
    }


    @Test
    fun testSimple() {
        val res = testMap(
            arrayOf("world"), mapOf(
                "hello" to FakeTask(
                    target = "hello", run = "echo hello"
                ),

                "world" to FakeTask(
                    listOf("hello"), target = "world", run = "echo world"
                )
            )
        )
        assertEquals("hello${System.lineSeparator()}world", res)
    }

    @Test
    fun testStderrMerged() {
        val res = testMap(arrayOf("hello"), mapOf("hello" to FakeTask(run = ">&2 echo error")))
        assertEquals("error", res)
    }


    @Test
    fun testEmpty() {
        val res = testMap(
            arrayOf("world"), mapOf(
                "hello" to FakeTask(),

                "world" to FakeTask(
                    listOf("hello")
                )
            )
        )
        assertEquals("", res)
    }

    @Test
    fun testDependencyDoesntExist() {
        assertThrows<DependencyUnresolved>("There is no file or task with name 2") {
            testMap(
                arrayOf("1"), mapOf(
                    "1" to FakeTask(
                        dependencies = listOf("2"),
                    ),
                ), System.out
            )
        }
    }

    @Test
    fun testCycleOf1() {
        assertThrows<CycleException> {
            testMap(
                arrayOf("1"), mapOf(
                    "1" to FakeTask(
                        dependencies = listOf("1"),
                    ),
                ), System.out
            )
        }
    }

    @Test
    fun testCycleOf2() {
        assertThrows<CycleException> {
            testMap(
                arrayOf("1"), mapOf(
                    "1" to FakeTask(
                        dependencies = listOf("2"),
                    ),

                    "2" to FakeTask(
                        dependencies = listOf("1"),
                    ),
                ), System.out
            )
        }
    }

    @Test
    fun testCycleOf3() {
        assertThrows<CycleException> {
            testMap(
                arrayOf("1"), mapOf(
                    "1" to FakeTask(
                        dependencies = listOf("2"),
                    ),

                    "2" to FakeTask(
                        dependencies = listOf("3"),
                    ),


                    "3" to FakeTask(
                        dependencies = listOf("1"),
                    ),
                ), System.out
            )
        }
    }
}