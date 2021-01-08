package yhsb.base.util

import groovy.transform.ToString

def xml = '''<?xml version="1.0" encoding="GBK"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" soap:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
  <soap:Header>
    <in:system xmlns:in="http://www.molss.gov.cn/">
      <para usr="abc"/>
      <para pwd="YLZ_A2ASSDFDFDSS"/>
      <para funid="F00.01.03"/>
    </in:system>
  </soap:Header>
  <soap:Body>
    <in:business xmlns:in="http://www.molss.gov.cn/">
      <para startrow="1"/>
      <para row_count="-1"/>
      <para pagesize="500"/>
      <para clientsql="( aac002 = &apos;430302195806251012&apos;)"/>
      <para functionid="F27.06"/>
    </in:business>
  </soap:Body>
 </soap:Envelope>'''

@ToString
class Envelope {
    @Attribute(value = 'encodingStyle', namespace = 'http://schemas.xmlsoap.org/soap/envelope/')
    String encodingStyle

    @Tag(value = 'Header', namespace = 'http://schemas.xmlsoap.org/soap/envelope/')
    Header header

    @Tag(value = 'Body', namespace = 'http://schemas.xmlsoap.org/soap/envelope/')
    Body body
}

@ToString
class Header {
    @Tag(value = 'system', namespace = 'http://www.molss.gov.cn/')
    System system
}

@ToString
class Body {
    @Tag(value = 'business', namespace = 'http://www.molss.gov.cn/')
    Business business
}

@ToString
class System {
    @Tags('para')
    SysParam para
}

@ToString
class SysParam {
    @Attribute('usr')
    String user

    @Attribute('pwd')
    String password

    @Attribute('funid')
    String funId
}

@ToString
class Business {
    @Tags('para')
    BssParam para
}

@ToString
class BssParam {
    @Attribute('startrow')
    String startRow

    @Attribute('row_count')
    String rowCount

    @Attribute('pagesize')
    String pageSize

    @Attribute('clientsql')
    String clientSql

    @Attribute('functionid')
    String functionId
}

def root = Xml.rootElement(xml)
println root

def env = root.toObject(Envelope)
println env

xml = '''<?xml version="1.0" encoding="GBK"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" soap:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
  <soap:Header>
    <result sessionID="DpPZb8mZ0qgv08kN26LyKmm1yDz4nn7QvXxh2VD32vDvgvQ2zw14!-23337339!1530701497001"/>
    <result message=""/>
  </soap:Header>
  <soap:Body>
    <out:business xmlns:out="http://www.molss.gov.cn/">
      <result result="" />
      <resultset name="querylist">
        <row aac003="徐X" rown="1" aac008="2" aab300="XXXXXXX服务局" sac007="101" aac031="3" aac002="43030219XXXXXXXXXX" />
      </resultset>
      <result row_count="1" />
      <result querysql="select * from
        from ac01_css a, ac02_css b
        where a.aac001 = b.aac001) where ( aac002 = &apos;43030219XXXXXXXX&apos;) and 1=1) row_ where rownum &lt;(501)) where rown &gt;=(1) " />
    </out:business>
  </soap:Body>
</soap:Envelope>'''