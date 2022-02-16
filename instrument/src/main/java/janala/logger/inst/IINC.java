package janala.logger.inst;

public class IINC extends Instruction {
  public int var;
  public int increment;

  public IINC(int iid, int mid, int var, int increment) {
    super(iid, mid);
    this.var = var;
    this.increment = increment;
  }

  public IINC(String fileName, int iid, int mid, int var, int increment) {
    super(fileName, iid, mid);
    this.var = var;
    this.increment = increment;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIINC(this);
  }

  @Override
  public String toString() {
    return "IINC iid=" + iid + " mid=" + mid + " var=" + Integer.toString(var) + " increment=" + increment;
  }
}
