import com.charleskorn.kaml.Yaml
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals


class TestParser {
    private fun testFile(fileName: String, expected: Map<String, FakeTask>) {
        val inputStream = javaClass.getResourceAsStream(fileName)!!
        val tasks: Map<String, FakeTask> = parseYaml(inputStream)
        assertEquals(expected, tasks)
    }

    private fun testMap(expected: Map<String, FakeTask>) {
        val str = Yaml.default.encodeToString(MapSerializer(String.serializer(), FakeTask.serializer()), expected)
        val allSections: Map<String, FakeTask> = parseYaml(str.byteInputStream())
        assertEquals(expected, allSections)
    }

    @Test
    fun testSimpleFile1() {
        testFile(
            "/testPartUpToDate/fake.yml", mapOf(
                "compile" to FakeTask(
                    listOf("main.c"),
                    "main.o",
                    "gcc -c main.c -o main.o; echo compile"
                ),

                "build" to FakeTask(
                    listOf("compile"),
                    "main",
                    "gcc main.o -o main; echo build"
                )
            )
        )
    }

    @Test
    fun testSimpleFile2() {
        testFile(
            "/fake1.yml", mapOf(
                "compile" to FakeTask(
                    listOf("main.c"),
                    "main.o",
                    "gcc -c main.c -o main.o"
                ),

                "build" to FakeTask(
                    listOf("compile"),
                    "main",
                    "gcc main.o -o main"
                )
            )
        )
    }

    @Test
    fun testSimple() {
        testMap(
            mapOf(
                "hello" to FakeTask(
                    target = "hello",
                    run = "echo hello"
                ),

                "world" to FakeTask(
                    listOf("hello"),
                    target = "world",
                    run = "echo world"
                )
            )
        )
    }
}


