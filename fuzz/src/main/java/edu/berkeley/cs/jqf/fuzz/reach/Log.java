package edu.berkeley.cs.jqf.fuzz.reach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Log {

  public static boolean runBuggyVersion = false;

  public Log() {
    String logDir = System.getProperty("jqf.ei.logDir");
    if (logDir == null) {
      return;
    }

    String inputID = System.getProperty("jqf.ei.inputID");

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

  public void logOut(String msg) {
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

  public void logOut(int val) {
    logOut(Integer.toString(val));
  }

  public void logOut(double val) {
    logOut(Double.toString(val));
  }

  public void logOut(byte val) {
    logOut(Byte.toString(val));
  }

  public void logOut(short val) {
    logOut(Short.toString(val));
  }

  public void logOut(long val) {
    logOut(Long.toString(val));
  }

  public void logOut(float val) {
    logOut(Float.toString(val));
  }

  public void logOut(boolean val) {
    if (val) logOut(0);
    else logOut(1);
  }

  public void logOut(char val) {
    logOut(Character.getNumericValue(val));
  }

  public void logOutArray(double[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Double[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(int[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Integer[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(byte[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Byte[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(short[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Short[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(long[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Long[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(float[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Float[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(boolean[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Boolean[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(char[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOutArray(Character[] arr) {
    if (arr.length <= 0) logOut("Warning: empty array");
    else if (arr.length == 1) logOut(arr[0]);
    else logOut(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logOut(Object first, Object ... rest) {
    if (first instanceof Double) {
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
      if (first instanceof double[]) logOutArray((double[]) first);
      else if (first instanceof Double[]) logOutArray((Double[]) first);
      else if (first instanceof int[]) logOutArray((int[]) first);
      else if (first instanceof Integer[]) logOutArray((Integer[]) first);
      else if (first instanceof byte[]) logOutArray((byte[]) first);
      else if (first instanceof Byte[]) logOutArray((Byte[]) first);
      else if (first instanceof short[]) logOutArray((short[]) first);
      else if (first instanceof Short[]) logOutArray((Short[]) first);
      else if (first instanceof long[]) logOutArray((long[]) first);
      else if (first instanceof Long[]) logOutArray((Long[]) first);
      else if (first instanceof float[]) logOutArray((float[]) first);
      else if (first instanceof Float[]) logOutArray((Float[]) first);
      else if (first instanceof boolean[]) logOutArray((boolean[]) first);
      else if (first instanceof Boolean[]) logOutArray((Boolean[]) first);
      else if (first instanceof char[]) logOutArray((char[]) first);
      else if (first instanceof Character[]) logOutArray((Character[]) first);
      else throw new RuntimeException("Unexpected array type: " + first.getClass());
    } else {
      throw new RuntimeException("Unexpected type: " + first.getClass());
    }

    for (Object o: rest) {
      logOut(";");
      logOut(o);
    }
  }

  public void logIn(String msg) {
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

  public void logIn(int val) {
    logIn(Integer.toString(val));
  }

  public void logIn(double val) {
    logIn(Double.toString(val));
  }

  public void logIn(byte val) {
    logIn(Byte.toString(val));
  }

  public void logIn(short val) {
    logIn(Short.toString(val));
  }

  public void logIn(long val) {
    logIn(Long.toString(val));
  }

  public void logIn(float val) {
    logIn(Float.toString(val));
  }

  public void logIn(boolean val) {
    if (val) logIn(0);
    else logIn(1);
  }

  public void logIn(char val) {
    logIn(Character.getNumericValue(val));
  }

  public void logInArray(double[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Double[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(int[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Integer[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(byte[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Byte[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(short[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Short[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(long[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Long[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(float[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Float[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(boolean[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Boolean[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(char[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logInArray(Character[] arr) {
    if (arr.length <= 0) logIn("Warning: empty array");
    else if (arr.length == 1) logIn(arr[0]);
    else logIn(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
  }

  public void logIn(Object first, Object ... rest) {
    if (first instanceof Double) {
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
      if (first instanceof double[]) logInArray((double[]) first);
      else if (first instanceof Double[]) logInArray((Double[]) first);
      else if (first instanceof int[]) logInArray((int[]) first);
      else if (first instanceof Integer[]) logInArray((Integer[]) first);
      else if (first instanceof byte[]) logInArray((byte[]) first);
      else if (first instanceof Byte[]) logInArray((Byte[]) first);
      else if (first instanceof short[]) logInArray((short[]) first);
      else if (first instanceof Short[]) logInArray((Short[]) first);
      else if (first instanceof long[]) logInArray((long[]) first);
      else if (first instanceof Long[]) logInArray((Long[]) first);
      else if (first instanceof float[]) logInArray((float[]) first);
      else if (first instanceof Float[]) logInArray((Float[]) first);
      else if (first instanceof boolean[]) logInArray((boolean[]) first);
      else if (first instanceof Boolean[]) logInArray((Boolean[]) first);
      else if (first instanceof char[]) logInArray((char[]) first);
      else if (first instanceof Character[]) logInArray((Character[]) first);
      else throw new RuntimeException("Unexpected array type: " + first.getClass());
    } else {
      throw new RuntimeException("Unexpected type: " + first.getClass());
    }

    for (Object o: rest) {
      logIn(";");
      logIn(o);
    }
  }
}
