package janala.logger.inst;

public class IF_ICMPGT extends Instruction implements ConditionalBranch {
  int label;

  public IF_ICMPGT(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIF_ICMPGT(this);
  }

  @Override
  public String toString() {
    return "IF_ICMPGT iid=" + iid + " mid=" + mid + " label=" + Integer.toString(label);
  }
}
