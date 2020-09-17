package kr.ac.unist.cse.jqf.aspect;

import java.util.List;
import java.util.Set;
import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.Log;
import org.aspectj.lang.Signature;

public class DumpUtil {

    private static List<StackTraceElement> methods;

    public static List<StackTraceElement> getInterestingMethods() {
        return methods;
    }

    public static void setInterestingMethods(List<StackTraceElement> methods) {
        DumpUtil.methods = methods;
    }

    public static void dump(Object returnVal, Object target) {
        XStream stream = new XStream();
        String xml = stream.toXML(target);
        xml = String.format("<values>\n<return>\n%s\n</return>\n%s\n</values>", returnVal.toString(), xml);
        Log.writeToFile(xml);
    }

    public static boolean isInteresting(Signature signature) {
        signature.getDeclaringTypeName();
        signature.getName();
        System.out.println(signature);
        return true;
    }
}
