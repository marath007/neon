package engineer.number.neon;

class HardDeconstructor extends Deconstructor {
    private StringBuilder sb = new StringBuilder();

    @Override
    public void append(int value) {
        sb.append(value);
    }

    @Override
    public void append(long value) {
        sb.append(value);
    }

    @Override
    public void append(char value) {
        sb.append(value);
    }

    @Override
    public void append(double value) {
        sb.append(value);
    }

    @Override
    public void append(float value) {
        sb.append(value);
    }

    @Override
    public void append(byte value) {
        sb.append(value);
    }

    @Override
    public void append(boolean value) {
        sb.append(value);
    }

    @Override
    public void appendString(String value) {
        sb.append('"');
        sb.append(value.length());
        sb.append(':');
        sb.append((value));
    }

    @Override
    public void appendInternalString(String value) {
        sb.append(value);
    }

    @Override
    public void append(byte[] value) {
        sb.append(new String(value));
    }

    @Override
    public String toString() {
        return sb.toString();
    }
    public StringBuilder sb() {
        return sb;
    }
}
