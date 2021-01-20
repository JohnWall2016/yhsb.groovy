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
import yhsb.qb.net.JoinedPerson
import yhsb.qb.net.JoinedPersonGeneralQuery
import yhsb.qb.net.JoinedPersonPayDetail
import yhsb.qb.net.JoinedPersonPayDetailQuery
import yhsb.qb.net.JoinedPersonQuery
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

                        ryResult.resultSet.each { ry ->
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

            Session.use('qqb') { sess ->
                for (i in (startRow - 1)..<(endRow)) {
                    def row = sheet.getRow(i)
                    def name = row.getCell(nameCol).value
                    def idCard = row.getCell(idCardCol).value

                    sess.sendService(new InProvincePersonQuery(idCard))
                    def perResult = sess.getResult(InProvincePerson)
                    def agencyName = (perResult?.resultSet?.find {
                        //println "${it.agencyName} ${sess.agencyName}"
                        it.agencyName == sess.agencyName
                    } as InProvincePerson)?.agencyName ?: ''

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
                    if (accDetail) {
                        println "$name $idCard $agencyName $accDetail"
                        row.with {
                            getOrCreateCell('F').cellValue = agencyName
                            getOrCreateCell('G').cellValue = accDetail.months
                            getOrCreateCell('H').cellValue = accDetail.payoffSalary
                            getOrCreateCell('I').cellValue = accDetail.index
                        }
                    } else {
                        sess.sendService(new JoinedPersonQuery(idCard, sess.agencyCode))
                        def result = sess.getResult(JoinedPerson)

                        (result.resultSet?.find {
                            it.agencyCode == sess.agencyCode
                        } as JoinedPerson)?.with {
                            sess.sendService(new JoinedPersonGeneralQuery(id, agencyCode))
                            sess.readBody()
                            sess.sendService(new JoinedPersonPayDetailQuery(id, agencyCode))
                            def payResult = sess.getResult(JoinedPersonPayDetail)
                            def months = payResult.resultSet?.count {
                                it.period.startsWith('2020') && it.payMonths == 1
                            }?.toString() ?: ''
                            def payoffSalary = (payResult.resultSet?.find {
                                it.period.startsWith('2020') && it.payMonths == 1
                            } as JoinedPersonPayDetail )?.payoffSalary ?: ''

                            println "$name $idCard $agencyName $months $payoffSalary"

                            row.with {
                                getOrCreateCell('F').cellValue = agencyName
                                getOrCreateCell('G').cellValue = months
                                getOrCreateCell('H').cellValue = payoffSalary
                            }
                        }
                    }
                }
            }

            workbook.save(excel.insertBeforeLast('.up'))
        }
    }
}
