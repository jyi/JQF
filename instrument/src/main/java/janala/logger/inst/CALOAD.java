package janala.logger.inst;

public class CALOAD extends Instruction {
  public CALOAD(int iid, int mid) {
    super(iid, mid);
  }

    public CALOAD(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitCALOAD(this);
  }

  @Override
  public String toString() {
    return "CALOAD iid=" + iid + " mid=" + mid;
  }
}
