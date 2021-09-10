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
package edu.berkeley.cs.jqf.fuzz.reach;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.time.Duration;

import com.pholser.junit.quickcheck.Pair;
import com.sun.istack.Nullable;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.util.TargetCoverage;
import edu.berkeley.cs.jqf.fuzz.util.TargetDistance;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import de.hub.se.cfg.CFGAnalysis;
import kr.ac.unist.cse.jqf.Log;
import kr.ac.unist.cse.jqf.aspect.DumpUtil;
import kr.ac.unist.cse.jqf.aspect.MethodInfo;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jetbrains.annotations.NotNull;
import org.magicwerk.brownies.collections.BigList;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/**
 * A front-end that only generates random inputs.
 *
 * <p>This class provides no guidance to quickcheck. It seeds random values from
 * {@link Random}, making it effectively an unguided random test input
 * generator.
 */
public class PoracleGuidance extends ZestGuidance {

    private String logDir;
    private int maxMutations;
    private TargetCoverage targetCoverage = TargetCoverage.getTargetCoverage();
    private long exploreDurationMills;

    private CFGAnalysis cfga = null;

    private final List<Input<?>> inputs = new ArrayList<>();
    private boolean isDiffOutFound = false;
    private BigList<BigList<Double>> stateDiffCoverage = new BigList<>();
    private File notIgnoreDirectory;
    private boolean diffOutFound;
    protected boolean USE_WIDENING_PLATEAU_THRESHOLD =
            System.getProperty("jqf.ei.WIDENING_PLATEAU_THRESHOLD") != null?
                    true : false;
    protected int wideningPlateauThreshold =
            System.getProperty("jqf.ei.WIDENING_PLATEAU_THRESHOLD") != null?
                    Integer.getInteger("jqf.ei.WIDENING_PLATEAU_THRESHOLD") : 10;
    public boolean isWideningPlateauReached = false;
    private long inputIdx = 0;
    private String inputID = null;
    private String parentID = null;
    private int targetNumChildren = Integer.parseInt(System.getProperty("jqf.ei.MAX_MUTATIONS"))  / 2;

    /** Coverage statistics for a single run. */
    protected Coverage runCoverageOfOrg = new Coverage();
    protected Coverage runCoverageOfPatch = new Coverage();

    /** Cumulative coverage statistics. */
    protected Coverage totalCoverageOfOrg = new Coverage();
    protected Coverage totalCoverageOfPatch = new Coverage();

    /** Cumulative coverage for valid inputs. */
    protected Coverage validCoverageOfOrg = new Coverage();
    protected Coverage validCoverageOfPatch = new Coverage();

    protected TargetDistance targetDistance = TargetDistance.getSingleton();

    /** The maximum number of keys covered by any single input found so far. */
    protected int maxCoverageOfOrg = 0;
    protected int maxCoverageOfPatch = 0;

    /** A mapping of coverage keys to inputs that are responsible for them. */
    protected Map<Object, Input> responsibleInputsOfOrg = new HashMap<>(totalCoverageOfOrg.size());
    protected Map<Object, Input> responsibleInputsOfPatch = new HashMap<>(totalCoverageOfPatch.size());

    public void fixRange() {
        this.rangeFixed = true;
    }

    public boolean isRangeFixed() {
        return this.rangeFixed;
    }

    public void resetRunCoverageOfOrg() {
        runCoverageOfOrg.clear();
    }

    enum Version {
        ORG ("ORG"),
        PATCH("PATCH");

        private final String dir;

        Version(String dir) {
            this.dir = dir;
        }

        public String getDir() {
            return this.dir;
        }
    }

    enum PointCutLocation {
        ENTRY("Entry"),
        EXIT("Exit");

        private final String suffix;

