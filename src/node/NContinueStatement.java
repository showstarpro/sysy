package node;

import java.io.PrintStream;

public class NContinueStatement extends NStatement {
    public NContinueStatement() {
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Continue");
    }
}
