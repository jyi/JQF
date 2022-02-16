package janala.logger.inst;

public class IF_ICMPEQ extends Instruction implements ConditionalBranch {
  int label;

  public IF_ICMPEQ(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIF_ICMPEQ(this);
  }

  @Override
  public String toString() {
    return "IF_ICMPEQ iid=" + iid + " mid=" + mid + " label=" + Integer.toString(label);
  }
}
