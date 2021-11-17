package janala.logger.inst;

public class MONITOREXIT extends Instruction {
  public MONITOREXIT(int iid, int mid) {
    super(iid, mid);
  }

    public MONITOREXIT(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitMONITOREXIT(this);
  }

  @Override
  public String toString() {
    return "MONITOREXIT iid=" + iid + " mid=" + mid;
  }
}
