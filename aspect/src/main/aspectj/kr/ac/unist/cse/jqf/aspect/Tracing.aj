package kr.ac.unist.cse.jqf.aspect;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public aspect Tracing {
    private pointcut methodPC () :
            execution(* *(..));

        after () returning (Object o): methodPC() {
            if (DumpUtil.isCallee(thisJoinPoint)) {
                DumpUtil.insideTargetMethod(true);
            }
            if (DumpUtil.isInterestingExit(thisJoinPoint)) {
                DumpUtil.dumpAtExit(o, thisJoinPoint);
                if (!DumpUtil.runOrgVerAgain) {
                    DumpUtil.addExitMethod(thisJoinPoint);
                }
                if (DumpUtil.isTargetMethod(thisJoinPoint))
                    DumpUtil.insideTargetMethod(false);
            }
            DumpUtil.dumpAtExit(o, thisJoinPoint);
        }

        before (): methodPC() {
            if (DumpUtil.isInterestingEntry(thisJoinPoint)) {
                DumpUtil.insideTargetMethod(false);
                // we consider only the first callee
                if (DumpUtil.getCalleesOfTaregetMethod().size() <= 0) {
                    DumpUtil.addCallee(thisJoinPoint);
                }
                DumpUtil.dumpAtEntry(thisJoinPoint);
                if (!DumpUtil.runOrgVerAgain) {
                    DumpUtil.addEnterMethod(thisJoinPoint);
                }
            }
        }
}