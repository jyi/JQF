package janala.logger.inst;

public class IUSHR extends Instruction {
  public IUSHR(int iid, int mid) {
    super(iid, mid);
  }

    public IUSHR(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitIUSHR(this);
  }

  @Override
  public String toString() {
    return "IUSHR iid=" + iid + " mid=" + mid;
  }
}
