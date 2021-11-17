package janala.logger.inst;

public class RETURN extends Instruction {
  public RETURN(int iid, int mid) {
    super(iid, mid);
  }

    public RETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitRETURN(this);
  }

  @Override
  public String toString() {
    return "RETURN iid=" + iid + " mid=" + mid;
  }
}
