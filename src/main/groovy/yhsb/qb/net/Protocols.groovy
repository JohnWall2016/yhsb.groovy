package yhsb.qb.net

import groovy.transform.PackageScope
import groovy.transform.ToString
import groovy.xml.MarkupBuilder
import yhsb.base.util.Attribute
import yhsb.base.util.AttrNode
import yhsb.base.util.MapField
import yhsb.base.util.NS
import yhsb.base.util.Namespaces
import yhsb.base.util.Node
import yhsb.base.util.ToXml

@ToString
@Namespaces([@NS(name = 'soap', value = 'http://schemas.xmlsoap.org/soap/envelope/')])
@Node('soap:Envelope')
class InEnvelope<T extends Parameters> implements ToXml {
    @Attribute('soap:encodingStyle')
    String encodingStyle = 'http://schemas.xmlsoap.org/soap/encoding/'

    @Node('soap:Header')
    InHeader header

    @Node('soap:Body')
    InBody<T> body

    InEnvelope(T params) {
        header = new InHeader(params.funId)
        body = new InBody<>(business: params)
    }

    void setUser(String user) {
        header.system.user = user
    }

    void setPassword(String password) {
        header.system.password = password
    }
}

@ToString
class InHeader implements ToXml {
    @Node('in:system')
    @Namespaces([@NS(name = 'in', value = 'http://www.molss.gov.cn/')])
    System system

    InHeader(String funId) {
        system = new System(
                funId: funId
        )
    }
}

@ToString
class System implements ToXml {
    @AttrNode(name = 'para', attr = 'usr')
    String user

    @AttrNode(name = 'para', attr = 'pwd')
    String password

    @AttrNode(name = 'para', attr = 'funid')
    String funId
}

@ToString
class InBody<T extends ToXml> implements ToXml {
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
    OutResult result
}

class OutResult {
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
    @AttrNode(name = 'result', attr = 'result')
    String result

    @AttrNode(name = 'result', attr = 'row_count')
    int rowCount

    @AttrNode(name = 'result', attr = 'querysql')
    String querySql

    @Node(value = 'resultset', filter = { it['@name'] ==~ /^querylist|cxjg$/ })
    ResultSet<T> resultSet

    @Node(value = 'resultset', filter = { !(it['@name'] ==~ /^querylist|cxjg$/) })
    List<ResultSet<T>> otherResultSets
};

@ToString
class Result {
    @Attribute('result')
    String result

    @Attribute('row_count')
    int rowCount

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
        (rowList ?: []).iterator()
    }
}

@ToString
class Login extends Parameters {
    Login() {
        super('F00.00.00.00|192.168.1.110|PC-20170427DGON|00-05-0F-08-1A-34')
    }
}

class FunctionId extends Parameters {
    FunctionId(String funId, String functionId) {
        super(funId)

        this.functionId = functionId
    }

    @AttrNode(name = 'para', attr = 'functionid')
    String functionId = ''
}

class Query extends Parameters {
    Query(String funId, String functionId) {
        super(funId)

        this.functionId = functionId
    }

    @AttrNode(name = 'para', attr = 'startrow')
    String startRow = '1'

    @AttrNode(name = 'para', attr = 'row_count')
    String rowCount = '-1'

    @AttrNode(name = 'para', attr = 'pagesize')
    String pageSize = '500'

    @AttrNode(name = 'para', attr = 'functionid')
    String functionId = ''
}

class SimpleClientSql extends Parameters {
    SimpleClientSql(String funId, String functionId, String sql = '') {
        super(funId)

        this.functionId = functionId
        this.clientSql = sql
    }

    @AttrNode(name = 'para', attr = 'clientsql')
    String clientSql = ''

    @AttrNode(name = 'para', attr = 'functionid')
    String functionId = ''
}

class ClientSql extends Parameters {
    ClientSql(String funId, String functionId, String sql = '') {
        super(funId)

        this.functionId = functionId
        this.clientSql = sql
    }

