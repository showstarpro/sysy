package node;

import java.io.PrintStream;

public class NBinaryExpression extends NExpression {
    public int op;
    public NExpression lhs, rhs;

    public NBinaryExpression() {

    }

    public NBinaryExpression(NExpression lhs, int op, NExpression rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.print("BinaryExpression OP: ");
        out.println(this.op);
        this.lhs.print(indentation+1, end,out);
        this.rhs.print(indentation+1, end,out);
    }
}
