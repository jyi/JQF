package janala.logger.inst;

/** A special marker instruction indicating that the method
 * is exiting abruptly due to an exception being thrown, and thus
 * the stack frame must be destroyed.
  */
public class METHOD_THROW extends Instruction {

  public final String fileName;
  public final String owner;
  public final String name;
  public final String desc;

  public METHOD_THROW() {
    super(-1, -1);
    this.fileName = "";
    this.owner = "";
    this.name = "";
    this.desc = "";
  }

  public METHOD_THROW(String fileName, String owner, String name, String desc) {
    super(-1, -1);
    this.fileName = fileName;
    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  public void visit(IVisitor visitor) {
    visitor.visitMETHOD_THROW(this);
  }

  @Override
  public String toString() {
    return "METHOD_THROW";
  }
}
