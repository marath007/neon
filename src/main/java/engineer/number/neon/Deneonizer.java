package engineer.number.neon;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static engineer.number.neon.Neon.*;


class Deneonizer {
    static ConcurrentHashMap<Class, RecordHelper> recordHelperHashMap = new ConcurrentHashMap<>();
    private transient Fabricator fabricator;
    ThreadLocal<HashMap<String, String>> lazyDecompression = new ThreadLocal<>();

    private NeonConfig neonConfig = new NeonConfig(false);

    private ClassMapper mapper = new ClassMapper();


    public Deneonizer(NeonConfig neonConfig, ClassMapper mapper) {
        this.neonConfig = neonConfig.clone();//to avoid reconfig
        this.mapper = mapper;
    }

    public Deneonizer() {
        this(new NeonConfig(false), new ClassMapper());
    }

    private static Object getEnum(Class c, String named, String ordinal) throws InvocationTargetException {
        try {
            try {
                return Enum.valueOf(c, named);
            } catch (IllegalArgumentException e) {
                Method valueOf = c.getMethod("values");
                return Array.get(valueOf.invoke(null), Integer.parseInt(ordinal));
                //getMainClassVariables(s, c.getName())={name=GAME_PRESTIGE_CLICKS}{ordinal=5}    =>  {GAME_PRESTIGE_CLICKS}
            }
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T deneonize(String s) throws InvalidNeonException, InvalidHeader {
        fabricator = new HardFabricator(s);
        return _deneonize("");
    }

    public <T> T deneonize(String s, Class c) throws InvalidNeonException, InvalidHeader {
        fabricator = new HardFabricator(s);
        return _deneonize(c.getName());
    }

    private void verifyVersion() throws InvalidHeader {
        if (!fabricator.verifyHeader(Neonizer.SerializationVersion.V2)
                && !fabricator.verifyHeader(Neonizer.SerializationVersion.V1)) {
            throw new InvalidHeader("Wrong Version");
        }
    }

    public <T> T deneonize(InputStream s) throws InvalidNeonException, InvalidHeader {
        fabricator = new StreamFabricator(s);
        return _deneonize("");
    }

    public <T> T deneonize(InputStream s, Class c) throws InvalidNeonException, InvalidHeader {
        fabricator = new StreamFabricator(s);
        return _deneonize(c.getName());
    }

    //    public <V> void galvanise(V v, String s) {
//
//    }
    private <T> T _deneonize(String className) throws InvalidNeonException, InvalidHeader {
        verifyVersion();
        lazyDecompression.set(new HashMap<>());
        final String s = readClassName();
        if (className.isEmpty()) {
            className = s;
        }
        try {
            return (T) readClassContent(className);
        } catch (Exception e) {
            throw new InvalidNeonException(e);
        }
    }

    private Object readClassContent(String className) {
        return readClassContent(className, "");
    }

    private Object readClassContent(String className, String fromSource) {
        if (className.startsWith("[")) {
            fabricator.traverseUpToChar('[');
            final String s = fabricator.readTextUntil(':');
            final int size = Integer.parseInt(s);
            switch (className) {
                case "[Ljava.lang.Integer;"://untested
                    final Integer[] ints = new Integer[size];
                    for (int i = 0; i < size; i++) {
                        ints[i] = (Integer) Integer.parseInt(fabricator.readTextUntil(','));
                    }
                    return ints;
                case "[I":
                    final int[] _ints = new int[size];
                    for (int i = 0; i < size; i++) {
                        _ints[i] = Integer.parseInt(fabricator.readTextUntil(','));
                    }
                    return _ints;
                case "[Ljava.lang.Long;":
                    final Long[] longs = new Long[size];
                    for (int i = 0; i < size; i++) {
                        longs[i] = (Long) Long.parseLong(fabricator.readTextUntil(','));
                    }
                    return longs;
                case "[J":
                    final long[] _longs = new long[size];
                    for (int i = 0; i < size; i++) {
                        _longs[i] = Long.parseLong(fabricator.readTextUntil(','));
                    }
                    return _longs;
                case "[Ljava.lang.Double;":
                    final Double[] doubles = new Double[size];
                    for (int i = 0; i < size; i++) {
                        doubles[i] = (Double) Double.parseDouble(fabricator.readTextUntil(','));
                    }
                    return doubles;
                case "[D":
                    final double[] _doubles = new double[size];
                    for (int i = 0; i < size; i++) {
                        _doubles[i] = Double.parseDouble(fabricator.readTextUntil(','));
                    }
                    return _doubles;
                case "[Ljava.lang.Float;"://untested
                    final Float[] floats = new Float[size];
                    for (int i = 0; i < size; i++) {
                        floats[i] = (Float) Float.parseFloat(fabricator.readTextUntil(','));
                    }
                    return floats;
                case "[F":
                    final float[] _floats = new float[size];
                    for (int i = 0; i < size; i++) {
                        _floats[i] = Float.parseFloat(fabricator.readTextUntil(','));
                    }
                    return _floats;
                case "[Ljava.lang.Boolean;"://untested
                    final Boolean[] booleans = new Boolean[size];
                    for (int i = 0; i < size; i++) {
                        booleans[i] = (Boolean) Boolean.parseBoolean(fabricator.readTextUntil(','));
                    }
                    return booleans;
                case "[Z":
                    final boolean[] _booleans = new boolean[size];
                    for (int i = 0; i < size; i++) {
                        _booleans[i] = Boolean.parseBoolean(fabricator.readTextUntil(','));
                    }
                    return _booleans;
                case "[Ljava.lang.Character;"://optimizable//untested
                    final Character[] characters = new Character[size];
                    String charString = fabricator.readBoundString();
                    final char[] chars = charString.toCharArray();
                    for (int i = 0; i < size; i++) {
                        characters[i] = (Character) chars[i];
                    }
                    return characters;
                case "[C"://optimizable//untested
                    return fabricator.readBoundString().toCharArray();
                case "[Ljava.lang.Byte;"://optimizable//untested
                    final Byte[] bytes = new Byte[size];
                    final byte[] bytes1 = fabricator.readXByte(size);
                    for (int i = 0; i < size; i++) {
                        bytes[i] = (Byte) bytes1[i];
                    }
                    return bytes;
                case "[B":
                    return fabricator.readXByte(size);
                case "[Ljava.lang.String;"://untested
                    final String[] strings = new String[size];
                    for (int i = 0; i < size; i++) {
                        strings[i] = fabricator.readBoundString();
                    }
                    return strings;
                default:
                    try {
                        String itemName;
                        if (className.startsWith("[[")) {
                            itemName = className.substring(1);
                        } else {
                            itemName = className.substring(2, className.length() - 1);
                        }
                        final Object o = Array.newInstance(Class.forName(itemName), size);
                        for (int i = 0; i < size; i++) {
                            Array.set(o, i, readClassContent(readClassName()));
                            fabricator.traverseUpToChar(',');
                        }
//                        fabricator.traverseUpToChar(',');
                        return o;
                    } catch (ClassNotFoundException e) {
                        throw new NeonException(e);
                    }
            }
        } else {
            switch (className) {
                case "java.lang.Integer":
                case "int":
                    String s = fabricator.readTextUntil('}');
                    try {
                        return (Integer) Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        return (Integer) (int) Double.parseDouble(s);
                    }
                case "java.lang.Long":
                case "long":
                    s = fabricator.readTextUntil('}');
                    try {
                        return (Long) Long.parseLong(s);
                    } catch (NumberFormatException ignored) {
                        return (Long) (long)Double.parseDouble(s);
                    }
                case "java.lang.Double":
                case "double":
                    return (Double) Double.parseDouble(fabricator.readTextUntil('}'));
                case "java.lang.Float":
                case "float":
                    return (Float) Float.parseFloat(fabricator.readTextUntil('}'));
                case "java.lang.Byte":
                case "byte":
                    return (Byte) fabricator.readXByte(1)[0];
                case "java.lang.Boolean":
                case "boolean":
                    return (Boolean) Boolean.parseBoolean(fabricator.readTextUntil('}'));
                case "java.lang.Character":
                case "char":
                    return (Character) fabricator.readCharBefore('}');
                case "java.lang.String":
                    return fabricator.readBoundString();
                default:
                    try {
                        Class<?> aClass = Class.forName(className);
                        Object oNew;
                        if (Neon.ManualSerialization.class.isAssignableFrom(aClass)) {
                            oNew = aClass.getConstructor().newInstance();
                            fabricator.traverseUpToChar('{');
                            oNew = ((Neon.ManualSerialization) oNew).fastValueOf(fabricator);
                        } else if (Collection.class.isAssignableFrom(aClass)) {
                            fabricator.traverseUpToChar('[');
                            final Integer size = Integer.valueOf(fabricator.readTextUntil(':'));
                            final Collection o2 = (Collection) aClass.getConstructor(Integer.TYPE).newInstance(size);
                            for (int i = 0; i < size; i++) {
                                o2.add(readClassContent(readClassName()));
                            }
                            oNew = o2;
                        } else if (Map.class.isAssignableFrom(aClass)) {
                            fabricator.traverseUpToChar('[');
                            final Integer size = Integer.valueOf(fabricator.readTextUntil(':'));
                            final Map o2 = (Map) aClass.getConstructor(Integer.TYPE).newInstance(size);
                            for (int i = 0; i < size; i++) {
                                o2.put(readClassContent(readClassName()), readClassContent(readClassName()));
                            }
                            oNew = o2;
                        } else if (aClass.isEnum()) {
                            return getEnum(aClass, fabricator.readTextFromTo('{', ','), fabricator.readTextUntil('}'));
//                            openIndent();
//                            deConstructor.append(((Enum<?>) o).name());
//                            deConstructor.append(',');
//                            deConstructor.append(((Enum<?>) o).ordinal());
//                            closeIndent();
                        } else if (RECORD_SUPPORTED && (boolean) isRecord.invoke(aClass)) {
                            if (!recordHelperHashMap.containsKey(aClass)) {
                                recordHelperHashMap.put(aClass, new RecordHelper(aClass));
                            }
                            return recordHelperHashMap.get(aClass).generate(this);
                        } else {
                            if (!fromSource.isEmpty()) {
                                return readClassContent(fromSource, "");
                            }
                            oNew = createCustomClass(aClass);
                        }
//                        if (oNew instanceof Neon.PostInit) {
//                            ((Neon.PostInit) oNew).postInit();
//                        }
                        return oNew;
                    } catch (Exception e) {
//                        e.printStackTrace();
                        e.printStackTrace();
                        throw new NeonException(e);
                    }
            }
        }
    }

    private Object createCustomClass(Class<?> aClass) {
        try {
            final Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            Object o = declaredConstructor.newInstance();
            final String name = o.getClass().getName();
            if (!fieldsOfClass.containsKey(name)) {
                fillFieldList(o);
            }
            final Map<String, Field> fields = fieldsOfClass.get(name);
            String fieldName;
            while (!(fieldName = fabricator.readTextFromTo(',', '=')).equals("~")) {
                final Field field = fields.get(fieldName);
                if (field == null) {
                    fabricator.traverseUpToChar('{');
                    fabricator.closeIndent((s) -> {
                        if (!s.startsWith("~")) {
                            final String[] split = s.split(":");
                            s = split[0];
                            lazyDecompression.get().put("~" + Integer.parseInt(split[1]), name);
                        }
                    });
                } else {
                    field.set(o, readClassContent(field.getType().getName(), readClassName()));
                }
            }
            if (o instanceof Neon.PostInit) {
                ((Neon.PostInit) o).postInit();
            }
            return o;
        } catch (Exception e) {
            throw new NeonException(e);
        }
    }

    private String readClassName() {
        final HashMap<String, String> stringStringHashMap = lazyDecompression.get();
        fabricator.traverseUpToChar('~');
        String name = fabricator.readTextUntil('{');
        if (!name.startsWith("~")) {
            final String[] split = name.split(":");
            name = split[0];
            if (split.length == 1) {
                new InvalidNeonException("split.length=1").printStackTrace();
                throw new NeonException("DEATH");
            }
            stringStringHashMap.put("~" + Integer.parseInt(split[1]), name);
        } else {
            return stringStringHashMap.get(name);
        }
        return mapper.decompress(name);
    }

    public <T> T deneonizeInto(T t, String s) {
        return null;
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
            constructor.setAccessible(true);
//            getMainClassVariables(s, tClass.getName());
        }

        public Object generate(Deneonizer deneonizer) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvalidNeonException, IOException, ClassNotFoundException {
            Object[] objects = new Object[fieldCount];
            for (int i = 0; i < fieldCount; i++) {
                deneonizer.fabricator.traverseUpToChar(',');
                deneonizer.fabricator.traverseUpToChar('{');
                objects[i] = deneonizer.readClassContent(classes[i].getName(), deneonizer.readClassName());
                deneonizer.fabricator.traverseUpToChar('}');
            }
            deneonizer.fabricator.traverseUpToChar('~');
            deneonizer.fabricator.traverseUpToChar('=');
            try {
                return constructor.newInstance(objects);
            } catch (Exception ignored) {
                throw ignored;
            }
        }
    }
}
