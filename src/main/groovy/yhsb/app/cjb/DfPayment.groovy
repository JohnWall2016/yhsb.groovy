package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import picocli.CommandLine.Option
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.DateTime
import yhsb.base.util.Excels
import yhsb.cjb.net.DfType
import yhsb.cjb.net.Dfry
import yhsb.cjb.net.DfryQuery
import yhsb.cjb.net.DfzfType
import yhsb.cjb.net.Dfzfd
import yhsb.cjb.net.DfzfdQuery
import yhsb.cjb.net.Dfzfdgrmx
import yhsb.cjb.net.DfzfdgrmxQuery
import yhsb.cjb.net.Dfzfdmx
import yhsb.cjb.net.DfzfdmxQuery
import yhsb.cjb.net.Session

import java.text.Collator

@Command(description = '代发数据导出制表程序', subcommands = [PersonList, PayList])
class DfPayment extends CommandWithHelp {
    static void main(String[] args) {
        new CommandLine(new DfPayment()).execute(args)
    }

    @Override
    void run() {
        CommandLine.usage(new DfPayment(), System.out)
    }

    private static final personListTemplate = 'D:\\代发管理\\雨湖区城乡居民基本养老保险代发人员名单.xlsx'

    private static final payListTemplate = 'D:\\代发管理\\雨湖区城乡居民基本养老保险代发人员支付明细.xlsx'

    @Command(name = 'personList', description = '正常代发人员名单导出')
    static class PersonList extends CommandWithHelp {
        @Parameters(
                description = '代发类型: 801 - 独生子女, 802 - 乡村教师, 803 - 乡村医生, 807 - 电影放映员')
        String type

        @Parameters(
                description = '代发年月: 格式 YYYYMM, 如 201901')
        String yearMonth

        @Option(names = ['-a', '--all'], description = '导出所有居保正常代发人员')
        boolean all = false

        @Option(names = ['-e', '--estimate'], description = '是否测算代发金额')
        boolean estimate = false

        @Override
        void run() {
            println '开始导出数据'

            def workbook = Excels.load(personListTemplate)
            def sheet = workbook.getSheetAt(0)

            int startRow = 3, currentRow = 3
            BigDecimal sum = 0, payedSum = 0

            def date = DateTime.format('yyyyMMdd')
            def dateCh = DateTime.format('yyyy年M月d日')
            sheet.getCell('G2').cellValue = "制表时间：$dateCh"

            Session.use { sess ->
                sess.sendService(new DfryQuery(type, '1', ''))
                def result = sess.getResult(Dfry)
                result.each { dfry ->
                    if (!dfry.pid) return

                    println("${dfry.name.padRight(8)}${dfry.idCard}")

                    if (!all && dfry.dfState.value != '1') return
                    if (dfry.dfState.value != '1' &&
                            !(dfry.dfState.value == '2' &&
                                    dfry.cbState.value == '1')) return

                    BigDecimal payAmount = 0
                    if (dfry.standard) {
                        def startYear = dfry.startYearMonth.intdiv(100)
                        def startMonth = dfry.startYearMonth % 100
                        startMonth -= 1
                        if (startMonth == 0) {
                            startYear -= 1
                            startMonth = 12
                        }
                        if (dfry.endYearMonth) {
                            startYear = dfry.endYearMonth.intdiv(100)
                            startMonth = dfry.endYearMonth % 100
                        }
                        def m = yearMonth =~ /^(\d\d\d\d)(\d\d)$/
                        if (m.find()) {
                            def endYear = Integer.parseInt(m.group(1))
                            def endMonth = Integer.parseInt(m.group(2))
                            payAmount = ((endYear - startYear) * 12 + endMonth - startMonth) * dfry.standard
                        }
                    } else if (type == '801' && !dfry.standard && dfry.totalPayed == 5000) {
                        return
                    }

                    sheet.getOrCopyRow(currentRow++, startRow, true).with {
                        getCell('A').cellValue = currentRow - startRow
                        getCell('B').cellValue = dfry.csName
                        getCell('C').cellValue = dfry.name
                        getCell('D').cellValue = dfry.idCard
                        getCell('E').cellValue = dfry.startYearMonth
                        getCell('F').cellValue = dfry.standard
                        getCell('G').cellValue = dfry.type
                        getCell('H').cellValue = dfry.dfState.toString()
                        getCell('I').cellValue = dfry.cbState.toString()
                        getCell('J').cellValue = dfry.endYearMonth ?: ""
                        getCell('K').cellValue = dfry.totalPayed ?: ""
                        payedSum += dfry.totalPayed ?: 0
                        if (estimate) getCell('L').cellValue = payAmount ?: ""
                        sum += payAmount
                    }
                }

                sheet.getOrCopyRow(currentRow, startRow, true).with {
                    getCell('A').cellValue = ''
                    getCell('C').cellValue = '共计'
                    getCell('D').cellValue = currentRow - startRow
                    getCell('E').cellValue = ''
                    getCell('F').cellValue = ''
                    getCell('J').cellValue = '合计'
                    getCell('K').cellValue = payedSum ?: ""
                    if (estimate) getCell('L').cellValue = sum ?: ""
                    null
                }

                workbook.save(personListTemplate.insertBeforeLast("(${new DfType(type)}${all ? 'ALL' : ''})$date"))
            }

            println '结束数据导出'
        }
    }

