package yhsb.app.qb

import picocli.CommandLine
import yhsb.base.util.CommandWithHelp
import yhsb.qb.net.AgencyCode
import yhsb.qb.net.AgencyCodeQuery
import yhsb.qb.net.Ltxry
import yhsb.qb.net.LtxryQuery
import yhsb.qb.net.Session
import yhsb.qb.net.Sncbry
import yhsb.qb.net.SncbryQuery

@CommandLine.Command(description = '企保信息查询程序', subcommands = [GrinfoQuery])
class Query extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new Query()).execute(args)
    }

    @Override
    void run() {
        CommandLine.usage(new Query(), System.out)
    }

    @CommandLine.Command(name = 'qryInfo', description = '个人综合查询')
    static class GrinfoQuery extends CommandWithHelp {
        @CommandLine.Parameters(description = "身份证号码")
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

                        it.sendService(new SncbryQuery(idCard))
                        def ryResult = it.getResult(Sncbry)

                        ryResult.resultSet.each {ry ->
                            println ry
                            it.sendService(new LtxryQuery(ry.idCard, getAgencyCode(ry.agencyName)))
                            // println it.readBody()
                            def rs = it.getResult(Ltxry)
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
}
