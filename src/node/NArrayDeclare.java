package node;

import java.io.PrintStream;

public class NArrayDeclare extends NDeclare {
    public NArrayIdentifier name;

    public NArrayDeclare() {

    }

    public NArrayDeclare(NArrayIdentifier name) {
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end, out);
        out.println("ArrayDeclare");
        name.print(indentation+1, true, out);
    }
}
