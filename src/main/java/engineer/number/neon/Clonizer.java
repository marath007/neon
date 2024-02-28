package engineer.number.neon;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

class Clonizer {
    private Neon.NeonConfig neonConfig = new Neon.NeonConfig(false);
    private Neon.ClassMapper mapper = new Neon.ClassMapper();


    public Clonizer(Neon.NeonConfig neonConfig, Neon.ClassMapper mapper) {
        this.neonConfig = neonConfig.clone();//to avoid reconfig
        this.mapper = mapper;
    }

    public Clonizer() {
        this(new Neon.NeonConfig(false), new Neon.ClassMapper());
    }

    @Override
    protected Clonizer clone() throws CloneNotSupportedException {
        return new Clonizer(neonConfig.clone(), mapper);
    }
    //    private HashMap<String,String> lazyClassName =new HashMap<>();

    private <T> T cloneUnknownObjectContent(T o) throws InvalidNeonException {
        if (o == null) {
            return null;
        }
        final String name = o.getClass().getName();
        if (name.startsWith("[")) {
            int length = Array.getLength(o);
            switch (name) {
                case "[Ljava.lang.Integer;"://untested
                    return (T) Arrays.copyOf((Integer[]) o, length);
                case "[I":
                    return (T) Arrays.copyOf((int[]) o, length);
                case "[Ljava.lang.Long;":
                    return (T) Arrays.copyOf((Long[]) o, length);
                case "[J":
                    return (T) Arrays.copyOf((long[]) o, length);
                case "[Ljava.lang.Double;":
                    return (T) Arrays.copyOf((Double[]) o, length);
                case "[D":
                    return (T) Arrays.copyOf((double[]) o, length);
                case "[Ljava.lang.Float;"://untested
                    return (T) Arrays.copyOf((Float[]) o, length);
                case "[F":
                    return (T) Arrays.copyOf((float[]) o, length);
                case "[Ljava.lang.Boolean;":
                    return (T) Arrays.copyOf((Boolean[]) o, length);
                case "[Z":
                    return (T) Arrays.copyOf((boolean[]) o, length);
                case "[Ljava.lang.Character;"://optimizable//untested
                    return (T) Arrays.copyOf((Character[]) o, length);
                case "[C"://optimizable//untested
                    return (T) Arrays.copyOf((char[]) o, length);
                case "[Ljava.lang.Byte;"://optimizable//untested
                    return (T) Arrays.copyOf((Byte[]) o, length);
                case "[B":
                    return (T) Arrays.copyOf((byte[]) o, length);
                case "[Ljava.lang.String;"://untested
                    return (T) Arrays.copyOf((String[]) o, length);
                default:
                    final Object[] oldOs = (Object[]) o;
                    String className = o.getClass().getName();
                    String itemName;
                    if (className.startsWith("[[")) {
                        itemName = className.substring(1);
                    } else {
                        itemName = className.substring(2, className.length() - 1);
                    }
                    final Object[] newOs;
                    try {
                        newOs = (Object[]) Array.newInstance(Class.forName(itemName), length);
                    } catch (ClassNotFoundException e) {
                        throw new NeonException(e);
                    }
                    for (int i = 0; i < oldOs.length; i++) {
                        newOs[i] = cloneUnknownObjectContent(oldOs[i]);
                    }
                    return (T) newOs;
            }
        } else if (o instanceof Collection) {
            final Collection<?> oldCollection = (Collection<?>) o;
            final Collection newCollection;
            try {
                newCollection = (Collection) o.getClass().getConstructor(Integer.TYPE).newInstance(oldCollection.size());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new InvalidNeonException(e);
            }
            for (Object object : oldCollection) {
                newCollection.add(cloneUnknownObjectContent(object));
            }
            return (T) newCollection;
        } else if (o instanceof Map) {
            final Map<?, ?> oldMap = (Map<?, ?>) o;
            try {
                final Map newMap = (Map) o.getClass().getConstructor(Integer.TYPE).newInstance(oldMap.size());
                for (Map.Entry<?, ?> entry : oldMap.entrySet()) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    newMap.put(cloneUnknownObjectContent(key), cloneUnknownObjectContent(value));
                }
                return (T) newMap;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new InvalidNeonException(e);
            }

        } else if (o instanceof Enum) {
            return o;
        } else {
            return (T) handleCustomClass(o);
        }
    }


    private Object handleCustomClass(Object oldObject) throws InvalidNeonException {
        final boolean listener = oldObject instanceof Neon.SerializationEvent;
        if (listener) {
            ((Neon.SerializationEvent) oldObject).serializationStarted();
        }
        try {
            final Constructor<?> declaredConstructor = oldObject.getClass().getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            Object newObject = declaredConstructor.newInstance();
            final String name = newObject.getClass().getName();
            if (!fieldsOfClass.containsKey(name)) {
                fillFieldList(newObject);
            }
            final Collection<Field> fields = (fieldsOfClass).get(name).values();
            try {
                for (Field field : fields) {
                    final Object childObject;
                    childObject = field.get(oldObject);
                    switch (field.getType().getName()) {
                        case "int":
                            field.setInt(newObject,field.getInt(oldObject));
                            break;
                        case "long":
                            field.setLong(newObject,field.getLong(oldObject));
                            break;
                        case "double":
                            field.setDouble(newObject,field.getDouble(oldObject));
                            break;
                        case "float":
                            field.setFloat(newObject,field.getFloat(oldObject));
                            break;
                        case "byte":
                            field.setByte(newObject,field.getByte(oldObject));
                            break;
                        case "boolean":
                            field.setBoolean(newObject,field.getBoolean(oldObject));
                            break;
                        case "char":
                            field.setChar(newObject,field.getChar(oldObject));
                            break;
                        case "java.lang.Integer":
                        case "java.lang.Long":
                        case "java.lang.Double":
                        case "java.lang.Float":
                        case "java.lang.Byte":
                        case "java.lang.Boolean":
                        case "java.lang.Character":
                        case "java.lang.String":
                            field.set(newObject, childObject);
                            break;
                        default:
                            field.set(newObject, cloneUnknownObjectContent(childObject));
                    }
                }
                return newObject;
            } catch (IllegalAccessException e) {
                throw new InvalidNeonException(e);
            }
        } catch (Exception e) {
            throw new NeonException(e);
        } finally {
            if (listener) {
                ((Neon.SerializationEvent) oldObject).serializationFinished();
            }
        }
    }

    <T> T clonize(T o) throws InvalidNeonException {
        return cloneUnknownObjectContent(o);
    }


}