package edu.berkeley.cs.jqf.instrument.tracing;

import com.dslplatform.json.JsonObject;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.StringConverter;

public class methodCallInfo implements JsonObject {
    public methodCallInfo() {
    }

    public methodCallInfo(String methodID, String fileName, String methodName, String descInfo) {
        this.methodID = methodID;
        this.fileName = fileName;
        this.methodName = methodName;
        this.descInfo = descInfo;
    }

    public String methodID = new String();
    public String fileName = new String();
    public String methodName = new String();
    public String descInfo = new String();
    public int callCount = 0;

    public void incCallCount() {
        this.callCount++;
    }

    @Override
    public void serialize(JsonWriter writer, boolean b) {
        writer.writeAscii("{\"id\":");
        StringConverter.serialize(methodID, writer);
        writer.writeAscii(",\"Name\":");
        StringConverter.serialize(methodName, writer);
        writer.writeAscii(",\"file\":");
        StringConverter.serialize(fileName, writer);
        writer.writeAscii("}");
    }
}
