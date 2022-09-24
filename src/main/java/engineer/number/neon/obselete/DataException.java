package engineer.number.neon.obselete;

class DataException extends Exception {
    private String smallMsg;

    public DataException(String className) {
        super("############ " + className + " not found ############");
        setStackTrace(new StackTraceElement[0]);
    }

    public DataException() {
        super();
    }
}

