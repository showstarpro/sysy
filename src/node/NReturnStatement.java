package node;

import java.io.PrintStream;

public class NReturnStatement extends NStatement {
    public NExpression value;

    public NReturnStatement() {
    }

    public NReturnStatement(NExpression value) {
        this.value = value;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Rrturn");
        if(value!=null){
            value.print(indentation+1, true, out);
        }
    }
}
