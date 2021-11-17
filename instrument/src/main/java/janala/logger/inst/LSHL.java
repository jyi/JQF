package janala.logger.inst;

public class LSHL extends Instruction {
  public LSHL(int iid, int mid) {
    super(iid, mid);
  }

    public LSHL(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLSHL(this);
  }

  @Override
  public String toString() {
    return "LSHL iid=" + iid + " mid=" + mid;
  }
}
