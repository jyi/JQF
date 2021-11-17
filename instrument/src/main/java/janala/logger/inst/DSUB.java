package janala.logger.inst;

public class DSUB extends Instruction {
  public DSUB(int iid, int mid) {
    super(iid, mid);
  }

    public DSUB(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitDSUB(this);
  }

  @Override
  public String toString() {
    return "DSUB iid=" + iid + " mid=" + mid;
  }
}
