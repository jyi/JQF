package janala.logger.inst;

public class LADD extends Instruction {
  public LADD(int iid, int mid) {
    super(iid, mid);
  }

    public LADD(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLADD(this);
  }

  @Override
  public String toString() {
    return "LADD iid=" + iid + " mid=" + mid;
  }
}
