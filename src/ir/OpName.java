package ir;

public class OpName {
    public enum Type {
        Var,
        Imm,
        Null
    }


    public Type type;
    public String name;
    public int value;

    public OpName() {
        this.type = Type.Null;
    }

    public OpName(String name) {
        this.type = Type.Var;
        this.name = name;
    }

    public OpName(int value) {
        this.type = Type.Imm;
        this.value = value;
    }
}
