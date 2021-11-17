package janala.logger.inst;

public class IRETURN extends Instruction {
  public IRETURN(int iid, int mid) {
    super(iid, mid);
  }

    public IRETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitIRETURN(this);
  }

  @Override
  public String toString() {
    return "IRETURN iid=" + iid + " mid=" + mid;
  }
}
