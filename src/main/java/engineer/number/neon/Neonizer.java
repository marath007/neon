package engineer.number.neon;

import engineer.number.neon.interfaces.Deconstructor;
import engineer.number.neon.interfaces.HardDeconstructor;
import engineer.number.neon.interfaces.StreamDeconstructor;

import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import static engineer.number.neon.Neon.fieldsOfClass;
import static engineer.number.neon.Neon.fillFieldList;

/**
 * convert any classes to a usable string
 * {}[] are escaped from strings
 * ~ distinguish class name from field name
 * <p>
 * cant handle array of strings
 * cant handle array set to null
 * cant handle nulls in records
 */

public class Neonizer {
    private transient Deconstructor deConstructor;
    ThreadLocal<HashMap<String, String>> lazyCompression = new ThreadLocal<>();
    private Neon.NeonConfig neonConfig = new Neon.NeonConfig(false);
    private Neon.ClassMapper mapper = new Neon.ClassMapper();


    public Neonizer(Neon.NeonConfig neonConfig, Neon.ClassMapper mapper) {
        this.neonConfig = neonConfig.clone();//to avoid reconfig
        this.mapper = mapper;
    }

    public Neonizer() {
        this(new Neon.NeonConfig(false), new Neon.ClassMapper());
    }

    @Override
    protected Neonizer clone() throws CloneNotSupportedException {
        return new Neonizer(neonConfig.clone(), mapper);
    }
    //    private HashMap<String,String> lazyClassName =new HashMap<>();

