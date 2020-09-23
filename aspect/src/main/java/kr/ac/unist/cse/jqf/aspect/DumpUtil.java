package kr.ac.unist.cse.jqf.aspect;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

public class DumpUtil {

    private static List<MethodInfo> methods;

    public static List<MethodInfo> getInterestingMethods() {
        return methods;
    }

    public static void setInterestingMethods(List<MethodInfo> methods) {
        DumpUtil.methods = methods;
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
        if(methods != null) {
            for (MethodInfo method : methods) {
                if (method.equals(new MethodInfo(signature.getDeclaringTypeName(), signature.getName())))
                    return true;
            }
        }
            return false;
    }
}