    @AttrNode(name = 'para', attr = 'startrow')
    String startRow = '1'

    @AttrNode(name = 'para', attr = 'row_count')
    String rowCount = '-1'

    @AttrNode(name = 'para', attr = 'pagesize')
    String pageSize = '500'

    @AttrNode(name = 'para', attr = 'clientsql')
    String clientSql = ''

    @AttrNode(name = 'para', attr = 'functionid')
    String functionId = ''
}

class AddSql extends Parameters {
    AddSql(String funId, String fid, String addSql, int begin = 0, int pageSize = 0) {
        super(funId)
        this.fid = fid
        this.addSql = addSql
        this.pageSize = pageSize
        this.begin = begin
    }

    @AttrNode(name = 'para', attr = 'pagesize')
    int pageSize

    @AttrNode(name = 'para', attr = 'addsql')
    String addSql

    @AttrNode(name = 'para', attr = 'begin')
    int begin

    @AttrNode(name = 'para', attr = 'fid')
    String fid
}

@PackageScope
class ParaList implements ToXml {
    LinkedHashMap<String, String> attrs
    LinkedHashMap<String, String> paraList

    ParaList(
            LinkedHashMap<String, String> attrs,
            LinkedHashMap<String, String> paraList
    ) {
        this.attrs = attrs
        this.paraList = paraList
    }

    void toXml(
            MarkupBuilder markup,
            String nodeName,
            Map<String, String> namespaces
    ) {
        markup."$nodeName"(this.attrs) {
            paraList.each {
                markup."row"(Map.of(it.key, it.value))
            }
        }
    }
}

class ParamList extends Parameters {
    ParamList(
            String funId,
            LinkedHashMap<String, String> attrs,
            LinkedHashMap<String, String> paraList
    ) {
        super(funId)

        list = new ParaList(attrs, paraList)
    }

    @Node('paralist')
    ParaList list
}

/** 省内参保人员查询 */
class InProvincePersonQuery extends ClientSql {
    InProvincePersonQuery(String idCard) {
        super('F00.01.03', 'F27.06', "( aac002 = &apos;$idCard&apos;)")
    }
}

/** 社保状态 */
class SbState extends MapField {
    @Override
    Map<String, String> getValueMap() {[
            '1': '在职',
            '2': '退休',
            '3': '终止'
    ]}
}

/** 参保状态 */
class CbState extends MapField {
    @Override
    Map<String, String> getValueMap() {[
            '1': '参保缴费',
            '2': '暂停缴费',
            '3': '终止缴费'
    ]}
}

/** 缴费人员类别 */
class JfKind extends MapField {
    @Override
    Map<String, String> getValueMap() {[
            '102': '个体缴费',
            '101': '单位在业人员',
    ]}
}

@ToString
class InProvincePerson {
    @Attribute('sac100')
    String pid // 个人编号

    @Attribute('aac002')
    String idCard

    @Attribute('aac003')
    String name

    @Attribute('aac008')
    SbState sbState

    @Attribute('aac031')
    CbState cbState

    @Attribute('sac007')
    JfKind jfKind

    @Attribute('aab300')
    String agencyName // 社保机构名称

    @Attribute('sab100')
    String companyCode // 单位编号
}

/** 社保机构编号查询 */
class AgencyCodeQuery extends FunctionId {
    AgencyCodeQuery() {
        super('F00.01.02', 'F28.02')
    }
}

@ToString
class AgencyCode {
    @Attribute('aab300')
    String name

    @Attribute('aab034')
    String code
}

/** 参保人员查询统计 */
class JoinedPersonQuery extends ClientSql {
    JoinedPersonQuery(String idCard, String agencyCode) {
        super('F00.01.03', 'F27.02', "( AC01.AAC002 = &apos;$idCard&apos;)")
        this.agencyCode = agencyCode
    }

    @AttrNode(name = 'para', attr = 'aab034')
    String agencyCode
}

@ToString
class JoinedPerson {
    @Attribute('aab004')
    String companyName

