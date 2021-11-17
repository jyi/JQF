package janala.logger.inst;

public class LUSHR extends Instruction {
  public LUSHR(int iid, int mid) {
    super(iid, mid);
  }

    public LUSHR(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLUSHR(this);
  }

  @Override
  public String toString() {
    return "LUSHR iid=" + iid + " mid=" + mid;
  }
}
