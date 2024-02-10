package engineer.number.neon;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class BufferedStreamDeconstructorLazy extends Deconstructor implements Closeable {
    OutputStream stream;
    private StringBuilder sb = new StringBuilder();
    //make a buffered deconstructor using string builder

    public BufferedStreamDeconstructorLazy(OutputStream stream) {
        this.stream = stream;
    }

    private void flushStringBuilder() {
        if (sb.length() > 0) {
            try {
                stream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                sb.setLength(0); // Clear the StringBuilder
            } catch (IOException e) {
                // Handle exception
            }
        }
    }

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
        flushStringBuilder();
        try {
            stream.write(value);
        } catch (IOException e) {
            // Handle exception
        }
    }

    public void close() throws IOException {
        flushStringBuilder();
    }
    @Override
    public String toString() {
        return "Stream has no string to show";
    }
}
