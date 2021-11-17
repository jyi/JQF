package janala.logger.inst;

public class IASTORE extends Instruction {
  public IASTORE(int iid, int mid) {
    super(iid, mid);
  }

    public IASTORE(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitIASTORE(this);
  }

  @Override
  public String toString() {
    return "IASTORE iid=" + iid + " mid=" + mid;
  }
}
