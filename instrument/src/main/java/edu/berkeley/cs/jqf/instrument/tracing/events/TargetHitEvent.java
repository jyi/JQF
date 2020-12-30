package edu.berkeley.cs.jqf.instrument.tracing.events;

import janala.logger.inst.MemberRef;

public class TargetHitEvent extends TraceEvent {

    private String fileName;

    public TargetHitEvent(int iid, MemberRef currentMethod, int lineNum, String fileName) {
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
