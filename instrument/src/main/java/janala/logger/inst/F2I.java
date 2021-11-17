package janala.logger.inst;

public class F2I extends Instruction {
  public F2I(int iid, int mid) {
    super(iid, mid);
  }

    public F2I(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitF2I(this);
  }

  @Override
  public String toString() {
    return "F2I iid=" + iid + " mid=" + mid;
  }
}
