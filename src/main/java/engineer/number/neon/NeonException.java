package engineer.number.neon;


import java.io.PrintWriter;
import java.io.StringWriter;

public class NeonException extends RuntimeException {
    private String smallMsg;

    public NeonException(Throwable e, String s) {
        super(eToString(e)+decorator(s));
        smallMsg = s;
    }
    public NeonException(String s) {
        super(decorator(s));
        smallMsg = s;
    }
    public NeonException(Exception e) {
        super(e instanceof  NeonException?e.getCause():e);
    }

    private static String decorator(String s) {
        return "\n\n############ ERROR DETECTED ############\n# #\n# #\n# #    " + s + "\n# #\n# #\n########### CLICK BELLOW ME ##########\n";
    }

    public static String eToString(Throwable e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return exceptionAsString;
    }

    public String getSmallMsg() {
        return smallMsg;
    }

    public void printMsg() {
        System.err.println(smallMsg);
    }
}

