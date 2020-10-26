package edu.berkeley.cs.jqf.fuzz.javassist;

import edu.berkeley.cs.jqf.fuzz.soot.examples.FizzBuzz;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JavassistTest {

    @Test
    public void lineNumber() throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        String className = FizzBuzz.class.getName();
        System.out.println("className: " + className);
        CtClass cc = pool.get(className);
        for (CtMethod method: cc.getDeclaredMethods()) {
            MethodInfo methodInfo = method.getMethodInfo();
            System.out.println(method.getName() + ": [" + methodInfo.getLineNumber(0) + ", " +
                    methodInfo.getLineNumber(methodInfo.getCodeAttribute().getCodeLength()) + "]");
        }
    }
}
