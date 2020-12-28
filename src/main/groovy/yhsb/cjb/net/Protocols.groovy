package yhsb.cjb.net

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.annotations.SerializedName
import groovy.transform.ToString
import groovyjarjarantlr4.v4.runtime.misc.Tuple2
import yhsb.base.util.JsonField
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

class CbxxQuery extends Request {
    CbxxQuery(String idCard) {
        super('executeSncbxxConQ')
        this.idCard = idCard
    }

    @SerializedName('aac002')
    String idCard
}

class CbState extends JsonField {
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

class JfState extends JsonField {
    @Override
    HashMap<String, String> getValueMap() {
        [
                '1': '参保缴费',
                '2': '暂停缴费',
                '3': '终止缴费',
        ]
    }
}

class JbKind extends JsonField {
    @Override
    Map<String, String> getValueMap() {
        jbKindMap
    }

    static final BiMap<String, String> jbKindMap = HashBiMap.create([
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
        null
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
class Cbxx implements Jsonable, JbState, XzqhName {
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

class DfState extends JsonField {
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

class DfType extends JsonField {
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