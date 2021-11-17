package janala.logger.inst;

public class SWAP extends Instruction {
  public SWAP(int iid, int mid) {
    super(iid, mid);
  }

    public SWAP(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitSWAP(this);
  }

  @Override
  public String toString() {
    return "SWAP iid=" + iid + " mid=" + mid;
  }
}
