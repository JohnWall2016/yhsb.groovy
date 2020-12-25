package yhsb.base.util

import com.typesafe.config.ConfigFactory

class Config {
    static com.typesafe.config.Config load(String configPrefix) {
        def factory = ConfigFactory.load()
        if (factory.hasPath(configPrefix)) {
            factory.getConfig(configPrefix)
        } else {
            ConfigFactory.empty()
        }
    }
}