    private void appendUnknownObjectContent(Object o) throws InvalidNeonException {
        if (o == null) {
            return;
        }
        final String name = o.getClass().getName();
        if (name.startsWith("[")) {
            openArray(Array.getLength(o));
            switch (name) {
                case "[Ljava.lang.Integer;"://untested
                    for (Integer integer : (Integer[]) o) {
                        deConstructor.append(integer);
                        deConstructor.append(',');
                    }
                    break;
                case "[I":
                    for (int i : (int[]) o) {
                        deConstructor.append(i);
                        deConstructor.append(',');
                    }
                    break;
                case "[Ljava.lang.Long;":
                    for (Long aLong : (Long[]) o) {
                        deConstructor.append(aLong);
                        deConstructor.append(',');
                    }
                    break;
                case "[J":
                    for (long l : (long[]) o) {
                        deConstructor.append(l);
                        deConstructor.append(',');
                    }
                    break;
                case "[Ljava.lang.Double;":
                    for (Double aDouble : (Double[]) o) {
                        deConstructor.append(aDouble);
                        deConstructor.append(',');
                    }
                    break;
                case "[D":
                    for (double v : (double[]) o) {
                        deConstructor.append(v);
                        deConstructor.append(',');
                    }
                    break;
                case "[Ljava.lang.Float;"://untested
                    for (Float aFloat : (Float[]) o) {
                        deConstructor.append(aFloat);
                        deConstructor.append(',');
                    }
                    break;
                case "[F":
                    assert o instanceof float[];
                    final float[] of = (float[]) o;
                    for (float f : of) {
                        deConstructor.append(f);
                        deConstructor.append(',');
                    }
                    break;
                case "[Ljava.lang.Boolean;"://untested
                    for (Boolean aBoolean : (Boolean[]) o) {
                        deConstructor.append(aBoolean);
                        deConstructor.append(',');
                    }
                    break;
                case "[Z":
                    assert o instanceof boolean[];
                    final boolean[] oz = (boolean[]) o;
                    for (boolean b : oz) {
                        deConstructor.append(b);
                        deConstructor.append(',');
                    }
                    break;
                case "[Ljava.lang.Character;"://optimizable//untested
                    final Character[] o1 = (Character[]) o;
                    char[] chars = new char[o1.length];
                    for (int i = 0; i < o1.length; i++) {
                        chars[i] = o1[i];
                    }
                    deConstructor.appendString(new String(chars));//ok
                    break;
                case "[C"://optimizable//untested
                    final String value = new String((char[]) o);
                    deConstructor.appendString(value);//ok
                    break;
                case "[Ljava.lang.Byte;"://optimizable//untested
                    deConstructor.append((Byte[]) o);
                    break;
                case "[B":
                    deConstructor.append((byte[]) o);
                    break;
                case "[Ljava.lang.String;"://untested
                    for (String s : (String[]) o) {
                        deConstructor.appendString(s);//ok
                        deConstructor.append(',');
                    }
                    break;
                default:
                    final Object[] oos = (Object[]) o;
                    for (Object item : oos) {
                        openIndent(item);
                        openIndent();
                        appendUnknownObjectContent(item);
                        closeIndent();
                        closeIndent();
                        deConstructor.append(',');
                    }
            }
            closeArray();
        } else {
            switch (name) {
                case "java.lang.Integer":
                    deConstructor.append((Integer) o);
                    break;
                case "java.lang.Long":
                    deConstructor.append((Long) o);
                    break;
                case "java.lang.Double":
                    deConstructor.append((Double) o);
                    break;
                case "java.lang.Float":
                    deConstructor.append((Float) o);
                    break;
                case "java.lang.Byte":
                    deConstructor.append((Float) o);
                    break;
                case "java.lang.Boolean":
                    deConstructor.append((Boolean) o);
                    break;
                case "java.lang.Character":
                    deConstructor.append((Character) o);
                    break;
                case "java.lang.String":
                    final String o1 = (String) o;
                    deConstructor.appendString(o1);//ok
                    break;
                default:
                    if (o instanceof Collection) {
                        final Collection<?> o2 = (Collection<?>) o;
                        openIndent();
                        openArray(o2.size());
                        for (Object i : o2) {
                            appendObject(i);
                        }
                        closeArray();
                        closeIndent();
                    } else if (o instanceof Map) {
                        final Map<?, ?> o2 = (Map<?, ?>) o;
                        openIndent();
                        openArray(o2.size());
                        for (Map.Entry<?, ?> entry : o2.entrySet()) {
                            Object key = entry.getKey();
                            Object value = entry.getValue();
                            appendObject(key);
                            appendObject(value);
                        }
                        closeArray();
                        closeIndent();
                    } else if (o instanceof Enum) {
                        openIndent();
                        deConstructor.appendInternalString(((Enum<?>) o).name());
                        deConstructor.append(',');
                        deConstructor.append(((Enum<?>) o).ordinal());
                        closeIndent();
                    } else {
                        openIndent();
                        handleCustomClass(o);
                        closeIndent();
                    }
            }
        }
//        if (isPrimitiveOrPrimitiveWrapper(c)) {
//            appendValue(fabricator, name, o);
//        } else if (c.equals(String.class)) {
//            appendString(fabricator, name, o);
//        } else if (c.isArray()) {
//            appendValuePrefix(fabricator, name);
//            appendArray(fabricator, o);
//            appendSuffix(fabricator);
//        } else if (o instanceof Collection) {
//            appendValuePrefix(fabricator, name);
//            appendCollection(fabricator, (Collection) o);
//            appendSuffix(fabricator);
//        } else if (o instanceof Map) {
//            appendValuePrefix(fabricator, name);
//            appendMap(fabricator, (Map) o);
//            appendSuffix(fabricator);
//        } else if (o instanceof Enum) {
//            appendValuePrefix(fabricator, name);
//            appendEnum(fabricator, (Enum) o);
//            appendSuffix(fabricator);
//        } else {
//            appendValuePrefix(fabricator, name);
//            appendClass(fabricator, o);
//            appendSuffix(fabricator);
//        }
    }

    private void appendClassName(String name) {
        final HashMap<String, String> stringStringHashMap = lazyCompression.get();
        name = mapper.compress(name);
        final String s = stringStringHashMap.get(name);
        if (s == null) {
            deConstructor.append('~');
            deConstructor.appendInternalString(name);
            final int size = stringStringHashMap.size();
            deConstructor.appendInternalString(":" + size);
            stringStringHashMap.put(name, "~~" + size);
        } else {
            deConstructor.appendInternalString(s);
        }
    }

