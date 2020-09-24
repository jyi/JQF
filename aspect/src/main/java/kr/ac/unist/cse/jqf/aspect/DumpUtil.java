package kr.ac.unist.cse.jqf.aspect;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

public class DumpUtil {

    private static List<MethodInfo> callers;

    public static List<MethodInfo> getInterestingMethods() {
        return callers;
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
        return false;
    }
}