    @Attribute('sab100')
    String companyCode

    @Attribute('aac001')
    String id

    @Attribute('aac002')
    String idCard

    @Attribute('aac003')
    String name

    @Attribute('sac100')
    String pid

    @Attribute('aab034')
    String agencyCode
}

class JoinedPersonGeneralQuery extends AddSql {
    JoinedPersonGeneralQuery(String id, String agencyCode) {
        super('F27.00.01', 'F27.02.01', "ac01.aac001 = &apos;${id}&apos;")
        this.agencyCode = agencyCode
    }

    @AttrNode(name = 'para', attr = 'aab034')
    String agencyCode = ''
}

/** 参保人员查询统计 - 缴费记录 */
class JoinedPersonPayDetailQuery extends AddSql {
    JoinedPersonPayDetailQuery(String id, String agencyCode) {
        super(
                'F27.00.01',
                'F27.02.04',
                "aac001 = &apos;${id}&apos; and aae140 = &apos;1&apos;",
                1,
                500
        )
        this.agencyCode = agencyCode
    }

    @AttrNode(name = 'para', attr = 'aab034')
    String agencyCode = ''
}

@ToString
class JoinedPersonPayDetail {
    @Attribute('aac003')
    String name

    /** 费款所属期 */
    @Attribute('aae002')
    String period

    /** 对应费款所属期 */
    @Attribute('aae003')
    String corPeriod

    /** 缴费月数 */
    @Attribute('sac047')
    int payMonths // 0 or 1

    @Attribute('sab100')
    String companyCode

    @Attribute('aab004')
    String companyName

    /** 缴费基数 */
    @Attribute('jfjs00')
    String payoffSalary
}

/** 参保人员查询统计 - 基金转入记录 */
class JoinedPersonTransferQuery extends AddSql {
    JoinedPersonTransferQuery(String id, String agencyCode) {
        super(
                'F27.00.01',
                'F27.02.07',
                "a.aac001 = &apos;${id}&apos;",
                1,
                500
        )
        this.agencyCode = agencyCode
    }

    @AttrNode(name = 'para', attr = 'aab034')
    String agencyCode = ''
}

class BookMark extends MapField {
    @Override
    Map<String, String> getValueMap() {[
            '1': '已记账',
            '0': '未记账',
    ]}
}

@ToString
class JoinedPersonTransfer {
    @Attribute('aac002')
    String idCard

    @Attribute('aac003')
    String name

    @Attribute('aac004')
    String sex

    @Attribute('aac005')
    String nation

    @Attribute('aac008')
    SbState sbState

    @Attribute('aac072')
    String agencyNameBeforeTransfer

    /** 记账标志 */
    @Attribute('aae112')
    BookMark bookMark
}


/** 参保人员查询统计 - 账户信息 */
class JoinedPersonAccountQuery extends AddSql {
    JoinedPersonAccountQuery(String id, String agencyCode) {
        super(
                'F27.00.01',
                'F27.02.06',
                "aac001 = &apos;${id}&apos;",
                0,
                0
        )
        this.agencyCode = agencyCode
    }

    @AttrNode(name = 'para', attr = 'aab034')
    String agencyCode = ''
}

@ToString
class JoinedPersonAccount {
    /** 开始日期 */
    @Attribute('ksny')
    Integer beginMonth

    /** 截止日期 */
    @Attribute('jzny')
    Integer endMonth

    /** 当年缴费月数 */
    @Attribute('dnjfys')
    int payMonths

    /** 累计缴费月数 */
    @Attribute('ljjfys')
    int totalPayMonths
}

/** 离退休人员参保查询统计 */
class RetiredPersonQuery extends ClientSql {
    RetiredPersonQuery(String idCard, String agencyCode) {
        super('F00.01.03', 'F27.03', "( v.aac002 = &apos;${idCard}&apos;)")
        this.agencyCode = agencyCode
    }

    @AttrNode(name = 'para', attr = 'aab034')
    String agencyCode
}

