package edu.berkeley.cs.jqf.fuzz.util;

import edu.berkeley.cs.jqf.instrument.tracing.Target;
import edu.berkeley.cs.jqf.instrument.tracing.events.DistanceUpdateEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEventVisitor;

import java.util.HashMap;
import java.util.Map;

public class TargetDistance implements TraceEventVisitor {

    Map<Target, Integer> distMap = new HashMap<>();

    public void handleEvent(TraceEvent e) {
        e.applyVisitor(this);
    }

    @Override
    public void visitDistanceUpdateEvent(DistanceUpdateEvent e) {
        Integer curDist = distMap.get(e.getTarget());
        if (curDist == null) {
            distMap.put(e.getTarget(), e.getDistToTarget());
        } else if (curDist > e.getDistToTarget()) {
            distMap.put(e.getTarget(), e.getDistToTarget());
        }
    }
}
