package engineer.number.neon.obselete;


import java.io.PrintWriter;
import java.io.StringWriter;

class InvalidNeonDesign extends Error {
    private String smallMsg;

    public InvalidNeonDesign(Throwable e, String s) {
        super(eToString(e)+decorator(s));
        smallMsg = s;
    }

    public InvalidNeonDesign(String s) {
        super(decorator(s));
        smallMsg = s;
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

