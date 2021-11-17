package janala.logger.inst;

public class SALOAD extends Instruction {
  public SALOAD(int iid, int mid) {
    super(iid, mid);
  }

    public SALOAD(String fileName, int iid, int mid) {
      super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitSALOAD(this);
  }

  @Override
  public String toString() {
    return "SALOAD iid=" + iid + " mid=" + mid;
  }
}
