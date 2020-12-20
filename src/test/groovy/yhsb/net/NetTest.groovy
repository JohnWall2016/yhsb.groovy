package yhsb.net

new HttpSocket("124.232.169.221", 80).withCloseable {
    println it.getHttp('/')
}

