package janala.logger.inst;

public class I2S extends Instruction {
  public I2S(int iid, int mid) {
    super(iid, mid);
  }

    public I2S(String fileName, int iid, int mid) {
        super(fileName, iid, mid);
    }

    public void visit(IVisitor visitor) {
    visitor.visitI2S(this);
  }

  @Override
  public String toString() {
    return "I2S iid=" + iid + " mid=" + mid;
  }
}
