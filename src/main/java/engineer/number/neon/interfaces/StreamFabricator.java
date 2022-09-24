package engineer.number.neon.interfaces;

import engineer.number.neon.NeonException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

public class StreamFabricator extends Fabricator {
    private final InputStream s;
    int index = 0;

    public StreamFabricator(InputStream s) {
        this.s = s;
    }

    @Override
    public void traverseUpToChar(char c) {
        int cur;
        try {
            while ((cur = s.read()) != -1 && cur != c) {
            }
        } catch (Exception e) {
            throw new NeonException(e);
        }
    }

    @Override
    public String readTextUntil(char c) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int cur;
            while ((cur = s.read()) != -1 && cur != c) {
                stringBuilder.append((char) cur);
            }
        } catch (Exception e) {
            throw new NeonException(e);
        }
        return stringBuilder.toString();
    }

    @Override
    public char readCharBefore(char c) {
        try {
            int cur;
            int previous = -1;
            while ((cur = s.read()) != -1 && cur != c) {
                previous = cur;
            }
            if (previous == -1) {
                throw new NeonException("previous=-1");
            }
            return (char) previous;
        } catch (Exception e) {
            throw new NeonException(e);
        }
    }

    @Override
    public byte[] readXByte(int x) {
        final byte[] b = new byte[x];
        int readSoFar = 0;
        try {
            while (x > 0) {
                int len = s.read(b, readSoFar, x);
                if (len != -1) {
                    readSoFar += len;
                    x -= len;
                } else {
                    throw new NeonException("read less byte than needed");
                }
            }
        } catch (IOException e) {
            throw new NeonException(e);
        }
        return b;
    }

    @Override
    public String readBoundString() {
        traverseUpToChar('"');
        final int i = Integer.parseInt(readTextUntil(':'));
        return new String(readXByte(i), StandardCharsets.UTF_8);
    }

    @Override
    public void closeIndent(Consumer<String> stringConsumer) {
        int rawIndent = 1;
        try {
            while (rawIndent > 0) {
                final char read = (char) s.read();
                switch (read) {
                    case '"':
                        final int i = Integer.parseInt(readTextUntil(':'));
                        s.skip(i);
                        break;
                    case '{':
                        rawIndent++;
                        break;
                    case '~':
                        char nextChar = (char) s.read();
                        if (nextChar != '=') {
                            final String t = readTextUntil('{');
                            stringConsumer.accept(nextChar + t);
                            rawIndent++;
                        }
                        break;
                    case '}':
                        rawIndent--;
                        break;
                }
            }
        } catch (IOException e) {
            throw new NeonException(e);
        }
    }

    @Override
    public String readTextFromTo(char c, char c1) {
        traverseUpToChar(c);
        return readTextUntil(c1);
    }

    @Override
    public boolean verifyHeader(String header) {
        final byte[] bytes2 = header.getBytes();
        s.mark(bytes2.length);
        final byte[] bytes1 = readXByte(bytes2.length);
        final boolean equals = Arrays.equals(bytes1, bytes2);
        if (equals) {
            return true;
        } else {
            try {
                s.reset();
            } catch (IOException e) {
            }
            return false;
        }
    }

    @Override
    public String toString() {
        s.mark(100);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int len = 100;
        while (len-- > 0) {
            try {
                byteArrayOutputStream.write(readXByte(1));
            } catch (IOException e) {
            } catch (NeonException e) {
                break;
            }
        }
        try {
            s.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }
}
