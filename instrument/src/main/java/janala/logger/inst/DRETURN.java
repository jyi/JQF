package janala.logger.inst;

public class DRETURN extends Instruction {

  public String owner;
  public String name;
  public String desc;


  public DRETURN(int iid, int mid) {
    super(iid, mid);
  }

    public DRETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

  public DRETURN(String fileName, String method, int iid, int mid) {
    super(fileName, method, iid, mid);
    this.name = method;
    this.owner = fileName;
  }

    public void visit(IVisitor visitor) {
    visitor.visitDRETURN(this);
  }

  @Override
  public String toString() {
    return "DRETURN iid=" + iid + " mid=" + mid;
  }
}
