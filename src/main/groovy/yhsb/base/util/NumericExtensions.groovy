package yhsb.base.util

class NumericExtensions {
    static boolean isValidInt(Double d) {
        d.toInteger().toDouble() == d
    }
}
