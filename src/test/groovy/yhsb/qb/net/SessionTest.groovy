package yhsb.qb.net

// println((new SncbryQuery('430302195806251012')).toXml())

Session.use {
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
}
