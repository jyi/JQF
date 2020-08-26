//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.hub.se.cfg;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CFGAnalysis implements Serializable {
    private static final long serialVersionUID = -820897258149888704L;
    Map<String, CFG> cfgMap;
    Set<String> skippedFilesDuringAnalysis;
    Map<String, Set<CFGNode>> callerCache = new HashMap();
    Set<CFGTarget> targets;

    public CFGAnalysis(Map<String, CFG> cfgMap, Set<String> skippedFilesDuringAnalysis) {
        this.cfgMap = cfgMap;
        this.skippedFilesDuringAnalysis = skippedFilesDuringAnalysis;
        this.targets = new HashSet();
    }

    public Collection<CFG> getAllIncludedCFG() {
        return this.cfgMap.values();
    }

    public Set<CFGTarget> getProcessedTargets() {
        return this.targets;
    }

    public boolean wasClassSkippedInCFGBuilding(String className) {
        return this.skippedFilesDuringAnalysis.contains(className);
    }

    public void calculateDistancesToTargets(Set<String> setOfTargets) {
        Iterator var2 = setOfTargets.iterator();

        while(var2.hasNext()) {
            String target = (String)var2.next();
            String[] separatedArgument = target.split(":");
            String targetMethod = separatedArgument[0];
            int targetSourceLine = Integer.parseInt(separatedArgument[1]);
            CFGTarget cfgTarget = new CFGTarget(targetMethod, targetSourceLine);
            if (!this.targets.add(cfgTarget)) {
                return;
            }

            CFGNode targetNode = this.getNodeByMethodAndSourceLine(targetMethod, targetSourceLine);
            int globalTargetNodeId = targetNode.getId();
            targetNode.setDistance(globalTargetNodeId, 0);
            this.updateNodeAndAllPredecessorNodes(targetNode, globalTargetNodeId, true);
        }

    }

    private int updateNodeAndAllPredecessorNodes(CFGNode node, int targetId, boolean includeMethodCallers) {
        Set<CFGNode> toCheck = new HashSet();
        toCheck.add(node);
        int lastDistance = node.getDistance(targetId);

        while(true) {
            CFGNode currentNode;
            int currentDistance;
            Set predecessorNodes;
            while(true) {
                if (toCheck.isEmpty()) {
                    return lastDistance;
                }

                currentNode = (CFGNode)toCheck.iterator().next();
                toCheck.remove(currentNode);
                currentDistance = currentNode.getDistance(targetId);
                lastDistance = currentDistance;
                if (currentNode.isRootNode) {
                    if (!includeMethodCallers) {
                        continue;
                    }

                    predecessorNodes = this.getCallers(currentNode.getFullQualifiedMethodName());
                    break;
                }

                predecessorNodes = currentNode.getPredecessors();
                break;
            }

            Iterator var9 = predecessorNodes.iterator();

            while(var9.hasNext()) {
                CFGNode preNode = (CFGNode)var9.next();
                int newDistance = currentDistance;
                if (preNode.isCallerNode()) {
                    Set<String> methodsCalledByPreNode = preNode.getMethodsCalled();
                    if (!this.isNotLastNodeInMethod(preNode)) {
                        methodsCalledByPreNode.remove(currentNode.getFullQualifiedMethodName());
                    }

                    Iterator var13 = methodsCalledByPreNode.iterator();

                    while(var13.hasNext()) {
                        String callingMethod = (String)var13.next();
                        if (this.isMethodIncludedInAnalysis(callingMethod)) {
                            CFGNode lastNodeInCalledMethod = this.getLastNodeForMethod(callingMethod);
                            boolean distanceUpdated = lastNodeInCalledMethod.setDistanceIfBetter(targetId, currentDistance);
                            if (distanceUpdated) {
                                newDistance = this.updateNodeAndAllPredecessorNodes(lastNodeInCalledMethod, targetId, false);
                            }
                        }
                    }
                }

                if (currentNode.isRootNode || !preNode.isVirtual) {
                    ++newDistance;
                }

                boolean distanceUpdated = preNode.setDistanceIfBetter(targetId, newDistance);
                if (distanceUpdated) {
                    toCheck.add(preNode);
                }
            }
        }
    }

    public Set<CFGNode> getCallers(String localTargetMethod) {
        Set<CFGNode> callingNodes = (Set)this.callerCache.get(localTargetMethod);
        if (callingNodes == null) {
            callingNodes = new HashSet();
            Iterator var3 = this.cfgMap.values().iterator();

            while(true) {
                CFG cfg;
                Set invocationNodes;
                do {
                    do {
                        if (!var3.hasNext()) {
                            return (Set)callingNodes;
                        }

                        cfg = (CFG)var3.next();
                    } while(cfg.getMethodName().equals(localTargetMethod));

                    invocationNodes = cfg.getInvocationNodesByTargetMethod(localTargetMethod);
                } while(invocationNodes == null);

                Iterator var6 = invocationNodes.iterator();

                while(var6.hasNext()) {
                    Integer nodeId = (Integer)var6.next();
                    CFGNode caller = cfg.getNodeById(nodeId);
                    ((Set)callingNodes).add(caller);
                }
            }
        } else {
            return (Set)callingNodes;
        }
    }

    public CFGNode getRootNodeForCurrentMethod(String fullQualifiedMethodName) {
        CFG cfg = (CFG)this.cfgMap.get(fullQualifiedMethodName);
        if (cfg == null) {
            throw new RuntimeException("Unknown method: " + fullQualifiedMethodName);
        } else {
            return cfg.getRootNode();
        }
    }

    public CFGNode getLastNodeForMethod(String fullQualifiedMethodName) {
        CFG cfg = (CFG)this.cfgMap.get(fullQualifiedMethodName);
        if (cfg == null) {
            throw new RuntimeException("Unknown method: " + fullQualifiedMethodName);
        } else {
            return cfg.getExitNode();
        }
    }

    public CFGNode getNodeByMethodAndSourceLine(String fullQualifiedMethodName, int sourceLineNumber, boolean muteExceptionForUnknownMethod) {
        CFG cfg = (CFG)this.cfgMap.get(fullQualifiedMethodName);
        if (cfg == null) {
            if (muteExceptionForUnknownMethod) {
                return null;
            } else {
                throw new RuntimeException("Unknown method: " + fullQualifiedMethodName);
            }
        } else {
            Set<CFGNode> nodes = cfg.getNodesBySourceLineNumber(sourceLineNumber);
            return nodes != null && !nodes.isEmpty() ? (CFGNode)nodes.iterator().next() : null;
        }
    }

    public CFGNode getNodeByMethodAndSourceLine(String fullQualifiedMethodName, int sourceLineNumber) {
        return this.getNodeByMethodAndSourceLine(fullQualifiedMethodName, sourceLineNumber, false);
    }

    protected boolean isMethodIncludedInAnalysis(String fullQualifiedMethodName) {
        return this.cfgMap.get(fullQualifiedMethodName) != null;
    }

    protected boolean isNotLastNodeInMethod(CFGNode node) {
        CFG cfg = (CFG)this.cfgMap.get(node.getFullQualifiedMethodName());
        if (node.isVirtual) {
            return node.getId() != cfg.getExitNodeId();
        } else {
            Set<CFGNode> lastNodes = cfg.getLastRealNodeIds();
            if (lastNodes.isEmpty()) {
                throw new RuntimeException("No real last nodes in CFG?!");
            } else {
                Iterator var4 = lastNodes.iterator();

                CFGNode lastNode;
                do {
                    if (!var4.hasNext()) {
                        return true;
                    }

                    lastNode = (CFGNode)var4.next();
                } while(node.getId() != lastNode.getId());

                return false;
            }
        }
    }
}
