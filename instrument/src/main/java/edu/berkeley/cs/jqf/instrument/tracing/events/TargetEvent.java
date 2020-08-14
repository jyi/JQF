package edu.berkeley.cs.jqf.instrument.tracing.events;

import edu.berkeley.cs.jqf.instrument.tracing.Target;
import janala.logger.inst.MemberRef;

public class TargetEvent extends TraceEvent {

    private String fileName;

    public TargetEvent(int iid, MemberRef currentMethod, int lineNum, String fileName) {
        super(iid, currentMethod, lineNum);
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public void applyVisitor(TraceEventVisitor v) {
        v.visitTargetEvent(this);
    }
}
