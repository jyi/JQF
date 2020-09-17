package kr.ac.unist.cse.jqf.aspect;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.Log;

public class StateHandler {
    public static void dump(Object returnVal, Object target) {
        XStream stream = new XStream();
        String xml = stream.toXML(target);
        xml = String.format("<values>\n<return>\n%s\n</return>\n%s\n</values>", returnVal.toString(), xml);
        Log.writeToFile(xml);
    }
}
