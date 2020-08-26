//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.hub.se.cfg;

import java.io.Serializable;

public class CFGEdge implements Serializable {
    private static final long serialVersionUID = 1515201509852115253L;
    private static int globalCurrentEdgeID = 0;
    protected int edgeId = generateNewEdgeID();
    protected int successorId;
    protected int predecessorId;
    protected int choice;

    public static int generateNewEdgeID() {
        return globalCurrentEdgeID++;
    }

    public CFGEdge(int successorId, int predecessorId, int choice) {
        this.successorId = successorId;
        this.predecessorId = predecessorId;
        this.choice = choice;
    }

    public int getId() {
        return this.edgeId;
    }

    public void setSuccessorId(int succId) {
        this.successorId = succId;
    }

    public int getSuccessorId() {
        return this.successorId;
    }

    public void setPredecessorId(int predId) {
        this.predecessorId = predId;
    }

    public int getPredecessorId() {
        return this.predecessorId;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

    public int getChoice() {
        return this.choice;
    }

    public String toString() {
        return new String("(" + this.predecessorId + " -> " + this.successorId) + ", " + this.choice + ")";
    }
}
