package edu.berkeley.cs.jqf.fuzz.prop;

import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI;
import edu.berkeley.cs.jqf.fuzz.ei.ZestCLI2;
import edu.berkeley.cs.jqf.fuzz.repro.ReproDriver;
import edu.berkeley.cs.jqf.fuzz.repro.ReproDriver2;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results-patch");
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
                "../src/test/resources/patches/Patch27/Math2b/target/test-classes:../src/test/resources/patches/Patch27/Math2b/target/classes:../../aspect/tracing.jar",
                "../src/test/resources/patches/Patch27/Math2p/target/test-classes:../src/test/resources/patches/Patch27/Math2p/target/classes:../../aspect/tracing.jar",
                "org.apache.commons.math3.distribution.JQF_HypergeometricDistributionTest", "testMath1021"});
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
                "../src/test/resources/patches/Patch180/Time4b/target/test-classes:../src/test/resources/patches/Patch180/Time4b/target/classes:../../aspect/tracing.jar",
                "../src/test/resources/patches/Patch180/Time4p/target/test-classes:../src/test/resources/patches/Patch180/Time4p/target/classes:../../aspect/tracing.jar",
                "org.joda.time.JQF_TestPartial_Basics", "testWith3"});
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
    public void runZestCLI2_patch197() throws IOException {
        //System.setProperty("org.aspectj.weaver.loadtime.configuration", "aspect/aop.xml");

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
                "-o", "../src/test/resources/fuzz-results-patch",
                "../src/test/resources/patches/Patch197/Math25b/target/test-classes:../src/test/resources/patches/Patch197/Math25b/target/classes",
                "../src/test/resources/patches/Patch197/Math25p/target/test-classes:../src/test/resources/patches/Patch197/Math25p/target/classes",
                "org.apache.commons.math3.optimization.fitting.JQF_HarmonicFitterTest", "testMath844"});
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
                "org.apache.commons.math3.optimization.linear.JQF_SimplexSolverTest", "testMath828Cycle"});
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
                "org.apache.commons.math.linear.JQF_EigenDecompositionImplTest", "testMathpbx02"});
    }

//    @Test
//    public void runZestCLI2_patch157() throws IOException {
//        System.setProperty("org.aspectj.weaver.loadtime.configuration", "aspect/aop.xml");
//
//        Path fuzz_results_patch_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "fuzz-results-patch");
//        Path log_dir = FileSystems.getDefault().getPath("..", "src", "test", "resources", "log");
//
//        if (fuzz_results_patch_dir.toFile().exists()) {
//            Files.walk(fuzz_results_patch_dir)
//                    .sorted(Comparator.reverseOrder())
//                    .map(Path::toFile)
//                    .forEach(File::delete);
//        }
//        if (log_dir.toFile().exists()) {
//            Files.walk(log_dir)
//                    .sorted(Comparator.reverseOrder())
//                    .map(Path::toFile)
//                    .forEach(File::delete);
//        }
//
//        ZestCLI2.main(new String[] {
//                "--target", "org/apache/commons/math3/analysis/FunctionUtils.java:146",
//                "--save-all-inputs",
//                "--logdir", "../src/test/resources/log",
//                "--seed", "885441",
//                "--max-corpus-size", "10",
//                "--plateau-threshold", "10",
//                "--verbose",
//                "-o", "../src/test/resources/fuzz-results-patch",
//                "../src/test/resources/patches/Patch157/Math24b/target/test-classes:../src/test/resources/patches/Patch157/Math24b/target/classes",
//                "../src/test/resources/patches/Patch157/Math24p/target/test-classes:../src/test/resources/patches/Patch157/Math24p/target/classes",
//                "org.apache.commons.math3.analysis.JQF_FunctionUtilsTest", "testMath855"});
//    }

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
            "org.apache.commons.math3.ode.nonstiff.JQF_DormandPrince853IntegratorTest", "testEventsScheduling"});
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
                "org.joda.time.format.JQF_TestDateTimeFormatter", "testParseInto_monthOnly_baseStartYear"});
    }
}
