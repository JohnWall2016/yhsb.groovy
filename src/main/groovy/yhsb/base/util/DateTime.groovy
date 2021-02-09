package yhsb.base.util

import yhsb.base.util.collections.LinkedNode

import java.text.SimpleDateFormat

class DateTime {
    static String format(String pattern = 'yyyyMMdd', Date date = new Date()) {
        new SimpleDateFormat(pattern).format(date)
    }

    static String toDashedDate(String date, String format = /^(\d\d\d\d)(\d\d)(\d\d)$/) {
        def m = date =~ format
        if (m.find()) {
            "${m.group(1)}-${m.group(2)}-${m.group(3)}"
        } else {
            throw new IllegalArgumentException("Invalid date format ($format)")
        }
    }

    static Tuple3<String, String, String> split(
            String date,
            String format = /^(\d\d\d\d)(\d\d)(\d\d)?$/
    ) {
        def m = date =~ format
        if (m.find()) {
            new Tuple3<>(m.group(1), m.group(2), m.group(3))
        } else {
            throw new IllegalArgumentException("Invalid date format ($format)")
        }
    }
}

class YearMonth implements Comparable<YearMonth> {
    final int year
    final int month

    YearMonth(int year, int month) {
        this.year = year
        this.month = month

        if (month < 1 || month > 12)
            throw new IllegalArgumentException('month must be >= 1 and <= 12')
    }

    YearMonth offset(int months) {
        months = month + months
        var y = months.intdiv(12)
        var m = months % 12
        if (m <= 0) {
            y -= 1
            m += 12
        }
        new YearMonth(year + y, m)
    }

    static YearMonth from(int yearMonth) {
        new YearMonth(yearMonth.intdiv(100), yearMonth % 100)
    }

    int compareTo(YearMonth that) {
        (this.year * 12 + this.month) - (that.year * 12 + that.month)
    }

    YearMonth max(YearMonth that) {
        if (this > that) this else that
    }

    YearMonth min(YearMonth that) {
        if (this < that) this else that
    }

    @Override
    boolean equals(Object obj) {
        if (!obj || !YearMonth.isInstance(obj)) return false
        compareTo(obj as YearMonth) == 0
    }

    @Override
    int hashCode() {
        year * 12 + month
    }

    @Override
    String toString() {
        String.format('%04d%02d', year, month)
    }
}

class YearMonthRange {
    final YearMonth start
    final YearMonth end

    YearMonthRange(YearMonth start, YearMonth end) {
        this.start = start
        this.end = end

        if (start > end)
            throw new IllegalArgumentException('start must be less than or equal end')
    }

    LinkedNode<YearMonthRange> minus(YearMonthRange that) {
        if (that.end < this.start || this.end < that.start) {
            LinkedNode.of(this)
        } else if (that.start <= this.start) {
            if (that.end < this.end) {
                LinkedNode.of(new YearMonthRange(that.end.offset(1), this.end))
            } else {
                LinkedNode.empty()
            }
        } else { // that.start > this.start
            if (that.end < this.end) {
                LinkedNode.of(
                        new YearMonthRange(this.start, that.start.offset(-1)),
                        new YearMonthRange(that.end.offset(1), this.end)
                )
            } else {
                LinkedNode.of(new YearMonthRange(this.start, that.start.offset(-1)))
            }
        }
    }

    LinkedNode<YearMonthRange> minus(LinkedNode<YearMonthRange> those) {
        subtract(LinkedNode.of(this), those)
    }

    static LinkedNode<YearMonthRange> subtract(
            LinkedNode<YearMonthRange> these,
            LinkedNode<YearMonthRange> those
    ) {
        those.each { that ->
            List<LinkedNode<YearMonthRange>> list = []
            these.each {
                list.add(it - that)
            }
            these = LinkedNode.flatten(list)
        }
        these
    }

    int getMonths() {
        (end.year * 12 + end.month) - (start.year * 12 + start.month) + 1
    }

    @Override
    String toString() {
        "$start-$end"
    }
}

class DateTimeExtensions {
    static int getMonths(LinkedNode<YearMonthRange> these) {
        var total = 0
        these.each {
            total += it.months
        }
        total
    }

    static LinkedNode<YearMonthRange> offset(LinkedNode<YearMonthRange> these, int months) {
        if (these.empty) return these
        var cur = these
        var firstMonths = cur.data.months
        if (firstMonths > months) {
            LinkedNode.cons(
                    new YearMonthRange(cur.data.start.offset(months), cur.data.end),
                    cur.next
            )
        } else if (firstMonths == months) {
            cur.next
        } else {
            cur.next.offset(months - firstMonths)
        }
    }

    static List<LinkedNode<YearMonthRange>> split(LinkedNode<YearMonthRange> these, int months) {
        if (these.empty)
            return [LinkedNode.empty(), LinkedNode.empty()] as List<LinkedNode<YearMonthRange>>
        var cur = these
        var firstMonths = cur.data.months
        if (firstMonths > months) {
            [
                    LinkedNode.of(new YearMonthRange(cur.data.start, cur.data.start.offset(months - 1))),
                    LinkedNode.cons(new YearMonthRange(cur.data.start.offset(months), cur.data.end), cur.next)
            ]
        } else if (firstMonths == months) {
            [
                    LinkedNode.of(new YearMonthRange(cur.data.start, cur.data.end)),
                    cur.next,
            ]
        } else {
            var list = cur.next.split(months - firstMonths)
            [
                    LinkedNode.cons(new YearMonthRange(cur.data.start, cur.data.end), list[0]),
                    list[1]
            ]
        }
    }
}
