//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.hub.se.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class CFGTarget implements Serializable {
    private static final long serialVersionUID = 1L;
    private String method;
    private int sourceLineNumber;
    private String toStringResult;

    public CFGTarget(String method, int sourceLineNumber) {
        this.method = method;
        this.sourceLineNumber = sourceLineNumber;
        this.toStringResult = method + ":" + sourceLineNumber;
    }

    public String getMethod() {
        return this.method;
    }

    public int getSourceLineNumber() {
        return this.sourceLineNumber;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        } else {
            if (anObject instanceof CFGTarget) {
                CFGTarget anotherCFGTarget = (CFGTarget)anObject;
                if (!this.method.equals(anotherCFGTarget.method)) {
                    return false;
                }

                if (this.sourceLineNumber != anotherCFGTarget.sourceLineNumber) {
                    return false;
                }
            }

            return true;
        }
    }

    public int hashCode() {
        int result = 31 + this.method.hashCode();
        result = 31 * result + this.sourceLineNumber;
        return result;
    }

    public String toString() {
        return this.toStringResult;
    }

    public static List<String> parseDistanceTargetArgument(String args) {
        ArrayList targets = new ArrayList();

        try {
            String[] var2 = args.split(",");
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String target = var2[var4];
                target.split(":");
                targets.add(target);
            }

            return targets;
        } catch (PatternSyntaxException var6) {
            throw new RuntimeException("Wrong target definition: " + args + "\nComma-separated list of target specification: method:line");
        }
    }

    public static CFGTarget createCFGTargetFromString(String target) {
        String[] splitted = target.split(":");
        return new CFGTarget(splitted[0], Integer.parseInt(splitted[1]));
    }
}
