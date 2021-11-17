package janala.logger.inst;

public class LSHR extends Instruction {
  public LSHR(int iid, int mid) {
    super(iid, mid);
  }

    public LSHR(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLSHR(this);
  }

  @Override
  public String toString() {
    return "LSHR iid=" + iid + " mid=" + mid;
  }
}