        PointCutLocation(String suffix) {
            this.suffix = suffix;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    class InputComparator implements Comparator<Input> {
        @Override
        public int compare(Input i1, Input i2) {
            return compare(i1.getDistance(), i2.getDistance());
        }

        private int compare(Distance dist1, Distance dist2) {
            assert dist1 != null;
            assert dist2 != null;
            List<List<Double>> distList1 = dist1.getDistList();
            List<List<Double>> distList2 = dist2.getDistList();
            return compare(distList1, distList2);
        }

        public int compare(@NotNull List<List<Double>> dists1, @NotNull List<List<Double>> dists2) {
            assert dists1.size() == dists2.size();
            for (int i = 0; i < dists1.size(); i++) {
                List<Double> d1 = dists1.get(i);
                List<Double> d2 = dists2.get(i);
                switch (compare2(d1, d2)) {
                    case 0:
                        continue;
                    case 1:
                        return 1;
                    case -1:
                        return -1;
                    default:
                        assert false;
                }
            }
            return 0;
        }

        private int compare2(@Nullable List<Double> dist1, @Nullable List<Double> dist2) {
            if (dist1 == null && dist2 == null) return 0;
            if (dist1 == null && dist2 != null) return -1;
            if (dist2 == null && dist1 != null) return 1;

            if (dist1.size() == dist2.size()) {
                for (int i = 0; i < dist1.size(); i++) {
                    Double d1 = dist1.get(i);
                    Double d2 = dist2.get(i);
                    if (d1.equals(d2)) continue;
                    if (d1 < d2) return -1;
                    else return 1;
                }
                return 0;
            }

            assert dist1.size() != dist2.size();

            int idx1 = firstNonZeroIndex(dist1);
            int idx2 = firstNonZeroIndex(dist2);

            if (idx1 < idx2) return -1;
            if (idx2 < idx1) return 1;

            return 0;
        }

        private int firstNonZeroIndex(List<Double> lst) {
            int i = 0;
            while (i < lst.size()) {
                Double d = lst.get(i);
                if (d != 0d) return i;
                i++;
            }
            return lst.size();
        }
    }

    public class Distance {

        private final double distToTraget;
        private final @Nullable List<Double> versionDistCallerExit;
        private final @Nullable List<Double> versionDistCalleeExit;
        private final @Nullable List<Double> versionDistCalleeEntry;
        private long pathDist;
        private long pathDiff;

        public Distance() {
            this.distToTraget = 0;
            this.versionDistCallerExit = null;
            this.versionDistCalleeExit = null;
            this.versionDistCalleeEntry = null;
            this.pathDiff = 0L;
            this.pathDist = 0L;
        }

        public Distance(double distToTarget, List<Double> versionDistCallerExit, List<Double> versionDistCalleeExit,
                        List<Double> versionDistCalleeEntry,
                        Pair<Long, Long> pathDiffDist) {
            this.distToTraget = distToTarget;
            this.versionDistCallerExit = versionDistCallerExit;
            this.versionDistCalleeExit = versionDistCalleeExit;
            this.versionDistCalleeEntry = versionDistCalleeEntry;
            this.pathDiff = pathDiffDist.first;
            this.pathDist = pathDiffDist.second;
        }

        public List<List<Double>> getDistList() {
            // the first one is the most important
            List<List<Double>> distList = new ArrayList<>();
            // state difference
            distList.add(versionDistCallerExit);
            distList.add(versionDistCalleeExit);
            distList.add(versionDistCalleeEntry);

            // path difference
            ArrayList<Double> lst = new ArrayList<>();
            lst.add(Double.valueOf(pathDiff));
            distList.add(lst);

            lst = new ArrayList<>();
            lst.add(Double.valueOf(pathDist));
            distList.add(lst);

            // target distance
            ArrayList<Double> distToTragetList = new ArrayList<>();
            distToTragetList.add(distToTraget);
            distList.add(distToTragetList);

            return distList;
        }
    }

    public class ComparableInput extends LinearInput {

        private Distance distance;

        public ComparableInput() {
            super();
        }

        public ComparableInput(ComparableInput other) {
            super(other);
        }

        public Distance getDistance() {
            return this.distance;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }

        @Override
        public Input fuzz(Random random) {
            // Clone this input to create initial version of new child
            LinearInput newInput = new ComparableInput(this);

            // Stack a bunch of mutations
            int numMutations = sampleGeometric(random, MEAN_MUTATION_COUNT);
            newInput.desc += ",havoc:"+numMutations;

            boolean setToZero = random.nextDouble() < 0.1; // one out of 10 times

            for (int mutation = 1; mutation <= numMutations; mutation++) {

                // Select a random offset and size
                int offset = random.nextInt(newInput.getValues().size());
                int mutationSize = sampleGeometric(random, MEAN_MUTATION_SIZE);

                // desc += String.format(":%d@%d", mutationSize, idx);

                // Mutate a contiguous set of bytes from offset
                for (int i = offset; i < offset + mutationSize; i++) {
                    // Don't go past end of list
                    if (i >= newInput.getValues().size()) {
                        break;
                    }

                    // Otherwise, apply a random mutation
                    int mutatedValue = setToZero ? 0 : random.nextInt(256);
                    newInput.getValues().set(i, mutatedValue);
                }
            }
            
            return newInput;
        }
    }

    public class ResultOfOrg {
        private final Input<?> input;
        private final File inputFile;
        private final boolean valid;
        private final Distance dist;
        private boolean inputNotIgnored;
        private boolean newCoverageFound;
        private String why;

        public ResultOfOrg(boolean inputNotIgnored, Input<?> input, File inputFile, boolean newCoverageFound, boolean valid, String why) {
            this.inputNotIgnored = inputNotIgnored;
            this.input = input;
            this.inputFile = inputFile;
            this.newCoverageFound = newCoverageFound;
            this.valid = valid;
            this.why = why;
            this.dist = new Distance();
        }

        public boolean isInputNotIgnored() {
            return inputNotIgnored;
        }

        public Input<?> getInput() {
            return this.input;
        }

        public File getInputFile() {
            return this.inputFile;
        }

        public boolean isNewCoverageFound() {
            return this.newCoverageFound;
        }

        public String getWhy() {
            return why;
        }

        public void setWhy(String why) {
            this.why = why;
        }

        public boolean isValid() {
            return this.valid;
        }

        public Distance getDistance() {
            return this.dist;
        }
    }

    public class ResultOfPatch {

        private final boolean newCoverageFound;
        private final Distance dist;
        private String why;
        private final boolean valid;

        public ResultOfPatch(boolean newCoverageFound, String why, boolean valid, Distance dist) {
            this.newCoverageFound = newCoverageFound;
            this.why = why;
            this.valid = valid;
            this.dist = dist;
        }

        public boolean isValid() {
            return valid;
        }

        public String getWhy() {
            return why;
        }

        public boolean isNewCoverageFound() {
            return newCoverageFound;
        }

        public Distance getDistance() {
            return dist;
        }

        public void setWhy(String why) {
            this.why = why;
        }
    }

    private double toDouble(String str) throws ConversionToDoubleFailure {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new ConversionToDoubleFailure();
        }
    }

