package yhsb.base.util

class NumericExtensions {
    static boolean isValidInt(Double d) {
        d.toInteger().toDouble() == d
    }

    private final static List<String> chineseNumbers = [
            "零", "壹", "贰", "叁", "肆",
            "伍", "陆", "柒", "捌", "玖",
    ]

    private final static List<String> places = [
            "", "拾", "佰", "仟", "万", "亿",
    ]

    private final static List<String> units = [
            "元", "角", "分",
    ]

    private final static String whole = '整'

    static String toChineseMoney(BigDecimal money) {
        def number = (money.round(2) * 100).toBigInteger()
        def integer = number.divide(100 as BigInteger)
        def fraction = number % 100

        def length = integer.toString().length()
        def ret = ''
        def zero = false
        for (int i = length - 1; i >= 0; i--) {
            def base = 10 ** i as BigInteger
            def quot = integer.divide(base)
            if (quot > 0) {
                if (zero) ret += chineseNumbers[0]
                ret += chineseNumbers[quot] + places[i % 4]
                zero = false
            } else if (quot == 0 && ret) {
                zero = true
            }
            if (i >= 4) {
                if (i % 8 == 0 && ret) {
                    ret += places[5]
                } else if (i % 4 == 0 && ret) {
                    ret += places[4]
                }
            }
            integer %= base
            if (integer == 0 && i != 0) {
                zero = true
                break
            }
        }
        ret += units[0]

        if (fraction == 0) { // .00
            ret += whole
        } else {
            def quot = fraction.intdiv(10)
            def rmnd = fraction % 10
            if (rmnd == 0) { // .D0
                if (zero) ret += chineseNumbers[0]
                ret += chineseNumbers[quot] + units[1] + whole
            } else {
                if (zero || quot == 0) { // .0D or .DD
                    ret += chineseNumbers[0]
                }
                if (quot != 0) { // .DD
                    ret += chineseNumbers[quot] + units[1]
                }
                ret += chineseNumbers[rmnd] + units[2]
            }
        }
        ret
    }
}
