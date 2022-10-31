import kotlinx.serialization.Serializable

@Serializable
data class FakeTask(
    val dependencies: List<String> = listOf(),
    val target: String? = null,
    val run: String = ""
)

