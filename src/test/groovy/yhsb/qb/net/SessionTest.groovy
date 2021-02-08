package yhsb.qb.net

// println((new SncbryQuery('430302195806251012')).toXml())

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
/*
    it.sendService(new AccountQuery('430302196209180513'))
    //println it.readBody()
    def result = it.getResult(Account)
    println result
    result.resultSet?.each { zh ->
        it.sendService(new AccountTotalQuery(zh.pid))
        println it.readBody()
        it.sendService(new AccountDetailQuery(zh.pid))
        def mxResult = it.getResult(AccountDetail)
        println mxResult.resultSet?.find {
            it.year == '2020'
        }
    }
 */
    //it.sendService(new InProvincePersonQuery('430302196408210017'))
    //println it.readBody()
    /*
    it.sendService(new JoinedPersonQuery('43030219640828151X', '430399'))
    //println it.readBody()
    def result = it.getResult(JoinedPerson)

    result.resultSet?[1].with {p ->
        println "${p.id} ${p.agencyCode}"

        it.sendService(new JoinedPersonGeneralQuery(p.id, p.agencyCode))
        println it.readBody()

        it.sendService(new JoinedPersonPayDetailQuery(p.id, p.agencyCode))
        //println it.readBody()
        def payDetailResult = it.getResult(JoinedPersonPayDetail)
        payDetailResult.resultSet?.each {
            println it
        }
    }
     */
    /*it.sendService(new InProvincePersonQuery('43030219640828151X'))
    println it.readBody()*/

    /*it.sendService(new TaxPushCompanyPayInfoQuery())
    println it.getResult(TaxPushCompanyPayInfo)*/

    /*it.sendService(new TaxPushSpecialPayInfoQuery())
    println it.getResult(TaxPushSpecialPayInfo)*/

    /*it.sendService(new TaxPushPersonalPayInfoQuery())
    println it.getResult(TaxPushPersonalPayInfo)*/
    /*it.sendService(new NoUKeyWorkerAddQuery())
    println it.getResult(NoUKeyWorkerAdd)*/
    /*it.sendService(new NoUKeyWorkerContinueQuery())
    println it.getResult(NoUKeyWorkerContinue)*/
    /*it.sendService(new NoUKeyWorkerJoinInProvinceQuery())
    println it.getResult(NoUKeyWorkerJoinInProvince)*/
    it.sendService(new NoUKeyWorkerJoinInChangShaQuery())
    println it.getResult(NoUKeyWorkerJoinInChangSha)
}
