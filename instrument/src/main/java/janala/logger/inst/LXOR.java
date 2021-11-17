package janala.logger.inst;

public class LXOR extends Instruction {
  public LXOR(int iid, int mid) {
    super(iid, mid);
  }

    public LXOR(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitLXOR(this);
  }

  @Override
  public String toString() {
    return "LXOR iid=" + iid + " mid=" + mid;
  }
}
