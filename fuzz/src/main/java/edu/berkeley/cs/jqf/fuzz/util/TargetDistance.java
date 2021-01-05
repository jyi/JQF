package edu.berkeley.cs.jqf.fuzz.util;

import edu.berkeley.cs.jqf.instrument.tracing.Target;
import edu.berkeley.cs.jqf.instrument.tracing.events.DistanceUpdateEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEventVisitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TargetDistance implements TraceEventVisitor {

    private static TargetDistance singleton = null;
    private Map<Target, Integer> distMap = new HashMap<>();

    public static TargetDistance getSingleton() {
        if (singleton == null) {
            singleton = new TargetDistance();
        }
        return singleton;
    }

    private TargetDistance() {
        for (Target target: Target.allTargets) {
            distMap.put(target, Integer.MAX_VALUE);
        }
    }

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

    public double getDistance() {
//        double mean = harmonicMean(this.distMap.values());
//        assert mean != Double.NaN;
//        return mean;

        return this.distMap.values().stream().min(Integer::compare).get();
    }

    private double harmonicMean(Collection<Integer> numbers) {
        if (numbers.isEmpty() || numbers.contains(0.0)) return Double.NaN;
        double mean = 0.0;
        for (int number : numbers) {
            mean += (1.0 / number);
        }
        return numbers.size() / mean;
    }

}
