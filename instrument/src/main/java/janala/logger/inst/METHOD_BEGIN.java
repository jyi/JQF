package janala.logger.inst;

import janala.instrument.Method;

public class METHOD_BEGIN extends Instruction implements MemberRef {
  public final String fileName;
  public final String owner;
  public final String name;
  public final String desc;

  public METHOD_BEGIN(String fileName, String owner, String name, String desc) {
    super(-1, -1);
    this.fileName = fileName;
    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  public METHOD_BEGIN(String owner, String name, String desc) {
    super(-1, -1);
    this.fileName = null;
    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  public void visit(IVisitor visitor) {
    visitor.visitMETHOD_BEGIN(this);
  }

  @Override
  public String toString() {
    return "METHOD_BEGIN"
            + " owner="
            + owner
            + " name="
            + name
            + " desc="
            + desc;
  }

  public String getFileName() { return fileName; }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDesc() {
    return desc;
  }

  public Method getMethod() {
    return new Method(getOwner(), getName(), getDesc());
  }
}
