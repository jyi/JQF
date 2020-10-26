package edu.berkeley.cs.jqf.fuzz.soot;

import edu.berkeley.cs.jqf.fuzz.soot.examples.FizzBuzz;
import org.junit.Ignore;
import org.junit.Test;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.JimpleBody;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class SootTest {

    @Test
    public void testMethodsExist() {
        HelloSoot.setupSoot();
        SootClass aClass = Scene.v().getSootClass(HelloSoot.clsName);
        assertFalse(aClass.isPhantom());
        SootMethod printFizzBuzzMethod = aClass.getMethodByName(HelloSoot.methodName);
        int startLine = printFizzBuzzMethod.getJavaSourceStartLineNumber();
        System.out.println("startLine: " + startLine);
        // printFizzBuzzMethod.getActiveBody().getUnits();
        assertFalse(printFizzBuzzMethod.isPhantom());
        Body body = printFizzBuzzMethod.retrieveActiveBody();
        assertTrue(body instanceof JimpleBody);
    }

    @Test
    public void sootTest() {

    }
}
