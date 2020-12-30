
package janala.instrument;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;


public class SnoopInstructionClassAdapter extends ClassVisitor {
  private final String className;
  private String superName;
  private String filePath;

  public SnoopInstructionClassAdapter(ClassVisitor cv, String className) {
    super(Opcodes.ASM5, cv);
    this.className = className;
  }

  @Override
  public void visit(int version,
                    int access,
                    String name,
                    String signature,
                    String superName,
                    String[] interfaces) {
    assert name.equals(this.className);
    this.superName = superName;
    cv.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public void visitSource(final String source, final String debug) {
    this.filePath = constructFilePath(source);
    if (cv != null) {
      cv.visitSource(source, debug);
    }
  }

  private String constructFilePath(String source) {
    StringBuffer sb = new StringBuffer();
    String[] cmpts = this.className.split(File.separator);
    for (int i = 0; i < cmpts.length - 1; i++) {
      sb.append(cmpts[i]);
      sb.append(File.separator);
    }
    sb.append(source);
    return sb.toString();
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, 
      String signature, String[] exceptions) {
    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
    if (mv != null) {
      return new SnoopInstructionMethodAdapter(mv, filePath, className, name, desc, superName,
          GlobalStateForInstrumentation.instance);
    }
    return null;
  }
}
