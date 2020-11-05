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


package edu.berkeley.cs.jqf.fuzz.ei;

import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.reach.PoracleGuidance;
import edu.berkeley.cs.jqf.fuzz.reach.Target;
import edu.berkeley.cs.jqf.fuzz.soot.examples.Circle;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import kr.ac.unist.cse.jqf.Log;
import org.junit.Test;
import org.junit.runner.Result;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.io.File;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CLI for Zest based guidance.
 *
 * @author Yevgeny Pats
 */
@CommandLine.Command(name = "ZestCLI", mixinStandardHelpOptions = true, version = "1.3")
public class ZestCLI2 implements Runnable {

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..2")
    Dependent dependent;

    static class Dependent {
        @Option(names = { "-e", "--exit-on-crash" },
                description = "Exit fuzzer on first crash (default: false)")
        boolean exitOnCrash = false;

        @Option(names = { "--exact-crash-path" },
                description = "exact path for the crash")
        String exactCrashPath;

        @Option(names = { "--exit-on-plateau" },
                description = "Exit fuzzer when plateau is reached (default: false)")
        boolean exitOnPlateau = false;
    }

    @Option(names = "--run-buggy-version", description =  "do you run a buggy version?")
    public boolean runBuggyVersion = false;

    @Option(names = "--logdir", description = "log directory")
    public String logDir = null;

    @Option(names = { "--save-all-inputs" },
            description = "Save all inputs")
    boolean saveAllInputs = false;

    @Option(names = { "--verbose" },
            description = "verbose")
    boolean verbose = false;

    @Option(names = { "--max-corpus-size" },
            description = "Max Corpus size")
    int maxCorpusSize = -1; // negative to denote that the option is not used

    @Option(names = { "--timeout" },
            description = "Timeout")
    int timeout = 3600000;

    @Option(names = { "--max-mutations" },
            description = "Timeout")
    int maxMutations = 100;

    @Option(names = { "--aop" }, description = "aop")
    public String aop = System.getProperty("user.dir") + File.separator + ".." + File.separator + "aspect" + File.separator + "aop.xml";

    @Option(names = { "--delta" },
            description = "Delta")
    double delta = 0;

    @Option(names = { "--widening-plateau-threshold" },
            description = "Widening plateau threshold")
    int wideningPlateauThreshold = -1; // negative to denote that the option is not used

    @Option(names = { "--widening-proportion" },
    description = "widening proportion")
    double widenProportion = 1;

    @Option(names = { "-l", "--libfuzzer-compat-output" },
            description = "Use libFuzzer compat output instead of AFL like stats screen (default: false)")
    private boolean libFuzzerCompatOutput = false;

    @Option(names = { "-i", "--input" },
            description = "Input directory containing seed test cases (default: none)")
    private File inputDirectory;

    @Option(names = { "-o", "--output" },
            description = "Output Directory containing results (default: fuzz_results)")
    private File outputDirectory = new File("fuzz-results");

    @Option(names = { "-d", "--duration" },
            description = "Total fuzz duration (e.g. PT5s or 5s)")
    private Duration duration;

    @Option(names = { "--exploreDuration" },
            description = "explore duration (e.g. PT5s or 5s)")
    private Duration exploreDuration;

    @Option(names = { "-b", "--blind" },
            description = "Blind fuzzing: do not use coverage feedback (default: false)")
    private boolean blindFuzzing;

    @Option(names = { "-t", "--target" }, split = ",",
            description = "")
    private Target[] targets;

    @Option(names = { "--seed" },
            description = "seed")
    private long seed = -1; // negative to denote that the option is not used

    @Parameters(index = "0", paramLabel = "CLASSPATH_FOR_ORG", description = "classpath for fuzz target and all dependencies")
    private String classPathForOrg;

    @Parameters(index = "1", paramLabel = "CLASSPATH_FOR_PATCH", description = "classpath for fuzz target and all dependencies")
    private String classPathForPatch;

    @Parameters(index="2", paramLabel = "TEST_CLASS", description = "full class name where the fuzz function is located")
    private String testClassName;

    @Parameters(index="3", paramLabel = "TEST_METHOD", description = "fuzz function name")
    private String testMethodName;

    public static ClassLoader loaderForOrg;

    private File[] readSeedFiles() {
        if (this.inputDirectory == null) {
            return new File[0];
        }

        ArrayList<File> seedFilesArray = new ArrayList<>();
        File[] allFiles = this.inputDirectory.listFiles();
        if (allFiles == null) {
            // this means the directory doesn't exist
            return new File[0];
        }
        for (int i = 0; i < allFiles.length; i++) {
            if (allFiles[i].isFile()) {
                seedFilesArray.add(allFiles[i]);
            }
        }
        File[] seedFiles = seedFilesArray.toArray(new File[seedFilesArray.size()]);
        return seedFiles;
    }

