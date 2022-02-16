package janala.logger.inst;

public class IF_ICMPGE extends Instruction implements ConditionalBranch {
  int label;

  public IF_ICMPGE(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIF_ICMPGE(this);
  }

  @Override
  public String toString() {
    return "IF_ICMPGE iid=" + iid + " mid=" + mid + " label=" + Integer.toString(label);
  }
}
