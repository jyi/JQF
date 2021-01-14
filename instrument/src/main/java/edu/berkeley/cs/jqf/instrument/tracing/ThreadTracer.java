/*
 * Copyright (c) 2017-2018 The Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.cs.jqf.instrument.tracing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import edu.berkeley.cs.jqf.instrument.tracing.events.*;
import janala.instrument.Method;
import janala.logger.inst.*;
//import sun.security.util.ArrayUtil;
import com.ibm.wala.cfg.cdg.BasicBlockDistance;

import org.apache.commons.io.FileUtils;

/**
 * This class is responsible for tracing for an instruction stream
 * generated by a single thread in the application.
 *
 * <p>A ThreadTracer instance processes low-level bytecode instructions
 * instrumented by JQF/Janala and converts them into appropriate
 * {@link TraceEvent} instances, which are then emitted to be processed
 * by the guidance-provided callback.</p>
 *
 * @author Rohan Padhye
 */
public class ThreadTracer {
    public static boolean evaluatingPatch;
    protected final Thread tracee;
    protected final String entryPointClass;
    protected final String entryPointMethod;
    protected final Consumer<TraceEvent> callback;
    private final Deque<IVisitor> handlers = new ArrayDeque<>();
    private HashMap<Target, HashMap<Target, Integer>> targetMap = new HashMap<>();

    // Values set by GETVALUE_* instructions inserted by Janala
    private final Values values = new Values();

    // Whether to instrument generators
    private final boolean traceGenerators;

    // Whether to check if caller and callee have the same method name/desc when tracing
    // Set this to TRUE if instrumenting JDK classes, in order to skip JVM classloading activity
    private static final boolean MATCH_CALLEE_NAMES = Boolean.getBoolean("jqf.tracing.MATCH_CALLEE_NAMES");

    private final Target[] targets;

    protected final boolean verbose = true;

    /**
     * Creates a new tracer that will process instructions executed by an application
     * thread.
     *
     * @param tracee the thread to trace
     * @param entryPoint the outermost method call to trace (formatted as fq-class#method)
     * @param callback the callback to invoke whenever a trace event is emitted
     */
    protected ThreadTracer(Thread tracee, String entryPoint, Consumer<TraceEvent> callback) {
        this.tracee = tracee;
        if (entryPoint != null) {
            int separator = entryPoint.indexOf('#');
            if (separator <= 0 || separator == entryPoint.length() - 1) {
                throw new IllegalArgumentException("Invalid entry point: " + entryPoint);
            }
            this.entryPointClass = entryPoint.substring(0, separator).replace('.', '/');
            this.entryPointMethod = entryPoint.substring(separator + 1);
        } else {
            this.entryPointClass = null;
            this.entryPointMethod = null;
        }
        this.traceGenerators = Boolean.getBoolean("jqf.traceGenerators");
        this.callback = callback;
        this.handlers.push(new BaseHandler());
        if (System.getProperty("jqf.ei.targets") != null) {
            targets = Target.getTargetArray(System.getProperty("jqf.ei.targets"));
        } else {
            targets = null;
        }
    }

    /**
     * Spawns a thread tracer for the given thread.
     *
     * @param thread the thread to trace
     * @return a tracer for the given thread
     */
    protected static ThreadTracer spawn(Thread thread) {
        String entryPoint = SingleSnoop.entryPoints.get(thread);
        Consumer<TraceEvent> callback = SingleSnoop.callbackGenerator.apply(thread);
        ThreadTracer t =
                new ThreadTracer(thread, entryPoint, callback);
        return t;
    }

    protected RuntimeException callBackException = null;

    /**
     * Emits a trace event to be consumed by the registered callback.
     *
     * @param e the event to emit
     */
    protected final void emit(TraceEvent e) {
        try {
            callback.accept(e);
        } catch (RuntimeException ex) {
            callBackException = ex;
        }
    }

