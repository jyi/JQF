package janala.logger.inst;

public class IF_ACMPEQ extends Instruction implements ConditionalBranch {
  int label;

  public IF_ACMPEQ(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIF_ACMPEQ(this);
  }

  @Override
  public String toString() {
    return "IF_ACMPEQ iid=" + iid + " mid=" + mid + " label=" + Integer.toString(label);
  }
}
