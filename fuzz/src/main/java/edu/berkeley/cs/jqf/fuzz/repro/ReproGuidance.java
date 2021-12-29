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
package edu.berkeley.cs.jqf.fuzz.repro;

import com.ibm.wala.analysis.exceptionanalysis.IntraproceduralExceptionAnalysis;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.BranchEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.CallEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import kr.ac.unist.cse.jqf.Log;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.csv.CSVFormatter;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * A front-end that provides a specified set of inputs for test
 * case reproduction,
 *
 * This class enables reproduction of a test case with an input file
 * generated by a guided fuzzing front-end such as AFL.
 *
 * @author Rohan Padhye
 */
public class ReproGuidance implements Guidance {
    private final File[] inputFiles;
    private final File traceDir;
    private int nextFileIdx = 0;
    private List<PrintStream> traceStreams = new ArrayList<>();
    private InputStream inputStream;
    private Coverage coverage = new Coverage();

    private Set<String> branchesCoveredInCurrentRun;
    private Set<String> allBranchesCovered;

    /** Coverage statistics for a single run. */
    protected Coverage runCoverage = new Coverage();

    private boolean ignoreInvalidCoverage;
    private boolean printArgs;

    /** The file where log data is written. */
    protected File logFile;
    /** The directory where fuzzing results are produced. */
    protected final File outputDirectory;

    protected final boolean verbose = true;

    HashMap<Integer, String> branchDescCache = new HashMap<>();

    private boolean useSeed = false;

    public boolean doNotLog = false;


    /**
     * Constructs an instance of ReproGuidance with a list of
     * input files to replay and a directory where the trace
     * events may be logged.
     *
     * @param inputFiles a list of input files
     * @param traceDir an optional directory, which if non-null will
     *                 be the destination for log files containing event
     *                 traces
     */
    public ReproGuidance(File[] inputFiles, File traceDir, File outputDirectory) {
        this.inputFiles = inputFiles;
        this.traceDir = traceDir;
        if (Boolean.getBoolean("jqf.repro.logUniqueBranches")) {
            allBranchesCovered = new HashSet<>();
            branchesCoveredInCurrentRun = new HashSet<>();
            ignoreInvalidCoverage = Boolean.getBoolean("jqf.repro.ignoreInvalidCoverage");
        }
        printArgs = Boolean.getBoolean("jqf.repro.printArgs");

        this.outputDirectory = outputDirectory;
        logFile = new File(outputDirectory, "fuzz.log");
    }

    public ReproGuidance(File[] inputFiles, File traceDir, File outputDirectory, boolean useSeed) {
        this.inputFiles = inputFiles;
        this.traceDir = traceDir;
        if (Boolean.getBoolean("jqf.repro.logUniqueBranches")) {
            allBranchesCovered = new HashSet<>();
            branchesCoveredInCurrentRun = new HashSet<>();
            ignoreInvalidCoverage = Boolean.getBoolean("jqf.repro.ignoreInvalidCoverage");
        }
        printArgs = Boolean.getBoolean("jqf.repro.printArgs");

        this.outputDirectory = outputDirectory;
        this.useSeed = useSeed;
        logFile = new File(outputDirectory, "fuzz.log");

    }


    /**
     * Constructs an instance of ReproGuidance with a single
     * input file to replay and a directory where the trace
     * events may be logged.
     *
     * @param inputFile an input file
     * @param traceDir an optional directory, which if non-null will
     *                 be the destination for log files containing event
     *                 traces
     */
    public ReproGuidance(File inputFile, File traceDir, File outputDirectory) {
        this(new File[]{inputFile}, traceDir, outputDirectory);
    }

    public ReproGuidance(File inputFile, File traceDir, File outputDirectory, boolean useSeed) {
        this(new File[]{inputFile}, traceDir, outputDirectory, useSeed);
    }

    /**
     * Returns an input stream corresponding to the next input file.
     *
     * @return an input stream corresponding to the next input file
     */
    @Override
    public InputStream getInput() {
//        runCoverage.clear();
        try {
            //Not sure? But There are difference between inputs for zest and repro
            File inputFile = inputFiles[nextFileIdx];
            System.out.println("Save in Repro: " + inputFile.toString());
//            if (nextFileIdx > 0) {
//                inputFile = inputFiles[nextFileIdx-1];
//            }
//            else {
//                inputFile = inputFiles[nextFileIdx];
//            }

//            if (inputFile.toString().contains("id_000000001") || inputFile.toString().contains("id_000000000") || this.useSeed == true) {
//                System.setProperty("kr.ac.unist.cse.jqf.USE_SEED",Boolean.toString(true));
//                System.out.println("UseSeed in Repro");
//            }
//            else {
//                System.setProperty("kr.ac.unist.cse.jqf.USE_SEED",Boolean.toString(false));
//            }
            this.inputStream = new BufferedInputStream(new FileInputStream(inputFile));

            if (allBranchesCovered != null) {
                branchesCoveredInCurrentRun.clear();
            }

            return this.inputStream;
        } catch (IOException e) {
            throw new GuidanceException(e);
        }
    }

