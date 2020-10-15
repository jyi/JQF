package kr.ac.unist.cse.jqf.aspect;

import com.thoughtworks.xstream.XStream;
import kr.ac.unist.cse.jqf.aspect.DumpUtil;
import kr.ac.unist.cse.jqf.Log;

public aspect Tracing {
    private pointcut methodPC () :
            execution(* *(..));

        after () returning (Object o): methodPC() {
            if ( DumpUtil.isInteresting(thisJoinPoint) &&
                !thisJoinPoint.getSignature().getDeclaringTypeName().contains("JQF_")) {
                // System.out.println("< target method is called");
                // System.out.println(thisJoinPoint.getSignature().getDeclaringTypeName());
                // System.out.println("Method " + thisJoinPoint.toString() + " is called ");
                DumpUtil.dumpAtExit(o, thisJoinPoint);
                if (DumpUtil.isTargetFunction(thisJoinPoint))
                    DumpUtil.setTargetReturned(true);
            }
            // TODO: check whether the target method exits.
            // If so, call DumpUtil.setTargetHit(false).

        }

        before (): methodPC() {
            if (DumpUtil.isTargetFunction(thisJoinPoint))
                DumpUtil.setTargetReturned(false);

            if (!DumpUtil.isTheTargetReturned() && DumpUtil.isTheTargetHit()) {
                if(DumpUtil.addCallee(thisJoinPoint))
                    DumpUtil.dumpAtEntry(thisJoinPoint);
            }
        }
}