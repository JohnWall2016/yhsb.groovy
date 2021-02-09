package yhsb.base.util

import yhsb.base.util.collections.LinkedNode

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
    var boughtSpans = LinkedNode.of(
            new YearMonthRange(new YearMonth(2008, 1), new YearMonth(2009, 9)),
            new YearMonthRange(new YearMonth(2010, 2), new YearMonth(2010, 4)),
    )

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

    List<LinkedNode<YearMonthRange>> spans
    if (canBuyMonths >= totalMonths) {
        var offset = canBuyMonths - totalMonths
        spans = validBounds.offset(offset).split(bonusMonths)
    } else if (canBuyMonths >= bonusMonths) {
        spans = validBounds.split(bonusMonths)
    } else {
        spans = [validBounds, LinkedNode.empty()] as List<LinkedNode<YearMonthRange>>
    }
    println "享受补贴年限: ${spans[0].join('|')}|共计${spans[0].months}个月"
    println "个人缴费年限: ${spans[1].join('|')}|共计${spans[1].months}个月"
}

//testYearMonth()
testYearMonthRange()
