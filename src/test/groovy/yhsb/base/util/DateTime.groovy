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

void testYearMonthRange() {
    var birthDay = new YearMonth(1990, 12)
    var boughtSpans = [
            new YearMonthRange(new YearMonth(2008, 1), new YearMonth(2009, 9)),
            new YearMonthRange(new YearMonth(2010, 2), new YearMonth(2010, 4)),
    ]

    var totalMonths = 180
    var bonusMonths = 48

    var startMonth = birthDay.offset(16 * 12 + 1).max(new YearMonth(2005, 8))
    var endMonth = new YearMonth(2020, 7)

    var validBound = new YearMonthRange(startMonth, endMonth)
    println "可以缴费年限: $validBound"

    println "已经缴费年限: ${boughtSpans.join('|')}"

    var validBounds = validBound - boughtSpans
    var canBuyMonths = validBounds.months

    println "尚未缴费年限: ${validBounds.join('|')}|共计${canBuyMonths}个月"


}

testYearMonth()
