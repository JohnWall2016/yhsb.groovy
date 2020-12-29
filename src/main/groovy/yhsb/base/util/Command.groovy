package yhsb.base.util

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(mixinStandardHelpOptions = true)
abstract class CommandWithHelp implements Runnable {
}

trait DateRange {
    @Parameters(paramLabel = 'startDate', description = "开始时间, 例如: 20200701")
    String startDate

    @Parameters(paramLabel = 'endDate', index = '1', arity = '0..1', description = "结束时间, 例如: 20200701")
    String endDate
}

trait Export {
    @Option(names = ['-e', '--export'], description = '是否导出数据')
    boolean export = false
}
