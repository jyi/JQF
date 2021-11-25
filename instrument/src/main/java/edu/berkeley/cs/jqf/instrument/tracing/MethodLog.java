package edu.berkeley.cs.jqf.instrument.tracing;

import com.dslplatform.json.JsonObject;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.StringConverter;

import java.util.ArrayList;

public class MethodLog implements JsonObject {
    public MethodLog() {
    }

    public MethodLog(String id, String caller, int Nth) {
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

    public ArrayList<String> getExe() {
        return this.exe;
    }

    @Override
    public void serialize(JsonWriter writer, boolean b) {
        writer.writeAscii("{\"id\":");
        StringConverter.serialize(id, writer);
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
