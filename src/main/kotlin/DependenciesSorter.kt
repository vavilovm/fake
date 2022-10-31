import java.util.logging.Logger

/**
 * fakeTask          -- name of a task
 * tasks             -- all tasks
 *
 * dependenciesList  -- list of dependencies to satisfy for fakeTask. Maybe a file or a name of a task.
 */
class DependenciesSorter(fakeTask: String, private val tasks: Map<String, FakeTask>) {
    val dependenciesList: List<String> by lazy {
        buildDependenciesList(fakeTask)
        list
    }
    private val list: MutableList<String> = mutableListOf()
    private val visited: MutableMap<String, Boolean> = mutableMapOf()
    private val logger: Logger = Logger.getLogger(this.javaClass.name)

    /**
     * dfs that builds a list of dependencies and detects cycles
     */
    private fun buildDependenciesList(task: String) {
        visited[task] = false // in dfs but still not out

        tasks[task]?.dependencies?.forEach { to ->
            if (visited[to] == false) {
                logger.info { "There is a cycle in dependencies" }
                throw CycleException("There is a cycle in dependencies")
            }
            if (visited[to] == null) {
                buildDependenciesList(to)
            }
        }

        list.add(task)
        visited[task] = true
    }
}

class CycleException(message: String) : Exception(message)
