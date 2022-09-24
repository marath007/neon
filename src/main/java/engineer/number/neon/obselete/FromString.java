package engineer.number.neon.obselete;

import static engineer.number.neon.obselete.Neon.CLASS_NAME_MIN_LENGTH;
import static engineer.number.neon.obselete.Neon.RECORD_SUPPORTED;
import static engineer.number.neon.obselete.Neon.compressedNameToClass;
import static engineer.number.neon.obselete.Neon.fieldsOfClass;
import static engineer.number.neon.obselete.Neon.getRecordComponents;
import static engineer.number.neon.obselete.Neon.getType;
import static engineer.number.neon.obselete.Neon.isRecord;
import static engineer.number.neon.obselete.ToString.unescape;

import engineer.number.neon.InvalidNeonException;
import engineer.number.neon.NeonException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * assumed that we only save Classes and never a primitive
 * interface ExtendedConstructor for behavior after construction
 * will always build from a zero argument constructor
 */

abstract class FromString {
    static HashMap<Class, RecordHelper> recordHelperHashMap = new HashMap<Class, RecordHelper>();

    //    static MethodHandle isRecord = null;
    static Object extractPrimitive(Class c, String s) throws InvalidNeonException {
        switch (c.getName()) {
            case "int":
            case "java.lang.Integer":
                return Integer.valueOf(s);
            case "long":
            case "java.lang.Long":
                return Long.valueOf(s);
            case "double":
            case "java.lang.Double":
                return Double.valueOf(s);
            case "float":
            case "java.lang.Float":
                return Float.valueOf(s);
            case "boolean":
            case "java.lang.Boolean":
                return Boolean.valueOf(s);
            case "char":
            case "java.lang.Character":
                return s.toCharArray()[0];
        }
//        if (long.class.equals(c) || Long.class.equals(c)) {
//            return Long.valueOf(s);
//        } else if (int.class.equals(c) || Integer.class.equals(c)) {
//            return Integer.valueOf(s);
//        } else if (double.class.equals(c) || Double.class.equals(c)) {
//            return Double.valueOf(s);
//        } else if (float.class.equals(c) || Float.class.equals(c)) {
//            return Float.valueOf(s);
//        } else if (boolean.class.equals(c) || Boolean.class.equals(c)) {
//            return Boolean.valueOf(s);
//            Character
//        }
        throw new InvalidNeonException(c.getName() + " isnt a supported primitive");
    }

    static void valueOf(String string, Object t) {
        if (string == null || string.isEmpty()) {
            return;
        }
        try {
            String s = getMainClassVariables(string, t.getClass().getName());
            if (s == null) {
                return;
            }
            fillClassFields(t, s);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvalidNeonException e) {
            e.printStackTrace();// TODO: 04/11/20 rethrow?
        }
        if (Neon.ExtendedConstructor.class.isAssignableFrom(t.getClass())) {
            Neon.ExtendedConstructor extendedConstructor = (Neon.ExtendedConstructor) t;
            try {
                extendedConstructor.finishConstruction();
            } catch (NeonException invalidInput) {
                invalidInput.printStackTrace();
            }
        }
    }

