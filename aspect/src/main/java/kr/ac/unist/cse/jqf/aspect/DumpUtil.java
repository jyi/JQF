package kr.ac.unist.cse.jqf.aspect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.weaver.Dump;

public class DumpUtil {

    // the last item of callers is the target method
    private static List<MethodInfo> callerChain;
    public static Set<MethodInfo> exitMethods = new HashSet<>();
    public static Set<MethodInfo> enterMethods = new HashSet<>();
    private static final List<MethodInfo> calleesOfTaregetMethod = new ArrayList<>();
    private static boolean isTheTargetHit = false;
    private static boolean isInsideTargetMethod = false;
    public static boolean runOrgVerAgain = false;
    public static boolean duringDumping = false;

    public static List<MethodInfo> getCallerChain() {
        return callerChain;
    }

    public static List<MethodInfo> getCalleesOfTaregetMethod() {
        return calleesOfTaregetMethod;
    }

    public static void setCallerChainToTargetMethod(List<MethodInfo> callerChain) {
        DumpUtil.callerChain = callerChain;
    }

    public static void addExitMethod(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());
        exitMethods.add(m);
    }

    public static void addEnterMethod(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());
        enterMethods.add(m);
    }

    // dump at an exit point
    public static void dumpAtExit(Object returnVal, JoinPoint target) {
        try {
            duringDumping = true;
            XStream stream = new XStream();
            String xml = null;
            if (target.getTarget() == null)
                xml = stream.toXML(target.getStaticPart());
            else
                xml = stream.toXML(target.getTarget());
            if (returnVal != null)
                xml = String.format("<values>\n<return>\n%s\n</return>\n%s\n</values>", stream.toXML(returnVal), xml);
            Log.writeToFile(xml, target.getSignature().getName() + "Exit" + ".xml");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            duringDumping = false;
        }
    }

    // dump at an entry point
    public static void dumpAtEntry(JoinPoint jp) {
        try {
            duringDumping = true;
            Object[] args = jp.getArgs();
            XStream stream = new XStream();
            String xml = null;
            if (jp.getTarget() == null) {
                xml = stream.toXML(jp.getStaticPart());
            } else {
                Object target = jp.getTarget();
                xml = stream.toXML(target);
            }
            if (args != null) {
                String argsXml = stream.toXML(args);
                xml = String.format("<values>\n<args>\n%s\n</args>\n%s\n</values>", argsXml, xml);
            }
            Log.writeToFile(xml, jp.getSignature().getName() + "Entry" + ".xml");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            duringDumping = false;
        }
    }

    public static boolean isInterestingExit(JoinPoint jp) {
        if (DumpUtil.duringDumping) return false;

        if (DumpUtil.runOrgVerAgain) {
            Signature signature = jp.getSignature();
            MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());
            return DumpUtil.exitMethods != null && DumpUtil.exitMethods.contains(m);
        }

        if (!isTheTargetHit()) return false;

        Signature signature = jp.getSignature();
        if (signature.getDeclaringTypeName().contains("JQF_")) return false;

        if (callerChain != null) {
            for (MethodInfo method : callerChain) {
                if (method.equals(new MethodInfo(signature.getDeclaringTypeName(), signature.getName())))
                    return true;
            }
        }

        if (calleesOfTaregetMethod != null) {
            for (MethodInfo method : calleesOfTaregetMethod) {
                if (method.equals(new MethodInfo(signature.getDeclaringTypeName(), signature.getName())))
                    return true;
            }
        }

        return false;
    }

    public static boolean isInterestingEntry(JoinPoint jp) {
        if (DumpUtil.duringDumping) return false;

        if (DumpUtil.runOrgVerAgain) {
            Signature signature = jp.getSignature();
            MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());
            return DumpUtil.enterMethods != null && DumpUtil.enterMethods.contains(m);
        }

        if (!DumpUtil.isTheTargetHit()) return false;

        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());

        if (!DumpUtil.insideTargetMethod()) return false;
        if (DumpUtil.callerChain != null && DumpUtil.callerChain.get(0) != null
                && DumpUtil.callerChain.get(0).equals(m)) return false;

        return true;
    }

    public static boolean isTargetCallee(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());

        return calleesOfTaregetMethod != null && calleesOfTaregetMethod.contains(m);
    }

    public static boolean isTargetMethod(JoinPoint jp) {
        if (callerChain == null) return false;
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());
        MethodInfo targetInfo = callerChain.get(0);
        return m.equals(targetInfo);
    }

    public static void addTargetCallee(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(), signature.getName());

        if (!calleesOfTaregetMethod.contains(m))
            calleesOfTaregetMethod.add(m);
    }

    public static boolean isTheTargetHit() {
        return DumpUtil.isTheTargetHit;
    }

    public static void setTargetHit(boolean val) {
        DumpUtil.isTheTargetHit = val;
    }

    public static void insideTargetMethod(boolean b) {
        isInsideTargetMethod = b;
    }

    public static boolean insideTargetMethod() {
        return DumpUtil.isInsideTargetMethod;
    }
}
