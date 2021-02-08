package yhsb.base.util

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
    int year
    int month

    YearMonth(int year, int month) {
        if (month < 1 || month > 12)
            throw new IllegalArgumentException('month must be >= 1 and <= 12')
        this.year = year
        this.month = month
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

    YearMonth offsetInPlace(int months) {
        months = month + months
        var y = months.intdiv(12)
        var m = months % 12
        if (m <= 0) {
            y -= 1
            m += 12
        }
        year += y
        month = m
        this
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

    YearMonth copy() {
        new YearMonth(this.year, this.month)
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
    YearMonth start
    YearMonth end

    YearMonthRange(YearMonth start, YearMonth end) {
        if (start > end)
            throw new IllegalArgumentException('start must be less than or equal end')

        this.start = start
        this.end = end
    }

    List<YearMonthRange> minus(YearMonthRange that) {
        if (that.end < this.start || this.end < that.start) {
            [this]
        } else if (that.start <= this.start) {
            if (that.end < this.end) {
                [new YearMonthRange(that.end.offset(1), this.end)]
            } else {
                []
            }
        } else { // that.start > this.start
            if (that.end < this.end) {
                [new YearMonthRange(this.start, that.start.offset(-1)), new YearMonthRange(that.end.offset(1), this.end)]
            } else {
                [new YearMonthRange(this.start, that.start.offset(-1))]
            }
        }
    }

    List<YearMonthRange> minus(List<YearMonthRange> those) {
        subtract([this], those)
    }

    static List<YearMonthRange> subtract(List<YearMonthRange> these, List<YearMonthRange> those) {
        those.each { that ->
            these = these.flatten {
                it - that
            }
        }
        these
    }

    int getMonths() {
        (end.year * 12 + end.month) - (start.year * 12 + start.month) + 1
    }

    YearMonthRange copy() {
        new YearMonthRange(this.start.copy(), this.end.copy())
    }

    @Override
    String toString() {
        "$start-$end"
    }
}

class DateTimeExtensions {
    static int getMonths(List<YearMonthRange> these) {
        var total = 0
        these.each {
            total += it.months
        }
        total
    }

    static LinkedList<YearMonthRange> offsetInPlace(LinkedList<YearMonthRange> these, int months) {
        if (these.empty) return these
        var first = these.first()
        var firstMonths = first.months
        if (firstMonths > months) {
            first.start.offsetInPlace(months)
        } else {
            these.remove(0)
            if (firstMonths < months) {
                these.offsetInPlace(months - firstMonths)
            }
        }
        these
    }

    static List<LinkedList<YearMonthRange>> splitInPlace(LinkedList<YearMonthRange> these, int months) {
        if (these.empty) return [[], []]
        var first = these.first()
        var firstMonths = first.months
        if (firstMonths > months) {
            [[new YearMonthRange(first.start, first.start.offset(months - 1))],
             these.offsetInPlace(months)]
        } else {
            first = these.removeFirst()
            if (firstMonths == months) {
                [[first], these]
            } else {
                var list = these.splitInPlace(months - firstMonths)
                [list[0].addFirst(first), list[1]]
            }
        }
    }
}
