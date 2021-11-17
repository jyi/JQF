package janala.logger.inst;

public class DALOAD extends Instruction {
  public DALOAD(int iid, int mid) {
    super(iid, mid);
  }

    public DALOAD(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitDALOAD(this);
  }

  @Override
  public String toString() {
    return "DALOAD iid=" + iid + " mid=" + mid;
  }
}
