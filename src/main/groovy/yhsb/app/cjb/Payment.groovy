package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.DateTime
import yhsb.base.util.Excels
import yhsb.cjb.net.Cbzzfh
import yhsb.cjb.net.CbzzfhQuery
import yhsb.cjb.net.Cbzzfhgrxx
import yhsb.cjb.net.CbzzfhgrxxQuery
import yhsb.cjb.net.Cwzfgl
import yhsb.cjb.net.CwzfglQuery
import yhsb.cjb.net.Cwzfglry
import yhsb.cjb.net.CwzfglryQuery
import yhsb.cjb.net.Dyzzfh
import yhsb.cjb.net.DyzzfhQuery
import yhsb.cjb.net.Dyzzfhgrxx
import yhsb.cjb.net.DyzzfhgrxxQuery
import yhsb.cjb.net.Session

@Command(description = '财务支付单生成程序')
class Payment extends CommandWithHelp {
    static void main(String[] args) {
        new CommandLine(new Payment()).execute(args)
    }

    private final static String template = 'D:\\支付管理\\雨湖区居保个人账户返还表.xlsx'

    @Parameters(
            description = '发放年月: 格式 YYYYMM, 如 201901')
    String yearMonth

    @Parameters(
            description = '业务状态: 0-待支付, 1-已支付, 默认为：所有',
            defaultValue = ''
    )
    String state

    @Override
    void run() {
        def workbook = Excels.load(template)
        def sheet = workbook.getSheetAt(0)

        def (String year, String month) = DateTime.split(yearMonth)
        sheet.getCell('A1').cellValue = "${year}年${month.stripPrefix('0')}月个人账户返还表"

        def date = DateTime.format()
        def dateCh = DateTime.format('yyyy年M月d日')
        sheet.getCell('H2').cellValue = "制表时间：$dateCh"

        Session.use {sess ->
            int startRow = 4, currentRow = 4
            BigDecimal sum = 0

            sess.sendService(new CwzfglQuery(yearMonth, state))
            def result = sess.getResult(Cwzfgl)
            def items = result.data.items.sort {e1, e2 ->
                e1.payList - e2.payList
            }

            for (item in items) {
                if (item.objectType == '3') {
                    sess.sendService(new CwzfglryQuery(
                            item.payList.toString(),
                            item.yearMonth.toString(),
                            item.state,
                            item.type.value
                    ))
                    def ryResult = sess.getResult(Cwzfglry)
                    def ry = ryResult[0]

                    def reason = '', bankName = ''
                    sess.sendService(new DyzzfhQuery(ry.idCard))
                    def dyzzResult = sess.getResult(Dyzzfh)
                    if (!dyzzResult.empty) {
                        sess.sendService(new DyzzfhgrxxQuery(dyzzResult[0]))
                        def dyzzgrxxResult = sess.getResult(Dyzzfhgrxx)
                        if (!dyzzgrxxResult.empty) {
                            def info = dyzzgrxxResult[0]
                            reason = info.reason.name
                            bankName = info.bankType.name
                        }
                    } else {
                        sess.sendService(new CbzzfhQuery(ry.idCard))
                        def cbzzResult = sess.getResult(Cbzzfh)
                        if (!cbzzResult.empty) {
                            sess.sendService(new CbzzfhgrxxQuery(cbzzResult[0]))
                            def cbzzgrxxResult = sess.getResult(Cbzzfhgrxx)
                            if (!cbzzgrxxResult.empty) {
                                def info = cbzzgrxxResult[0]
                                reason = info.reason.name
                                bankName = info.bankType.name
                            }
                        }
                    }

                    def row = sheet.getOrCopyRow(currentRow++, startRow)
                    row.getCell('A').cellValue = currentRow - startRow
                    row.getCell('B').cellValue = ry.name
                    row.getCell('C').cellValue = ry.idCard

                    def type = ry.type.toString()
                    if (reason) {
                        type = "$type($reason)"
                    }
                    def amount = ry.amount
                    row.getCell('D').cellValue = type
                    row.getCell('E').cellValue = ry.payList
                    row.getCell('F').cellValue = amount
                    row.getCell('G').cellValue = amount.toChineseMoney()
                    row.getCell('H').cellValue = item.name
                    row.getCell('I').cellValue = item.account
                    row.getCell('J').cellValue = bankName

                    sum += amount
                }
            }

            def row = sheet.getOrCopyRow(currentRow, startRow)
            row.getCell('A').cellValue = '合计'
            row.getCell('F').cellValue = sum

            workbook.save(template.insertBeforeLast(date))
        }
    }
}
