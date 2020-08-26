//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.hub.se.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class CFG implements Serializable {
    private static final long serialVersionUID = 7889362340700254268L;
    protected List<CFGNode> nodes = new ArrayList();
    protected List<CFGEdge> edges = new ArrayList();
    protected String completeMethodName;
    protected Set<String> invokedMethods = new HashSet();
    protected Map<Integer, CFGNode> nodeOffsetMap = new HashMap();
    protected Map<Integer, Integer> branchNodeMap = new HashMap();
    protected Map<String, Set<Integer>> callNodeMap = new HashMap();
    protected Map<Integer, Integer> nodeBranchMap = new HashMap();
    protected Map<Integer, Integer> edgeCoverageMap = new HashMap();
    protected Map<Integer, Integer> nodeCoverageMap = new HashMap();
    protected Map<Integer, CFGNode> idNodeMap = new HashMap();
    protected Map<Integer, Set<CFGNode>> nodeSourceLineMap = new HashMap();
    public static final int OUTGOINGMATCH = 0;
    public static final int INCOMINGMATCH = 1;

    public CFG(String name) {
        this.completeMethodName = name;
    }

    protected int getEntryBlockNodeId() {
        CFGNode firstNode = (CFGNode)this.nodes.get(0);
        if (!firstNode.isVirtual()) {
            throw new RuntimeException("First node in cfg is not virtual!");
        } else {
            return firstNode.getId();
        }
    }

    protected CFGNode getRootNode() {
        return (CFGNode)this.nodes.get(0);
    }

    protected int getFirstRealNodeId() {
        CFGNode firstRealNode = null;
        Iterator var2 = this.nodes.iterator();

        while(var2.hasNext()) {
            CFGNode node = (CFGNode)var2.next();
            if (!node.isVirtual()) {
                firstRealNode = node;
                break;
            }
        }

        if (firstRealNode == null) {
            throw new RuntimeException("No real node in given cfg!");
        } else {
            return firstRealNode.getId();
        }
    }

    protected int getExitNodeId() {
        CFGNode exitNode = (CFGNode)this.nodes.get(this.nodes.size() - 1);
        if (!exitNode.isVirtual()) {
            throw new RuntimeException("Last node (exit node) is not virtual!");
        } else {
            return exitNode.getId();
        }
    }

    protected CFGNode getExitNode() {
        CFGNode exitNode = (CFGNode)this.nodes.get(this.nodes.size() - 1);
        if (!exitNode.isVirtual()) {
            throw new RuntimeException("Last node (exit node) is not virtual!");
        } else {
            return exitNode;
        }
    }

    protected Set<CFGNode> getLastRealNodeIds() {
        CFGNode lastNode = this.getExitNode();
        return lastNode.getPredecessors();
    }

    protected void removeNode(CFGNode n) {
        this.nodes.remove(n);
    }

    protected void removeEdge(CFGEdge e) {
        this.edges.remove(e);
    }

    public CFGEdge getEdge(CFGNode sourceNode, CFGNode sinkNode) {
        for(int i = 0; i < this.edges.size(); ++i) {
            CFGEdge e = (CFGEdge)this.edges.get(i);
            if (e.predecessorId == sourceNode.nodeId && e.successorId == sinkNode.nodeId) {
                return e;
            }
        }

        throw new NoSuchElementException("No matching edge exists in graph");
    }

    public CFGEdge getEdge(int nodeId, int choice) {
        for(int i = 0; i < this.edges.size(); ++i) {
            CFGEdge e = (CFGEdge)this.edges.get(i);
            if (e.predecessorId == nodeId && e.choice == choice) {
                return e;
            }
        }

        throw new NoSuchElementException("No matching edge exists in graph");
    }

    public CFGEdge[] getEdges() {
        CFGEdge[] e = new CFGEdge[this.edges.size()];
        this.edges.toArray(e);
        return e;
    }

    public CFGEdge[] getEdges(CFGNode sourceNode, CFGNode sinkNode, CFGEdge[] a) {
        List<Object> matchingEdges = new ArrayList();

        for(int i = 0; i < this.edges.size(); ++i) {
            CFGEdge e = (CFGEdge)this.edges.get(i);
            if (e.predecessorId == sourceNode.nodeId && e.successorId == sinkNode.nodeId) {
                matchingEdges.add(e);
            }
        }

        if (matchingEdges.size() > 0) {
            return (CFGEdge[])((CFGEdge[])matchingEdges.toArray(a));
        } else {
            throw new NoSuchElementException("No matching edges exist in graph");
        }
    }

    public CFGEdge[] getEdges(CFGNode n, int matchType, CFGEdge[] a) {
        List<Object> matchingEdges = new ArrayList();

        for(int i = 0; i < this.edges.size(); ++i) {
            CFGEdge e = (CFGEdge)this.edges.get(i);
            if (matchType == 0 && e.predecessorId == n.nodeId) {
                matchingEdges.add(e);
            } else if (matchType == 1 && e.successorId == n.nodeId) {
                matchingEdges.add(e);
            }
        }

        if (matchingEdges.size() > 0) {
            return (CFGEdge[])((CFGEdge[])matchingEdges.toArray(a));
        } else {
            return null;
        }
    }

    public List<CFGEdge> getEdges(CFGNode n, int matchType) {
        List<CFGEdge> matchingEdges = new ArrayList();

        for(int i = 0; i < this.edges.size(); ++i) {
            CFGEdge e = (CFGEdge)this.edges.get(i);
            if (matchType == 0 && e.predecessorId == n.nodeId) {
                matchingEdges.add(e);
            } else if (matchType == 1 && e.successorId == n.nodeId) {
                matchingEdges.add(e);
            }
        }

        return matchingEdges;
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getEdgeCount() {
        return this.edges.size();
    }

    protected void clear() {
        this.nodes.clear();
        this.edges.clear();
    }

    public CFGNode[] getNodes() {
        CFGNode[] b = new CFGNode[this.nodes.size()];
        this.nodes.toArray(b);
        return b;
    }

    public CFGNode getNodeById(int id) {
        CFGNode node = (CFGNode)this.idNodeMap.get(id);
        if (node == null) {
            throw new NoSuchElementException();
        } else {
            return node;
        }
    }

    public CFGNode getNodeByStartOffset(int startOffset) {
        Iterator<CFGNode> iterator = this.nodes.iterator();
        int var3 = this.nodes.size();

        CFGNode node;
        do {
            if (var3-- <= 0) {
                throw new NoSuchElementException();
            }

            node = (CFGNode)iterator.next();
        } while(node.getStartOffset() != startOffset);

        return node;
    }

    protected void addEdge(CFGEdge e) {
        this.edges.add(e);
        CFGNode fromNode = this.getNodeById(e.getPredecessorId());
        CFGNode toNode = this.getNodeById(e.getSuccessorId());
        fromNode.addSuccessor(toNode);
        toNode.addPredecessor(fromNode);
    }

    protected void addNode(CFGNode newNode) {
        this.nodes.add(newNode);
        this.nodeOffsetMap.put(newNode.getStartOffset(), newNode);
        this.updateIdNodeMapping(newNode);
        int startSourceLineNumber = newNode.getStartSourceLineNumber();
        int endSourceLineNumber = newNode.getEndSourceLineNumber();
        if (startSourceLineNumber > -1) {
            for(int i = startSourceLineNumber; i <= endSourceLineNumber; ++i) {
                Set<CFGNode> nodeSet = (Set)this.nodeSourceLineMap.get(i);
                if (nodeSet == null) {
                    nodeSet = new HashSet();
                    this.nodeSourceLineMap.put(i, nodeSet);
                }

                ((Set)nodeSet).add(newNode);
            }
        }

    }

    protected void addVirtualNode(CFGNode n, boolean isRootNode) {
        this.nodes.add(n);
        n.setVirtual(true);
        n.setRootNode(isRootNode);
        this.updateIdNodeMapping(n);
    }

    private void updateIdNodeMapping(CFGNode newNode) {
        if (this.idNodeMap.containsKey(newNode.getId())) {
            throw new RuntimeException("Node id " + newNode.getId() + " does already exist!");
        } else {
            this.idNodeMap.put(newNode.getId(), newNode);
        }
    }

    public String getMethodName() {
        return this.completeMethodName;
    }

    public boolean isReachable(int fromId, int toId) {
        return this.isReachable(this.getNodeById(fromId), this.getNodeById(toId));
    }

    public Set<CFGNode> getNodesBySourceLineNumber(int sourceLineNumber) {
        return (Set)this.nodeSourceLineMap.get(sourceLineNumber);
    }

    public boolean isReachable(CFGNode from, CFGNode to) {
        if (from == to) {
            return true;
        } else {
            Set<CFGNode> seen = new HashSet();
            Set<CFGNode> toCheck = new HashSet();
            seen.add(from);
            toCheck.add(from);

            while(true) {
                Set<CFGNode> newNodes = new HashSet();
                Iterator var6 = toCheck.iterator();

                while(var6.hasNext()) {
                    CFGNode n = (CFGNode)var6.next();
                    newNodes.addAll(n.getSuccessors());
                }

                if (seen.containsAll(newNodes)) {
                    return false;
                }

                seen.addAll(newNodes);
                if (seen.contains(to)) {
                    return true;
                }

                toCheck = newNodes;
            }
        }
    }

    public int distance(int fromId, int toId) {
        return this.distance(this.getNodeById(fromId), this.getNodeById(toId));
    }

    public Integer distance(CFGNode from, CFGNode to) {
        int distance = 0;
        if (from == to) {
            return distance;
        } else {
            Set<CFGNode> seen = new HashSet();
            Set<CFGNode> toCheck = new HashSet();
            seen.add(from);
            toCheck.add(from);

            while(true) {
                ++distance;
                Set<CFGNode> newNodes = new HashSet();
                Iterator var7 = toCheck.iterator();

                while(var7.hasNext()) {
                    CFGNode n = (CFGNode)var7.next();
                    newNodes.addAll(n.getSuccessors());
                }

                if (seen.containsAll(newNodes)) {
                    return null;
                }

                seen.addAll(newNodes);
                if (seen.contains(to)) {
                    return distance;
                }

                toCheck = newNodes;
            }
        }
    }

    public void addBranch(int nodeId, int pos) {
        this.branchNodeMap.put(pos, nodeId);
        this.nodeBranchMap.put(nodeId, pos);
    }

    public void addCall(int nodeId, String methodName) {
        if (this.callNodeMap.containsKey(methodName)) {
            ((Set)this.callNodeMap.get(methodName)).add(nodeId);
        } else {
            Set<Integer> nodes = new HashSet();
            nodes.add(nodeId);
            this.callNodeMap.put(methodName, nodes);
        }

        this.invokedMethods.add(methodName);
    }

    public int getBranchNode(int pos) {
        return this.branchNodeMap.containsKey(pos) ? (Integer)this.branchNodeMap.get(pos) : -1;
    }

    public int getNodeBranch(int nodeId) {
        return this.nodeBranchMap.containsKey(nodeId) ? (Integer)this.nodeBranchMap.get(nodeId) : -1;
    }

    public void clearCoverage() {
        this.edgeCoverageMap.clear();
        this.nodeCoverageMap.clear();
    }

    public Map<Integer, Integer> getEdgeCoverage() {
        return this.edgeCoverageMap;
    }

    public void setEdgeCoverage(Map<Integer, Integer> coverage) {
        this.edgeCoverageMap = coverage;
    }

    public Map<Integer, Integer> getNodeCoverage() {
        return this.nodeCoverageMap;
    }

    public void setNodeCoverage(Map<Integer, Integer> coverage) {
        this.nodeCoverageMap = coverage;
    }

    public String toString() {
        return this.edgesToString() + this.nodesToString();
    }

    private String edgesToString() {
        StringBuilder sb = new StringBuilder();
        Iterator var2 = this.edges.iterator();

        while(var2.hasNext()) {
            CFGEdge edge = (CFGEdge)var2.next();
            sb.append(edge + "\n");
        }

        return sb.toString();
    }

    private String nodesToString() {
        StringBuilder sb = new StringBuilder();
        Iterator var2 = this.nodes.iterator();

        while(var2.hasNext()) {
            CFGNode node = (CFGNode)var2.next();
            sb.append(node + "\n");
        }

        return sb.toString();
    }

    public Set<Integer> getInvocationNodesByTargetMethod(String targetMethodName) {
        return (Set)this.callNodeMap.get(targetMethodName);
    }
}
