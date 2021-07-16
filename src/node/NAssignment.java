package node;

import java.io.PrintStream;

public class NAssignment extends NStatement {
    public NIdentifier lhs;
    public NExpression rhs;

    public NAssignment() {

    }

    public NAssignment(NIdentifier lhs, NExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("Assignment");
        lhs.print(indentation+1, false,out);
        rhs.print(indentation+1, true,out);
    }
}
