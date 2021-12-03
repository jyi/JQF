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

import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import janala.logger.AbstractLogger;
import janala.logger.inst.*;

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

    public class FileInfo {
        public String id = new String();
        public String name = new String();

        public FileInfo() {
        }

        public FileInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }






    private static final TraceLogger singleton = new TraceLogger();

    private final ThreadLocal<ThreadTracer> tracer
            = ThreadLocal.withInitial(() -> ThreadTracer.spawn(Thread.currentThread()));



    public ArrayList<methodCallInfo> methods = new ArrayList<>();
    public int numofMethods = 0;
    public int numofFiles = 0;
    public Map<String, Object> methodMap = new HashMap<String, Object>();
    public Map<String, String> fileMap = new HashMap<String, String>();
    public Map<String, String> methodNameMap = new HashMap<String, String>();

    public ArrayList<String> methodStack = new ArrayList<>();
    public ArrayList<String> keyWordStack = new ArrayList<>();
    public ArrayList<FileInfo> fileList = new ArrayList<>();

    public ArrayList<methodCallInfo> methodsP = new ArrayList<>();
    public int numofMethodsP = 0;
    public int numofFilesP = 0;
    public Map<String, Object> methodMapP = new HashMap<String, Object>();
    public Map<String, String> fileMapP = new HashMap<String, String>();
    public Map<String, String> methodNameMapP = new HashMap<String, String>();

    public ArrayList<String> methodStackP = new ArrayList<>();
    public ArrayList<String> keyWordStackP = new ArrayList<>();
    public ArrayList<FileInfo> fileListP = new ArrayList<>();


    public boolean firstCall = false;

    public int stackSize = 0;



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
//        System.out.println("KeyStack: " + keyWordStack);
        boolean fileExist = false;

        if(!System.getProperty("jqf.ei.run_patch").equals("true")) {
            try {
                if (instruction.fileName.contains("\\.")) {
//                    System.out.println("Contain: " + instruction.fileName);
                    if (!fileMap.containsKey(instruction.fileName.split("\\.")[0])) {
                        fileMap.put(instruction.fileName.split("\\.")[0], "f" + Integer.toString(numofFiles));
//                        System.out.println("NewFile: " + instruction.fileName.split("\\.")[0] + "f" + Integer.toString(numofFiles));
                        numofFiles++;
                    }
                }
                else if (instruction instanceof INVOKESTATIC || instruction instanceof INVOKESPECIAL || instruction instanceof METHOD_BEGIN || instruction instanceof INVOKEVIRTUAL || instruction instanceof INVOKEINTERFACE) {
                    if (!fileMap.containsKey(((MemberRef) instruction).getOwner())) {
                        fileMap.put(((MemberRef) instruction).getOwner(), "f" + Integer.toString(numofFiles));
//                        System.out.println("NewOwner: " + ((MemberRef) instruction).getOwner() + "f" + Integer.toString(numofFiles));
                        numofFiles++;
                    }
                }
                else if (!instruction.owner.equals("")) {
                    if (!fileMap.containsKey(instruction.owner)) {
                        fileMap.put(instruction.owner,  "f" + Integer.toString(numofFiles));
                        numofFiles++;
                    }
                }
                else {
//                    System.out.println("File:" + instruction.fileName);
                    fileMap.put(instruction.fileName, "n");
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (instruction instanceof INVOKESTATIC || instruction instanceof INVOKESPECIAL || instruction instanceof METHOD_BEGIN || instruction instanceof INVOKEVIRTUAL || instruction instanceof INVOKEINTERFACE) {
                stackSize++;
                firstCall = true;
                boolean exist = false;
//            methodStack.add(((INVOKESTATIC) instruction).name);
                for (methodCallInfo m : methods) {
                    try {
                        if (m.methodName.equals(((MemberRef) instruction).getName()) && m.fileName.equals(((MemberRef) instruction).getOwner())) {

//                            System.out.println("ExistName: " + ((MemberRef) instruction).getName());
                            exist = true;
                            m.incCallCount();
                            MethodLog newMethod;

                            if (keyWordStack.size() == 0) {
                                newMethod = new MethodLog(m.methodID + "_" + Integer.toString(m.callCount), m.methodName, "Entry", m.callCount);
                            }
                            else {
                                ((MethodLog)methodMap.get(keyWordStack.get(keyWordStack.size()-1))).addExe(m.methodID + "_" + Integer.toString(m.callCount));
                                newMethod = new MethodLog(m.methodID + "_" + Integer.toString(m.callCount), m.methodName, keyWordStack.get(keyWordStack.size()-1), m.callCount);
                            }

                            keyWordStack.add(m.methodID + "_" + Integer.toString(m.callCount));
                            if (!((MemberRef) instruction).getOwner().equals("")) {
                                newMethod.addExe(((MemberRef) instruction).getName() + fileMap.get(((MemberRef) instruction).getOwner()) + ":" + instruction.mid);
                            }
                            methodMap.put(m.methodID + "_" + Integer.toString(m.callCount), newMethod);
                            break;
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Exception in Exist");
                        e.printStackTrace();
                    }

                }
                if (!exist) {
                    try {
                        String newKeyword = "m_id" + Integer.toString(numofMethods);
                        methodNameMap.put(newKeyword, ((MemberRef) instruction).getOwner() + ":" + ((MemberRef) instruction).getName());
                        MethodLog newMethod;
                        if (keyWordStack.size() == 0) {
                            newMethod = new MethodLog(newKeyword + "_" + Integer.toString(0), ((MemberRef) instruction).getName(), "Entry", 0);
                        }
                        else {
                            ((MethodLog)methodMap.get(keyWordStack.get(keyWordStack.size()-1))).addExe(newKeyword + "_" + Integer.toString(0));
                            newMethod = new MethodLog(newKeyword + "_" + Integer.toString(0), ((MemberRef) instruction).getName(), keyWordStack.get(keyWordStack.size()-1), 0);
                        }

                        keyWordStack.add(newKeyword + "_" + Integer.toString(0));
                        methods.add(new methodCallInfo(newKeyword, ((MemberRef) instruction).getOwner(), ((MemberRef) instruction).getName()));
                        if (!((MemberRef) instruction).getOwner().equals("")) {
                            newMethod.addExe(((MemberRef) instruction).getName() + fileMap.get(((MemberRef) instruction).getOwner()) + ":" + instruction.mid);
                        }
                        methodMap.put(newKeyword + "_0", newMethod);

                        numofMethods++;
                    }
                    catch (Exception e) {
                        System.out.println("Exception in Not Exist");
                        e.printStackTrace();
                    }

                }
            }
//        instruction instanceof RETURN || instruction instanceof ARETURN ||
//                instruction instanceof DRETURN || instruction instanceof FRETURN ||
//                instruction instanceof IRETURN || instruction instanceof LRETURN
            else if (instruction instanceof INVOKEMETHOD_END ||         instruction instanceof RETURN || instruction instanceof ARETURN ||
                    instruction instanceof DRETURN || instruction instanceof FRETURN ||
                    instruction instanceof IRETURN || instruction instanceof LRETURN) {
                try {

                    if (!instruction.fileName.equals("")) {
                        ((MethodLog)methodMap.get(keyWordStack.get(keyWordStack.size()-1))).addExe(fileMap.get(instruction.fileName.split("\\.")[0]) + ":" + instruction.mid);
                    }
//                int lastIndex = methodStack.size()-1;
//                methodStack.remove(lastIndex);
                    int lastIndex = keyWordStack.size()-1;
                    keyWordStack.remove(lastIndex);
                }
                catch (Exception e) {
                    System.out.println("Exception in End");
                    e.printStackTrace();
                }
                stackSize--;
            }
            else if (keyWordStack.size() > 0) {
                if (!instruction.fileName.equals("")) {
                    ((MethodLog)methodMap.get(keyWordStack.get(keyWordStack.size()-1))).addExe(fileMap.get(instruction.fileName.split("\\.")[0]) + ":" + instruction.mid);
                }
            }
            else {
                System.out.println("Out of Method");
            }
//            System.out.println(instruction.getClass().getName());
        }
        else {
            try {
                if (instruction.fileName.contains("\\.")) {
                    if (!fileMapP.containsKey(instruction.fileName.split("\\.")[0])) {
                        fileMapP.put(instruction.fileName.split("\\.")[0], "f" + Integer.toString(numofFilesP));
                        numofFilesP++;
                    }
                }
                else if (instruction instanceof INVOKESTATIC || instruction instanceof INVOKESPECIAL || instruction instanceof METHOD_BEGIN || instruction instanceof INVOKEVIRTUAL || instruction instanceof INVOKEINTERFACE) {
                    if (!fileMapP.containsKey(((MemberRef) instruction).getOwner())) {
                        fileMapP.put(((MemberRef) instruction).getOwner(), "f" + Integer.toString(numofFilesP));
                        numofFilesP++;
                    }
                }
                else if (!instruction.owner.equals("")) {
                    if (!fileMapP.containsKey(instruction.owner)) {
                        fileMap.put(instruction.owner,  "f" + Integer.toString(numofFilesP));
                        numofFilesP++;
                    }
                }
                else {
                    fileMapP.put(instruction.fileName, "n");
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (instruction instanceof INVOKESTATIC || instruction instanceof INVOKESPECIAL || instruction instanceof METHOD_BEGIN || instruction instanceof INVOKEVIRTUAL || instruction instanceof INVOKEINTERFACE) {
                stackSize++;
                firstCall = true;
                boolean exist = false;
//            methodStack.add(((INVOKESTATIC) instruction).name);
                for (methodCallInfo m : methodsP) {
                    try {
                        if (m.methodName.equals(((MemberRef) instruction).getName()) && m.fileName.equals(((MemberRef) instruction).getOwner())) {
                            exist = true;
                            m.incCallCount();
                            MethodLog newMethod;

                            if (keyWordStackP.size() == 0) {
                                newMethod = new MethodLog(m.methodID + "_" + Integer.toString(m.callCount), m.methodName, "Entry", m.callCount);
                            }
                            else {
                                ((MethodLog)methodMapP.get(keyWordStackP.get(keyWordStackP.size()-1))).addExe(m.methodID + "_" + Integer.toString(m.callCount));
                                newMethod = new MethodLog(m.methodID + "_" + Integer.toString(m.callCount), m.methodName, keyWordStackP.get(keyWordStackP.size()-1), m.callCount);
                            }

                            keyWordStackP.add(m.methodID + "_" + Integer.toString(m.callCount));
                            if (!((MemberRef) instruction).getOwner().equals("")) {
                                newMethod.addExe(((MemberRef) instruction).getName() + fileMapP.get(((MemberRef) instruction).getOwner()) + ":" + instruction.mid);
                            }
                            methodMapP.put(m.methodID + "_" + Integer.toString(m.callCount), newMethod);
                            break;
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Exception in Exist");
                        e.printStackTrace();
                    }

                }
                if (!exist) {
                    try {
                        String newKeyword = "m_id" + Integer.toString(numofMethodsP);
                        methodNameMapP.put(newKeyword, ((MemberRef) instruction).getOwner() + ":"  + ((MemberRef) instruction).getName());
                        MethodLog newMethod;
                        if (keyWordStackP.size() == 0) {
                            newMethod = new MethodLog(newKeyword + "_" + Integer.toString(0), ((MemberRef) instruction).getName(), "Entry", 0);
                        }
                        else {
                            ((MethodLog)methodMapP.get(keyWordStackP.get(keyWordStackP.size()-1))).addExe(newKeyword + "_" + Integer.toString(0));
                            newMethod = new MethodLog(newKeyword + "_" + Integer.toString(0), ((MemberRef) instruction).getName(), keyWordStackP.get(keyWordStackP.size()-1), 0);
                        }


                        keyWordStackP.add(newKeyword + "_" + Integer.toString(0));
                        methodsP.add(new methodCallInfo(newKeyword, ((MemberRef) instruction).getOwner(), ((MemberRef) instruction).getName()));
                        if (!((MemberRef) instruction).getOwner().equals("")) {
                            newMethod.addExe(((MemberRef) instruction).getName() + fileMapP.get(((MemberRef) instruction).getOwner()) + ":" + instruction.mid);
                        }
                        methodMapP.put(newKeyword + "_0", newMethod);

                        numofMethodsP++;
                    }
                    catch (Exception e) {
                        System.out.println("Exception in Not Exist");
                        e.printStackTrace();
                    }

                }
            }
//        instruction instanceof RETURN || instruction instanceof ARETURN ||
//                instruction instanceof DRETURN || instruction instanceof FRETURN ||
//                instruction instanceof IRETURN || instruction instanceof LRETURN
            else if (instruction instanceof INVOKEMETHOD_END ||         instruction instanceof RETURN || instruction instanceof ARETURN ||
                    instruction instanceof DRETURN || instruction instanceof FRETURN ||
                    instruction instanceof IRETURN || instruction instanceof LRETURN) {
                try {

                    if (!instruction.fileName.equals("")) {
                        ((MethodLog)methodMapP.get(keyWordStackP.get(keyWordStackP.size()-1))).addExe(fileMapP.get(instruction.fileName.split("\\.")[0]) + ":" + instruction.mid);
                    }
//                int lastIndex = methodStack.size()-1;
//                methodStack.remove(lastIndex);
                    int lastIndex = keyWordStackP.size()-1;
                    keyWordStackP.remove(lastIndex);
                }
                catch (Exception e) {
                    System.out.println("Exception in End");
                    e.printStackTrace();
                }
                stackSize--;
            }
            else if (keyWordStackP.size() > 0) {
                if (!instruction.fileName.equals("")) {
                    ((MethodLog)methodMapP.get(keyWordStackP.get(keyWordStackP.size()-1))).addExe(fileMapP.get(instruction.fileName.split("\\.")[0]) + ":" + instruction.mid);
                }
            }
            else {
                System.out.println("Out of Method");
            }
        }


//        System.out.println("StackSize: " + Integer.toString(stackSize));
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

    public void initMethodLog () {
        this.stackSize = 0;
        this.keyWordStack = new ArrayList<>();
        this.firstCall = false;
        this.methodMap = new HashMap<String, Object>();
        this.numofMethods = 0;
        this.methods = new ArrayList<methodCallInfo>();
        this.methodNameMap = new HashMap<String, String>();
    }
    public void initMethodLogP () {
        this.stackSize = 0;
        this.keyWordStackP = new ArrayList<>();
        this.firstCall = false;
        this.methodMapP = new HashMap<String, Object>();
        this.numofMethodsP = 0;
        this.methodsP = new ArrayList<methodCallInfo>();
        this.methodNameMapP = new HashMap<String, String>();
    }

}

