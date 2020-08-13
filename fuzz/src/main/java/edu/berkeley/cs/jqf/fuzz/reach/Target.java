package edu.berkeley.cs.jqf.fuzz.reach;

public class Target {

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
}
