package edu.berkeley.cs.jqf.fuzz.reach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Log {

  public Log() {
    String logDir = System.getProperty("jqf.ei.logDir");
    if (logDir == null) {
      return;
    }

    Path outFile = Paths.get(logDir, "OUT.log");
    try {
      Files.deleteIfExists(outFile);
    } catch (IOException e) {
      System.err.println("Failed to delete " + outFile);
    }

    try {
      Files.createFile(outFile);
    } catch (IOException e) {
      System.err.println("Failed to create " + outFile);
    }
  }

  public void logOut(String msg) {
    String logDir = System.getProperty("jqf.ei.logDir");
    if (logDir == null) return;

    try {
      Files.write(Paths.get(logDir, "OUT.log"), msg.getBytes(),
              StandardOpenOption.APPEND);
    } catch (IOException e) {
      System.err.println("Failed to write output due to IOException");
    }
  }

  public void logOut(int out) {
    logOut(Integer.toString(out));
  }

  public void logOut(double out) {
    logOut(Double.toString(out));
  }

  public void logOut(byte out) {
    logOut(Byte.toString(out));
  }

  public void logOut(short out) {
    logOut(Short.toString(out));
  }

  public void logOut(long out) {
    logOut(Long.toString(out));
  }

  public void logOut(float out) {
    logOut(Float.toString(out));
  }

  public void logOut(boolean out) {
    logOut(Boolean.toString(out));
  }

  public void logOut(char out) {
    logOut(Character.toString(out));
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
    } else {
      throw new RuntimeException("Unexpected type: " + first.getClass());
    }

    for (Object o: rest) {
      logOut(";");
      logOut(o);
    }
  }
}
