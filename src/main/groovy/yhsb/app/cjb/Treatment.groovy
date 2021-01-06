package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.DateTime
import yhsb.base.util.Excels
import yhsb.cjb.net.BankInfo
import yhsb.cjb.net.BankInfoQuery
import yhsb.cjb.net.Dyfh
import yhsb.cjb.net.DyfhQuery
import yhsb.cjb.net.Session
import yhsb.cjb.net.Xzqh

import java.nio.file.Files
import java.nio.file.Path

@Command(description = '信息核对报告表和养老金计算表生成程序', subcommands = [Download, Split])
class Treatment extends CommandWithHelp {
    static void main(String[] args) {
        new CommandLine(new Treatment()).execute(args)
    }

    @Override
    void run() {
        CommandLine.usage(new Treatment(), System.out)
    }

    @Command(name = 'download', description = '从业务系统下载信息核对报告表')
    static class Download extends CommandWithHelp {
        @Parameters(description = '报表生成日期, 格式: YYYYMMDD, 例如: 20210101')
        String date

        private static final String rootDir = 'D:\\待遇核定'

        private static final String template = "$rootDir\\信息核对报告表模板.xlsx"

        @Override
        void run() {
            def result = Session.use {
                it.sendService(new DyfhQuery('', '0'))
                it.getResult(Dyfh)
            }

            def workbook = Excels.load(template)
            def sheet = workbook.getSheetAt(0)
            int startRow = 3, currentRow = 3

            result.each { dyfh ->
                def index = currentRow - startRow + 1

                println "$index ${dyfh.idCard} ${dyfh.name} ${dyfh.bz}"

                sheet.getOrCopyRow(currentRow++, startRow).with {
                    getCell('A').cellValue = index
                    getCell('B').cellValue = dyfh.name
                    getCell('C').cellValue = dyfh.idCard
                    getCell('D').cellValue = dyfh.xzqh
                    getCell('E').cellValue = dyfh.payAmount
                    getCell('F').cellValue = dyfh.payMonth
                    getCell('G').cellValue = '是 [ ]'
                    getCell('H').cellValue = '否 [ ]'
                    getCell('I').cellValue = '是 [ ]'
                    getCell('J').cellValue = '否 [ ]'
                    getCell('L').cellValue = dyfh.bz
                }
                workbook.save("$rootDir\\信息核对报告表${date}.xlsx")
            }
        }
    }

    @Command(name = 'split', description = '对下载的信息表分组并生成养老金计算表')
    static class Split extends CommandWithHelp {
        @Parameters(description = '报表生成日期, 格式: YYYYMMDD, 例如: 20210101')
        String date

        @Parameters(description = '开始行(从1开始)')
        int startRow

        @Parameters(description = '结束行(包含在内)')
        int endRow

        private static final String rootDir = 'D:\\待遇核定'

        private static final String payInfoTemplate = 'D:\\待遇核定\\养老金计算表模板.xlsx'

        @Override
        void run() {
            def (String year, String month) = DateTime.split(date)
            if (month[0] == '0') month = month[1..-1]

            def inputExcel = "$rootDir\\信息核对报告表${date}.xlsx"
            def outputDir = "$rootDir\\${year}年${month}月待遇核定数据"
            def infoExcel = "$rootDir\\信息核对报告表模板.xlsx"

            def workbook = Excels.load(inputExcel)
            def sheet = workbook.getSheetAt(0)

            println '生成分组映射表'
            def map = new HashMap<String, Map<String, List<Integer>>>()
            for (index in (startRow - 1)..(endRow - 1)) {
                def xzqh = sheet.getCell(index, 'D').value
                def (String dw, String cs) = Xzqh.getDwAndCsName(xzqh)
                if (dw == null) {
                    throw new Exception("未匹配行政区划: $xzqh")
                } else {
                    if (!map.containsKey(dw)) {
                        map[dw] = new HashMap<String, List<Integer>>()
                    }
                    if (!map[dw].containsKey(cs)) {
                        map[dw][cs] = [index]
                    } else {
                        map[dw][cs].add(index)
                    }
                }
            }

            println '生成分组目录并分别生成信息核对报告表'
            if (Files.exists(Path.of(outputDir))) {
                Files.move(Path.of(outputDir), Path.of(outputDir + '.orig'))
            }
            Files.createDirectory(Path.of(outputDir))

            for (dw in map.keySet()) {
                println "$dw:"
                Files.createDirectory(Path.of(outputDir, dw))

                for (cs in map[dw].keySet()) {
                    println "  $cs: ${map[dw][cs]}"
                    Files.createDirectory(Path.of(outputDir, dw, cs))

                    def outWorkbook = Excels.load(infoExcel)
                    def outSheet = outWorkbook.getSheetAt(0)
                    int startRow = 3, currentRow = 3

                    map[dw][cs].each { rowIndex ->
                        def index = currentRow - startRow + 1
                        def inRow = sheet.getRow(rowIndex)

                        println "    $index ${inRow.getCell('C').value} " +
                                "${inRow.getCell('B').value}"

                        outSheet.getOrCopyRow(currentRow++, startRow).with {
                            getCell('A').cellValue = index
                            inRow.copyTo(it, 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'L')
                        }
                    }

                    outWorkbook.save(Path.of(outputDir, dw, cs, "${cs}信息核对报告表.xlsx"))
                }
            }

            println '\n按分组生成养老金养老金计算表'
            Session.use {sess ->
                for (dw in map.keySet()) {
                    for (cs in map[dw].keySet()) {
                        map[dw][cs].each {index ->
                            def row = sheet.getRow(index)
                            def name = row.getCell('B').value
                            def idCard = row.getCell('C').value
                            println "  $idCard $name"

                            try {
                                getPaymentInfoReport(sess, name, idCard, Path.of(outputDir, dw, cs))
                            } catch(Exception e) {
                                println "$idCard $name 获得养老金计算表岀错: $e"
                            }
                        }
                    }
                }
            }
        }

