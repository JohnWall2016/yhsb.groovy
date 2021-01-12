package yhsb.cjb.net

Session.use() {
    it.sendService(new SncbxxQuery('430311194511291027'))
    def result = it.getResult(Sncbxx)
    println(result)
    result.each {cbxx ->
        println cbxx
        println "${cbxx.cbState} ${cbxx.jfState} ${cbxx.jbKind} ${cbxx.jbState}"
        println cbxx.czName
        println cbxx.dwName
    }
}
