package edu.berkeley.cs.jqf.fuzz.prop;

import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI;
import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI2;
import edu.berkeley.cs.jqf.fuzz.repro.ReproDriver;
import edu.berkeley.cs.jqf.fuzz.repro.ReproDriver2;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
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

    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }

    @Test
    public void runZestCLI2_patch27() {
        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_patch_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_patch_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/distribution/AbstractIntegerDistribution.java:138",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", "../src/test/resources/fuzz-results",
                "../src/test/resources/patches/Patch27/Math2b/target/test-classes:../src/test/resources/patches/Patch27/Math2b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch27/Math2p/target/test-classes:../src/test/resources/patches/Patch27/Math2p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math3.distribution.JQF_HypergeometricDistributionTest", "testMath1021",
                "--srcdir-for-org", "../src/test/resources/patches/Patch27/Math2b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch27/Math2p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_patch180() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/joda/time/Partial.java:459",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Patch180/Time4b/target/test-classes:../src/test/resources/patches/Patch180/Time4b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch180/Time4p/target/test-classes:../src/test/resources/patches/Patch180/Time4p/target/classes:../aspect/tracing.jar",
                "org.joda.time.JQF_TestPartial_Basics", "testWith3",
                "--srcdir-for-org", "../src/test/resources/patches/Patch180/Time4b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch180/Time4p/src/main/java",
                "--enable-dist-to-target"
        });
    }

    @Test
    public void runRepro_patch180() throws IOException {
        // Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log", "FIX", "id_000000004");
        Path input = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results", "diff_out", "id_000000004");

        ReproDriver.main(new String[] {
                "--cp",
                "../src/test/resources/patches/Patch180/Time4f/target/test-classes:../src/test/resources/patches/Patch180/Time4f/target/classes",
                "--logdir", log_dir.toString(),
                "org.joda.time.JQF_TestPartial_Basics", "testWith3",
                input.toString()
        });
    }

    @Test
    public void runZestCLI2_patch181() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/joda/time/format/DateTimeParserBucket.java:359",
                "--logdir", "../src/test/resources/log",
                "--seed", "88546",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "30m",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Patch181/Time7b/target/test-classes:../src/test/resources/patches/Patch181/Time7b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch181/Time7p/target/test-classes:../src/test/resources/patches/Patch181/Time7p/target/classes:../aspect/tracing.jar",
                "org.joda.time.format.JQF_TestDateTimeFormatter", "testParseInto_monthDay_feb29_tokyo_endOfYear",
                "--srcdir-for-org", "../src/test/resources/patches/Patch181/Time7b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch181/Time7p/src/main/java"
    });
    }

    // CORRECT PATCH
    // TODO: check whether the target is hit
    @Test
    public void runZestCLI2_patch197() throws IOException {
        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results-patch");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_patch_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_patch_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/optimization/fitting/HarmonicFitter.java:327",
//                "--save-all-inputs",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", "../src/test/resources/fuzz-results",
                "../src/test/resources/patches/Patch197/Math25b/target/test-classes:../src/test/resources/patches/Patch197/Math25b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch197/Math25p/target/test-classes:../src/test/resources/patches/Patch197/Math25p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math3.optimization.fitting.JQF_HarmonicFitterTest", "testMath844",
                "--srcdir-for-org", "../src/test/resources/patches/Patch197/Math25b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch197/Math25p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_patch32() throws IOException {
        //System.setProperty("org.aspectj.weaver.loadtime.configuration", "aspect/aop.xml");

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
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch32/Math28b/target/test-classes:../src/test/resources/patches/Patch32/Math28b/target/classes",
                "../src/test/resources/patches/Patch32/Math28p/target/test-classes:../src/test/resources/patches/Patch32/Math28p/target/classes",
                "org.apache.commons.math3.optimization.linear.JQF_SimplexSolverTest", "testMath828Cycle",
                "--srcdir-for-org", "../src/test/resources/patches/Patch32/Math28b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch32/Math28p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_patch34() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/geometry/euclidean/twod/PolygonsSet.java:136",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "1h",
                "--exploreDuration", "10m",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Patch34/Math32b/target/test-classes:../src/test/resources/patches/Patch34/Math32b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch34/Math32p/target/test-classes:../src/test/resources/patches/Patch34/Math32p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math3.geometry.euclidean.threed.JQF_PolyhedronsSetTest", "testIssue780",
                "--srcdir-for-org", "../src/test/resources/patches/Patch34/Math32b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch34/Math32p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_patch172() throws IOException {
        //System.setProperty("org.aspectj.weaver.loadtime.configuration", "file:../aspect/aop.xml");

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
                "--target", "org/apache/commons/math/linear/EigenDecompositionImpl.java:1139",
                "--save-all-inputs",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "10",
                "--widening-plateau-threshold", "10",
                "--verbose",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch172/Math80b/target/test-classes:../src/test/resources/patches/Patch172/Math80b/target/classes",
                "../src/test/resources/patches/Patch172/Math80p/target/test-classes:../src/test/resources/patches/Patch172/Math80p/target/classes",
                "org.apache.commons.math.linear.JQF_EigenDecompositionImplTest", "testMathpbx02",
                "--srcdir-for-org", "../src/test/resources/patches/Patch172/Math80b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch172/Math80p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_Patch156() throws IOException {
    // turn on the following to see instrumentation log
    // System.setProperty("janala.verbose", "true");
    // String currentDir = System.getProperty("user.dir");
    //System.setProperty("org.aspectj.weaver.loadtime.configuration", "aspect/aop.xml");
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
            "--target", "org/apache/commons/math3/ode/AbstractIntegrator.java:373",
            "--save-all-inputs",
            "--logdir", "../src/test/resources/log",
            "--seed", "885441",
            //"--max-corpus-size", "15",
            //"--plateau-threshold", "10",
            "--widening-plateau-threshold", "10",
            "--verbose",
            "--duration", "30s",
            "--delta", "0",
            "-o", "../src/test/resources/fuzz-results-patch",
            "../src/test/resources/patches/Patch156/Math7b/target/test-classes:../src/test/resources/patches/Patch156/Math7b/target/classes",
            "../src/test/resources/patches/Patch156/Math7p/target/test-classes:../src/test/resources/patches/Patch156/Math7p/target/classes",
            "org.apache.commons.math3.ode.nonstiff.JQF_DormandPrince853IntegratorTest", "testEventsScheduling",
            "--srcdir-for-org", "../src/test/resources/patches/Patch156/Math7b/src/main/java",
            "--srcdir-for-patch", "../src/test/resources/patches/Patch156/Math7p/src/main/java"
    });
}
    @Test
    public void runZestCLI2_Time16() throws IOException {
        // turn on the following to see instrumentation log
        // System.setProperty("janala.verbose", "true");
        // String currentDir = System.getProperty("user.dir");
        //System.setProperty("org.aspectj.weaver.loadtime.configuration", "aspect/aop.xml");
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
                "--target", "org/joda/time/format/DateTimeFormatter.java:710",
                "--save-all-inputs",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                //"--max-corpus-size", "15",
                "--widening-plateau-threshold", "10",
                "--verbose",
                "--duration", "30s",
                "--delta", "0",
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/timebug16/Time16b/build/tests:../src/test/resources/patches/timebug16/Time16b/build/classes",
                "../src/test/resources/patches/timebug16/Time16p/build/tests:../src/test/resources/patches/timebug16/Time16p/build/classes",
                "org.joda.time.format.JQF_TestDateTimeFormatter", "testParseInto_monthOnly_baseStartYear",
                "--srcdir-for-org", "../src/test/resources/patches/timebug16/Time16b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/timebug16/Time16p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_Mathbug58() throws IOException {
        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
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
                "--target", "org/apache/commons/math/optimization/fitting/GaussianFitter.java:121",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                //"--max-corpus-size", "15",
                "--widening-plateau-threshold", "10",
                "--max-mutations", "200",
                "--verbose",
                "--duration", "60s",
                "--exploreDuration", "3h",
                "--delta", "0",
                "-o", "../src/test/resources/fuzz-results",
                "../src/test/resources/patches/Mathbug58/Math58b/target/test-classes:../src/test/resources/patches/Mathbug58/Math58b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Mathbug58/Math58p/target/test-classes:../src/test/resources/patches/Mathbug58/Math58p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math.optimization.fitting.JQF_GaussianFitterTest", "testMath519",
                "--srcdir-for-org", "../src/test/resources/patches/Mathbug58/Math58b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Mathbug58/Math58p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_patch30() {
        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_patch_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_patch_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/distribution/DiscreteDistribution.java:187",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", "../src/test/resources/fuzz-results",
                "../src/test/resources/patches/Patch30/Math95b/target/test-classes:../src/test/resources/patches/Patch30/Math95b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch30/Math95p/target/test-classes:../src/test/resources/patches/Patch30/Math95p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math.distribution.JQF_FDistributionTest", "testMath1021",
                "--srcdir-for-org", "../src/test/resources/patches/Patch30/Math95b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch30/Math95p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_335_NFL_ACS_PATCH() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/util/MathArrays.java:846",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "30m",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/335_NFL_ACS_Patch_1_1/Math3b/target/test-classes:../src/test/resources/patches/335_NFL_ACS_Patch_1_1/Math3b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/335_NFL_ACS_Patch_1_1/Math3p/target/test-classes:../src/test/resources/patches/335_NFL_ACS_Patch_1_1/Math3p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math3.util.JQF_MathArraysTest", "testLinearCombinationWithSingleElementArray",
                "--srcdir-for-org", "../src/test/resources/patches/335_NFL_ACS_Patch_1_1/Math3b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/335_NFL_ACS_Patch_1_1/Math3p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_time11() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--threadName", "testThread",
                "--target", "org/joda/time/tz/ZoneInfoCompiler.java:70",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Time11/Time11b/target/test-classes:../src/test/resources/patches/Time11/Time11b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Time11/Time11p/target/test-classes:../src/test/resources/patches/Time11/Time11p/target/classes:../aspect/tracing.jar",
                "org.joda.time.tz.JQF_TestCompiler", "testDateTimeZoneBuilder",
                "--srcdir-for-org", "../src/test/resources/patches/Time11/Time11b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Time11/Time11p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_patch182() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--threadName", "testThread",
                "--target", "org/joda/time/tz/ZoneInfoCompiler.java:70",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Patch182/Time11b/target/test-classes:../src/test/resources/patches/Patch182/Time11b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch182/Time11p/target/test-classes:../src/test/resources/patches/Patch182/Time11p/target/classes:../aspect/tracing.jar",
                "org.joda.time.tz.JQF_TestCompiler", "testDateTimeZoneBuilder",
                "--srcdir-for-org", "../src/test/resources/patches/Patch182/Time11b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Patch182/Time11p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_Lang_bug_24() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/lang3/math/NumberUtils.java:1413",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Lang24/Lang24b/target/tests:../src/test/resources/patches/Lang24/Lang24b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Lang24/Lang24p/target/tests:../src/test/resources/patches/Lang24/Lang24p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.lang3.math.JQF_NumberUtilsTest", "testIsNumber",
                "--srcdir-for-org", "../src/test/resources/patches/Lang24/Lang24b/src/main/java",
                "--srcdir-for-patch", "../src/test/resources/patches/Lang24/Lang24p/src/main/java"
        });
    }

    @Test
    public void runZestCLI2_patch192() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/lang3/ArrayUtils.java:3290,org/apache/commons/lang3/ArrayUtils.java:3570",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Patch192/Lang35b/target/tests:../src/test/resources/patches/Patch192/Lang35b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch192/Lang35p/target/tests:../src/test/resources/patches/Patch192/Lang35p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.lang3.JQF_ArrayUtilsAddTest", "testLANG571"});
    }

    @Test
    public void runZestCLI2_patch54() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math/analysis/solvers/BrentSolver.java:138",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "10",
                "--widening-plateau-threshold", "10",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "2m",
                "--exploreDuration", "5s",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Patch54/Math73p/target/test-classes:../src/test/resources/patches/Patch54/Math73p/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Patch54/Math73p/target/test-classes:../src/test/resources/patches/Patch54/Math73p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math.analysis.solvers.JQF_BrentSolverTest", "testBadEndpoints"});
    }

    @Test
    public void runZestCLI2_Mathbug41() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math/stat/descriptive/moment/Variance.java:520",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Math41/Math41b/target/test-classes:../src/test/resources/patches/Math41/Math41b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Math41/Math41p/target/test-classes:../src/test/resources/patches/Math41/Math41p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math.stat.descriptive.moment.JQF_VarianceTest", "testEvaluateArraySegmentWeighted"
        });
    }

    @Test
    public void runZestCLI2_Mathbug7() {
        Path fuzz_results_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results");
        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");

        if (fuzz_results_dir.toFile().exists()) {
            executeCommand("rm -rf " + fuzz_results_dir);
        }
        if (log_dir.toFile().exists()) {
            executeCommand("rm -rf " + log_dir);
        }

        ZestCLI2.main(new String[] {
                "--target", "org/apache/commons/math3/ode/AbstractIntegrator.java:375",
                "--logdir", "../src/test/resources/log",
                "--seed", "885441",
                "--max-corpus-size", "100",
                "--widening-plateau-threshold", "50",
                "--verbose",
                "--max-mutations", "200",
                "--duration", "12h",
                "--exploreDuration", "3h",
                //"--delta", "1e-6",
                "-o", fuzz_results_dir.toString(),
                "../src/test/resources/patches/Math7/Math7b/target/test-classes:../src/test/resources/patches/Math7/Math7b/target/classes:../aspect/tracing.jar",
                "../src/test/resources/patches/Math7/Math7p/target/test-classes:../src/test/resources/patches/Math7/Math7p/target/classes:../aspect/tracing.jar",
                "org.apache.commons.math3.ode.nonstiff.JQF_DormandPrince853IntegratorTest", "testEventsScheduling"
        });
    }
}
