package yhsb.app.qb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.base.util.Excels
import yhsb.base.util.UpInfoParametersWithIdCard
import yhsb.qb.net.Account
import yhsb.qb.net.AccountDetail
import yhsb.qb.net.AccountDetailQuery
import yhsb.qb.net.AccountQuery
import yhsb.qb.net.AccountTotalQuery
import yhsb.qb.net.AgencyCode
import yhsb.qb.net.AgencyCodeQuery
import yhsb.qb.net.RetiredPerson
import yhsb.qb.net.RetiredPersonQuery
import yhsb.qb.net.Session
import yhsb.qb.net.InProvincePerson
import yhsb.qb.net.InProvincePersonQuery

@Command(description = '企保信息查询程序', subcommands = [GrinfoQuery, UpdateAccountInfo])
class Query extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new Query()).execute(args)
    }

    @Override
    void run() {
        CommandLine.usage(new Query(), System.out)
    }

    @Command(name = 'qryInfo', description = '个人综合查询')
    static class GrinfoQuery extends CommandWithHelp {
        @Parameters(description = "身份证号码")
        String[] idCards

        @Override
        void run() {
            //println idCards
            if (idCards) {
                for (idCard in idCards) {
                    println '=' * 60
                    Session.use {
                        it.sendService(new AgencyCodeQuery())
                        def acResult = it.getResult(AgencyCode)

                        def getAgencyCode = { String name ->
                            (acResult.resultSet.find {
                                it.name == name
                            } as AgencyCode)?.code
                        }

                        it.sendService(new InProvincePersonQuery(idCard))
                        def ryResult = it.getResult(InProvincePerson)

                        ryResult.resultSet.each {ry ->
                            println ry
                            it.sendService(new RetiredPersonQuery(ry.idCard, getAgencyCode(ry.agencyName)))
                            // println it.readBody()
                            def rs = it.getResult(RetiredPerson)
                            rs.resultSet.each {
                                println it
                            }
                        }
                    }
                    println '=' * 60
                }
            }
        }
    }

    @Command(name = 'upAccountInfo', description = '更新个人账户信息')
    static class UpdateAccountInfo extends CommandWithHelp implements UpInfoParametersWithIdCard {
        @Override
        void run() {
            def workbook = Excels.load(excel)
            def sheet = workbook.getSheetAt(0)

            Session.use('qqb') {sess ->
                for (i in (startRow - 1) ..< (endRow)) {
                    def row = sheet.getRow(i)
                    def name = row.getCell(nameCol).value
                    def idCard = row.getCell(idCardCol).value

                    sess.sendService(new InProvincePersonQuery(idCard))
                    def perResult = sess.getResult(InProvincePerson)
                    def agencyName = ''
                    perResult.resultSet?.each {
                        agencyName = it.agencyName
                    }

                    sess.sendService(new AccountQuery(idCard))
                    def accResult = sess.getResult(Account)
                    AccountDetail accDetail = null
                    accResult.resultSet?.each {
                        sess.sendService(new AccountTotalQuery(it.id))
                        sess.readBody()
                        sess.sendService(new AccountDetailQuery(it.id))
                        def accDetailResult = sess.getResult(AccountDetail)
                        accDetail = accDetailResult.resultSet?.find {
                            it.year == '2020'
                        } as AccountDetail
                    }

                    println "$name $idCard $agencyName $accDetail"

                    row.with {
                        getOrCreateCell('F').cellValue = agencyName
                        getOrCreateCell('G').cellValue = accDetail?.months ?: ''
                        getOrCreateCell('H').cellValue = accDetail?.payoffSalary ?: ''
                        getOrCreateCell('I').cellValue = accDetail?.index ?: ''
                    }
                }
            }

            workbook.save(excel.insertBeforeLast('.up'))
        }
    }
}
