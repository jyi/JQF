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
package edu.berkeley.cs.jqf.fuzz.junit.quickcheck;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.java.lang.DoubleGenerator;
import com.pholser.junit.quickcheck.generator.java.lang.IntegerGenerator;
import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import com.pholser.junit.quickcheck.internal.generator.CompositeGenerator;
import com.pholser.junit.quickcheck.internal.generator.GeneratorRepository;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.PatchInfo;
import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI2;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.*;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.junit.ReproRun;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import edu.berkeley.cs.jqf.fuzz.reach.PoracleGuidance;
import edu.berkeley.cs.jqf.fuzz.reach.Target;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import edu.berkeley.cs.jqf.instrument.tracing.ThreadTracer;
import kr.ac.unist.cse.jqf.Log;
import kr.ac.unist.cse.jqf.aspect.DumpUtil;
import kr.ac.unist.cse.jqf.fuzz.generator.InRangeFactory;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import ru.vyarus.java.generics.resolver.GenericsResolver;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.berkeley.cs.jqf.fuzz.guidance.Result.*;
//import org.apache.commons.io.FileUtils;

/**
 *
 * A JUnit {@link Statement} that will be run using guided fuzz
 * testing.
 *
 * @author Rohan Padhye
 */
public class FuzzStatement extends Statement {
    private ClassLoader loaderForPatch;
    private ArrayList<ClassLoader> loaderForPatchList = new ArrayList<>();
    private ArrayList<PatchInfo> patchInfos = new ArrayList<>();
    private FrameworkMethod method;
    private final TestClass testClass;
    private final Map<String, Type> typeVariables;
    private final GeneratorRepository generatorRepository;
    private final List<Class<?>> expectedExceptions;
    private final List<Throwable> failures = new ArrayList<>();
    private final InRangeFactory inRangeFactory = InRangeFactory.singleton();
    private static int wideningCount;
    private static boolean useRepro = false;
    private static boolean verbose = true;
    private static boolean first = true;
    private static boolean firstPatch = true;

    public FuzzStatement(FrameworkMethod method, TestClass testClass,
                         GeneratorRepository generatorRepository) {
        this.method = method;
        this.testClass = testClass;
        this.typeVariables =
                GenericsResolver.resolve(testClass.getJavaClass())
                        .method(method.getMethod())
                        .genericsMap();
        this.generatorRepository = generatorRepository;
        this.expectedExceptions = Arrays.asList(method.getMethod().getExceptionTypes());
    }


    public FuzzStatement(FrameworkMethod method, TestClass testClass,
                         GeneratorRepository generatorRepository, ClassLoader loaderForPatch) {
        this(method, testClass, generatorRepository);
        this.loaderForPatch = loaderForPatch;
    }

    public FuzzStatement(FrameworkMethod method, TestClass testClass,
                         GeneratorRepository generatorRepository, ArrayList<ClassLoader> loaderForPatch, boolean onlyLoader) {
        this(method, testClass, generatorRepository);
        this.loaderForPatchList = loaderForPatch;
        this.loaderForPatch = loaderForPatch.get(0);
    }

    public FuzzStatement(FrameworkMethod method, TestClass testClass,
                         GeneratorRepository generatorRepository, ArrayList<PatchInfo> patchInfos) {
        this(method, testClass, generatorRepository);
        this.loaderForPatch = patchInfos.get(0).patchLoader;
        this.patchInfos = patchInfos;
    }



