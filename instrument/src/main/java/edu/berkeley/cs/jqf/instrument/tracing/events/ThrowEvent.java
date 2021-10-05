package edu.berkeley.cs.jqf.instrument.tracing.events;

import janala.logger.inst.MemberRef;

public class ThrowEvent extends TraceEvent {
    public ThrowEvent(int iid, MemberRef containingMethod, int lineNumber) {
        super(iid, containingMethod, lineNumber);
    }

    @Override
    public String toString() {
        return String.format("RET(%d,%d)", iid, lineNumber);
    }

    @Override
    public void applyVisitor(TraceEventVisitor v) {
        v.visitThrowEvent(this);
    }
}
