package yhsb.app.cjb

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import yhsb.base.util.CommandWithHelp
import yhsb.cjb.net.Cbxx
import yhsb.cjb.net.CbxxQuery
import yhsb.cjb.net.Session

@Command(description = '城居保信息查询程序', subcommands = [ GrinfoQuery ])
class Query extends CommandWithHelp {
    static void main(String[] args) {
        //println args
        new CommandLine(new Query()).execute(args)
    }

    @Override
    void run() {
        CommandLine.usage(new Query(), System.out)
    }

    @Command(name = 'grinfo', description = '个人综合查询')
    static class GrinfoQuery extends CommandWithHelp {
        @Parameters(description = "身份证号码")
        String[] idCards

        @Override
        void run() {
            //println idCards
            if (idCards) {
                Session.use {sess ->
                    for (idCard in idCards) {
                        sess.sendService(new CbxxQuery(idCard))
                        def result = sess.getResult(Cbxx)
                        if (result.isEmpty()) {
                            println "$idCard 未在我区参保"
                        } else {
                            def info = result[0]
                            println "${info.idCard} ${info.name} ${info.jbState}" +
                                    "${info.dwName} ${info.csName}"
                        }
                    }
                }
            }
        }
    }
}
