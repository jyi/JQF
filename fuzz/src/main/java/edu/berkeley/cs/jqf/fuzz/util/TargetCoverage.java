package edu.berkeley.cs.jqf.fuzz.util;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.reach.Target;
import edu.berkeley.cs.jqf.instrument.tracing.events.TargetEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEventVisitor;

import java.util.ArrayList;
import java.util.List;

public class TargetCoverage implements TraceEventVisitor {

    private List<Target> covered = new ArrayList<>();

    protected final boolean verbose = Boolean.getBoolean("jqf.ei.verbose");

    public void handleEvent(TraceEvent e) {
        e.applyVisitor(this);
    }

    private static TargetCoverage singleton = new TargetCoverage();

    private TargetCoverage() {
    }

    public static TargetCoverage getTargetCoverage() {
        return singleton;
    }

    @Override
    public void visitTargetEvent(TargetEvent e) {
        infoLog("Target is hit at %s: %d", e.getFileName(), e.getLineNumber());
        covered.add(new Target(e.getFileName(), e.getLineNumber()));
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
