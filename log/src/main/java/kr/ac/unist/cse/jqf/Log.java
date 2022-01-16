package kr.ac.unist.cse.jqf;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import edu.berkeley.cs.jqf.instrument.tracing.TraceLogger;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Log {

    public static File logFile;
    public static boolean verbose = true;

    public static File measureTimeFile;

//    private TraceLogger singleton;

    public static String currOutPath = new String();

    public static DslJson<Object> dslJson = new DslJson<Object>();
    public static DslJson<Object> dslJson2 = new DslJson<Object>();
    //writer should be reused. For per thread reuse use ThreadLocal pattern
    public static JsonWriter writer = dslJson.newWriter();
    public static JsonWriter methodWriter = dslJson2.newWriter();

    public boolean diffOutFound = false;
    public boolean itWasDiff = false;

    public String savedOutForOrg = null;
    public String SavedOutForPatch = null;

    public static class LogResult {
        private static String outputForOrg = null;
        private static String outputForPatch = null;
        public static String currOutPath = new String();
        public static String origOutPath = new String();

        public static void clear() {
            if (Boolean.getBoolean("jqf.ei.run_patch")) {
                outputForPatch = null;
            } else {
                outputForOrg = null;
            }
        }

        public static void addOutput(String out) {
            String realOut = null;
            if (!(out.equals("IGNORE_OUTPUT") ||
                    out.contains("edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException")))
                realOut = out;

            if (Boolean.getBoolean("jqf.ei.run_patch")) {
                outputForPatch = outputForPatch == null? realOut : outputForPatch + realOut;
            } else {
                outputForOrg = outputForOrg == null? realOut : outputForOrg + realOut;
            }
        }

        public void Cmd() throws IOException {
            List cmdList = new ArrayList();
            cmdList.add("pwd");

            Process process = null;
            String str = null;

            try {
                process = new ProcessBuilder(cmdList).start();
                BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));

                while((str = stdOut.readLine()) != null) {
                    System.out.println(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static boolean isDiffOutputFound() {
            if(outputForOrg == null || outputForPatch == null) {
                return false;
            }
            assert outputForOrg != null;
            assert outputForPatch != null;

//            System.out.println("isDiffOutputFound is called");
//            System.out.println("outputForOrg: " + outputForOrg);
//            System.out.println("outputForPatch: " + outputForPatch);

            if (outputForOrg.contains("IGNORE_OUTPUT"))
                return false;

            boolean opad = Boolean.parseBoolean(System.getProperty("jqf.ei.opad"));
            if (opad && outputForPatch.contains("IGNORE_OUTPUT"))
                return false;

            try {
                // TODO: currently, we only handle single number case.
                // What if output has has more than one numbers?
                // What if output is not a number?
                double delta = Double.parseDouble(System.getProperty("jqf.ei.delta"));
                if (Math.abs(Double.parseDouble(outputForOrg) - Double.parseDouble(outputForPatch)) > delta) {
                    return true;
                }
                else if (!outputForOrg.equals(outputForPatch)) {
                    return true;
                }
                else
                    return false;
            } catch (NumberFormatException e) { }
            // fall back to equals
            return !outputForOrg.equals(outputForPatch);
        }
    }

    public static boolean logOutIfCalled = false;
    public static boolean runBuggyVersion = false;

    private static int ignoreCount = 0;
    private static int actualCount = 0;

    public static void turnOnRunBuggyVersion() {
        Log.runBuggyVersion = true;
    }

    public static void turnOffRunBuggyVersion() {
        Log.runBuggyVersion = false;
    }

    private static String getLogDir() {
        String logDir = System.getProperty("jqf.ei.logDir");
//        System.out.println("getLogDir");
        if (logDir == null) {
            return null;
        }

//        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
//            if (Log.runBuggyVersion) {
//                logDir += File.separator + "ORG";
//            } else {
//                logDir += File.separator + "PATCH";
//            }
//        }
        if (System.getProperty("jqf.ei.repro_fix") != null && System.getProperty("jqf.ei.PATCH_ID") != null) {
            String patchIndex = System.getProperty("jqf.ei.PATCH_ID");
//            System.out.println(System.getProperty("jqf.ei.PATCH_ID"));
            logDir = logDir.replace("all", "PATCH" + File.separator + patchIndex + File.separator + "FIXED");
//            System.out.println("FixedLog: " + logDir);
//            logDir += File.separator + "PATCH" + File.separator + patchIndex + File.separator + "FIXED";
            return logDir;
        }
        if (System.getProperty("kr.ac.unist.cse.jqf.NO_FUZZ").equals("true")) {
            logDir += File.separator + "D4JTST";
            if(System.getProperty("jqf.ei.fail_tests") != null && System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
                if (System.getProperty("jqf.ei.fail_tests").contains(System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"))) {
                    logDir = logDir + "/" + "FAIL";
                }
                else {
                    logDir = logDir + "/" + "PASS";
                }
            }
            if (System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
                logDir = logDir + "/" + System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD");
            }
        }
        else if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
            String patchIndex = System.getProperty("jqf.ei.PATCH_ID");
//                System.out.println("Current Patch: " + patchIndex);
//                logDir += File.separator + "PATCH" + File.separator + patchIndex.split("patched/")[1].split("/target")[0];
            logDir += File.separator + "PATCH" + File.separator + patchIndex;
            if (Log.runBuggyVersion) {
                logDir += File.separator + "ORG";
            }

            else if (System.getProperty("jqf.ei.run_patch").equals("true")) {
                logDir += File.separator + "PATCHED";
            }

//                System.out.println("New LogDir: " + logDir);
        }
//        if (System.getProperty("jqf.ei.run_fix") != null) {
////            System.out.println("RunFix: " + System.getProperty("jqf.ei.run_fix"));
////            System.out.println(System.getProperty("jqf.ei.PATCH_ID"));
//
////            if (System.getProperty("jqf.ei.run_fix").equals("true") && System.getProperty("jqf.ei.PATCH_ID") != null) {
////
//////                logDir += File.separator + "FIXED";
////            }
//
////            else {
////                logDir += File.separator + "PATCH";
////            }
//        }




        return logDir;
    }



    public static void writeToFile(String xml, String filename) {
        String logDir = getLogDir();
        if (logDir == null) {
            return;
        }

        Path outFile;
        String inputID = System.getProperty("jqf.ei.inputID");
        if (inputID == null) {
            outFile = Paths.get(logDir, filename);
        } else {
            try {
                Files.createDirectories(Paths.get(logDir, inputID));
            } catch (IOException e) {
                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                e.printStackTrace();
            }
            outFile = Paths.get(logDir, inputID, filename);
        }
        if (Files.exists(outFile)) {
            try {
                Files.delete(outFile);
            } catch (IOException e) {
                System.err.println("Failed to delete a file: " + outFile);
                e.printStackTrace();
            }
        }
//        try {
//            Files.createFile(outFile);
//        } catch (IOException e) {
//            System.err.println("Failed to create a file: " + outFile);
//            e.printStackTrace();
//        }
//
//
//        try {
//            Files.write(outFile, xml.getBytes(),
//                    StandardOpenOption.WRITE);
//        } catch (IOException e) {
//            System.err.println("Failed to write output due to IOException");
//        }
    }


    public static void reset() {
        logOutIfCalled = false;
        ignoreCount = 0;
        actualCount = 0;
        LogResult.clear();
        emptyOutFile();
        emptyInFile();
    }

    private static void emptyOutFile() {
        String logDir = System.getProperty("jqf.ei.logDir");
        Path outFile;
        String inputID = System.getProperty("jqf.ei.inputID");
        if (inputID == null) {
            outFile = Paths.get(logDir, "OUT.log");
        } else {
            try {
                Files.createDirectories(Paths.get(logDir, inputID));
            } catch (IOException e) {
                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                e.printStackTrace();
            }
            outFile = Paths.get(logDir, inputID, "OUT.log");
//            System.out.println("Output: " + outFile.toString());
        }
        try {
            Files.deleteIfExists(outFile);
        } catch (IOException e) {
            System.err.println("Failed to create a file: " + outFile);
            e.printStackTrace();
        }
    }
    private static void emptyInFile() {
        String logDir = System.getProperty("jqf.ei.logDir");
        Path inFile;
        String inputID = System.getProperty("jqf.ei.inputID");
        if (inputID == null) {
            inFile = Paths.get(logDir, "IN.log");
        } else {
            try {
                Files.createDirectories(Paths.get(logDir, inputID));
            } catch (IOException e) {
                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                e.printStackTrace();
            }
            inFile = Paths.get(logDir, inputID, "IN.log");
        }
        try {
            Files.deleteIfExists(inFile);
        } catch (IOException e) {
            System.err.println("Failed to create a file: " + inFile);
            e.printStackTrace();
        }
    }

    public static void resetLogDirForInput() {
        String logDir = getLogDir();
        String inputID = System.getProperty("jqf.ei.inputID");
        assert inputID != null;
        Path logDirForInput = FileSystems.getDefault().getPath(logDir, inputID);
        if (Files.exists(logDirForInput)) {
            try {
                Files.walk(logDirForInput)
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                System.err.println("Failed to delete " + logDirForInput);
                e.printStackTrace();
            }
        }
    }


    public static int getIgnoreCount() {
        return ignoreCount;
    }

    public static int getActualCount() {
        return actualCount;
    }

    public static void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            if (logFile != null) {
                appendLineToFile(logFile, line);
            } else {
                System.err.println(line);
            }
        }
    }

    private static void appendLineToFile(File file, String line) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println(line);
        } catch (IOException e) { }
    }

    protected static void logOut(String msg) {
//        System.out.println("Output: " + msg);
        LogResult.addOutput(msg);
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
//            System.out.println("out: " + msg);
            return;
        }