    static ArrayList<String> unWrap(String s) {
        int bCount = 0;
        int from = 0;
        int to = 0;
        boolean escapeNext = false;
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            if (!escapeNext) {
                if (s.charAt(i) == '{') {
                    bCount++;
                    if (bCount == 1) {
                        from = i;
                    }
                } else if (s.charAt(i) == '}') {
                    bCount--;
                    if (bCount == 0) {
                        to = i;
                        arrayList.add(s.substring(from + 1, to));
                    }
                } else if (s.charAt(i) == '\\') {
                    escapeNext = true;
                }
            } else {
                escapeNext = false;
            }
        }
        return arrayList;
    }

    static <T> T fromString(String s) throws InvalidNeonException {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            ArrayList<String> unknown = unWrap(s);
            String[] strs = unknown.get(0).split("=", 2);
            if (strs[0].startsWith("~")) {
                String className = strs[0].replace("~", "");
                try {
                    return (T) fromString(s, Class.forName(secretMapping(className)));
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found : " + secretMapping(className));
                    return null;
                }
            } else {
                return null;
                //            throw new CustomError("reached this places without being a class wtf? here is some info then\n" + "extractUnknownObject() called with s = [" + s + "]");
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            throw new InvalidNeonException(s);
        }
    }

    static <T> T fromString(File f) throws InvalidNeonException {
        String s = "";
        try (FileInputStream fileInputStream = new FileInputStream(f)) {
            byte[] bytes = new byte[(int) f.length()];
            fileInputStream.read(bytes);
            s = new String(bytes);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fromString(s);
    }

    static <T> T fromString(String string, Class<T> classOfT) throws InvalidNeonException {
        return fromString(string, classOfT, null, false);
    }

    static <T> T fromString(String string, Class<T> classOfT, ParameterizedType parameter) throws InvalidNeonException {
        return fromString(string, classOfT, parameter, false);
    }

    static <T> T returnRecordJava15(String s, Class<T> tClass) throws InvalidNeonException {
//        System.out.println(s);
//        System.out.println("now what");
        try {
            if (!recordHelperHashMap.containsKey(tClass)) {
                recordHelperHashMap.put(tClass, new RecordHelper(tClass));
            }
            return (T) recordHelperHashMap.get(tClass).generate(s);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    static <T> T fromString(String string, Class<T> classOfT, boolean compat) throws InvalidNeonException {
        return fromString(string, classOfT, null, compat);
    }

    static <T> T fromString(String string, Class<T> classOfT, ParameterizedType parameters, boolean compat) throws InvalidNeonException {
        if (string == null || string.isEmpty()) {
            return null;
        }
//        Constructor[] constructors = classOfT.getConstructors();
        Constructor finalConstructor = null;
//        for (int i = 0; i < constructors.length; i++) {
//            if (constructors[i].getParameterCount() == 0) {
//                finalConstructor = constructors[i];
//                break;
//            }
//        }
        T t;
        try {
//            classOfT.co
//            classOfT.getDeclaredConstructor().setAccessible(true);
            if (RECORD_SUPPORTED && (boolean) isRecord.invoke(classOfT)) {
                return returnRecordJava15(string, classOfT);
            } else {
                finalConstructor = classOfT.getDeclaredConstructor();
                finalConstructor.setAccessible(true);
            }
//            finalConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new InvalidNeonException(classOfT.getName() + " doesn't have a zero argument constructor\n# #    or it's not accessible[protected/private]\n# #    or you somehow tried to save an anonymous class\n# #    or you didn't intend to save this class");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (finalConstructor == null) {
            throw new InvalidNeonException(classOfT.getName() + " doesn't have a zero argument constructor");
        }
        try {
            t = (T) finalConstructor.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new InvalidNeonException("wtf tho?");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new InvalidNeonException("wtf tho?");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new InvalidNeonException("wtf tho? " + classOfT.getName());
        }
//        if (ExtendedPreConstructor.class.isAssignableFrom(classOfT)) {
//            ((ExtendedPreConstructor) t).preInit(args);
//        }
        try {
            if (compat) {
                fillClassFields(t, string, parameters);
            } else {
                String s = getMainClassVariables(string, classOfT.getName());
                if (s == null) {
                    throw new InvalidNeonException(string);
                }
                if (Neon.CustomToString.class.isAssignableFrom(classOfT)) {
                    return (T) ((Neon.CustomToString) t).fastValueOf(s);
                } else {
                    fillClassFields(t, s, parameters);
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        if (Neon.ExtendedConstructor.class.isAssignableFrom(classOfT)) {
            Neon.ExtendedConstructor extendedConstructor = (Neon.ExtendedConstructor) t;
            try {
                extendedConstructor.finishConstruction();
            } catch (NeonException invalidInput) {
                invalidInput.printStackTrace();
                return null;
            }
        }
        return t;
    }

    private static <T> void fillClassFields(T t, String s) throws InvocationTargetException, InstantiationException, InvalidNeonException {
        fillClassFields(t, s, null);
    }

    private static <T> void fillClassFields(T t, String s, ParameterizedType parameters) throws InvocationTargetException, InstantiationException, InvalidNeonException {
        ArrayList<String> fields = unWrap(s);
        //System.out.println(s);
        if (!fieldsOfClass.containsKey(t.getClass().getName())) {
            Neon.fillFieldList(t);
        }
        for (int i = 0; i < fields.size(); i++) {
            String[] parts = fields.get(i).split("=", 2);
            String fieldName = parts[0];
            String value = parts[1];
            Field field = fieldsOfClass.get(t.getClass().getName()).get(fieldName);
//            try {
//                field = t.getClass().getDeclaredField(fieldName);
//            } catch (NoSuchFieldException e) {
//                Class sup = t.getClass().getSuperclass();
//                while (sup != null) {
//                    try {
//                        field = sup.getDeclaredField(fieldName);
//                        break;
//                    } catch (NoSuchFieldException ex) {
//                        // System.err.println("[NORMAL] NoSuchFieldException in fillClassFields : " +fieldName );
//                        //  ex.printStackTrace();
//                    }
//                    sup = sup.getSuperclass();
//                }
//                //e.printStackTrace();
//            }
            try {
                if (field != null) {
//                    int modifiers = field.getModifiers();
//                    if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
//                        field.setAccessible(true);TypeVariableImpl
                    final Class<?> type = field.getType();
                    if (type.equals(Object.class)) {
                        final Type actualTypeArgument = parameters.getActualTypeArguments()[0];
//                        field.set(t, fromString(value,(Class)((ParameterizedType)actualTypeArgument).getRawType()));
                        if (actualTypeArgument instanceof ParameterizedType) {
                            field.set(t, extractUnknownObject((Class) ((ParameterizedType) actualTypeArgument).getRawType(), actualTypeArgument, (ParameterizedType) actualTypeArgument, value));
                        } else {
                            field.set(t, extractUnknownObject((Class) actualTypeArgument, actualTypeArgument, null, value));
                        }
                    } else {
                        final Type genericType = field.getGenericType();
                        Class keyClass;
                        ParameterizedType keyParam;
                        if (genericType instanceof ParameterizedType) {
                            keyClass = (Class) ((ParameterizedType) genericType).getRawType();
//            keyType = keyParam.getRawType();
                            keyParam = (ParameterizedType) genericType;
                        } else {
                            keyClass = (Class) genericType;
                            keyParam = null;
                        }
                        field.set(t, extractUnknownObject(keyClass, genericType, keyParam, value));
                    }
//                    }
                }
            } catch (IllegalAccessException e) {
                //    e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private static Object extractUnknownObject(Class c, Type t, ParameterizedType parameter, String s) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidNeonException {
        if (s.equals("{NULL}")) {
            return null;
        } else {
            if (isPrimitiveOrPrimitiveWrapper(c)) {
                return extractPrimitive(c, s);
            } else if (c.equals(String.class)) {
//                return s.replace("\\[", "[").replace("\\]", "]").replace("\\}", "}").replace("\\{", "{").replace("\\\\", "\\");
                return unescape.matcher(s).replaceAll("");
            } else if (c.isArray()) {
                return fillArray(c, s);
            } else if (Collection.class.isAssignableFrom(c)) {
                return fillCollection(t, s);
            } else if (Map.class.isAssignableFrom(c)) {
                return fillMap(t, s);
            } else if (c.isEnum() || c.isAssignableFrom(Enum.class)) {
                return getEnum(c, s);
            } else {
//                return fromString(s, c, parameter);
                ArrayList<String> unknown = unWrap(s);
                String[] strs = unknown.get(0).split("=", 2);
                if (strs[0].startsWith("~")) {
                    String className = strs[0].replace("~", "");
                    try {
                        return fromString(s, Class.forName(secretMapping(className)), parameter);
                    } catch (ClassNotFoundException e) {
                        System.err.println("Class not found : " + secretMapping(className));
                        return null;
                    } catch (InvalidNeonException e) {
                        System.err.println("Class corrupted" + s);
                        return null;
                    }
                } else {
                    throw new InvalidNeonException("reached this places without being a class wtf? here is some info then\n" + "extractUnknownObject() called with: c = [" + c + "], t = [" + t + "], s = [" + s + "]");
                }
            }
        }
    }

    private static Object fillCollection(Type t, String s) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvalidNeonException {
        ArrayList<String> unknown = unWrap(s);
        Collection temp = (Collection) ((Class) ((ParameterizedType) t).getRawType()).getConstructor(Integer.TYPE).newInstance(unknown.size());
        ParameterizedType pt;
        Type keyType;
        Class keyClass;
        pt = (ParameterizedType) t;
        keyType = pt.getActualTypeArguments()[0];
        ParameterizedType keyParam = null;
        if (keyType instanceof ParameterizedType) {
            keyClass = (Class) ((ParameterizedType) keyType).getRawType();
//            keyType = keyParam.getRawType();
            keyParam = (ParameterizedType) keyType;
        } else {
            keyClass = (Class) keyType;
        }
        for (int i = 0; i < unknown.size(); i++) {
            temp.add(extractUnknownObject(keyClass, keyType, keyParam, unknown.get(i)));
        }
        return temp;
    }

    private static Object extractUnknownObject(Class c, Type t, String s) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidNeonException {
        return extractUnknownObject(c, t, null, s);
    }

    private static Object getEnum(Class c, String s) throws InvocationTargetException, InvalidNeonException {
        try {
            Method valueOf = c.getMethod("valueOf", String.class);
            String enumStr = getMainClassVariables(s, c.getName());
            if (enumStr == null) {
                return null;
            }
            return valueOf.invoke(null, unWrap(enumStr).get(0).split("=")[1]);
            //enumStr={name=TIME}{ordinal=3} => {TIME}
        } catch (NoSuchMethodException e) {
            String[] strs = unWrap(s).get(0).split("=", 2);
            String className;
            className = strs[0].replace("~", "");
            try {
                c = Class.forName(className);
                Method valueOf = null;
                valueOf = c.getMethod("valueOf", String.class);
                return valueOf.invoke(null, unWrap(getMainClassVariables(s, c.getName())).get(0).split("=")[1]);
                //getMainClassVariables(s, c.getName())={name=GAME_PRESTIGE_CLICKS}{ordinal=5}    =>  {GAME_PRESTIGE_CLICKS}
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                throw new InvalidNeonException("an enum that is not public? wtf? here is class name: " + c.getName());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        } catch (IllegalAccessException e) {
            throw new InvalidNeonException("an enum that is not public? wtf? here is class name: " + c.getName());
        }
        return null;
    }

    /**
     * @param s all class generation pass by this method
     * @return
     */
    static String secretMapping(String s) throws InvalidNeonException {
        String[] ss = s.split("[.]");
        if (ss[ss.length - 1].length() < CLASS_NAME_MIN_LENGTH) {
            throw new InvalidNeonException("What kind of dumbass name their class " + s);
        }
        String name;
        return (name = compressedNameToClass.get(s)) == null
               ? s
               : name;
    }

    private static Object fillArray(Class c, String s) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, InvalidNeonException {
        ArrayList<String> unknown = unWrap(s);
        Object temp = Array.newInstance(c.getComponentType(), unknown.size());
        for (int i = 0; i < unknown.size(); i++) {
            Array.set(temp, i, extractUnknownObject(c.getComponentType(), null, unknown.get(i)));
        }
        return temp;
    }
//    private static Object fillAbstractList(Type t, String s) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, InvalidNeonException {
//        ArrayList<String> unknown = unWrap(s);
//        AbstractList temp = (AbstractList) ((Class) ((ParameterizedType) t).getRawType()).getConstructor(Integer.TYPE).newInstance(unknown.size());
//        ParameterizedType pt;
//        Type keyType;
//        Type valueType;
//        Class keyClass;
//        pt = (ParameterizedType) t;
//        keyType = pt.getActualTypeArguments()[0];
//        ParameterizedType keyParam = null;
//        if (keyType instanceof ParameterizedType) {
//            keyClass = (Class) ((ParameterizedType) keyType).getRawType();
////            keyType = keyParam.getRawType();
//            keyParam = (ParameterizedType) keyType;
//        } else {
//            keyClass = (Class) keyType;
//        }
//        for (int i = 0; i < unknown.size(); i++) {
//            temp.add(extractUnknownObject(keyClass, keyType, keyParam, unknown.get(i)));
//        }
//        return temp;
//    }

    private static Object fillMap(Type t, String s) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, InvalidNeonException {
        ArrayList<String> unknown = unWrap(s);
        Map temp = (Map) ((Class) ((ParameterizedType) t).getRawType()).getConstructor(Integer.TYPE).newInstance(unknown.size() / 2);
        ParameterizedType pt;
        Type keyType;
        Type valueType;
        Class keyClass;
        Class valueClass;
        pt = (ParameterizedType) t;
        keyType = pt.getActualTypeArguments()[0];
        valueType = pt.getActualTypeArguments()[1];
        ParameterizedType keyParam = null;
        ParameterizedType valueParam = null;
        if (keyType instanceof ParameterizedType) {
            keyClass = (Class) ((ParameterizedType) keyType).getRawType();
//            keyType = keyParam.getRawType();
            keyParam = (ParameterizedType) keyType;
        } else {
            keyClass = (Class) keyType;
        }
        if (valueType instanceof ParameterizedType) {
            valueClass = (Class) ((ParameterizedType) valueType).getRawType();
//            keyType = keyParam.getRawType();
            valueParam = (ParameterizedType) valueType;
        } else {
            valueClass = (Class) valueType;
        }
        for (int i = 0; i < unknown.size(); i += 2) {
            temp.put(extractUnknownObject(keyClass, keyType, keyParam, unknown.get(i)), extractUnknownObject(valueClass, valueType, valueParam, unknown.get(i + 1)));
        }
        return temp;
    }

    private static String getMainClassVariables(String s, String className) throws InvalidNeonException {
        String[] strs = s.split("=", 2);
        if (!secretMapping(strs[0].substring(2)).equals(className)) {
            return null;
        }
        s = strs[1];
        s = s.substring(0, s.length() - 1);
        return s;
    }

    static boolean isPrimitiveOrPrimitiveWrapper(Class<?> type) {
        return (type.isPrimitive() && type != void.class) ||
                type == Double.class || type == Float.class || type == Long.class ||
                type == Integer.class || type == Short.class || type == Character.class ||
                type == Byte.class || type == Boolean.class;
    }

    static class RecordHelper {

        private final int fieldCount;
        private final Class[] classes;
        //final int[] remapper;
        private final Constructor constructor;
        private final Class tClass;

        public RecordHelper(Class tClass) throws InvocationTargetException, IllegalAccessException {
            this.tClass = tClass;
            final Object[] components = (Object[]) getRecordComponents.invoke(tClass);
            classes = new Class[components.length];
            fieldCount = components.length;
            for (int i = 0; i < components.length; i++) {
                classes[i] = (Class) getType.invoke(components[i]);
            }
            constructor = tClass.getDeclaredConstructors()[0];
//            getMainClassVariables(s, tClass.getName());
        }

        public Object generate(String s) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvalidNeonException {
            Object[] objects = new Object[fieldCount];
            final ArrayList<String> data = unWrap(getMainClassVariables(s, tClass.getName()));
            for (int i = 0; i < fieldCount; i++) {
                objects[i] = extractUnknownObject(classes[i], null, data.get(i).split("=", 2)[1]);
            }
            try {
                return constructor.newInstance(objects);
            } catch (Exception ignored) {
                throw ignored;
            }
        }
    }
}

