package janala.logger.inst;

public class LRETURN extends Instruction {

  public String owner;
  public String name;
  public String desc;

  public LRETURN(int iid, int mid) {
    super(iid, mid);
  }

    public LRETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

  public LRETURN(String fileName, String method, int iid, int mid) {
    super(fileName, method, iid, mid);
    this.name = method;
    this.owner = fileName;
  }



    public void visit(IVisitor visitor) {
    visitor.visitLRETURN(this);
  }

  @Override
  public String toString() {
    return "LRETURN iid=" + iid + " mid=" + mid;
  }
}
