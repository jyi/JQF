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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.reach.Target;
import kr.ac.unist.cse.jqf.Log;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * @author Rohan Padhye
 */
@Command(name = "ReproDriver", mixinStandardHelpOptions = true)
public class ReproDriver implements Runnable {

    @Parameters(index = "0") String testClassName;
    @Parameters(index = "1") String testMethodName;
    @Parameters(index = "2") File[] testInputFiles;
    @Parameters(index = "3") String classPath;
//    @Parameters(index = "4") String logDir;

    @Option(names = "--logdir", description = "log directory")
    public String logDir = null;

    @Option(names = "--batch", description = "batch mode")
    public Boolean batch = false;
//
//    @Option(names = "--cpTarget", description = "classpath")
//    public String classPath = null;


    @Option(names = "--run-buggy-version", description =  "do you run a buggy version?")
    public boolean runBuggyVersion = false;

    @Option(names = {"--cp-for-patch"}, description = "classpath for patched program")
    private String classPathForPatch = null;

    @Option(names={"--multi-fuzzing"},description="multi version differential fuzzing")
    private boolean multiFuzz=false;

    @Option(names = { "-t", "--target" }, split = ",",
            description = "")
    private String[] targets;

    @Option(names = { "--failing-tests" }, split = ",",
            description = "")
    private String[] failTests;

    @Option(names = {"--patch-id"}, description = "patch name")
    private String patchID = null;

    @Override
    public void run() {
        System.out.println("Repro");
        try {
            if (logDir != null)
                System.setProperty("jqf.ei.logDir", logDir);

            System.setProperty("kr.ac.unist.cse.jqf.BATCH",Boolean.toString(this.batch));
            System.setProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ",Boolean.toString(this.multiFuzz));
            System.setProperty("kr.ac.unist.cse.jqf.IS_REPRO", Boolean.toString(true));
            System.setProperty("kr.ac.unist.cse.jqf.ONLY_DIFF", Boolean.toString(false));
            System.setProperty("kr.ac.unist.cse.jqf.NO_FUZZ", "true");
            System.setProperty("kr.ac.unist.cse.jqf.IGNORE_COND",Boolean.toString(true));
            System.setProperty("kr.ac.unist.cse.jqf.GO_ON",Boolean.toString(false));
            System.setProperty("kr.ac.unist.cse.jqf.USE_SEED",Boolean.toString(false));
            System.setProperty("jqf.ei.PATCHED_METHOD", "");
            System.setProperty("jqf.ei.repro_fix", "true");
            System.setProperty("jqf.ei.PATCH_ID", this.patchID);


            if (this.classPath != null) {
                System.setProperty("jqf.ei.CLASSPATH_FOR_PATCH", this.classPath);
            }

            if (targets != null) {
                System.setProperty("jqf.ei.targets", Arrays.toString(targets));
                edu.berkeley.cs.jqf.instrument.tracing.Target.init(Arrays.toString(targets));
            }

            if (failTests != null) {
                System.setProperty("jqf.ei.fail_tests", Arrays.toString(failTests));
            }

            System.out.println(testInputFiles[0].toString());
            if (System.getProperty("kr.ac.unist.cse.jqf.BATCH").equals("true")) {
                File dir = testInputFiles[0];
                File files[] = dir.listFiles();

                for (int i = 0; i < files.length; i++) {
                    System.out.println("file: " + files[i]);
                }

                testInputFiles = files;
            }

            if (runBuggyVersion) {
                Log.runBuggyVersion = true;
                kr.ac.unist.cse.jqf.Log.runBuggyVersion = true;
            }

            // Maybe log the trace
            String traceDirName = System.getProperty("jqf.repro.traceDir");
            File traceDir = traceDirName != null ? new File(traceDirName) : null;

            // Load the guidance
            ReproGuidance guidance;

            ClassLoader classLoader = new InstrumentingClassLoader(
                    this.classPath.split(File.pathSeparator),
                    ReproDriver.class.getClassLoader());

            // Run the Junit test
            for (File inputFile : testInputFiles) {
                guidance = new ReproGuidance(inputFile, traceDir, null);
                String eachLog = logDir + "/" + inputFile.toString().split("ids/")[1];
                System.out.println("Each LogDir: " + eachLog);
                System.setProperty("jqf.ei.logDir", eachLog);
                // make sure that target classes are instrumented

                for (String target : targets) {
                    System.out.println("Instrument: " + target.replace(".java", "").replace("/", ".").split(":")[0]);
                    ((InstrumentingClassLoader) classLoader).instrumentClass(target.replace(".java", "").replace("/", ".").split(":")[0]);
                }


                GuidedFuzzing.run(testClassName, testMethodName, classLoader, guidance, System.out);

                if (guidance.getBranchesCovered() != null) {
                    String cov = "";
                    for (String s : guidance.getBranchesCovered()) {
                        cov += "# Covered: " + s + "\n";
                    }
                    final String finalFooter = cov;
                    System.out.println(finalFooter);
                }

                if (Boolean.getBoolean("jqf.logCoverage")) {
                    System.out.println(String.format("Covered %d edges.",
                            guidance.getCoverage().getNonZeroCount()));
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

//    public static void main(String[] args) {
//        int exitCode = 0;
////        System.out.println("print_args__");
////        System.out.println(args[4]);
//        String origLogDir = args[1];
//        if(args[4].contains("_/p")) {
//            String[] text1 = args[4].split("_/p");
//            for (int i = 0; i < text1.length; i++) {
//                if (i > 0) {
//                    args[4] = "/p" + text1[i];
//                } else {
//                    args[4] = text1[i];
//                }
//                String idNumber = text1[i].split("id_")[1];
////                System.out.println("id: " + idNumber);
////                System.out.println("LogDir: " + args[1]);
//                args[1] = origLogDir + "/id_" + idNumber;
//                System.out.println("NewLogDir: " + args[1]);
//                System.out.println("print_args2__");
//                System.out.println(Arrays.toString(args));
//                if (args[1].contains("id_000000001")) {
//                    System.setProperty("kr.ac.unist.cse.jqf.USE_SEED",Boolean.toString(true));
//                    System.out.println("UseSeed in Repro");
//                }
//                else {
//                    System.setProperty("kr.ac.unist.cse.jqf.USE_SEED",Boolean.toString(false));
//                }
//
//                exitCode = new CommandLine(new ReproDriver()).execute(args);
//            }
//        }
//        else {
//            exitCode = new CommandLine(new ReproDriver()).execute(args);
//        }
//        System.exit(exitCode);
//    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ReproDriver()).execute(args);
        System.exit(exitCode);
    }
}