    /**
     * Handles tracing of a single bytecode instruction.
     *
     * @param ins the instruction to process
     */
    protected final void consume(Instruction ins) {
        boolean isTargetHit = false;
        if (!(ins instanceof SPECIAL || ins instanceof METHOD_BEGIN || ins instanceof INVOKEMETHOD_END)) {
            if (this.targets != null) {
                for (Target target : this.targets) {
                    if (target.getLinenum() == ins.mid) {
                        String fileName = getFileName(ins);
                        if (fileName != null) {
                            if (target.getFilename().equals(fileName)) {
                                emit(new TargetHitEvent(ins.iid, null, ins.mid, target.getFilename()));
                                isTargetHit = true;
                            }
                        }
                    }
                }
            }
        }

        if (Boolean.getBoolean("jqf.ei.run_patch") && !isTargetHit && isConditionalBranch(ins)) {
            String fileName = getFileNameQuick(ins);
            if (fileName != null && this.targets != null) {
                for (Target target : this.targets) {
                    int distToTarget = getDistToTarget(fileName, ins.mid, target);
                    emit(new DistanceUpdateEvent(ins.iid, null, ins.mid, target, distToTarget));
                }
            }
        }

        // Apply the visitor at the top of the stack
        ins.visit(handlers.peek());
        if (callBackException != null) {
            RuntimeException e = callBackException;
            callBackException = null;
            throw e;
        }
    }

    private boolean isConditionalBranch(Instruction ins) {
        return ins instanceof ConditionalBranch;
    }

    private String getFileNameQuick(Instruction ins) {
        return ins.fileName;
    }

