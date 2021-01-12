package yhsb.base.util

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

trait UpInfoParameters {
    @Parameters(paramLabel = 'excel', index = '0', description = 'excel表格文件路径')
    String excel

    @Parameters(paramLabel = 'startRow', index = '1', description = '开始行(从1开始)')
    int startRow

    @Parameters(paramLabel = 'endRow', index = '2', description = '结束行(包含在内)')
    int endRow

    @Parameters(paramLabel = 'nameCol', index = '3', description = '姓名所在列, 例如: H')
    String nameCol

    @Parameters(paramLabel = 'idCardCol', index = '4', description = '身份证所在列, 例如: I')
    String idCardCol

    @Parameters(paramLabel = 'upInfoCol', index = '5', description = '更新状态信息所在列, 例如: J')
    String upInfoCol
}

trait UpInfoParameters2 {
    @Parameters(paramLabel = 'excel', index = '0', description = 'excel表格文件路径')
    String excel

    @Parameters(paramLabel = 'startRow', index = '1', description = '开始行(从1开始)')
    int startRow

    @Parameters(paramLabel = 'endRow', index = '2', description = '结束行(包含在内)')
    int endRow

    @Parameters(paramLabel = 'nameCol', index = '3', description = '姓名所在列, 例如: H')
    String nameCol

    @Parameters(paramLabel = 'upInfoCol', index = '4', description = '更新状态信息所在列, 例如: J')
    String upInfoCol
}
