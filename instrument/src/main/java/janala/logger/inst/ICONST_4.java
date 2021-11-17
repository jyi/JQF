package janala.logger.inst;

public class ICONST_4 extends Instruction {
  public ICONST_4(int iid, int mid) {
    super(iid, mid);
  }

    public ICONST_4(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitICONST_4(this);
  }

  @Override
  public String toString() {
    return "ICONST_4 iid=" + iid + " mid=" + mid;
  }
}
