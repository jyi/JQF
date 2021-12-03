package edu.berkeley.cs.jqf.instrument.tracing;

import com.dslplatform.json.JsonObject;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.StringConverter;

import java.util.ArrayList;

public class MethodLog implements JsonObject {
    public MethodLog() {
    }

    public MethodLog(String id, String name, String caller, int Nth) {
        this.id = id;
        this.name = name;
        this.caller = caller;
        this.Nth = Nth;
    }

    public String id = new String();
    public int Nth = 0;
    public String caller = new String();
    public String name = new String();
    public ArrayList<String> exe = new ArrayList<>();

    public void addExe(String newLine) {
        if(exe.size() > 0) {
            if (newLine.equals(exe.get(exe.size()-1))) {
                return;
            }
        }

        this.exe.add(newLine);
    }

    public ArrayList<String> getExe() {
        return this.exe;
    }

    @Override
    public void serialize(JsonWriter writer, boolean b) {
        writer.writeAscii("{\"id\":");
        StringConverter.serialize(id, writer);
        writer.writeAscii(",\"Name\":");
        StringConverter.serialize(name, writer);
        writer.writeAscii(",\"Nth\":");
        NumberConverter.serialize(Nth, writer);
        writer.writeAscii(",\"Caller\":");
        StringConverter.serialize(caller, writer);
        writer.writeAscii(",\"Exe\":");
        StringConverter.serialize(exe, writer);
        writer.writeAscii("}");
        writer.writeAscii("\n");
    }
}
