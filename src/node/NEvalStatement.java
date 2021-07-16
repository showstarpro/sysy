package node;

import java.io.PrintStream;

public class NEvalStatement extends NStatement {
    public NExpression value;

    public NEvalStatement() {
    }

    public NEvalStatement(NExpression value) {
        this.value = value;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Eval");
        value.print(indentation+1, true, out);
    }
}
