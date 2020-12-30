package edu.berkeley.cs.jqf.fuzz.util;

import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.reach.Target;
import edu.berkeley.cs.jqf.instrument.tracing.events.TargetHitEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEventVisitor;
import kr.ac.unist.cse.jqf.aspect.DumpUtil;
import kr.ac.unist.cse.jqf.aspect.MethodInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TargetCoverage implements TraceEventVisitor {

    private static TargetCoverage singleton = new TargetCoverage();

    private List<Target> covered = new ArrayList<>();

    protected final boolean verbose = Boolean.getBoolean("jqf.ei.verbose");

    /** The file where log data is written. */
    protected File logFile;

    public static boolean isTargetHit() {
        return DumpUtil.isTheTargetHit();
    }

    public void handleEvent(TraceEvent e) {
        e.applyVisitor(this);
    }

    private TargetCoverage() {
        File outputDirectory = new File(System.getProperty("jqf.ei.outputDirectory"));
        this.logFile = new File(outputDirectory, "fuzz.log");
    }

    public static TargetCoverage getTargetCoverage() {
        return singleton;
    }

    public void extractCallers() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException re) {
            StackTraceElement[] stackTrace = re.getStackTrace();
            List<MethodInfo> callChainToTargetMethod = new ArrayList();
            String targetInfo = System.getProperty("jqf.ei.targets");
            List<String> targets = Arrays.asList(targetInfo.substring(1, targetInfo.length() - 1).split(", "));
            List<String> packages = new ArrayList<>();
            for (String target : targets) {
                String pkg = target.substring(0, target.lastIndexOf('/')).replace('/', '.');
                packages.add(pkg);
            }

            for (StackTraceElement method : stackTrace) {
                String className = method.getClassName();
                if (packages.contains(className.substring(0, className.lastIndexOf('.')))
                        && !className.contains("JQF"))
                    if (!callChainToTargetMethod.contains(new MethodInfo(method.getClassName(), method.getMethodName())))
                        callChainToTargetMethod.add(new MethodInfo(method.getClassName(), method.getMethodName()));
            }
            DumpUtil.setCallerChainToTargetMethod(callChainToTargetMethod);
        }
    }

    @Override
    public void visitTargetEvent(TargetHitEvent e) {
        if (!DumpUtil.isTheTargetHit()) {
            infoLog("Target is hit at %s: %d", e.getFileName(), e.getLineNumber());
            covered.add(new Target(e.getFileName(), e.getLineNumber()));
            extractCallers();
            DumpUtil.insideTargetMethod(true);
            DumpUtil.setTargetHit(true);
        }
    }

    public List<Target> getCoveredTargets() {
        return covered;
    }

    public void clear() {
        covered.clear();
    }

    /** Writes a line of text to the log file. */
    protected void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            if (logFile != null) {
                appendLineToFile(logFile, line);
            } else {
                System.err.println(line);
            }
        }
    }

    /** Writes a line of text to a given log file. */
    protected void appendLineToFile(File file, String line) throws GuidanceException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println(line);
        } catch (IOException e) {
            throw new GuidanceException(e);
        }
    }
}
