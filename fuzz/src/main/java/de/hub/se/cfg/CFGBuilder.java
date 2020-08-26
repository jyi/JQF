//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.hub.se.cfg;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;

public class CFGBuilder {
    private String className;
    Map<String, CFG> cfgMap = new HashMap();
    private JavaClass javaClass;
    private Method[] methods;
    private ConstantPoolGen CPG;
    private TreeSet<InstructionHandle> leaders;
    private TreeSet<InstructionHandle> ends;
    private TreeSet<InstructionHandle> calls;
    private TreeSet<InstructionHandle> branches;
    private static final boolean DEBUG = false;
    MethodGen mg;

    public CFGBuilder() {
        Comparator<Object> c = new CFGBuilder.IHandleComparator();
        this.leaders = new TreeSet(c);
        this.ends = new TreeSet(c);
        this.calls = new TreeSet(c);
        this.branches = new TreeSet(c);
    }

    public boolean parseClass(String className, Set<String> skipped) throws Exception {
        try {
            this.javaClass = Repository.lookupClass(className);
        } catch (ClassNotFoundException var5) {
            File f = new File(className);
            if (!f.exists()) {
                throw new Exception("Cannot find class: " + className);
            }

            this.javaClass = (new ClassParser(className)).parse();
        }

        if (this.javaClass.isInterface()) {
            // System.out.println("We cannot build CFG for interface: " + this.javaClass.getClassName());
            skipped.add(this.javaClass.getClassName());
            return false;
        } else {
            this.className = this.javaClass.getClassName();
            this.CPG = new ConstantPoolGen(this.javaClass.getConstantPool());
            this.methods = this.javaClass.getMethods();
            return true;
        }
    }

