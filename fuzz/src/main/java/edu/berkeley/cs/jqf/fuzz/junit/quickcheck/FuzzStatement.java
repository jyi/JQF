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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.java.lang.IntegerGenerator;
import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import com.pholser.junit.quickcheck.internal.generator.CompositeGenerator;
import com.pholser.junit.quickcheck.internal.generator.GeneratorRepository;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzingForPatched;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import edu.berkeley.cs.jqf.fuzz.reach.ReachGuidance;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.StreamBackedRandom;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import kr.ac.unist.cse.jqf.Log;
import kr.ac.unist.cse.jqf.fuzz.generator.InRangeFactory;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import ru.vyarus.java.generics.resolver.GenericsResolver;

import static edu.berkeley.cs.jqf.fuzz.guidance.Result.*;

/**
 *
 * A JUnit {@link Statement} that will be run using guided fuzz
 * testing.
 *
 * @author Rohan Padhye
 */
public class FuzzStatement extends Statement {
    private ClassLoader loaderForPatch;
    private FrameworkMethod method;
    private final TestClass testClass;
    private final Map<String, Type> typeVariables;
    private final GeneratorRepository generatorRepository;
    private final List<Class<?>> expectedExceptions;
    private final List<Throwable> failures = new ArrayList<>();
    private final InRangeFactory inRangeFactory = InRangeFactory.singleton();
    private static int wideningCount;

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

    /**
     * Run the test.
     *
     * @throws Throwable if the test fails
     */
    @Override
    public void evaluate() throws Throwable {
        init();
        if (this.loaderForPatch != null) {
            evaluateTwoVersions();
        }

        // Construct generators for each parameter
        List<Generator<?>> generators = Arrays.stream(method.getMethod().getParameters())
                .map(this::createParameterTypeContext)
                .map(this::produceGenerator)
                .collect(Collectors.toList());

        // update input range
        updateInputRange(generators);

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
                    guidance = new ReproGuidance(files, null);
                } else {
                    guidance = new ReproGuidance(inputFile, null);
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
                    try {

                        // Generate input values
                        StreamBackedRandom randomFile = new StreamBackedRandom(guidance.getInput(), Long.BYTES);
                        SourceOfRandomness random = new FastSourceOfRandomness(randomFile);
                        GenerationStatus genStatus = new NonTrackingGenerationStatus(random);
                        args = generators.stream()
                                .map(g -> g.generate(random, genStatus))
                                .toArray();

                        Log.logIn(args);

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
            evaluatePatch((ReproGuidance) GuidedFuzzingForPatched.getCurrentGuidance(),
                    generators);
        } else {
            Log.turnOnRunBuggyVersion();
            evaluateOrg((ReachGuidance) GuidedFuzzing.getCurrentGuidance(),
                    generators);
        }
    }

    private void evaluateOrg(ReachGuidance guidance, List<Generator<?>> generators) throws Throwable {
        // Keep fuzzing until no more input or I/O error with guidance
        try {
            // update input range
            updateInputRange(generators);

            // Keep fuzzing as long as guidance wants to
            while (guidance.hasInput()) {
                Log.logOutIfCalled = false;
                Result result = INVALID;
                Throwable error = null;

                if (guidance.isPlateauReached()) {
                    System.out.println("Plateau is reached, and the range is widened");
                    updateInputRange(generators);
                    wideningCount++;
                }

                // Initialize guided fuzzing using a file-backed random number source
                try {
                    Object[] args;
                    try {

                        // Generate input values
                        StreamBackedRandom randomFile = new StreamBackedRandom(guidance.getInput(), Long.BYTES);
                        SourceOfRandomness random = new FastSourceOfRandomness(randomFile);
                        GenerationStatus genStatus = new NonTrackingGenerationStatus(random);
                        args = generators.stream()
                                .map(g -> g.generate(random, genStatus))
                                .toArray();

                        Log.logIn(args);

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
                ReachGuidance.HandleResult info = guidance.handleResult2(result, error);

                if (info.isInputAdded()) {
                    System.out.println("Succeeded to log out actual");
                    // run patched version
                    assert guidance.getSavedAllDirectory() != null;
                    assert guidance.getCurSaveFileName() != null;
                    File saveFile = info.getInput().getSaveFile();
                    ReproGuidance reproGuidance = new ReproGuidance(saveFile, null);

                    System.setProperty("jqf.ei.run_patch", "true");
                    Log.reset();
                    GuidedFuzzingForPatched.run(testClass.getName(), method.getName(), this.loaderForPatch, reproGuidance, System.out);
                    System.setProperty("jqf.ei.run_patch", "false");
                } else {
                    // System.out.println("Failed to log out actual");
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

    private void evaluatePatch(ReproGuidance guidance, List<Generator<?>> generators) throws Throwable {
        // Keep fuzzing until no more input or I/O error with guidance
        wideningCount--;
        try {
            // update input range
            updateInputRange(generators);

            // Keep fuzzing as long as guidance wants to
            while (guidance.hasInput()) {
                Log.logOutIfCalled = false;
                Result result = INVALID;
                Throwable error = null;

                // Initialize guided fuzzing using a file-backed random number source
                try {
                    Object[] args;
                    try {

                        // Generate input values
                        StreamBackedRandom randomFile = new StreamBackedRandom(guidance.getInput(), Long.BYTES);
                        SourceOfRandomness random = new FastSourceOfRandomness(randomFile);
                        GenerationStatus genStatus = new NonTrackingGenerationStatus(random);
                        args = generators.stream()
                                .map(g -> g.generate(random, genStatus))
                                .toArray();

                        Log.logIn(args);

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

    private void init() {
        String logDir = System.getProperty("jqf.ei.logDir");
        String inputID = System.getProperty("jqf.ei.inputID");

        if (logDir == null) {
            return;
        }

        Path dir;
        if (inputID == null)
            dir = Paths.get(logDir);
        else
            dir = Paths.get(logDir, inputID);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Failed to create directory " + dir);
            e.printStackTrace();
        }
        List<Path> files = new ArrayList<>();
        files.add(Paths.get(dir.toString(), "OUT.log"));
        files.add(Paths.get(dir.toString(), "IN.log"));

        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                System.err.println("Failed to delete " + file);
            }

            try {
                Files.createFile(file);
            } catch (IOException e) {
                System.err.println("Failed to create " + file);
            }
        }
    }

    private void updateInputRange(List<Generator<?>> generators) {
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
                        updateRange(gen2, (InRange) ann);
                    }
                }
            }
        }
    }

    private void updateRange(Generator<?> gen, InRange range) {
        try {
            if(!range.isFixed()){
                range = inRangeFactory.generate(gen, range, wideningCount);
            }
            Method m = gen.getClass().getMethod("configure", InRange.class);
            m.invoke(gen, range);
            if (gen instanceof IntegerGenerator) {
                System.out.println("[" + range.minInt() + ", " + range.maxInt() + "]");
            }
        } catch (NoSuchMethodException e) {
            System.err.println(String.format("Class %s does not have configure(InRange)", gen.getClass()));
        } catch (IllegalAccessException e) {
            System.err.println(String.format("Class %s does not have public configure(InRange)", gen.getClass()));
        } catch (InvocationTargetException e) {
            System.err.println(String.format("An exception is thrown from %s.configure(InRange)", gen.getClass()));
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
