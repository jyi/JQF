package kr.ac.unist.cse.jqf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class Log {

    public static File logFile;
    public static boolean verbose = true;

    public static File measureTimeFile;

    public static class LogResult {
        private static String outputForOrg = null;
        private static String outputForPatch = null;

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

        public static boolean isDiffOutputFound() {
            assert outputForOrg != null;
            assert outputForPatch != null;

//            System.out.println("isDiffOutputFound is called");
            System.out.println("outputForOrg: " + outputForOrg);
            System.out.println("outputForPatch: " + outputForPatch);

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
                } else
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
        if (logDir == null) {
            return null;
        }

        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
            if (Log.runBuggyVersion) {
                logDir += File.separator + "ORG";
            } else {
                logDir += File.separator + "PATCH";
            }
        }

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
            try {
                Files.createFile(outFile);
            } catch (IOException e) {
                System.err.println("Failed to create a file: " + outFile);
                e.printStackTrace();
            }


            try {
                Files.write(outFile, xml.getBytes(),
                        StandardOpenOption.WRITE);
            } catch (IOException e) {
                System.err.println("Failed to write output due to IOException");
            }
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
            System.out.println("Output: " + outFile.toString());
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
        LogResult.addOutput(msg);
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
            System.out.println("out: " + msg);
            return;
        }

        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
            if (Log.runBuggyVersion) {
                logDir += File.separator + "ORG";
            } else {
                logDir += File.separator + "PATCH";
            }
        }

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
//            Files.write(outFile, msg.getBytes(),
//                    StandardOpenOption.APPEND);
            Files.write(outFile, msg.getBytes(),
                    StandardOpenOption.CREATE);
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
        System.out.println("in: " + msg);

        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
            if (Log.runBuggyVersion) {
                logDir += File.separator + "ORG";
            } else {
                logDir += File.separator + "PATCH";
            }
        }

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
            // Files.write(inFile, msg.getBytes(),
            //                    StandardOpenOption.APPEND);
            Files.write(inFile, msg.getBytes(),
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.err.println("Failed to write output to " + inFile);
            e.printStackTrace();
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
            System.out.println("branch: " + logDir);
            return;
        }

        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
            if (!isPatch) {
                logDir += File.separator + "ORG";
            } else {
                logDir += File.separator + "PATCH";
            }
        }

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
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.err.println("Failed to write branch to " + inFile);
            e.printStackTrace();
        }

    }

    public static void logPathSpectrum(List<String> spectrum,boolean isPatch){
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
            System.out.println("path: " + logDir);
            return;
        }

        if (Boolean.getBoolean("jqf.ei.run_two_versions")) {
            if (!isPatch) {
                logDir += File.separator + "ORG";
            } else {
                logDir += File.separator + "PATCH";
            }
        }

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

        try {
            Files.write(inFile, msg.getBytes(),
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.err.println("Failed to write path to " + inFile);
            e.printStackTrace();
        }

    }

}
