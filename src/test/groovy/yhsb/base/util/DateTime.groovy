package yhsb.base.util

void testYearMonth() {
    var ym1 = new YearMonth(2010, 8)
    var ym2 = new YearMonth(2011, 12)
    var ym3 = new YearMonth(2010, 8)
    println ym1
    println ym2
    println ym1 < ym2
    println ym1 > ym2
    println ym1 == ym3
    println ym1 == ym2

    var map = [:]
    map[ym1] = 'ym1'
    map[ym3] = 'ym1'
    println map
}

testYearMonth()
