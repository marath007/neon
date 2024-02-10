package engineer.number.neon;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

class ThreadedBufferedStreamDeconstructor extends BufferedStreamDeconstructor implements Closeable {
    private final Consumer<Boolean> onFinished;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    boolean failed = false;

    public ThreadedBufferedStreamDeconstructor(OutputStream stream, Consumer<Boolean> onFinished) {
        super(stream);
        this.onFinished = onFinished;
    }

    @Override
    protected void flushStringBuilder() {
        if (sb.length() > 0) {
            final StringBuilder sb1 = sb;
            sb=new StringBuilder(BUFFER_SIZE); // Clear the StringBuilder
            executor.submit(() -> {
                try {
                    final byte[] bytes = sb1.toString().getBytes(StandardCharsets.UTF_8);
                    stream.write(bytes);
                } catch (IOException e) {
                    failed = true;
                }
            });
        }
    }

    @Override
    public void close()  {
        super.close();
        executor.submit(() -> {
            try {
                stream.close();
            } catch (IOException e) {
                failed = true;

            }
            onFinished.accept(failed);
        });
    }
}
