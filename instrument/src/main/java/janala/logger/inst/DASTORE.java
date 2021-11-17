package janala.logger.inst;

public class DASTORE extends Instruction {
  public DASTORE(int iid, int mid) {
    super(iid, mid);
  }

    public DASTORE(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitDASTORE(this);
  }

  @Override
  public String toString() {
    return "DASTORE iid=" + iid + " mid=" + mid;
  }
}
