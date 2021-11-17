package janala.logger.inst;

public class FASTORE extends Instruction {
  public FASTORE(int iid, int mid) {
    super(iid, mid);
  }

    public FASTORE(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitFASTORE(this);
  }

  @Override
  public String toString() {
    return "FASTORE iid=" + iid + " mid=" + mid;
  }
}
