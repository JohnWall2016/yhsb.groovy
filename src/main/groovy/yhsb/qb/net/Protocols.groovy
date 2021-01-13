package yhsb.qb.net

import groovy.transform.ToString
import yhsb.base.util.Attribute
import yhsb.base.util.NS
import yhsb.base.util.Namespaces
import yhsb.base.util.Node
import yhsb.base.util.Spread
import yhsb.base.util.ToXml

@ToString
@Namespaces([@NS(name = 'soap', value = 'http://schemas.xmlsoap.org/soap/envelope/')])
@Node('soap:Envelope')
class InEnvelope<T extends Parameters> implements ToXml {
    @Attribute('soap:encodingStyle')
    String encodingStyle = 'http://schemas.xmlsoap.org/soap/encoding/'

    @Node('soap:Header')
    Header header

    @Node('soap:Body')
    Body<T> body

    InEnvelope(T params) {
        header = new Header(params.funId)
        body = new Body<>(business: params)
    }

    void setUser(String user) {
        header.system.userParams.user = user
    }

    void setPassword(String password) {
        header.system.userParams.password = password
    }
}

@ToString
class Header implements ToXml {
    @Node('in:system')
    @Namespaces([@NS(name = 'in', value = 'http://www.molss.gov.cn/')])
    System system

    Header(String funId) {
        system = new System(
                userParams: new UserParams(funId: funId)
        )
    }
}

@ToString
class System implements ToXml {
    @Spread @Node('para')
    UserParams userParams
}

@ToString
class UserParams implements ToXml {
    @Attribute('usr')
    String user

    @Attribute('pwd')
    String password

    @Attribute('funid')
    String funId
}

@ToString
class Body<T extends ToXml> implements ToXml {
    @Node('in:business')
    @Namespaces([@NS(name = 'in', value = 'http://www.molss.gov.cn/')])
    T business
}

@ToString
class Parameters implements ToXml {
    transient String funId

    Parameters(String funId) {
        this.funId = funId
    }
}

@ToString
@Namespaces([@NS(name = 'soap', value = 'http://schemas.xmlsoap.org/soap/envelope/')])
@Node('soap:Envelope')
class OutEnvelope<T> {
    @Attribute('soap:encodingStyle')
    String encodingStyle = 'http://schemas.xmlsoap.org/soap/encoding/'

    @Node('soap:Header')
    OutHeader header

    @Node('soap:Body')
    OutBody<T> body
}

class OutHeader {
    @Attribute('sessionID')
    String sessionId

    @Attribute('message')
    String message
}

@ToString
class OutBody<T> {
    @Node('out:business')
    @Namespaces([@NS(name = 'out', value = 'http://www.molss.gov.cn/')])
    OutBusiness<T> result
}

@ToString
class OutBusiness<T> {
    @Node('result')
    Result result

    @Node('resultset')
    ResultSet<T> resultSet
}


@ToString
class Result {
    @Attribute('result')
    String result

    @Attribute('row_count')
    String rowCount

    @Attribute('querysql')
    String querySql
}

@ToString
class ResultSet<T> implements Iterable<T> {
    @Attribute('name')
    String name

    @Node('row')
    List<T> rowList

    @Override
    Iterator<T> iterator() {
        rowList?.iterator() ?: [] as Iterator<T>
    }
}

@ToString
class Login extends Parameters {
    Login() {
        super('F00.00.00.00|192.168.1.110|PC-20170427DGON|00-05-0F-08-1A-34')
    }
}

class ClientSql extends Parameters {
    ClientSql(String funId, String functionId, String sql = '') {
        super(funId)

        params = new Params(functionId: functionId, clientSql: sql)
    }

    @Spread @Node('para')
    Params params

    static class Params implements ToXml {
        @Attribute('startrow')
        String startRow = '1'

        @Attribute('row_count')
        String rowCount = '-1'

        @Attribute('pagesize')
        String pageSize = '500'

        @Attribute('clientsql')
        String clientSql = ''

        @Attribute('functionid')
        String functionId = ''
    }
}

class SncbryQuery extends ClientSql {
    SncbryQuery(String idCard) {
        super('F00.01.03', 'F27.06', "( aac002 = &apos;$idCard&apos;)")
    }
}

@ToString
class Sncbry implements ToXml {
    @Attribute('sac100')
    String pid // 个人编号

    @Attribute('aac003')
    String name

    @Attribute('aac002')
    String idCard

    @Attribute('aab300')
    String agency

    @Attribute('sab100')
    String agencyId // 单位编号
}