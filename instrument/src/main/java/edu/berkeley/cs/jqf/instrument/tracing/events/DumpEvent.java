package edu.berkeley.cs.jqf.instrument.tracing.events;

import janala.logger.inst.MemberRef;

public class DumpEvent extends TraceEvent{

    public DumpEvent(int iid, MemberRef method, int lineNumber) {

        super(iid, method, lineNumber);
    }

    @Override
    public void applyVisitor(TraceEventVisitor v) {

    }
}
