package yhsb.cjb.net

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.annotations.SerializedName
import groovy.transform.ToString
import yhsb.base.net.HttpSocket
import yhsb.base.util.Config
import yhsb.base.util.MapField
import yhsb.base.util.Jsonable

import java.util.regex.Matcher

class SysLogin extends Request {
    SysLogin(String userName, String password) {
        super('syslogin')
        this.userName = userName
        this.password = password
    }

    @SerializedName('username')
    String userName

    @SerializedName('passwd')
    String password
}

class SncbxxQuery extends Request {
    SncbxxQuery(String idCard) {
        super('executeSncbxxConQ')
        this.idCard = idCard
    }

    @SerializedName('aac002')
    String idCard
}

class CbState extends MapField {
    @Override
    HashMap<String, String> getValueMap() {
        [
                '0': '未参保',
                '1': '正常参保',
                '2': '暂停参保',
                '4': '终止参保',
        ]
    }

    CbState() {}

    CbState(String value) {
        this.value = value
    }
}

class JfState extends MapField {
    @Override
    HashMap<String, String> getValueMap() {
        [
                '1': '参保缴费',
                '2': '暂停缴费',
                '3': '终止缴费',
        ]
    }
}

class JbKind extends MapField {
    @Override
    Map<String, String> getValueMap() {
        map
    }

    static final BiMap<String, String> map = HashBiMap.create([
            '011': '普通参保人员',
            '021': '残一级',
            '022': '残二级',
            '023': '残三级',
            '031': '特困一级',
            '032': '特困二级',
            '033': '特困三级',
            '051': '贫困人口一级',
            '052': '贫困人口二级',
            '053': '贫困人口三级',
            '061': '低保对象一级',
            '062': '低保对象二级',
            '063': '低保对象三级',
            '071': '计生特扶人员',
            '090': '其他'
    ])
}

trait JbState {
    abstract CbState getCbState()

    abstract JfState getJfState()

    String getJbState() {
        def cbState = getCbState()
        def jfState = getJfState()
        if (!jfState || !jfState.value) "未参保"
        else {
            def (String jf, String cb) = [jfState.value, cbState.value]
            switch (jf) {
                case '1':
                    switch (cb) {
                        case '1':
                            return '正常缴费人员'
                        default:
                            return "未知类型参保缴费人员: $cb"
                    }
                case '2':
                    switch (cb) {
                        case '2':
                            return '暂停缴费人员'
                        default:
                            return "未知类型暂停缴费人员: $cb"
                    }
                case '3':
                    switch (cb) {
                        case '1':
                            return '正常待遇人员'
                        case '2':
                            return '暂停待遇人员'
                        case '4':
                            return '终止参保人员'
                        default:
                            return "未知类型终止缴费人员: $cb"
                    }
                default:
                    return "未知类型人员: $jf, $cb"
            }
        }
    }
}

class Xzqh {
    static final BiMap codeMap = HashBiMap.create([
            '43030200': '代发虚拟乡镇',
            '43030201': '长城乡',
            '43030202': '昭潭街道',
            '43030203': '先锋街道',
            '43030204': '万楼街道',
            '43030205': '（原）鹤岭镇',
            '43030206': '楠竹山镇',
            '43030207': '姜畲镇',
            '43030208': '鹤岭镇',
            '43030209': '城正街街道',
            '43030210': '雨湖路街道',
            '43030211': '（原）平政路街道',
            '43030212': '云塘街道',
            '43030213': '窑湾街道',
            '43030214': '（原）窑湾街道',
            '43030215': '广场街道',
            '43030216': '（原）羊牯塘街',
    ])

    static final List<String> regExps = [
            /湘潭市雨湖区((.*?乡)(.*?社区)).*/,
            /湘潭市雨湖区((.*?乡)(.*?村)).*/,
            /湘潭市雨湖区((.*?乡)(.*?政府机关)).*/,
            /湘潭市雨湖区((.*?街道)办事处(.*?社区)).*/,
            /湘潭市雨湖区((.*?街道)办事处(.*?政府机关)).*/,
            /湘潭市雨湖区((.*?镇)(.*?社区)).*/,
            /湘潭市雨湖区((.*?镇)(.*?居委会)).*/,
            /湘潭市雨湖区((.*?镇)(.*?村)).*/,
            /湘潭市雨湖区((.*?街道)办事处(.*?村)).*/,
            /湘潭市雨湖区((.*?镇)(.*?政府机关)).*/,
            /湘潭市雨湖区((.*?街道)办事处(.*))/,
    ]