    private String getCompleteMethodName(int methodIndex) {
        if (methodIndex >= 0 && methodIndex < this.methods.length) {
            if (!this.methods[methodIndex].isAbstract() && !this.methods[methodIndex].isNative()) {
                this.reset();
                this.mg = new MethodGen(this.methods[methodIndex], this.className, this.CPG);
                String completeMethodName = CFGUtility.getCompleteMethodName(this.className, this.methods[methodIndex]);
                return completeMethodName;
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Method index out of range");
        }
    }

    public MethodGen getMethodGen() {
        return this.mg;
    }

    private CFG buildCFG(int methodIndex) throws Exception {
        String completeMethodName = this.getCompleteMethodName(methodIndex);
        if (completeMethodName == null) {
            return null;
        } else {
            CFG cfg = (CFG)this.cfgMap.get(completeMethodName);
            if (cfg != null) {
                return cfg;
            } else {
                List<Object> pendingInference = new ArrayList();
                cfg = new CFG(completeMethodName);
                this.cfgMap.put(completeMethodName, cfg);
                MethodGen mg = new MethodGen(this.methods[methodIndex], this.className, this.CPG);
                this.formNodes(cfg, mg.getInstructionList(), mg.getLineNumberTable(this.CPG));
                this.formEdges(cfg, pendingInference);
                this.checkBranchInstruction(cfg);
                this.checkCalls(cfg);
                return cfg;
            }
        }
    }

    public CFG getCFG(String completeMethodName) throws Exception {
        CFG cfg = (CFG)this.cfgMap.get(completeMethodName);
        if (cfg != null) {
            return cfg;
        } else {
            if (cfg == null) {
                int methodIndex = this.getMethodIndex(completeMethodName);
                if (methodIndex < 0) {
                    throw new Exception("No such method");
                }

                if (this.methods[methodIndex].isAbstract() || this.methods[methodIndex].isNative()) {
                    return null;
                }

                cfg = this.buildCFG(methodIndex);
            }

            return cfg;
        }
    }

    private int getMethodIndex(String completeMethodName) {
        if (!completeMethodName.startsWith(this.className)) {
            return -1;
        } else {
            String methodName = completeMethodName.substring(this.className.length() + 1);

            for(int i = 0; i < this.methods.length; ++i) {
                Method m = this.methods[i];
                if (methodName.equals(m.getName() + m.getSignature())) {
                    return i;
                }
            }

            return -1;
        }
    }

    public Map<String, CFG> buildCFGForAll() throws Exception {
        for(int i = 0; i < this.methods.length; ++i) {
            this.buildCFG(i);
        }

        return this.cfgMap;
    }

    private void reset() {
        this.leaders.clear();
        this.ends.clear();
        this.calls.clear();
        this.branches.clear();
    }

    private void formNodes(CFG cfg, InstructionList il, LineNumberTable lineNumberTable) {
        this.leaders.add(il.getStart());

        for(InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
            if (!this.calls.contains(ih) || !this.leaders.contains(ih) && !this.ends.contains(ih)) {
                Instruction insn = ih.getInstruction();
                if (!(insn instanceof BranchInstruction)) {
                    if (insn instanceof InvokeInstruction) {
                        InvokeInstruction invokeInstr = (InvokeInstruction)insn;
                        String methodClass = invokeInstr.getReferenceType(this.CPG).toString();
                        if (methodClass.indexOf("java.lang.System") != -1 && invokeInstr.getMethodName(this.CPG).equals("exit")) {
                            this.ends.add(ih);
                            this.leaders.add(ih.getNext());
                        } else {
                            this.calls.add(ih);
                        }
                    } else if (insn instanceof ReturnInstruction) {
                        this.ends.add(ih);
                        if (ih.getNext() != null) {
                            this.leaders.add(ih.getNext());
                        }
                    }
                } else {
                    this.branches.add(ih);
                    InstructionHandle target;
                    InstructionHandle prev_ih;
                    if (!(insn instanceof GotoInstruction) && !(insn instanceof IfInstruction)) {
                        if (insn instanceof Select) {
                            Select selectInstr = (Select)insn;
                            this.ends.add(ih);
                            target = selectInstr.getTarget();
                            this.leaders.add(target);
                            prev_ih = target.getPrev();
                            if (prev_ih != null) {
                                this.ends.add(prev_ih);
                            }

                            InstructionHandle[] targets = selectInstr.getTargets();

                            for(int k = 0; k < targets.length; ++k) {
                                this.leaders.add(targets[k]);
                                prev_ih = targets[k].getPrev();
                                if (prev_ih != null) {
                                    this.ends.add(prev_ih);
                                }
                            }

                            this.leaders.add(ih.getNext());
                        }
                    } else {
                        this.ends.add(ih);
                        target = ((BranchInstruction)insn).getTarget();
                        this.leaders.add(target);
                        prev_ih = target.getPrev();
                        if (prev_ih != null) {
                            this.ends.add(prev_ih);
                        }

                        InstructionHandle next_ih = ih.getNext();
                        if (next_ih != null) {
                            this.leaders.add(ih.getNext());
                        }
                    }
                }
            }
        }

        this.ends.add(il.getEnd());
        cfg.addVirtualNode(new CFGNode(0, 0, cfg.getMethodName(), -1, -1), true);
        Iterator<InstructionHandle> leadersIt = this.leaders.iterator();
        Iterator endIt = this.ends.iterator();

        InstructionHandle startHandle;
        while(leadersIt.hasNext() && endIt.hasNext()) {
            startHandle = (InstructionHandle)leadersIt.next();
            InstructionHandle endHandle = (InstructionHandle)endIt.next();
            int firstHandledLineNumber = lineNumberTable.getSourceLine(startHandle.getPosition());
            int lastHandledLineNumber = firstHandledLineNumber;

            for(int i = startHandle.getPosition() + 1; i <= endHandle.getPosition(); ++i) {
                int currentLineNumber = lineNumberTable.getSourceLine(i);
                if (currentLineNumber < firstHandledLineNumber) {
                    firstHandledLineNumber = currentLineNumber;
                } else if (currentLineNumber > lastHandledLineNumber) {
                    lastHandledLineNumber = currentLineNumber;
                }
            }

            CFGNode node = new CFGNode(startHandle.getPosition(), endHandle.getPosition(), cfg.getMethodName(), firstHandledLineNumber, lastHandledLineNumber);
            cfg.addNode(node);
        }

        startHandle = (InstructionHandle)this.ends.last();
        int offset = startHandle.getPosition();
        CFGNode virtualExitNode = new CFGNode(offset, offset, cfg.getMethodName(), -1, -1);
        cfg.addVirtualNode(virtualExitNode, false);
    }

    private void formEdges(CFG cfg, List<Object> pendingInference) {
        int nodeId = cfg.getFirstRealNodeId();
        cfg.addEdge(new CFGEdge(nodeId, cfg.getEntryBlockNodeId(), -1));

        for(Iterator var5 = this.ends.iterator(); var5.hasNext(); ++nodeId) {
            InstructionHandle ih = (InstructionHandle)var5.next();
            Instruction insn = ih.getInstruction();
            if (insn instanceof Select) {
                Select selectInstr = (Select)insn;
                InstructionHandle[] targets = selectInstr.getTargets();
                int[] matches = selectInstr.getMatchs();
                if (matches.length != targets.length) {
                    throw new ClassFormatError("Invalid switch instruction: " + ih.toString().trim());
                }

                CFGNode node;
                for(int j = 0; j < targets.length; ++j) {
                    node = (CFGNode)cfg.nodeOffsetMap.get(targets[j].getPosition());
                    cfg.addEdge(new CFGEdge(node.getId(), nodeId, j));
                }

                node = (CFGNode)cfg.nodeOffsetMap.get(selectInstr.getTarget().getPosition());
                cfg.addEdge(new CFGEdge(node.getId(), nodeId, matches.length));
            } else {
                int targetOffset;
                if (insn instanceof GotoInstruction) {
                    BranchInstruction branchInstr = (BranchInstruction)insn;
                    InstructionHandle target = branchInstr.getTarget();
                    targetOffset = target.getPosition();
                    CFGNode successor = (CFGNode)cfg.nodeOffsetMap.get(targetOffset);
                    cfg.addEdge(new CFGEdge(successor.getId(), nodeId, -1));
                } else if (insn instanceof IfInstruction) {
                    targetOffset = ((IfInstruction)insn).getTarget().getPosition();
                    CFGNode targetNode = (CFGNode)cfg.nodeOffsetMap.get(targetOffset);
                    cfg.addEdge(new CFGEdge(targetNode.getId(), nodeId, 1));
                    cfg.addEdge(new CFGEdge(nodeId + 1, nodeId, 0));
                } else if (!(insn instanceof ReturnInstruction) && (!(insn instanceof INVOKESTATIC) || ((INVOKESTATIC)insn).getReferenceType(this.CPG).toString().indexOf("java.lang.System") == -1 || !((INVOKESTATIC)insn).getMethodName(this.CPG).equals("exit"))) {
                    cfg.addEdge(new CFGEdge(nodeId + 1, nodeId, -1));
                } else {
                    cfg.addEdge(new CFGEdge(cfg.getExitNodeId(), nodeId, -1));
                }
            }
        }

    }

    private void checkBranchInstruction(CFG cfg) {
        int nodeId = cfg.getFirstRealNodeId();
        int exitNodeId = cfg.getExitNodeId();
        Iterator var4 = this.branches.iterator();

        while(var4.hasNext()) {
            InstructionHandle ih = (InstructionHandle)var4.next();

            for(int pos = ih.getPosition(); nodeId < exitNodeId; ++nodeId) {
                CFGNode node = cfg.getNodeById(nodeId);
                if (node.getStartOffset() <= pos && pos <= node.getEndOffset()) {
                    cfg.addBranch(nodeId, pos);
                    break;
                }
            }

            if (nodeId == exitNodeId) {
                System.err.println("Error in corrsponding branches to nodes.");
            }
        }

    }

    private void checkCalls(CFG cfg) {
        int nodeId = cfg.getFirstRealNodeId();
        int exitNodeId = cfg.getExitNodeId();
        Iterator var4 = this.calls.iterator();

        while(var4.hasNext()) {
            InstructionHandle ih = (InstructionHandle)var4.next();
            Instruction instr = ih.getInstruction();
            InvokeInstruction invokeInstr = (InvokeInstruction)instr;
            String methodClass = invokeInstr.getReferenceType(this.CPG).toString();
            String methodName = invokeInstr.getMethodName(this.CPG);
            String fullQualifiedMethodName = CFGUtility.getFullQualifiedMethodName(methodClass, methodName, invokeInstr.getSignature(this.CPG));

            for(int pos = ih.getPosition(); nodeId < exitNodeId; ++nodeId) {
                CFGNode node = cfg.getNodeById(nodeId);
                if (node.getStartOffset() <= pos && pos <= node.getEndOffset()) {
                    node.addCall(fullQualifiedMethodName);
                    cfg.addCall(nodeId, fullQualifiedMethodName);
                    break;
                }
            }

            if (nodeId == exitNodeId) {
                System.err.println("Error in corrsponding calls to nodes.");
            }
        }

    }

    public Map<String, CFG> getCfgMap() {
        return this.cfgMap;
    }

    public void setCfgMap(Map<String, CFG> cfgMap) {
        this.cfgMap = cfgMap;
    }

    public static Set<String> loadInput(String inputRawString) {
        Set<String> inputClasses = new HashSet<>();
        for (String input : inputRawString.split(":")) {
            if (input.endsWith(".class")) {
                // single class file, has to be a relative path from a directory on the class
                // path
                inputClasses.add(input);
            } else if (input.endsWith(".jar")) {
                // JAR file
                extractJar(input, inputClasses);
                addToClassPath(input);
            } else {
                // directory
                System.out.println("Loading dir: " + input);
                loadDirectory(input, inputClasses);
                // addToClassPath(input);
            }
        }
        return inputClasses;
    }

    private static void loadDirectory(String input, Set<String> inputClasses) {
        final int dirprefix;
        if (input.endsWith("/"))
            dirprefix = input.length();
        else
            dirprefix = input.length() + 1;
        try {
            Files.walk(Paths.get(input)).filter(Files::isRegularFile).forEach(filePath -> {
                String name = filePath.toString();
                if (name.endsWith(".class")) {
//                    inputClasses.add(name.substring(dirprefix));
                    inputClasses.add(name);
                }

            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading from directory: " + input);
        }
    }

    private static void addToClassPath(String url) {
        try {
            File file = new File(url);
            java.lang.reflect.Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
        } catch (Exception e) {
            throw new RuntimeException("Error adding location to class path: " + url);
        }

    }

    /**
     * Extracts all class file names from a JAR file (possibly nested with more JARs).
     *
     * @param file
     *            The name of the file.
     * @param classes
     *            Class names will be stored in here.
     */
    public static void extractJar(String file, Set<String> classes) {
        try {
            // open JAR file
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();

            // iterate JAR entries
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith(".class")) {
                    entryName = entryName.substring(0, entryName.length()-6);
                    classes.add(entryName);
                } else if (entryName.endsWith(".jar")) {
                    // load nested JAR
                    // extractJar(entryName, classes); TODO YN: skip for now
                }
            }

            // close JAR file
            jarFile.close();

        } catch (IOException e) {
            throw new RuntimeException("Error reading from JAR file: " + file);
        }
    }

    public static CFGAnalysis genCFGForClasses(Set<String> classes, Set<String> classesToSkip, String additionalClasses) {
        Map<String, CFG> map = new HashMap();
        Set<String> skipped = new HashSet();
        CFGBuilder cfgb = new CFGBuilder();
        Iterator var6 = classes.iterator();

        while(var6.hasNext()) {
            String entry = (String)var6.next();
            if (classesToSkip.contains(entry)) {
                System.out.println("Skip CFG construction for class: " + entry);
            } else {
                boolean parsed = false;

                try {
                    parsed = cfgb.parseClass(entry, skipped);
                } catch (Exception var15) {
                    var15.printStackTrace();
                    System.exit(1);
                }

                if (parsed) {
                    try {
                        cfgb.buildCFGForAll();
                    } catch (Exception var14) {
                        var14.printStackTrace();
                        System.exit(1);
                    }

                    map.putAll(cfgb.getCfgMap());
                }
            }
        }

        if (additionalClasses != null) {
            String[] var16 = additionalClasses.split(",");
            int var17 = var16.length;

            for(int var18 = 0; var18 < var17; ++var18) {
                String additionalClass = var16[var18];
                boolean parsed = false;

                try {
                    parsed = cfgb.parseClass(additionalClass, skipped);
                } catch (Exception var13) {
                    var13.printStackTrace();
                    System.exit(1);
                }

                if (parsed) {
                    try {
                        cfgb.buildCFGForAll();
                    } catch (Exception var12) {
                        var12.printStackTrace();
                        System.exit(1);
                    }

                    map.putAll(cfgb.getCfgMap());
                }
            }
        }

        CFGAnalysis analysis = new CFGAnalysis(map, skipped);
        return analysis;
    }

    private class IHandleComparator implements Comparator<Object> {
        private IHandleComparator() {
        }

        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            } else {
                return ((InstructionHandle)o1).getPosition() < ((InstructionHandle)o2).getPosition() ? -1 : 1;
            }
        }
    }
}