    /**
     * Returns <tt>true</tt> if there are more input files to replay.
     * @return <tt>true</tt> if there are more input files to replay
     */
    @Override
    public boolean hasInput() {
        return nextFileIdx < inputFiles.length;
    }

    @Override
    public void observeGeneratedArgs(Object[] args) {
        if (printArgs) {
            String inputFileName = getCurrentInputFile().getName();
            for (int i = 0; i < args.length; i++) {
                System.out.printf("%s[%d]: %s\n", inputFileName, i, String.valueOf(args[i]));
            }
        }
    }

    /** Writes a line of text to the log file. */
    public void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            if (logFile != null) {
                appendLineToFile(logFile, line);
            } else {
                System.err.println(line);
            }
        }
    }

    /** Writes a line of text to a given log file. */
    protected void appendLineToFile(File file, String line) throws GuidanceException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println(line);
        } catch (IOException e) {
            throw new GuidanceException(e);
        }
    }

    /**
     * Returns the input file which is currently being repro'd.
     * @return the current input file
     */
    private File getCurrentInputFile() {
        return inputFiles[nextFileIdx];
    }

    /**
     * Logs the end of run in the log files, if any.
     *
     * @param result   the result of the fuzzing trial
     * @param error    the error thrown during the trial, or <tt>null</tt>
     */
    @Override
    public void handleResult(Result result, Throwable error) {
        // Close the open input file
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new GuidanceException(e);
        }

        // Print result
        File inputFile = getCurrentInputFile();
        if (result == Result.FAILURE) {
            System.out.printf("%s ::= %s (%s)\n", inputFile.getName(), result, error.getClass().getName());
        } else {
            System.out.printf("%s ::= %s\n", inputFile.getName(), result);
        }

        // Get spectrums
        Map<String,Integer> branchSpectrum=coverage.getBranchSpectrum();
        List<String> pathSpectrum=coverage.getPathSpectrum();

        if (!doNotLog || true) {
            if(System.getProperty("jqf.ei.run_patch").equals("true")) {
                System.out.println("HandleResultPatch in Repro");
                Log.logBranchSpectrum(branchSpectrum,true);
                Log.logPathSpectrum(pathSpectrum,true);
                Log.logJson(true);
            }
            else {
                Log.logBranchSpectrum(branchSpectrum,false);
                Log.logPathSpectrum(pathSpectrum,false);
                Log.logJson(false);
            }
        }

