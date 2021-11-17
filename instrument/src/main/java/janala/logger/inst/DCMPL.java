package janala.logger.inst;

public class DCMPL extends Instruction {
  public DCMPL(int iid, int mid) {
    super(iid, mid);
  }

    public DCMPL(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitDCMPL(this);
  }

  @Override
  public String toString() {
    return "DCMPL iid=" + iid + " mid=" + mid;
  }
}
