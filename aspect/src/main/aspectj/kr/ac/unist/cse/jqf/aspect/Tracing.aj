package kr.ac.unist.cse.jqf.aspect;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.aspect.StateHandler;
import kr.ac.unist.cse.jqf.Log;

public aspect Tracing {
    private pointcut methodPC () :
            execution(* *(..)) && within(org.apache.commons.math3.distribution..*);

        after () returning (Object o): methodPC() {
                if (!thisJoinPoint.getTarget().getClass().toString().contains("JQF_") &&
                    DumpUtil.isInteresting(thisJoinPoint.getSignature())) {
                    System.out.println("< target method is called");
                    System.out.println(thisJoinPoint.getTarget().getClass());
                    System.out.println("Method "+thisJoinPoint.toString()+ " is called ");

                    DumpUtil.dump(o, thisJoinPoint.getTarget());
                }
        }
}