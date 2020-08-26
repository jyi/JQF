//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.hub.se.cfg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CFGNode implements Serializable {
    private static final long serialVersionUID = 4625833866485308480L;
    private static int globalCurrentNodeID = 0;
    protected int nodeId = generateNewNodeID();
    protected Set<CFGNode> successors = new HashSet();
    protected Set<CFGNode> predecessors = new HashSet();
    protected boolean isVirtual;
    protected boolean isRootNode;
    protected int startOffset;
    protected int endOffset;
    protected String fullQualifiedMethodName;
    protected int startSourceLineNumber;
    protected int endSourceLineNumber;
    protected Map<Integer, Integer> distances = new HashMap();
    protected Set<String> callsMethod = new HashSet();

    public CFGNode(int startOffset, int endOffset, String fullQualifiedMethodName, int startSourceLineNumber, int endSourceLineNumber) {
        this.fullQualifiedMethodName = fullQualifiedMethodName;
        this.isVirtual = false;
        this.isRootNode = false;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.startSourceLineNumber = startSourceLineNumber;
        this.endSourceLineNumber = endSourceLineNumber;
    }

    public static int generateNewNodeID() {
        return globalCurrentNodeID++;
    }

    public int getId() {
        return this.nodeId;
    }

    public void setFullQualifiedMethodName(String fullQualifiedMethodName) {
        this.fullQualifiedMethodName = fullQualifiedMethodName;
    }

    public String getFullQualifiedMethodName() {
        return this.fullQualifiedMethodName;
    }

    public void addSuccessor(CFGNode n) {
        this.successors.add(n);
    }

    public void removeSuccessor(CFGNode n) {
        this.successors.remove(n);
    }

    public void addPredecessor(CFGNode n) {
        this.predecessors.add(n);
    }

    public void removePredecessor(CFGNode n) {
        this.predecessors.remove(n);
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getStartOffset() {
        return this.startOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public int getEndOffset() {
        return this.endOffset;
    }

    public Set<CFGNode> getPredecessors() {
        return this.predecessors;
    }

    public Set<CFGNode> getSuccessors() {
        return this.successors;
    }

    public void setVirtual(boolean flag) {
        this.isVirtual = flag;
    }

    public boolean isVirtual() {
        return this.isVirtual;
    }

    public void setRootNode(boolean flag) {
        this.isRootNode = flag;
    }

    public boolean isRootNode() {
        return this.isRootNode;
    }

    public String toString() {
        return new String("[" + this.nodeId + ", [" + this.startOffset + ", " + this.endOffset + "], " + (this.startSourceLineNumber == this.endSourceLineNumber ? this.startSourceLineNumber : this.startSourceLineNumber + "-" + this.endSourceLineNumber) + "]" + (this.isVirtual() ? "v" : "") + this.distances);
    }

    public void setSourceLineNumber(int startLineNumber, int endLineNumber) {
        this.startSourceLineNumber = startLineNumber;
        this.endSourceLineNumber = endLineNumber;
    }

    public int getStartSourceLineNumber() {
        return this.startSourceLineNumber;
    }

    public int getEndSourceLineNumber() {
        return this.endSourceLineNumber;
    }

    public void setDistance(int targetNodeId, int distance) {
        this.distances.put(targetNodeId, distance);
    }

    public boolean setDistanceIfBetter(int targetNodeId, int newDistance) {
        Integer existingDistance = (Integer)this.distances.get(targetNodeId);
        if (existingDistance != null && existingDistance < newDistance) {
            return false;
        } else {
            this.distances.put(targetNodeId, newDistance);
            return true;
        }
    }

    public Integer getDistance(int targetNodeId) {
        return (Integer)this.distances.get(targetNodeId);
    }

    public void addCall(String method) {
        this.callsMethod.add(method);
    }

    public boolean isCallerNode() {
        return this.callsMethod != null;
    }

    public Set<String> getMethodsCalled() {
        return this.callsMethod;
    }
}
