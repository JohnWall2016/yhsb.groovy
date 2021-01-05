package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Option
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels
import yhsb.base.util.UpInfoParameters
import yhsb.cjb.net.BankInfo
import yhsb.cjb.net.BankInfoQuery
import yhsb.cjb.net.Cbxx
import yhsb.cjb.net.CbxxQuery
import yhsb.cjb.net.Session

@Command(description = '城居保信息查询程序', subcommands = [GrinfoQuery, UpInfo, UpBankInfo])
class Query extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new Query()).execute(args)
    }

    @Override
    void run() {
        CommandLine.usage(new Query(), System.out)
    }

    @Command(name = 'qryInfo', description = '个人综合查询')
    static class GrinfoQuery extends CommandWithHelp {
        @Parameters(description = "身份证号码")
        String[] idCards

        @Override
        void run() {
            //println idCards
            if (idCards) {
                Session.use { sess ->
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

    @Command(name = 'upInfo', description = '更新excel表格中人员参保信息')
    static class UpInfo extends CommandWithHelp implements UpInfoParameters {
        @Override
        void run() {
            println '开始处理数据'

            def workbook = Excels.load(excel)
            def sheet = workbook.getSheetAt(0)

            Session.use { sess ->
                for (r in (startRow - 1)..(endRow - 1)) {
                    def row = sheet.getRow(r)
                    def name = row.getCell(nameCol).value
                    def idCard = row.getCell(idCardCol).value.trim().toUpperCase()

                    sess.sendService(new CbxxQuery(idCard))
                    def result = sess.getResult(Cbxx)
                    def state = '未参保'
                    if (!result.isEmpty() && result[0].valid()) {
                        state = result[0].jbState
                    }

                    println "更新 ${name.padRight(8)}$idCard $state"
                    row.getOrCreateCell(upInfoCol).cellValue = state
                }
            }

            workbook.save(excel.insertBeforeLast('.up'))

            println '结束数据处理'
        }
    }

    @Command(name = 'upBankInfo', description = '更新银行信息')
    static class UpBankInfo extends CommandWithHelp implements UpInfoParameters {
        @Option(names = ['-o', '--only-state'], description = '是否只更新绑卡状态')
        boolean onlyState = true

        @Override
        void run() {
            def workbook = Excels.load(excel)
            def sheet = workbook.getSheetAt(0)

            Session.use { sess ->
                for (r in (startRow - 1)..(endRow - 1)) {
                    def row = sheet.getRow(r)
                    def name = row.getCell(nameCol).value
                    def idCard = row.getCell(idCardCol).value.trim().toUpperCase()
                    def msg = "${name.padRight(8)}$idCard"

                    sess.sendService(new BankInfoQuery(idCard))
                    def result = sess.getResult(BankInfo)
                    if (!result.empty) {
                        msg = "$msg ${result[0].bankType.toString().padRight(14)}" +
                                "${result[0].countName} ${result[0].cardNumber}"
                    } else {
                        msg = "$msg 未绑卡"
                        if (onlyState) {
                            row.getOrCreateCell(upInfoCol).cellValue = '未绑卡'
                        }
                    }
                    println msg
                }
            }
            workbook.save(excel.insertBeforeLast('.up'))
        }
    }
}
