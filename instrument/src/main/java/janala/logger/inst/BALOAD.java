package janala.logger.inst;

public class BALOAD extends Instruction {
  public BALOAD(int iid, int mid) {
    super(iid, mid);
  }

    public BALOAD(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitBALOAD(this);
  }

  @Override
  public String toString() {
    return "BALOAD iid=" + iid + " mid=" + mid;
  }
}
