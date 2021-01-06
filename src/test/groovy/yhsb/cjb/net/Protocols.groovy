package yhsb.cjb.net

// println JbKind.map['011']

Session.use {
    it.sendService(new DyfhQuery('430302196003130043', ''))
    def result = it.getResult(Dyfh)
    result.each {
        println it
        println it.paymentInfoUrl
        def m = it.paymentInfoMatcher
        println m
        println m.group(1)
    }
}