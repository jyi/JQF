package janala.logger.inst;

public class BASTORE extends Instruction {
  public BASTORE(int iid, int mid) {
    super(iid, mid);
  }

    public BASTORE(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitBASTORE(this);
  }

  @Override
  public String toString() {
    return "BASTORE iid=" + iid + " mid=" + mid;
  }
}
