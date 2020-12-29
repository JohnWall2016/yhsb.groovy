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
    static Map<String, Object> toMap(com.typesafe.config.Config config) {
        Map<String, Object> map = [:]
        config.entrySet().each { Map.Entry<String, ConfigValue> entry ->
            map[entry.key] = entry.value.unwrapped()
        }
        map
    }
}