    static String getDwName(String fullName) {
        for (r in regExps) {
            def m = fullName =~ r
            if (m.find()) {
                return m.group(2)
            }
        }
        null
    }

    static String getCsName(String fullName) {
        for (r in regExps) {
            def m = fullName =~ r
            if (m.find()) {
                return m.group(3)
            }
        }
        null
    }

    static Tuple2<String, String> getDwAndCsName(String fullName) {
        for (r in regExps) {
            def m = fullName =~ r
            if (m.find()) {
                return new Tuple2<>(m.group(2), m.group(3))
            }
        }
        new Tuple2<>(null, null)
    }
}

trait XzqhName {
    abstract String getCzName()

    String getDwName() {
        Xzqh.getDwName(czName)
    }

    String getCsName() {
        Xzqh.getCsName(czName)
    }

    Tuple2<String, String> getDwAndCsName() {
        Xzqh.getDwAndCsName(czName)
    }
}

@ToString
class Sncbxx implements Jsonable, JbState, XzqhName {
    @SerializedName('aac001')
    int pid

    @SerializedName('aac002')
    String idCard

    @SerializedName('aac003')
    String name

    @SerializedName('aac006')
    String birthDay

    @SerializedName('aac008')
    CbState cbState

    @SerializedName('aac031')
    JfState jfState

    @SerializedName('aac049')
    int cbTime

    @SerializedName('aac066')
    JbKind jbKind

    @SerializedName('aaa129')
    String agency

    @SerializedName('aae036')
    String opTime

    @SerializedName('aaf102')
    String czName

    boolean valid() {
        if (idCard) return true
        false
    }
}

class DfState extends MapField {
    @Override
    HashMap<String, String> getValueMap() {
        [
                '1': '正常发放',
                '2': '暂停发放',
                '3': '终止发放'
        ]
    }

    DfState() {}

    DfState(String value) {
        this.value = value
    }
}

class DfType extends MapField {
    @Override
    HashMap<String, String> getValueMap() {
        [
                '801': '独生子女',
                '802': '乡村教师',
                '803': '乡村医生',
                '807': '电影放映员'
        ]
    }

    DfType() {}

    DfType(String value) {
        this.value = value
    }
}

/**
 * 代发人员名单查询
 */
class DfryQuery extends PageRequest {
    String aaf013 = ''
    String aaf030 = ''

    @SerializedName('aae100')
    CbState cbState // 居保参保状态

    String aac002 = ''
    String aac003 = ''

    @SerializedName('aae116')
    DfState state

    String aac082 = ''

    @SerializedName('aac066')
    DfType type

    DfryQuery(String type, String cbState, String dfState, int page = 1, int pageSize = 1000) {
        super('executeDfrymdQuery', page, pageSize, ['dataKey': 'aaf103', 'sortDirection': 'ascending'])

        this.type = new DfType(type)
        this.cbState = new CbState(cbState)
        this.state = new DfState(dfState)
    }
}

@ToString
class Dfry implements Jsonable {
    @SerializedName('aac001')
    int pid // 个人编号

    @SerializedName('aac002')
    String idCard // 身份证号码

    @SerializedName('aac003')
    String name

    @SerializedName('aaf103')
    String csName // 村社区名称

    @SerializedName('aic160')
    int startYearMonth // 代发开始年月

    @SerializedName('aae019')
    BigDecimal standard // 代发标准

    @SerializedName('aac066s')
    String type // 代发类型

    @SerializedName('aae116')
    DfState dfState // 代发状态

    @SerializedName('aac008s')
    CbState cbState // 居保状态

    @SerializedName('aae002jz')
    int endYearMonth // 代发截至成功发放年月

    @SerializedName('aae019jz')
    BigDecimal totalPayed // 代发截至成功发放金额
}

class DfzfType extends MapField {
    @Override
    HashMap<String, String> getValueMap() {
        [
                'DF0001': '独生子女',
                'DF0002': '乡村教师',
                'DF0003': '乡村医生',
                'DF0007': '电影放映员'
        ]
    }

    DfzfType() {}

    DfzfType(String value) {
        this.value = value
    }
}

