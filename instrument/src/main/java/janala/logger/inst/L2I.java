package janala.logger.inst;

public class L2I extends Instruction {
  public L2I(int iid, int mid) {
    super(iid, mid);
  }

    public L2I(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitL2I(this);
  }

  @Override
  public String toString() {
    return "L2I iid=" + iid + " mid=" + mid;
  }
}
