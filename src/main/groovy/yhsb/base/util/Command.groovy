package yhsb.base.util

import picocli.CommandLine.Command

@Command(mixinStandardHelpOptions = true)
abstract class CommandWithHelp implements Runnable {
}
