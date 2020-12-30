package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Parameters
import picocli.CommandLine.Command
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels

import java.nio.file.Paths

@Command(description = '生成清除身份excel文件程序')
class ClearSf extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new ClearSf()).execute(args)
    }

    @Parameters(description = "系统导出文件目录")
    String inputDir

    @Parameters(description = "导出批量文件目录")
    String outputDir

    static final int columnsPerExcel = 500

    static final String template = "D:\\个账管理\\2020年参保身份重置\\批量清除参保身份模板.xls"

    @Override
    void run() {
        new File(inputDir).eachFile {
            def m = it.name =~ /(?i)(.*).xls/
            if (m.find()) {
                def kind = m.group(1)
                println kind

                def workbook = Excels.load(Paths.get(inputDir, it.name))
                def sheet = workbook.getSheetAt(0)

                (1..sheet.lastRowNum).collate(columnsPerExcel).eachWithIndex { data, idx ->
                    println "  生成 $kind${idx+1}.xls"
                    def outWorkbook = Excels.load(template)
                    def outSheet = outWorkbook.getSheetAt(0)
                    int index = 1, copyIndex = 1
                    data.each {
                        def row = sheet.getRow(it)
                        def name = row.getCell('C').value
                        def idCard = row.getCell('E').value

                        outSheet.getOrCopyRow(index++, copyIndex).with {
                            getCell('B').cellValue = idCard
                            getCell('E').cellValue = name
                        }
                    }
                    outWorkbook.save(Paths.get(outputDir, "$kind${idx+1}.xls"))
                }
            }
        }
    }
}
