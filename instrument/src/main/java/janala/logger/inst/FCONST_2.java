package janala.logger.inst;

public class FCONST_2 extends Instruction {
  public FCONST_2(int iid, int mid) {
    super(iid, mid);
  }

    public FCONST_2(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitFCONST_2(this);
  }

  @Override
  public String toString() {
    return "FCONST_2 iid=" + iid + " mid=" + mid;
  }
}