/**
 * 代发支付单查询
 */
class DfzfdQuery extends PageRequest {
    @SerializedName('aaa121')
    String type // 代发类型

    @SerializedName('aaz031')
    String payList = '' // 支付单号

    @SerializedName('aae002')
    String yearMonth // 发放年月

    @SerializedName('aae089')
    String state

    DfzfdQuery(String type, String yearMonth, String state = '0') {
        super('dfpayffzfdjQuery')
        this.type = type
        this.yearMonth = yearMonth
        this.state = state
    }
}

@ToString
class Dfzfd implements Jsonable {
    @SerializedName('aaa121')
    String typeCh // 业务类型中文名

    @SerializedName('aaz031')
    int payList // 付款单号
}

/**
 * 代发支付单明细查询
 */
class DfzfdmxQuery extends PageRequest {
    @SerializedName('aaz031')
    String payList // 付款单号

    DfzfdmxQuery(int payList, int page = 1, int pageSize = 500) {
        super('dfpayffzfdjmxQuery', page, pageSize)
        this.payList = "$payList"
    }
}

@ToString
class Dfzfdmx implements Jsonable {
    @SerializedName('aac001')
    int pid // 个人编号

    @SerializedName('aac002')
    String idCard // 身份证号码

    @SerializedName('aac003')
    String name

    @SerializedName('aaf103')
    String csName // 村社区名称

    @SerializedName('aae117')
    String flag // 支付标志

    @SerializedName('aae002')
    int yearMonth // 发放年月

    @SerializedName('aaz031')
    int payList // 付款单号

    @SerializedName('aaz220')
    long personalPayList // 个人单号

    @SerializedName('aae019')
    BigDecimal amount // 支付总金额
}

/**
 * 代发支付单个人明细查询
 */
class DfzfdgrmxQuery extends PageRequest {
    @SerializedName('aac001')
    String pid // 个人编号

    @SerializedName('aaz031')
    String payList // 付款单号

    @SerializedName('aaz220')
    String personalPayList // 个人单号

    DfzfdgrmxQuery(int pid, int payList, long personalPayList, int page = 1, int pageSize = 500) {
        super('dfpayffzfdjgrmxQuery', page, pageSize)
        this.pid = "$pid"
        this.payList = "$payList"
        this.personalPayList = "$personalPayList"
    }
}

@ToString
class Dfzfdgrmx implements Jsonable {
    @SerializedName('aae003')
    int date // 待遇日期

    @SerializedName('aae117')
    String flag // 支付标志

    @SerializedName('aae002')
    String yearMonth // 发放年月

    @SerializedName('aaz031')
    int payList // 付款单号

    @SerializedName('aae019')
    BigDecimal amount // 支付总金额
}

class CbshQuery extends PageRequest {
    String aaf013 = ''
    String aaf030 = ''
    String aae011 = ''
    String aae036 = ''
    String aae036s = ''
    String aae014 = ''
    String aac009 = ''
    String aac002 = ''
    String aac003 = ''
    String sfccb = ''

    @SerializedName('aae015')
    String startDate

    @SerializedName('aae015s')
    String endDate

    @SerializedName('aae016')
    String shState

    CbshQuery(String startDate, String endDate, String shState = '1') {
        super('cbshQuery', 500)
        this.startDate = startDate
        this.endDate = endDate
        this.shState = shState
    }
}

class Cbsh implements Jsonable {
    @SerializedName('aac002')
    String idCard

    @SerializedName('aac003')
    String name

    @SerializedName('aac006')
    String birthDay
}

class BankInfoQuery extends Request {
    BankInfoQuery(String idCard) {
        super('executeSncbgrBankinfoConQ')
        this.idCard = idCard
    }

    @SerializedName('aac002')
    String idCard
}

class BankInfo implements Jsonable {
    @SerializedName('bie013')
    BankType bankType // 银行类型

    @SerializedName('aae009')
    String countName // 户名

    @SerializedName('aae010')
    String cardNumber // 卡号
}

class BankType extends MapField {
    @Override
    HashMap<String, String> getValueMap() {
        [
                'LY': '中国农业银行',
                'ZG': '中国银行',
                'JS': '中国建设银行',
                'NH': '农村信用合作社',
                'YZ': '邮政',
                'JT': '交通银行',
                'GS': '中国工商银行',
        ]
    }
}

