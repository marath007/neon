package engineer.number.neon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Neon {
    static final public HashSet<Class> ignoreTransientClasses = new HashSet<>();
    final static Method getRecordComponents;
    final static Method getType;
    final static Method getName;
    final static Method isRecord;
    static final transient ConcurrentHashMap<String, Map<String, Field>> fieldsOfClass = new ConcurrentHashMap<>();
    static boolean RECORD_SUPPORTED;

    static {
        boolean b = false;
        for (Method method : Class.class.getMethods()) {
            if ("isRecord".equals(method.getName())) {
                b = true;
                break;
            }
        }
        RECORD_SUPPORTED = b;
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

    static public <T> T readObject(File file) throws IOException, InvalidHeader, InvalidNeonException {
        return readObject(Files.newInputStream(file.toPath()));
    }

    static public <T> T readObject(InputStream file) throws InvalidHeader, InvalidNeonException {
        return new Deneonizer().deneonize(file);
    }

    static public <T> T readObject(String s) throws InvalidHeader, InvalidNeonException {
        return new Deneonizer().deneonize(s);
    }

    static public <T> T readObject(File file, Class<T> c) throws IOException, InvalidHeader, InvalidNeonException {
        return readObject(Files.newInputStream(file.toPath()), c);
    }

    static public <T> T readObject(InputStream file, Class<T> c) throws InvalidHeader, InvalidNeonException {
        return new Deneonizer().deneonize(file, c);
    }

    static public <T> T readObject(String s, Class<T> c) throws InvalidHeader, InvalidNeonException {
        return new Deneonizer().deneonize(s, c);
    }

    static public void writeObjectToFile(Object o, File f) throws IOException, InvalidNeonException {
        writeObjectToStream(o, Files.newOutputStream(f.toPath()));
    }

    static public void writeObjectToStream(Object o, OutputStream f) throws InvalidNeonException {
        new Neonizer().neonize(o, f);
    }

    static public void writeObjectToBufferedStream(Object o, OutputStream f) throws InvalidNeonException, IOException {
        new Neonizer().neonizeBuffered(o, f);
    }

    static public void writeObjectToThreadedStream(Object o, OutputStream f, Consumer<Boolean> onFinished) throws InvalidNeonException, IOException {
        new Neonizer().neonizeThreaded(o, f, onFinished);
    }

    static public String writeObjectToString(Object o) throws InvalidNeonException {
        return new Neonizer().neonize(o);

    }

    static public StringBuilder writeObjectToStringBuilder(Object o) throws InvalidNeonException {
        return new Neonizer().neonizeBuilder(o);

    }

    static public <T> T deepClone(T t) throws InvalidNeonException, InvalidHeader {
        return new Deneonizer().deneonize(new Neonizer().neonize(t));
    }

    static public <T> T deepDownCast(T t) throws InvalidNeonException, InvalidHeader {
        return new Deneonizer().deneonize(new Neonizer().neonize(t), t.getClass().getSuperclass());
    }

    static public <T, U> T zombieCast(U u, Class<T> clazz) throws InvalidNeonException, InvalidHeader {
        return new Deneonizer().deneonize(new Neonizer().neonize(u), clazz);
    }

//    static public <V> void galvanise(V v, String s) throws InvalidNeonException, InvalidHeader {
//        return new Deneonizer().galvanise(v,s);
//    }

    static public <T, K> boolean deepCompare(T t, K k) throws InvalidNeonException {
        final String neonize1 = new Neonizer().neonize(t);
        final String neonize2 = new Neonizer().neonize(k);
        return Objects.equals(neonize1, neonize2);
    }

    static void fillFieldList(Object o) {
        LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
        LinkedHashMap<String, Field> allFields = new LinkedHashMap<>();
        Field[] fields1 = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields1.length; i++) {
            int modifiers = fields1[i].getModifiers();
            if ((!Modifier.isTransient(modifiers) || ignoreTransientClasses.contains(o.getClass())) && !Modifier.isStatic(modifiers)) {
                if (fields1[i].getName().startsWith("this$")) {
                    continue;
                }
                fields1[i].setAccessible(true);
                fields.put(fields1[i].getName(), fields1[i]);
            }
            if (!Modifier.isStatic(modifiers)) {
                if (fields1[i].getName().startsWith("this$")) {
                    continue;
                }
                fields1[i].setAccessible(true);
                allFields.put(fields1[i].getName(), fields1[i]);
            }
        }
        Class sup = o.getClass().getSuperclass();
        while (sup != null) {
            Field[] fields2 = sup.getDeclaredFields();
            for (int i = 0; i < fields2.length; i++) {
                int modifiers = fields2[i].getModifiers();
                if ((!Modifier.isTransient(modifiers)) && !Modifier.isStatic(modifiers)) {
                    if (fields2[i].getName().startsWith("this$")) {
                        continue;
                    }
                    fields2[i].setAccessible(true);
                    fields.put(fields2[i].getName(), fields2[i]);
                }
                if (!Modifier.isStatic(modifiers)) {
                    if (fields2[i].getName().startsWith("this$")) {
                        continue;
                    }
                    fields2[i].setAccessible(true);
                    allFields.put(fields2[i].getName(), fields2[i]);
                }
            }
            sup = sup.getSuperclass();
        }
        synchronized (fieldsOfClass) {
            fieldsOfClass.put(o.getClass().getName(), fields);
        }
    }

    public interface ManualSerialization<T> {
        void toString(Deconstructor deConstructor);

        T fastValueOf(Fabricator f);
    }

    public interface SerializationEvent {
        void serializationStarted();

        void serializationFinished();
    }

    public interface PostInit {
        void postInit() throws NeonException;
    }

    static public class NeonConfig {
        public boolean makePretty = false;
        transient int indentLevel = 0;

        public NeonConfig(boolean makePretty) {
            this.makePretty = makePretty;
        }

        @Override
        protected NeonConfig clone() {
            return new NeonConfig(this.makePretty);
        }
    }

    static public class ClassMapper {
        private HashMap<String, String> compressedNameToClass = new HashMap<>();
        private HashMap<String, String> classToCompressedName = new HashMap<>();

        public void addCompressionEntry(String className, String compressedName) {
            compressedNameToClass.put(compressedName, className);
            classToCompressedName.put(className, compressedName);
        }

        public String compress(String name) {
            final String s = classToCompressedName.get(name);
            return s == null
                   ? name
                   : s;
        }

        public String decompress(String name) {
            final String s = compressedNameToClass.get(name);
            return s == null
                   ? name
                   : s;
        }
    }
}
