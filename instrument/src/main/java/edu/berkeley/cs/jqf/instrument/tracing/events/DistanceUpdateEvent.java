package edu.berkeley.cs.jqf.instrument.tracing.events;

import edu.berkeley.cs.jqf.instrument.tracing.Target;
import janala.logger.inst.MemberRef;

public class DistanceUpdateEvent extends TraceEvent {

    private final Target target;
    private final int distToTarget;

    public DistanceUpdateEvent(int iid, MemberRef method, int lineNumber,
                               Target target, int distToTarget) {
        super(iid, method, lineNumber);
        this.target = target;
        this.distToTarget = distToTarget;
    }

    public Target getTarget() {
        return target;
    }

    public int getDistToTarget() {
        return distToTarget;
    }

    @Override
    public void applyVisitor(TraceEventVisitor v) {
        v.visitDistanceUpdateEvent(this);
    }
}
