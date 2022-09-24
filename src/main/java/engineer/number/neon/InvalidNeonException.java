package engineer.number.neon;

public class InvalidNeonException extends Exception {
    public InvalidNeonException(String className) {
        super(className);
    }

    public InvalidNeonException(Exception e) {
        super(e instanceof  InvalidNeonException?e.getCause():e);
    }
}

