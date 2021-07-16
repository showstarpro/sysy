package node;

import java.io.PrintStream;

public class NBreakStatement extends NStatement {
    public NBreakStatement() {
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end,out);
        out.println("Break");
    }
}
