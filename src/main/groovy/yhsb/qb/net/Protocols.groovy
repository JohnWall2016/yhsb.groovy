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
class InEnvelope<T extends ToXml> implements ToXml {
    @Attribute('soap:encodingStyle')
    String encodingStyle = 'http://schemas.xmlsoap.org/soap/encoding/'

    @Node('soap:Header')
    Header header

    @Node('soap:Body')
    Body<T> body

    InEnvelope(String funId, T params) {
        header = new Header(funId)
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
class EmptyParams implements ToXml {}