    private String getFileName(Instruction ins) {
        String fileName = getFileNameQuick(ins);
        if (fileName != null) return fileName;

        boolean singleSnoopFound = false;
        StackTraceElement[] traces = new Exception().getStackTrace();
        if (traces != null) {
//            int SingleSnoopIdx = 0;
//            for (StackTraceElement te : traces) {
//                if (te.getClassName().equals("edu.berkeley.cs.jqf.instrument.tracing.SingleSnoop")) {
//                    singleSnoopFound = true;
//                    break;
//                }
//                SingleSnoopIdx++;
//            }
            singleSnoopFound = true;
            int singleSnoopIdx = 4;
            // the next element behind SingleSnoop is the target candidate
            if (singleSnoopFound) {
                try {
                    fileName = traces[singleSnoopIdx + 1].getClassName().replace(".", File.separator) + ".java";
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
        }
        return fileName;
    }

    private int getDistToTarget(String currentFile, int lineNumber, Target target) {
        // if distance is not known, return Integer.MAX_VALUE
        //System.out.println("get dist to target: " + currentFile + ":" + lineNumber + " <-> " + target.toString());
        if(!targetMap.containsKey(target)) {
            try {
                String out = System.getProperty("jqf.ei.outputDirectory");
                Path outDir = FileSystems.getDefault().getPath((new File(out)).getAbsolutePath(),
                         "wala_out").normalize();
                //System.out.println("outdir: " + outDir.toString());
                if (outDir.toFile().exists()) {
                    FileUtils.cleanDirectory(outDir.toFile());
                } else {
                    FileUtils.forceMkdir(outDir.toFile());
                }
                String[] cps = System.getProperty("jqf.ei.CLASSPATH_FOR_PATCH").split("[:;]");
                String classPath = "";
                for(String cp: cps) {
                    if((new File(cp)).exists()) {
                        classPath += (new File(cp)).getCanonicalPath() + ":";
                    } else {
                        System.out.println("####path does not exist: " + cp);
                    }
                }
                classPath = classPath.substring(0, classPath.length() - 1);
                //System.out.println("classPath!!!! " + classPath);
                //Path cp = FileSystems.getDefault().getPath(Paths.get(System.getProperty("user.dir")).getParent().toString(),
                //        "src", "test", "resources", "patches", "Patch180", "Time4p", "target", "classes");
                Path outFile = FileSystems.getDefault().getPath(outDir.toString(), "output.csv");
                getDistUsingWALA(classPath, target.toString(), outFile.toString());
                targetMap.put(target, parseCSVFile(outFile.toString()));
            } catch (IOException e) {
                infoLog("Failed to make out dir");
                return Integer.MAX_VALUE;
            }
        }
        Target current = new Target(currentFile, lineNumber);
        HashMap<Target, Integer> fileLineDistMap = targetMap.get(target);
        if(fileLineDistMap == null || fileLineDistMap.isEmpty()) return Integer.MAX_VALUE;
        if (fileLineDistMap.containsKey(current)) {
            //System.out.println("get dist to target: " + current.toString() + " <-> " + target.toString() + " = " + fileLineDistMap.get(current));
            return fileLineDistMap.get(current);
        }
        return Integer.MAX_VALUE;
    }
    private void getDistUsingWALA(String classPath, String target, String outDir) {
        BasicBlockDistance bbd = new BasicBlockDistance(outDir);
        bbd.runWithClassPathFromFile(classPath, target);
    }
    private HashMap<Target, Integer> parseCSVFile(String filename) {
        HashMap<Target, Integer> fileLineDistMap = new HashMap<>();
        FileReader f = null;
        try {
            f = new FileReader(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BufferedReader br = new BufferedReader(f);
        try {
            String row = br.readLine();
            //System.out.println(row);
            while(true) {
                row = br.readLine();
                if(row == null) return fileLineDistMap;
                String[] temp = row.split(",");
                //System.out.println(temp[0] + " + " + temp[1] + " + " + temp[2]);
                Target t = new Target(temp[0], Integer.parseInt(temp[1]));
                if(fileLineDistMap.containsKey(t)) {
                    if (fileLineDistMap.get(t) > Integer.parseInt(temp[2])) {
                        //System.out.println("update " + t.toString() + " to " + temp[2] + " from " + fileLineDistMap.get(t));
                        fileLineDistMap.put(t, Integer.parseInt(temp[2]));
                    }
                } else {
                    fileLineDistMap.put(t, Integer.parseInt(temp[2]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isReturnOrMethodThrow(Instruction inst) {
        return  inst instanceof ARETURN ||
                inst instanceof LRETURN ||
                inst instanceof DRETURN ||
                inst instanceof FRETURN ||
                inst instanceof IRETURN ||
                inst instanceof RETURN  ||
                inst instanceof METHOD_THROW;
    }


    private static boolean isInvoke(Instruction inst) {
        return  inst instanceof InvokeInstruction;
    }

    private static boolean isIfJmp(Instruction inst) {
        return  inst instanceof ConditionalBranch;
    }


    private static class Values {
        private boolean booleanValue;
        private byte byteValue;
        private char charValue;
        private double doubleValue;
        private float floatValue;
        private int intValue;
        private long longValue;
        private Object objectValue;
        private short shortValue;
    }
    
    private static boolean sameNameDesc(MemberRef m1, MemberRef m2) {
        return m1 != null && m2 != null &&
                m1.getName().equals(m2.getName()) &&
                m1.getDesc().equals(m2.getDesc());
    }

    protected void infoLog(String str, Object... args) {
        if (verbose) {
            String line = String.format(str, args);
            System.err.println(line);
        }
    }

    class BaseHandler extends ControlFlowInstructionVisitor {
        @Override
        public void visitMETHOD_BEGIN(METHOD_BEGIN begin) {
            // Try to match the top-level call with the entry point
            String clazz = begin.getOwner();
            String method = begin.getName();
            if ((clazz.equals(entryPointClass) && method.equals(entryPointMethod)) ||
                    (traceGenerators && clazz.endsWith("Generator") && method.equals("generate")) ) {
                emit(new CallEvent(0, null, 0, begin));
                handlers.push(new TraceEventGeneratingHandler(begin, 0));
            } else {
                // Ignore all top-level calls that are not the entry point
                handlers.push(new MatchingNullHandler());
            }
        }
    }

    class TraceEventGeneratingHandler extends ControlFlowInstructionVisitor {

        private final int depth;
        private final MemberRef method;
        TraceEventGeneratingHandler(METHOD_BEGIN begin, int depth) {
            this.depth = depth;
            this.method = begin;
            //logger.log(tabs() + begin);
        }

        private String tabs() {
            StringBuffer sb = new StringBuffer(depth);
            for (int i = 0; i < depth; i++) {
                sb.append("  ");
            }
            return sb.toString();
        }

        private MemberRef invokeTarget = null;
        private boolean invokingSuperOrThis = false;

        @Override
        public void visitMETHOD_BEGIN(METHOD_BEGIN begin) {
            if ((MATCH_CALLEE_NAMES == false && begin.name.equals("<clinit>") == false) || sameNameDesc(begin, this.invokeTarget)) {
                // Trace continues with callee
                int invokerIid = invokeTarget != null ? ((Instruction) invokeTarget).iid : -1;
                int invokerMid = invokeTarget != null ? ((Instruction) invokeTarget).mid : -1;
                emit(new CallEvent(invokerIid, this.method, invokerMid, begin));
                handlers.push(new TraceEventGeneratingHandler(begin, depth+1));
            } else {
                // Class loading or static initializer
                handlers.push(new MatchingNullHandler());
            }

            super.visitMETHOD_BEGIN(begin);
        }

        @Override
        public void visitINVOKEMETHOD_EXCEPTION(INVOKEMETHOD_EXCEPTION ins) {
            if (this.invokeTarget == null) {
                throw new RuntimeException("Unexpected INVOKEMETHOD_EXCEPTION");
            } else {
                // Unset the invocation target for the rest of the instruction stream
                this.invokeTarget = null;
                // Handle end of super() or this() call
                if (invokingSuperOrThis) {
                    while (true) { // will break when outer caller of <init> found
                        emit(new ReturnEvent(-1, this.method, -1));
                        handlers.pop();
                        IVisitor handler = handlers.peek();
                        // We should not reach the BaseHandler without finding
                        // the TraceEventGeneratingHandler who called the outer <init>().
                        assert (handler instanceof TraceEventGeneratingHandler);
                        TraceEventGeneratingHandler traceEventGeneratingHandler = (TraceEventGeneratingHandler) handler;
                        if (traceEventGeneratingHandler.invokingSuperOrThis) {
                            // Go down the stack further
                            continue;
                        } else {
                            // Found caller of new()
                            assert(traceEventGeneratingHandler.invokeTarget.getName().startsWith("<init>"));
                            // Let this handler (now top-of-stack) process the instruction
                            ins.visit(traceEventGeneratingHandler);
                            break;
                        }
                    }
                }
            }

            super.visitINVOKEMETHOD_EXCEPTION(ins);
        }

        @Override
        public void visitINVOKEMETHOD_END(INVOKEMETHOD_END ins) {
            if (this.invokeTarget == null) {
                throw new RuntimeException("Unexpected INVOKEMETHOD_END");
            } else {
                // Unset the invocation target for the rest of the instruction stream
                this.invokeTarget = null;
                // Handle end of super() or this() call
                if (invokingSuperOrThis) {
                    // For normal end, simply unset the flag
                    this.invokingSuperOrThis = false;
                }
            }

            super.visitINVOKEMETHOD_END(ins);
        }

        @Override
        public void visitSPECIAL(SPECIAL special) {
            // Handle marker that says calling super() or this()
            if (special.i == SPECIAL.CALLING_SUPER_OR_THIS) {
                this.invokingSuperOrThis = true;
            }
            return; // Do not process SPECIAL instructions further
        }

        @Override
        public void visitInvokeInstruction(InvokeInstruction ins) {
            // Remember invocation target until METHOD_BEGIN or INVOKEMETHOD_END/INVOKEMETHOD_EXCEPTION
            this.invokeTarget = ins;

            super.visitInvokeInstruction(ins);
        }

        @Override
        public void visitGETVALUE_int(GETVALUE_int gv) {
            values.intValue = gv.v;

            super.visitGETVALUE_int(gv);
        }

        @Override
        public void visitGETVALUE_boolean(GETVALUE_boolean gv) {
            values.booleanValue = gv.v;

            super.visitGETVALUE_boolean(gv);
        }

        @Override
        public void visitConditionalBranch(Instruction ins) {
            int iid = ins.iid;
            int lineNum = ins.mid;
            // The branch taken-or-not would have been set by a previous
            // GETVALUE instruction
            boolean taken = values.booleanValue;
            emit(new BranchEvent(iid, this.method, lineNum, taken ? 1 : 0));

            super.visitConditionalBranch(ins);
        }

        @Override
        public void visitTABLESWITCH(TABLESWITCH tableSwitch) {
            int iid = tableSwitch.iid;
            int lineNum = tableSwitch.mid;
            int value = values.intValue;
            int numCases = tableSwitch.labels.length;
            // Compute arm index or else default
            int arm = -1;
            if (value >= 0 && value < numCases) {
                arm = value;
            }
            // Emit a branch instruction corresponding to the arm
            emit(new BranchEvent(iid, this.method, lineNum, arm));

            super.visitTABLESWITCH(tableSwitch);
        }

        @Override
        public void visitLOOKUPSWITCH(LOOKUPSWITCH lookupSwitch) {
            int iid = lookupSwitch.iid;
            int lineNum = lookupSwitch.mid;
            int value = values.intValue;
            int[] cases = lookupSwitch.keys;
            // Compute arm index or else default
            int arm = -1;
            for (int i = 0; i < cases.length; i++) {
                if (value == cases[i]) {
                    arm = i;
                    break;
                }
            }
            // Emit a branch instruction corresponding to the arm
            emit(new BranchEvent(iid, this.method, lineNum, arm));

            super.visitLOOKUPSWITCH(lookupSwitch);
        }

        @Override
        public void visitHEAPLOAD(HEAPLOAD heapload) {
            int iid = heapload.iid;
            int lineNum = heapload.mid;
            int objectId = heapload.objectId;
            String field = heapload.field;
            // Log the object access (unless it was a NPE)
            if (objectId != 0) {
                emit(new ReadEvent(iid, this.method, lineNum, objectId, field));
            }

            super.visitHEAPLOAD(heapload);
        }

        @Override
        public void visitNEW(NEW newInst) {
            int iid = newInst.iid;
            int lineNum = newInst.mid;
            emit(new AllocEvent(iid, this.method, lineNum, 1));

            super.visitNEW(newInst);
        }

        @Override
        public void visitNEWARRAY(NEWARRAY newArray) {
            int iid = newArray.iid;
            int lineNum = newArray.mid;
            int size = values.intValue;
            emit(new AllocEvent(iid, this.method, lineNum, size));

            super.visitNEWARRAY(newArray);
        }

        @Override
        public void visitReturnOrMethodThrow(Instruction ins) {
            emit(new ReturnEvent(ins.iid, this.method, ins.mid));
            handlers.pop();

            super.visitReturnOrMethodThrow(ins);
        }

    }

    class MatchingNullHandler extends ControlFlowInstructionVisitor {

        @Override
        public void visitMETHOD_BEGIN(METHOD_BEGIN begin) {
            handlers.push(new MatchingNullHandler());
        }

        @Override
        public void visitReturnOrMethodThrow(Instruction ins) {
            handlers.pop();
        }
    }
}
