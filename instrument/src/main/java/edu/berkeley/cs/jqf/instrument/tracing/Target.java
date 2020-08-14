package edu.berkeley.cs.jqf.instrument.tracing;

public class Target {

    public static Target[] getTargetArray(String s) {
        if (!s.startsWith("[") && !s.endsWith("]")) {
            throw new RuntimeException("Illformed target array string: " + s);
        }

        s = s.substring(1, s.length() - 1);
        String[] targetStrs = s.split(",");

        Target[] results = new Target[targetStrs.length];
        int i = 0;
        for (String str: targetStrs) {
            str = str.trim();
            String[] cmpts = str.split(":");
            if (cmpts.length != 2) {
                throw new RuntimeException("Illformed target array string: " + str);
            }
            results[i++] = new Target(cmpts[0], Integer.parseInt(cmpts[1]));
        }

        return results;
    }

    private final String filename;
    private final int linenum;

    public Target(String filename, int linenum) {
        this.filename = filename;
        this.linenum = linenum;
    }

    public int getLinenum() {
        return this.linenum;
    }

    public String getFilename() {
        return this.filename;
    }

    // without suffix ".java"
    public String getFilename2() {
        return this.filename.substring(0, this.filename.length() - ".java".length());
    }

    @Override
    public String toString() {
        return filename + ":" + linenum;
    }
}