class DyfhQuery extends PageRequest {
    String aaf013 = '', aaf030 = ''

    @SerializedName('aae016')
    String approvalState = ''

    String aae011 = '', aae036 = '', aae036s = '', aae014 = ''

    @SerializedName('aae015')
    String approvalBeginTime = ''

    @SerializedName('aae015s')
    String approvalEndTime = ''

    String aac009 = '', aac003 = ''

    @SerializedName('aac002')
    String idCard = ''

    DyfhQuery(
            String idCard = '',
            String approvalState = '0',
            String approvalBeginTime = '',
            String approvalEndTime = '',
            int page = 1,
            int pageSize = 500,
            Map<String, String> sorting = [
                    'dataKey': 'aaa027',
                    'sortDirection': 'ascending'
            ]
    ) {
        super('dyfhQuery', page, pageSize, sorting)

        this.idCard = idCard
        this.approvalState = approvalState
        this.approvalBeginTime = approvalBeginTime
        this.approvalEndTime = approvalEndTime
    }
}

class Dyfh implements Jsonable {
    @SerializedName('aac001')
    int pid // 个人编号

    @SerializedName('aac002')
    String idCard // 身份证号码

    @SerializedName('aac003')
    String name

    @SerializedName('aaa027')
    String xzqh // 行政区划

    @SerializedName('aaa129')
    String agency // 单位名称

    @SerializedName('aic166')
    BigDecimal payAmount // 月养老金

    @SerializedName('aae211')
    int accountMonth // 财务月份

    @SerializedName('aic160')
    int payMonth // 实际待遇开始月份

    String bz = ''

    int aaz170, aaz159, aaz157

    static String escape(Object str) {
        URLEncoder.encode(str.toString(), 'UTF-8')
    }

    String getPaymentInfoUrl() {
        "/hncjb/reports?method=htmlcontent&name=yljjs&" +
        "aaz170=${escape(aaz170)}&aaz159=${escape(aaz159)}&aac001=${escape(pid)}&" +
        "aaz157=${escape(aaz157)}&aaa129=${escape(agency)}&aae211=${escape(accountMonth)}"
    }

    Matcher getPaymentInfoMatcher() {
        def path = "/hncjb/reports?method=htmlcontent&name=yljjs&" +
                "aaz170=${escape(aaz170)}&aaz159=${escape(aaz159)}&aac001=${escape(pid)}&" +
                "aaz157=${escape(aaz157)}&aaa129=${escape(agency)}&aae211=${escape(accountMonth)}"
        new HttpSocket(
                Config.session.getString('host'),
                Config.session.getInt('port')
        ).with {
            def content = it.getHttp(path)?.replaceAll(/[\r\n\t]/, '')
            (content =~ regexPaymentInfo).with {
                if (it.find()) it else null
            }
        }
    }

