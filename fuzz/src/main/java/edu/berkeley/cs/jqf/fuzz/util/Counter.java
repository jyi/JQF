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

import com.pholser.junit.quickcheck.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Maps integer keys to integer counts using a fixed-size table.
 *
 * <p>Hash collisions are completely ignored; therefore, the counts
 * are unreliable.</p>
 *
 * <p>Throughout the internal documentation, the term "key" is used
 * to refer to the keys that are hashed, while "index" is used to
 * the result of key-hashing, i.e. the location in the internal
 * array storage.</p>
 *
 * @author Rohan Padhye
 */
public class Counter {

    /** The size of the counter map. */
    protected final int size;

    /** The counter map as an array of integers. */
    protected final int[] counts;

    /**
     * Creates a new counter with given size.
     *
     * @param size the fixed-number of elements in the hashtable.
     */
    public Counter(int size) {
        this.size = size;
        this.counts = new int[size];
    }

    /**
     * Returns the size of this counter.
     *
     * @return the size of this counter
     */
    public int size() {
        return this.size;
    }

    /**
     * Clears the counter by setting all values to zero.
     */
    public void clear() {
        Arrays.fill(this.counts, 0);
    }

    private int idx(int key) {
        return Hashing.hash(key, size);
    }

    private int idx1(int k1, int k2) {
        return Hashing.hash1(k1, k2, size);
    }

    protected int incrementAtIndex(int index, int delta) {
        return (this.counts[index] += delta);
    }

    /**
     * Increments the count at the given key.
     *
     * <p>Note that the key is hashed and therefore the count
     * to increment may be shared with another key that hashes
     * to the same value. </p>
     *
     * @param key the key whose count to increment
     * @return the new value after incrementing the count
     */
    public int increment(int key) {
        return incrementAtIndex(idx(key), 1);
    }

    /**
     * Increments the count at the given key pair.
     *
     * <p>Note that the key pair is hashed and therefore the count
     * to increment may be shared with another key pair that hashes
     * to the same value. </p>
     *
     * @param k1 the key (part 1) whose count to increment
     * @param k2 the key (part 2) whose count to increment
     * @return the new value after incrementing the count
     */
    public int increment1(int k1, int k2) {
        return incrementAtIndex(idx1(k1, k2), 1);
    }


    /**
     *
     * Increments the count at the given key by a given delta.
     *
     * <p>Note that the key is hashed and therefore the count
     * to increment may be shared with another key that hashes
     * to the same value. </p>
     *
     * @param key the key whose count to increment
     * @param delta the amount to increment by
     * @return the new value after incrementing the count
     */
    public int increment(int key, int delta) {
        return incrementAtIndex(idx(key), delta);
    }

    /**
     * Returns the number of indices with non-zero counts.
     *
     * @return the number of indices with non-zero counts
     */
    public int getNonZeroSize() {
        int size = 0;
        for (int i = 0; i < counts.length; i++) {
            int count = counts[i];
            if (count != 0) {
                size++;
            }
        }
        return size;
    }

    /**
     * Checks if all indices have zero counts
     * and returns a boolean as result.
     *
     * @return {@code true} if some indices have non-zero counts, otherwise {@code false}.
     */
    public boolean hasNonZeros(){
        if (counts.length> 0){
            for (int i = 0; i < counts.length; i++) {
                int count = counts[i];
                if (count != 0) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Returns a set of indices at which the count is non-zero.
     *
     * <p>Note that indices are different from keys, in that
     * multiple keys can map to the same index due to hash
     * collisions.</p>
     *
     * @return a set of indices at which the count is non-zero
     */
    public List<Integer> getNonZeroIndices() {
        List<Integer> indices = new ArrayList<>(size /2);
        for (int i = 0; i < counts.length; i++) {
            int count = counts[i];
            if (count != 0) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Returns a set of non-zero count values in this counter.
     *
     * @return a set of non-zero count values in this counter.
     */
    public Collection<Integer> getNonZeroValues() {
        List<Integer> values = new ArrayList<>(size /2);
        for (int i = 0; i < counts.length; i++) {
            int count = counts[i];
            if (count != 0) {
                values.add(count);
            }
        }
        return values;
    }

    /**
     * Retreives a value for a given key.
     *
     * <p>The key is first hashed to retreive a value from
     * the counter, and hence the result is modulo collisions.</p>
     *
     * @param key the key to query
     * @return the count for the index corresponding to this key
     */
    public int get(int key) {
        return this.counts[idx(key)];
    }


    public int getAtIndex(int idx) {
        return this.counts[idx];
    }

    public void setAtIndex(int idx, int value) {
        this.counts[idx] = value;
    }

    public int getIdx(int key) {
        return idx(key);
    }

    public int getIdx1(int k1, int k2) {
        return idx1(k1, k2);
    }

    public Pair<Long, Long> getDistance(Counter otherCounter) {
        long diff = 0;
        long dist = 0;
        List<Integer> nonZeroIndices = getNonZeroIndices();
        List<Integer> nonZeroIndices2 = otherCounter.getNonZeroIndices();
        /*
        TargetCoverage targetCoverage = TargetCoverage.getTargetCoverage();
        List<Target> targets = targetCoverage.getCoveredTargets();
        String[] orgCPs = System.getProperty("jqf.ei.CLASSPATH_FOR_ORG").split(":");
        String[] targetCPs = System.getProperty("jqf.ei.CLASSPATH_FOR_PATCH").split(":");
        String orgClassPath = "";
        String targetClassPath = "";
        try {
            for(Target target: targets) {
                String classTarget = target.getClassName();
                for(String cp: orgCPs) {
                    File directoryOrJar = new File((new File(cp)).getCanonicalPath());
                    File org;
                    if(directoryOrJar.isFile()) {
                        org = new File(directoryOrJar.getParent());
                    } else {
                        org = new File(directoryOrJar, target.getFilename());
                    }
                    if (org.exists()) break;
                    //orgClassPath += (new File(cp)).getCanonicalPath() + ":";
                }
                //orgClassPath = orgClassPath.substring(0, orgClassPath.length() - 1);
                for(String cp: targetCPs) {
                    //targetClassPath += (new File(cp)).getCanonicalPath() + ":";
                }
                //targetClassPath = targetClassPath.substring(0, targetClassPath.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("orgClassPath: " + orgClassPath);
        System.out.println("targetClassPath: " + targetClassPath);
        //JdtTreeContext srcTreeCtxt = (JdtTreeContext) new JdtTreeGenerator().generateFrom().file(srcFile);
        //JdtTreeContext dstTreeCtxt = (JdtTreeContext) new JdtTreeGenerator().generateFrom().file(dstFile);
        */

        //com.github.gumtreediff.gen.jdt.JdtTreeContext srcTreeCtxt =
        //        (com.github.gumtreediff.gen.jdt.JdtTreeContext) new com.github.gumtreediff.gen.jdt.JdtTreeGenerator().generateFrom().file(src);
        //System.out.println("non-zero indices: " + nonZeroIndices.toString());
        for (int i = 0; i < nonZeroIndices.size(); i++) {
            try {
                int count1 = nonZeroIndices.get(i);
                int count2 = nonZeroIndices2.get(i);
                if (count1 != count2) {
                    diff += 1;
                    dist += Math.abs(count1 - count2);
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return new Pair<>(diff, dist);
    }
}
