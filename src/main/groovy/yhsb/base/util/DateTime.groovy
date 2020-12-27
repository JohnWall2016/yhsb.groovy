package yhsb.base.util

import java.text.SimpleDateFormat

class DateTime {
    static String format(String pattern = 'yyyyMMdd', Date date = new Date()) {
        new SimpleDateFormat(pattern).format(date)
    }
}