    private double toDouble(Object obj) throws ConversionToDoubleFailure {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        if (obj instanceof Double) {
            return ((Double) obj).doubleValue();
        }
        if (obj instanceof Long) {
            return ((Long) obj).longValue();
        }
        if (obj instanceof Short) {
            return ((Short) obj).shortValue();
        }
        if (obj instanceof Byte) {
            return ((Byte) obj).byteValue();
        }
        if (obj instanceof Character) {
            return ((Character) obj).charValue();
        }
        if (obj instanceof Float) {
            return ((Float) obj).floatValue();
        }
        if (obj instanceof Boolean) {
            boolean boolVal = ((Boolean) obj).booleanValue();
            if (boolVal == true) return 1;
            if (boolVal == false) return 0;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.equals("true")) return 1;
            if (str.equals("false")) return 0;
            else return toDouble(str);
        }
        throw new ConversionToDoubleFailure();
    }

    @Override
    protected void prepareOutputDirectory() throws IOException {
        super.prepareOutputDirectory();

        this.notIgnoreDirectory = new File(outputDirectory, "not_ignore");
        this.notIgnoreDirectory.mkdirs();
    }

    public boolean shouldSaveInput(ResultOfOrg resultOfOrg, ResultOfPatch resultOfPatch) {
        boolean newCoverageFound = false;
        Distance dist = null;
        if (resultOfPatch != null) {
            newCoverageFound = resultOfPatch.isNewCoverageFound();
            dist = resultOfPatch.getDistance();
        } else {
            newCoverageFound = resultOfOrg.isNewCoverageFound();
            dist = resultOfOrg.getDistance();
        }

        if (newCoverageFound) {
            if (savedInputs.isEmpty()) return true;

            Input best = this.savedInputs.get(savedInputs.size() - 1);
            int result = (new InputComparator()).compare(dist, best.getDistance());
            if (result > 0) {
                if (resultOfPatch != null) {
                    String why = resultOfPatch.getWhy() + "+dist";
                    resultOfPatch.setWhy(why);
                } else {
                    String why = resultOfOrg.getWhy() + "+dist";
                    resultOfOrg.setWhy(why);
                }
                return true;
            }
        }

        long elapsedTime = new Date().getTime() - startTime.getTime();
        double temp = this.temparature(elapsedTime) / 2d;
        if (Math.random() < temp) return true;
        else return false;
    }

    public void saveInputs(String reason, boolean valid) {
        Set<Object> responsibilities = computeResponsibilities(valid);
        GuidanceException.wrap(() -> saveCurrentInput(responsibilities, reason));
    }

    public void saveInputs(String reason, boolean valid, Distance dist) {
        this.currentInput.setDistance(dist);
        Set<Object> responsibilities = computeResponsibilities(valid);
        GuidanceException.wrap(() -> saveCurrentInput(responsibilities, reason));
    }

