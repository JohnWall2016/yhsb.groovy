package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels
import yhsb.cjb.net.Xzqh

import java.nio.file.Files
import java.nio.file.Paths

@Command(description = '按居保单位分组excel表格程序')
class SplitExcel extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new SplitExcel()).execute(args)
    }

    @Parameters(description = "导入处理文件")
    String inputFile

    @Parameters(description = "开始行(从1开始)")
    int startRow

    @Parameters(description = "结束行(包含在内)")
    int endRow

    @Parameters(description = "行政区划所在列, 例如: H")
    String columnName

    @Parameters(description = "导出文件目录")
    String outputDir

    @Override
    void run() {
        def fileName = Paths.get(inputFile).fileName.toString()

        def workbook = Excels.load(inputFile)
        def sheet = workbook.getSheetAt(0)

        Set<String> dwmcs = []

        for (r in (startRow - 1)..(endRow - 1)) {
            def row = sheet.getRow(r)
            def value = row.getCell(columnName).value
            for (re in Xzqh.regExps) {
                def m = value =~ re
                if (m.find()) {
                    dwmcs.add(m.group(2))
                    break
                }
            }
        }

        if (dwmcs.size() > 0) {
            if (Files.notExists(Paths.get(outputDir))) {
                Files.createDirectory(Paths.get(outputDir))
            }
        }

        dwmcs.each { mc ->
            println "生成 $mc 表格"

            def outWorkbook = Excels.load(inputFile)
            def outSheet = outWorkbook.getSheetAt(0)

            def start = startRow - 1
            def endRow = endRow
            while (start < endRow) {
                def end = start
                while (end < endRow) {
                    if (outSheet.getRow(end).getCell(columnName).value =~ /^湘潭市雨湖区$mc/) {
                        break
                    }
                    end += 1
                }
                def count = end - start
                if (count > 0) {
                    outSheet.deleteRows(start, count)
                    endRow -= count
                } else {
                    start += 1
                }
            }

            outWorkbook.save(Paths.get(outputDir, "$mc$fileName"))
        }
    }
}
