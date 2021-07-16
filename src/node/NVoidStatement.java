package node;

import java.io.PrintStream;

public class NVoidStatement extends NStatement {
    public NVoidStatement() {

    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Void");
    }
}