    /* Saves an interesting input to the queue. */
    protected void saveCurrentInput(Set<Object> responsibilities, String why) throws IOException {
        this.curCorpusSize++;

        // First, save to disk (note: we issue IDs to everyone, but only write to disk  if valid)
        numSavedInputs++;
        String how = currentInput.desc;
        File saveFile = new File(savedCorpusDirectory, System.getProperty("jqf.ei.inputID"));
        if (SAVE_ONLY_VALID == false || currentInput.valid) {
            writeCurrentInputToFile(saveFile);
            infoLog("Saved - %s %s %s", saveFile.getPath(), how, why);
        }

        // If not using guidance, do nothing else
        if (blind) {
            return;
        }

        // Second, save to queue
        savedInputs.add(currentInput);

        // Third, store basic book-keeping data
        currentInput.id = inputIdx;
        currentInput.saveFile = saveFile;
        currentInput.coverage = new Coverage(runCoverageOfPatch);
        currentInput.nonZeroCoverage = runCoverageOfPatch.getNonZeroCount();
        currentInput.offspring = 0;
        savedInputs.get(currentParentInputIdx).offspring += 1;

        // Fourth, assume responsibility for branches
        currentInput.responsibilities = responsibilities;

        for (Object b : responsibilities) {
            // If there is an old input that is responsible,
            // subsume it
            Input oldResponsible = responsibleInputsOfPatch.get(b);
            if (oldResponsible != null) {
                oldResponsible.responsibilities.remove(b);
                // infoLog("-- Stealing responsibility for %s from input %d", b, oldResponsible.id);
            } else {
                // infoLog("-- Assuming new responsibility for %s", b);
            }
            // We are now responsible
            responsibleInputsOfPatch.put(b, currentInput);
        }
    }

    public boolean isDiffOutFound() {
        return this.diffOutFound;
    }

    // the first item of the return value is the most important
    // the last itme of the return value is the distance at the target method
    private List<Double> getDistance(String inputID1, String inputID2, List<MethodInfo> methods,
                               Version version1, Version version2, PointCutLocation loc) {
        assert methods != null;
        List<Double> distList = new ArrayList<>();
        double distance = 0d;
        for (int i = methods.size() - 1; i >= 0; i--) {
            MethodInfo m = methods.get(i);
            Path file1 = Paths.get(logDir + File.separator + version1.getDir(),
                    inputID1, m.getMethodName() + loc.getSuffix() + ".xml");
            Path file2 = Paths.get(logDir + File.separator + version2.getDir(),
                    inputID2, m.getMethodName() + loc.getSuffix() + ".xml");
            if (!Files.exists(file1)) {
                infoLog("No matching method exists: %s", file1);
                continue;
            }

            if (!Files.exists(file2)) {
                infoLog("No matching method exists: %s", file2);
                continue;
            }

            try {
                String orgContents = new String(Files.readAllBytes(file1));
                String patchContents = new String(Files.readAllBytes(file2));
                Diff myDiff = DiffBuilder.compare(orgContents).withTest(patchContents).build();
                for (Difference xmlDiff : myDiff.getDifferences()) {
                    Comparison cmp = xmlDiff.getComparison();

                    // TODO: this is temporary code to ignore random variables
                    if (cmp.getControlDetails().getParentXPath().contains("random"))
                        continue;

                    Object obj1 = null;
                    Object obj2 = null;
                    try {
                        obj1 = cmp.getControlDetails().getValue();
                        double val1 = toDouble(obj1);
                        Comparison.Detail test = cmp.getTestDetails();
                        obj2 = test.getValue();
                        double val2 = toDouble(obj2);
                        distance += Math.abs(val1 - val2);
                    } catch (ConversionToDoubleFailure e) {
                        String str1 = "";
                        String str2 = "";
                        if (obj1 != null) str1 = obj1.toString();
                        if (obj2 != null) str2 = obj2.toString();
                        distance += new LevenshteinDistance().apply(str1, str2);
                    }
                }
                distList.add(distance);
            } catch (Exception e) {
                infoLog("Failed to read xml files");
                System.err.println("Failed to read xml files");
                System.err.println(e.toString());
                e.printStackTrace();
            }
        }

        return distList;
    }

    private List<Double> getVersionDistance(String inputID, List<MethodInfo> methods, PointCutLocation loc) {
        return getDistance(inputID, inputID, methods, Version.ORG, Version.PATCH, loc);
    }

    public void noProgress() {
        noProgressCount++;
    }

    public void resetProgressCount() {
        noProgressCount = 0;
    }

