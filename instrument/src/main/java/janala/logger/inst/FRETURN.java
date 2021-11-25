package janala.logger.inst;

public class FRETURN extends Instruction {
  public String owner;
  public String name;
  public String desc;

  public FRETURN(int iid, int mid) {
    super(iid, mid);
  }

    public FRETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

  public FRETURN(String fileName, String method, int iid, int mid) {
    super(fileName, method, iid, mid);
    this.name = method;
    this.owner = fileName;
  }



    public void visit(IVisitor visitor) {
    visitor.visitFRETURN(this);
  }

  @Override
  public String toString() {
    return "FRETURN iid=" + iid + " mid=" + mid;
  }
}
