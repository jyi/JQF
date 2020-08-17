package kr.ac.unist.cse.jqf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class Log {
    public static boolean logOutIfCalled = false;
    public static boolean runBuggyVersion = false;

    protected static void logOut(String msg) {
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
            System.out.println("out: " + msg);
            return;
        }

        Path outFile;
        String inputID = System.getProperty("jqf.ei.inputID");
        if (inputID == null) {
            outFile = Paths.get(logDir, "OUT.log");
        } else {
            outFile = Paths.get(logDir, inputID, "OUT.log");
        }

        try {
            Files.write(outFile, msg.getBytes(),
                    StandardOpenOption.APPEND);
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
        if (first instanceof String) {
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

    public static <T> void logOutIf(boolean cond, T[] actual) {
        if (logOutIfCalled) logOut(";");
        if (Log.runBuggyVersion) {
            if (cond) {
                logOut(actual);
            } else {
                logOut("IGNORE_OUTPUT: " + actual);
            }
        } else {
            logOut(actual);
        }
        logOutIfCalled = true;
    }

    public static <T> void logOutIf(boolean cond, T[] actual, T[] expected) {
        if (logOutIfCalled) logOut(";");
        if (Log.runBuggyVersion) {
            if (cond) {
                logOut(actual);
            } else {
                logOut(expected);
            }
        } else {
            logOut(actual);
        }
        logOutIfCalled = true;
    }

    protected static void logIn(String msg) {
        String logDir = System.getProperty("jqf.ei.logDir");
        if (logDir == null) {
            System.out.println("in: " + msg);
            return;
        }

        Path inFile;
        String inputID = System.getProperty("jqf.ei.inputID");
        if (inputID == null) {
            inFile = Paths.get(logDir, "IN.log");
        } else {
            inFile = Paths.get(logDir, inputID, "IN.log");
        }

        try {
            Files.write(inFile, msg.getBytes(),
                    StandardOpenOption.APPEND);
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
}
