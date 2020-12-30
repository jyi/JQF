package janala.logger.inst;

import janala.instrument.Method;

public class INVOKEMETHOD_END extends Instruction {

  public final String owner;
  public final String name;
  public final String desc;

  public INVOKEMETHOD_END(String owner, String name, String desc) {
    super(-1, -1);
    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  public void visit(IVisitor visitor) {
    visitor.visitINVOKEMETHOD_END(this);
  }

  @Override
  public String toString() {
    return "INVOKEMETHOD_END"
            + " owner="
            + owner
            + " name="
            + name
            + " desc="
            + desc;
  }

  public String getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public String getDesc() {
    return desc;
  }

  public Method getMethod() {
    return new Method(getOwner(), getName(), getDesc());
  }
}
