package engineer.number.neon;

import engineer.number.neon.interfaces.Deconstructor;
import engineer.number.neon.interfaces.Fabricator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Neon {
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

    static public <T> T deepClone(T t) throws InvalidNeonException, InvalidHeader {
        return new Deneonizer().deneonize(new Neonizer().neonize(t));
    }

    static public <T> T deepDownCast(T t) throws InvalidNeonException, InvalidHeader {
        return new Deneonizer().deneonize(new Neonizer().neonize(t), t.getClass().getSuperclass());
    }

    static public <T, K> boolean deepCompare(T t, K k) throws InvalidNeonException {
        final String neonize1 = new Neonizer().neonize(t);
        final String neonize2 = new Neonizer().neonize(k);
        return Objects.equals(neonize1, neonize2);
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
        transient int indentLevel = 0;
        public boolean makePretty = false;

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
