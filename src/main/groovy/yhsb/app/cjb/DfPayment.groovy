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
import yhsb.cjb.net.Session

@Command(description = '代发数据导出制表程序', subcommands = [PersonList])
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

        @Override
        void run() {
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

                    sheet.getOrCopyRow(currentRow++, startRow).with {
                        getCell('A').cellValue = currentRow - startRow
                        getCell('B').cellValue = dfry.csName
                        getCell('C').cellValue = dfry.name
                        getCell('D').cellValue = dfry.idCard
                        getCell('E').cellValue = dfry.startYearMonth
                        getCell('F').cellValue = dfry.standard
                        getCell('G').cellValue = dfry.type.toString()
                        getCell('H').cellValue = dfry.dfState.toString()
                        getCell('I').cellValue = dfry.cbState.toString()
                        getCell('J').cellValue = dfry.endYearMonth
                        if (dfry.totalPayed) {
                            getCell('K').cellValue = dfry.totalPayed
                            payedSum += dfry.totalPayed
                        }
                        getCell('L').cellValue = payAmount
                        sum += payAmount
                    }
                }

                sheet.getOrCopyRow(currentRow, startRow).with {
                    getCell('A').cellValue = ''
                    getCell('C').cellValue = '共计'
                    getCell('D').cellValue = currentRow - startRow
                    getCell('F').cellValue = ''
                    getCell('J').cellValue = '合计'
                    getCell('K').cellValue = payedSum
                    getCell('L').cellValue = sum
                }

                workbook.save(personListTemplate.insertBeforeLast("(${new DfType(type)}${all ? 'ALL' : ''})$date"))
            }
        }
    }
}
