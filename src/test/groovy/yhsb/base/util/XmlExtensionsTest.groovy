package yhsb.base.util

import groovy.transform.ToString
import groovy.xml.XmlSlurper
import yhsb.base.util.reflect.GenericClass

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
      <paraset name="paralist">
        <row aac003="徐A" rown="1" />
        <row aac003="徐B" rown="2" />
        <row aac003="徐C" rown="3" />
        <row aac003="徐D" rown="4" />
      </paraset>
    </in:business>
  </soap:Body>
 </soap:Envelope>'''

@ToString
@Namespaces([@NS(name = 'soap', value = 'http://schemas.xmlsoap.org/soap/envelope/')])
@Node('soap:Envelope')
class Envelope implements ToXml {
    @Attribute('soap:encodingStyle')
    String encodingStyle

    @Node('soap:Header')
    Header header

    @Node('soap:Body')
    Body body
}

@ToString
class Header implements ToXml {
    @Node('in:system')
    @Namespaces([@NS(name = 'in', value = 'http://www.molss.gov.cn/')])
    System system
}

@ToString
class Body implements ToXml {
    @Node('in:business')
    @Namespaces([@NS(name = 'in', value = 'http://www.molss.gov.cn/')])
    Business business
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
class Business implements ToXml {
    @AttrNode(name = 'para', attr = 'startrow')
    String startRow

    @AttrNode(name = 'para', attr = 'row_count')
    String rowCount

    @AttrNode(name = 'para', attr = 'pagesize')
    String pageSize

    @AttrNode(name = 'para', attr = 'clientsql')
    String clientSql

    @AttrNode(name = 'para', attr = 'functionid')
    String functionId

    @Node('paraset')
    ParaSet paraSet
}

@ToString
class ParaSet implements ToXml {
    @Attribute('name')
    String name

    @Node('row')
    List<Row> rowList
}

@ToString
class Row implements ToXml {
    @Attribute('aac003')
    String name

    @Attribute('rown')
    String number
}

/*
def root = Xml.rootElement(xml)
println root

def env = root.toObject(Envelope)
println env
*/

def root = new XmlSlurper().parseText(xml)
def env = root.toObject(new GenericClass<Envelope>(Envelope))
println env

println env.header.system

println env.header.system.toXml()

println env.toXml()

/*
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

import groovy.xml.XmlSlurper

def env = new XmlSlurper()
        .parseText(xml)
        //.declareNamespace('soap': 'http://schemas.xmlsoap.org/soap/envelope/', 'out': 'http://www.molss.gov.cn/')

println env.getClass()
println env.Header.getClass()
println env.Header.result.getClass()
println env.Header.result.@sessionID.getClass()
*/