    /**
     * Run the test.
     *
     * @throws Throwable if the test fails
     */
    @Override
    public void evaluate() throws Throwable {
        if (this.loaderForPatch != null && Boolean.getBoolean("jqf.ei.run_two_versions")) {
            System.out.println("RunTwoVersion: " + Boolean.toString(Boolean.getBoolean("jqf.ei.run_two_versions")));
            evaluateTwoVersions();
            return;
        }
        System.out.println("RunTwoVersion: " + Boolean.toString(Boolean.getBoolean("jqf.ei.run_two_versions")));
        // Construct generators for each parameter
        List<Generator<?>> generators = Arrays.stream(method.getMethod().getParameters())
                .map(this::createParameterTypeContext)
                .map(this::produceGenerator)
                .collect(Collectors.toList());

        // Get the currently registered fuzz guidance
        Guidance guidance = GuidedFuzzing.getCurrentGuidance();

        // If nothing is set, default to random or repro
        if (guidance == null) {
            // Check for @Fuzz(repro=)
            String repro = method.getAnnotation(Fuzz.class).repro();
            if (repro.isEmpty()) {
                guidance = new NoGuidance(GuidedFuzzing.DEFAULT_MAX_TRIALS, System.err);
            } else {
                File inputFile = new File(repro);
                if (inputFile.isDirectory()) {
                    File[] files = inputFile.listFiles();
                    Arrays.sort(files);
                    guidance = new ReproGuidance(files, null, null);
                } else {
                    guidance = new ReproGuidance(inputFile, null, null);
                }
            }
        }

        // Keep fuzzing until no more input or I/O error with guidance
        try {
            // Keep fuzzing as long as guidance wants to
            while (guidance.hasInput()) {
                Log.logOutIfCalled = false;
                Result result = INVALID;
                Throwable error = null;

                // Initialize guided fuzzing using a file-backed random number source
                try {
                    Object[] args;
                    ArrayList<Object> argsList = new ArrayList<>();
                    try {

                        // Generate input values
                        StreamBackedRandom randomFile = new StreamBackedRandom(guidance.getInput(), Long.BYTES);
                        SourceOfRandomness random = new FastSourceOfRandomness(randomFile);
                        GenerationStatus genStatus = new NonTrackingGenerationStatus(random);
                        args = generators.stream()
                                .map(g -> g.generate(random, genStatus))
                                .toArray();


                        if (first && Boolean.getBoolean("kr.ac.unist.cse.jqf.USE_SEED")) {
                            System.out.println("useSeed: " + Boolean.toString(Boolean.getBoolean("kr.ac.unist.cse.jqf.USE_SEED")));
                            for (int i = 0; i < method.getMethod().getParameterCount(); i++) {
                                Generator<?> gen = generators.get(i);
                                if (!(gen instanceof CompositeGenerator)) {
                                    throw new RuntimeException("Unsupported generator type: " + gen.getClass());
                                }
                                CompositeGenerator comGen = (CompositeGenerator) gen;
                                for (int j = 0; j < comGen.numberOfComposedGenerators(); j++) {
                                    Generator<?> gen2 = ((CompositeGenerator) comGen).composed(j);
                                    Annotation[] anns = method.getMethod().getParameterAnnotations()[i];
                                    for (Annotation ann: anns) {
                                        if (ann instanceof InRange) {
                                            if (gen2 instanceof DoubleGenerator) {
                                                argsList.add(((InRange) ann).seedDouble());
                                            }
                                            else if (gen2 instanceof IntegerGenerator) {
//                                                System.out.println("SeedInt");
                                                argsList.add(((InRange) ann).seedInt());
                                            }
//                                        updateRange(guidance, gen2, (InRange) ann);
                                        }
                                    }
                                }
                            }
//                            for (Object arg : argsList) {
//                                System.out.println("ArgList: " + Double.toString((Double)arg));
//
//                            }
                            if (argsList.size() > 0) {
                                args = argsList.toArray(new Object[argsList.size()]);
                            }
//                            for (Object arg : args) {
//                                System.out.println("ArgArray: " + Double.toString((Double)arg));
//                            }
                            first = false;
                        }



                        Log.logIn(args);
//                        for (Object arg : args) {
//                            System.out.println("ArgArray: " + Double.toString((Double)arg));
//                        }
                        // Let guidance observe the generated input args
                        guidance.observeGeneratedArgs(args);
                    } catch (IllegalStateException e) {
                        if (e.getCause() instanceof EOFException) {
                            // This happens when we reach EOF before reading all the random values.
                            // Treat this as an assumption failure, so that the guidance considers the
                            // generated input as INVALID
                            System.err.println(e.toString());
                            throw new AssumptionViolatedException("StreamBackedRandom does not have enough data", e.getCause());
                        } else {
                            throw e;
                        }
                    } catch (AssumptionViolatedException | TimeoutException e) {
                        // Propagate early termination of tests from generator
                        throw e;
                    } catch (GuidanceException e) {
                        // Throw the guidance exception outside to stop fuzzing
                        throw e;
                    } catch (Throwable e) {
                        // Throw the guidance exception outside to stop fuzzing
                        throw new GuidanceException(e);
                    } finally {
                        // System.out.println(randomFile.getTotalBytesRead() + " random bytes read");
                    }

                    // Attempt to run the trial
                    new TrialRunner(testClass.getJavaClass(), method, args).run();

                    // If we reached here, then the trial must be a success
                    result = SUCCESS;
                } catch (GuidanceException e) {
                    // Throw the guidance exception outside to stop fuzzing
                    throw e;
                } catch (AssumptionViolatedException e) {
                    result = INVALID;
                    error = e;
                } catch (TimeoutException e) {
                    result = TIMEOUT;
                    error = e;
                } catch (Throwable e) {

                    // Check if this exception was expected
                    if (isExceptionExpected(e.getClass())) {
                        result = SUCCESS; // Swallow the error
                    } else {
                        result = FAILURE;
                        error = e;
                        failures.add(e);
                    }
                }
                System.out.println("Result: " + result.toString());

                // Inform guidance about the outcome of this trial
                guidance.handleResult(result, error);
                if (System.getProperty("kr.ac.unist.cse.jqf.NO_FUZZ").equals("true")) {
                    break;
                }
            }
        } catch (GuidanceException e) {
            System.err.println("Fuzzing stopped due to guidance exception: " + e.getMessage());
            e.printStackTrace();
        }

        if (failures.size() > 0) {
            if (failures.size() == 1) {
                throw failures.get(0);
            } else {
                // Not sure if we should report each failing run,
                // as there may be duplicates
                throw new MultipleFailureException(failures);
            }
        }
    }

