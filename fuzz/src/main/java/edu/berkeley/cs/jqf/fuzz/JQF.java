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
package edu.berkeley.cs.jqf.fuzz;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.internal.generator.GeneratorRepository;
import com.pholser.junit.quickcheck.internal.generator.ServiceLoaderGeneratorSource;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI2;
import edu.berkeley.cs.jqf.fuzz.junit.quickcheck.FuzzStatement;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * This class extends JUnit and Quickcheck runners to enable guided
 * fuzz testing. 
 *
 * @author Rohan Padhye
 */
public class JQF extends JUnitQuickcheck {

    protected final GeneratorRepository generatorRepository;
    private String classPathForPatch;
    private ArrayList<String> classPathForEachPatch = new ArrayList<>();

    @SuppressWarnings("unused") // Invoked reflectively by JUnit
    public JQF(Class<?> clazz) throws InitializationError {
        super(clazz);
        // Initialize generator repository with a deterministic seed (for reproducibility)
        SourceOfRandomness randomness = new SourceOfRandomness(new Random(42));
        this.generatorRepository = new GeneratorRepository(randomness).register(new ServiceLoaderGeneratorSource());

        classPathForPatch = System.getProperty("jqf.ei.CLASSPATH_FOR_PATCH");
        classPathForEachPatch = new ArrayList<String>(Arrays.asList(System.getProperty("jqf.ei.CLASSPATH_FOR_PATCH").split(File.pathSeparator)));


        if (Boolean.getBoolean(System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ"))) {
            File dir = new File(classPathForPatch.split("patched")[0] + "patched");
            File files[] = dir.listFiles();

            for (int i = 0; i < files.length; i++) {
                System.out.println("patched dir: " + files[i]);
                classPathForEachPatch.add(files[i].toString() + classPathForPatch.split("patched")[1]);
            }
        }
    }

    @Override protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> methods = super.computeTestMethods();
        methods.addAll(getTestClass().getAnnotatedMethods(Fuzz.class));
        return methods;
    }


    @Override protected void validateTestMethods(List<Throwable> errors) {
        super.validateTestMethods(errors);
        validateFuzzMethods(errors);
    }

    private void validateFuzzMethods(List<Throwable> errors) {
        for (FrameworkMethod method : getTestClass().getAnnotatedMethods(Fuzz.class)) {
            method.validatePublicVoid(false, errors);
            if (method.getAnnotation(Property.class) != null) {
                errors.add(new Exception("Method " + method.getName() +
                        " cannot have both @Property and @Fuzz annotations"));
            }
        }
    }

    public void setEachClasspath(int index) {
        System.out.println("Current Classpath: " + classPathForEachPatch.get(index));
        this.classPathForPatch = classPathForEachPatch.get(index);
    }

    @Override public Statement methodBlock(FrameworkMethod method) {
        if (method.getAnnotation(Fuzz.class) != null) {
            if (this.classPathForPatch != null) {
                System.out.println("MULTIFUZZ: " + System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ"));
                if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
                    ArrayList<ClassLoader> loaderForPatch = new ArrayList<>();
                    ArrayList<PatchInfo> patchInfos = new ArrayList<>();
                    try {
                        File dir = new File(classPathForPatch.split("patched")[0] + "patched");
                        File files[] = dir.listFiles();

                        for (int i = 0; i < files.length; i++) {
                            System.out.println("patched dir: " + files[i]);
                            System.out.println("Replaced: " + this.classPathForPatch.replace("patched", "patched/"+files[i].toString().split("patched/")[1]));
                            ClassLoader newClassLoader = new InstrumentingClassLoader(
                                    this.classPathForPatch.replace("patched", "patched/"+files[i].toString().split("patched/")[1]).split(File.pathSeparator),
                                    ZestCLI2.class.getClassLoader());
                            loaderForPatch.add(newClassLoader);
                            PatchInfo newPatchInfo = new PatchInfo(this.classPathForPatch.replace("patched", "patched/"+files[i].toString().split("patched/")[1]), newClassLoader, 0);
                            patchInfos.add(newPatchInfo);
                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        System.exit(2);
                    }
                    System.out.println("Multi");
                    return new FuzzStatement(method, getTestClass(), generatorRepository, patchInfos);
                }
                else {
                    ClassLoader loaderForPatch = null;
                    try {
                        loaderForPatch = new InstrumentingClassLoader(
                                this.classPathForPatch.split(File.pathSeparator),
                                ZestCLI2.class.getClassLoader());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        System.exit(2);
                    }
                    System.out.println("Not multi");
                    return new FuzzStatement(method, getTestClass(), generatorRepository, loaderForPatch);
                }

            } else {
                return new FuzzStatement(method, getTestClass(), generatorRepository);
            }
        }
        return super.methodBlock(method);
    }
}
