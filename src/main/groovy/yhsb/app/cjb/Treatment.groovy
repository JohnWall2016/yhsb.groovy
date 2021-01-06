package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels
import yhsb.cjb.net.Dyfh
import yhsb.cjb.net.DyfhQuery
import yhsb.cjb.net.Session

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
        @Parameters(description = '报表生成日期: 格式 YYYYMMDD, 如 20210101')
        String date

        private static final String outDir = 'D:\\待遇核定'

        private static final String template = "$outDir\\信息核对报告表模板.xlsx"

        @Override
        void run() {
            def result = Session.use {
                it.sendService(new DyfhQuery('', '0'))
                it.getResult(Dyfh)
            }

            def workbook = Excels.load(template)
            def sheet = workbook.getSheetAt(0)
            int startRow = 3, currentRow = 3

            result.each {dyfh ->
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
                workbook.save("$outDir\\信息核对报告表${date}.xlsx")
            }
        }
    }

    @Command(name = 'split', description = '对下载的信息表分组并生成养老金计算表')
    static class Split extends CommandWithHelp {
        @Override
        void run() {

        }
    }
}
