package janala.logger.inst;

public class FCMPL extends Instruction {
  public FCMPL(int iid, int mid) {
    super(iid, mid);
  }

    public FCMPL(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitFCMPL(this);
  }

  @Override
  public String toString() {
    return "FCMPL iid=" + iid + " mid=" + mid;
  }
}
