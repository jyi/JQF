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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.time.Duration;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.util.TargetCoverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import de.hub.se.cfg.CFGAnalysis;
import kr.ac.unist.cse.jqf.Log;
import kr.ac.unist.cse.jqf.aspect.DumpUtil;
import kr.ac.unist.cse.jqf.aspect.MethodInfo;
import org.apache.commons.text.similarity.LevenshteinDistance;
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
    private boolean isWideningPlateauReached = false;
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
            return compare(i1.getDists(), i2.getDists());
        }

        public int compare(double[] dists1, double[] dists2) {
            for (int i = 0; i < dists1.length; i++) {
                double d1 = dists1[i];
                double d2 = dists2[i];
                if (d1 == d2) {
                    continue;
                } else {
                    if (d1 > d2) return 1;
                    else return -1;
                }
            }
            return 0;
        }
    }

    public class ComparableInput extends LinearInput {

        private double[] dists;

        public ComparableInput() {
            super();
        }

        public ComparableInput(ComparableInput other) {
            super(other);
        }

        public void setDists(double[] dists) {
            this.dists = dists;
        }

        public double[] getDists() {
            return dists;
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

    public class HandleResult {
        private final Input<?> input;
        private final File inputFile;
        private boolean inputNotIgnored;

        public HandleResult(boolean inputNotIgnored, Input<?> input, File inputFile) {
            this.inputNotIgnored = inputNotIgnored;
            this.input = input;
            this.inputFile = inputFile;
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
    }

    public void reset() {
        this.isWideningPlateauReached = false;
        Log.turnOnRunBuggyVersion();
    }

    private double getDiffVal(Object v) {
        double val = Double.NaN;
        if (v instanceof String) {
            boolean done = true;
            String s = (String) v;
            try {
                val = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                done = false;
            }

            if (!done) {
                if(s.equals("true")||s.equals("false")){
                    val = s.equals("true")?1:0;
                }
            }
        }
        return val;
    }

    @Override
    protected void prepareOutputDirectory() throws IOException {
        super.prepareOutputDirectory();

        this.notIgnoreDirectory = new File(outputDirectory, "not_ignore");
        this.notIgnoreDirectory.mkdirs();
    }

    private boolean shouldKeep(boolean toSave, double[] dists) {
        if (savedInputs.isEmpty()) return true;

        if (toSave) {
            Input best = this.savedInputs.get(savedInputs.size() - 1);
            int result = (new InputComparator()).compare(dists, best.getDists());
            if (result > 0) return true;
        }

        long elapsedTime = new Date().getTime() - startTime.getTime();
        double temp = this.temparature(elapsedTime) / 2d;
        if (Math.random() < temp) return true;
        else return false;
    }

    // public void saveInputs(double ... dist)
    public void saveInputs(String reason, boolean valid, double... dist) {
        Set<Object> responsibilities = computeResponsibilities(valid);
        GuidanceException.wrap(() -> saveCurrentInput(dist, responsibilities, reason));
    }

    /* Saves an interesting input to the queue. */
    protected void saveCurrentInput(double[] dist, Set<Object> responsibilities, String why) throws IOException {
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
        if (currentInput instanceof ComparableInput && dist != null) {
            ((ComparableInput) currentInput).setDists(dist);
        }
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

    private double getDistance(String inputID1, String inputID2, List<MethodInfo> methods,
                               Version version1, Version version2, PointCutLocation loc) {
        if (methods == null) return 0;
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

                    Object v1 = cmp.getControlDetails().getValue();
                    double val1 = getDiffVal(v1);
                    Comparison.Detail test = cmp.getTestDetails();
                    Object v2 = test.getValue();
                    double val2 = getDiffVal(v2);
                    if (val1 == Double.MAX_VALUE || val1 == Double.MIN_VALUE || val2 == Double.MAX_VALUE || val2 == Double.MIN_VALUE)
                        continue;
                    if (Double.isNaN(val1) || Double.isNaN(val2)) {
                        String s1 = (String) v1;
                        String s2 = (String) v2;
                        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
                        double strDist = levenshteinDistance.apply(s1, s2);
                        if (strDist == Double.MAX_VALUE || strDist == Double.MIN_VALUE) continue;
                        distance += strDist;
                    } else {
                        distance += Math.abs(val1 - val2);
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to read xml files");
                e.printStackTrace();
            }
        }

        return distance;
    }

    private double getVersionDistance(String inputID, List<MethodInfo> methods, PointCutLocation loc) {
        return getDistance(inputID, inputID, methods, Version.ORG, Version.PATCH, loc);
    }

    private double getParentDistance(String parentID, String inputID, List<MethodInfo> methods, PointCutLocation loc) {
        return getDistance(parentID, inputID, methods, Version.PATCH, Version.PATCH, loc);
    }

    public void noProgress() {
        noProgressCount++;
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

    public void handleResultOfPatch(Result result, boolean targetHit) {
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
        boolean toSave = false;
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
            toSave = true;
            why = why + "+count";
        }

        // Save if new total coverage found
        if (nonZeroAfter > nonZeroBefore) {
            // Must be responsible for some branch
            assert (responsibilities.size() > 0);
            toSave = true;
            why = why + "+cov";
        }

        // Save if new valid coverage is found
        int validNonZeroAfter = validCoverageOfPatch.getNonZeroCount();
        if (this.validityFuzzing && validNonZeroAfter > validNonZeroBefore) {
            // Must be responsible for some branch
            assert (responsibilities.size() > 0);
            currentInput.valid = true;
            toSave = true;
            why = why + "+valid";
        }

        double versionDistCallerExit = 0;
        double versionDistCalleeExit = 0;
        double versionDistCalleeEntry = 0;

        List<MethodInfo> callers = DumpUtil.getCallerChain();
        List<MethodInfo> callees = DumpUtil.getCalleesOfTaregetMethod();

        if (targetHit && callers != null && callees != null) {
            versionDistCallerExit = getVersionDistance(inputID, callers, PointCutLocation.EXIT);
            versionDistCalleeExit = getVersionDistance(inputID, callees, PointCutLocation.EXIT);
            versionDistCalleeEntry = getVersionDistance(inputID, callees, PointCutLocation.ENTRY);
        }
        double[] dists = new double[]{versionDistCallerExit, versionDistCalleeExit, versionDistCalleeEntry};

        if (shouldKeep(toSave, dists)) {
            noProgressCount = 0; // reset
            why = why + "+dist";
            infoLog("new distances: " + distsToString(dists));
            saveInputs(why, valid, dists);
        } else {
            noProgress();
            infoLog("skip duplicate coverage");
        }
    }

    private String distsToString(double[] dists) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dists.length; i++) {
            sb.append(dists[i]);
            if (i < dists.length - 1) sb.append(", ");
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
        buildCFGAnalysis();
    }

    protected void buildCFGAnalysis() {
        // TODO: enable it
//        String classPathForPatch = System.getProperty("jqf.ei.CLASSPATH_FOR_PATCH");
//        if (classPathForPatch != null) {
//            Set<String> classes = CFGBuilder.loadInput(classPathForPatch);
//            Set<String> classesToSkip = new HashSet<>();
//            String additionalClasses = null;
//            cfga = CFGBuilder.genCFGForClasses(classes, classesToSkip, additionalClasses);
//        }
    }

    public void setDiffOutputFound(boolean isDiffOutFound) {
        this.isDiffOutFound = isDiffOutFound;
    }

    @Override
    public boolean hasInput() {
        diffOutFound = false;
        Date now = new Date();
        long elapsedMilliseconds = now.getTime() - startTime.getTime();

        if (timeOutOccurred) return false;

        if (EXIT_ON_PLATEAU && isPlateauReached) {
            System.out.println("stop because plateau is reached");
            return false;
        }

        String inputID = System.getProperty("jqf.ei.inputID");
        String path = outputDirectory.getPath() + File.separator + "diff_out";
        if(!Files.exists(Paths.get(path))) {
            // System.out.println("Current dir using System:" +currentDir);
            try {
                Files.createDirectories(Paths.get(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // return false when the two outputs differ from each other
        if (this.isDiffOutFound) {
            // save the current input into diff_out dir
            try {
                Path inFile = Paths.get(path, inputID);
                Files.createFile(inFile);
                String msg = "diff_output is found";
                Files.write(inFile, msg.getBytes(), StandardOpenOption.APPEND);
                diffOutFound = true;
            } catch (IOException e) {
                System.err.println("Something went wrong while writing diff-revealing input");
                e.printStackTrace();
            }

            infoLog("Diff out is found!");
            System.out.println("Diff out is found!");
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
                    infoLog(distsToString(input.getDists()));
                }
            }

            // The number of children to produce is determined by how much of the coverage
            // pool this parent input hits
            Input currentParentInput = savedInputs.get(currentParentInputIdx);
            if (numChildrenGeneratedForCurrentParentInput >= targetNumChildren) {
                // Select the next saved input to fuzz
                currentParentInputIdx = random.nextInt(savedInputs.size());
                infoLog("currentParentInputIdx: %d / %d", currentParentInputIdx, savedInputs.size());
                targetNumChildren = getTargetChildrenForParent(savedInputs.size(), currentParentInputIdx);
                infoLog("targetNumChildren: %d", targetNumChildren);
                numChildrenGeneratedForCurrentParentInput = 0;
            }
            Input parent = savedInputs.get(currentParentInputIdx);
            parentID = String.format("id_%09d", parent.id);
            // Fuzz it to get a new input
            infoLog("Mutating input: %s", parent.desc);
            currentInput = parent.fuzz(random);
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
        if (appThread != null) {
            throw new IllegalStateException(PoracleGuidance.class +
                    " only supports single-threaded apps at the moment");
        }
        appThread = thread;

        return this::handleEvent;
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
        // Check for possible timeouts every so often
        if (this.singleRunTimeoutMillis > 0
                // && (++this.branchCount) % 10_000 == 0
                && this.runStart != null) {
            long elapsed = new Date().getTime() - runStart.getTime();
            if (elapsed > this.singleRunTimeoutMillis) {
                timeOutOccurred = true;
                System.err.println("timeout occurred");
                throw new TimeoutException(elapsed, this.singleRunTimeoutMillis);
            }
        }

        if (Boolean.getBoolean("jqf.ei.run_patch")) {
            targetCoverage.handleEvent(e);
        }
    }


    public HandleResult handleResultOfOrg(Result result, Throwable error) throws GuidanceException {
        boolean inputNotIgnored = false;

        // Stop timeout handling
        this.runStart = null;

        boolean valid = result == Result.SUCCESS;

        if (Log.getActualCount() > 0) {
            // Trim input (remove unused keys)
            currentInput.gc();

            // It must still be non-empty
            assert (currentInput.size() > 0) : String.format("Empty input: %s", currentInput.getDesc());

            // update inputs
            inputs.add(currentInput);
            inputNotIgnored = true;
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

        return new HandleResult(inputNotIgnored, currentInput, inputFile);
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