        private static void getPaymentInfoReport(Session sess, String name, String idCard, Path outDir, int retry = 3) {
            sess.sendService(new DyfhQuery(idCard, '0'))
            def result = sess.getResult(Dyfh)
            if (!result.empty) {
                sess.sendService(new BankInfoQuery(idCard))
                def bankInfoResult = sess.getResult(BankInfo)
                def payInfo = result[0].paymentInfoMatcher
                while (!payInfo) {
                    if (--retry > 0) {
                        payInfo = result[0].paymentInfoMatcher
                    } else {
                        throw new Exception('养老金计算信息无效')
                    }
                }
                def workbook = Excels.load(payInfoTemplate)
                workbook.getSheetAt(0).with {
                    getCell('A5').cellValue = payInfo.group(1)
                    getCell('B5').cellValue = payInfo.group(2)
                    getCell('C5').cellValue = payInfo.group(3)
                    getCell('F5').cellValue = payInfo.group(4)
                    getCell('I5').cellValue = payInfo.group(5)
                    getCell('L5').cellValue = payInfo.group(6)
                    getCell('A8').cellValue = payInfo.group(7)
                    getCell('B8').cellValue = payInfo.group(8)
                    getCell('C8').cellValue = payInfo.group(9)
                    getCell('E8').cellValue = payInfo.group(10)
                    getCell('F8').cellValue = payInfo.group(11)
                    getCell('G8').cellValue = payInfo.group(12)
                    getCell('H8').cellValue = payInfo.group(13)
                    getCell('I8').cellValue = payInfo.group(14)
                    getCell('J8').cellValue = payInfo.group(15)
                    getCell('K8').cellValue = payInfo.group(16)
                    getCell('L8').cellValue = payInfo.group(17)
                    getCell('M8').cellValue = payInfo.group(18)
                    getCell('A11').cellValue = payInfo.group(19)
                    getCell('B11').cellValue = payInfo.group(20)
                    getCell('C11').cellValue = payInfo.group(21)
                    getCell('D11').cellValue = payInfo.group(22)
                    getCell('E11').cellValue = payInfo.group(23)
                    getCell('F11').cellValue = payInfo.group(24)
                    getCell('G11').cellValue = payInfo.group(25)
                    getCell('H11').cellValue = payInfo.group(26)
                    getCell('I11').cellValue = payInfo.group(27)
                    getCell('J11').cellValue = payInfo.group(28)
                    getCell('K11').cellValue = payInfo.group(29)
                    getCell('L11').cellValue = payInfo.group(30)
                    getCell('M11').cellValue = payInfo.group(31)
                    getCell('I12').cellValue = DateTime.format('yyyy-MM-dd HH:mm:ss')

                    if (!bankInfoResult.empty) {
                        def bankInfo = bankInfoResult[0]
                        getCell('B15').cellValue = bankInfo.countName ?: ''
                        getCell('F15').cellValue = bankInfo.bankType?.toString() ?: ''

                        def card = ''
                        if (bankInfo.cardNumber) {
                            card = bankInfo.cardNumber
                            def l = card.length()
                            if (l > 7) {
                                card = card.substring(0, 3) + ''.padLeft(l - 7, '*') + card.substring(l - 4)
                            } else if (l > 4) {
                                card = ''.padLeft(l - 4, '*') + card.substring(l - 4)
                            }
                        }
                        getCell('J15').cellValue = card
                    } else {
                        getCell('B15').cellValue = '未绑定银行账户'
                    }
                }
                workbook.save(Path.of(outDir.toString(), "${name}[$idCard]养老金计算表.xlsx"))
            } else {
                throw new Exception('未查到该人员核定数据')
            }
        }
    }
}
