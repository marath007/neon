package engineer.number.neon.interfaces;

import java.util.Arrays;
import java.util.function.Consumer;

public class HardFabricator extends Fabricator {
    private final String s;
    private final int length;
    int index = 0;
    int mark = 0;

    public HardFabricator(String s) {
        this.s = s;
        length = s.length();
    }

    @Override
    public void traverseUpToChar(char c) {
        while (index + 1 < length && (s.charAt(index++)) != c) {
        }
    }

    @Override
    public String readTextUntil(char c) {
        int start_index = index;
        traverseUpToChar(c);
        return s.substring(start_index, index - 1);
    }

    @Override
    public char readCharBefore(char c) {
        traverseUpToChar(c);
        return s.charAt(index - 1);
    }

    @Override
    public byte[] readXByte(int x) {
        StringBuilder stringBuilder = new StringBuilder();
        int cur = 0;
        while (cur < x && index < length) {
            final char c = s.charAt(index++);//wtf help me
            cur += String.valueOf(c).getBytes().length;//gross as fuck
            stringBuilder.append(c);//wtf help me
        }
        return stringBuilder.toString().getBytes();
    }

    @Override
    public String readBoundString() {
        traverseUpToChar('"');
        return readXChars(Integer.parseInt(readTextUntil(':')));
    }

    @Override
    public void closeIndent(Consumer<String> stringConsumer) {
        int rawIndent = 1;
        while (rawIndent > 0) {
            final char c = s.charAt(index++);//wtf help me
            switch (c) {
                case '"':
                    final int i = Integer.parseInt(readTextUntil(':'));
                    index += i;
                    break;
                case '{':
                    rawIndent++;
                    break;
//                case '~':
//                    char nextChar = (char) s.charAt(index++);
//                    if (nextChar != '=') {
//                        final String t = readTextUntil('{');
//                        stringConsumer.accept(nextChar + t);
//                        rawIndent++;
//                    }
//                    rawIndent++;
//                    break;
                case '}':
                    rawIndent--;
                    break;
            }
        }
    }

    @Override
    public String readTextFromTo(char from, char to) {
        traverseUpToChar(from);
        return readTextUntil(to);
    }

    @Override
    public boolean verifyHeader(String header) {
        final byte[] bytes2 = header.getBytes();
        mark = index;
        final byte[] bytes1 = readXByte(bytes2.length);
        final boolean equals = Arrays.equals(bytes1, bytes2);
        if (equals) {
            return true;
        } else {
            index = mark;
            return false;
        }
    }

    private String readXChars(int x) {
        int start_index = index;
        index += x + 1;//wtf help me
        return s.substring(start_index, index - 1);//wtf help me
    }

    @Override
    public String toString() {
        if (index > s.length()) {
            return "EOF";
        } else {
            return s.substring(index);
        }
    }
}
