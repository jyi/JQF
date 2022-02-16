package janala.logger.inst;

public class DLOAD extends Instruction {
  public int var;

  public DLOAD(int iid, int mid, int var) {
    super(iid, mid);
    this.var = var;
  }

  public DLOAD(String fileName, int iid, int mid, int var) {
    super(fileName, iid, mid);
    this.var = var;
  }

  public void visit(IVisitor visitor) {
    visitor.visitDLOAD(this);
  }

  @Override
  public String toString() {
    return "DLOAD iid=" + iid + " mid=" + mid + " var=" + Integer.toString(var);
  }
}
