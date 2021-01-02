package janala.logger.inst;

public class IFNONNULL extends Instruction implements ConditionalBranch {
  int label;

  public IFNONNULL(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIFNONNULL(this);
  }

  @Override
  public String toString() {
    return "IFNONNULL iid=" + iid + " mid=" + mid + " label=" + label;
  }
}
