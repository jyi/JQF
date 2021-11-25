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
import edu.berkeley.cs.jqf.instrument.util.DoublyLinkedList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;


@SuppressWarnings("unused") // Dynamically loaded
public final class SingleSnoop {


    static DoublyLinkedList<Thread> threadsToUnblock = new DoublyLinkedList<>();

    private static ThreadLocal<Boolean> block = new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
        String threadName = Thread.currentThread().getName();
            if (threadName.startsWith("__JWIG_TRACER__")) {
                return true; // Always block snooping on the tracing thread to prevent cycles
            } else if (threadsToUnblock.synchronizedRemove(Thread.currentThread())){
                return false; // Snoop on threads that were added to the queue explicitly
            } else {
                return true; // Block all other threads (e.g. JVM cleanup threads)
            }
        }
    };

    public static final Map<Thread, String> entryPoints = new WeakHashMap<>();


    /** A supplier of callbacks for each thread (does nothing by default). */
    static Function<Thread, Consumer<TraceEvent>> callbackGenerator = (t) -> (e) -> {};


    private static final TraceLogger intp = TraceLogger.get();

    private SingleSnoop() {}


    /**
     * Register a supplier of callbacks for each named thread, which will consume
     * {@link TraceEvent}s.
     *
     * @param callbackGenerator a supplier of thread-specific callbacks
     */
    public static void setCallbackGenerator(Function<Thread, Consumer<TraceEvent>> callbackGenerator) {
        SingleSnoop.callbackGenerator = callbackGenerator;
    }


    /** Start snooping for this thread, with the top-level call being
     * the <tt>entryPoint</tt>.
     *
     * @param entryPoint the top-level method, formatted as
     *                   <tt>CLASS#METHOD</tt> (e.g.
     *                   <tt>FooBar#main</tt>).
     */
    public static void startSnooping(String entryPoint) {
        System.setProperty("jqf.ei.run_patch", "false");
        // Mark entry point
        entryPoints.put(Thread.currentThread(), entryPoint);
        // XXX: Offer a dummy instruction to warm-up
        // class-loaders of the logger, in order to avoid
        // deadlocks when tracing is triggered from
        // SnoopInstructionTransformer#transform()
        intp.SPECIAL(-1);
        // Unblock snooping for current thread
        unblock();
    }

    public static void unblock() {
        block.set(false);
    }

    public static void REGISTER_THREAD(Thread thread) {
        // Mark entry point as run()
        try {
            // Get a reference to the Thread's Runnable if it exists
            Field targetField = Thread.class.getDeclaredField("target");
            targetField.setAccessible(true);
            Object target =  targetField.get(thread);
            if (target == null) {
                // If the Runnable is not provided explicitly,
                // it is likely a sub-class of Thread with an overriden run() method
                target = thread;
            }
            Method runMethod = target.getClass().getMethod("run");
            String entryPoint = runMethod.getDeclaringClass().getName() + "#run";
            entryPoints.put(thread, entryPoint);
            // Mark thread for unblocking when we snoop its first instruction
            threadsToUnblock.synchronizedAddFirst(thread);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            // Print error and keep going
            e.printStackTrace();
        }
    }

    public static void LDC(int iid, int mid, int c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(int iid, int mid, long c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(int iid, int mid, float c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(int iid, int mid, double c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(int iid, int mid, String c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(int iid, int mid, Object c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(iid, mid, c); } finally { block.set(false); }
    }

    public static void IINC(int iid, int mid, int var, int increment) {
        if (block.get()) return; else block.set(true);
        try { intp.IINC(iid, mid, var, increment); } finally { block.set(false); }
    }

    public static void MULTIANEWARRAY(int iid, int mid, String desc, int dims) {
        if (block.get()) return; else block.set(true);
        try { intp.MULTIANEWARRAY(iid, mid, desc, dims); } finally { block.set(false); }
    }

    public static void LOOKUPSWITCH(int iid, int mid, int dflt, int[] keys, int[] labels) {
        if (block.get()) return; else block.set(true);
        try { intp.LOOKUPSWITCH(iid, mid, dflt, keys, labels); } finally { block.set(false); }
    }

    public static void TABLESWITCH(int iid, int mid, int min, int max, int dflt, int[] labels) {
        if (block.get()) return; else block.set(true);
        try { intp.TABLESWITCH(iid, mid, min, max, dflt, labels); } finally { block.set(false); }
    }

    public static void IFEQ(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFEQ(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IFEQ(String fileName, int iid, int mid, int label, String className) {
        if (block.get()) return; else block.set(true);
        try { intp.IFEQ(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IFNE(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFNE(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IFLT(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFLT(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IFGE(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFGE(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IFGT(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFGT(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IFLE(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFLE(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ICMPEQ(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ICMPEQ(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ICMPNE(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ICMPNE(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ICMPLT(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ICMPLT(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ICMPGE(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ICMPGE(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ICMPGT(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ICMPGT(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ICMPLE(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ICMPLE(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ACMPEQ(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ACMPEQ(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IF_ACMPNE(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IF_ACMPNE(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void GOTO(int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.GOTO(iid, mid, label); } finally { block.set(false); }
    }

    public static void JSR(int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.JSR(iid, mid, label); } finally { block.set(false); }
    }

    public static void IFNULL(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFNULL(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void IFNONNULL(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.IFNONNULL(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void INVOKEVIRTUAL(int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKEVIRTUAL(iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void INVOKESPECIAL(int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKESPECIAL(iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void INVOKESTATIC(int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKESTATIC(iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void INVOKEINTERFACE(int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKEINTERFACE(iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void GETSTATIC(int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.GETSTATIC(iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void PUTSTATIC(int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.PUTSTATIC(iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void GETFIELD(int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.GETFIELD(iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void PUTFIELD(int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.PUTFIELD(iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void HEAPLOAD1(Object object, String field, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.HEAPLOAD(iid, mid, System.identityHashCode(object), field); } finally { block.set(false); }
    }

    public static void HEAPLOAD2(Object object, int idx, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.HEAPLOAD(iid, mid, System.identityHashCode(object), String.valueOf(idx)); } finally { block.set(false); }
    }

    public static void NEW(int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.NEW(iid, mid, type, 0); } finally { block.set(false); }
    }

    public static void ANEWARRAY(int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.ANEWARRAY(iid, mid, type); } finally { block.set(false); }
    }

    public static void CHECKCAST(int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.CHECKCAST(iid, mid, type); } finally { block.set(false); }
    }

    public static void INSTANCEOF(int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.INSTANCEOF(iid, mid, type); } finally { block.set(false); }
    }

    public static void BIPUSH(int iid, int mid, int value) {
        if (block.get()) return; else block.set(true);
        try { intp.BIPUSH(iid, mid, value); } finally { block.set(false); }
    }

    public static void SIPUSH(int iid, int mid, int value) {
        if (block.get()) return; else block.set(true);
        try { intp.SIPUSH(iid, mid, value); } finally { block.set(false); }
    }

    public static void NEWARRAY(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.NEWARRAY(iid, mid); } finally { block.set(false); }
    }

    public static void ILOAD(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ILOAD(iid, mid, var); } finally { block.set(false); }
    }

    public static void LLOAD(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.LLOAD(iid, mid, var); } finally { block.set(false); }
    }

    public static void FLOAD(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.FLOAD(iid, mid, var); } finally { block.set(false); }
    }

    public static void DLOAD(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.DLOAD(iid, mid, var); } finally { block.set(false); }
    }

    public static void ALOAD(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ALOAD(iid, mid, var); } finally { block.set(false); }
    }

    public static void ISTORE(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ISTORE(iid, mid, var); } finally { block.set(false); }
    }

    public static void LSTORE(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.LSTORE(iid, mid, var); } finally { block.set(false); }
    }

    public static void FSTORE(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.FSTORE(iid, mid, var); } finally { block.set(false); }
    }

    public static void DSTORE(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.DSTORE(iid, mid, var); } finally { block.set(false); }
    }

    public static void ASTORE(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ASTORE(iid, mid, var); } finally { block.set(false); }
    }

    public static void RET(int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.RET(iid, mid, var); } finally { block.set(false); }
    }

    public static void NOP(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.NOP(iid, mid); } finally { block.set(false); }
    }

    public static void ACONST_NULL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ACONST_NULL(iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_M1(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_M1(iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_0(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_0(iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_1(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_1(iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_2(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_2(iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_3(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_3(iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_4(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_4(iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_5(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_5(iid, mid); } finally { block.set(false); }
    }

    public static void LCONST_0(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LCONST_0(iid, mid); } finally { block.set(false); }
    }

    public static void LCONST_1(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LCONST_1(iid, mid); } finally { block.set(false); }
    }

    public static void FCONST_0(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCONST_0(iid, mid); } finally { block.set(false); }
    }

    public static void FCONST_1(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCONST_1(iid, mid); } finally { block.set(false); }
    }

    public static void FCONST_2(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCONST_2(iid, mid); } finally { block.set(false); }
    }

    public static void DCONST_0(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCONST_0(iid, mid); } finally { block.set(false); }
    }

    public static void DCONST_1(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCONST_1(iid, mid); } finally { block.set(false); }
    }

    public static void IALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void LALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void FALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void DALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void AALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.AALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void BALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.BALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void CALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.CALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void SALOAD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.SALOAD(iid, mid); } finally { block.set(false); }
    }

    public static void IASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void LASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void FASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void DASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void AASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.AASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void BASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.BASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void CASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.CASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void SASTORE(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.SASTORE(iid, mid); } finally { block.set(false); }
    }

    public static void POP(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.POP(iid, mid); } finally { block.set(false); }
    }

    public static void POP2(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.POP2(iid, mid); } finally { block.set(false); }
    }

    public static void DUP(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP(iid, mid); } finally { block.set(false); }
    }

    public static void DUP_X1(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP_X1(iid, mid); } finally { block.set(false); }
    }

    public static void DUP_X2(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP_X2(iid, mid); } finally { block.set(false); }
    }

    public static void DUP2(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP2(iid, mid); } finally { block.set(false); }
    }

    public static void DUP2_X1(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP2_X1(iid, mid); } finally { block.set(false); }
    }

    public static void DUP2_X2(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP2_X2(iid, mid); } finally { block.set(false); }
    }

    public static void SWAP(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.SWAP(iid, mid); } finally { block.set(false); }
    }

    public static void IADD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IADD(iid, mid); } finally { block.set(false); }
    }

    public static void LADD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LADD(iid, mid); } finally { block.set(false); }
    }

    public static void FADD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FADD(iid, mid); } finally { block.set(false); }
    }

    public static void DADD(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DADD(iid, mid); } finally { block.set(false); }
    }

    public static void ISUB(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ISUB(iid, mid); } finally { block.set(false); }
    }

    public static void LSUB(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LSUB(iid, mid); } finally { block.set(false); }
    }

    public static void FSUB(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FSUB(iid, mid); } finally { block.set(false); }
    }

    public static void DSUB(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DSUB(iid, mid); } finally { block.set(false); }
    }

    public static void IMUL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IMUL(iid, mid); } finally { block.set(false); }
    }

    public static void LMUL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LMUL(iid, mid); } finally { block.set(false); }
    }

    public static void FMUL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FMUL(iid, mid); } finally { block.set(false); }
    }

    public static void DMUL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DMUL(iid, mid); } finally { block.set(false); }
    }

    public static void IDIV(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IDIV(iid, mid); } finally { block.set(false); }
    }

    public static void LDIV(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LDIV(iid, mid); } finally { block.set(false); }
    }

    public static void FDIV(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FDIV(iid, mid); } finally { block.set(false); }
    }

    public static void DDIV(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DDIV(iid, mid); } finally { block.set(false); }
    }

    public static void IREM(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IREM(iid, mid); } finally { block.set(false); }
    }

    public static void LREM(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LREM(iid, mid); } finally { block.set(false); }
    }

    public static void FREM(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FREM(iid, mid); } finally { block.set(false); }
    }

    public static void DREM(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DREM(iid, mid); } finally { block.set(false); }
    }

    public static void INEG(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.INEG(iid, mid); } finally { block.set(false); }
    }

    public static void LNEG(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LNEG(iid, mid); } finally { block.set(false); }
    }

    public static void FNEG(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FNEG(iid, mid); } finally { block.set(false); }
    }

    public static void DNEG(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DNEG(iid, mid); } finally { block.set(false); }
    }

    public static void ISHL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ISHL(iid, mid); } finally { block.set(false); }
    }

    public static void LSHL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LSHL(iid, mid); } finally { block.set(false); }
    }

    public static void ISHR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ISHR(iid, mid); } finally { block.set(false); }
    }

    public static void LSHR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LSHR(iid, mid); } finally { block.set(false); }
    }

    public static void IUSHR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IUSHR(iid, mid); } finally { block.set(false); }
    }

    public static void LUSHR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LUSHR(iid, mid); } finally { block.set(false); }
    }

    public static void IAND(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IAND(iid, mid); } finally { block.set(false); }
    }

    public static void LAND(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LAND(iid, mid); } finally { block.set(false); }
    }

    public static void IOR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IOR(iid, mid); } finally { block.set(false); }
    }

    public static void LOR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LOR(iid, mid); } finally { block.set(false); }
    }

    public static void IXOR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IXOR(iid, mid); } finally { block.set(false); }
    }

    public static void LXOR(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LXOR(iid, mid); } finally { block.set(false); }
    }

    public static void I2L(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2L(iid, mid); } finally { block.set(false); }
    }

    public static void I2F(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2F(iid, mid); } finally { block.set(false); }
    }

    public static void I2D(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2D(iid, mid); } finally { block.set(false); }
    }

    public static void L2I(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.L2I(iid, mid); } finally { block.set(false); }
    }

    public static void L2F(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.L2F(iid, mid); } finally { block.set(false); }
    }

    public static void L2D(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.L2D(iid, mid); } finally { block.set(false); }
    }

    public static void F2I(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.F2I(iid, mid); } finally { block.set(false); }
    }

    public static void F2L(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.F2L(iid, mid); } finally { block.set(false); }
    }

    public static void F2D(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.F2D(iid, mid); } finally { block.set(false); }
    }

    public static void D2I(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.D2I(iid, mid); } finally { block.set(false); }
    }

    public static void D2L(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.D2L(iid, mid); } finally { block.set(false); }
    }

    public static void D2F(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.D2F(iid, mid); } finally { block.set(false); }
    }

    public static void I2B(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2B(iid, mid); } finally { block.set(false); }
    }

    public static void I2C(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2C(iid, mid); } finally { block.set(false); }
    }

    public static void I2S(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2S(iid, mid); } finally { block.set(false); }
    }

    public static void LCMP(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LCMP(iid, mid); } finally { block.set(false); }
    }

    public static void FCMPL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCMPL(iid, mid); } finally { block.set(false); }
    }

    public static void FCMPG(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCMPG(iid, mid); } finally { block.set(false); }
    }

    public static void DCMPL(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCMPL(iid, mid); } finally { block.set(false); }
    }

    public static void DCMPG(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCMPG(iid, mid); } finally { block.set(false); }
    }

    public static void IRETURN(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IRETURN(iid, mid); } finally { block.set(false); }
    }

    public static void LRETURN(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LRETURN(iid, mid); } finally { block.set(false); }
    }

    public static void FRETURN(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FRETURN(iid, mid); } finally { block.set(false); }
    }

    public static void DRETURN(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DRETURN(iid, mid); } finally { block.set(false); }
    }

    public static void ARETURN(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ARETURN(iid, mid); } finally { block.set(false); }
    }

    public static void RETURN(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.RETURN(iid, mid); } finally { block.set(false); }
    }

    public static void ARRAYLENGTH(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ARRAYLENGTH(iid, mid); } finally { block.set(false); }
    }

    public static void ATHROW(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ATHROW(iid, mid); } finally { block.set(false); }
    }

    public static void MONITORENTER(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.MONITORENTER(iid, mid); } finally { block.set(false); }
    }

    public static void MONITOREXIT(int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.MONITOREXIT(iid, mid); } finally { block.set(false); }
    }

    public static void LDC(String fileName, int iid, int mid, int c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(fileName, iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(String fileName, int iid, int mid, long c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(fileName, iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(String fileName, int iid, int mid, float c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(fileName, iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(String fileName, int iid, int mid, double c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(fileName, iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(String fileName, int iid, int mid, String c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(fileName, iid, mid, c); } finally { block.set(false); }
    }

    public static void LDC(String fileName, int iid, int mid, Object c) {
        if (block.get()) return; else block.set(true);
        try { intp.LDC(fileName, iid, mid, c); } finally { block.set(false); }
    }

    public static void IINC(String fileName, int iid, int mid, int var, int increment) {
        if (block.get()) return; else block.set(true);
        try { intp.IINC(fileName, iid, mid, var, increment); } finally { block.set(false); }
    }

    public static void MULTIANEWARRAY(String fileName, int iid, int mid, String desc, int dims) {
        if (block.get()) return; else block.set(true);
        try { intp.MULTIANEWARRAY(fileName, iid, mid, desc, dims); } finally { block.set(false); }
    }

    public static void LOOKUPSWITCH(String fileName, int iid, int mid, int dflt, int[] keys, int[] labels) {
        if (block.get()) return; else block.set(true);
        try { intp.LOOKUPSWITCH(fileName, iid, mid, dflt, keys, labels); } finally { block.set(false); }
    }

    public static void TABLESWITCH(String fileName, int iid, int mid, int min, int max, int dflt, int[] labels) {
        if (block.get()) return; else block.set(true);
        try { intp.TABLESWITCH(fileName, iid, mid, min, max, dflt, labels); } finally { block.set(false); }
    }

    public static void GOTO(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.GOTO(fileName, iid, mid, label); } finally { block.set(false); }
    }

    public static void JSR(String fileName, int iid, int mid, int label) {
        if (block.get()) return; else block.set(true);
        try { intp.JSR(fileName, iid, mid, label); } finally { block.set(false); }
    }


    public static void INVOKEVIRTUAL(String fileName, int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKEVIRTUAL(fileName, iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void INVOKESPECIAL(String fileName, int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKESPECIAL(fileName, iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void INVOKESTATIC(String fileName, int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKESTATIC(fileName, iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void INVOKEINTERFACE(String fileName, int iid, int mid, String owner, String name, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKEINTERFACE(fileName, iid, mid, owner, name, desc); } finally { block.set(false); }
    }

    public static void GETSTATIC(String fileName, int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.GETSTATIC(fileName, iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void PUTSTATIC(String fileName, int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.PUTSTATIC(fileName, iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void GETFIELD(String fileName, int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.GETFIELD(fileName, iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void PUTFIELD(String fileName, int iid, int mid, int cIdx, int fIdx, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.PUTFIELD(fileName, iid, mid, cIdx, fIdx, desc); } finally { block.set(false); }
    }

    public static void NEW(String fileName, int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.NEW(fileName, iid, mid, type, 0); } finally { block.set(false); }
    }

    public static void ANEWARRAY(String fileName, int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.ANEWARRAY(fileName, iid, mid, type); } finally { block.set(false); }
    }

    public static void CHECKCAST(String fileName, int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.CHECKCAST(fileName, iid, mid, type); } finally { block.set(false); }
    }

    public static void INSTANCEOF(String fileName, int iid, int mid, String type) {
        if (block.get()) return; else block.set(true);
        try { intp.INSTANCEOF(fileName, iid, mid, type); } finally { block.set(false); }
    }

    public static void BIPUSH(String fileName, int iid, int mid, int value) {
        if (block.get()) return; else block.set(true);
        try { intp.BIPUSH(fileName, iid, mid, value); } finally { block.set(false); }
    }

    public static void SIPUSH(String fileName, int iid, int mid, int value) {
        if (block.get()) return; else block.set(true);
        try { intp.SIPUSH(fileName, iid, mid, value); } finally { block.set(false); }
    }

    public static void NEWARRAY(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.NEWARRAY(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ILOAD(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ILOAD(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void LLOAD(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.LLOAD(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void FLOAD(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.FLOAD(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void DLOAD(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.DLOAD(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void ALOAD(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ALOAD(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void ISTORE(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ISTORE(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void LSTORE(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.LSTORE(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void FSTORE(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.FSTORE(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void DSTORE(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.DSTORE(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void ASTORE(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.ASTORE(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void RET(String fileName, int iid, int mid, int var) {
        if (block.get()) return; else block.set(true);
        try { intp.RET(fileName, iid, mid, var); } finally { block.set(false); }
    }

    public static void NOP(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.NOP(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ACONST_NULL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ACONST_NULL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_M1(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_M1(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_0(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_0(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_1(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_1(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_2(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_2(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_3(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_3(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_4(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_4(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ICONST_5(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ICONST_5(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LCONST_0(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LCONST_0(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LCONST_1(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LCONST_1(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FCONST_0(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCONST_0(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FCONST_1(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCONST_1(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FCONST_2(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCONST_2(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DCONST_0(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCONST_0(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DCONST_1(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCONST_1(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void AALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.AALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void BALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.BALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void CALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.CALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void SALOAD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.SALOAD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void AASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.AASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void BASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.BASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void CASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.CASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void SASTORE(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.SASTORE(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void POP(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.POP(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void POP2(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.POP2(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DUP(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DUP_X1(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP_X1(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DUP_X2(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP_X2(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DUP2(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP2(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DUP2_X1(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP2_X1(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DUP2_X2(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DUP2_X2(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void SWAP(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.SWAP(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IADD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IADD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LADD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LADD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FADD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FADD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DADD(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DADD(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ISUB(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ISUB(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LSUB(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LSUB(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FSUB(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FSUB(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DSUB(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DSUB(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IMUL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IMUL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LMUL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LMUL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FMUL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FMUL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DMUL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DMUL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IDIV(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IDIV(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LDIV(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LDIV(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FDIV(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FDIV(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DDIV(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DDIV(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IREM(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IREM(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LREM(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LREM(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FREM(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FREM(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DREM(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DREM(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void INEG(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.INEG(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LNEG(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LNEG(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FNEG(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FNEG(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DNEG(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DNEG(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ISHL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ISHL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LSHL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LSHL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ISHR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ISHR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LSHR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LSHR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IUSHR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IUSHR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LUSHR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LUSHR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IAND(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IAND(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LAND(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LAND(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IOR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IOR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LOR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LOR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IXOR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IXOR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LXOR(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LXOR(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void I2L(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2L(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void I2F(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2F(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void I2D(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2D(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void L2I(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.L2I(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void L2F(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.L2F(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void L2D(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.L2D(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void F2I(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.F2I(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void F2L(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.F2L(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void F2D(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.F2D(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void D2I(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.D2I(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void D2L(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.D2L(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void D2F(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.D2F(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void I2B(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2B(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void I2C(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2C(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void I2S(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.I2S(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LCMP(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LCMP(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FCMPL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCMPL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FCMPG(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FCMPG(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DCMPL(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCMPL(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DCMPG(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DCMPG(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IRETURN(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IRETURN(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void LRETURN(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LRETURN(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void FRETURN(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FRETURN(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void DRETURN(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DRETURN(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ARETURN(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ARETURN(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void RETURN(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.RETURN(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void IRETURN(String fileName, String method, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.IRETURN(fileName, method, iid, mid); } finally { block.set(false); }
    }

    public static void LRETURN(String fileName, String method, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.LRETURN(fileName, method, iid, mid); } finally { block.set(false); }
    }

    public static void FRETURN(String fileName, String method, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.FRETURN(fileName, method, iid, mid); } finally { block.set(false); }
    }

    public static void DRETURN(String fileName, String method, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.DRETURN(fileName, method, iid, mid); } finally { block.set(false); }
    }

    public static void ARETURN(String fileName, String  method, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ARETURN(fileName, method, iid, mid); } finally { block.set(false); }
    }

    public static void RETURN(String fileName, String method, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.RETURN(fileName, method, iid, mid); } finally { block.set(false); }
    }

    public static void ARRAYLENGTH(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ARRAYLENGTH(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void ATHROW(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.ATHROW(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void MONITORENTER(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.MONITORENTER(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void MONITOREXIT(String fileName, int iid, int mid) {
        if (block.get()) return; else block.set(true);
        try { intp.MONITOREXIT(fileName, iid, mid); } finally { block.set(false); }
    }

    public static void GETVALUE_double(double v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_double(v); } finally { block.set(false); }
    }

    public static void GETVALUE_long(long v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_long(v); } finally { block.set(false); }
    }

    public static void GETVALUE_Object(Object v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_Object(v); } finally { block.set(false); }
    }

    public static void GETVALUE_boolean(boolean v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_boolean(v); } finally { block.set(false); }
    }

    public static void GETVALUE_byte(byte v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_byte(v); } finally { block.set(false); }
    }

    public static void GETVALUE_char(char v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_char(v); } finally { block.set(false); }
    }

    public static void GETVALUE_float(float v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_float(v); } finally { block.set(false); }
    }

    public static void GETVALUE_int(int v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_int(v); } finally { block.set(false); }
    }

    public static void GETVALUE_short(short v) {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_short(v); } finally { block.set(false); }
    }

    public static void GETVALUE_void() {
        if (block.get()) return; else block.set(true);
        try { intp.GETVALUE_void(); } finally { block.set(false); }
    }

    public static void METHOD_BEGIN(String fileName, String className, String methodName, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.METHOD_BEGIN(fileName, className, methodName, desc); } finally { block.set(false); }
    }

    public static void METHOD_THROW() {
        if (block.get()) return; else block.set(true);
        try { intp.METHOD_THROW(); } finally { block.set(false); }
    }

    public static void INVOKEMETHOD_EXCEPTION() {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKEMETHOD_EXCEPTION(); } finally { block.set(false); }
    }

    public static void INVOKEMETHOD_END(String owner, String methodName, String desc) {
        if (block.get()) return; else block.set(true);
        try { intp.INVOKEMETHOD_END(owner, methodName, desc); } finally { block.set(false); }
    }

    public static void SPECIAL(int i) {
        if (block.get()) return; else block.set(true);
        try { intp.SPECIAL(i); } finally { block.set(false); }
    }

    public static void MAKE_SYMBOLIC() {
        if (block.get()) return; else block.set(true);
        try { intp.MAKE_SYMBOLIC(); } finally { block.set(false); }
    }

    public static void flush() {
        intp.flush();
    }
}
