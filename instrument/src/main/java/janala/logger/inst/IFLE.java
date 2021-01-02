package janala.logger.inst;

public class IFLE extends Instruction implements ConditionalBranch {
  int label;

  public IFLE(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIFLE(this);
  }

  @Override
  public String toString() {
    return "IFLE iid=" + iid + " mid=" + mid + " label=" + label;
  }
}
