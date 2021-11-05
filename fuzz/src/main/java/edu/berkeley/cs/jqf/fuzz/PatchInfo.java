package edu.berkeley.cs.jqf.fuzz;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PatchInfo {
    public String patchPath = null;
    public ClassLoader patchLoader = null;
    public int diffFound = 0;

    public PatchInfo() {

    }

    public PatchInfo(String patchPath, ClassLoader patchLoader, int diffFound) {
        this.patchPath = patchPath;
        this.patchLoader = patchLoader;
        this.diffFound = diffFound;
    }

    public void setDiffFound(int diffFound) {
        this.diffFound = diffFound;
    }

    public int getDiffFound() {
        return diffFound;
    }

    public void increaseDiffFound() {
        this.diffFound++;
    }

    public void setPatchLoader(ClassLoader patchLoader) {
        this.patchLoader = patchLoader;
    }

    public ClassLoader getPatchLoader() {
        return patchLoader;
    }

    public void setPatchPath(String patchPath) {
        this.patchPath = patchPath;
    }

    public String getPatchPath() {
        return patchPath;
    }
}
