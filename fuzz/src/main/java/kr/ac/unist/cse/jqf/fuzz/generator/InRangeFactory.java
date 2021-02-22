package kr.ac.unist.cse.jqf.fuzz.generator;

import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.java.lang.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

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

    //gen2 instanceof DoubleGenerator
    public void generate(Generator<?> gen2, int wideningCount) throws IllegalAccessException {
        Class<? extends Generator> genClz = gen2.getClass();
        Field[] fields = genClz.getDeclaredFields();
        try {
            if (gen2.getClass().getField("minDouble") != null || gen2.getClass().getField("maxDouble") != null) {
                try {
                    double minDouble = fields[0].getDouble(genClz);
                    double maxDouble = fields[1].getDouble(genClz);
                    double diff = wideningCount * Math.pow(2, wideningCount - 1) * (maxDouble - minDouble) * widenProportion / 2;
                    fields[1].setDouble(genClz, maxDouble + diff);
                    fields[0].setDouble(genClz, minDouble - diff);
                    System.out.println(gen2.getClass().getField("minDouble").getDouble(gen2.getClass()) + " " + gen2.getClass().getField("maxDouble").getDouble(gen2.getClass()));
                } catch (NoSuchFieldException e) {
                    // ignore
                } catch (IllegalAccessException e) {
                    System.err.println(e.toString());
                }
            }
            if (gen2.getClass().getField("minChar") != null || gen2.getClass().getField("maxChar") != null) {

                char minChar = fields[0].getChar(genClz);
                char maxChar = fields[1].getChar(genClz);
                char diff = (char) (wideningCount * (char) Math.pow(2, wideningCount - 1) * (char) ((maxChar - minChar) * widenProportion / 2));
                fields[1].setChar(genClz, (char) (maxChar + diff));
                fields[0].setChar(genClz, (char) (minChar - diff));
                try {
                    System.out.println(gen2.getClass().getField("minChar").getChar(gen2.getClass()) + " " + gen2.getClass().getField("maxChar").getChar(gen2.getClass()));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

            }
            if (gen2.getClass().getField("minInt") != null || gen2.getClass().getField("maxInt") != null) {

                int minInt = fields[0].getInt(genClz);
                int maxInt = fields[1].getInt(genClz);
                int diff = wideningCount * (int) Math.pow(2, wideningCount - 1) * (int) ((maxInt - minInt) * widenProportion / 2);
                fields[1].setInt(genClz, maxInt + diff);
                fields[0].setInt(genClz, minInt - diff);
                try {
                    System.out.println(gen2.getClass().getField("minInt").getInt(gen2.getClass()) + " " + gen2.getClass().getField("maxInt").getInt(gen2.getClass()));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

            }
        } catch (NoSuchFieldException e) {
            // e.printStackTrace();
        }


    }

    public InRange generate(Generator<?> gen, InRange range, int wideningCount) {
        InRange rst = null;
        if (gen instanceof IntegerGenerator) {
            int diff = wideningCount * (int) Math.pow(2, wideningCount - 1) * (int) ((range.maxInt() - range.minInt()) * widenProportion / 2);
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

                @Override
                public boolean useSeed() {
                    return false;
                }

                @Override
                public byte seedByte() {
                    return 0;
                }

                @Override
                public int seedInt() {
                    return 0;
                }

                @Override
                public double seedDouble() {
                    return 0;
                }

                @Override
                public float seedFloat() {
                    return 0;
                }
		
		@Override
		public long seedLong() {
			return 0L;
		}

		@Override
		public short seedShort() {
			return (short)0;
		}

		@Override
		public char seedChar() {
			return (char)0;
		}

		@Override
		public double ratioPF() {
			return 0D;
		}

		@Override
		public boolean useRatio() {
			return false;
		}

		@Override
		public double deltaCoeff() {
			return 1D;
		}

		@Override
		public int deltaExpo() {
			return 1;
		}

            };
        } else if (gen instanceof LongGenerator) {
            long diff = wideningCount * (long) Math.pow(2, wideningCount - 1) * (long) ((range.maxLong() - range.minLong()) * widenProportion / 2);
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

                @Override
                public boolean useSeed() {
                    return false;
                }

                @Override
                public byte seedByte() {
                    return 0;
                }

                @Override
                public int seedInt() {
                    return 0;
                }

                @Override
                public double seedDouble() {
                    return 0;
                }

                @Override
                public float seedFloat() {
                    return 0;
                }

		@Override
                public long seedLong() {
                        return 0L;
                }

                @Override
                public short seedShort() {
                        return (short)0;
                }

                @Override
                public char seedChar() {
                        return (char)0;
                }

                @Override
                public double ratioPF() {
                        return 0D;
                }

                @Override
                public boolean useRatio() {
                        return false;
                }

                @Override
                public double deltaCoeff() {
                        return 1D;
                }

                @Override
                public int deltaExpo() {
                        return 1;
                }
            };
        } else if (gen instanceof ShortGenerator) {
            short diff = (short) (wideningCount * (short) Math.pow(2, wideningCount - 1) * (short) ((range.maxShort() - range.minShort()) * widenProportion / 2));
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

                @Override
                public boolean useSeed() {
                    return false;
                }

                @Override
                public byte seedByte() {
                    return 0;
                }

                @Override
                public int seedInt() {
                    return 0;
                }

                @Override
                public double seedDouble() {
                    return 0;
                }

                @Override
                public float seedFloat() {
                    return 0;
                }

		@Override
                public long seedLong() {
                        return 0L;
                }

                @Override
                public short seedShort() {
                        return (short)0;
                }

                @Override
                public char seedChar() {
                        return (char)0;
                }

                @Override
                public double ratioPF() {
                        return 0D;
                }

                @Override
                public boolean useRatio() {
                        return false;
                }

                @Override
                public double deltaCoeff() {
                        return 1D;
                }

                @Override
                public int deltaExpo() {
                        return 1;
                }
            };
        } else if (gen instanceof ByteGenerator) {
            byte diff = (byte) (wideningCount * (byte) Math.pow(2, wideningCount - 1) * (byte) ((range.maxByte() - range.minByte()) * widenProportion / 2));
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

                @Override
                public boolean useSeed() {
                    return false;
                }

                @Override
                public byte seedByte() {
                    return 0;
                }

                @Override
                public int seedInt() {
                    return 0;
                }

                @Override
                public double seedDouble() {
                    return 0;
                }

                @Override
                public float seedFloat() {
                    return 0;
                }

		@Override
                public long seedLong() {
                        return 0L;
                }

                @Override
                public short seedShort() {
                        return (short)0;
                }

                @Override
                public char seedChar() {
                        return (char)0;
                }

                @Override
                public double ratioPF() {
                        return 0D;
                }

                @Override
                public boolean useRatio() {
                        return false;
                }

                @Override
                public double deltaCoeff() {
                        return 1D;
                }

                @Override
                public int deltaExpo() {
                        return 1;
                }
            };
        } else if (gen instanceof CharacterGenerator) {
            char diff = (char) (wideningCount * (char) Math.pow(2, wideningCount - 1) * (char) ((range.maxChar() - range.minChar()) * widenProportion / 2));
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

                @Override
                public boolean useSeed() {
                    return false;
                }

                @Override
                public byte seedByte() {
                    return 0;
                }

                @Override
                public int seedInt() {
                    return 0;
                }

                @Override
                public double seedDouble() {
                    return 0;
                }

                @Override
                public float seedFloat() {
                    return 0;
                }

		@Override
                public long seedLong() {
                        return 0L;
                }

                @Override
                public short seedShort() {
                        return (short)0;
                }

                @Override
                public char seedChar() {
                        return (char)0;
                }

                @Override
                public double ratioPF() {
                        return 0D;
                }

                @Override
                public boolean useRatio() {
                        return false;
                }

                @Override
                public double deltaCoeff() {
                        return 1D;
                }

                @Override
                public int deltaExpo() {
                        return 1;
                }
            };
        } else if (gen instanceof FloatGenerator) {
            float diff = (wideningCount * (float) Math.pow(2, wideningCount - 1) * (float) ((range.maxFloat() - range.minFloat()) * widenProportion / 2));
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

                @Override
                public boolean useSeed() {
                    return false;
                }

                @Override
                public byte seedByte() {
                    return 0;
                }

                @Override
                public int seedInt() {
                    return 0;
                }

                @Override
                public double seedDouble() {
                    return 0;
                }

                @Override
                public float seedFloat() {
                    return 0;
                }

		@Override
                public long seedLong() {
                        return 0L;
                }

                @Override
                public short seedShort() {
                        return (short)0;
                }

                @Override
                public char seedChar() {
                        return (char)0;
                }

                @Override
                public double ratioPF() {
                        return 0D;
                }

                @Override
                public boolean useRatio() {
                        return false;
                }

                @Override
                public double deltaCoeff() {
                        return 1D;
                }

                @Override
                public int deltaExpo() {
                        return 1;
                }
            };
        } else if (gen instanceof DoubleGenerator) {
            double diff = (wideningCount * (double) Math.pow(2, wideningCount - 1) * (double) ((range.maxDouble() - range.minDouble()) * widenProportion / 2));
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

                @Override
                public boolean useSeed() {
                    return false;
                }

                @Override
                public byte seedByte() {
                    return 0;
                }

                @Override
                public int seedInt() {
                    return 0;
                }

                @Override
                public double seedDouble() {
                    return 0;
                }

                @Override
                public float seedFloat() {
                    return 0;
                }

		@Override
                public long seedLong() {
                        return 0L;
                }

                @Override
                public short seedShort() {
                        return (short)0;
                }

                @Override
                public char seedChar() {
                        return (char)0;
                }

                @Override
                public double ratioPF() {
                        return 0D;
                }

                @Override
                public boolean useRatio() {
                        return false;
                }

                @Override
                public double deltaCoeff() {
                        return 1D;
                }

                @Override
                public int deltaExpo() {
                        return 1;
                }
            };
        } else {
            throw new RuntimeException("Unhandled generator: " + gen.getClass());
        }

        assert rst != null;
        return rst;
    }
}
