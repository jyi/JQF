package janala.logger.inst;

public class POP extends Instruction {
  public POP(int iid, int mid) {
    super(iid, mid);
  }

    public POP(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitPOP(this);
  }

  @Override
  public String toString() {
    return "POP iid=" + iid + " mid=" + mid;
  }
}
