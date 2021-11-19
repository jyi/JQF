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

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import janala.logger.AbstractLogger;
import janala.logger.inst.Instruction;
import janala.logger.inst.METHOD_BEGIN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class which manages per-thread tracers.
 *
 * This class is used both to log instrumented instructions
 * via {@link SingleSnoop}, as well as to provide programmatic
 * access to emit {@link TraceEvent}s.
 *
 * @author Rohan Padhye
 */
public class TraceLogger extends AbstractLogger {

    public class methodCallInfo {
        public methodCallInfo () {
        }

        public methodCallInfo (String methodID, String fileName, String methodName) {
            this.methodID = methodID;
            this.fileName = fileName;
            this.methodName = methodName;
        }

        public String methodID = new String();
        public String fileName = new String();
        public String methodName = new String();
        public int callCount = 0;

        public void incCallCount() {
            this.callCount++;
        }
    }

    public class MethodLog {
        public MethodLog () {
        }

        public MethodLog (String id, String caller, int Nth) {
            this.id = id;
            this.caller = caller;
            this.Nth = Nth;
        }

        public String id = new String();
        public int Nth = 0;
        public String caller = new String();
        public ArrayList<String> exe = new ArrayList<>();

        public void addExe(String newLine) {
            this.exe.add(newLine);
        }

        public ArrayList<String> getExe () {
            return this.exe;
        }
    }

    private static final TraceLogger singleton = new TraceLogger();

    private final ThreadLocal<ThreadTracer> tracer
            = ThreadLocal.withInitial(() -> ThreadTracer.spawn(Thread.currentThread()));

    public DslJson<Object> dslJson = new DslJson<Object>();
    //writer should be reused. For per thread reuse use ThreadLocal pattern
    public JsonWriter writer = dslJson.newWriter();

    public ArrayList<methodCallInfo> methods = new ArrayList<>();
    public int numofMethods = 0;
    public Map<String, Object> methodMap = new HashMap<String, Object>();

    public ArrayList<String> methodStack = new ArrayList<>();
    public ArrayList<String > keyWordStack = new ArrayList<>();

    public boolean firstCall = false;

    private TraceLogger() {

        // Singleton: Prevent outside construction
    }

    /** Returns a handle to the singleton instance. */
    public static TraceLogger get() {
        return singleton;
    }

    /** Logs an instrumented bytecode instruction for the current thread. */
    @Override
    protected void log(Instruction instruction) {
        if (instruction instanceof METHOD_BEGIN) {
            firstCall = true;
            boolean exist = false;
            methodStack.add(((METHOD_BEGIN) instruction).name);
            System.out.println("MethodName: " + ((METHOD_BEGIN) instruction).name);
//            for (methodCallInfo m : methods) {
//                if (m.methodName.equals(((METHOD_BEGIN) instruction).name) && m.fileName.equals(((METHOD_BEGIN) instruction).fileName)) {
//                    System.out.println("ExistName: " + ((METHOD_BEGIN) instruction).name);
//                    exist = true;
//                    m.incCallCount();
//                    System.out.println("CurKey: " + keyWordStack.get(keyWordStack.size()-1));
//                    MethodLog newMethod = new MethodLog(m.methodID + "_" + Integer.toString(m.callCount), keyWordStack.get(keyWordStack.size()-1), m.callCount);
//                    keyWordStack.add(m.methodID + "_" + Integer.toString(m.callCount));
//                    newMethod.addExe(instruction.fileName + ":" + instruction.mid);
////                    methodMap.put(m.methodID + "_" + Integer.toString(m.callCount), newMethod);
//                }
//                break;
//            }
//            if (!exist) {
//                String newKeyword = "m_id" + Integer.toString(numofMethods);
//                MethodLog newMethod;
//                if (keyWordStack.size() == 0) {
//                    newMethod = new MethodLog(newKeyword + "_" + Integer.toString(0), "Entry", 0);
//                }
//                else {
//                    newMethod = new MethodLog(newKeyword + "_" + Integer.toString(0), keyWordStack.get(keyWordStack.size()-1), 0);
//                }
//
//                System.out.println(newKeyword + "_" + Integer.toString(0));
//
//                keyWordStack.add(newKeyword + "_" + Integer.toString(0));
//                methods.add(new methodCallInfo(newKeyword, ((METHOD_BEGIN) instruction).fileName, ((METHOD_BEGIN) instruction).name));
//                newMethod.addExe(instruction.fileName + ":" + instruction.mid);
//                methodMap.put(newKeyword + "_" + Integer.toString(methods.get(-1).callCount), newMethod);
//
//                numofMethods++;
//            }
        }
//        else if (instruction instanceof INVOKEMETHOD_END || instruction instanceof METHOD_THROW) {
//            System.out.println("KeyWordSize: " + Integer.toString(keyWordStack.size()-1));
//            ((MethodLog)methodMap.get(keyWordStack.get(keyWordStack.size()-1))).addExe(instruction.fileName + ":" + instruction.mid);
//            methodStack.remove(methodStack.size()-1);
//            keyWordStack.remove(keyWordStack.size()-1);
//        }
//        else if (firstCall) {
//            ((MethodLog)methodMap.get(keyWordStack.get(keyWordStack.size()-1))).addExe(instruction.fileName + ":" + instruction.mid);
//        }
//        if (firstCall) {
//            System.out.println("CurrentID : " + keyWordStack.get(keyWordStack.size() - 1));
//        }
//        System.out.println("Patched Method: " + System.getProperty("jqf.ei.PATCHED_METHOD"));
        tracer.get().consume(instruction);
    }

    /** Emits a trace event for the current thread. */
    public void emit(TraceEvent event) {
        tracer.get().emit(event);
    }

}
