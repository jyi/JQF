package edu.berkeley.cs.jqf.fuzz.prop;

import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI;
import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI2;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class PropagationTest {

    @Ignore
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

    @Ignore
    @Test
    public void runZestCLI2() throws IOException {
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
                "--max-corpus-size", "10",
                "--plateau-threshold", "100",
                "--exit-on-plateau",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch27/Math2b/target/test-classes:../src/test/resources/patches/Patch27/Math2b/target/classes",
                "../src/test/resources/patches/Patch27/Math2p/target/test-classes:../src/test/resources/patches/Patch27/Math2p/target/classes",
                "org.apache.commons.math3.distribution.JQF_HypergeometricDistributionTest", "testMath1021"});
    }
}
