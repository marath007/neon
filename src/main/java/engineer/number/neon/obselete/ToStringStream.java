package engineer.number.neon.obselete;

import engineer.number.neon.NeonException;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static engineer.number.neon.obselete.Neon.classToCompressedName;
import static engineer.number.neon.obselete.Neon.fieldsOfClass;
import static engineer.number.neon.obselete.Neon.fillFieldList;
//import library.misc.PrintWriter;

/**
 * convert any classes to a usable string
 * {}[] are escaped from strings
 * ~ distinguish class name from field name
 */
abstract class ToStringStream {
    static Pattern escape = Pattern.compile("([\\[\\]{}\\\\])");
    static Pattern unescape = Pattern.compile("\\\\(?!\\\\)");

    public static boolean toString(Object o, PrintWriter printWriter) {
        try {
            appendClass(printWriter, o);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            printWriter.close();
        }
        return false;
    }

    private static void appendClass(PrintWriter printWriter, Object o) throws IllegalAccessException {
        if (o == null) {
            printWriter.print("{NULL}");
        } else {
            appendClassPrefix(printWriter, o.getClass().getName());
            if (!fieldsOfClass.containsKey(o.getClass().getName())) {
                fillFieldList(o);//so this doesnt work or something?
            }
            if (Neon.CustomToString.class.isAssignableFrom(o.getClass())) {
                ((Neon.CustomToString) o).fastToString(printWriter);
            } else {
                appendFields(printWriter, o, fieldsOfClass.get(o.getClass().getName()).values());
            }
            appendSuffix(printWriter);
        }
    }

    private static void appendFields(PrintWriter printwriter, Object o, Collection<Field> fields) throws IllegalAccessException {
        for (Field field : fields) {
            appendUnknownObject(printwriter, field.getType(), field.get(o), field.getName());
        }
//        for (int i = 0; i < fields.size(); i++) {
//
//            appendUnknownObject(PrintWriter, fields.getType(), field.get(o), field.getName());
//        }
    }

    private static void appendCollection(PrintWriter printWriter, Object collection) throws IllegalAccessException {
        printWriter.print('[');
        Collection abstractList = (Collection) collection;
        for (Object o : abstractList) {
            appendUnknownObject(printWriter, o.getClass(), o, null);
        }
        printWriter.print(']');
    }

    private static void appendMap(PrintWriter printWriter, Object map) throws IllegalAccessException {
        printWriter.print('[');
        Map abstractMap = (Map) map;
        Iterator key = abstractMap.keySet().iterator();
        Iterator value = abstractMap.values().iterator();
        for (int i = 0; i < abstractMap.size(); i++) {
            Object tKey = key.next();
            Object tValue = value.next();
            if (tKey != null && tValue != null) {
                appendUnknownObject(printWriter, tKey.getClass(), tKey, null);
                appendUnknownObject(printWriter, tValue.getClass(), tValue, null);
            } else {
//                System.out.println("egg"); // TODO: 21/02/20 fix me?
            }
        }
        printWriter.print(']');
    }

    /**
     * @param name is nullable
     */
    private static void appendUnknownObject(PrintWriter printWriter, Class c, Object o, String name) throws IllegalAccessException {
        if (o == null) {
            return;
        }
        if (isPrimitiveOrPrimitiveWrapper(c)) {
            appendValue(printWriter, name, o);
        } else if (c.equals(String.class)) {
            appendString(printWriter, name, o);
        } else if (c.isArray()) {
            appendValuePrefix(printWriter, name);
            appendArray(printWriter, o);
            appendSuffix(printWriter);
        } else if (o instanceof Collection) {
            appendValuePrefix(printWriter, name);
            appendCollection(printWriter, o);
            appendSuffix(printWriter);
        } else if (o instanceof Map) {
            appendValuePrefix(printWriter, name);
            appendMap(printWriter, o);
            appendSuffix(printWriter);
        } else if (o instanceof Enum) {
            appendValuePrefix(printWriter, name);
            appendEnum(printWriter, (Enum) o);
            appendSuffix(printWriter);
        } else {
            appendValuePrefix(printWriter, name);
            appendClass(printWriter, o);
            appendSuffix(printWriter);
        }
    }

