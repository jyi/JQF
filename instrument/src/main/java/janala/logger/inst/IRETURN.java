package janala.logger.inst;

public class IRETURN extends Instruction {

  public String owner;
  public String name;
  public String desc;

  public IRETURN(int iid, int mid) {
    super(iid, mid);
  }

    public IRETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

  public IRETURN(String fileName, String method, int iid, int mid) {
    super(fileName, method, iid, mid);
    this.name = method;
    this.owner = fileName;
  }



    public void visit(IVisitor visitor) {
    visitor.visitIRETURN(this);
  }

  @Override
  public String toString() {
    return "IRETURN iid=" + iid + " mid=" + mid;
  }
}
