package kr.ac.unist.cse.jqf.aspect;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

public class DumpUtil {

    private static List<MethodInfo> callers;
    private static List<MethodInfo> callees = new ArrayList<>();
    private static boolean isTheTargetHit = false;
    private static boolean isCallChainReady = false;
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

    public static void dump(Object returnVal, JoinPoint target) {
        XStream stream = new XStream();
        String xml = stream.toXML(target.getTarget());
        if(returnVal!=null)
            xml = String.format("<values>\n<return>\n%s\n</return>\n%s\n</values>", returnVal.toString(), xml);
        Log.writeToFile(xml,target.getSignature().getName()+".xml");
    }

    public static boolean isInteresting(JoinPoint jp) {
        Signature signature = jp.getSignature();
        if(callers != null) {
            for (MethodInfo method : callers) {
                if (method.equals(new MethodInfo(signature.getDeclaringTypeName(), signature.getName())))
                    return true;
            }
        }
        if(callees !=null){
            for (MethodInfo method : callees) {
                if (method.equals(new MethodInfo(signature.getDeclaringTypeName(), signature.getName())))
                    return true;
            }
        }
        return false;
    }

    public static void addCallee(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodInfo m = new MethodInfo(signature.getDeclaringTypeName(),signature.getName());
        setTargetHit(false);
        if(callees.contains(m)) return;
        callees.add(m);

    }

    public static boolean isTheTargetHit() {
        return DumpUtil.isTheTargetHit;
    }

    public static void setTargetHit(boolean val) {
        DumpUtil.isTheTargetHit = val;
    }

    public static boolean isCallChainReady() {
        return DumpUtil.isCallChainReady;
    }

    public static boolean isTheTargetReturned() {
        return DumpUtil.isTheTargetReturned;
    }
}
