package janala.logger.inst;

public class ARETURN extends Instruction {

  public String owner;
  public String name;
  public String desc;

  public ARETURN(int iid, int mid) {
    super(iid, mid);
  }

    public ARETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

  public ARETURN(String fileName, String method, int iid, int mid) {
    super(fileName, method, iid, mid);
    this.name = method;
    this.owner = fileName;
  }

    public void visit(IVisitor visitor) {
    visitor.visitARETURN(this);
  }

  @Override
  public String toString() {
    return "ARETURN iid=" + iid + " mid=" + mid;
  }
}
