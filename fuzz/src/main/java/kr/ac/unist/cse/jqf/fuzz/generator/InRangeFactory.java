package kr.ac.unist.cse.jqf.fuzz.generator;

import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.java.lang.*;

import java.lang.annotation.Annotation;

public class InRangeFactory {

    private static InRangeFactory singleton = new InRangeFactory();

    public static InRangeFactory singleton() {
        return singleton;
    }

    private InRangeFactory() { }

    public InRange generate(Generator<?> gen, InRange range) {
        // TODO
        InRange rst = null;
        double widenProportion =  Double.parseDouble(System.getProperty("jqf.ei.widenProportion"));
        if (gen instanceof IntegerGenerator) {
            int diff = (int) ((range.maxInt()-range.minInt())*widenProportion/2);
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
                    return range.minInt()-diff;
                }

                @Override
                public int maxInt() {
                    return range.maxInt()+diff;
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
            long diff = (long) ((range.maxLong()-range.minLong())*widenProportion/2);
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
                    return range.minLong()-diff;
                }

                @Override
                public long maxLong() {
                    return range.maxLong()+diff;
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
            short diff = (short) ((range.maxShort()-range.minShort())*widenProportion/2);
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
                    return (short) (range.minShort()-diff);
                }

                @Override
                public short maxShort() {
                    return (short) (range.maxShort()+diff);
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
            byte diff = (byte) (((range.maxByte()-range.minByte()))*widenProportion/2);
            rst = new InRange() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return range.annotationType();
                }

                @Override
                public byte minByte() {
                    return (byte) (range.minByte()-diff);
                }

                @Override
                public byte maxByte() {
                    return (byte) (range.maxByte()+diff);
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
            char diff = (char) (((long)range.maxChar() - (long)range.minChar())*widenProportion/2);
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
                    return (char) ((long)range.minChar()-(long)diff);
                }

                @Override
                public char maxChar() {
                    return (char) ((long)range.maxChar()+(long)diff);
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
            float diff = (float) ((range.maxFloat() - range.minFloat())*widenProportion/2);
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
                    return range.minFloat()-diff;
                }

                @Override
                public float maxFloat() {
                    return range.maxFloat()+diff;
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
            double diff = (range.maxDouble()-range.minDouble())*widenProportion/2;
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
                    return range.minDouble()-diff;
                }

                @Override
                public double maxDouble() {
                    return range.maxDouble()+diff;
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

        return rst;
    }
}
