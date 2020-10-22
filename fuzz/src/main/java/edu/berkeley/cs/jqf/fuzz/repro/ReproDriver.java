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

import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
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
    @Parameters(index = "2..*") File[] testInputFiles;

    @Option(names = "--logdir", description = "log directory")
    public String logDir = null;

    @Option(names = "--cp", description = "classpath")
    public String classPath = null;

    @Option(names = "--run-buggy-version", description =  "do you run a buggy version?")
    public boolean runBuggyVersion = false;

    @Override
    public void run() {
        try {
            if (logDir != null)
                System.setProperty("jqf.ei.logDir", logDir);

            if (runBuggyVersion) {
                Log.runBuggyVersion = true;
                kr.ac.unist.cse.jqf.Log.runBuggyVersion = true;
            }

            // Maybe log the trace
            String traceDirName = System.getProperty("jqf.repro.traceDir");
            File traceDir = traceDirName != null ? new File(traceDirName) : null;

            // Load the guidance
            ReproGuidance guidance = new ReproGuidance(testInputFiles, traceDir, null);

            ClassLoader classLoader = new InstrumentingClassLoader(
                    this.classPath.split(File.pathSeparator),
                    ReproDriver.class.getClassLoader());

            // Run the Junit test
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
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ReproDriver()).execute(args);
        System.exit(exitCode);
    }
}
