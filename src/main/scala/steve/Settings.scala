package steve

import com.typesafe.config.Config
import scala.collection.JavaConverters._

final
case class Entry (
                    name: String,
                    workDir: String,
                    startScript: String
                  )

class Settings(config: Config) {

  val timeout = config.getDouble("steve.timeout")
  val slackWebHook = config.getString("steve.slackWebHook")

  val entries: List[Entry] = {
    config.getConfigList("steve.entries")
      .asScala.toList
      .map { config =>
        val name = config.getString("name")
        val workDir = config.getString("workDir")
        val startScript = config.getString("startScript")
        Entry(name, workDir, startScript)
      }
  }

}
