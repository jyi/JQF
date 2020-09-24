package edu.berkeley.cs.jqf.fuzz.util;

import edu.berkeley.cs.jqf.fuzz.reach.Target;
import edu.berkeley.cs.jqf.instrument.tracing.events.TargetEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEventVisitor;
import kr.ac.unist.cse.jqf.aspect.DumpUtil;
import kr.ac.unist.cse.jqf.aspect.MethodInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TargetCoverage implements TraceEventVisitor {

    private static TargetCoverage singleton = new TargetCoverage();

    private List<Target> covered = new ArrayList<>();

    protected final boolean verbose = Boolean.getBoolean("jqf.ei.verbose");

    private static boolean isTargetHit = false;

    public static boolean isTargetHit() {
        return isTargetHit;
    }

    public static void resetHit() {
        TargetCoverage.isTargetHit = false;
    }

    public void handleEvent(TraceEvent e) {
        e.applyVisitor(this);
    }

    private TargetCoverage() {
    }

    public static TargetCoverage getTargetCoverage() {
        return singleton;
    }

    public void extractCallers(){
        try {
            throw new RuntimeException();
        } catch (RuntimeException re) {
            StackTraceElement[] stackTrace = re.getStackTrace();
            List<MethodInfo> callers = new ArrayList();
            String s = System.getProperty("jqf.ei.targets");
            List<String> temp = Arrays.asList(s.substring(1, s.length() - 1).split(", "));
            List<String> targets = new ArrayList<>();
            for (String t: temp){
                String z = t.substring(0,t.lastIndexOf('/'));
                z  = z.replace('/','.');
                targets.add(z);

            }
            for (StackTraceElement method: stackTrace){
                String className= method.getClassName();
                if (targets.contains(className.substring(0,className.lastIndexOf('.')))
                        && !className.contains("JQF"))
                    callers.add(new MethodInfo(method.getClassName(), method.getMethodName()));
            }
            DumpUtil.setMethods(callers);
        }
    }
    @Override
    public void visitTargetEvent(TargetEvent e) {
        infoLog("Target is hit at %s: %d", e.getFileName(), e.getLineNumber());
        isTargetHit = true;
        covered.add(new Target(e.getFileName(), e.getLineNumber()));
        extractCallers();
        DumpUtil.setTargetHit(true);
    }

    public List<Target> getCoveredTargets() {
        return covered;
    }

    public void clear() {
        covered.clear();
    }

    protected void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            System.err.println(line);
        }
    }
}
