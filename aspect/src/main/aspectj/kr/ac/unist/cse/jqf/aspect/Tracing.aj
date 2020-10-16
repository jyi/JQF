package kr.ac.unist.cse.jqf.aspect;

import org.aspectj.weaver.Dump;

public aspect Tracing {
    private pointcut methodPC () :
            execution(* *(..));

        after () returning (Object o): methodPC() {
            if (DumpUtil.isTargetCallee(thisJoinPoint)) {
                DumpUtil.insideTargetMethod(true);
            }
            if (DumpUtil.isInterestingExit(thisJoinPoint)) {
                // System.out.println("< target method is called");
                // System.out.println(thisJoinPoint.getSignature().getDeclaringTypeName());
                // System.out.println("Method " + thisJoinPoint.toString() + " is called ");
                DumpUtil.dumpAtExit(o, thisJoinPoint);
                if (!DumpUtil.runOrgVerAgain) {
                    DumpUtil.addExitMethod(thisJoinPoint);
                }
                if (DumpUtil.isTargetMethod(thisJoinPoint))
                    DumpUtil.insideTargetMethod(false);
            }
        }

        before (): methodPC() {
            if (DumpUtil.isInterestingEntry(thisJoinPoint)) {
                DumpUtil.insideTargetMethod(false);
                DumpUtil.addTargetCallee(thisJoinPoint);
                DumpUtil.dumpAtEntry(thisJoinPoint);
                if (!DumpUtil.runOrgVerAgain) {
                    DumpUtil.addEnterMethod(thisJoinPoint);
                }
            }
        }
}