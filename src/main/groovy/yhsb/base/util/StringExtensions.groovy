package yhsb.base.util

class StringExtensions {
    private static int padCount(
            String s,
            int width,
            List<SpecialChars> specialChars
    ) {
        int count = 0
        s.each { ch ->
            count += specialChars.find { sc ->
                ch as char in sc.chars
            }?.width ?: 1
        }
        width - count
    }

    static String pad(
            String self,
            int width,
            char padChar = ' ',
            List<SpecialChars> specialChars = SpecialChars.chinese,
            boolean left = false
    ) {
        def count = padCount(self, width, specialChars)
        if (count > 0) {
            def builder = new StringBuilder()
            if (left) builder.append(padChar.toString() * count)
            builder.append(self)
            if (!left) builder.append(padChar.toString() * count)
            builder.toString()
        } else {
            self
        }
    }

    static String padLeft(
            String self,
            int width,
            char padChar = ' ',
            List<SpecialChars> specialChars = SpecialChars.chinese
    ) {
        self.pad(width, padChar, specialChars, true)
    }

    static String padRight(
            String self,
            int width,
            char padChar = ' ',
            List<SpecialChars> specialChars = SpecialChars.chinese
    ) {
        self.pad(width, padChar, specialChars, false)
    }
}

interface IsCase {
    boolean isCase(char ch)
}

class CharRange implements IsCase {
    final char start
    final char end

    CharRange(char start, char end) {
        this.start = start
        this.end = end
    }

    boolean isCase(char ch) {
        if (ch >= start && ch <= end) true
        else false
    }
}

class CharList implements IsCase {
    final char[] chars

    CharList(char[] chars) {
        this.chars = chars
    }

    CharList(int[] chars) {
        char[] chs = new char[chars.length]
        chars.eachWithIndex { int entry, int i ->
            chs[i] = entry as char
        }
        this.chars = chs
    }

    boolean isCase(char ch) {
        chars.contains(ch)
    }
}

class SpecialChars<T extends IsCase> {
    final T chars
    final int width

    SpecialChars(T chars, int width) {
        this.chars = chars
        this.width = width
    }

    static final SpecialChars chineseChars = new SpecialChars(
            new CharRange('\u4000' as char, '\u9fa5' as char),
            2
    )

    static final SpecialChars chineseSymbols = new SpecialChars(
            new CharList(
                    [
                            8211, 8212, 8216, 8217, 8220, 8221,
                            8230, 12289, 12290, 12296, 12297, 12298,
                            12299, 12300, 12301, 12302, 12303, 12304,
                            12305, 12308, 12309, 65281, 65288, 65289,
                            65292, 65294, 65306, 65307, 65311
                    ] as int[]
            ), 2
    )

    static final List<SpecialChars> chinese = [chineseChars, chineseSymbols]
}