//        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
//            if (System.getProperty("jqf.ei.run_fix").equals("true")) {
//                logDir += File.separator + "FIXED";
//            }
//            else if (Log.runBuggyVersion) {
//                logDir += File.separator + "ORG";
//            }
//            else if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
//                String patchIndex = System.getProperty("jqf.ei.CURRENT_PATH_FOR_PATCH");
////                System.out.println("Current Patch: " + patchIndex);
//                logDir += File.separator + "PATCH" + File.separator + patchIndex.split("patched/")[1].split("/target")[0];
////                System.out.println("New LogDir: " + logDir);
//            }
//            else {
//                logDir += File.separator + "PATCH";
//            }
//        }
        logDir = getLogDir();

//        if(System.getProperty("jqf.ei.fail_tests") != null && System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            if (System.getProperty("jqf.ei.fail_tests").contains(System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"))) {
//                logDir = logDir + "/" + "FAIL";
//            }
//            else {
//                logDir = logDir + "/" + "PASS";
//            }
//        }
//        if (System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            logDir = logDir + "/" + System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD");
//        }

        Path outFile;
        String inputID = System.getProperty("jqf.ei.inputID");

        if (inputID == null) {
            outFile = Paths.get(logDir, "OUT.log");
            LogResult.currOutPath = outFile.toString();
            if (!Boolean.getBoolean("jqf.ei.run_patch")) {
                LogResult.origOutPath = outFile.toString();
            }
        } else {
            try {
                Files.createDirectories(Paths.get(logDir, inputID));
            } catch (IOException e) {
                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                e.printStackTrace();
            }
            outFile = Paths.get(logDir, inputID, "OUT.log");
            LogResult.currOutPath = outFile.toString();
            if (!Boolean.getBoolean("jqf.ei.run_patch")) {
                LogResult.origOutPath = outFile.toString();
            }
        }

        if (!Files.exists(outFile)) {
            try {
                Files.createFile(outFile);
            } catch (IOException e) {
                System.err.println("Failed to create a file: " + outFile);
                e.printStackTrace();
            }
        }

        try {
            if (false) {
//                System.out.println("out: " + msg);
                Files.write(outFile, msg.getBytes(),
                        StandardOpenOption.WRITE);
            }
            else {
                Files.write(outFile, msg.getBytes(),
                        StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            System.err.println("Failed to write output due to IOException");
        }
    }

    protected static void logOut(int val) {
        logOut(Integer.toString(val));
    }

    protected static void logOut(double val) {
        logOut(Double.toString(val));
    }

    protected static void logOut(byte val) {
        logOut(Byte.toString(val));
    }

    protected static void logOut(short val) {
        logOut(Short.toString(val));
    }

    protected static void logOut(long val) {
        logOut(Long.toString(val));
    }

    protected static void logOut(float val) {
        logOut(Float.toString(val));
    }

    protected static void logOut(boolean val) {
        if (val) logOut(0);
        else logOut(1);
    }

    protected static void logOut(char val) {
        logOut(Character.getNumericValue(val));
    }

    protected static <T> void logOutArray(T[] arr) {
        if (arr.length <= 0) logOut("Warning: empty array");
        else if (arr.length == 1) logOut(arr[0]);
        else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
    }

    protected static void logOut(Object first, Object ... rest) {
        if (first == null) {
            logOut("null");
        } else if (first instanceof String) {
            logOut((String) first);
        } else if (first instanceof Double) {
            logOut((double) first);
        } else if (first instanceof Integer) {
            logOut((int) first);
        } else if (first instanceof Byte) {
            logOut((byte) first);
        } else if (first instanceof Short) {
            logOut((short) first);
        } else if (first instanceof Long) {
            logOut((long) first);
        } else if (first instanceof Float) {
            logOut((float) first);
        } else if (first instanceof Boolean) {
            logOut((boolean) first);
        } else if (first instanceof Character) {
            logOut((char) first);
        } else if (first.getClass().isArray()) {
            logOutArray((Object[]) first);
        } else {
            throw new RuntimeException("Unexpected type: " + first.getClass());
        }

        for (Object o: rest) {
            logOut(";");
            logOut(o);
        }
    }

    public static <T> void logOutIf(boolean cond, final T actual, T expected) {
        logOutIf(cond, new Actual() {
            @Override
            public Object[] values() {
                return new Object[] { actual };
            }
        }, new Object[] { expected });
    }

    // We assume that an exception should not occur while
    // evaluating actual.
    public static <T> void logOutIf(boolean cond, Actual actual) {
        logOutIf(cond, actual, null);
    }

    public static <T> void logOutIf(boolean cond, Actual actual, T[] expected) {
        if (logOutIfCalled) logOut(";");
        if (Log.runBuggyVersion) {
            if (Boolean.valueOf(System.getProperty("kr.ac.unist.cse.jqf.IGNORE_COND")) || cond) {
                try {
                    logOut(actual.values());
                    actualCount++;
                } catch (Exception e) {
                    ignoreOut();
                }
            } else {
                if (expected != null) {
                    logOut(expected);
                } else {
                    ignoreOut();
                }
            }
        } else {
            try {
                logOut(actual.values());
            } catch (Exception e) {
                logOut("Exception occurred: " + e.getClass());
            } finally {
                actualCount++;
            }
        }
        logOutIfCalled = true;
    }

    public static void ignoreOut() {
        ignoreOut("");
    }

    public static void ignoreOut(String msg) {
        ignoreCount++;
        logOut("IGNORE_OUTPUT: " + msg);
    }

    public static void logMeasuredTime(String id, long time) {
        if(measureTimeFile == null) {
            String logDir = System.getProperty("jqf.ei.logDir");
            if (logDir == null) {
                return;
            }
            measureTimeFile = new File(Paths.get(logDir, "measuredTime.csv").toString());
        }
        appendLineToFile(measureTimeFile, id + "," + time);
    }

    protected static void logIn(String msg) {
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
            System.out.println("in: " + msg);
            return;
        }
//        System.out.println("in: " + msg);
//        System.out.println("LogMultiFuzzing" + System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ"));
//        System.out.println("RunBuggy: " + Log.runBuggyVersion);

//        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
//            if (Log.runBuggyVersion) {
//                logDir += File.separator + "ORG";
//            }
//            else if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
//                String patchIndex = System.getProperty("jqf.ei.CURRENT_PATH_FOR_PATCH");
////                System.out.println("Current Patch: " + patchIndex);
//                logDir += File.separator + "PATCH" + File.separator + patchIndex.split("patched/")[1].split("/target")[0];
////                System.out.println("New In LogDir: " + logDir);
//            }
//            else {
//                logDir += File.separator + "PATCH";
//            }
//        }
        logDir = getLogDir();

//        if(System.getProperty("jqf.ei.fail_tests") != null && System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            if (System.getProperty("jqf.ei.fail_tests").contains(System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"))) {
//                logDir = logDir + "/" + "FAIL";
//            }
//            else {
//                logDir = logDir + "/" + "PASS";
//            }
//        }
//
//        if (System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            logDir = logDir + "/" + System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD");
//        }

        Path inFile;
        String inputID = System.getProperty("jqf.ei.inputID");
        if (inputID == null) {
            if (!Files.exists(FileSystems.getDefault().getPath(logDir))) {
                try {
                    Files.createDirectories(FileSystems.getDefault().getPath(logDir));
                } catch (IOException e) {
                    System.err.println("Failed to create a dir: " + logDir);
                    e.printStackTrace();
                }
            }
            inFile = Paths.get(logDir, "IN.log");
        } else {
            try {
                Files.createDirectories(Paths.get(logDir, inputID));
            } catch (IOException e) {
                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                e.printStackTrace();
            }
            inFile = Paths.get(logDir, inputID, "IN.log");
        }

        if (!Files.exists(inFile)) {
            try {
                Files.createFile(inFile);
            } catch (IOException e) {
                System.err.println("Failed to create a file: " + inFile);
                e.printStackTrace();
            }
        }

        try {
            if (false) {
//                System.out.println("out: " + msg);
                Files.write(inFile, msg.getBytes(),
                        StandardOpenOption.WRITE);
            }
            else {
                Files.write(inFile, msg.getBytes(),
                        StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            System.err.println("Failed to write output due to IOException");
        }
    }

    protected static void logIn(int val) {
        logIn(Integer.toString(val));
    }

    protected static void logIn(double val) {
        logIn(Double.toString(val));
    }

    protected static void logIn(byte val) {
        logIn(Byte.toString(val));
    }

    protected static void logIn(short val) {
        logIn(Short.toString(val));
    }

    protected static void logIn(long val) {
        logIn(Long.toString(val));
    }

    protected static void logIn(float val) {
        logIn(Float.toString(val));
    }

    protected static void logIn(boolean val) {
        if (val) logIn(0);
        else logIn(1);
    }

    protected static void logIn(char val) {
        logIn(Character.getNumericValue(val));
    }

    public static <T> void logInArray(T[] arr) {
        if (arr.length <= 0) logIn("Warning: empty array");
        else if (arr.length == 1) logIn(arr[0]);
        else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
    }

    public static void logIn(Object first, Object ... rest) {
        if (first instanceof  String) {
            logIn((String) first);
        } else if (first instanceof Double) {
            logIn((double) first);
        } else if (first instanceof Integer) {
            logIn((int) first);
        } else if (first instanceof Byte) {
            logIn((byte) first);
        } else if (first instanceof Short) {
            logIn((short) first);
        } else if (first instanceof Long) {
            logIn((long) first);
        } else if (first instanceof Float) {
            logIn((float) first);
        } else if (first instanceof Boolean) {
            logIn((boolean) first);
        } else if (first instanceof Character) {
            logIn((char) first);
        } else if (first.getClass().isArray()) {
            logInArray((Object[]) first);
        } else {
            throw new RuntimeException("Unexpected type: " + first.getClass());
        }

        for (Object o: rest) {
            logIn(";");
            logIn(o);
        }
    }

    public static void logBranchSpectrum(Map<String,Integer> spectrum,boolean isPatch){
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
//            System.out.println("branch: " + logDir);
            return;
        }
        if (System.getProperty("jqf.ei.run_fix") != null) {
            if (System.getProperty("jqf.ei.run_fix").equals("true")) {
                return;
            }
        }


//        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
//            if (System.getProperty("jqf.ei.run_fix").equals("true")) {
//                logDir += File.separator + "FIXED";
//            }
//            else if (!isPatch) {
//                logDir += File.separator + "ORG";
//            }
//            else if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
//                String patchIndex = System.getProperty("jqf.ei.CURRENT_PATH_FOR_PATCH");
////                System.out.println("Current Patch: " + patchIndex);
//                logDir += File.separator + "PATCH" + File.separator + patchIndex.split("patched/")[1].split("/target")[0];
////                System.out.println("New Path LogDir: " + logDir);
//            }
//            else {
//                logDir += File.separator + "PATCH";
//            }
//        }
        logDir = getLogDir();

        Path inFile = null;
        String inputID = System.getProperty("jqf.ei.inputID");

//        if(System.getProperty("jqf.ei.fail_tests") != null && System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            if (System.getProperty("jqf.ei.fail_tests").contains(System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"))) {
//                logDir = logDir + "/" + "FAIL";
//            }
//            else {
//                logDir = logDir + "/" + "PASS";
//            }
//        }
//        if (System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            logDir = logDir + "/" + System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD");
//        }

        if(LogResult.isDiffOutputFound()) {
            logDir = logDir + "/" + "DIFF";
            System.out.println("Diff Log: " + logDir);
        }
        else {
            logDir = logDir + "/" + "SAME";
            System.out.println("Same Log: " + logDir);
        }


        if (inputID == null) {
            if (!Files.exists(FileSystems.getDefault().getPath(logDir))) {
                try {
                    Files.createDirectories(FileSystems.getDefault().getPath(logDir));
                } catch (IOException e) {
                    System.err.println("Failed to create a dir: " + logDir);
                    e.printStackTrace();
                }
            }
            inFile = Paths.get(logDir, "BRANCH.log");
        } else {
            try {
                Files.createDirectories(Paths.get(logDir, inputID));
            } catch (IOException e) {
                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                e.printStackTrace();
            }
            inFile = Paths.get(logDir, inputID, "BRANCH.log");
        }

        if (!Files.exists(inFile)) {
            try {
                Files.createFile(inFile);
            } catch (IOException e) {
                System.err.println("Failed to create a file: " + inFile);
                e.printStackTrace();
            }
        }

        String msg="";
        for (Map.Entry<String,Integer> entry:spectrum.entrySet())
            msg+=entry.getKey()+":"+entry.getValue()+",";

        try {
            //Files.write(inFile, msg.getBytes(),
            //                    StandardOpenOption.APPEND);
            Files.write(inFile, msg.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write branch to " + inFile);
            e.printStackTrace();
        }

    }

    public static void logPathSpectrum(List<String> spectrum,boolean isPatch){
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
//            System.out.println("path: " + logDir);
            return;
        }
        if (System.getProperty("jqf.ei.run_fix") != null) {
            if (System.getProperty("jqf.ei.run_fix").equals("true")) {
                return;
            }
        }
//        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
//            if (System.getProperty("jqf.ei.run_fix").equals("true")) {
//                logDir += File.separator + "FIXED";
//            }
//            else if (!isPatch) {
//                logDir += File.separator + "ORG";
//            }
//            else if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
//                String patchIndex = System.getProperty("jqf.ei.CURRENT_PATH_FOR_PATCH");
////                System.out.println("Current Patch: " + patchIndex);
//                logDir += File.separator + "PATCH" + File.separator + patchIndex.split("patched/")[1].split("/target")[0];
////                System.out.println("New Path LogDir: " + logDir);
//            }
//            else {
//                logDir += File.separator + "PATCH";
//            }
//        }
        logDir = getLogDir();

        Path inFile = null;
        String inputID = System.getProperty("jqf.ei.inputID");

//        if(System.getProperty("jqf.ei.fail_tests") != null && System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            if (System.getProperty("jqf.ei.fail_tests").contains(System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"))) {
//                logDir = logDir + "/" + "FAIL";
//            }
//            else {
//                logDir = logDir + "/" + "PASS";
//            }
//        }
//
//        if (System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//            logDir = logDir + "/" + System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD");
//        }

        if(LogResult.isDiffOutputFound()) {
            logDir = logDir + "/" + "DIFF";
            System.out.println("Diff Log: " + logDir);
        }
        else {
            logDir = logDir + "/" + "SAME";
            System.out.println("Same Log: " + logDir);
        }


        if (inputID == null) {
            if (!Files.exists(FileSystems.getDefault().getPath(logDir))) {
                try {
                    Files.createDirectories(FileSystems.getDefault().getPath(logDir));
                } catch (IOException e) {
                    System.err.println("Failed to create a dir: " + logDir);
                    e.printStackTrace();
                }
            }
            inFile = Paths.get(logDir, "PATH.log");
        } else {
            try {
                Files.createDirectories(Paths.get(logDir, inputID));
            } catch (IOException e) {
                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                e.printStackTrace();
            }
            inFile = Paths.get(logDir, inputID, "PATH.log");
        }

        if (!Files.exists(inFile)) {
            try {
                Files.createFile(inFile);
            } catch (IOException e) {
                System.err.println("Failed to create a file: " + inFile);
                e.printStackTrace();
            }
        }

        String msg="";
        for (String id:spectrum)
            msg+=id+",";

//        System.out.println("PATH: " + msg);

        try {
//            System.out.println("InFile: " + inFile.toString());
//            System.out.println(msg);
            Files.write(inFile, msg.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write path to " + inFile);
            e.printStackTrace();
        }

//        try {
//            writer = dslJson.newWriter();
//            methodWriter = dslJson2.newWriter();
//
//            if (System.getProperty("jqf.ei.run_patch").equals("true")) {
//                dslJson.serialize(writer, TraceLogger.get().methodMapP);
//                dslJson2.serialize(methodWriter, TraceLogger.get().methodNameMapP);
//            }
//            else {
//                dslJson.serialize(writer, TraceLogger.get().methodMap);
//                dslJson2.serialize(methodWriter, TraceLogger.get().methodNameMap);
//            }
//
////            writer.flush();
////            methodWriter.flush();
////            writer.reset();
////            methodWriter.reset();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //resulting buffer with JSON
//        byte[] buffer = writer.getByteBuffer();
//        byte[] methodBuffer = methodWriter.getByteBuffer();
//
//        //end of buffer
//        int size = writer.size();
////        System.out.println(writer);
//        Path methodListFile;
//
//        if (inputID == null) {
////            if (!Files.exists(FileSystems.getDefault().getPath(logDir))) {
////                try {
////                    Files.createDirectories(FileSystems.getDefault().getPath(logDir));
////                } catch (IOException e) {
////                    System.err.println("Failed to create a dir: " + logDir);
////                    e.printStackTrace();
////                }
////            }
//            methodListFile = Paths.get(logDir, "METHODMAP.json");
//        } else {
//            try {
//                Files.createDirectories(Paths.get(logDir, inputID));
//            } catch (IOException e) {
//                System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
//                e.printStackTrace();
//            }
//            inFile = Paths.get(logDir, inputID, "TRACE.json");
//            methodListFile = Paths.get(logDir, inputID, "METHODMAP.json");
//        }
//
//
//
//        if (!Files.exists(inFile)) {
//            try {
//                Files.createFile(inFile);
//                Files.createFile(methodListFile);
//            } catch (IOException e) {
//                System.err.println("Failed to create a file: " + inFile);
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            Files.write(inFile, buffer,
//                    StandardOpenOption.CREATE);
//        } catch (IOException e) {
//            System.err.println("Failed to write path to " + inFile);
//            e.printStackTrace();
//        }
//
//        try {
//            System.out.println(methodWriter);
//            Files.write(methodListFile, methodBuffer,
//                    StandardOpenOption.CREATE);
//        } catch (IOException e) {
//            System.err.println("Failed to write path to " + methodListFile);
//            e.printStackTrace();
//        }
////        writer = dslJson.newWriter();
////        methodWriter = dslJson2.newWriter();
//
//        if (System.getProperty("jqf.ei.run_patch").equals("true")) {
//            TraceLogger.get().initMethodLogP();
//        }
//        else {
//            TraceLogger.get().initMethodLog();
//        }
    }

    public static void logJson(boolean isPatch) {

//        System.out.println("LogJson");
//        && System.getProperty("kr.ac.unist.cse.jqf.IS_REPRO").equals("false")
        if ((LogResult.isDiffOutputFound() || !System.getProperty("kr.ac.unist.cse.jqf.ONLY_DIFF").equals("true")) || true) {
//            System.out.println("LogJson Diff: " + LogResult.isDiffOutputFound());
//            System.out.println("LogJson Diff: " + LogResult.outputForPatch + " " + LogResult.outputForOrg);
            String logDir = System.getProperty("jqf.ei.logDir");
            if (logDir == null) {
//            System.out.println("path: " + logDir);
                return;
            }
            if (System.getProperty("jqf.ei.run_fix") != null) {
                if (System.getProperty("jqf.ei.run_fix").equals("true")) {
                    return;
                }
            }

//            if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
//                if (System.getProperty("jqf.ei.run_fix").equals("true")) {
//                    logDir += File.separator + "FIXED";
//                }
//                else if (!isPatch) {
//                    logDir += File.separator + "ORG";
//                }
//                else if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
//                    String patchIndex = System.getProperty("jqf.ei.CURRENT_PATH_FOR_PATCH");
////                System.out.println("Current Patch: " + patchIndex);
//                    logDir += File.separator + "PATCH" + File.separator + patchIndex.split("patched/")[1].split("/target")[0];
////                System.out.println("New Path LogDir: " + logDir);
//                }
//                else {
//                    logDir += File.separator + "PATCH";
//                }
//            }
            logDir = getLogDir();

            Path inFile = null;
            String inputID = System.getProperty("jqf.ei.inputID");

//            if(System.getProperty("jqf.ei.fail_tests") != null && System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//                if (System.getProperty("jqf.ei.fail_tests").contains(System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"))) {
//                    logDir = logDir + "/" + "FAIL";
//                }
//                else {
//                    logDir = logDir + "/" + "PASS";
//                }
//            }
//
//            if (System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD") != null) {
//                logDir = logDir + "/" + System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD");
//            }

            if(LogResult.isDiffOutputFound()) {
                logDir = logDir + "/" + "DIFF";
                System.out.println("Diff Log: " + logDir);
            }
            else {
                logDir = logDir + "/" + "SAME";
                System.out.println("Same Log: " + logDir);
            }



            try {
                writer = dslJson.newWriter();
                methodWriter = dslJson2.newWriter();

                if (isPatch) {
                    dslJson.serialize(writer, TraceLogger.get().methodMapP);
                    dslJson2.serialize(methodWriter, TraceLogger.get().methodNameMapP);
                }
                else {
                    dslJson.serialize(writer, TraceLogger.get().methodMap);
                    dslJson2.serialize(methodWriter, TraceLogger.get().methodNameMap);
                }

//            writer.flush();
//            methodWriter.flush();
//            writer.reset();
//            methodWriter.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //resulting buffer with JSON
            byte[] buffer = writer.getByteBuffer();
            byte[] methodBuffer = methodWriter.getByteBuffer();

            //end of buffer
            int size = writer.size();
//        System.out.println(writer);
            Path methodListFile;

            if (inputID == null) {
//            if (!Files.exists(FileSystems.getDefault().getPath(logDir))) {
//                try {
//                    Files.createDirectories(FileSystems.getDefault().getPath(logDir));
//                } catch (IOException e) {
//                    System.err.println("Failed to create a dir: " + logDir);
//                    e.printStackTrace();
//                }
//            }
                inFile = Paths.get(logDir, "TRACE.json");
                methodListFile = Paths.get(logDir, "METHODMAP.json");
            } else {
                try {
//                System.out.println("Path for each test" + Paths.get(logDir, System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"), inputID).toString());
                    Files.createDirectories(Paths.get(logDir, inputID));
                } catch (IOException e) {
                    System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                    e.printStackTrace();
                }
                inFile = Paths.get(logDir,  inputID, "TRACE.json");
                methodListFile = Paths.get(logDir,  inputID, "METHODMAP.json");
            }



            if (!Files.exists(inFile)) {
                try {
                    Files.createFile(inFile);
                    Files.createFile(methodListFile);
                } catch (IOException e) {
                    System.err.println("Failed to create a file: " + inFile);
                    e.printStackTrace();
                }
            }
            else {
                if (isPatch) {
                    TraceLogger.get().initMethodLogP();
                }
                else {
                    TraceLogger.get().initMethodLog();
                }
                return;
            }

            try {
//            for (byte b : buffer) {
////                if(Byte.toUnsignedInt(b) == 64 || Byte.toUnsignedInt(b) == 94)
//                System.out.println("Buffer: " + (char) b);
//            }
//            System.out.println("Json: " + inFile.toString());
                Files.write(inFile, buffer,
                        StandardOpenOption.WRITE);
            } catch (IOException e) {
                System.err.println("Failed to write path to " + inFile);
                e.printStackTrace();
            }

//        try {
////            System.out.println(methodWriter);
//            Files.write(methodListFile, methodBuffer,
//                    StandardOpenOption.CREATE);
//
//        } catch (IOException e) {
//            System.err.println("Failed to write path to " + methodListFile);
//            e.printStackTrace();
//        }
//        writer = dslJson.newWriter();
//        methodWriter = dslJson2.newWriter();

            if (isPatch) {
                TraceLogger.get().initMethodLogP();
            }
            else {
                TraceLogger.get().initMethodLog();
            }
        }


    }

    public static void logJson(boolean isPatch, boolean wasDiff) {
//        System.out.println("LogJson");
        if (LogResult.isDiffOutputFound() || !System.getProperty("kr.ac.unist.cse.jqf.ONLY_DIFF").equals("true") || true) {
//            System.out.println("LogJson Diff: " + LogResult.isDiffOutputFound());
//            System.out.println("LogJson Diff: " + LogResult.outputForPatch + " " + LogResult.outputForOrg);
            String logDir = System.getProperty("jqf.ei.logDir");
            if (logDir == null) {
//            System.out.println("path: " + logDir);
                return;
            }

            if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
                if (System.getProperty("jqf.ei.run_fix").equals("true")) {
                    logDir += File.separator + "FIXED";
                }
                else if (!isPatch) {
                    logDir += File.separator + "ORG";
                }
                else if (System.getProperty("kr.ac.unist.cse.jqf.MULTI_FUZZ").equals("true")) {
                    String patchIndex = System.getProperty("jqf.ei.CURRENT_PATH_FOR_PATCH");
//                System.out.println("Current Patch: " + patchIndex);
                    logDir += File.separator + "PATCH" + File.separator + patchIndex.split("patched/")[1].split("/target")[0];
//                System.out.println("New Path LogDir: " + logDir);
                }
                else {
                    logDir += File.separator + "PATCH";
                }
            }

            Path inFile = null;
            String inputID = System.getProperty("jqf.ei.inputID");

            if(wasDiff) {
                logDir = logDir + "/" + "DIFF";
                System.out.println("Diff Log: " + logDir);
            }
            else {
                logDir = logDir + "/" + "SAME";
                System.out.println("Same Log: " + logDir);
            }


            try {
                writer = dslJson.newWriter();
                methodWriter = dslJson2.newWriter();

                if (isPatch) {
                    dslJson.serialize(writer, TraceLogger.get().methodMapP);
                    dslJson2.serialize(methodWriter, TraceLogger.get().methodNameMapP);
                }
                else {
                    dslJson.serialize(writer, TraceLogger.get().methodMap);
                    dslJson2.serialize(methodWriter, TraceLogger.get().methodNameMap);
                }

//            writer.flush();
//            methodWriter.flush();
//            writer.reset();
//            methodWriter.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //resulting buffer with JSON
            byte[] buffer = writer.getByteBuffer();
            byte[] methodBuffer = methodWriter.getByteBuffer();

            //end of buffer
            int size = writer.size();
//        System.out.println(writer);
            Path methodListFile;

            if (inputID == null) {
//            if (!Files.exists(FileSystems.getDefault().getPath(logDir))) {
//                try {
//                    Files.createDirectories(FileSystems.getDefault().getPath(logDir));
//                } catch (IOException e) {
//                    System.err.println("Failed to create a dir: " + logDir);
//                    e.printStackTrace();
//                }
//            }
                inFile = Paths.get(logDir, "TRACE.json");
                methodListFile = Paths.get(logDir, "METHODMAP.json");
            } else {
                try {
//                System.out.println("Path for each test" + Paths.get(logDir, System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"), inputID).toString());
                    Files.createDirectories(Paths.get(logDir, System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"), inputID));
                } catch (IOException e) {
                    System.err.println("Failed to create directory " + Paths.get(logDir, inputID));
                    e.printStackTrace();
                }
                inFile = Paths.get(logDir, System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"), inputID, "TRACE.json");
                methodListFile = Paths.get(logDir, System.getProperty("kr.ac.unist.cse.jqf.TEST_METHOD"), inputID, "METHODMAP.json");
            }



            if (!Files.exists(inFile)) {
                try {
                    Files.createFile(inFile);
                    Files.createFile(methodListFile);
                } catch (IOException e) {
                    System.err.println("Failed to create a file: " + inFile);
                    e.printStackTrace();
                }
            }
            else {
                if (isPatch) {
                    TraceLogger.get().initMethodLogP();
                }
                else {
                    TraceLogger.get().initMethodLog();
                }
                return;
            }

            try {
//            for (byte b : buffer) {
////                if(Byte.toUnsignedInt(b) == 64 || Byte.toUnsignedInt(b) == 94)
//                System.out.println("Buffer: " + (char) b);
//            }
//            System.out.println("Json: " + inFile.toString());
                Files.write(inFile, buffer,
                        StandardOpenOption.WRITE);
            } catch (IOException e) {
                System.err.println("Failed to write path to " + inFile);
                e.printStackTrace();
            }

//        try {
////            System.out.println(methodWriter);
//            Files.write(methodListFile, methodBuffer,
//                    StandardOpenOption.CREATE);
//
//        } catch (IOException e) {
//            System.err.println("Failed to write path to " + methodListFile);
//            e.printStackTrace();
//        }
//        writer = dslJson.newWriter();
//        methodWriter = dslJson2.newWriter();

            if (isPatch) {
                TraceLogger.get().initMethodLogP();
            }
            else {
                TraceLogger.get().initMethodLog();
            }
        }


    }

}
