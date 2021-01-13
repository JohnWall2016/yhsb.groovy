package yhsb.qb.net

import groovy.xml.XmlSlurper
import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket
import yhsb.base.util.Config
import yhsb.base.util.ToXml
import yhsb.base.util.reflect.GenericClass

import java.util.function.Function

class Session extends HttpSocket {
    private final String userId
    private final String password

    Session(String host, int port, String userId, String password) {
        super(host, port, 'GBK')
        this.userId = userId
        this.password = password
    }

    private final HashMap<String, String> cookies = new HashMap<>()

    HttpRequest createRequest() {
        def request = new HttpRequest('/sbzhpt/MainServlet', 'POST', charset)
        request.addHeader('SOAPAction', 'mainservlet')
            .addHeader('Content-Type', 'text/html;charset=GBK')
            .addHeader('Host', url)
            .addHeader('Connection', 'keep-alive')
            .addHeader('Cache-Control', 'no-cache')
        if (!cookies.isEmpty()) {
            request.addHeader(
                    'Cookie',
                    cookies.collect {"${it.key}=${it.value}"}.join('; ')
            )
        }
        request
    }

    HttpRequest buildRequest(String content) {
        def request = createRequest()
        request.addBody(content)
        request
    }

    void request(String content) {
        def request = buildRequest(content)
        write(request.getBytes())
    }

    static String toService(ToXml req) {
        "<?xml version=\"1.0\" encoding=\"GBK\"?>" +
                req.toXml()
    }

    public <T extends Parameters> void sendService(T params) {
        request(toService(new InEnvelope(params).with {
            user = this.userId
            it.password = this.password
            it
        }))
    }

    static <T> T fromXml(String xml, Class<T> classOfT, Class<Object> argClass) {
        new XmlSlurper().parseText(xml).toObject(new GenericClass<T>(classOfT, argClass))
    }

    public <T> OutBusiness<T> getResult(Class<T> classOfT) {
        def result = readBody()
        def outEnv = fromXml(result, OutEnvelope<T>, classOfT)
        outEnv.body.result
    }

    String login() {
        sendService(new Login())

        def header = readHeader()
        if (header.containsKey('set-cookie')) {
            header.getValues('set-cookie').each {
                def m = it =~ /([^=]+?)=(.+?);/
                if (m.find()) {
                    cookies[m.group(1)] = m.group(2)
                }
            }
        }
        readBody(header)
    }

    void logout() {

    }

    static <T> T use(String user = 'sqb', boolean autoLogin = true, Function<Session, T> func) {
        def usr = Config.qbSession.getConfig("users.$user")
        new Session(
                Config.qbSession.getString('host'),
                Config.qbSession.getInt('port'),
                usr.getString('id'),
                usr.getString('pwd')
        ).withCloseable {
            if (autoLogin) it.login()
            try {
                func(it)
            } finally {
                if (autoLogin) it.logout()
            }
        }
    }
}
