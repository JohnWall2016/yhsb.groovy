package yhsb.cjb.net

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import groovy.transform.ToString
import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket
import yhsb.base.util.Config
import yhsb.base.util.Json
import yhsb.base.util.Jsonable

import java.util.function.Function

class Session extends HttpSocket {
    private final String userId
    private final String password

    Session(String host, int port, String userId, String password) {
        super(host, port)
        this.userId = userId
        this.password = password
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

    String toService(Request req) {
        def service = new JsonService(req, userId, password)
        service.toString()
    }

    String toService(String id) {
        toService(new Request(id))
    }

    void sendService(Request req) {
        request(toService(req))
    }

    void sendService(String id) {
        request(toService(id))
    }

    static <T extends Jsonable> Result<T> fromJson(String json, Class<T> classOfT) {
        Result.fromJson(json, classOfT)
    }

    public <T extends Jsonable> Result<T> getResult(Class<T> classOfT) {
        def result = readBody()
        Result.fromJson(result, classOfT)
    }

    String login() {
        sendService('loadCurrentUser')

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

        sendService(new SysLogin(userId, password))
        readBody()
    }

    String logout() {
        sendService('syslogout')
        readBody()
    }

    static <T> T use(String user = '002', boolean autoLogin = true, Function<Session, T> func) {
        def config = Config.load('cjb.session')
        def usr = config.getConfig("users.$user")
        new Session(
                config.getString('host'),
                config.getInt('port'),
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

class Request implements Jsonable {
    transient String id

    Request(String id) {
        this.id = id
    }
}

class PageRequest extends Request {
    PageRequest(String id) {
        super(id)
    }

    PageRequest(String id, int page, int pageSize) {
        super(id)
        this.page = page
        this.pageSize = pageSize
    }

    PageRequest(String id, int page, int pageSize, HashMap<String, String> sorting) {
        super(id)
        this.page = page
        this.pageSize = pageSize
        this.sorting = [sorting]
    }

    int page = 1

    @SerializedName('pagesize')
    int pageSize = 15

    List<HashMap<String, String>> filtering = []

    List<HashMap<String, String>> sorting = []

    List<HashMap<String, String>> totals = []
}

class JsonService<T extends Request> implements Jsonable {
    JsonService(T params, String userId, String password) {
        serviceId = params.id
        loginName = userId
        this.password = password
        this.params = params
        data = [params]
    }

    @SerializedName('serviceid')
    String serviceId

    String target = ''

    @SerializedName('sessionid')
    String sessionId

    @SerializedName('loginname')
    String loginName

    String password

    T params

    @SerializedName('datas')
    List<T> data
}

@ToString
class Result<T extends Jsonable> implements Iterable<T>, Jsonable {
    @SerializedName('rowcount')
    int rowCount = 0

    int page = 0

    @SerializedName('pagesize')
    int pageSize = 0

    @SerializedName('serviceid')
    String serviceId

    String type

    String vcode

    String message

    @SerializedName('messagedetail')
    String messageDetail

    @SerializedName('datas')
    List<T> data

    void add(T d) {
        data?.add(d)
    }

    T getAt(int index) {
        data?[index]
    }

    int size() {
        data?.size() ?: 0
    }

    boolean isEmpty() {
        data?.isEmpty() ?: false
    }

    @Override
    Iterator<T> iterator() {
        data?.iterator() ?: [].iterator()
    }

    static <T extends Jsonable> Result<T> fromJson(String json, Class<T> classOfT) {
        def typeOf = TypeToken
                .getParameterized(Result, classOfT)
                .getType()
        Json.fromJson(json, typeOf)
    }
}