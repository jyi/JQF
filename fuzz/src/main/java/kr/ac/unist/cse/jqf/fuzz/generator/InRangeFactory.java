package kr.ac.unist.cse.jqf.fuzz.generator;

import com.pholser.junit.quickcheck.generator.InRange;

import java.lang.annotation.Annotation;

public class InRangeFactory {

    private static InRangeFactory singleton = new InRangeFactory();

    private InRange range;

    public static InRangeFactory singleton() {
        return singleton;
    }

    private InRangeFactory() { }

    public InRange generate() {
        // TODO
        // if the type is int
//        InRange rst = new InRange() {
//
//            @Override
//            public Class<? extends Annotation> annotationType() {
//                return null;
//            }
//
//            @Override
//            public byte minByte() {
//                return 0;
//            }
//
//            @Override
//            public byte maxByte() {
//                return 0;
//            }
//
//            @Override
//            public short minShort() {
//                return 0;
//            }
//
//            @Override
//            public short maxShort() {
//                return 0;
//            }
//
//            @Override
//            public char minChar() {
//                return 0;
//            }
//
//            @Override
//            public char maxChar() {
//                return 0;
//            }
//
//            @Override
//            public int minInt() {
//                return 0;
//            }
//
//            @Override
//            public int maxInt() {
//                return 0;
//            }
//
//            @Override
//            public long minLong() {
//                return 0;
//            }
//
//            @Override
//            public long maxLong() {
//                return 0;
//            }
//
//            @Override
//            public float minFloat() {
//                return 0;
//            }
//
//            @Override
//            public float maxFloat() {
//                return 0;
//            }
//
//            @Override
//            public double minDouble() {
//                return 0;
//            }
//
//            @Override
//            public double maxDouble() {
//                return 0;
//            }
//
//            @Override
//            public String min() {
//                return null;
//            }
//
//            @Override
//            public String max() {
//                return null;
//            }
//
//            @Override
//            public String format() {
//                return null;
//            }
//
//            @Override
//            public boolean isFixed() {
//                return false;
//            }
//        }

        // if the type is long
        // a different InRnange object

        return null;
    }
}
