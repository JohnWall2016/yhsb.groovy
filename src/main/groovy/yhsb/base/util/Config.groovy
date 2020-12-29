package yhsb.base.util

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue

class Config {
    static com.typesafe.config.Config load(String prefix) {
        def factory = ConfigFactory.load()
        if (factory.hasPath(prefix)) {
            factory.getConfig(prefix)
        } else {
            ConfigFactory.empty()
        }
    }
}

class ConfigExtensions {
    static Map<String, String> toMap(com.typesafe.config.Config config) {
        def map = new LinkedHashMap()
        config.entrySet().each { Map.Entry<String, ConfigValue> entry ->
            map[entry.key] = entry.value.unwrapped().toString()
        }
        map
    }
}
