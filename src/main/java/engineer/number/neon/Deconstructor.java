package engineer.number.neon;

public abstract class Deconstructor {
    public abstract void append(int value);

    public abstract void append(long value);

    public abstract void append(char value);

    public abstract void append(double value);

    public abstract void append(float value);

    public abstract void append(byte value);

    public abstract void append(boolean value);

    /**
     * this implements the string length
     */
    public abstract void appendString(String value);
    /**
     * this doesnt implements the string length
     */
    public abstract void appendInternalString(String value);

    public abstract void append(byte[] value);

    public void append(Byte[] value) {
        byte[] bytes = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            bytes[i] = value[i];
        }
        append(bytes);
    }

    public StringBuilder sb() {
        return null;
    }
}
