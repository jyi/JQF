package janala.logger.inst;

public class LASTORE extends Instruction {
  public LASTORE(int iid, int mid) {
    super(iid, mid);
  }

    public LASTORE(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLASTORE(this);
  }

  @Override
  public String toString() {
    return "LASTORE iid=" + iid + " mid=" + mid;
  }
}
