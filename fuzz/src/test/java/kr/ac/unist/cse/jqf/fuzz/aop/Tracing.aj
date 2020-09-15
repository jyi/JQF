package kr.ac.unist.cse.jqf.fuzz.aop;
import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.fuzz.aop.StateHandler;
import edu.berkeley.cs.jqf.fuzz.util.TargetCoverage;
import kr.ac.unist.cse.jqf.Log;

public aspect Tracing {
    private pointcut methodPC () :
        execution(* *(..));

    after () returning (Object o): methodPC() {
            if (thisJoinPoint.toString().contains("inverseCumulativeProbability")) {
                System.out.println("< target method is called");
                StateHandler.dump(o, thisJoinPoint.getTarget());
            }
    }

}