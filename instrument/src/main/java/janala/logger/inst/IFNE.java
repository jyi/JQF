package janala.logger.inst;

public class IFNE extends Instruction implements ConditionalBranch {
  int label;

  public IFNE(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIFNE(this);
  }

  @Override
  public String toString() {
    return "IFNE iid=" + iid + " mid=" + mid + " label=" + Integer.toString(label);
  }
}
