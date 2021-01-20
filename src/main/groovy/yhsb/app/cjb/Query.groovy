package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Option
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels
import yhsb.base.util.UpInfoParametersWithInfoCol1
import yhsb.base.util.UpInfoParametersWithInfoCol2
import yhsb.cjb.net.BankInfo
import yhsb.cjb.net.BankInfoQuery
import yhsb.cjb.net.Grinfo
import yhsb.cjb.net.Sncbxx
import yhsb.cjb.net.SncbxxQuery
import yhsb.cjb.net.Session

@Command(description = '城居保信息查询程序', subcommands = [GrinfoQuery, UpInfo, UpBankInfo, UpIdCardInfo])
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
                        sess.sendService(new SncbxxQuery(idCard))
                        def result = sess.getResult(Sncbxx)
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
    static class UpInfo extends CommandWithHelp implements UpInfoParametersWithInfoCol1 {
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

                    sess.sendService(new SncbxxQuery(idCard))
                    def result = sess.getResult(Sncbxx)
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
    static class UpBankInfo extends CommandWithHelp implements UpInfoParametersWithInfoCol1 {
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

    @Command(name = 'upIdCardInfo', description = '从姓名更新身份证信息')
    static class UpIdCardInfo extends CommandWithHelp implements UpInfoParametersWithInfoCol2 {
        @Option(names = ['-f', '--filter'], description = '参保身份过滤条件')
        String filter = '.*'

        @Override
        void run() {
            def workbook = Excels.load(excel)
            def sheet = workbook.getSheetAt(0)

            println "filter: $filter"

            def pattern = /.*${filter.split(', *').join('|')}.*/
            println pattern

            Session.use {sess ->
                for (r in (startRow - 1)..(endRow - 1)) {
                    def row = sheet.getRow(r)
                    def name = row.getCell(nameCol).value

                    sess.sendService(new yhsb.cjb.net.GrinfoQuery(name: name))
                    def result = sess.getResult(Grinfo)

                    List<String> idCards = []
                    result.each {
                        def m = it.jbState.toString() =~ pattern
                        if (m.find()) {
                            idCards.add(it.idCard)
                        }
                    }
                    def idCard = idCards.join('|')
                    println "${name.padRight(8)}${idCard}"

                    row.getOrCreateCell(upInfoCol).cellValue = idCard
                }
            }

            workbook.save(excel.insertBeforeLast('.up'))
        }
    }
}
