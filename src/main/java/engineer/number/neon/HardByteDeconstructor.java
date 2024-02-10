package engineer.number.neon;

class HardByteDeconstructor extends Deconstructor {
    private StringBuilder sb = new StringBuilder();

    @Override
    public void append(int value) {
        append(ByteUtil.toByta(value));
    }

    @Override
    public void append(long value) {
        append(ByteUtil.toByta(value));
    }

    @Override
    public void append(char value) {
        append(ByteUtil.toByta(value));
    }

    @Override
    public void append(double value) {
        append(ByteUtil.toByta(value));
    }

    @Override
    public void append(float value) {
        append(ByteUtil.toByta(value));
    }

    @Override
    public void append(byte value) {
        append(ByteUtil.toByta(value));
    }

    @Override
    public void append(boolean value) {
        append(ByteUtil.toByta(value));
    }

    @Override
    public void appendString(String value) {
        sb.append((value));
    }

    @Override
    public void appendInternalString(String value) {
    }

    @Override
    public void append(byte[] value) {
        sb.append(new String(value));
    }



    @Override
    public String toString() {
        return sb.toString();
    }
}
