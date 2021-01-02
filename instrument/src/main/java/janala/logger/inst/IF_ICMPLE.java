package janala.logger.inst;

public class IF_ICMPLE extends Instruction implements ConditionalBranch {
  int label;

  public IF_ICMPLE(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIF_ICMPLE(this);
  }

  @Override
  public String toString() {
    return "IF_ICMPLE iid=" + iid + " mid=" + mid + " label=" + label;
  }
}