    @Command(name = 'payList', description = '代发支付明细导出')
    static class PayList extends CommandWithHelp {
        @Parameters(
                description = '业务类型: DF0001 - 独生子女, DF0002 - 乡村教师, DF0003 - 乡村医生, DF0007 - 电影放映员')
        String type

        @Parameters(
                description = '支付年月: 格式 YYYYMM, 如 201901')
        String date

        static class Item {
            String csName, name, idCard, type
            int yearMonth, startDate, endDate
            BigDecimal amount
        }

        @Override
        void run() {
            List<Item> items = []
            BigDecimal total = 0
            def zfType = new DfzfType(type)

            Session.use { sess ->
                sess.sendService(new DfzfdQuery(type, date))
                def result = sess.getResult(Dfzfd)
                result.each { dfzfd ->
                    if (dfzfd.typeCh) {
                        sess.sendService(new DfzfdmxQuery(dfzfd.payList))
                        def mxResult = sess.getResult(Dfzfdmx)
                        mxResult.each { dfzfdmx ->
                            if (dfzfdmx.csName && dfzfdmx.flag == '0') {
                                sess.sendService(new DfzfdgrmxQuery(dfzfdmx.pid, dfzfdmx.payList, dfzfdmx.personalPayList))
                                def grmxResult = sess.getResult(Dfzfdgrmx)
                                def startDate = null, endDate = null
                                def count = grmxResult.size()
                                if (count > 0) {
                                    startDate = grmxResult[0].date
                                    if (count > 2) {
                                        endDate = grmxResult[count - 2].date
                                    } else {
                                        endDate = startDate
                                    }
                                }
                                total += dfzfdmx.amount
                                items.add(
                                        new Item(
                                                csName: dfzfdmx.csName,
                                                name: dfzfdmx.name,
                                                idCard: dfzfdmx.idCard,
                                                type: zfType.toString(),
                                                yearMonth: dfzfdmx.yearMonth,
                                                startDate: startDate ?: 0,
                                                endDate: endDate ?: 0,
                                                amount: dfzfdmx.amount
                                        )
                                )
                            }
                        }
                    }
                }
            }

            items.sort(true) { e1, e2 ->
                Collator.getInstance(Locale.CHINESE).compare(e1.csName, e2.csName)
            }

            def workbook = Excels.load(payListTemplate)
            def sheet = workbook.getSheetAt(0)
            int startRow = 3, currentRow = 3
            def date = DateTime.format('yyyyMMdd')
            def dateCh = DateTime.format('yyyy年M月d日')
            sheet.getCell('G2').cellValue = "制表时间：$dateCh"

            for (item in items) {
                sheet.getOrCopyRow(currentRow++, startRow, true).with {
                    getCell('A').cellValue = currentRow - startRow
                    getCell('B').cellValue = item.csName
                    getCell('C').cellValue = item.name
                    getCell('D').cellValue = item.idCard
                    getCell('E').cellValue = item.type
                    getCell('F').cellValue = item.yearMonth ?: ''
                    getCell('G').cellValue = item.startDate ?: ''
                    getCell('H').cellValue = item.endDate ?: ''
                    getCell('I').cellValue = item.amount
                }
            }

            sheet.getOrCopyRow(currentRow, startRow, true).with {
                getCell('A').cellValue = ''
                getCell('C').cellValue = '共计'
                getCell('D').cellValue = currentRow - startRow
                getCell('F').cellValue = ''
                getCell('G').cellValue = ''
                getCell('H').cellValue = '合计'
                getCell('I').cellValue = total
            }

            workbook.save(payListTemplate.insertBeforeLast("(${zfType.toString()})$date"))
        }
    }
}
