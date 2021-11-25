package janala.logger.inst;

import java.io.Serializable;

public abstract class Instruction implements Serializable {
  public String fileName = "";
  public final int iid;
  public final int mid;
    public String name;
    public String owner = "";

    public abstract void visit(IVisitor visitor);

  public Instruction(int iid, int mid) {
    this.iid = iid;
    this.mid = mid;
  }

  public Instruction(String fileName, int iid, int mid) {
    this.fileName = fileName;
    this.iid = iid;
    this.mid = mid;
//    System.out.println("Instruction in " + fileName + " " + Integer.toString(mid));
  }

  public Instruction(String fileName, String method, int iid, int mid) {
    this.fileName = fileName;
    this.name = method;
    this.iid = iid;
    this.mid = mid;
//    System.out.println("Instruction in " + fileName + " " + Integer.toString(mid));
  }
}
