package kr.ac.unist.cse.jqf.fuzz.aop;

public aspect Tracing {
    private pointcut mainMethod () :
        execution(public static void main(String[]));

    before () : mainMethod() {
        System.out.println("> Before mainMethod()" + thisJoinPoint);
    }

    after () : mainMethod() {
        System.out.println("< After mainMethod()" + thisJoinPoint);
    }
}