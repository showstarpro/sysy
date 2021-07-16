package node;

import java.io.PrintStream;

public class NUnaryExpression extends NExpression {
    public int op;
    public NExpression rhs;

    public NUnaryExpression() {

    }

    public NUnaryExpression(int op, NExpression rhs) {
        this.op = op;
        this.rhs = rhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.print("UnaryExpression OP: ");
        out.println(this.op);
        this.rhs.print(indentation+1, end,out);
    }
}