    public void run() {

        File[] seedFiles = readSeedFiles();

        if (this.dependent != null) {
            if (this.dependent.exitOnCrash) {
                System.setProperty("jqf.ei.EXIT_ON_CRASH", "true");
            }

            if (this.dependent.exactCrashPath != null) {
                System.setProperty("jqf.ei.EXACT_CRASH_PATH", this.dependent.exactCrashPath);
            }

            if (this.dependent.exitOnPlateau) {
                System.setProperty("jqf.ei.EXIT_ON_PLATEAU", "true");
            }
        }

        if (runBuggyVersion) {
            Log.runBuggyVersion = true;
        }

        if (logDir != null)
            System.setProperty("jqf.ei.logDir", logDir);

        if (this.wideningPlateauThreshold >= 0) {
            System.setProperty("jqf.ei.WIDENING_PLATEAU_THRESHOLD",
                    String.valueOf(this.wideningPlateauThreshold));
        }

        if (this.maxCorpusSize >= 0) {
            System.setProperty("jqf.ei.MAX_CORPUS_SIZE", String.valueOf(this.maxCorpusSize));
        }

        if (this.timeout >= 0) {
            System.setProperty("jqf.ei.TIMEOUT",
                    String.valueOf(this.timeout));
        }
        if (this.maxMutations >= 0) {
            System.setProperty("jqf.ei.MAX_MUTATIONS",
                    String.valueOf(this.maxMutations));
        }

        if (this.saveAllInputs) {
            System.setProperty("jqf.ei.SAVE_ALL_INPUTS", "true");
        } else {
            System.setProperty("jqf.ei.SAVE_ALL_INPUTS", "false");
        }

        if (this.classPathForPatch != null) {
            System.setProperty("jqf.ei.CLASSPATH_FOR_PATCH", this.classPathForPatch);
            System.setProperty("jqf.ei.run_two_versions", "true");
        } else {
            System.setProperty("jqf.ei.run_two_versions", "false");
        }

        if (this.verbose) {
            System.setProperty("jqf.ei.verbose", "true");
        } else {
            System.setProperty("jqf.ei.verbose", "false");
        }

        if (this.outputDirectory != null) {
            System.setProperty("jqf.ei.outputDirectory", this.outputDirectory.toString());
        }

        if (this.libFuzzerCompatOutput) {
            System.setProperty("jqf.ei.LIBFUZZER_COMPAT_OUTPUT", "true");
        }

        if (this.aop != null) {
            System.setProperty("org.aspectj.weaver.loadtime.configuration", "file:"+aop);
        }

        System.setProperty("jqf.ei.widenProportion", String.valueOf(this.widenProportion));
        System.setProperty("jqf.ei.delta", String.valueOf(delta));

        try {
            ClassLoader loaderForOrg = new InstrumentingClassLoader(
                    this.classPathForOrg.split(File.pathSeparator),
                    ZestCLI2.class.getClassLoader());
            ZestCLI2.loaderForOrg = loaderForOrg;

            // Load the guidance
            String title = this.testClassName +"#"+this.testMethodName;
            ZestGuidance guidance;
            if (targets != null) {
                System.setProperty("jqf.ei.targets", Arrays.toString(targets));
                callGraphTest(classPathForOrg,targets[0].getClassName());
                // TODO: we need to store target methods
                extractTargetMethod(targets);
                guidance = seedFiles.length > 0 ?
                        new PoracleGuidance(title, this.seed, duration, exploreDuration, this.outputDirectory, seedFiles) :
                        new PoracleGuidance(title, this.seed, duration, exploreDuration, this.outputDirectory);
            } else {
                guidance = seedFiles.length > 0 ?
                        new ZestGuidance(title, duration, this.outputDirectory, seedFiles) :
                        new ZestGuidance(title, duration, this.outputDirectory);
            }
            guidance.setBlind(blindFuzzing);
            // Run the Junit test (original version)
            Result res = GuidedFuzzing.run(testClassName, testMethodName, loaderForOrg, guidance, System.out);
            if (Boolean.getBoolean("jqf.logCoverage")) {
                System.out.println(String.format("Covered %d edges.",
                        guidance.getTotalCoverage().getNonZeroCount()));
            }
            if (Boolean.getBoolean("jqf.ei.EXIT_ON_CRASH") && !res.wasSuccessful()) {
                System.exit(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

    }

    public void callGraphTest(String sourceDirectory, String cls) {
        setupSoot(sourceDirectory,cls);
        SootClass targetClass = Scene.v().getSootClass(cls);
        CallGraph callGraph = Scene.v().getCallGraph();
//        System.out.println(callGraph.toString());
    }

    private static void setupSoot(String sourceDirectory,String cls) {
        G.reset();
        Options.v().set_keep_line_number(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(sourceDirectory);
        SootClass appclass = Scene.v().loadClassAndSupport(cls);
        String soot_cp = Options.v().soot_classpath();
        System.out.println("soot_cp: " + soot_cp);
        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();
    }

    private void extractTargetMethod(Target[] targets) {
        ClassPool pool = ClassPool.getDefault();
        try {
            for (String cp: removeJar(this.classPathForPatch).split(":")) {
                pool.insertClassPath(cp);
            }
            for (Target target: targets) {
                CtClass cc = pool.get(target.getClassName());
                // TODO: find a target method
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private String removeJar(String classPath) {
        return Arrays.stream(classPath.split(":")).filter(s -> !s.endsWith(".jar")).collect(Collectors.joining(":"));
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ZestCLI2())
                .registerConverter(Duration.class, v -> {
                    try {
                        return Duration.parse(v);
                    } catch (DateTimeParseException e) {
                        return Duration.parse("PT" + v);
                    }
                })
                .registerConverter(Target.class, Target::parse)
                .execute(args);
        System.exit(exitCode);
    }
}
