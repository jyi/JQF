/*
 * Copyright (c) 2017-2018 The Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs.jqf.fuzz.util;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;


import com.pholser.junit.quickcheck.Pair;
import edu.berkeley.cs.jqf.fuzz.reach.Target;
import edu.berkeley.cs.jqf.instrument.tracing.events.*;
import com.github.gumtreediff.gen.jdt.JdtTreeMapping;
/**
 * Utility class to collect branch and function coverage
 *
 * @author Rohan Padhye
 */
public class Coverage implements TraceEventVisitor {

    /** The size of the coverage map. */
    private final int COVERAGE_MAP_SIZE = (1 << 16) - 1; // Minus one to reduce collisions

    /** The coverage counts for each edge. */
    private final Counter counter = new NonZeroCachingCounter(COVERAGE_MAP_SIZE);

    private HashMap<Integer, HashSet<EventInfo>> hashToEventInfoMap = new HashMap<>();
    private HashMap<String, HashSet<Integer>> eventInfoToHashMap = new HashMap<>();

    /** Creates a new coverage map. */
    public Coverage() {

    }

    /**
     * Creates a copy of an existing coverage map.
     *
     * @param that the coverage map to copy
     */
    public Coverage(Coverage that) {
        for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
            this.counter.setAtIndex(idx, that.counter.getAtIndex(idx));
        }
    }

    /**
     * Returns the size of the coverage map.
     *
     * @return the size of the coverage map
     */
    public int size() {
        return COVERAGE_MAP_SIZE;
    }

    /**
     * Updates coverage information based on emitted event.
     *
     * <p>This method updates its internal counters for branch and
     * call events.</p>
     *
     * @param e the event to be processed
     */
    public void handleEvent(TraceEvent e) {
        /*
        if(e.getFileName().equals("<unknown>") == false) {
            System.out.println("Coverage:handleEvent->" + e.getFileName() + " : " + e.getLineNumber());
        }
        */
        e.applyVisitor(this);
    }

    @Override
    public void visitBranchEvent(BranchEvent b) {
        int hashed = counter.getIdx1(b.getIid(), b.getArm());
        EventInfo ei = new EventInfo(b.getFileName(), b.getLineNumber(), hashed);
        if(eventInfoToHashMap.containsKey(ei.getFileAndLine())) {
            eventInfoToHashMap.get(ei.getFileAndLine()).add(hashed);
        } else {
            HashSet<Integer> hs = new HashSet<>(2);
            hs.add(hashed);
            eventInfoToHashMap.put(ei.getFileAndLine(), hs);
        }
        if(hashToEventInfoMap.containsKey(hashed)) {
            hashToEventInfoMap.get(hashed).add(ei);
        } else {
            HashSet<EventInfo> hs = new HashSet<EventInfo>(2);
            hs.add(ei);
            hashToEventInfoMap.put(hashed, hs);
        }
        counter.increment1(b.getIid(), b.getArm());
    }

    @Override
    public void visitCallEvent(CallEvent e) {
        int hashed = counter.getIdx(e.getIid());
        EventInfo ei = new EventInfo(e.getFileName(), e.getLineNumber(), hashed);
        if(eventInfoToHashMap.containsKey(ei.getFileAndLine())) {
            eventInfoToHashMap.get(ei.getFileAndLine()).add(hashed);
        } else {
            HashSet<Integer> hs = new HashSet<>(2);
            hs.add(hashed);
            eventInfoToHashMap.put(ei.getFileAndLine(), hs);
        }
        if(hashToEventInfoMap.containsKey(hashed)) {
            hashToEventInfoMap.get(hashed).add(ei);
        } else {
            HashSet<EventInfo> hs = new HashSet<EventInfo>(2);
            hs.add(ei);
            hashToEventInfoMap.put(hashed, hs);
        }
        counter.increment(e.getIid());
    }

    /**
     * Returns the number of edges covered.
     *
     * @return the number of edges with non-zero counts
     */
    public int getNonZeroCount() {
        return counter.getNonZeroSize();
    }

    /**
     * Returns a collection of branches that are covered.
     *
     * @return a collection of keys that are covered
     */
    public Collection<?> getCovered() {
        return counter.getNonZeroIndices();
    }

    /** Returns a set of edges in this coverage that don't exist in baseline */
    public Collection<?> computeNewCoverage(Coverage baseline) {
        Collection<Integer> newCoverage = new ArrayList<>();
        for (int idx : this.counter.getNonZeroIndices()) {
            if (baseline.counter.getAtIndex(idx) == 0) {
                newCoverage.add(idx);
            }
        }
        return newCoverage;

    }

    /**
     * Clears the coverage map.
     */
    public void clear() {
        this.counter.clear();
    }

    private static int[] HOB_CACHE = new int[1024];

    /** Computes the highest order bit */
    private static int computeHob(int num)
    {
        if (num == 0)
            return 0;

        int ret = 1;

        while ((num >>= 1) != 0)
            ret <<= 1;

        return ret;
    }

    /** Populates the HOB cache. */
    static {
        for (int i = 0; i < HOB_CACHE.length; i++) {
            HOB_CACHE[i] = computeHob(i);
        }
    }

    /** Returns the highest order bit (perhaps using the cache) */
    private static int hob(int num) {
        if (num < HOB_CACHE.length) {
            return HOB_CACHE[num];
        } else {
            return computeHob(num);
        }
    }


    /**
     * Updates this coverage with bits from the parameter.
     *
     * @param that the run coverage whose bits to OR
     *
     * @return <tt>true</tt> iff <tt>that</tt> is not a subset
     *         of <tt>this</tt>, causing <tt>this</tt> to change.
     */
    public boolean updateBits(Coverage that) {
        boolean changed = false;
        if (that.counter.hasNonZeros()) {
            for (int idx = 0; idx < COVERAGE_MAP_SIZE; idx++) {
                int before = this.counter.getAtIndex(idx);
                int after = before | hob(that.counter.getAtIndex(idx));
                if (after != before) {
                    this.counter.setAtIndex(idx, after);
                    changed = true;
                }
            }
        }
        return changed;
    }

    /** Returns a hash code of the edge counts in the coverage map. */
    @Override
    public int hashCode() {
        return Arrays.hashCode(counter.counts);
    }

    /** Returns a hash code of the list of edges that have been covered at least once. */
    public int nonZeroHashCode() {
        return counter.getNonZeroIndices().hashCode();
    }

    public Pair<Long, Long> getDistance(Coverage otherCoverage) {
        long diff = 0;
        long dist = 0;
        List<Integer> nonZeroIndices = counter.getNonZeroIndices();
        Counter otherCounter = otherCoverage.counter;
        List<Integer> nonZeroIndices2 = otherCounter.getNonZeroIndices();
        //System.out.println(eventInfoToHashMap.toString());
        //System.out.println("counter: " + hashToEventInfoMap.toString());
        //System.out.println("other: " + otherCoverage.hashToEventInfoMap.toString());
        // format = file_name:line_number
        HashMap<String, Integer> mapping = new HashMap<>();
        boolean valid = false;
        if(System.getProperty("jqf.ei.have_srcdir").equals("true")) {
            TargetCoverage targetCoverage = TargetCoverage.getTargetCoverage();
            List<Target> targets = targetCoverage.getCoveredTargets();
            for(Target target: targets) {
                Path srcFile = FileSystems.getDefault().getPath(System.getProperty("jqf.ei.SRCDIR_FOR_PATCH"), target.getFilename());
                Path dstFile = FileSystems.getDefault().getPath(System.getProperty("jqf.ei.SRCDIR_FOR_ORG"), target.getFilename());
                if((new File(srcFile.toString())).exists() && (new File(dstFile.toString())).exists()) {
                    try {
                        valid = true;
                        JdtTreeMapping jtm = new JdtTreeMapping();
                        mapping.putAll(jtm.mapping(srcFile, dstFile, target.getFilename()));
                    } catch (Exception e) {
                        valid = false;
                        break;
                    }
                }
            }
        }
        if (valid){
            for (int i: nonZeroIndices) {
                int count1 = counter.getAtIndex(i);
                int count2 = otherCounter.getAtIndex(i);
                HashSet<EventInfo> hs = hashToEventInfoMap.get(i);
                if(!hs.isEmpty()) {
                    for(EventInfo ei: hs) {
                        if(mapping.containsKey(ei.getFileAndLine())) {
                            int newLine = mapping.get(ei.getFileAndLine());
                            String str1 = ei.getFileAndLine();
                            String str2 = ei.filename + ":" + newLine;
                            if(otherCoverage.eventInfoToHashMap.containsKey(str2)) {
                                HashSet<Integer> hashes1 = eventInfoToHashMap.get(str1);
                                HashSet<Integer> hashes2 = otherCoverage.eventInfoToHashMap.get(str2);
                                //System.out.println(hashes1.toString() + " vs " + hashes2.toString());
                                for(int hash1: hashes1) {
                                    for(int hash2: hashes2) {
                                        if(counter.getAtIndex(hash1) != otherCounter.getAtIndex(hash2)) {
                                            diff++;
                                            dist += Math.abs(counter.getAtIndex(hash1) - otherCounter.getAtIndex(hash2));
                                        }
                                    }
                                }
                            }
                        } else if (count1 != count2){
                            //System.out.println("iNot equal " + i + ": " + count1 + " , " + count2);
                            diff += 1;
                            dist += Math.abs(count1 - count2);
                        }
                    }
                }
            }
        } else {
            System.out.println("No source directory available: compare without gumtree");
            for (int i: nonZeroIndices) {
                int count1 = counter.getAtIndex(i);
                int count2 = otherCounter.getAtIndex(i);
                if (count1 != count2) {
                    System.out.println("iNot equal " + i + ": " + count1 + " , " + count2);
                    diff += 1;
                    dist += Math.abs(count1 - count2);
                }
            }
            for (int j: nonZeroIndices2) {
                int count1 = counter.getAtIndex(j);
                int count2 = otherCounter.getAtIndex(j);
                if (count1 != count2 && count1 == 0) {
                    System.out.println("jNot equal " + j + "," + count1 + " , " + count2);
                    diff += 1;
                    dist += Math.abs(count1 - count2);
                }
            }
        }
        System.out.println("diff: " + diff + " dist: " + dist);
        return new Pair<>(diff, dist);
        //return this.counter.getDistance(otherCoverage.counter);
    }
}