    static private final String regexPaymentInfo =
    $/<tr id="75">
        <td height="32" align="center" id="3">姓名</td>
        <td align="center" id="4">性别</td>
        <td align="center" colspan="3" id="5">身份证</td>
        <td align="center" colspan="3" id="6">困难级别</td>
        <td align="center" colspan="3" id="7">户籍所在地</td>
        <td align="center" colspan="3" id="8">所在地行政区划编码</td>
      </tr>
      <tr class="detail" component="detail" id="76">
        <td height="39" align="center" id="9">(.+?)</td>
        <td align="center" colspan="" id="10">(.+?)</td>
        <td align="center" colspan="3" id="11">(.+?)</td>
        <td align="center" colspan="3" id="12">(.+?)</td>
        <td align="center" colspan="3" id="13"(?:/>|>(.+?)</td>)
        <td align="center" colspan="3" id="14">(.+?)</td>
      </tr>
      <tr id="77">
        <td height="77" align="center" rowspan="2" id="15">缴费起始年月</td>
        <td align="center" rowspan="2" id="16">累计缴费年限</td>
        <td align="center" rowspan="2" colspan="2" id="17">个人账户累计存储额</td>
        <td height="25" align="center" colspan="9" id="18">其中</td>
      </tr>
      <tr id="78">
        <td height="30" align="center" id="19">个人缴费</td>
        <td align="center" id="20">省级补贴</td>
        <td align="center" id="21">市级补贴</td>
        <td align="center" id="22">县级补贴</td>
        <td align="center" id="23">集体补助</td>
        <td align="center" id="24">被征地补助</td>
        <td align="center" id="24">退捕渔民补助</td>
        <td align="center" id="25">政府代缴</td>
        <td align="center" id="26">利息</td>
      </tr>
      <tr class="detail" component="detail" id="79">
        <td height="40" align="center" id="27">(.+?)</td>
        <td align="center" id="28">(.+?)</td>
        <td align="center" colspan="2" id="29">(.+?)</td>
        <td align="center" id="30">(.+?)</td>
        <td align="center" id="31">(.+?)</td>
        <td align="center" id="32">(.+?)</td>
        <td align="center" id="33">(.+?)</td>
        <td align="center" id="34">(.+?)</td>
        <td align="center" id="35">(.+?)</td>
        <td align="center" id="35">(.+?)</td>
        <td align="center" id="36">(.+?)</td>
        <td align="center" id="37">(.+?)</td>
      </tr>
      <tr id="80">
        <td align="center" rowspan="2" id="38">
          <p>领取养老金起始时间</p>
        </td>
        <td align="center" rowspan="2" id="39">月养老金</td>
        <td height="29" align="center" colspan="5" id="40">其中：基础养老金</td>
        <td align="center" colspan="6" id="41">个人账户养老金</td>
      </tr>
      <tr id="81">
        <td height="31" align="center" id="42">国家补贴</td>
        <td height="31" align="center" id="43">省级补贴</td>
        <td align="center" id="44">市级补贴</td>
        <td align="center" id="45">县级补贴</td>
        <td align="center" id="46">加发补贴</td>
        <td align="center" id="47">个人实缴部分</td>
        <td align="center" id="48">缴费补贴部分</td>
        <td align="center" id="49">集体补助部分</td>
        <td align="center" id="50">被征地补助部分</td>
        <td align="center" id="50">退捕渔民补助部分</td>
        <td align="center" id="51">政府代缴部分</td>
      </tr>
      <tr class="detail" component="detail" id="82">
        <td height="40" align="center" id="52">(.+?)</td>
        <td align="center" id="53">(.+?)</td>
        <td align="center" id="54">(.+?)</td>
        <td align="center" id="55">(.+?)</td>
        <td align="center" id="56">(.+?)</td>
        <td align="center" id="57">(.+?)</td>
        <td align="center" id="58">(.+?)</td>
        <td align="center" id="59">(.+?)</td>
        <td align="center" id="60">(.+?)</td>
        <td align="center" id="61">(.+?)</td>
        <td align="center" id="62">(.+?)</td>
        <td align="center" id="62">(.+?)</td>
        <td align="center" id="63">(.+?)</td>
      </tr>/$.replaceAll(/[\r\n\t]/, '')
}

class GrinfoQuery extends PageRequest {
    GrinfoQuery() {
        super('zhcxgrinfoQuery')
    }

    String aaf013 = '', aaz070 = '', aaf101 = '', aac009 = ''

    @SerializedName('aac008')
    String cbState = '' // 参保状态: "1"-正常参保 "2"-暂停参保 "4"-终止参保 "0"-未参保

    @SerializedName('aac031')
    String jfState = '' //缴费状态: "1"-参保缴费 "2"-暂停缴费 "3"-终止缴费

    String aac006str = '', aac006end = ''
    String aac066 = ''
    String aae030str = '', aae030end = ''
    String aae476 = '', aae480 = '', aac058 = ''

    @SerializedName('aac002')
    String idCard = ''

    String aae478 = ''

    @SerializedName('aac003')
    String name = ''
}

class Grinfo implements Jsonable, JbState {
    @SerializedName('aac001')
    int pid

    @SerializedName('aac002')
    String idCard

    @SerializedName('aac003')
    String name

    @SerializedName('aac006')
    String birthDay

    @SerializedName('aac008')
    CbState cbState

    @SerializedName('aac010')
    String hkArea // 户口所在地

    @SerializedName('aac031')
    JfState jfState

    @SerializedName('aae005')
    String phoneNumber

    @SerializedName('aae006')
    String address

    @SerializedName('aae010')
    String bankCardNumber

    @SerializedName('aaf101')
    String xzName // 乡镇街区划编码

    @SerializedName('aaf102')
    String csName // 村社名称区划编码

    @SerializedName('aaf103')
    String zdName // 组队名称区划编码
}