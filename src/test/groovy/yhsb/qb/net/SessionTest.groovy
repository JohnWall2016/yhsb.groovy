package yhsb.qb.net

// println((new SncbryQuery('430302195806251012')).toXml())

Session.use('qqb') {
/*
    it.sendService(new SncbryQuery('430302195806251012'))
    //println it.readBody()

    def result = it.getResult(Sncbry)
    println result

    result.resultSet.each {
        println it
    }
*/
/*
    it.sendService(new AgencyCodeQuery())
    //println it.readBody()

    def result = it.getResult(AgencyCode)
    println result

    result.resultSet.each {
        println "${it.code}: ${it.name}"
    }
*/
/*
    it.sendService(new AgencyCodeQuery())
    def acResult = it.getResult(AgencyCode)

    def getAgencyCode = { String name ->
        (acResult.resultSet.find {
            it.name == name
        } as AgencyCode)?.code
    }

    it.sendService(new SncbryQuery('432501192608057027'))
    def ryResult = it.getResult(Sncbry)

    ryResult.resultSet.each {ry ->
        println ry
        it.sendService(new LtxryQuery(ry.idCard, getAgencyCode(ry.agencyName)))
        // println it.readBody()
        def rs = it.getResult(Ltxry)
        rs.resultSet.each {
            println it
        }
    }
 */
    it.sendService(new YlgrzhQuery('430304198907193524'))
    //println it.readBody()
    def result = it.getResult(Ylgrzh)
    println result
    result.resultSet?.each {zh ->
        it.sendService(new YlgrzhzhQuery(zh.pid))
        println it.readBody()
        it.sendService(new YlgrzhmxQuery(zh.pid))
        def mxResult = it.getResult(Ylgrzhmx)
        println mxResult.resultSet?.find {
            it.year == '2020'
        }
    }

}
/*
println new ParamList('abcefg',
        [
                'name': 'ac01'
        ],
        [
            'aac001': '8829087',
            'aac002': '======='
        ]
).toXml()

println new ClientSql('abc', 'efg', 'sssssss').toXml()
*/