package kr.ac.unist.cse.jqf.fuzz.generator;

import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.java.lang.*;

import java.lang.annotation.Annotation;

public class InRangeFactory {

    private static InRangeFactory singleton = new InRangeFactory();
    private final double widenProportion;

    public static InRangeFactory singleton() {
        return singleton;
    }

    private InRangeFactory() {
        if (System.getProperty("jqf.ei.widenProportion") != null) {
            widenProportion = Double.parseDouble(System.getProperty("jqf.ei.widenProportion"));
        } else {
            widenProportion = 0;
        }
    }

    public InRange generate(Generator<?> gen, InRange range, int wideningCount) {
        InRange rst = null;
        if (gen instanceof IntegerGenerator) {
            int diff = wideningCount * (int) Math.pow(2, wideningCount - 1) * (int) ((range.maxInt()-range.minInt()) * widenProportion / 2);
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    return range.minByte();
                }

                @Override
                public byte maxByte() {
                    return range.maxByte();
                }

                @Override
                public short minShort() {
                    return range.minShort();
                }

                @Override
                public short maxShort() {
                    return range.maxShort();
                }

                @Override
                public char minChar() {
                    return range.minChar();
                }

                @Override
                public char maxChar() {
                    return range.maxChar();
                }

                @Override
                public int minInt() {
                    if (diff < 0) {
                        return Integer.MIN_VALUE;
                    }

                    int curMin = range.minInt();
                    try {
                        int newMin = Math.subtractExact(curMin, diff);
                        return newMin;
                    } catch (ArithmeticException e) {
                        return Integer.MIN_VALUE;
                    }
                }

                @Override
                public int maxInt() {
                    if (diff < 0) {
                        return Integer.MAX_VALUE;
                    }

                    int curMax = range.maxInt();
                    try {
                        int newMax = Math.addExact(curMax, diff);
                        return newMax;
                    } catch (ArithmeticException e) {
                        return Integer.MAX_VALUE;
                    }
                }

                @Override
                public long minLong() {
                    return range.minLong();
                }

                @Override
                public long maxLong() {
                    return range.maxLong();
                }

                @Override
                public float minFloat() {
                    return range.minFloat();
                }

                @Override
                public float maxFloat() {
                    return range.maxFloat();
                }

                @Override
                public double minDouble() {
                    return range.minDouble();
                }

                @Override
                public double maxDouble() {
                    return range.maxDouble();
                }

                @Override
                public String min() {
                    return range.min();
                }

                @Override
                public String max() {
                    return range.max();
                }

                @Override
                public String format() {
                    return range.format();
                }

                @Override
                public boolean isFixed() {
                    return range.isFixed();
                }
            };
        } else if (gen instanceof LongGenerator) {
            long diff = wideningCount * (long) Math.pow(2, wideningCount - 1) * (long) ((range.maxLong()-range.minLong()) * widenProportion / 2);
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    return range.minByte();
                }

                @Override
                public byte maxByte() {
                    return range.maxByte();
                }

                @Override
                public short minShort() {
                    return range.minShort();
                }

                @Override
                public short maxShort() {
                    return range.maxShort();
                }

                @Override
                public char minChar() {
                    return range.minChar();
                }

                @Override
                public char maxChar() {
                    return range.maxChar();
                }

                @Override
                public int minInt() {
                    return range.minInt();
                }

                @Override
                public int maxInt() {
                    return range.maxInt();
                }

                @Override
                public long minLong() {
                    if (diff < 0) {
                        return Long.MIN_VALUE;
                    }

                    long curMin = range.minLong();
                    try {
                        long newMin = Math.subtractExact(curMin, diff);
                        return newMin;
                    } catch (ArithmeticException e) {
                        return Long.MIN_VALUE;
                    }
                }

                @Override
                public long maxLong() {
                    if (diff < 0) {
                        return Long.MAX_VALUE;
                    }

                    long curMax = range.maxLong();
                    try {
                        long newMax = Math.addExact(curMax, diff);
                        return newMax;
                    } catch (ArithmeticException e) {
                        return Long.MAX_VALUE;
                    }
                }

                @Override
                public float minFloat() {
                    return range.minFloat();
                }

                @Override
                public float maxFloat() {
                    return range.maxFloat();
                }

                @Override
                public double minDouble() {
                    return range.minDouble();
                }

                @Override
                public double maxDouble() {
                    return range.maxDouble();
                }

                @Override
                public String min() {
                    return range.min();
                }

                @Override
                public String max() {
                    return range.max();
                }

                @Override
                public String format() {
                    return range.format();
                }

                @Override
                public boolean isFixed() {
                    return range.isFixed();
                }
            };
        } else if(gen instanceof ShortGenerator) {
            short diff = (short) (wideningCount * (short) Math.pow(2, wideningCount - 1) * (short) ((range.maxLong()-range.minLong()) * widenProportion / 2));
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    return range.minByte();
                }

                @Override
                public byte maxByte() {
                    return range.maxByte();
                }

                @Override
                public short minShort() {
                    if (diff < 0) {
                        return Short.MIN_VALUE;
                    }

                    short curMin = range.minShort();
                    short newMin = (short) (curMin - diff);

                    if (newMin <= curMin) {
                        return newMin;
                    } else {
                        return Short.MIN_VALUE;
                    }
                }

                @Override
                public short maxShort() {
                    if (diff < 0) {
                        return Short.MAX_VALUE;
                    }

                    short curMax = range.maxShort();
                    short newMax = (short) (curMax + diff);

                    if (newMax >= curMax) {
                        return newMax;
                    } else {
                        return Short.MAX_VALUE;
                    }
                }

                @Override
                public char minChar() {
                    return range.minChar();
                }

                @Override
                public char maxChar() {
                    return range.maxChar();
                }

                @Override
                public int minInt() {
                    return range.minInt();
                }

                @Override
                public int maxInt() {
                    return range.maxInt();
                }

                @Override
                public long minLong() {
                    return range.minLong();
                }

                @Override
                public long maxLong() {
                    return range.maxLong();
                }

                @Override
                public float minFloat() {
                    return range.minFloat();
                }

                @Override
                public float maxFloat() {
                    return range.maxFloat();
                }

                @Override
                public double minDouble() {
                    return range.minDouble();
                }

                @Override
                public double maxDouble() {
                    return range.maxDouble();
                }

                @Override
                public String min() {
                    return range.min();
                }

                @Override
                public String max() {
                    return range.max();
                }

                @Override
                public String format() {
                    return range.format();
                }

                @Override
                public boolean isFixed() {
                    return range.isFixed();
                }
            };
        }else if (gen instanceof ByteGenerator){
            byte diff = (byte) (wideningCount * (byte) Math.pow(2, wideningCount - 1) * (byte) ((range.maxLong()-range.minLong()) * widenProportion / 2));
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    if (diff < 0) {
                        return Byte.MIN_VALUE;
                    }

                    byte curMin = range.minByte();
                    byte newMin = (byte) (curMin - diff);

                    if (newMin <= curMin) {
                        return newMin;
                    } else {
                        return Byte.MIN_VALUE;
                    }
                }

                @Override
                public byte maxByte() {
                    if (diff < 0) {
                        return Byte.MAX_VALUE;
                    }

                    byte curMax = range.maxByte();
                    byte newMax = (byte) (curMax + diff);

                    if (newMax >= curMax) {
                        return newMax;
                    } else {
                        return Byte.MAX_VALUE;
                    }
                }

                @Override
                public short minShort() {
                    return range.minShort();
                }

                @Override
                public short maxShort() {
                    return range.maxShort();
                }

                @Override
                public char minChar() {
                    return range.minChar();
                }

                @Override
                public char maxChar() {
                    return range.maxChar();
                }

                @Override
                public int minInt() {
                    return range.minInt();
                }

                @Override
                public int maxInt() {
                    return range.maxInt();
                }

                @Override
                public long minLong() {
                    return range.minLong();
                }

                @Override
                public long maxLong() {
                    return range.maxLong();
                }

                @Override
                public float minFloat() {
                    return range.minFloat();
                }

                @Override
                public float maxFloat() {
                    return range.maxFloat();
                }

                @Override
                public double minDouble() {
                    return range.minDouble();
                }

                @Override
                public double maxDouble() {
                    return range.maxDouble();
                }

                @Override
                public String min() {
                    return range.min();
                }

                @Override
                public String max() {
                    return range.max();
                }

                @Override
                public String format() {
                    return range.format();
                }

                @Override
                public boolean isFixed() {
                    return range.isFixed();
                }
            };
        }else if (gen instanceof CharacterGenerator){
            char diff = (char) (wideningCount * (char) Math.pow(2, wideningCount - 1) * (char) ((range.maxLong()-range.minLong()) * widenProportion / 2));
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    return range.minByte();
                }

                @Override
                public byte maxByte() {
                    return range.maxByte();
                }

                @Override
                public short minShort() {
                    return range.minShort();
                }

                @Override
                public short maxShort() {
                    return range.maxShort();
                }

                @Override
                public char minChar() {
                    if (diff < 0) {
                        return Character.MIN_VALUE;
                    }

                    char curMin = range.minChar();
                    if (curMin - Character.MIN_VALUE > diff) {
                        return (char) (curMin - diff);
                    } else {
                        return Character.MIN_VALUE;
                    }
                }

                @Override
                public char maxChar() {
                    if (diff < 0) {
                        return Character.MAX_VALUE;
                    }

                    char curMax = range.maxChar();
                    if (Character.MAX_VALUE - curMax > diff) {
                        return (char) (curMax + diff);
                    } else {
                        return Character.MAX_VALUE;
                    }
                }

                @Override
                public int minInt() {
                    return range.minInt();
                }

                @Override
                public int maxInt() {
                    return range.maxInt();
                }

                @Override
                public long minLong() {
                    return range.minLong();
                }

                @Override
                public long maxLong() {
                    return range.maxLong();
                }

                @Override
                public float minFloat() {
                    return range.minFloat();
                }

                @Override
                public float maxFloat() {
                    return range.maxFloat();
                }

                @Override
                public double minDouble() {
                    return range.minDouble();
                }

                @Override
                public double maxDouble() {
                    return range.maxDouble();
                }

                @Override
                public String min() {
                    return range.min();
                }

                @Override
                public String max() {
                    return range.max();
                }

                @Override
                public String format() {
                    return range.format();
                }

                @Override
                public boolean isFixed() {
                    return range.isFixed();
                }
            };
        } else if(gen instanceof FloatGenerator){
            float diff = (wideningCount * (float) Math.pow(2, wideningCount - 1) * (float) ((range.maxLong()-range.minLong()) * widenProportion / 2));
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    return range.minByte();
                }

                @Override
                public byte maxByte() {
                    return range.maxByte();
                }

                @Override
                public short minShort() {
                    return range.minShort();
                }

                @Override
                public short maxShort() {
                    return range.maxShort();
                }

                @Override
                public char minChar() {
                    return range.minChar();
                }

                @Override
                public char maxChar() {
                    return range.maxChar();
                }

                @Override
                public int minInt() {
                    return range.minInt();
                }

                @Override
                public int maxInt() {
                    return range.maxInt();
                }

                @Override
                public long minLong() {
                    return range.minLong();
                }

                @Override
                public long maxLong() {
                    return range.maxLong();
                }

                @Override
                public float minFloat() {
                    if (diff < 0) {
                        return Float.MIN_VALUE;
                    }

                    float curMin = range.minFloat();
                    float newMin = (float) (curMin - diff);

                    if (newMin <= curMin) {
                        return newMin;
                    } else {
                        return Float.MIN_VALUE;
                    }
                }

                @Override
                public float maxFloat() {
                    if (diff < 0) {
                        return Float.MAX_VALUE;
                    }

                    float curMax = range.maxFloat();
                    float newMax = (float) (curMax + diff);

                    if (newMax >= curMax) {
                        return newMax;
                    } else {
                        return Float.MAX_VALUE;
                    }
                }

                @Override
                public double minDouble() {
                    return range.minDouble();
                }

                @Override
                public double maxDouble() {
                    return range.maxDouble();
                }

                @Override
                public String min() {
                    return range.min();
                }

                @Override
                public String max() {
                    return range.max();
                }

                @Override
                public String format() {
                    return range.format();
                }

                @Override
                public boolean isFixed() {
                    return range.isFixed();
                }
            };
        } else if(gen instanceof DoubleGenerator){
            double diff = (wideningCount * (double) Math.pow(2, wideningCount - 1) * (double) ((range.maxLong()-range.minLong()) * widenProportion / 2));
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    return range.minByte();
                }

                @Override
                public byte maxByte() {
                    return range.maxByte();
                }

                @Override
                public short minShort() {
                    return range.minShort();
                }

                @Override
                public short maxShort() {
                    return range.maxShort();
                }

                @Override
                public char minChar() {
                    return range.minChar();
                }

                @Override
                public char maxChar() {
                    return range.maxChar();
                }

                @Override
                public int minInt() {
                    return range.minInt();
                }

                @Override
                public int maxInt() {
                    return range.maxInt();
                }

                @Override
                public long minLong() {
                    return range.minLong();
                }

                @Override
                public long maxLong() {
                    return range.maxLong();
                }

                @Override
                public float minFloat() {
                    return range.minFloat();
                }

                @Override
                public float maxFloat() {
                    return range.maxFloat();
                }

                @Override
                public double minDouble() {
                    if (diff < 0) {
                        return Double.MIN_VALUE;
                    }

                    double curMin = range.minDouble();
                    double newMin = (double) (curMin - diff);

                    if (newMin <= curMin) {
                        return newMin;
                    } else {
                        return Double.MIN_VALUE;
                    }
                }

                @Override
                public double maxDouble() {
                    if (diff < 0) {
                        return Double.MAX_VALUE;
                    }

                    double curMax = range.maxDouble();
                    double newMax = (double) (curMax + diff);

                    if (newMax >= curMax) {
                        return newMax;
                    } else {
                        return Double.MAX_VALUE;
                    }
                }

                @Override
                public String min() {
                    return range.min();
                }

                @Override
                public String max() {
                    return range.max();
                }

                @Override
                public String format() {
                    return range.format();
                }

                @Override
                public boolean isFixed() {
                    return range.isFixed();
                }
            };
        }
        else {
            throw new RuntimeException("Unhandled generator: " + gen.getClass());
        }

        assert rst != null;
        return rst;
    }
}
