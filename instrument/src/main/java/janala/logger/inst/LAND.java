package janala.logger.inst;

public class LAND extends Instruction {
  public LAND(int iid, int mid) {
    super(iid, mid);
  }

    public LAND(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLAND(this);
  }

  @Override
  public String toString() {
    return "LAND iid=" + iid + " mid=" + mid;
  }
}
