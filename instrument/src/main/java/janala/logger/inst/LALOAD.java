package janala.logger.inst;

public class LALOAD extends Instruction {
  public LALOAD(int iid, int mid) {
    super(iid, mid);
  }

    public LALOAD(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLALOAD(this);
  }

  @Override
  public String toString() {
    return "LALOAD iid=" + iid + " mid=" + mid;
  }
}
