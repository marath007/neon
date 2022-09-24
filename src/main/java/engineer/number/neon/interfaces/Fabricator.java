package engineer.number.neon.interfaces;

import java.util.function.Consumer;

public abstract class Fabricator {
    /**
     * @param c move reader up to. So if you do read 1 char, it won't be the one it stopped at.
     *          side consequence is moving of at least 1 char.
     */
    abstract public void traverseUpToChar(char c);

    abstract public String readTextUntil(char c);

    abstract public char readCharBefore(char c);

    abstract public byte[] readXByte(int x);

    public abstract String readBoundString();

    public abstract void closeIndent(Consumer<String >stringConsumer);

    public abstract String readTextFromTo(char c, char c1);
    public abstract boolean verifyHeader(String header);
}
