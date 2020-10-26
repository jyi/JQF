package edu.berkeley.cs.jqf.fuzz.soot;

import edu.berkeley.cs.jqf.fuzz.soot.examples.FizzBuzz;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JIfStmt;
import soot.options.Options;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HelloSoot {

    public static String sourceDirectory = FizzBuzz.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    public static String clsName = FizzBuzz.class.getName();
    public static String methodName = "printFizzBuzz";

    public static void setupSoot() {
        G.reset();

        List<String> process_dir = new ArrayList<>();
        process_dir.add(sourceDirectory);
        Options.v().set_process_dir(process_dir);

        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);

        // Options.v().set_soot_classpath(System.getProperty("java.class.path") + File.pathSeparator + sourceDirectory);
        // SootClass sc = Scene.v().loadClassAndSupport(clsName);
        // sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        Scene scene = Scene.v();
    }

    public static void main(String[] args) {
        setupSoot();

        // Retrieve printFizzBuzz's body
        SootClass mainClass = Scene.v().getSootClass(clsName);
        SootMethod sm = mainClass.getMethodByName(methodName);
        JimpleBody body = (JimpleBody) sm.retrieveActiveBody();

        // Print some information about printFizzBuzz
        System.out.println("Method Signature: " + sm.getSignature());
        System.out.println("--------------");
        System.out.println("Argument(s):");
        for (Local l : body.getParameterLocals()) {
            System.out.println(l.getName() + " : " + l.getType());
        }
        System.out.println("--------------");
        System.out.println("This: " + body.getThisLocal());
        System.out.println("--------------");
        System.out.println("Units:");
        int c = 1;
        for (Unit u : body.getUnits()) {
            System.out.println("(" + c + ") " + u.toString());
            c++;
        }
        System.out.println("--------------");

        // Print statements that have branch conditions
        System.out.println("Branch Statements:");
        for (Unit u : body.getUnits()) {
            if (u instanceof JIfStmt)
                System.out.println(u.toString());
        }

        // Draw the control-flow graph of the method if 'draw' is provided in arguments
        boolean drawGraph = false;
        if (args.length > 0 && args[0].equals("draw"))
            drawGraph = true;
        if (drawGraph) {
            UnitGraph ug = new ClassicCompleteUnitGraph(sm.getActiveBody());
            Visualizer.v().addUnitGraph(ug);
            Visualizer.v().draw();
        }
    }
}
