package node;

import java.io.PrintStream;

public class NIdentifier extends NExpression {
    public String name;

    public NIdentifier() {

    }

    public NIdentifier(String name) {
        this.name = name;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.print("Identifier: ");
        out.println(this.name);
    }
}
