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
import yhsb.cjb.net.Jfxx
import yhsb.cjb.net.JfxxRequest
import yhsb.cjb.net.Result
import yhsb.cjb.net.Sncbxx
import yhsb.cjb.net.SncbxxQuery
import yhsb.cjb.net.Session

import java.nio.file.Paths

@Command(description = '城居保信息查询程序',
        subcommands = [GrinfoQuery, UpInfo, UpBankInfo, UpIdCardInfo, JfxxQuery])
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
                    if (!result.isEmpty() && result[0].isValid()) {
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

            Session.use { sess ->
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


    @Command(name = 'jfxx', description = '缴费信息查询')
    static class JfxxQuery extends CommandWithHelp {
        @Option(names = ['-e', '--export'], description = '导出信息表')
        boolean export = false

        @Parameters(description = '身份证号码')
        String idCard = null

        /** 缴费记录  */
        static class JfxxRecord {
            /** 年度  */
            Integer year = null

            /** 个人缴费  */
            BigDecimal grjf = 0

            /** 省级补贴  */
            BigDecimal sjbt = 0

            /** 市级补贴  */
            BigDecimal sqbt = 0

            /** 县级补贴  */
            BigDecimal xjbt = 0

            /** 政府代缴  */
            BigDecimal zfdj = 0

            /** 集体补助  */
            BigDecimal jtbz = 0

            /** 退捕渔民补助 */
            BigDecimal tbymbz = 0

            /** 划拨日期  */
            LinkedHashSet<String> hbrq = []

            /** 社保机构  */
            LinkedHashSet<String> sbjg = []

            JfxxRecord(Integer year) {
                this.year = year
            }

            JfxxRecord() {}
        }

        /** 缴费合计记录  */
        static class JfxxTotalRecord extends JfxxRecord {
            /** 合计  */
            BigDecimal total = 0

            JfxxTotalRecord() {
                super()
            }
        }

        static void getJfxxRecords(Result<Jfxx> result,
                            Map<Integer, JfxxRecord> paidRecords,
                            Map<Integer, JfxxRecord> unpaidRecords) {
            for (data in result) {
                var year = data.year
                if (year != null) {
                    var records = data.paidOff ? paidRecords : unpaidRecords
                    var record = records[year]
                    if (record == null) {
                        record = new JfxxRecord(year)
                        records[year] = record
                    }
                    BigDecimal amount = data.amount ?: 0
                    var type = data.item?.value
                    switch (type) {
                        case '1':
                            record.grjf += amount
                            break
                        case '3':
                            record.sjbt += amount
                            break
                        case '4':
                            record.sqbt += amount
                            break
                        case '5':
                            record.xjbt += amount
                            break
                        case '6':
                            record.jtbz += amount
                            break
                        case '11':
                            record.zfdj += amount
                            break
                        case '15':
                            record.tbymbz += amount
                            break
                        default:
                            throw new Exception("未知缴费类型$type, 金额$amount")
                    }
                    record.sbjg.add(data.agency ?: "")
                    record.hbrq.add(data.paidOffDay ?: "")
                }
            }
        }

        static List<JfxxRecord> orderAndSum(Map<Integer, JfxxRecord> records) {
            var results = records.values().sort {
                it.year
            }
            var total = new JfxxTotalRecord()
            results.forEach {
                total.grjf += it.grjf
                total.sjbt += it.sjbt
                total.sqbt += it.sqbt
                total.xjbt += it.xjbt
                total.zfdj += it.zfdj
                total.jtbz += it.jtbz
                total.tbymbz += it.tbymbz
            }
            total.total = total.grjf + total.sjbt + total.sqbt +
                    total.xjbt + total.zfdj + total.jtbz + total.tbymbz
            results.add(total)
            results
        }

        static void printInfo(Sncbxx info) {
            println('个人信息:')
            println(String.format('%s %s %s %s %s %s %s', info.name, info.idCard,
                    info.jbState, info.jbKind, info.agency,
                    info.czName, info.opTime))
        }

        static String formatRecord(JfxxRecord r) {
            if (!JfxxTotalRecord.isInstance(r)) {
                String.format('%5s%9s%9s%9s%9s%9s%9s%9s  %s %s', r.year,
                        r.grjf, r.sjbt, r.sqbt, r.xjbt, r.zfdj, r.jtbz, r.tbymbz,
                        r.sbjg.join('|'), r.hbrq.join('|'))
            } else {
                String.format(' 合计%9s%9s%9s%9s%9s%9s%9s', r.grjf, r.sjbt,
                        r.sqbt, r.xjbt, r.zfdj, r.jtbz, r.tbymbz) +
                        '  总计: ' + (r as JfxxTotalRecord).total
            }
        }

        static void printJfxxRecords(List<JfxxRecord> records, String message) {
            println(message)
            println(String.format('%2s%3s%6s%5s%5s%5s%5s%5s%7s %s', '序号', '年度', '个人缴费',
                    '省级补贴', '市级补贴', '县级补贴', '政府代缴', '集体补助', '退捕渔民补助', '社保经办机构', '划拨时间'))
            var i = 1
            for (r in records) {
                var t = JfxxTotalRecord.isInstance(r) ? '' : "${i++}"
                println(String.format("%3s %s", t, formatRecord(r)))
            }
        }

        @Override
        void run() {
            if (!idCard) return

            def (Sncbxx info, Result<Jfxx> jfxx) = Session.use() {
                Sncbxx info = null
                Result<Jfxx> jfxx = null

                it.sendService(new SncbxxQuery(idCard))
                var infoRes = it.getResult(Sncbxx)
                if (infoRes.empty || !infoRes[0].valid)
                    return new Tuple2<>(info, jfxx)
                info = infoRes[0]

                it.sendService(new JfxxRequest(idCard))
                var jfxxRes = it.getResult(Jfxx)
                if (!jfxxRes.empty && jfxxRes[0].year)
                    jfxx = jfxxRes
                new Tuple2<>(info, jfxx)
            }

            if (info == null) {
                println("未查到参保记录")
                return
            }

            printInfo(info)

            List<JfxxRecord> records = null
            List<JfxxRecord> unrecords

            if (jfxx == null) {
                println("未查询到缴费信息")
            } else {
                var paidRecords = new LinkedHashMap<Integer, JfxxRecord>()
                var unpaidRecords = new LinkedHashMap<Integer, JfxxRecord>()
                getJfxxRecords(jfxx, paidRecords, unpaidRecords)
                records = orderAndSum(paidRecords)
                unrecords = orderAndSum(unpaidRecords)
                printJfxxRecords(records, "\n已拨付缴费历史记录:")
                if (!unpaidRecords.isEmpty()) {
                    printJfxxRecords(unrecords, "\n未拨付补录入记录:")
                }
            }

            if (export) {
                var dir = 'D:\\征缴管理'
                var xlsx = Paths.get(dir, "雨湖区城乡居民基本养老保险缴费查询单模板.xlsx")
                var workbook = Excels.load(xlsx.toString())
                var sheet = workbook.getSheetAt(0).with {
                    getCell('A5').cellValue = info.name
                    getCell('C5').cellValue = info.idCard
                    getCell('E5').cellValue = info.agency
                    getCell('G5').cellValue = info.czName
                    getCell('K5').cellValue = info.opTime
                    it
                }

                if (records != null) {
                    var index = 8
                    var copyIndex = index
                    for (r in records) {
                        sheet.getOrCopyRow(index++, copyIndex, true).with {
                            if (JfxxTotalRecord.isInstance(r)) {
                                getCell('A').cellValue = ''
                                getCell('B').cellValue = '合计'
                            } else {
                                getCell('A').cellValue = "${index - copyIndex}"
                                getCell('B').cellValue = r.year
                            }
                            getCell('C').cellValue = r.grjf
                            getCell('D').cellValue = r.sjbt
                            getCell('E').cellValue = r.sqbt
                            getCell('F').cellValue = r.xjbt
                            getCell('G').cellValue = r.zfdj
                            getCell('H').cellValue = r.jtbz
                            getCell('I').cellValue = r.tbymbz
                            if (JfxxTotalRecord.isInstance(r)) {
                                getCell('J').cellValue = '总计'
                                getCell('L').cellValue = (r as JfxxTotalRecord).total
                            } else {
                                getCell('J').cellValue = r.sbjg.join('|')
                                getCell('L').cellValue = r.hbrq.join('|')
                            }
                        }
                    }
                }
                workbook.save(Paths.get(dir, info.name + "缴费查询单.xlsx"))
            }
        }
    }
}