@ToString
class RetiredPerson {
    @Attribute('aab004')
    String companyName

    @Attribute('aae116')
    String payState // 待遇发放姿态

    @Attribute('aic162')
    String retireDate // 离退休日期

    @Attribute('aic160')
    String beginTime // 待遇开始时间

    @Attribute('txj')
    String pension // 退休金
}

/** 养老个人账户查询单 */
class AccountQuery extends ClientSql {
    AccountQuery(String idCard) {
        super('F00.01.03', 'F03.01.19', "( AC01.AAC002 = &apos;${idCard}&apos;)")
    }
}

@ToString
class Account {
    @Attribute('aac001')
    String id // 账户ID

    @Attribute('aac003')
    String name

    @Attribute('aac002')
    String idCard

    @Attribute('aac006')
    String birthDay

    @Attribute('aab004')
    String companyName

    @Attribute('sab100')
    String companyCode
}

/** 养老个人账户总账查询 */
class AccountTotalQuery extends ParamList {
    AccountTotalQuery(String id) {
        super('F03.01.19.01', ['name': 'ac01'], ['aac001': id])
    }
}

/** 养老个人账户明细查询 */
class AccountDetailQuery extends ClientSql {
    AccountDetailQuery(String id) {
        super('F00.01.02', 'F03.01.19.01',  "a.aac001 = &apos;${id}&apos;")
    }
}

@ToString
class AccountDetail {
    @Attribute('aae001')
    String year

    @Attribute('saa014')
    String averageSalary // 社平公资

    @Attribute('aic020')
    String payoffSalary // 缴费基数

    @Attribute('aic110')
    String index // 缴费指数

    @Attribute('aic090')
    String months // 实缴月数
}

/** 查询税务推送 单位缴费信息 */
class TaxPushCompanyPayInfoQuery extends Query {
    TaxPushCompanyPayInfoQuery() {
        super('F00.01.03', 'F02.08.01')
    }
}

@ToString
class TaxPushCompanyPayInfo {
    @Attribute('aab004')
    String companyName // 单位名称

    @Attribute('sab100')
    String companyCode // 单位编号

    @Attribute('aaz288')
    String serialNumber // 征集通知流水号

    @Attribute('aae041')
    String periodStartTime // 费款期起

    @Attribute('aae042')
    String periodEndTime // 费款期止

    @Attribute('aae063')
    BigDecimal companyTotalActualPayment // 单位实缴汇总

    @Attribute('aae064')
    BigDecimal personalTotalActualPayment // 个人实缴汇总

    @Attribute('aae057')
    BigDecimal overdueFine // 实缴滞纳金

    @Attribute('aae057')
    BigDecimal interest // 实缴利息
}

/** 查询税务推送 特殊缴费信息 */
class TaxPushSpecialPayInfoQuery extends Query {
    TaxPushSpecialPayInfoQuery() {
        super('F00.01.03', 'F02.08.03')
    }
}

@ToString
class TaxPushSpecialPayInfo {
    @Attribute('aaz288')
    String serialNumber // 征集通知流水号

    @Attribute('aab004')
    String companyName // 单位名称

    @Attribute('aab001')
    String companyCode // 单位编号

    @Attribute('yije')
    BigDecimal shouldPaySum // 社保应缴金额合计

    @Attribute('sjje')
    BigDecimal actualPaySum // 税务实缴金额合计

    @Attribute('aae015')
    String memo
}

/** 查询税务推送 灵活就业缴费信息 */
class TaxPushPersonalPayInfoQuery extends Query {
    TaxPushPersonalPayInfoQuery() {
        super('F00.01.03', 'F02.08.02')
    }
}

@ToString
class TaxPushPersonalPayInfo {
    @Attribute('aae041')
    String periodStartTime // 缴费开始

    @Attribute('aae042')
    String periodEndTime // 缴费截止

    @Attribute('jfys')
    int months // 缴费月数

