package edu.berkeley.cs.jqf.fuzz.reach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Log {

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
    if (logDir == null) return;

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

  public void logIn(String msg) {
    String logDir = System.getProperty("jqf.ei.logDir");
    if (logDir == null) return;

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

  public void logIn(int out) {
    logIn(Integer.toString(out));
  }

  public void logIn(double out) {
    logIn(Double.toString(out));
  }

  public void logIn(byte out) {
    logIn(Byte.toString(out));
  }

  public void logIn(short out) {
    logIn(Short.toString(out));
  }

  public void logIn(long out) {
    logIn(Long.toString(out));
  }

  public void logIn(float out) {
    logIn(Float.toString(out));
  }

  public void logIn(boolean out) {
    logIn(Boolean.toString(out));
  }

  public void logIn(char out) {
    logIn(Character.toString(out));
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
    } else {
      throw new RuntimeException("Unexpected type: " + first.getClass());
    }

    for (Object o: rest) {
      logIn(";");
      logIn(o);
    }
  }
}