//        Log.logJson(false);

        // Possibly accumulate coverage
        if (allBranchesCovered != null && (ignoreInvalidCoverage == false || result == Result.SUCCESS)) {
            assert branchesCoveredInCurrentRun != null;
            allBranchesCovered.addAll(branchesCoveredInCurrentRun);
        }

        // Maybe add to results csv
        if (traceDir != null) {
            File resultsCsv = new File(traceDir, "results.csv");
            boolean append = nextFileIdx > 0; // append for all but the first input
            try (PrintStream out = new PrintStream(new FileOutputStream(resultsCsv, append))) {
                String inputName = getCurrentInputFile().toString();
                String exception = result == Result.FAILURE ? error.getClass().getName() : "";
                out.printf("%s,%s,%s\n", inputName, result, exception);
            } catch (IOException e) {
                throw new GuidanceException(e);
            }
        }

        // Maybe checkpoint JaCoCo coverage
        String jacocoAccumulateJar = System.getProperty("jqf.repro.jacocoAccumulateJar");
        if (jacocoAccumulateJar != null) {
            String dir = System.getProperty("jqf.repro.jacocoAccumulateDir", ".");
            jacocoCheckpoint(new File(jacocoAccumulateJar), new File(dir));

        }

        // Increment file
        nextFileIdx++;


    }

    public void handleResultPatch(Result result, Throwable error) {

        // Close the open input file
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new GuidanceException(e);
        }

        // Get spectrums
        Map<String,Integer> branchSpectrum=coverage.getBranchSpectrum();
        List<String> pathSpectrum=coverage.getPathSpectrum();
        Log.logBranchSpectrum(branchSpectrum,true);
        Log.logPathSpectrum(pathSpectrum,true);
        Log.logJson(true);

    }

    /**
     * Returns a callback that can log trace events or code coverage info.
     *
     * <p>If the system property <tt>jqf.repro.logUniqueBranches</tt> was
     * set to <tt>true</tt>, then the callback collects coverage info into
     * the set {@link #branchesCoveredInCurrentRun}, which can be accessed using
     * {@link #getBranchesCovered()}.</p>
     *
     * <p>Otherwise, if the <tt>traceDir</tt> was non-null during the construction of
     * this Guidance instance, then one log file per thread of
     * execution is created in this directory. The callbacks generated
     * by this method write trace event descriptions in sequence to
     * their own thread's log files.</p>
     *
     * <p>If neither of the above are true, the returned callback simply updates
     * a total coverage map (see {@link #getCoverage()}.</p>
     *
     * @param thread the thread whose events to handle
     * @return a callback to log code coverage or execution traces
     */
    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        if (branchesCoveredInCurrentRun != null) {
            return (e) -> {
                coverage.handleEvent(e);
                if (e instanceof BranchEvent) {
                    BranchEvent b = (BranchEvent) e;
                    int hash = b.getIid() * 31 + b.getArm();
                    String str = branchDescCache.get(hash);
                    if (str == null) {
                        str = String.format("(%09d) %s#%s():%d [%d]", b.getIid(), b.getContainingClass(), b.getContainingMethodName(),
                                b.getLineNumber(), b.getArm());
                        branchDescCache.put(hash, str);
                    }
                    branchesCoveredInCurrentRun.add(str);
                } else if (e instanceof CallEvent) {
                    CallEvent c = (CallEvent) e;
                    String str = branchDescCache.get(c.getIid());
                    if (str == null) {
                        str = String.format("(%09d) %s#%s():%d --> %s", c.getIid(), c.getContainingClass(), c.getContainingMethodName(),
                                c.getLineNumber(), c.getInvokedMethodName());
                        branchDescCache.put(c.getIid(), str);
                    }
                    branchesCoveredInCurrentRun.add(str);
                }
            };
        } else if (traceDir != null) {
            File traceFile = new File(traceDir, thread.getName() + ".log");
            try {
                PrintStream out = new PrintStream(traceFile);
                traceStreams.add(out);

                // Return an event logging callback
                return (e) -> {
                    coverage.handleEvent(e);
                    out.println(e);
                };
            } catch (FileNotFoundException e) {
                // Note the exception, but ignore trace events
                System.err.println("Could not open trace file: " + traceFile.getAbsolutePath());
            }
        }

        // If none of the above work, just update coverage
        return coverage::handleEvent;

    }

    /**
     * Returns a reference to the coverage statistics.
     * @return a reference to the coverage statistics
     */
    public Coverage getCoverage() {
        return coverage;
    }


    /**
     * Retyrns the set of branches covered by this repro.
     *
     * <p>This set will only be non-empty if the system
     * property <tt>jqf.repro.logUniqueBranches</tt> was
     * set to <tt>true</tt> before the guidance instance
     * was constructed.</p>
     *
     * <p>The format of each element in this set is a
     * custom format that strives to be both human and
     * machine readable.</p>
     *
     * <p>A branch is only logged for inputs that execute
     * successfully. In particular, branches are not recorded
     * for failing runs or for runs that violate assumptions.</p>
     *
     * @return the set of branches covered by this repro
     */
    public Set<String> getBranchesCovered() {
        return allBranchesCovered;
    }


    public void jacocoCheckpoint(File classFile, File csvDir) {
        int idx = nextFileIdx;
        csvDir.mkdirs();
        try {
            // Get exec data by dynamically calling RT.getAgent().getExecutionData()
            Class RT = Class.forName("org.jacoco.agent.rt.RT");
            Method getAgent = RT.getMethod("getAgent");
            Object agent = getAgent.invoke(null);
            Method dump = agent.getClass().getMethod("getExecutionData", boolean.class);
            byte[] execData = (byte[]) dump.invoke(agent, false);

            // Analyze exec data
            ExecFileLoader loader = new ExecFileLoader();
            loader.load(new ByteArrayInputStream(execData));
            final CoverageBuilder builder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(loader.getExecutionDataStore(), builder);
            analyzer.analyzeAll(classFile);

            // Generate CSV
            File csv = new File(csvDir, String.format("cov-%05d.csv", idx));
            try (FileOutputStream out = new FileOutputStream(csv)) {
                IReportVisitor coverageVisitor = new CSVFormatter().createVisitor(out);
                coverageVisitor.visitBundle(builder.getBundle("JQF"), null);
                coverageVisitor.visitEnd();
                out.flush();
            }


        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void setDoNotLog (boolean doNot) {
        this.doNotLog = doNot;
    }

}
