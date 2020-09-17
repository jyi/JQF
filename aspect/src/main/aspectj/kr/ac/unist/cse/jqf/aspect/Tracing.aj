package kr.ac.unist.cse.jqf.aspect;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.aspect.StateHandler;
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