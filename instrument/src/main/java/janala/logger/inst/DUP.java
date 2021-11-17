package janala.logger.inst;

public class DUP extends Instruction {
  public DUP(int iid, int mid) {
    super(iid, mid);
  }

    public DUP(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitDUP(this);
  }

  @Override
  public String toString() {
    return "DUP iid=" + iid + " mid=" + mid;
  }
}
