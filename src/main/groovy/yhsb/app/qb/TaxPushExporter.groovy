package yhsb.app.qb

import picocli.CommandLine
import picocli.CommandLine.Command
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.DateTime
import yhsb.base.util.Excels
import yhsb.qb.net.Session
import yhsb.qb.net.TaxPushCompanyPayInfo
import yhsb.qb.net.TaxPushCompanyPayInfoQuery
import yhsb.qb.net.TaxPushPersonalPayInfo
import yhsb.qb.net.TaxPushPersonalPayInfoQuery
import yhsb.qb.net.TaxPushSpecialPayInfo
import yhsb.qb.net.TaxPushSpecialPayInfoQuery

import java.nio.file.Files
import java.nio.file.Path

@Command(description = '查询税务推送数据导出程序',
        subcommands = [CompanyPayInfoExporter, SpecialPayInfoExporter, PersonalPayInfoExporter])
class TaxPushExporter extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new TaxPushExporter()).execute(args)
    }

    private static final String templateDir = "D:\\企保数据"
    private static final String exportDir = "D:\\企保数据\\导出目录"
    private static final String companyTemplate = "雨湖区用人单位税务缴费导入明细表.xls"
    private static final String specialTemplate = "雨湖区用人单位税务缴费导入明细表（特殊补缴）.xls"
    private static final String personalTemplate = "雨湖区灵活就业缴费导入明细表.xls"

    @Override
    void run() {
        CommandLine.usage(new TaxPushExporter(), System.out)
    }

    private static String getExportFileName(String template) {
        String export
        int i = 1
        var date = DateTime.format()
        while (true) {
            export = "$exportDir\\${template.insertBeforeLast( "${date}-${i}")}"
            if (!Files.exists(Path.of(export)))
                break
            i += 1
        }
        export
    }

    @Command(name = 'company', description = '单位缴费信息')
    static class CompanyPayInfoExporter extends CommandWithHelp {
        @Override
        void run() {
            Session.use('qqb2') {sess ->
                sess.sendService(new TaxPushCompanyPayInfoQuery())
                var result = sess.getResult(TaxPushCompanyPayInfo)

                if (!result.resultSet?.empty) {
                    var workbook = Excels.load("$templateDir\\$companyTemplate")
                    var sheet = workbook.getSheetAt(0)
                    int startRow = 3, currentRow = 3

                    result.resultSet.each {info ->
                        sheet.getOrCopyRow(currentRow ++, startRow).with {
                            getCell('A').cellValue = currentRow - startRow
                            getCell('B').cellFormula = 'NOW()'
                            getCell('C').cellValue = info.companyCode
                            getCell('D').cellValue = info.companyName
                            getCell('E').cellValue = info.serialNumber
                            getCell('F').cellValue = info.peroidStartTime
                            getCell('G').cellValue = info.overdueFine
                            getCell('H').cellValue = info.interest
                            getCell('I').cellValue = info.companyTotalActualPayment
                            getCell('J').cellValue = info.personalTotalActualPayment
                        }
                    }

                    sheet.getRow(currentRow).with {
                        getCell('C').cellValue = currentRow - startRow
                        getCell('G').cellFormula = "SUM(G${startRow + 1}:G${currentRow})"
                        getCell('H').cellFormula = "SUM(H${startRow + 1}:H${currentRow})"
                        getCell('I').cellFormula = "SUM(I${startRow + 1}:I${currentRow})"
                        getCell('J').cellFormula = "SUM(J${startRow + 1}:J${currentRow})"
                    }

                    var fileName = getExportFileName(companyTemplate)
                    println fileName
                    workbook.save(fileName)
                }
            }
        }
    }

    @Command(name = 'special', description = '特殊缴费信息')
    static class SpecialPayInfoExporter extends CommandWithHelp {
        @Override
        void run() {
            Session.use('qqb2') {sess ->
                sess.sendService(new TaxPushSpecialPayInfoQuery())
                var result = sess.getResult(TaxPushSpecialPayInfo)

                if (!result.resultSet?.empty) {
                    var workbook = Excels.load("$templateDir\\$specialTemplate")
                    var sheet = workbook.getSheetAt(0)
                    int startRow = 3, currentRow = 3

                    result.resultSet.each {info ->
                        sheet.getOrCopyRow(currentRow ++, startRow).with {
                            getCell('A').cellValue = currentRow - startRow
                            getCell('B').cellValue = info.serialNumber
                            getCell('C').cellValue = info.companyCode
                            getCell('D').cellValue = info.companyName
                            getCell('E').cellValue = info.shouldPaySum
                            getCell('F').cellValue = info.actualPaySum
                            getCell('G').cellValue = info.memo
                            getCell('H').cellFormula = 'NOW()'
                        }
                    }

                    sheet.getRow(currentRow).with {
                        getCell('C').cellValue = currentRow - startRow
                        getCell('E').cellFormula = "SUM(E${startRow + 1}:E${currentRow})"
                        getCell('F').cellFormula = "SUM(F${startRow + 1}:F${currentRow})"
                    }

                    var fileName = getExportFileName(specialTemplate)
                    println fileName
                    workbook.save(fileName)
                }
            }
        }
    }


    @Command(name = 'personal', description = '灵活就业缴费信息')
    static class PersonalPayInfoExporter extends CommandWithHelp {
        @Override
        void run() {
            Session.use('qqb2') {sess ->
                sess.sendService(new TaxPushPersonalPayInfoQuery())
                var result = sess.getResult(TaxPushPersonalPayInfo)

                if (!result.resultSet?.empty) {
                    var workbook = Excels.load("$templateDir\\$personalTemplate")
                    var sheet = workbook.getSheetAt(0)
                    int startRow = 3, currentRow = 3

                    result.resultSet.each {info ->
                        sheet.getOrCopyRow(currentRow ++, startRow).with {
                            getCell('A').cellValue = currentRow - startRow
                            getCell('B').cellFormula = 'NOW()'
                            getCell('C').cellValue = info.peroidStartTime
                            getCell('D').cellValue = info.peroidEndTime
                            getCell('E').cellValue = info.months
                            getCell('F').cellValue = info.actualPaySum
                            getCell('G').cellValue = info.pid
                            getCell('H').cellValue = info.idCard
                            getCell('I').cellValue = info.name
                            getCell('J').cellValue = info.taxStampNumber
                            getCell('K').cellValue = info.companyName
                        }
                    }

                    sheet.getRow(currentRow).with {
                        getCell('C').cellValue = currentRow - startRow
                        getCell('F').cellFormula = "SUM(F${startRow + 1}:F${currentRow})"
                    }

                    var fileName = getExportFileName(personalTemplate)
                    println fileName
                    workbook.save(fileName)
                }
            }
        }
    }
}
