package kr.ac.unist.cse.jqf.aspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

public class DumpUtil {

    // the last item of callers is the target method
    private static List<MethodInfo> callers;
    private static final List<MethodInfo> callees = new ArrayList<>();
    private static boolean isTheTargetHit = false;
    private static boolean isTheTargetReturned = false;

    public static List<MethodInfo> getCallers() {
        return callers;
    }
    public static List<MethodInfo> getCallees() {
        return callees;
    }

    public static void setCallers(List<MethodInfo> callers) {
        DumpUtil.callers = callers;
    }

    // dump at an exit point
    public static void dumpAtExit(Object returnVal, JoinPoint target) {
        XStream stream = new XStream();
        String xml = null;
        if(target.getTarget()==null)
            xml = stream.toXML(target.getStaticPart());
        else
            xml = stream.toXML(target.getTarget());
        Signature signature = target.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(),signature.getName());
        if(returnVal!=null)
                xml = String.format("<values>\n<return>\n%s\n</return>\n%s\n</values>", stream.toXML(returnVal), xml);
        Log.writeToFile(xml,target.getSignature().getName()+"Exit"+".xml");
    }

    // dump at an entry point
    public static void dumpAtEntry(JoinPoint target) {
        Object[] args = target.getArgs();
        XStream stream = new XStream();
        String xml = null;
        if(target.getTarget()==null)
            xml = stream.toXML(target.getStaticPart());
        else
            xml = stream.toXML(target.getTarget());
        if(args != null) {
            String argsXml = stream.toXML(args);
            xml = String.format("<values>\n<args>\n%s\n</args>\n%s\n</values>", argsXml, xml);
        }
        Log.writeToFile(xml,target.getSignature().getName()+"Entry"+".xml");
    }

    public static boolean isInteresting(JoinPoint jp) {
        Signature signature = jp.getSignature();
        if (callers != null) {
            for (MethodInfo method : callers) {
                if (method.equals(new MethodInfo(signature.getDeclaringTypeName(), signature.getName())))
                    return true;
            }
        }
        if (callees != null) {
            for (MethodInfo method : callees) {
                if (method.equals(new MethodInfo(signature.getDeclaringTypeName(), signature.getName())))
                    return true;
            }
        }
        return false;
    }

    public static boolean isTargetFunction(JoinPoint jp){
        if(callers==null) return false;
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(),signature.getName());
        MethodInfo targetInfo = callers.get(0);
        if(m.equals(targetInfo))
            return  true;
        return false;
    }

    // returns true if jp is a callee of the target function
    public static boolean addCallee(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(),signature.getName());
        if(callees.contains(m)) return true;
        if(callees.size() >= 1) return false;
        callees.add(m);
        return true;
    }

    public static boolean isTheTargetHit() {
        return DumpUtil.isTheTargetHit;
    }

    public static void setTargetHit(boolean val) {
        DumpUtil.isTheTargetHit = val;
    }
    public static void setTargetReturned(boolean b){ isTheTargetReturned=b; }
    public static boolean isTheTargetReturned() {
        return DumpUtil.isTheTargetReturned;
    }
}
