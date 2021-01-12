package edu.berkeley.cs.jqf.fuzz.reach;

public class Target {

  public static Target[] allTargets = null;

  private final String filename;
  private final int linenum;

  public Target(String filename, int linenum) {
    this.filename = filename;
    this.linenum = linenum;
  }

  public static Target parse(CharSequence v) {
    System.out.println("[Target] method parse took " + v);
    String[] split = v.toString().split(":");
    assert split.length == 2;
    String filename = split[0];
    int linenum = Integer.decode(split[1]);
    return new Target(filename, linenum);
  }

  public static Target[] getTargetArray(String s) {
    if (allTargets == null) {
      allTargets = makeTargetArray(s);
    }
    return allTargets;
  }

  private static Target[] makeTargetArray(String s) {
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

  public int getLinenum() {
    return this.linenum;
  }

  public String getFilename() {
    return this.filename;
  }

  @Override
  public String toString() {
    return filename + ":" + linenum;
  }

  public String getClassName() {
    return this.filename.replace(".java", "").replace("/", ".");
  }
}
