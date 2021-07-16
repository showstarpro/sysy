package node;

import java.io.PrintStream;

public class NArrayDeclareWithInit extends NDeclare {
    public NArrayIdentifier name;
    public NArrayDeclareInitValue value;
    public boolean is_const;

    public NArrayDeclareWithInit() {
    }

    public NArrayDeclareWithInit(NArrayIdentifier name, NArrayDeclareInitValue value, boolean is_const) {
        this.name = name;
        this.value = value;
        this.is_const = is_const;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end, out);
        out.println("ArrayDeclareWithInit");
        name.print(indentation+1, false, out);
        value.print(indentation+1, true, out);
    }
}
