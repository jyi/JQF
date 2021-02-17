package edu.berkeley.cs.jqf.fuzz.util;

public class EventInfo {
    public String filename;
    public int line;
    public int hash;
    public EventInfo(String filename, int line, int hash) {
        this.filename = filename;
        this.line = line;
        this.hash = hash;
    }
    public String getFileAndLine() {
        return this.filename + ":" + this.line;
    }
    @Override
    public boolean equals(Object obj) {
        EventInfo other = (EventInfo) obj;
        return this.toString().equals(other.toString());
    }
    @Override
    public String toString() {
        return filename + ":" + line + ":" + hash ;
    }
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
