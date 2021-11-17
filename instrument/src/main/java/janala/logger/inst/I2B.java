package janala.logger.inst;

public class I2B extends Instruction {
  public I2B(int iid, int mid) {
    super(iid, mid);
  }

    public I2B(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitI2B(this);
  }

  @Override
  public String toString() {
    return "I2B iid=" + iid + " mid=" + mid;
  }
}
