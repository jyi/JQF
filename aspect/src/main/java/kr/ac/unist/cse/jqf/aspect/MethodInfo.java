package kr.ac.unist.cse.jqf.aspect;

import java.util.Objects;

public class MethodInfo {
    private String typeName;
    private String methodName;

    public MethodInfo(String typeName, String methodName) {
        this.typeName = typeName;
        this.methodName = methodName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return getTypeName().equals(that.getTypeName()) &&
                getMethodName().equals(that.getMethodName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTypeName(), getMethodName());
    }
}
