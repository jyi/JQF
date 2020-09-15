package edu.berkeley.cs.jqf.fuzz.prop;

import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI;
import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI2;
import edu.berkeley.cs.jqf.fuzz.repro.ReproDriver2;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Ignore
public class PropagationTest {

    @Test
    public void runRepro() {
        ReproDriver2.main(new String[] {
                "../src/test/resources/patches/Patch27/Math2b/target/test-classes:../src/test/resources/patches/Patch27/Math2b/target/classes",
                "org.apache.commons.math3.distribution.JQF_HypergeometricDistributionTest", "testMath1021",
                "../src/test/resources/fuzz-results-patch/all/id_000000030"});
    }

    @Test
    public void runZestCLI() throws IOException {
        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results-patch");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_patch_dir.toFile().exists()) {
            Files.walk(fuzz_results_patch_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        if (log_dir.toFile().exists()) {
            Files.walk(log_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        ZestCLI.main(new String[] {
                "--target", "org/apache/commons/math3/distribution/AbstractIntegerDistribution.java:138",
                "--save-all-inputs",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "10",
                "--plateau-threshold", "100",
                "--exit-on-plateau",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch27/Math2p/target/test-classes:../src/test/resources/patches/Patch27/Math2p/target/classes",
                "org.apache.commons.math3.distribution.JQF_HypergeometricDistributionTest", "testMath1021"});
    }

    @Test
    public void runZestCLI2() throws IOException {
        // turn on the following to see instrumentation log
        // System.setProperty("janala.verbose", "true");
        // String currentDir = System.getProperty("user.dir");
        System.setProperty("org.aspectj.weaver.loadtime.configuration", "file:../aspect/aop.xml");
        // System.setProperty("org.aspectj.weaver.loadtime.configuration", "file:/home/elkhan/Remote/poracle/modules/JQF/aspect/aop.xml");

        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results-patch");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_patch_dir.toFile().exists()) {
            Files.walk(fuzz_results_patch_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        if (log_dir.toFile().exists()) {
            Files.walk(log_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/distribution/AbstractIntegerDistribution.java:138",
                "--save-all-inputs",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
//                "--max-corpus-size", "15",
//                "--plateau-threshold", "10",
                "--verbose",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch27/Math2b/target/test-classes:../src/test/resources/patches/Patch27/Math2b/target/classes:../../aspect/tracing.jar",
                "../src/test/resources/patches/Patch27/Math2p/target/test-classes:../src/test/resources/patches/Patch27/Math2p/target/classes:../../aspect/tracing.jar",
                "org.apache.commons.math3.distribution.JQF_HypergeometricDistributionTest", "testMath1021"});
    }

    @Test
    public void runZestCLI2_patch197() throws IOException {
        System.setProperty("org.aspectj.weaver.loadtime.configuration", "aspect/aop.xml");

        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results-patch");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_patch_dir.toFile().exists()) {
            Files.walk(fuzz_results_patch_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        if (log_dir.toFile().exists()) {
            Files.walk(log_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/optimization/fitting/HarmonicFitter.java:327",
                "--save-all-inputs",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "10",
                "--plateau-threshold", "10",
                "--verbose",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch197/Math25b/target/test-classes:../src/test/resources/patches/Patch197/Math25b/target/classes",
                "../src/test/resources/patches/Patch197/Math25p/target/test-classes:../src/test/resources/patches/Patch197/Math25p/target/classes",
                "org.apache.commons.math3.optimization.fitting.JQF_HarmonicFitterTest", "testMath844"});
    }
    @Test
    public void runZestCLI2_patch32() throws IOException {
        System.setProperty("org.aspectj.weaver.loadtime.configuration", "aspect/aop.xml");

        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results-patch");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_patch_dir.toFile().exists()) {
            Files.walk(fuzz_results_patch_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        if (log_dir.toFile().exists()) {
            Files.walk(log_dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/optimization/linear/SimplexSolver.java:124",
                "--save-all-inputs",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "10",
                "--plateau-threshold", "10",
                "--verbose",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch32/Math28b/target/test-classes:../src/test/resources/patches/Patch32/Math28b/target/classes",
                "../src/test/resources/patches/Patch32/Math28p/target/test-classes:../src/test/resources/patches/Patch32/Math28p/target/classes",
                "org.apache.commons.math3.optimization.linear.JQF_SimplexSolverTest", "testMath828Cycle"});
    }
}
