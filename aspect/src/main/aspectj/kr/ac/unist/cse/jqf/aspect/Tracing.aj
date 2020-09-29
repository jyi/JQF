package kr.ac.unist.cse.jqf.aspect;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.aspect.DumpUtil;
import kr.ac.unist.cse.jqf.Log;

public aspect Tracing {
    private pointcut methodPC () :
            execution(* *(..));

        after () returning (Object o): methodPC() {
            if (DumpUtil.isInteresting(thisJoinPoint) &&
                !thisJoinPoint.getTarget().getClass().toString().contains("JQF_")) {
                 System.out.println("< target method is called");
                 System.out.println(thisJoinPoint.getTarget().getClass());
                 System.out.println("Method " + thisJoinPoint.toString() + " is called ");
                 DumpUtil.dumpAtExit(o, thisJoinPoint);
            }
            // TODO: check whether the target method exits.
            // If so, call DumpUtil.setTargetHit(false).
            if(DumpUtil.isTargetFunction(thisJoinPoint)&&DumpUtil.isTheTargetHit())
                    DumpUtil.setTargetHit(false);
        }

        before (): methodPC() {
            if (DumpUtil.isTheTargetHit()) {
                if(DumpUtil.addCallee(thisJoinPoint))
                    DumpUtil.dumpAtEntry(thisJoinPoint);
            }
        }
}