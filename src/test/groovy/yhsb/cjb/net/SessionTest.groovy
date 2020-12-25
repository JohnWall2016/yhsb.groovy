package yhsb.cjb.net

Session.use() {
    it.sendService(new CbxxQuery('430311194511291027'))
    def result = it.getResult(Cbxx)
    println(result)
    result.each {cbxx ->
        println cbxx
        println "${cbxx.cbState} ${cbxx.jfState} ${cbxx.jbKind} ${cbxx.jbState}"
        println cbxx.czName
        println cbxx.dwName
    }
}
