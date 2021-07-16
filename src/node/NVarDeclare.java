package node;

import java.io.PrintStream;

public class NVarDeclare extends NDeclare {
    public NIdentifier name;

    public NVarDeclare() {

    }

    public NVarDeclare(NIdentifier name) {
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Declare");
        name.print(indentation+1, true, out);
    }
}
