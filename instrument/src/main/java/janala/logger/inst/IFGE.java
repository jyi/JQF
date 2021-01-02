package janala.logger.inst;

public class IFGE extends Instruction implements ConditionalBranch {
  int label;

  public IFGE(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIFGE(this);
  }

  @Override
  public String toString() {
    return "IFGE iid=" + iid + " mid=" + mid + " label=" + label;
  }
}
