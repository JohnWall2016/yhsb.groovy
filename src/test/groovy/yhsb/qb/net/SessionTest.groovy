package yhsb.qb.net

// println((new SncbryQuery('430302195806251012')).toXml())

Session.use {
    it.sendService(new SncbryQuery('430302195806251012'))
    //println it.readBody()

    def result = it.getResult(Sncbry)
    println result

    result.resultSet.each {
        println it
    }
}


