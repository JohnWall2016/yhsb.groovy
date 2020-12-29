package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels
import yhsb.cjb.net.Cbxx
import yhsb.cjb.net.CbxxQuery
import yhsb.cjb.net.Session

@Command(description = '城居保信息查询程序', subcommands = [GrinfoQuery, UpInfo])
class Query extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new Query()).execute(args)
    }

    @Override
    void run() {
        CommandLine.usage(new Query(), System.out)
    }

    @Command(name = 'grinfo', description = '个人综合查询')
    static class GrinfoQuery extends CommandWithHelp {
        @Parameters(description = "身份证号码")
        String[] idCards

        @Override
        void run() {
            //println idCards
            if (idCards) {
                Session.use {sess ->
                    for (idCard in idCards) {
                        sess.sendService(new CbxxQuery(idCard))
                        def result = sess.getResult(Cbxx)
                        if (result.isEmpty()) {
                            println "$idCard 未在我区参保"
                        } else {
                            def info = result[0]
                            println "${info.idCard} ${info.name} ${info.jbState}" +
                                    "${info.dwName} ${info.csName}"
                        }
                    }
                }
            }
        }
    }

    @Command(name = 'upinfo', description = '更新excel表格中人员参保信息')
    static class UpInfo extends CommandWithHelp {
        @Parameters(description = 'excel表格文件路径')
        String excel

        @Parameters(description = '开始行(从1开始)')
        int startRow

        @Parameters(description = '结束行(包含在内)')
        int endRow

        @Parameters(description = '姓名所在列, 例如: H')
        String nameCol

        @Parameters(description = '身份证所在列, 例如: I')
        String idCardCol

        @Parameters(description = '更新状态信息所在列, 例如: J')
        String upInfoCol

        @Override
        void run() {
            println '开始处理数据'

            def workbook = Excels.load(excel)
            def sheet = workbook.getSheetAt(0)

            Session.use { sess ->
                for (r in (startRow-1)..(endRow-1)) {
                    def row = sheet.getRow(r)
                    def name = row.getCell(nameCol).value
                    def idCard = row.getCell(idCardCol).value.trim().toUpperCase()

                    println "更新 ${name.padRight(8)}$idCard"

                    sess.sendService(new CbxxQuery(idCard))
                    def result = sess.getResult(Cbxx)
                    if (!result.isEmpty() && result[0].valid()) {
                        row.getOrCreateCell(upInfoCol).cellValue = result[0].jbState
                    } else {
                        row.getOrCreateCell(upInfoCol).cellValue = '未参保'
                    }
                }
            }

            workbook.save(excel.insertBeforeLast('.up'))

            println '结束数据处理'
        }
    }
}