    private void evaluateTwoVersions() throws Throwable {
        // Construct generators for each parameter
        List<Generator<?>> generators = Arrays.stream(method.getMethod().getParameters())
                .map(this::createParameterTypeContext)
                .map(this::produceGenerator)
                .collect(Collectors.toList());

        // Get the currently registered fuzz guidance
        if (Boolean.getBoolean("jqf.ei.run_patch")) {
            Log.turnOffRunBuggyVersion();
        } else {
            Log.turnOnRunBuggyVersion();
        }

        // NOTE: When the original version is called the second time,
        // we use evaluatePatch to use the repro guidance.
        if (useRepro) {
            evaluatePatch((ReproGuidance) ReproRun.getCurrentGuidance(),
                    generators);
        } else {
            evaluateOrg((PoracleGuidance) GuidedFuzzing.getCurrentGuidance(),
                    generators);
        }
    }


    private void evaluateOrg(PoracleGuidance guidance, List<Generator<?>> generators) throws Throwable {
        // Keep fuzzing until no more input or I/O error with guidance
        try {
            // Keep fuzzing as long as guidance wants to
            while (guidance.hasInput()) {
                Log.reset();
                Result result = INVALID;
                Throwable error = null;

                if (!guidance.isRangeFixed() && guidance.isWideningPlateauReached()) {
                    guidance.infoLog("Plateau is reached, and the range is widened");
                    // update input range
                    wideningCount++;
                    updateInputRange(guidance, generators, wideningCount);
                }

                // Initialize guided fuzzing using a file-backed random number source
                Object[] args = null;
                ArrayList<Object> argsList = new ArrayList<>();
                try {
                    try {
                        // Generate input values
                        StreamBackedRandom randomFile = new StreamBackedRandom(guidance.getInput(), Long.BYTES);
                        SourceOfRandomness random = new FastSourceOfRandomness(randomFile);
                        GenerationStatus genStatus = new NonTrackingGenerationStatus(random);

                        args = generators.stream()
                                .map(g -> g.generate(random, genStatus))
                                .toArray();

                        Log.resetLogDirForInput();

                        System.out.println("First: " + Boolean.toString(first));
                        if (first && Boolean.getBoolean("kr.ac.unist.cse.jqf.USE_SEED")) {
                            System.out.println("useSeed: " + Boolean.toString(Boolean.getBoolean("kr.ac.unist.cse.jqf.USE_SEED")));
                            for (int i = 0; i < method.getMethod().getParameterCount(); i++) {
                                Generator<?> gen = generators.get(i);
                                if (!(gen instanceof CompositeGenerator)) {
                                    throw new RuntimeException("Unsupported generator type: " + gen.getClass());
                                }
                                CompositeGenerator comGen = (CompositeGenerator) gen;
                                for (int j = 0; j < comGen.numberOfComposedGenerators(); j++) {
                                    Generator<?> gen2 = ((CompositeGenerator) comGen).composed(j);
                                    Annotation[] anns = method.getMethod().getParameterAnnotations()[i];
                                    for (Annotation ann: anns) {
                                        if (ann instanceof InRange) {
                                            if (gen2 instanceof DoubleGenerator) {
                                                argsList.add(((InRange) ann).seedDouble());
                                            }
                                            else if (gen2 instanceof IntegerGenerator) {
//                                                System.out.println("SeedInt");
                                                argsList.add(((InRange) ann).seedInt());
                                            }
//                                        updateRange(guidance, gen2, (InRange) ann);
                                        }
                                    }
                                }
                            }
//                            for (Object arg : argsList) {
//                                System.out.println("ArgList: " + Double.toString((Double)arg));
//
//                            }
                            if (argsList.size() > 0) {
                                args = argsList.toArray(new Object[argsList.size()]);
                            }

//                            for (Object arg : args) {
//                                System.out.println("ArgArray: " + Double.toString((Double)arg));
//                            }
                            first = false;
                        }

                        Log.logIn(args);
                        for (Object o : args) {
//                            System.out.println("Arg: " + String.valueOf(o));
                        }
                        DumpUtil.reset();

                        // Let guidance observe the generated input args
                        guidance.observeGeneratedArgs(args);
                    } catch (IllegalStateException e) {
                        if (e.getCause() instanceof EOFException) {
                            // This happens when we reach EOF before reading all the random values.
                            // Treat this as an assumption failure, so that the guidance considers the
                            // generated input as INVALID
                            throw new AssumptionViolatedException("StreamBackedRandom does not have enough data", e.getCause());
                        } else {
                            throw e;
                        }
                    } catch (AssumptionViolatedException | TimeoutException e) {
                        // Propagate early termination of tests from generator
                        throw e;
                    } catch (GuidanceException e) {
                        // Throw the guidance exception outside to stop fuzzing
                        throw e;
                    } catch (Throwable e) {
                        // Throw the guidance exception outside to stop fuzzing
                        if (!System.getProperty("kr.ac.unist.cse.jqf.NO_FUZZ").equals("true")) {
                            throw new GuidanceException(e);
                        }
                        throw new GuidanceException(e);

                    } finally {
                        // System.out.println(randomFile.getTotalBytesRead() + " random bytes read");
                    }

                    // Attempt to run the trial
                    new TrialRunner(testClass.getJavaClass(), method, args).run();

                    // If we reached here, then the trial must be a success
                    result = SUCCESS;
                } catch (GuidanceException e) {
                    // Throw the guidance exception outside to stop fuzzing
                    throw e;
                } catch (AssumptionViolatedException e) {
                    result = INVALID;
                    error = e;
                }
                catch (TimeoutException e) {
                    result = TIMEOUT;
                    error = e;
                } catch (NoSuchMethodError e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (Throwable e) {

                    // Check if this exception was expected
                    if (isExceptionExpected(e.getClass())) {
                        result = SUCCESS; // Swallow the error
                    } else {
//                        continue;
                        result = FAILURE;
                        error = e;
                        failures.add(e);
                    }
                }
                System.out.println("Result: " + result.toString());

                PoracleGuidance.ResultOfOrg resultOfOrg = guidance.handleResultOfOrg(result, error);
                PoracleGuidance.ResultOfPatch resultOfPatch = null;
                ArrayList<PoracleGuidance.ResultOfPatch> resultOfPatchesList = new ArrayList<>();
                if (resultOfOrg.isInputNotIgnored()) {
                    guidance.infoLog("Found an non-ignoring input");
                    guidance.fixRange();
                    // run patched version
                    assert guidance.getCurSaveFileName() != null;

                    // we call the patched version
                    if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
                        guidance.setNumOfPatches(patchInfos.size());
                        System.out.println("PatchesLength:" + Integer.toString(patchInfos.size()));
                        for (PatchInfo pi : patchInfos) {
                            System.setProperty("jqf.ei.CURRENT_PATH_FOR_PATCH", pi.patchPath);
                            System.out.println("PatchInfo: " + pi.patchPath);
                            runPatch(guidance, resultOfOrg, pi.getPatchLoader(), argsList.size());
                            if (!guidance.isDiffOutFound()) {
                                guidance.setDiffOutputFound(isDiffOutputFound());
                            }
                            if (isDiffOutputFound()) {
                                pi.increaseDiffFound();
                                guidance.incDiffOutputFound();
                            }
                            boolean targetHit = false;
                            if (DumpUtil.isTheTargetHit() && !isDiffOutputFound()) {
                                targetHit = true;
                                // we call the original version again
                                // we should retrieve the class loader for the buggy version
                                DumpUtil.runOrgVerAgain = true;
                                ReproGuidance reproGuidance2 = new ReproGuidance(resultOfOrg.getInputFile(), null,
                                        guidance.getOutputDirectory());
                                guidance.resetRunCoverageOfOrg();
                                run(testClass.getName(), method.getName(), ZestCLI2.loaderForOrg, reproGuidance2);
                                DumpUtil.runOrgVerAgain = false;
                                DumpUtil.setTargetHit(false);
                                DumpUtil.exitMethods.clear();
                                DumpUtil.enterMethods.clear();
                            }
                            Log.turnOffRunBuggyVersion();
                            resultOfPatchesList.add(guidance.handleResultOfPatch(result, targetHit));
                        }
                    }
                    else {
                        runPatch(guidance, resultOfOrg);
                        guidance.setDiffOutputFound(isDiffOutputFound());

                        boolean targetHit = false;
                        if (DumpUtil.isTheTargetHit() && !isDiffOutputFound()) {
                            targetHit = true;
                            // we call the original version again
                            // we should retrieve the class loader for the buggy version
                            DumpUtil.runOrgVerAgain = true;
                            ReproGuidance reproGuidance2 = new ReproGuidance(resultOfOrg.getInputFile(), null,
                                    guidance.getOutputDirectory());
                            guidance.resetRunCoverageOfOrg();
                            run(testClass.getName(), method.getName(), ZestCLI2.loaderForOrg, reproGuidance2);
                            DumpUtil.runOrgVerAgain = false;
                            DumpUtil.setTargetHit(false);
                            DumpUtil.exitMethods.clear();
                            DumpUtil.enterMethods.clear();
                        }
                        Log.turnOffRunBuggyVersion();
                        resultOfPatch = guidance.handleResultOfPatch(result, targetHit);
                    }
                    Log.turnOnRunBuggyVersion();
                } else {
                    guidance.infoLog("Ignore input");
                }

                boolean shouldSave = false;
                if (Boolean.getBoolean(System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ"))) {
                    shouldSave = guidance.shouldSaveInput(resultOfOrg, resultOfPatchesList);
                    if (shouldSave) {
                        guidance.resetProgressCount();

                        String why = "";
                        PoracleGuidance.Distance dist = null;
                        boolean valid = false;
                        if (resultOfPatchesList.get(resultOfPatchesList.size()-1) != null) {
                            why = resultOfPatchesList.get(resultOfPatchesList.size()-1).getWhy() + "+dist";
                            dist = resultOfPatchesList.get(resultOfPatchesList.size()-1).getDistance();
                            valid = resultOfPatchesList.get(resultOfPatchesList.size()-1).isValid();
                        } else {
                            why = resultOfOrg.getWhy() + "+dist";
                            dist = resultOfOrg.getDistance();
                            valid = resultOfOrg.isValid();
                        }
                        infoLog("new distances: " + guidance.distsToString(dist));
                        guidance.saveInputs(why, valid, dist);
                    } else {
                        guidance.noProgress();
                        infoLog("skip duplicate coverage");
                    }

                }
                else {
                    shouldSave = guidance.shouldSaveInput(resultOfOrg, resultOfPatch);
                    if (shouldSave) {
                        guidance.resetProgressCount();

                        String why = "";
                        PoracleGuidance.Distance dist = null;
                        boolean valid = false;
                        if (resultOfPatch != null) {
                            why = resultOfPatch.getWhy() + "+dist";
                            dist = resultOfPatch.getDistance();
                            valid = resultOfPatch.isValid();
                        } else {
                            why = resultOfOrg.getWhy() + "+dist";
                            dist = resultOfOrg.getDistance();
                            valid = resultOfOrg.isValid();
                        }
                        infoLog("new distances: " + guidance.distsToString(dist));
                        guidance.saveInputs(why, valid, dist);
                    } else {
                        guidance.noProgress();
                        infoLog("skip duplicate coverage");
                    }

                }


                guidance.checkProgress();
                if (System.getProperty("kr.ac.unist.cse.jqf.NO_FUZZ").equals("true")) {
                    break;
                }
            }
        } catch (GuidanceException e) {
            System.err.println("Fuzzing stopped due to guidance exception: " + e.getMessage());
            e.printStackTrace();
        }

        if (guidance.isDiffOutFound()) {
            guidance.saveInputs("diff_out_found", true);
        }

        if (failures.size() > 0) {
            if (failures.size() == 1) {
                throw failures.get(0);
            } else {
                // Not sure if we should report each failing run,
                // as there may be duplicates
                throw new MultipleFailureException(failures);
            }
        }
    }

    private void runPatch(PoracleGuidance guidance, PoracleGuidance.ResultOfOrg resultOfOrg) throws ClassNotFoundException {
        ReproGuidance reproGuidance = new ReproGuidance(resultOfOrg.getInputFile(), null,
                guidance.getOutputDirectory());
        System.setProperty("jqf.ei.run_patch", "true");
        if (loaderForPatch instanceof InstrumentingClassLoader) {
            // make sure that target classes are instrumented
            Target[] targets = Target.getTargetArray(System.getProperty("jqf.ei.targets"));
            for (Target target: targets) {
                ((InstrumentingClassLoader) loaderForPatch).instrumentClass(target.getClassName());
            }
        }
        ThreadTracer.evaluatingPatch = true;
        run(testClass.getName(), method.getName(), this.loaderForPatch, reproGuidance);
        System.setProperty("jqf.ei.run_patch", "false");

        guidance.isWideningPlateauReached = false;
        Log.turnOnRunBuggyVersion();
    }

    private void runPatch(PoracleGuidance guidance, PoracleGuidance.ResultOfOrg resultOfOrg, ClassLoader loaderPatch, int argLen) throws ClassNotFoundException {
        ReproGuidance reproGuidance;
        if (argLen > 0) {
            reproGuidance = new ReproGuidance(resultOfOrg.getInputFile(), null,
                    guidance.getOutputDirectory(), true);
        }
        else {
            reproGuidance = new ReproGuidance(resultOfOrg.getInputFile(), null,
                    guidance.getOutputDirectory());
        }

        System.setProperty("jqf.ei.run_patch", "true");
        if (patchInfos.size() != 0) {
            if (loaderPatch instanceof InstrumentingClassLoader) {
                // make sure that target classes are instrumented
                Target[] targets = Target.getTargetArray(System.getProperty("jqf.ei.targets"));
                for (Target target: targets) {
                    ((InstrumentingClassLoader) loaderPatch).instrumentClass(target.getClassName());
                }
            }
            run(testClass.getName(), method.getName(), loaderPatch, reproGuidance);
            System.setProperty("jqf.ei.run_patch", "false");

            guidance.isWideningPlateauReached = false;
            Log.turnOnRunBuggyVersion();
        }
    }

    private void run(String className, String methodName, ClassLoader loader, ReproGuidance reproGuidance) throws ClassNotFoundException {
        run(className, methodName, loader, reproGuidance, null);
    }

    private void run(String className, String methodName, ClassLoader loader, ReproGuidance reproGuidance, PrintStream out) throws ClassNotFoundException {
        useRepro = true;
        ReproRun.run(className, methodName, loader, reproGuidance, out);
        useRepro = false;
    }

    // return true when outputs are equal to each other
    private boolean isDiffOutputFound() {

        return Log.LogResult.isDiffOutputFound();
    }

    private void evaluatePatch(ReproGuidance guidance, List<Generator<?>> generators) throws Throwable {
        // Keep fuzzing until no more input or I/O error with guidance
        Log.turnOffRunBuggyVersion();
        System.setProperty("jqf.ei.run_patch", "true");
        try {
            // Keep fuzzing as long as guidance wants to
            while (guidance.hasInput()) {
                Log.reset();
                Result result = INVALID;
                Throwable error = null;

                // update input range based on the current wideningCount
                updateInputRange(guidance, generators, wideningCount);

                // Initialize guided fuzzing using a file-backed random number source
                try {
                    Object[] args;
                    ArrayList<Object> argsList = new ArrayList<>();
                    try {

                        // Generate input values
                        StreamBackedRandom randomFile = new StreamBackedRandom(guidance.getInput(), Long.BYTES);
                        SourceOfRandomness random = new FastSourceOfRandomness(randomFile);
                        GenerationStatus genStatus = new NonTrackingGenerationStatus(random);
                        args = generators.stream()
                                .map(g -> g.generate(random, genStatus))
                                .toArray();

                        System.out.println("FirstPatch: " + Boolean.toString(firstPatch));
                        if (firstPatch && Boolean.getBoolean("kr.ac.unist.cse.jqf.USE_SEED")) {
                            System.out.println("useSeed: " + Boolean.toString(Boolean.getBoolean("kr.ac.unist.cse.jqf.USE_SEED")));
                            for (int i = 0; i < method.getMethod().getParameterCount(); i++) {
                                Generator<?> gen = generators.get(i);
                                if (!(gen instanceof CompositeGenerator)) {
                                    throw new RuntimeException("Unsupported generator type: " + gen.getClass());
                                }
                                CompositeGenerator comGen = (CompositeGenerator) gen;
                                for (int j = 0; j < comGen.numberOfComposedGenerators(); j++) {
                                    Generator<?> gen2 = ((CompositeGenerator) comGen).composed(j);
                                    Annotation[] anns = method.getMethod().getParameterAnnotations()[i];
                                    for (Annotation ann: anns) {
                                        if (ann instanceof InRange) {
                                            if (gen2 instanceof DoubleGenerator) {
                                                argsList.add(((InRange) ann).seedDouble());
                                            }
                                            else if (gen2 instanceof IntegerGenerator) {
//                                                System.out.println("SeedInt");
                                                argsList.add(((InRange) ann).seedInt());
                                            }
//                                        updateRange(guidance, gen2, (InRange) ann);
                                        }
                                    }
                                }
                            }
//                            for (Object arg : argsList) {
//                                System.out.println("ArgList: " + Double.toString((Double)arg));
//
//                            }
                            if (argsList.size() > 0) {
                                args = argsList.toArray(new Object[argsList.size()]);
                            }
//                            for (Object arg : args) {
//                                System.out.println("ArgArray: " + Double.toString((Double)arg));
//                            }
                            firstPatch = false;
                        }

                        Log.logIn(args);
                        DumpUtil.reset();

                        // Let guidance observe the generated input args
                        guidance.observeGeneratedArgs(args);
                    } catch (IllegalStateException e) {
                        if (e.getCause() instanceof EOFException) {
                            // This happens when we reach EOF before reading all the random values.
                            // Treat this as an assumption failure, so that the guidance considers the
                            // generated input as INVALID
                            throw new AssumptionViolatedException("StreamBackedRandom does not have enough data", e.getCause());
                        } else {
                            throw e;
                        }
                    } catch (AssumptionViolatedException | TimeoutException e) {
                        // Propagate early termination of tests from generator
                        throw e;
                    } catch (GuidanceException e) {
                        // Throw the guidance exception outside to stop fuzzing
                        throw e;
                    } catch (Throwable e) {
                        // Throw the guidance exception outside to stop fuzzing
                        throw new GuidanceException(e);
                    } finally {
                        // System.out.println(randomFile.getTotalBytesRead() + " random bytes read");
                    }

                    // Attempt to run the trial
                    ThreadTracer.evaluatingPatch = true;
                    new TrialRunner(testClass.getJavaClass(), method, args).run();

                    // If we reached here, then the trial must be a success
                    result = SUCCESS;
                } catch (GuidanceException e) {
                    // Throw the guidance exception outside to stop fuzzing
                    throw e;
                } catch (AssumptionViolatedException e) {
                    result = INVALID;
                    error = e;
                } catch (TimeoutException e) {
                    result = TIMEOUT;
                    error = e;
                } catch (Throwable e) {

                    // Check if this exception was expected
                    if (isExceptionExpected(e.getClass())) {
                        result = SUCCESS; // Swallow the error
                    } else {
                        result = FAILURE;
                        error = e;
                        failures.add(e);
                    }
                }

                // Inform guidance about the outcome of this trial
                guidance.handleResult(result, error);
                if (System.getProperty("kr.ac.unist.cse.jqf.NO_FUZZ").equals("true")) {
                    break;
                }
            }
        } catch (GuidanceException e) {
            System.err.println("Fuzzing stopped due to guidance exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ThreadTracer.evaluatingPatch = false;
        }

        if (failures.size() > 0) {
            if (failures.size() == 1) {
                throw failures.get(0);
            } else {
                // Not sure if we should report each failing run,
                // as there may be duplicates
                throw new MultipleFailureException(failures);
            }
        }
        System.setProperty("jqf.ei.run_patch", "false");
        Log.turnOnRunBuggyVersion();
    }

    private void updateInputRange(Guidance guidance, List<Generator<?>> generators, int wideningCount) {
        for (int i = 0; i < method.getMethod().getParameterCount(); i++) {
            Generator<?> gen = generators.get(i);
            if (!(gen instanceof CompositeGenerator)) {
                throw new RuntimeException("Unsupported generator type: " + gen.getClass());
            }
            CompositeGenerator comGen = (CompositeGenerator) gen;
            for (int j = 0; j < comGen.numberOfComposedGenerators(); j++) {
                Generator<?> gen2 = ((CompositeGenerator) comGen).composed(j);
                Annotation[] anns = method.getMethod().getParameterAnnotations()[i];
                for (Annotation ann: anns) {
                    if (ann instanceof InRange) {
                        updateRange(guidance, gen2, (InRange) ann);
                    }
                }

                // update the range of custom generators
                try {
                    inRangeFactory.generate(gen2, wideningCount);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateRange(Guidance guidance, Generator<?> gen, InRange range) {
        try {
            InRange newRange = range;
            if(!range.isFixed()){
                newRange = inRangeFactory.generate(gen, range, wideningCount);
            }
            Method m = gen.getClass().getMethod("configure", InRange.class);
            m.invoke(gen, newRange);
//            if (gen instanceof IntegerGenerator) {
//                infoLog("[" + newRange.minInt() + ", " + newRange.maxInt() + "]");
//            } else if (gen instanceof DoubleGenerator) {
//                infoLog("[" + newRange.minDouble() + ", " + newRange.maxDouble() + "]");
//            }
        } catch (NoSuchMethodException e) {
            infoLog(String.format("Class %s does not have configure(InRange)", gen.getClass()));
        } catch (IllegalAccessException e) {
            infoLog(String.format("Class %s does not have public configure(InRange)", gen.getClass()));
        } catch (InvocationTargetException e) {
            infoLog(String.format("An exception is thrown from %s.configure(InRange)", gen.getClass()));
        }
    }

    /** Writes a line of text to the log file. */
    private void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            if (ZestGuidance.logFile != null) {
                appendLineToFile(ZestGuidance.logFile, line);
            } else {
                System.err.println(line);
            }
        }
    }

    /** Writes a line of text to a given log file. */
    private void appendLineToFile(File file, String line) throws GuidanceException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println(line);
        } catch (IOException e) {
            throw new GuidanceException(e);
        }
    }

    /**
     * Returns whether an exception is expected to be thrown by a trial method
     *
     * @param e the class of an exception that is thrown
     * @return <tt>true</tt> if e is a subclass of any exception specified
     * in the <tt>throws</tt> clause of the trial method.
     */
    private boolean isExceptionExpected(Class<? extends Throwable> e) {
        for (Class<?> expectedException : expectedExceptions) {
            if (expectedException.isAssignableFrom(e)) {
                return true;
            }
        }
        return false;
    }

    private ParameterTypeContext createParameterTypeContext(Parameter parameter) {
        Executable exec = parameter.getDeclaringExecutable();
        String declarerName = exec.getDeclaringClass().getName() + '.' + exec.getName();
        return new ParameterTypeContext(
                        parameter.getName(),
                        parameter.getAnnotatedType(),
                        declarerName,
                        typeVariables)
                        .allowMixedTypes(true).annotate(parameter);
    }

    private Generator<?> produceGenerator(ParameterTypeContext parameter) {
        Generator<?> generator = generatorRepository.generatorFor(parameter);
        generator.provide(generatorRepository);
        generator.configure(parameter.annotatedType());
        return generator;
    }
}
