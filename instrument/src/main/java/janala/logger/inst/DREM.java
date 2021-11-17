package janala.logger.inst;

public class DREM extends Instruction {
  public DREM(int iid, int mid) {
    super(iid, mid);
  }

    public DREM(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitDREM(this);
  }

  @Override
  public String toString() {
    return "DREM iid=" + iid + " mid=" + mid;
  }
}
