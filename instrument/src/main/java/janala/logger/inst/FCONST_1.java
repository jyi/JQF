package janala.logger.inst;

public class FCONST_1 extends Instruction {
  public FCONST_1(int iid, int mid) {
    super(iid, mid);
  }

    public FCONST_1(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitFCONST_1(this);
  }

  @Override
  public String toString() {
    return "FCONST_1 iid=" + iid + " mid=" + mid;
  }
}
