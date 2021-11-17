package janala.logger.inst;

public class DRETURN extends Instruction {
  public DRETURN(int iid, int mid) {
    super(iid, mid);
  }

    public DRETURN(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitDRETURN(this);
  }

  @Override
  public String toString() {
    return "DRETURN iid=" + iid + " mid=" + mid;
  }
}
