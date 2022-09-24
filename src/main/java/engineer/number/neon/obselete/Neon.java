package engineer.number.neon.obselete;

import engineer.number.neon.InvalidNeonException;
import engineer.number.neon.NeonException;
import engineer.number.neon.obselete.ClassReader;
import engineer.number.neon.obselete.NullDataException;
import engineer.number.neon.obselete.OldClassException;
import engineer.number.neon.obselete.ToStringStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

 abstract class Neon {
    final static Method getRecordComponents;
    final static Method getType;
    final static Method getName;
    final static Method isRecord;
    static final transient ConcurrentHashMap<String, Map<String, Field>> fieldsOfClass = new ConcurrentHashMap<>();
     static boolean RECORD_SUPPORTED = Arrays.stream(Class.class.getMethods()).anyMatch(method -> "isRecord".equals(method.getName()));
     public static int CLASS_NAME_MIN_LENGTH = 3;
    static HashMap<String, String> compressedNameToClass = new HashMap<>();
    static HashMap<String, String> classToCompressedName = new HashMap<>();

    static {
        Method temp1;
        Method temp2;
        Method temp3;
        Method temp4;
        try {
            temp1 = Class.class.getMethod("isRecord");
            temp2 = Class.forName("java.lang.reflect.RecordComponent").getMethod("getType");
            temp3 = Class.class.getMethod("getRecordComponents");
            temp4 = Class.forName("java.lang.reflect.RecordComponent").getMethod("getName");
        } catch (Exception e) {
            temp1 = null;
            temp2 = null;
            temp3 = null;
            temp4 = null;
        }
        isRecord = temp1;
        getType = temp2;
        getRecordComponents = temp3;
        getName = temp4;
    }

    public static void addCompressionEntry(String className, String compressedName) {
        compressedNameToClass.put(compressedName, className);
        classToCompressedName.put(className, compressedName);
    }

    static public String neonize(Object o) {
        return ToString.toString(o);
    }

    static public void neonize(Object o, PrintWriter printWriter) {
        ToStringStream.toString(o, printWriter);
    }

    static public <T> T deNeonize(String string, Class<T> classOfT) throws InvalidNeonException {
        return FromString.fromString(string, classOfT, null, false);
    }

    static public <T> T deNeonize(ClassReader classReader) throws InvalidNeonException, OldClassException, IOException, NullDataException {
        return FromStringStream.fromString(classReader);
    }

    static public <T> T deNeonize(ClassReader classReader, ParameterizedType parameterizedType) throws InvalidNeonException, OldClassException, IOException, NullDataException {
        return FromStringStream.fromString(classReader, parameterizedType);
    }

    static public <T> T deNeonize(String string, Class<T> classOfT, ParameterizedType parameter) throws InvalidNeonException {
        return FromString.fromString(string, classOfT, parameter, false);
    }

    static public <T> T deNeonize(String string) throws InvalidNeonException {
        return FromString.fromString(string);
    }

    static public boolean testNeonQuality(Object o) {
        String neonize = neonize(o);
        String neonize1;
        try {
            neonize1 = neonize(deNeonize(neonize));
        } catch (InvalidNeonException e) {
            neonize1 = null;
        }
        return Objects.equals(neonize, neonize1);
    }

    static public boolean testNeonStreamQuality(Object o) {
        String neonize = neonize(o);
        String neonize1 = null;
        try {
            neonize1 = neonize(deNeonize(new ClassReader(new InputStreamReader(new ByteArrayInputStream(neonize.getBytes())))));
        } catch (InvalidNeonException e) {
            e.printStackTrace();
        } catch (NullDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OldClassException e) {
            e.printStackTrace();
        }
        return Objects.equals(neonize, neonize1);
    }

    static void fillFieldList(Object o) {
        LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
        Field[] fields1 = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields1.length; i++) {
            int modifiers = fields1[i].getModifiers();
            if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
                if (fields1[i].getName().startsWith("this$")) {
                    continue;
                }
                fields1[i].setAccessible(true);
                fields.put(fields1[i].getName(), fields1[i]);
            }
        }
        Class sup = o.getClass().getSuperclass();
        while (sup != null) {
            Field[] fields2 = sup.getDeclaredFields();
            for (int i = 0; i < fields2.length; i++) {
                int modifiers = fields2[i].getModifiers();
                if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
                    if (fields2[i].getName().startsWith("this$")) {
                        continue;
                    }
                    fields2[i].setAccessible(true);
                    fields.put(fields2[i].getName(), fields2[i]);
                }
            }
            sup = sup.getSuperclass();
        }
        synchronized (fieldsOfClass) {
            fieldsOfClass.put(o.getClass().getName(), fields);
        }
    }

    public interface CustomToString<T> {
        void fastToString(StringBuilder stringBuilder);

        void fastToString(PrintWriter stringBuilder);

        T fastValueOf(String s);
    }

    /**
     * Use the ExtendedConstructor interface when the class need further construction
     * after being built from the default constructor
     * <p>
     * by concept, the design work once out of the constructors code, must be called independently
     * or inconsistend behavior will ensue
     */
    public interface ExtendedConstructor {
        void finishConstruction() throws NeonException;
    }
}