    private static void appendEnum(PrintWriter printWriter, Enum o) {
        try {
            if (o == null) {
                printWriter.print("{NULL}");
            } else {
                appendClassPrefix(printWriter, o.getClass().getName());
                printWriter.print("{name=");
                printWriter.print(o.name());
                printWriter.print("}{ordinal=");
                printWriter.print(o.ordinal());
                printWriter.print('}');
                appendSuffix(printWriter);
            }
        } catch (NullPointerException e) {
            throw new NeonException(o.getClass().getName());
        }
    }

    private static void appendArray(PrintWriter printWriter, Object o) throws IllegalAccessException {
        printWriter.print('[');
        String arrayName = o.getClass().getName();
        int len = Array.getLength(o);
        if (arrayName.startsWith("[[")) {
            for (int i = 0; i < len; i++) {
                printWriter.print('{');
                appendArray(printWriter, Array.get(o, i));
                printWriter.print('}');
            }
        } else if (arrayName.endsWith(";")) {
            for (int i = 0; i < len; i++) {
                printWriter.print('{');
                appendClass(printWriter, Array.get(o, i));
                printWriter.print('}');
            }
        } else {
            for (int i = 0; i < len; i++) {
                printWriter.print('{');
                printWriter.print(Array.get(o, i));
                printWriter.print('}');
            }
        }
        //System.out.println(arrayName);
        printWriter.print(']');
    }

    private static void appendClassPrefix(PrintWriter printWriter, String name) {
        printWriter.print("{~");
        final String s = classToCompressedName.get(name);
        printWriter.print(s == null
                          ? name
                          : s);
        printWriter.print('=');
    }

    private static void appendSuffix(PrintWriter printWriter) {
        printWriter.print('}');
    }

    private static void appendString(PrintWriter printWriter, String name, Object value) {
        String string = String.valueOf(value);
//        string = string.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]").replace("{", "\\{").replace("}", "\\}");
        string = escape.matcher(string).replaceAll("\\\\$1");
        appendValue(printWriter, name, string);
        string = null;
    }

    private static void appendValuePrefix(PrintWriter printWriter, String name) {
        if (name == null) {
            printWriter.print('{');
        } else {
            printWriter.print('{');
            printWriter.print(name);
            printWriter.print('=');
        }
    }

    public static char[] longToChar(long l) {
        char[] result = new char[8];
        int i = 7;
        while (i >= 0) {
            result[i] = (char) (l & 0xFF);
            l >>= 8;
            i--;
        }
        return result;
    }

    public static char[] intToChar(int l) {
        char[] result = new char[4];
        int i = 3;
        while (i >= 0) {
            result[i] = (char) (l & 0xFF);
            l >>= 8;
            i--;
        }
        return result;
    }

    public static char[] boolToChar(boolean l) {
        return new char[]{l
                          ? '1'
                          : '0'};
    }

    private static void appendValue(PrintWriter printWriter, String name, Object value) {
//        switch (value.getClass().getName()) {
//            case "int":
//            case "java.lang.Integer":
//                value = new String(intToChar((Integer) value));
//                break;
//            case "long":
//            case "java.lang.Long":
//                value = new String(longToChar((Long) value));
//                break;
//            case "boolean":
//            case "java.lang.Boolean":
//                value = new String(boolToChar((Boolean) value));
//                break;
//
//        }
        if (name == null) {
            printWriter.print('{');
            printWriter.print(value);
            printWriter.print('}');
        } else {
            printWriter.print('{');
            printWriter.print(name);
            printWriter.print('=');
            printWriter.print(value);
            printWriter.print('}');
        }

    }

    private static boolean isPrimitiveOrPrimitiveWrapper(Class<?> type) {
        return (type.isPrimitive() && type != void.class) ||
                type == Double.class || type == Float.class || type == Long.class ||
                type == Integer.class || type == Short.class || type == Character.class ||
                type == Byte.class || type == Boolean.class;
    }
    ////                    if (long.class.equals(field.getDrawType())) {
    ////
    ////                    }
}