    @Attribute('aae082')
    BigDecimal actualPaySum // 个人实缴金额

    @Attribute('sac100')
    String pid // 个人编号

    @Attribute('aac002')
    String idCard // 社会保障号码

    @Attribute('aac003')
    String name

    @Attribute('aad009')
    String TaxStampNumber // 税务电子税票号码

    @Attribute('aab004')
    String companyName // 单位名称
}

@ToString
class NoUKeyWorkerAddQuery extends SimpleClientSql {
    NoUKeyWorkerAddQuery() {
        super(
                'F00.01.02',
                'F01.02.01.99',
                '(ADS3.SAE114=&apos;0&apos; AND ADS3.AAE013 IS NULL AND ADS3.SAE118=&apos;0&apos; and ads3.functionid=&apos;B06.03.01&apos; and ADS3.AAE013 IS NULL )'
        )
    }
}

interface CompanyInfo {
    String getCompanyName()
    String getCompanyCode()
}

@ToString
class NoUKeyWorkerAdd implements CompanyInfo {
    @Attribute('aab004')
    String companyName // 单位名称

    @Attribute('sab100')
    String companyCode // 单位编号
}

@ToString
class NoUKeyWorkerStopQuery extends SimpleClientSql {
    NoUKeyWorkerStopQuery() {
        super(
                'F00.01.02',
                'F01.02.05.99',
                '(ADS3.SAE114=&apos;0&apos; AND ADS3.AAE013 IS NULL AND ADS3.SAE118=&apos;0&apos; and ads3.functionid=&apos;B06.03.02&apos; and ADS3.AAE013 IS NULL )'
        )
    }
}

@ToString
class NoUKeyWorkerStop implements CompanyInfo {
    @Attribute('aab004')
    String companyName // 单位名称

    @Attribute('sab100')
    String companyCode // 单位编号
}

@ToString
class NoUKeyWorkerContinueQuery extends SimpleClientSql {
    NoUKeyWorkerContinueQuery() {
        super(
                'F00.01.02',
                'F01.02.06.99',
                '(ADS3.SAE114=&apos;0&apos; AND ADS3.AAE013 IS NULL AND ADS3.SAE118=&apos;0&apos; and ads3.functionid=&apos;B06.03.03&apos; and ADS3.AAE013 IS NULL )'
        )
    }
}

@ToString
class NoUKeyWorkerContinue implements CompanyInfo {
    @Attribute('aab004')
    String companyName // 单位名称

    @Attribute('sab100')
    String companyCode // 单位编号
}

@ToString
class NoUKeyWorkerJoinInProvinceQuery extends SimpleClientSql {
    NoUKeyWorkerJoinInProvinceQuery() {
        super(
                'F00.01.02',
                'B06.02.05.99',
                '(ADS3.SAE114=&apos;0&apos; AND ADS3.AAE013 IS NULL AND ADS3.SAE118=&apos;0&apos; and ads3.functionid=&apos;B06.03.05&apos; and ADS3.AAE013 IS NULL )'
        )
    }
}

@ToString
class NoUKeyWorkerJoinInProvince implements CompanyInfo {
    @Attribute('aaf010')
    String companyName // 接续单位名称

    @Attribute('aaf009')
    String companyCode // 接续单位编号
}

@ToString
class NoUKeyWorkerJoinInChangShaQuery extends SimpleClientSql {
    NoUKeyWorkerJoinInChangShaQuery() {
        super(
                'F00.01.02',
                'B06.05.01.99',
                '(ADS3.SAE114=&apos;0&apos; AND ADS3.AAE013 IS NULL AND ADS3.SAE118=&apos;0&apos; and ads3.functionid=&apos;B06.05.02&apos; and ADS3.AAE013 IS NULL )'
        )
    }
}

@ToString
class NoUKeyWorkerJoinInChangSha implements CompanyInfo {
    @Attribute('aaf010')
    String companyName // 接续单位名称

    @Attribute('aaf009')
    String companyCode // 接续单位编号
}