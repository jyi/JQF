package janala.logger.inst;

public class CASTORE extends Instruction {
  public CASTORE(int iid, int mid) {
    super(iid, mid);
  }

    public CASTORE(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitCASTORE(this);
  }

  @Override
  public String toString() {
    return "CASTORE iid=" + iid + " mid=" + mid;
  }
}
