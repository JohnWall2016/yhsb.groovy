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

    static YearMonth from(int yearMonth) {
        new YearMonth(yearMonth.intdiv(100), yearMonth % 100)
    }

    int compareTo(YearMonth that) {
        (this.year * 12 + this.month) - (that.year * 12 + that.month)
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
