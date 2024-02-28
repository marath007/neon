package engineer.number.neon;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Optional;

import static engineer.number.neon.Neon.deepClonize;
import static engineer.number.neon.Neon.ignoreTransientClasses;

class TestUnit {
    //    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static Neonizer neonizer = new Neonizer(new Neon.NeonConfig(false), new Neon.ClassMapper());
    static Deneonizer deneonizer = new Deneonizer(new Neon.NeonConfig(false), new Neon.ClassMapper());


    private static void testArrays() throws InvalidNeonException {
        final Neon.ClassMapper mapper = new Neon.ClassMapper();
//        mapper.addCompressionEntry("java.util.ArrayList","AL");
//        mapper.addCompressionEntry("java.lang.String","Ss");
//        mapper.addCompressionEntry("java.util.HashMap","Hash");
        Neonizer neonizer = new Neonizer(new Neon.NeonConfig(true), mapper);
        System.out.println(neonizer.neonize(new TestOne()));
        System.out.println(neonizer.neonize(new TestOne()));
        System.out.println(neonizer.neonize(new EggNog()));
        System.out.println(neonizer.neonize(Car.Blue));
//        System.out.println(neonizer.neonize(Blue));
        HashMap<Object, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("a", "1");
        stringStringHashMap.put(Optional.of(1L), "2");
        stringStringHashMap.put("c", "3");
        System.out.println(neonizer.neonize(stringStringHashMap));
        final ArrayList<String> o = new ArrayList<>();
        System.out.println(neonizer.neonize(o));
        o.add("1");
        System.out.println(neonizer.neonize(o));
        o.add("2");
        System.out.println(neonizer.neonize(o));
        System.out.println(neonizer.neonize(new byte[]{1, 4, 7}));
        System.out.println(neonizer.neonize(new byte[][]{{}, {4}, {7, 3}}));
        System.out.println(neonizer.neonize(new boolean[]{false, true, false}));
        System.out.println(neonizer.neonize(new float[]{1, 4, 6}));
        System.out.println(neonizer.neonize(new int[]{1, 5, 6}));
        System.out.println(neonizer.neonize(new byte[][]{{1, 4, 7}, {1, 4, 7}, {1, 4, 7}}));
        System.out.println(neonizer.neonize(new boolean[][]{{false, true, false}, {false, true, false}, {false, true, false}}));
        System.out.println(neonizer.neonize(new float[][]{{1, 4, 6}, {1, 4, 6}, {1, 4, 6}}));
        System.out.println(neonizer.neonize(new int[][]{{1, 5, 6}, {1, 5, 6}, {1, 5, 6}}));
        System.out.println(neonizer.neonize(new long[]{14861l, 14l, 1413l}));
        System.out.println(neonizer.neonize(new long[][]{{14861l, 14L, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}));
        System.out.println(neonizer.neonize(new long[][][]{{{14861L, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}}));
//        System.out.println(neonizer.neonize(new Long[]{14861l, 14l, 1413l}));
//        System.out.println(neonizer.neonize(new Long[][]{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}));
//        System.out.println(neonizer.neonize(new Long[][][]{{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}}));
//        System.out.println(neonizer.neonize(new double[]{14861l, 14l, 1413l}));
//        System.out.println(neonizer.neonize(new double[][]{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}));
//        System.out.println(neonizer.neonize(new double[][][]{{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}}));
//        System.out.println(neonizer.neonize(new Double[]{14861d, 14d, 1413d}));
//        System.out.println(neonizer.neonize(new Double[][]{{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}));
//        System.out.println(neonizer.neonize(new Double[][][]{{{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}, {{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}, {{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}}));
    }

    private static void testClass(Object o) throws InvalidNeonException {
        System.out.println(neonizer.neonize(o));
//        System.out.println(gson.toJson(o));
//        System.out.println(gson.fromJson(gson.toJson(o), o.getClass()));
//        System.out.println(engineer.number.neon.obselete.Neon.testNeonQuality(o));
//        System.out.println(neonizer.testNeonStreamQuality(o));
    }

    public static void main(String[] args) throws InvalidNeonException, InvalidHeader {
        EggNog eggNog=new EggNog(true);
        EggNog eggNog2=deepClonize(eggNog);



        final String[] strings = new String[]{"egg", null, "nog"};
        ignoreTransientClasses.add(BitSet.class);
        final int pow = (int) Math.pow(7, 7);
        final BitSet o1 = new BitSet(pow);
        long x = System.currentTimeMillis();


        x = System.currentTimeMillis();
        String s;
        for (int i = 0; i < pow; i += 2) {
            o1.set(i);
        }
        x = System.currentTimeMillis();
        s = Neon.writeObjectToString(o1);
        System.out.println(System.currentTimeMillis() - x);
        System.out.println(s.length());
        for (int i = 1; i < pow; i += 2) {
            o1.set(i);
        }
        x = System.currentTimeMillis();
        s = Neon.writeObjectToString(o1);
        System.out.println(System.currentTimeMillis() - x);
        System.out.println(s.length());
        final BitSet o2 = Neon.readObject(s);
        o1.get(2);
        o2.cardinality();
        o1.cardinality();
        o2.get(2);

        Neon.writeObjectToString(strings);
        final Object c = new Carrot();
        final Object v = Neon.deepDownCast(c);
        final Object o = Neon.deepDownCast(Neon.deepDownCast(c));
        final Object vc = c;
        final Object z = Neon.zombieCast(c, CarrotZombie.class);
        System.out.println("lol");
    }

    enum Car {Blue, Red}

    public enum Test {
        ONE, TWO
    }

    public static class Vegetable {
        int roots = 16;
        int vitamins = 124;
    }

    public static class Zombie {
        int brain = 10;
        int vitamins = 12;
    }

    public static class Carrot extends Vegetable {
        int orangeRoot = 42;
        int greenLeaf = 5;
        int mold = 5;
    }

    public static class CarrotZombie extends Zombie {
        int greenRoot = 42;
        int mold = 12;
    }

    public static class Garden {
        Carrot f = new Carrot();
        //        Carrot g = new Carrot();
        Carrot h = new Carrot();
    }


    public static class Origami {
        Greg<EggNog> greg;

        public Origami() {
        }

        public Origami(boolean b) {
            this.greg = new Greg<>(new EggNog());
        }

        public class Bois {

        }
    }

    public static class Greg<T> {
        private T t;

        public Greg() {
        }

        public Greg(T t) {
            this.t = t;
        }
    }

    public static class TestOne {
        double d = 0;
    }

    public static class EggNog {
        int i;
        //        int[] eggs;
        int[] eggs2;
        //        EggNeg[] eggs3;
        String cat;
        EggNeg eggNeg;
        EggNeg eggNeg2;
        Test test;
        private double[][][] doubles;

        public EggNog() {
            super();
        }

        public EggNog(boolean b) {
            i = 181;
//            eggs = new int[]{1, 34, 57};
            eggs2 = new int[]{};
            cat = "{}[]\\=123456";
            eggNeg = new EggNeg();
            eggNeg2 = new EggNeg();
            doubles = new double[][][]{{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}};
//            eggs3 = new EggNeg[]{new EggNeg(9),new EggNeg(6)};
            test = Test.ONE;
        }
    }

    public static class EggNeg implements Neon.ManualSerialization<EggNeg> {
        //        Test test = ONE;
        int cat = 0;

        public EggNeg(int cat) {
            this.cat = cat;
        }

        public EggNeg() {
        }

        @Override
        public void toString(Deconstructor deConstructor) {
            deConstructor.appendString("" + cat);
        }

        @Override
        public EggNeg fastValueOf(Fabricator f) {
            return new EggNeg(Integer.parseInt(f.readBoundString()));
        }
    }

    private static class BoxedString {
        String s;

        public BoxedString() {
        }

        public BoxedString(String s) {
            this.s = s;
        }
    }

    private static class Arrrrr {
        public ArrayList<BoxedString> eggs;

        public Arrrrr() {
        }

        public Arrrrr(boolean b) {
            eggs = new ArrayList<>();
            eggs.add(new BoxedString("1"));
            eggs.add(new BoxedString("2"));
            eggs.add(new BoxedString("3"));
            eggs.add(new BoxedString("4"));
            eggs.add(new BoxedString("5"));
        }
    }
}
