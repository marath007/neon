package engineer.number.neon.obselete;

import engineer.number.neon.InvalidNeonException;
import engineer.number.neon.NeonException;

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

import static engineer.number.neon.obselete.FromString.extractPrimitive;
import static engineer.number.neon.obselete.FromString.isPrimitiveOrPrimitiveWrapper;
import static engineer.number.neon.obselete.FromString.secretMapping;
import static engineer.number.neon.obselete.Neon.RECORD_SUPPORTED;
import static engineer.number.neon.obselete.Neon.fieldsOfClass;
import static engineer.number.neon.obselete.Neon.fillFieldList;
import static engineer.number.neon.obselete.Neon.getRecordComponents;
import static engineer.number.neon.obselete.Neon.getType;
import static engineer.number.neon.obselete.Neon.isRecord;

/**
 * assumed that we only save Classes and never a primitive
 * interface ExtendedConstructor for behavior after construction
 * will always build from a zero argument constructor
 */
abstract class FromStringStream {
    static HashMap<String, String> fromTo = new HashMap<>();
    //    public static void valueOf(String string, Object t) {
//        if (string == null || string.equals("")) {
//            return;
//        }
//        try {
//            String s = getMainClassVariables(string, t.getClass().getName());
//            if (s == null) {
//                return;
//            }
//            fillClassFields(t, s);
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        }
//        if (ExtendedConstructor.class.isAssignableFrom(t.getClass())) {
//            ExtendedConstructor extendedConstructor = (ExtendedConstructor) t;
//            try {
//                extendedConstructor.finishConstruction();
//            } catch (InvalidInput invalidInput) {
//                invalidInput.printStackTrace();
//            }
//        }
//    }
    static HashMap<Class, RecordHelper> recordHelperHashMap = new HashMap<>();

    static <T> T fromString(ClassReader sr) throws InvalidNeonException, IOException, NullDataException, OldClassException {
        return (T) fromString(sr, null);
    }
    static <T> T fromString(ClassReader sr, ParameterizedType parameterizedType) throws InvalidNeonException {
        if (sr == null) {
            return null;
        }
        String className = "";
        try {
            className = sr.readClassName();
            return (T) fromString(sr, getClassFromName(className),parameterizedType);
        } catch (OldClassException e) {
            e.printStackTrace();
            try {
                sr.skipOldClass();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return null;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NullDataException e) {
            try {
                sr.skipNullData();
                return null;
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return null;
            }
        } finally {
            try {
                sr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static <T> T _fromString(ClassReader sr) throws InvalidNeonException {
        if (sr == null) {
            return null;
        }
        String className = "";
        try {
            className = sr.readClassName();
            return (T) fromString(sr, getClassFromName(className),false);
        } catch (OldClassException e) {
            e.printStackTrace();
            try {
                sr.skipOldClass();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return null;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NullDataException e) {
            try {
                sr.skipNullData();
                return null;
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return null;
            }
        }
    }

    private static Class<?> getClassFromName(String className) throws OldClassException, InvalidNeonException {
        try {
            return Class.forName(secretMapping(className));
        } catch (ClassNotFoundException e) {
            throw new OldClassException(className);
        }
    }

//    private static <T> T fromString(ClassReader sr, Class<T> classOfT) throws InvalidNeonException, IOException {
//        return fromString(sr, classOfT, false);
//    }

    private static <T> T fromString(ClassReader sr, Class<T> classOfT, boolean noFastToString) throws InvalidNeonException, IOException {
        return fromString(sr, classOfT, null, noFastToString);
    }

    private static <T> T fromString(ClassReader sr, Class<T> classOfT, ParameterizedType parameter) throws InvalidNeonException, IOException {
        return fromString(sr, classOfT, parameter, false);
    }

    static <T> T returnRecordJava15(ClassReader sr, Class<T> tClass) throws InvalidNeonException, IOException, ClassNotFoundException {
//        System.out.println(s);
//        System.out.println("now what");
        try {
            if (!recordHelperHashMap.containsKey(tClass)) {
                recordHelperHashMap.put(tClass, new RecordHelper(tClass));
            }
            return (T) recordHelperHashMap.get(tClass).generate(sr);
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

    private static <T> T fromString(ClassReader sr, Class<T> classOfT, ParameterizedType parameter, boolean noFastToString) throws InvalidNeonException, IOException {
        if (sr == null) {
            return null;
        }
        Constructor finalConstructor = null;
        T t;
        try {
            if (RECORD_SUPPORTED && (boolean) isRecord.invoke(classOfT)) {
                return returnRecordJava15(sr, classOfT);
            } else {
                finalConstructor = classOfT.getDeclaredConstructor();
                finalConstructor.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            throw new InvalidNeonDesign(classOfT.getName() + " doesn't have a zero argument constructor\n# #    or it's not accessible[protected/private]\n# #    or you somehow tried to save an anonymous class\n# #    or you didn't intend to save this class");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new InvalidNeonDesign("wtf tho? " + classOfT.getName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (finalConstructor == null) {
            throw new InvalidNeonDesign(classOfT.getName() + " doesn't have a zero argument constructor");
        }
        try {
            t = (T) finalConstructor.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new InvalidNeonDesign("wtf tho?");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new InvalidNeonDesign("wtf tho?");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new InvalidNeonDesign("wtf tho? " + classOfT.getName());
        }
//        if (ExtendedPreConstructor.class.isAssignableFrom(classOfT)) {
//            ((ExtendedPreConstructor) t).preInit(args);
//        }
        try {
            if (noFastToString) {
                fillClassFields(t, sr, parameter);
            } else {
                String s = null;
                if (Neon.CustomToString.class.isAssignableFrom(classOfT)) {
                    return (T) ((Neon.CustomToString) t).fastValueOf(sr.readFastValue());// TODO: 10/07/20 handle fastvalues
                } else {
                    fillClassFields(t, sr, parameter);
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
        try {
            sr.skip();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    private static <T> void fillClassFields(T t, ClassReader sr) throws InvocationTargetException, InstantiationException, InvalidNeonException {
        fillClassFields(t, sr, null);
    }

    private static <T> void fillClassFields(T t, ClassReader sr, ParameterizedType parameters) throws InvocationTargetException, InstantiationException, InvalidNeonException {
        if (!fieldsOfClass.containsKey(t.getClass().getName())) {
            fillFieldList(t);
        }
        while (true) {
            try {
                String fieldName = sr.readFieldName();
                if (fieldName == null|| fieldName.isEmpty()) {
                    return;
//                    System.out.println("egg");
                }

                Field field = fieldsOfClass.get(t.getClass().getName()).get(fieldName);
                if (field != null) {
                    final Class<?> type = field.getType();
                    if (type.equals(Object.class)) {
                        final Type actualTypeArgument = parameters.getActualTypeArguments()[0];
//                        field.set(t, fromString(value,(Class)((ParameterizedType)actualTypeArgument).getRawType()));
                        if (actualTypeArgument instanceof ParameterizedType) {
                            field.set(t, extractUnknownObject((Class) ((ParameterizedType) actualTypeArgument).getRawType(), actualTypeArgument, (ParameterizedType) actualTypeArgument, sr));
                        } else {
                            field.set(t, extractUnknownObject((Class) actualTypeArgument, actualTypeArgument, null, sr));
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
                        field.set(t, extractUnknownObject(keyClass, genericType, keyParam, sr));
                    }
                } else {
                    sr.skip();
                    sr.skipOldClass();
                }
            } catch (IOException e) {
                return;
            } catch (IllegalAccessException e) {
//                return;
            } catch (NoSuchMethodException e) {
//                return;
            } catch (ClassNotFoundException e) {
//                return;
            }
        }
    }

    private static Object extractUnknownObject(Class c, Type t, ClassReader sr) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, InvalidNeonException, IOException, ClassNotFoundException {
        return extractUnknownObject(c, t, null, sr);
    }

    private static Object extractUnknownObject(Class c, Type t, ParameterizedType parameter, ClassReader sr) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException, ClassNotFoundException, InvalidNeonException {
        String s;
        if (isPrimitiveOrPrimitiveWrapper(c)) {//ok?
            s = sr.readPrimitive();
            return extractPrimitive(c, s);
        } else if (c.equals(String.class)) {// TODO: 10/07/20 string
            return sr.readString();
        } else if (c.isArray()) {
            return fillArray(c, sr);
        } else if (Collection.class.isAssignableFrom(c)) {
            return fillCollection(t, sr);
        } else if (Map.class.isAssignableFrom(c)) {// TODO: 10/07/20 abstractmap
            return fillMap(t, sr);
        } else if (c.isEnum() || c.isAssignableFrom(Enum.class)) {// TODO: 10/07/20 enum
            return getEnum(c, sr);
        } else {
            final String s1;
            try {
                s1 = sr.readClassName();

            return fromString(sr, getClassFromName(s1), parameter);
            } catch (NullDataException | OldClassException e) {
                sr.skipNullData();
                return null;
            }
        }
    }

    private static Object fillCollection(Type t, ClassReader sr) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvalidNeonException, IOException, ClassNotFoundException {
        Collection temp = (Collection) ((Class) ((ParameterizedType) t).getRawType()).getConstructor().newInstance();
        ParameterizedType pt;
        Type keyType;
        Type valueType;
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
        while (sr.isArrayItem()) {
            temp.add(extractUnknownObject(keyClass, keyType, keyParam, sr));
        }
        sr.skip();//skip last }
        return temp;
    }

    private static Object getEnum(Class c, ClassReader sr) throws InvocationTargetException, IOException, InvalidNeonException {
        final String className;
        try {
            className = sr.readClassName();
        } catch (NullDataException e) {
            sr.skipNullData();
            return null;
        }
        sr.readFieldName();
        final String enumStr = sr.readString();
        sr.skip();
        sr.readFieldName();
        sr.readString();
        sr.skip(2);
        try {
            Method valueOf = c.getMethod("valueOf", String.class);
            if (enumStr == null) {
                return null;
            }
            return valueOf.invoke(null, enumStr);
            //enumStr={name=TIME}{ordinal=3} => {TIME}
        } catch (NoSuchMethodException e) {
            try {
                c = Class.forName(className);
                Method valueOf = null;
                valueOf = c.getMethod("valueOf", String.class);
                return valueOf.invoke(null, enumStr);
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

    private static Object fillArray(Class c, ClassReader sr) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException, ClassNotFoundException, InvalidNeonException {//        ArrayList<String> unknown = unWrap(s);
        ArrayList arrayList = new ArrayList();// TODO: 11/07/20 optimize regrowth?
        final Class componentType = c.getComponentType();
        while (sr.isArrayItem()) {
            arrayList.add(extractUnknownObject(componentType, null, sr));
        }
        Object temp;
        temp = Array.newInstance(componentType, arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            Array.set(temp, i, arrayList.get(i));
        }
        sr.skip();//skip last }
        return temp;
    }



    private static Object fillMap(Type t, ClassReader sr) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException, ClassNotFoundException, InvalidNeonException {
        Map temp = (Map) ((Class) ((ParameterizedType) t).getRawType()).getConstructor().newInstance();
        ParameterizedType pt = null;
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
        while (sr.isArrayItem()) {
            final Object key = extractUnknownObject(keyClass, keyType, keyParam, sr);
            if (sr.isArrayItem()) {
                final Object value = extractUnknownObject(valueClass, valueType, valueParam, sr);
                temp.put(key, value);
            } else {
                throw new InvalidNeonException("Hashmap with non-even items");
            }
        }
        sr.skip();//skip last }
        return temp;
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

        public Object generate(ClassReader sr) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvalidNeonException, IOException, ClassNotFoundException {
            Object[] objects = new Object[fieldCount];

            for (int i = 0; i < fieldCount; i++) {
                if (sr.readFieldName() != null) {
                    objects[i] = extractUnknownObject(classes[i], null, sr);
                }else {
                    sr.skip();
                    sr.skipOldClass();
                    return null;
                }

            }
            sr.skip();
            return constructor.newInstance(objects);
        }
    }
}

