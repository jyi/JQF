package edu.berkeley.cs.jqf.fuzz.soot;

import edu.berkeley.cs.jqf.fuzz.soot.examples.Circle;
import edu.berkeley.cs.jqf.fuzz.soot.examples.FizzBuzz;
import org.junit.Ignore;
import org.junit.Test;
import soot.Body;
import soot.JastAddJ.Opt;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.JimpleBody;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JIfStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class SootTest {

    @Test
    public void testMethodsExist() {
        setupSoot(FizzBuzz.class.getProtectionDomain().getCodeSource().getLocation().getPath(),FizzBuzz.class.getName());
        SootClass aClass = Scene.v().getSootClass(FizzBuzz.class.getName());
        assertFalse(aClass.isPhantom());
        SootMethod printFizzBuzzMethod = aClass.getMethodByName("printFizzBuzz");
        int startLine = printFizzBuzzMethod.getJavaSourceStartLineNumber();
        System.out.println("startLine: " + startLine);
        assertFalse(printFizzBuzzMethod.isPhantom());
        Body body = printFizzBuzzMethod.retrieveActiveBody();
        assertTrue(body instanceof JimpleBody);
    }
    @Test
    public void CFGTest() {
        setupSoot(Circle.class.getProtectionDomain().getCodeSource().getLocation().getPath(),Circle.class.getName());
        SootClass circleClass = Scene.v().getSootClass(Circle.class.getName());
        ClassicCompleteUnitGraph classicCompleteUnitGraph = new ClassicCompleteUnitGraph(circleClass.getMethods().get(3).retrieveActiveBody());
        System.out.println(classicCompleteUnitGraph.toString());
    }
    @Test
    public void callGraphTest() {
        setupSoot(Circle.class.getProtectionDomain().getCodeSource().getLocation().getPath(),Circle.class.getName());
        SootClass circleClass = Scene.v().getSootClass(Circle.class.getName());
        // SootMethod areaMethod = circleClass.getMethod("int area(boolean)");
        CallGraph callGraph = Scene.v().getCallGraph();
        System.out.println(callGraph.toString());
        Iterator<MethodOrMethodContext> srcMethods = callGraph.sourceMethods();
        List<SootMethod> methods = circleClass.getMethods();
        while (srcMethods.hasNext()) {
            SootMethod srcMethod = srcMethods.next().method();
            if (!srcMethod.isJavaLibraryMethod()) {
//                System.out.println(srcMethod);
            }
        }
        // assertTrue(Scene.v().getCallGraph().edgesOutOf(areaMethod).hasNext());
    }

    private static void setupSoot(String sourceDirectory,String cls) {
        G.reset();

//        List<String> process_dir = new ArrayList<>();
//        process_dir.add(sourceDirectory);
//        Options.v().set_process_dir(process_dir);
        Options.v().set_keep_line_number(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Scene.v().loadClassAndSupport(cls);
        String soot_cp = Options.v().soot_classpath();
        System.out.println("soot_cp: " + soot_cp);
        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();
    }
}
