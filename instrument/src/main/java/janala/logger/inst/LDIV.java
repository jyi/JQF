package janala.logger.inst;

public class LDIV extends Instruction {
  public LDIV(int iid, int mid) {
    super(iid, mid);
  }

    public LDIV(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLDIV(this);
  }

  @Override
  public String toString() {
    return "LDIV iid=" + iid + " mid=" + mid;
  }
}
