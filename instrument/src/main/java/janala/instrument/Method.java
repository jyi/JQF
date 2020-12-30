package janala.instrument;

public class Method {
    private final String owner;
    private final String name;
    private final String desc;

    public Method(String owner, String name, String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Method)) return false;
        Method m2 = (Method) other;
        return owner.equals(m2.owner) && name.equals(m2.name) && desc.equals(m2.desc);
    }
}