    public void checkProgress() {
        if (USE_WIDENING_PLATEAU_THRESHOLD && !rangeFixed && noProgressCount > wideningPlateauThreshold) {
            infoLog("A widening plateau is reached!!!");
            isWideningPlateauReached = true;
        }
    }

    // Compute a set of branches for which the current input may assume responsibility
    protected Set<Object> computeResponsibilities(boolean valid) {
        Set<Object> result = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());

        // This input is responsible for all new coverage
        Collection<?> newCoverage = runCoverageOfPatch.computeNewCoverage(totalCoverageOfPatch);
        if (newCoverage.size() > 0) {
            result.addAll(newCoverage);
        }

        // If valid, this input is responsible for all new valid coverage
        if (valid) {
            Collection<?> newValidCoverage = runCoverageOfPatch.computeNewCoverage(validCoverageOfPatch);
            if (newValidCoverage.size() > 0) {
                result.addAll(newValidCoverage);
            }
        }

        // Perhaps it can also steal responsibility from other inputs
        if (STEAL_RESPONSIBILITY) {
            int currentNonZeroCoverage = runCoverageOfPatch.getNonZeroCount();
            int currentInputSize = currentInput.size();
            Set<?> covered = new HashSet<>(runCoverageOfPatch.getCovered());

            // Search for a candidate to steal responsibility from
            candidate_search:
            for (Input candidate : savedInputs) {
                Set<?> responsibilities = candidate.responsibilities;

                // Candidates with no responsibility are not interesting
                if (responsibilities.isEmpty()) {
                    continue candidate_search;
                }

                // To avoid thrashing, only consider candidates with either
                // (1) strictly smaller total coverage or
                // (2) same total coverage but strictly larger size
                if (candidate.nonZeroCoverage < currentNonZeroCoverage ||
                        (candidate.nonZeroCoverage == currentNonZeroCoverage &&
                                currentInputSize < candidate.size())) {

                    // Check if we can steal all responsibilities from candidate
                    for (Object b : responsibilities) {
                        if (covered.contains(b) == false) {
                            // Cannot steal if this input does not cover something
                            // that the candidate is responsible for
                            continue candidate_search;
                        }
                    }
                    // If all of candidate's responsibilities are covered by the
                    // current input, then it can completely subsume the candidate
                    result.addAll(responsibilities);
                }

            }
        }