    private void handleCustomClass(Object o) throws InvalidNeonException {
        if (o instanceof Neon.ManualSerialization) {
            ((Neon.ManualSerialization) o).toString(deConstructor);
            return;
        }
        final boolean listener = o instanceof Neon.SerializationEvent;
        if (listener) {
            ((Neon.SerializationEvent) o).serializationStarted();
        }
        try {
            final String name = o.getClass().getName();
            if (!fieldsOfClass.containsKey(name)) {
                fillFieldList(o);
            }
            final Collection<Field> fields = fieldsOfClass.get(name).values();
            try {
                for (Field field : fields) {
                    final Object o1;
                    o1 = field.get(o);
                    if (o1 != null) {
                        deConstructor.append(',');
                        deConstructor.appendInternalString(field.getName());
                        deConstructor.append('=');
                        appendObject(o1);
//                    openIndent(o1);
//                    appendUnknownObjectContent(o1);
//                    closeIndent();
                    }
                }
                deConstructor.appendInternalString(",~=");
            } catch (IllegalAccessException e) {
                throw new InvalidNeonException(e);
            }
        }finally {
            if (listener) {
                ((Neon.SerializationEvent) o).serializationFinished();
            }
        }


    }

    private void appendObject(Object i) throws InvalidNeonException {
        openIndent(i);
        openIndent();
        appendUnknownObjectContent(i);
        closeIndent();
        closeIndent();
    }

    public String neonize(Object o) throws InvalidNeonException {
        deConstructor = new HardDeconstructor();
        return _neonize(o);
    }

    private String _neonize(Object o) throws InvalidNeonException {
        lazyCompression.set(new HashMap<>());
        deConstructor.appendInternalString(SerializationVersion.V2);
        openIndent(o);
        openIndent();
        appendUnknownObjectContent(o);
        closeIndent();
        closeIndent();
        return deConstructor.toString();
    }

    public String neonize(Object o, OutputStream stream) throws InvalidNeonException {
        deConstructor = new StreamDeconstructor(stream);
        return _neonize(o);
    }

    private void closeIndent() {
        if (neonConfig.makePretty) {
            deConstructor.append('\n');
            neonConfig.indentLevel--;
            for (int i = 0; i < neonConfig.indentLevel; i++) {
                deConstructor.append('\t');
            }
            deConstructor.append('}');
        } else {
            deConstructor.append('}');
        }
    }

    private void openIndent() {
        if (neonConfig.makePretty) {
            deConstructor.append('{');
            neonConfig.indentLevel++;
            deConstructor.append('\n');
            for (int i = 0; i < neonConfig.indentLevel; i++) {
                deConstructor.append('\t');
            }
        } else {
            deConstructor.append('{');
        }
    }

    private void openIndent(Object o) {
        deConstructor.append('{');
        final String name = o.getClass().getName();
        appendClassName(name);
        if (neonConfig.makePretty) {
            neonConfig.indentLevel++;
            deConstructor.append('\n');
            for (int i = 0; i < neonConfig.indentLevel; i++) {
                deConstructor.append('\t');
            }
        }
    }

    private void openArray(int length) {
        deConstructor.append('[');
        deConstructor.append(length);
        deConstructor.append(':');
        if (neonConfig.makePretty) {
            neonConfig.indentLevel++;
            deConstructor.append('\n');
            for (int i = 0; i < neonConfig.indentLevel; i++) {
                deConstructor.append('\t');
            }
        }
    }

    private void closeArray() {
        if (neonConfig.makePretty) {
            deConstructor.append('\n');
            neonConfig.indentLevel--;
            for (int i = 0; i < neonConfig.indentLevel; i++) {
                deConstructor.append('\t');
            }
            deConstructor.append(']');
        } else {
            deConstructor.append(']');
        }
    }

    static class SerializationVersion {
        @Deprecated
        static String V1 = "N̉̾̂e̓ͮ̓o͗͒̉nͣ̅͑";
        static String V2 = "NeonV2";
    }
}