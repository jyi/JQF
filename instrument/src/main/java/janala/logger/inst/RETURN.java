package janala.logger.inst;

public class RETURN extends Instruction {
  public String owner;
  public String name;
  public String desc;
  public RETURN(int iid, int mid) {
    super(iid, mid);
  }

    public RETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

  public RETURN(String fileName, String method, int iid, int mid) {
    super(fileName, method, iid, mid);
    this.name = method;
    this.owner = fileName;
  }

    public void visit(IVisitor visitor) {
    visitor.visitRETURN(this);
  }

  @Override
  public String toString() {
    return "RETURN iid=" + iid + " mid=" + mid;
  }
}