        return result;
    }

    public ResultOfPatch handleResultOfPatch(Result result, boolean targetHit) {
        // Stop timeout handling
        this.runStart = null;

        // Increment run count
        this.numTrials++;

        boolean valid = result == Result.SUCCESS;
        if (valid) {
            // Increment valid counter
            numValid++;
        }

        // Possibly save input
        boolean newCoverageFound = false;
        String why = "";

        // Coverage before
        int nonZeroBefore = totalCoverageOfPatch.getNonZeroCount();
        int validNonZeroBefore = validCoverageOfPatch.getNonZeroCount();

        // Compute a list of keys for which this input can assume responsiblity.
        // Newly covered branches are always included.
        // Existing branches *may* be included, depending on the heuristics used.
        // A valid input will steal responsibility from invalid inputs
        Set<Object> responsibilities = computeResponsibilities(valid);

        // Update total coverage
        boolean coverageBitsUpdated = totalCoverageOfPatch.updateBits(runCoverageOfPatch);
        if (valid) {
            validCoverageOfPatch.updateBits(runCoverageOfPatch);
        }

        // Coverage after
        int nonZeroAfter = totalCoverageOfPatch.getNonZeroCount();
        if (nonZeroAfter > maxCoverageOfPatch) {
            maxCoverageOfPatch = nonZeroAfter;
        }

        if (SAVE_NEW_COUNTS && coverageBitsUpdated) {
            newCoverageFound = true;
            why = why + "+count";
        }

        // Save if new total coverage found
        if (nonZeroAfter > nonZeroBefore) {
            // Must be responsible for some branch
            assert (responsibilities.size() > 0);
            newCoverageFound = true;
            why = why + "+cov";
        }

        // Save if new valid coverage is found
        int validNonZeroAfter = validCoverageOfPatch.getNonZeroCount();
        if (this.validityFuzzing && validNonZeroAfter > validNonZeroBefore) {
            // Must be responsible for some branch
            assert (responsibilities.size() > 0);
            currentInput.valid = true;
            newCoverageFound = true;
            why = why + "+valid";
        }

        // save the input that reaches the target
        if (targetHit) {
            String targetHitPath = outputDirectory.getPath() + File.separator + "target_hit";
            if(!Files.exists(Paths.get(targetHitPath))) {
                try {
                    Files.createDirectories(Paths.get(targetHitPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                String inputID = System.getProperty("jqf.ei.inputID");
                Path inFile = Paths.get(targetHitPath, inputID);
                if (Files.exists(inFile)) {
                    Files.delete(inFile);
                }
                Files.createFile(inFile);
                writeCurrentInputToFile(inFile.toFile());
            } catch (IOException e) {
                infoLog("Something went wrong while writing target-reaching input");
                System.err.println("Something went wrong while writing target-reaching input");
                e.printStackTrace();
            }
        }

        List<Double> versionDistCallerExit = null;
        List<Double> versionDistCalleeExit = null;
        List<Double> versionDistCalleeEntry = null;
        Pair<Long, Long> pathDiffDist = new Pair(0L, 0L);

        List<MethodInfo> callers = DumpUtil.getCallerChain();
        List<MethodInfo> callees = DumpUtil.getCalleesOfTaregetMethod();

        double distToTarget = Double.POSITIVE_INFINITY;
        if (targetHit && callers != null && callees != null) {
            distToTarget = 0;
            pathDiffDist = this.runCoverageOfPatch.getDistance(this.runCoverageOfOrg);
            versionDistCallerExit = getVersionDistance(inputID, callers, PointCutLocation.EXIT);
            versionDistCalleeExit = getVersionDistance(inputID, callees, PointCutLocation.EXIT);
            versionDistCalleeEntry = getVersionDistance(inputID, callees, PointCutLocation.ENTRY);
        } else if (!targetHit) {
            distToTarget = targetDistance.getDistance();
        }
        Distance dist = new Distance(distToTarget, versionDistCallerExit, versionDistCalleeExit, versionDistCalleeEntry, pathDiffDist);

        ResultOfPatch resultOfPatch = new ResultOfPatch(newCoverageFound, why, valid, dist);
        return resultOfPatch;
    }

    public String distsToString(Distance dist) {
        List<List<Double>> distList = dist.getDistList();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < distList.size(); i++) {
            sb.append(distList.get(i) == null? 0 : distList.get(i));
            if (i < distList.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Creates a new guidance instance.
     *
     * @param testName the name of test to display on the status screen
     * @param duration the amount of time to run fuzzing for, where
     *                 {@code null} indicates unlimited time.
     * @param outputDirectory the directory where fuzzing results will be written
     * @throws IOException if the output directory could not be prepared
     */
    public PoracleGuidance(String testName, long seed,
                           Duration duration, Duration exploreDuration, File outputDirectory) throws IOException {
        super(testName, duration, outputDirectory);
        init(seed, exploreDuration);
    }

    /**
     * Creates a new guidance instance.
     *
     * @param testName the name of test to display on the status screen
     * @param duration the amount of time to run fuzzing for, where
     *                 {@code null} indicates unlimited time.
     * @param outputDirectory the directory where fuzzing results will be written
     * @param seedInputFiles one or more input files to be used as initial inputs
     * @throws IOException if the output directory could not be prepared
     */
    public PoracleGuidance(String testName, long seed,
                           Duration duration, Duration exploreDuration, File outputDirectory,
                           File[] seedInputFiles) throws IOException {
        super(testName, duration, outputDirectory, seedInputFiles);
        init(seed, exploreDuration);
    }

    private void init(long seed, Duration exploreDuration) {
        this.exploreDurationMills = exploreDuration.toMillis();
        logDir = System.getProperty("jqf.ei.logDir");
        maxMutations = Integer.parseInt(System.getProperty("jqf.ei.MAX_MUTATIONS"));
        if (seed != -1) this.random.setSeed(seed);
    }
    
    public void setDiffOutputFound(boolean isDiffOutFound) {
        this.isDiffOutFound = isDiffOutFound;
    }

    @Override
    public boolean hasInput() {
        diffOutFound = false;
        Date now = new Date();
        long elapsedMilliseconds = now.getTime() - startTime.getTime();

        // if (timeOutOccurred) return false;

        if (EXIT_ON_PLATEAU && isPlateauReached) {
            infoLog("stop because plateau is reached");
            System.out.println("stop because plateau is reached");
            return false;
        }

        String inputID = System.getProperty("jqf.ei.inputID");
        Log.logMeasuredTime(inputID, elapsedMilliseconds);
        String diffOutPath = outputDirectory.getPath() + File.separator + "diff_out";
        if(!Files.exists(Paths.get(diffOutPath))) {
            try {
                Files.createDirectories(Paths.get(diffOutPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.isDiffOutFound) {
            // save the current input into diff_out dir
            try {
                diffOutFound = true;
                Path inFile = Paths.get(diffOutPath, inputID);
                if (Files.exists(inFile)) {
                    Files.delete(inFile);
                }
                Files.createFile(inFile);
                writeCurrentInputToFile(inFile.toFile());
            } catch (IOException e) {
                infoLog("Something went wrong while writing diff-revealing input");
                System.err.println("Something went wrong while writing diff-revealing input");
                e.printStackTrace();
            }

            infoLog("Diff out is found after %d ms!", elapsedMilliseconds);
            System.out.println(String.format("Diff out is found after %d ms!", elapsedMilliseconds));
            
            if (requiredRun>0) return true;
            else return false;
        }
        if (requiredRun>0 && this.numTrials>=requiredRun){
            infoLog("Finish for required count is reached: %d", requiredRun);
            return false;
        }
        return elapsedMilliseconds < maxDurationMillis;
    }

    /** Spawns a new input from thin air (i.e., actually random) */
    @Override
    protected Input<?> createFreshInput() {
        return new ComparableInput();
    }

    @Override
    public InputStream getInput() throws GuidanceException {
        targetCoverage.clear();
        // Clear coverage stats for this run
        runCoverageOfOrg.clear();
        runCoverageOfPatch.clear();

        // set inputID
        String saveFileName = String.format("id_%09d", numTrials + 1);
        inputIdx = numTrials + 1;
        inputID = saveFileName;
        System.setProperty("jqf.ei.inputID", saveFileName);

        // Choose an input to execute based on state of queues
        if (!seedInputs.isEmpty()) {
            // First, if we have some specific seeds, use those
            currentInput = seedInputs.removeFirst();

            // Hopefully, the seeds will lead to new coverage and be added to saved inputs

        } else if (savedInputs.isEmpty()) {
            // If no seeds given try to start with something random
            if (!blind && numTrials > 100_000) {
                throw new GuidanceException("Too many trials without coverage; " +
                        "likely all assumption violations");
            }

            // Make fresh input using either list or maps
            // infoLog("Spawning new input from thin air");
            currentInput = createFreshInput();

            // Start time-counting for timeout handling
            this.runStart = new Date();
        } else {
            // sorted in an ascending order
            savedInputs.sort(new InputComparator());
            if (savedInputs.size() > maxCorpusSize) {
                savedInputs = savedInputs.subList(savedInputs.size() - maxCorpusSize, savedInputs.size());
                infoLog("saved distances:");
                for (Input input: savedInputs) {
                    infoLog(distsToString(input.getDistance()));
                }
            }

            // The number of children to produce is determined by how much of the coverage
            // pool this parent input hits
            Input currentParentInput = savedInputs.get(currentParentInputIdx);
            if (numChildrenGeneratedForCurrentParentInput >= targetNumChildren) {
                // Select the next saved input to fuzz
                currentParentInputIdx = random.nextInt(savedInputs.size());
                infoLog("currentParentInputIdx: %d / %d", currentParentInputIdx, savedInputs.size());
                console.printf("currentParentInputIdx: %d / %d", currentParentInputIdx, savedInputs.size());
                targetNumChildren = getTargetChildrenForParent(savedInputs.size(), currentParentInputIdx);
                infoLog("targetNumChildren: %d", targetNumChildren);
                numChildrenGeneratedForCurrentParentInput = 0;
            }
            // Input parent = savedInputs.get(currentParentInputIdx);
            parentID = String.format("id_%09d", currentParentInput.id);
            // Fuzz it to get a new input
            infoLog("Mutating input: %s", currentParentInput.desc);
            
            currentInput = currentParentInput.fuzz(random);
            numChildrenGeneratedForCurrentParentInput++;

            // Write it to disk for debugging
            try {
                writeCurrentInputToFile(currentInputFile);
            } catch (IOException ignore) { }

            // Start time-counting for timeout handling
            this.runStart = new Date();
            this.branchCount = 0;
        }

        return createParameterStream();
    }

    protected int getTargetChildrenForParent(int size, int idx) {
        idx = idx + 1; // the index should start from 1
        long elapsedTime = new Date().getTime() - startTime.getTime();
        double energy = ((double) idx / (double) size) * (1 - temparature(elapsedTime)) + 0.5 * temparature(elapsedTime);
        int answer = (int) (maxMutations * energy);
        return answer;
    }

    private double temparature(long elapsedTime) {
        double ret = Math.pow(20, -(((double) elapsedTime) / (double) exploreDurationMills));
        return ret;
    }

    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        String threadName = System.getProperty("jqf.ei.threadName");
        if (thread.getName().equals(threadName)) {
            return this::handleEvent;
        } else {
            return this::ignoreEvent;
        }
    }

    private Coverage getRunCoverage() {
        if (Boolean.getBoolean("jqf.ei.run_patch")) {
            return runCoverageOfPatch;
        } else {
            return runCoverageOfOrg;
        }
    }

    /** Handles a trace event generated during test execution */
    protected void handleEvent(TraceEvent e) {
        // Collect totalCoverage
        getRunCoverage().handleEvent(e);
        if (Boolean.getBoolean("jqf.ei.run_patch")) {
            targetDistance.handleEvent(e);
        }
        // Check for possible timeouts every so often
        if (this.singleRunTimeoutMillis > 0
                // && (++this.branchCount) % 10_000 == 0
                && this.runStart != null) {
            long elapsed = new Date().getTime() - runStart.getTime();
            if (elapsed > this.singleRunTimeoutMillis) {
                timeOutOccurred = true;
                infoLog("timeout occurred");
                System.err.println("timeout occurred");
                throw new TimeoutException(elapsed, this.singleRunTimeoutMillis);
            }
        }

        if (Boolean.getBoolean("jqf.ei.run_patch")) {
            targetCoverage.handleEvent(e);
        }
    }

    protected void ignoreEvent(TraceEvent e) {
    }

    public ResultOfOrg handleResultOfOrg(Result result, Throwable error) throws GuidanceException {
        boolean inputNotIgnored = false;

        // Stop timeout handling
        this.runStart = null;

        if (Log.getActualCount() > 0) {
            // Trim input (remove unused keys)
            currentInput.gc();

            // It must still be non-empty
            assert (currentInput.size() > 0) : String.format("Empty input: %s", currentInput.getDesc());

            // update inputs
            inputs.add(currentInput);
            inputNotIgnored = true;
        }

        // Get spectrums
        Map<String,Integer> branchSpectrum=runCoverageOfOrg.getBranchSpectrum();
        List<String> pathSpectrum=runCoverageOfOrg.getPathSpectrum();
        Log.logBranchSpectrum(branchSpectrum);
        Log.logPathSpectrum(pathSpectrum);

        boolean newCoverageFound = false;
        String why = "";
        // Coverage before
        int nonZeroBefore = totalCoverageOfOrg.getNonZeroCount();
        int validNonZeroBefore = validCoverageOfOrg.getNonZeroCount();

        // Update total coverage
        boolean coverageBitsUpdated = totalCoverageOfOrg.updateBits(runCoverageOfOrg);
        boolean valid = result == Result.SUCCESS;
        if (valid) {
            validCoverageOfOrg.updateBits(runCoverageOfOrg);
        }

        // Coverage after
        int nonZeroAfter = totalCoverageOfPatch.getNonZeroCount();
        if (nonZeroAfter > maxCoverageOfPatch) {
            maxCoverageOfPatch = nonZeroAfter;
        }

        if (SAVE_NEW_COUNTS && coverageBitsUpdated) {
            newCoverageFound = true;
            why = why + "+count";
        }

        // Save if new total coverage found
        if (nonZeroAfter > nonZeroBefore) {
            newCoverageFound = true;
            why = why + "+cov";
        }

        // Save if new valid coverage is found
        int validNonZeroAfter = validCoverageOfPatch.getNonZeroCount();
        if (this.validityFuzzing && validNonZeroAfter > validNonZeroBefore) {
            newCoverageFound = true;
            why = why + "+valid";
        }

        // displaying stats on every interval is only enabled for AFL-like stats screen
        if (console != null && !LIBFUZZER_COMPAT_OUTPUT) {
            displayStats();
        }

        // Save input unconditionally if such a setting is enabled
        if (savedAllDirectory != null) {
            this.curSaveFileName = String.format("id_%09d", numTrials);
            File saveFile = new File(savedAllDirectory, this.curSaveFileName);
            GuidanceException.wrap(() -> writeCurrentInputToFile(saveFile));
        }

        File inputFile = null;
        if (inputNotIgnored) {
            this.curSaveFileName = String.format("id_%09d", numTrials);
            inputFile = new File(notIgnoreDirectory, this.curSaveFileName);
            final File saved = inputFile;
            GuidanceException.wrap(() -> writeCurrentInputToFile(saved));
        }

        return new ResultOfOrg(inputNotIgnored, currentInput, inputFile, newCoverageFound, valid, why);
    }

    public boolean isWideningPlateauReached() {
        return isWideningPlateauReached;
    }

    private boolean isTargetReached() {
        return targetCoverage.getCoveredTargets().size() > 0;
    }

    private boolean isDuplicate() {
        return inputs.contains(this.currentInput);
    }
}
