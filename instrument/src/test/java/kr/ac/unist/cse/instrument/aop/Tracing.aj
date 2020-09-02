package kr.ac.unist.cse.jqf.fuzz.aop;

public aspect Tracing {
    private pointcut mainMethod () :
        execution(* *(..));

    after () : mainMethod() {
        System.out.println("< After mainMethod()" + thisJoinPoint);
    }
}