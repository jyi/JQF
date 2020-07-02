package edu.berkeley.cs.jqf.fuzz.reach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Log {
  public static void logOut(String msg) {
    try {
      Files.write(Paths.get("OUT.log"), msg.getBytes());
    } catch (IOException e) {
      System.err.println("Failed to write output due to IOException");
    }
  }

  public static void logOut(int out) {
    logOut(Integer.toString(out));
  }

  public static void logOut(double out) {
    logOut(Double.toString(out));
  }

  public static void logOut(byte out) {
    logOut(Byte.toString(out));
  }

  public static void logOut(short out) {
    logOut(Short.toString(out));
  }

  public static void logOut(long out) {
    logOut(Long.toString(out));
  }

  public static void logOut(float out) {
    logOut(Float.toString(out));
  }

  public static void logOut(boolean out) {
    logOut(Boolean.toString(out));
  }

  public static void logOut(char out) {
    logOut(Character.toString(out));
  }
}
