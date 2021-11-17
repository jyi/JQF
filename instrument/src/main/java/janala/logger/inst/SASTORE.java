package janala.logger.inst;

public class SASTORE extends Instruction {
  public SASTORE(int iid, int mid) {
    super(iid, mid);
  }

    public SASTORE(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitSASTORE(this);
  }

  @Override
  public String toString() {
    return "SASTORE iid=" + iid + " mid=" + mid;
  }
}
