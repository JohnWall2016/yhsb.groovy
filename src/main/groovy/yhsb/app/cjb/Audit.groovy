package yhsb.app.cjb

import picocli.CommandLine
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.DateRange
import yhsb.base.util.DateTime
import yhsb.base.util.Excels
import yhsb.base.util.Export
import yhsb.cjb.db.FpHistoryData
import yhsb.cjb.db.Jzfp2020Db
import yhsb.cjb.net.Cbsh
import yhsb.cjb.net.CbshQuery
import yhsb.cjb.net.JbKind
import yhsb.cjb.net.Session

import java.nio.file.Paths

@CommandLine.Command(description = '参保审核与参保身份变更程序')
class Audit extends CommandWithHelp implements DateRange, Export {
    static void main(String[] args) {
        //println args
        new CommandLine(new Audit()).execute(args)
    }

    private static final String outputDir = 'D:\\精准扶贫\\'

    private static final String template = '批量信息变更模板.xls'

    private static class ChangeInfo {
        String idCard
        String name
        String jbsf
    }

    @Override
    void run() {
        def startDate = DateTime.toDashedDate(startDate)
        def endDate = endDate ? DateTime.toDashedDate(endDate) : ''

        def timeSpan = endDate ? "${startDate}_$endDate" : startDate
        println timeSpan

        def result = Session.use {
            it.sendService(new CbshQuery(startDate, endDate))
            it.getResult(Cbsh)
        }

        println "共计 ${result.size()} 条"

        if (!result.isEmpty()) {
            List<ChangeInfo> changeList = []

            Jzfp2020Db.datastore.withNewSession {
                for (cbsh in result) {
                    def info = FpHistoryData.find {
                        idCard == cbsh.idCard
                    }
                    if (info) {
                        println "${cbsh.idCard} ${cbsh.name.padRight(6)} ${cbsh.birthDay}  " +
                                "${info.jbrdsf ?: ""} ${cbsh.name != info.name ? info.name : ''}"
                        changeList.add(
                                new ChangeInfo(
                                        idCard: cbsh.idCard,
                                        name: cbsh.name,
                                        jbsf: JbKind.map.inverse().getOrDefault(info.jbrdsf, "")
                                )
                        )
                    }
                }
            }

            if (export && !changeList.empty) {
                def workbook = Excels.load(Paths.get(outputDir, template))
                def sheet = workbook.getSheetAt(0)
                int index = 1, copyIndex = 1

                changeList.each { chInfo ->
                    sheet.getOrCopyRow(index++, copyIndex, false).with {
                        getCell('B').cellValue = chInfo.idCard
                        getCell('E').cellValue = chInfo.name
                        getCell('J').cellValue = chInfo.jbsf
                    }
                }

                workbook.save(Paths.get(outputDir, "批量信息变更${timeSpan}.xls"))
            }
        }
    }
}
