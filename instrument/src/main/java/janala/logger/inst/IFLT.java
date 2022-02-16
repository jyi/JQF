package janala.logger.inst;

public class IFLT extends Instruction implements ConditionalBranch {
  int label;

  public IFLT(String fileName, int iid, int mid, int label) {
    super(iid, mid);
    this.label = label;
    this.fileName = fileName;
  }

  public void visit(IVisitor visitor) {
    visitor.visitIFLT(this);
  }

  @Override
  public String toString() {
    return "IFLT iid=" + iid + " mid=" + mid + " label=" + Integer.toString(label);
  }
}
