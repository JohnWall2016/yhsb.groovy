package yhsb.app.qb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels
import yhsb.qb.net.NoUKeyWorkerAdd
import yhsb.qb.net.NoUKeyWorkerAddQuery
import yhsb.qb.net.NoUKeyWorkerContinue
import yhsb.qb.net.NoUKeyWorkerContinueQuery
import yhsb.qb.net.NoUKeyWorkerJoinInChangSha
import yhsb.qb.net.NoUKeyWorkerJoinInChangShaQuery
import yhsb.qb.net.NoUKeyWorkerJoinInProvince
import yhsb.qb.net.NoUKeyWorkerJoinInProvinceQuery
import yhsb.qb.net.NoUKeyWorkerStop
import yhsb.qb.net.NoUKeyWorkerStopQuery
import yhsb.qb.net.Session

@Command(description = '非数字证书网上审核数据查询程序')
class NoUKeyAudit extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new NoUKeyAudit()).execute(args)
    }

    @Parameters(paramLabel = 'companyList', description = "承诺书签订单位名册")
    String companyList

    static class CompanyInfo {
        String code
        String name
        String contact
        String phone
    }

    Map<String, CompanyInfo> loadCompanyInfo() {
        var workbook = Excels.load(companyList)
        var sheet = workbook.getSheetAt(0)
        LinkedHashMap<String, CompanyInfo> map = []
        for (row in sheet.rowIterator()) {
            String code = row.getCell('B')?.value?.trim()
            if (code ==~ /\d{12}/) {
                map[code] = new CompanyInfo(
                        code: code,
                        name: row.getCell('C')?.value?.trim() ?: '',
                        contact: row.getCell('D')?.value?.trim() ?: '',
                        phone: row.getCell('E')?.value?.trim() ?: '',
                )
            }
        }
        map
    }

    @Override
    void run() {
        var companyInfo = loadCompanyInfo()
        var modules = [
                '在职人员新增'  : [new NoUKeyWorkerAddQuery(), NoUKeyWorkerAdd],
                '在职人员停保'  : [new NoUKeyWorkerStopQuery(), NoUKeyWorkerStop],
                '在职人员续保'  : [new NoUKeyWorkerContinueQuery(), NoUKeyWorkerContinue],
                '在职省内接续申请': [new NoUKeyWorkerJoinInProvinceQuery(), NoUKeyWorkerJoinInProvince],
                '长沙接续申请'  : [new NoUKeyWorkerJoinInChangShaQuery(), NoUKeyWorkerJoinInChangSha],
        ]

        int width = 60

        Session.use('qqb') {
            for (module in modules) {
                println "查询 ${module.key}".bar(width)
                var map = new LinkedHashMap<>(companyInfo)
                it.sendService(module.value[0] as yhsb.qb.net.Parameters)
                var result = it.getResult(module.value[1] as Class)
                var list = result.resultSet?.rowList ?: []
                println "共计查询到 ${list.size()} 条记录，其中承诺单位如下：\n"
                for (row in list) {
                    var info = row as yhsb.qb.net.CompanyInfo
                    var company = map.remove(info.companyCode)
                    if (company) {
                        var moduleName = module.key
                        println "$moduleName ${company.code} ${company.name} ${company.contact} ${company.phone}"
                    }
                }
                println ""
            }
        }
    }
}
