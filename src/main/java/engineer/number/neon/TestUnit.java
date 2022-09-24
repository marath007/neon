package engineer.number.neon;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

import static engineer.number.neon.TestUnit.Car.Blue;

import engineer.number.neon.interfaces.Deconstructor;
import engineer.number.neon.interfaces.Fabricator;

import java.util.ArrayList;
import java.util.HashMap;

class TestUnit {
    //    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static Neonizer neonizer = new Neonizer(new Neon.NeonConfig(false), new Neon.ClassMapper());
    static Deneonizer deneonizer = new Deneonizer(new Neon.NeonConfig(false), new Neon.ClassMapper());

    public static void main(String[] args) throws InvalidNeonException, InvalidHeader {
        final Carrot o = new Carrot();
        final Vegetable v = Neon.deepDownCast(o);

        final String neonize = neonizer.neonize(o);
        System.out.println(neonize);
        final Object deneonize = deneonizer.deneonize(neonize, Vegetable.class);
//        final Object deneonize = deneonizer.deneonize("NeonV2{~engineer.number.neon.TestUnit$Garden:0{{,f={~engineer.number.neon.TestUnit$Carrot:1{{,d={~java.lang.Integer:2{1}},e={~~2{1}},a={~~2{1}},b={~~2{1}},c={~~2{1}},~=}}},g={~~1{{,d={~~2{1}},e={~~2{1}},a={~~2{1}},b={~~2{1}},c={~~2{1}},~=}}},h={~~1{{,d={~~2{1}},e={~~2{1}},a={~~2{1}},b={~~2{1}},c={~~2{1}},~=}}},~=}}}");
        System.out.println(deneonize);
//        testArrays();
//        if (true)return;
//        final Neon.ClassMapper mapper = new Neon.ClassMapper();
////        mapper.addCompressionEntry("java.util.ArrayList","AL");
////        mapper.addCompressionEntry("java.lang.String","Ss");
////        mapper.addCompressionEntry("java.util.HashMap","Hash");
//        Neonizer neonizer = new Neonizer();
//        Deneonizer deneonizer = new Deneonizer();
//
//        final ArrayList<Object> o = new ArrayList<>();
//        o.add(1);
//        o.add("2");
//        o.add(4L);
//        final Long[] e = {14861l, 14l, 1413l};
//
//            deneonizer.deneonize(neonizer.neonize(e),Double[].class);
//
//        o.add(e);
//        o.add(new TestOne());
//        o.add(new EggNog(true));
//        final String s = neonizer.neonize(o);
//        System.out.println(s);
//        deneonizer.deneonize(s);
//        try {
//            System.out.println(neonizer.toString(new EggNog(true),new FileOutputStream("F:\\DevFolder\\IdeaProjects\\NumberEngineersAndroid\\neon\\src\\main\\java\\com\\numberengineer\\neon\\test.txt")));
//        } catch (FileNotFoundException fileNotFoundException) {
//            fileNotFoundException.printStackTrace();
//        }
//        testArrays();
//        testClass(new Arrrrr(true));
//        testClass(new EggNog());
//        testClass(new EggNog(true));
//        testClass(new Origami());
//        testClass(new Origami(true));
    }

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
        stringStringHashMap.put(1l, "2");
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
        System.out.println(neonizer.neonize(new long[][][]{{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}}));
        System.out.println(neonizer.neonize(new Long[]{14861l, 14l, 1413l}));
        System.out.println(neonizer.neonize(new Long[][]{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}));
        System.out.println(neonizer.neonize(new Long[][][]{{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}}));
        System.out.println(neonizer.neonize(new double[]{14861l, 14l, 1413l}));
        System.out.println(neonizer.neonize(new double[][]{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}));
        System.out.println(neonizer.neonize(new double[][][]{{{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}, {{14861l, 14l, 1413l}, {14862121l, 1421l, 14343l}, {14454861l, 614l, 141423l}}}));
        System.out.println(neonizer.neonize(new Double[]{14861d, 14d, 1413d}));
        System.out.println(neonizer.neonize(new Double[][]{{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}));
        System.out.println(neonizer.neonize(new Double[][][]{{{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}, {{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}, {{14861d, 14d, 1413d}, {14862121d, 1421d, 14343d}, {14454861d, 614d, 141423d}}}));
    }

    private static void testClass(Object o) throws InvalidNeonException {
        System.out.println(neonizer.neonize(o));
//        System.out.println(gson.toJson(o));
//        System.out.println(gson.fromJson(gson.toJson(o), o.getClass()));
//        System.out.println(engineer.number.neon.obselete.Neon.testNeonQuality(o));
//        System.out.println(neonizer.testNeonStreamQuality(o));
    }

    enum Car {Blue, Red}

    public enum Test {
        ONE, TWO
    }

    public static class Vegetable {
        int a = 1;
        int b = 2;
        int c = 3;
    }
    public static class Carrot extends Vegetable {
        int d = 4;
        int e = 4;
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
