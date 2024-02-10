package engineer.number.neon;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class StreamDeconstructor extends Deconstructor {
    OutputStream stream;
    //make a buffered deconstructor using string builder

    public StreamDeconstructor(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void append(int value) {
        appendInternalString(String.valueOf(value));
    }

    @Override
    public void append(long value) {
        appendInternalString(String.valueOf(value));
    }

    @Override
    public void append(char value) {
        appendInternalString(String.valueOf(value));
    }

    @Override
    public void append(double value) {
        appendInternalString(String.valueOf(value));
    }

    @Override
    public void append(float value) {
        appendInternalString(String.valueOf(value));
    }

    @Override
    public void append(byte value) {
        try {
            stream.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void append(boolean value) {
        appendInternalString(String.valueOf(value));
    }

    @Override
    public void appendString(String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        append('"');
        append(bytes.length);
        append(':');
        append(bytes);
    }

    @Override
    public void appendInternalString(String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        append(bytes);
    }

    @Override
    public void append(byte[] value) {
        try {
            stream.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
