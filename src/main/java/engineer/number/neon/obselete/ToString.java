package engineer.number.neon.obselete;

import engineer.number.neon.NeonException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static engineer.number.neon.obselete.Neon.classToCompressedName;
import static engineer.number.neon.obselete.Neon.fieldsOfClass;
import static engineer.number.neon.obselete.Neon.fillFieldList;
//import library.misc.StringBuilder;

/**
 * convert any classes to a usable string
 * {}[] are escaped from strings
 * ~ distinguish class name from field name
 * <p>
 * cant handle array of strings
 * cant handle array set to null
 */
abstract class ToString {
    static transient Pattern escape = Pattern.compile("([\\[\\]{}\\\\])");
    static transient Pattern unescape = Pattern.compile("\\\\(?!\\\\)");

    static void toFile(Object o, File f) {
        String s = ToString.toString(o);
        final byte[] bytes = s.getBytes();
        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            fileOutputStream.write(bytes);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static String toString(Object o) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
//            appendUnknownObject(stringBuilder,o.getClass(),o,null);
            appendClass(stringBuilder, o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static void appendClass(StringBuilder stringBuilder, Object o) throws IllegalAccessException {
        if (o == null) {
            stringBuilder.append("{NULL}");
        } else {
            appendClassPrefix(stringBuilder, o.getClass().getName());
            if (!fieldsOfClass.containsKey(o.getClass().getName())) {
                fillFieldList(o);
            }
            if (Neon.CustomToString.class.isAssignableFrom(o.getClass())) {
                ((Neon.CustomToString) o).fastToString(stringBuilder);
            } else {
                appendFields(stringBuilder, o, fieldsOfClass.get(o.getClass().getName()).values());
            }
            appendSuffix(stringBuilder);
        }
    }

    private static void appendEnum(StringBuilder stringBuilder, Enum o) throws IllegalAccessException {
        try {
            if (o == null) {
                stringBuilder.append("{NULL}");
            } else {
                appendClassPrefix(stringBuilder, o.getClass().getName());
                stringBuilder.append("{name=").append(o.name()).append("}{ordinal=").append(o.ordinal()).append('}');
                appendSuffix(stringBuilder);
            }
        } catch (NullPointerException e) {
            throw new NeonException(o.getClass().getName());
        }
    }

    private static void appendFields(StringBuilder stringBuilder, Object o, Collection<Field> fields) throws IllegalAccessException {
        for (Field field : fields) {
            final Object o1 = field.get(o);
            if (o1 != null) {
                appendUnknownObject(stringBuilder, o1.getClass(), o1, field.getName());
            } else {
                appendUnknownObject(stringBuilder, field.getType(), field.get(o), field.getName());
            }
        }
//        for (int i = 0; i < fields.size(); i++) {
//
//            appendUnknownObject(stringBuilder, fields.getType(), field.get(o), field.getName());
//        }
    }

    private static void appendCollection(StringBuilder stringBuilder, Collection abstractList) throws IllegalAccessException {
        stringBuilder.append('[');
        for (Object o : abstractList) {
            appendUnknownObject(stringBuilder, o.getClass(), o, null);
        }
//        for (int i = 0; i < abstractList.size(); i++) {
//            appendUnknownObject(stringBuilder, abstractList.get(i).getClass(), abstractList.get(i), null);
//        }
        stringBuilder.append(']');
    }

    private static void appendMap(StringBuilder stringBuilder, Map abstractMap) throws IllegalAccessException {
        stringBuilder.append('[');
        Iterator key = abstractMap.keySet().iterator();
        Iterator value = abstractMap.values().iterator();
        for (int i = 0; i < abstractMap.size(); i++) {
            Object tKey = key.next();
            Object tValue = value.next();
            if (tKey != null && tValue != null) {
                appendUnknownObject(stringBuilder, tKey.getClass(), tKey, null);
                appendUnknownObject(stringBuilder, tValue.getClass(), tValue, null);
            } else {
//                System.out.println("egg"); // TODO: 21/02/20 fix me?
            }
        }
        stringBuilder.append(']');
    }

    /**
     * @param name is nullable
     */
    private static void appendUnknownObject(StringBuilder stringBuilder, Class c, Object o, String name) throws IllegalAccessException {
        if (o == null) {
            return;
        }
        if (isPrimitiveOrPrimitiveWrapper(c)) {
            appendValue(stringBuilder, name, o);
        } else if (c.equals(String.class)) {
            appendString(stringBuilder, name, o);
        } else if (c.isArray()) {
            appendValuePrefix(stringBuilder, name);
            appendArray(stringBuilder, o);
            appendSuffix(stringBuilder);
        } else if (o instanceof Collection) {
            appendValuePrefix(stringBuilder, name);
            appendCollection(stringBuilder, (Collection) o);
            appendSuffix(stringBuilder);
        } else if (o instanceof Map) {
            appendValuePrefix(stringBuilder, name);
            appendMap(stringBuilder, (Map) o);
            appendSuffix(stringBuilder);
        } else if (o instanceof Enum) {
            appendValuePrefix(stringBuilder, name);
            appendEnum(stringBuilder, (Enum) o);
            appendSuffix(stringBuilder);
        } else {
            appendValuePrefix(stringBuilder, name);
            appendClass(stringBuilder, o);
            appendSuffix(stringBuilder);
        }
    }

    private static void appendArray(StringBuilder stringBuilder, Object o) throws IllegalAccessException {
        stringBuilder.append('[');
        String arrayName = o.getClass().getName();
        int len = Array.getLength(o);
        if (arrayName.startsWith("[[")) {
            for (int i = 0; i < len; i++) {
                stringBuilder.append('{');
                appendArray(stringBuilder, Array.get(o, i));
                stringBuilder.append('}');
            }
        } else if (arrayName.endsWith(";")) {
            for (int i = 0; i < len; i++) {
                stringBuilder.append('{');
                appendClass(stringBuilder, Array.get(o, i));
                stringBuilder.append('}');
            }
        } else {
            for (int i = 0; i < len; i++) {
                stringBuilder.append('{');
                stringBuilder.append(Array.get(o, i));
                stringBuilder.append('}');
            }
        }
        //System.out.println(arrayName);
        stringBuilder.append(']');
    }

    private static void appendClassPrefix(StringBuilder stringBuilder, String name) {
        stringBuilder.append("{~");
        final String s = classToCompressedName.get(name);
        stringBuilder.append(s == null
                             ? name
                             : s);
        stringBuilder.append('=');
    }

    private static void appendSuffix(StringBuilder stringBuilder) {
        stringBuilder.append('}');
    }

    private static void appendString(StringBuilder stringBuilder, String name, Object value) {
        String string = String.valueOf(value);
//        string = string.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]").replace("{", "\\{").replace("}", "\\}");
        string = escape.matcher(string).replaceAll("\\\\$1");
        appendValue(stringBuilder, name, string);
        string = null;
    }

    private static void appendValuePrefix(StringBuilder stringBuilder, String name) {
        if (name == null) {
            stringBuilder.append('{');
        } else {
            stringBuilder.append('{');
            stringBuilder.append(name);
            stringBuilder.append('=');
        }
    }

    private static void appendValue(StringBuilder stringBuilder, String name, Object value) {
        if (name == null) {
            stringBuilder.append('{');
            stringBuilder.append(value);
            stringBuilder.append('}');
        } else {
            stringBuilder.append('{');
            stringBuilder.append(name);
            stringBuilder.append('=');
            stringBuilder.append(value);
            stringBuilder.append('}');
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