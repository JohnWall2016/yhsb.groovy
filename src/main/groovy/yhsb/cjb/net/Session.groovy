package yhsb.cjb.net

import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket

class Session extends HttpSocket {
    private final String userId
    private final String password

    Session(String host, int port, String userId, String password) {
        super(host, port)
    }

    private final HashMap<String, String> cookies = new HashMap<>()

    HttpRequest createRequest() {
        def request = new HttpRequest('/hncjb/reports/crud', 'POST')
        request.addHeader('Host', url)
                .addHeader('Connection', 'keep-alive')
                .addHeader('Accept', 'application/json, text/javascript, */*; q=0.01')
                .addHeader('Origin', "http://$url")
                .addHeader('X-Requested-With', 'XMLHttpRequest')
                .addHeader(
                        'User-Agent',
                        'Mozilla/5.0 (Windows NT 5.1) ' +
                                'AppleWebKit/537.36 (KHTML, like Gecko) ' +
                                'Chrome/39.0.2171.95 Safari/537.36'
                )
                .addHeader('Content-Type', 'multipart/form-data;charset=UTF-8')
                .addHeader('Referer', "http://$url/hncjb/pages/html/index.html")
                .addHeader('Accept-Encoding', 'gzip, deflate')
                .addHeader('Accept-Language', 'zh-CN,zh;q=0.8')
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


}


