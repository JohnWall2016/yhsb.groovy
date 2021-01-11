package yhsb.qb.net

import groovy.xml.XmlSlurper
import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket
import yhsb.base.util.ToXml

class Session extends HttpSocket {
    private final String userId
    private final String password
    private final String charset = 'GBK'

    Session(String host, int port, String userId, String password) {
        super(host, port, charset)
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

    public <T extends ToXml> void sendService(InEnvelope<T> req) {
        request(toService(req.with {
            user = this.userId
            password = this.password
            it
        }))
    }

    static <T> T fromJson(String xml, Class<T> classOfT) {
        new XmlSlurper().parseText(xml).toObject(classOfT)
    }

    public <T> T getResult(Class<T> classOfT) {
        def result = readBody()
        fromJson(result, classOfT)
    }
}
