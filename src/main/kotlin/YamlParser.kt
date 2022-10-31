import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import java.io.InputStream

fun parseYaml(inputStream: InputStream): Map<String, FakeTask> =
    Yaml.default.decodeFromStream(inputStream)
