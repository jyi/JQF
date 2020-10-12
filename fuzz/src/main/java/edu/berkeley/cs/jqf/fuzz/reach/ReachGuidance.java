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
import java.util.function.Consumer;
import java.time.Duration;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
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
public class ReachGuidance extends ZestGuidance {

    private TargetCoverage targetCoverage = TargetCoverage.getTargetCoverage();

    private CFGAnalysis cfga = null;

    private final List<Input<?>> inputs = new ArrayList<>();
    private boolean isDiffOutFound = false;
    private BigList<BigList<Double>> stateDiffCoverage = new BigList<>();
    private File notIgnoreDirectory;
    private boolean diffOutFound;
    private static boolean valid = false;
    protected boolean USE_WIDENING_PLATEAU_THRESHOLD =
            System.getProperty("jqf.ei.WIDENING_PLATEAU_THRESHOLD") != null?
                    true : false;
    protected int wideningPlateauThreshold =
            System.getProperty("jqf.ei.WIDENING_PLATEAU_THRESHOLD") != null?
                    Integer.getInteger("jqf.ei.WIDENING_PLATEAU_THRESHOLD") : 10;
    private boolean isWideningPlateauReached = false;

    public void reset() {
        this.isWideningPlateauReached = false;
        this.noProgressCount = 0;
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

    private boolean shouldKeep(BigList<Double> currentStateDiff) {
        System.out.println("|stateDiffCoverage| = " + stateDiffCoverage.size());
        System.out.println("|currentStateDiff| = " + currentStateDiff.size());
        if (stateDiffCoverage.isEmpty()) {
            stateDiffCoverage.add(currentStateDiff);
            return true;
        }

        // TODO: try with various options.
        // Currently, we accumulate distances
        BigList<Double> last = stateDiffCoverage.peekLast();
        double distOfLast = last.getLast();
        double distOfCur = currentStateDiff.getLast();
        System.out.println("distOfLast = " + distOfLast);
        System.out.println("distOfCur = " + distOfCur);
        if (distOfLast < distOfCur) {
            stateDiffCoverage.add(currentStateDiff);
            return true;
        }
    //    TargetCoverage.resetHit();
//        if (TargetCoverage.isTargetHit() && Log.getActualCount() > 0) {
//
//           return true;
//        }

        return false;
    }

    public void saveInputs() {
        Set<Object> responsibilities = computeResponsibilities(ReachGuidance.valid);
        String reason = "+poracle";
        GuidanceException.wrap(() -> saveCurrentInput(responsibilities, reason));
    }

    public boolean isDiffOutFound() {
        return this.diffOutFound;
    }

    private double compareDifferences(String logDir, String inputID, BigList<Double> currentStateDiff,
                                  List<MethodInfo> methods, String suffix) {
        if (methods == null) return 0;

        double distance = 0d;
        for (int i = methods.size() - 1; i >= 0; i--) {
            MethodInfo m  = methods.get(i);
            Path orgD = Paths.get(logDir + File.separator + "ORG", inputID, m.getMethodName()+suffix+".xml");
            Path patchD = Paths.get(logDir + File.separator + "PATCH", inputID, m.getMethodName()+suffix+".xml");
            if(!Files.exists(orgD)||!Files.exists(patchD)){
                System.out.println("No matching method exists");
                continue;
            }
            try {
                String orgContents = new String(Files.readAllBytes(orgD));
                String patchContents = new String(Files.readAllBytes(patchD));
                Diff myDiff = DiffBuilder.compare(orgContents).withTest(patchContents).build();
                for (Difference diff : myDiff.getDifferences()) {

                    Comparison cmp = diff.getComparison();

                    // TODO: this is temporary code to ignore random variables
                    if (cmp.getControlDetails().getParentXPath().contains("random"))
                        continue;

                    Comparison.Detail control = cmp.getControlDetails();
                    Object v1 = control.getValue();
                    double val1 = getDiffVal(v1);
                    Comparison.Detail test = cmp.getTestDetails();
                    Object v2 = test.getValue();
                    double val2 = getDiffVal(v2);
                    if(Double.isNaN(val1)||Double.isNaN(val2)){
                        String s1 = (String) v1;
                        String s2 = (String) v2;
                        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
                        distance += levenshteinDistance.apply(s1,s2);
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

    public void handleResult() {
        // TODO: compute the difference between the two xml files
        String logDir = System.getProperty("jqf.ei.logDir");
        String inputID = System.getProperty("jqf.ei.inputID");
        List<MethodInfo> callers = DumpUtil.getCallers();
        List<MethodInfo> callees = DumpUtil.getCallees();

        if ((callers == null && callees == null)) return;

        BigList<Double> currentStateDiff = new BigList<>();
        // compute total distance at the caller level
        double distance = compareDifferences(logDir, inputID, currentStateDiff, callers, "Exit");
        currentStateDiff.add(distance);

        // compute total distance at the callee (exit)
        distance = compareDifferences(logDir, inputID, currentStateDiff, callees, "Exit");
        currentStateDiff.add(distance);

        distance = compareDifferences(logDir, inputID, currentStateDiff, callees, "Entry");
        currentStateDiff.add(distance);

        if (shouldKeep(currentStateDiff)) {
            saveInputs();
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

    /**
     * Creates a new guidance instance.
     *
     * @param testName the name of test to display on the status screen
     * @param duration the amount of time to run fuzzing for, where
     *                 {@code null} indicates unlimited time.
     * @param outputDirectory the directory where fuzzing results will be written
     * @throws IOException if the output directory could not be prepared
     */
    public ReachGuidance(String testName, long seed,
                         Duration duration, File outputDirectory) throws IOException {
        super(testName, duration, outputDirectory);
        if (seed != -1) this.random.setSeed(seed);
        buildCFGAnalysis();
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
    public ReachGuidance(String testName, long seed,
                         Duration duration, File outputDirectory,
                         File[] seedInputFiles) throws IOException {
        super(testName, duration, outputDirectory, seedInputFiles);
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

        if (USE_CORPUS_SIZE && this.curCorpusSize > this.maxCorpusSize) {
            System.out.println("stop because corpus size exceeds the max");
            return false;
        }
        String inputID = System.getProperty("jqf.ei.inputID");
        String currentDir = System.getProperty("user.dir");
        String path = outputDirectory.getPath()+"/diff_out";
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

            System.out.println("stop because diff out is found");
            return false;
        }
        return elapsedMilliseconds < maxDurationMillis;
    }

    @Override
    public InputStream getInput() throws GuidanceException {
        targetCoverage.clear();
        return super.getInput();
    }

    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        if (appThread != null) {
            throw new IllegalStateException(ReachGuidance.class +
                    " only supports single-threaded apps at the moment");
        }
        appThread = thread;

        return this::handleEvent;
    }

    /** Handles a trace event generated during test execution */
    protected void handleEvent(TraceEvent e) {
        super.handleEvent(e);
        targetCoverage.handleEvent(e);
    }

    public HandleResult handleResultOfOrg(Result result, Throwable error) throws GuidanceException {
        boolean inputNotIgnored = false;

        // Stop timeout handling
        this.runStart = null;

        // Increment run count
        this.numTrials++;

        boolean valid = result == Result.SUCCESS;
        ReachGuidance.valid = valid;
        if (valid) {
            // Increment valid counter
            numValid++;
        }

        if (result == Result.SUCCESS || result == Result.INVALID) {

            // Coverage before
            int nonZeroBefore = totalCoverage.getNonZeroCount();
            int validNonZeroBefore = validCoverage.getNonZeroCount();

            // Update total coverage
            boolean coverageBitsUpdated = totalCoverage.updateBits(runCoverage);
            if (valid) {
                validCoverage.updateBits(runCoverage);
            }

            // Coverage after
            int nonZeroAfter = totalCoverage.getNonZeroCount();
            if (nonZeroAfter > maxCoverage) {
                maxCoverage = nonZeroAfter;
                noProgressCount = 0; // reset
            } else {
                noProgressCount++;
                if (USE_WIDENING_PLATEAU_THRESHOLD && noProgressCount > wideningPlateauThreshold) {
                    System.out.println("A widening plateau is reached!!!");
                    isWideningPlateauReached = true;
                }
            }
            int validNonZeroAfter = validCoverage.getNonZeroCount();

            // Possibly save input
            boolean toSave = false;
            String why = "";

            // Save if the target is reached.
            if (Log.getActualCount() > 0 && !isDuplicate()) {
                // Trim input (remove unused keys)
                currentInput.gc();

                // It must still be non-empty
                assert(currentInput.size() > 0) : String.format("Empty input: %s", currentInput.getDesc());

                // update inputs
                inputs.add(currentInput);
                inputNotIgnored = true;
            }
        } else if (result == Result.FAILURE || result == Result.TIMEOUT) {
            String msg = error.getMessage();

            // Get the root cause of the failure
            Throwable rootCause = error;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            // Attempt to add this to the set of unique failures
            if (uniqueFailures.add(Arrays.asList(rootCause.getStackTrace()))) {
                // Trim input (remove unused keys)
                currentInput.gc();

                // It must still be non-empty
                assert(currentInput.size() > 0) : String.format("Empty input: %s", currentInput.getDesc());

                // Save crash to disk
                int crashIdx = uniqueFailures.size()-1;
                String saveFileName = String.format("id_%06d", crashIdx);
                File saveFile = new File(savedFailuresDirectory, saveFileName);
                GuidanceException.wrap(() -> writeCurrentInputToFile(saveFile));
                infoLog("%s","Found crash: " + error.getClass() + " - " + (msg != null ? msg : ""));
                String how = currentInput.getDesc();
                String why = result == Result.FAILURE ? "+crash" : "+hang";
                infoLog("Saved - %s %s %s", saveFile.getPath(), how, why);

                if (EXACT_CRASH_PATH != null && !EXACT_CRASH_PATH.equals("")) {
                    File exactCrashFile = new File(EXACT_CRASH_PATH);
                    GuidanceException.wrap(() -> writeCurrentInputToFile(exactCrashFile));
                }

                // libFuzzerCompat stats are only displayed when they hit new coverage or crashes
                if (console != null && LIBFUZZER_COMPAT_OUTPUT) {
                    displayStats();
                }
            }
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
