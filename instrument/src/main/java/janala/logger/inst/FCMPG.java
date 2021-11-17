package janala.logger.inst;

public class FCMPG extends Instruction {
  public FCMPG(int iid, int mid) {
    super(iid, mid);
  }

    public FCMPG(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitFCMPG(this);
  }

  @Override
  public String toString() {
    return "FCMPG iid=" + iid + " mid=" + mid;
  }
}
