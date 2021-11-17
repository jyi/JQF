package janala.logger.inst;

public class LCONST_1 extends Instruction {
  public LCONST_1(int iid, int mid) {
    super(iid, mid);
  }

    public LCONST_1(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLCONST_1(this);
  }

  @Override
  public String toString() {
    return "LCONST_1 iid=" + iid + " mid=" + mid;
  }
}
