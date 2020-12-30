package yhsb.base.util

import java.text.SimpleDateFormat

class DateTime {
    static String format(String pattern = 'yyyyMMdd', Date date = new Date()) {
        new SimpleDateFormat(pattern).format(date)
    }

    static String toDashedDate(String date, String dateFormat = /^(\d\d\d\d)(\d\d)(\d\d)$/) {
        def m = date =~ dateFormat
        if (m.find()) {
            "${m.group(1)}-${m.group(2)}-${m.group(3)}"
        } else {
            throw new IllegalArgumentException("Invalid date format ($dateFormat)")
        }
    }
}
