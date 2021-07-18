package node;

import ir.ContextIR;
import parser.sym;

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

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.print("UnaryExpression OP: ");
        out.println(this.op);
        this.rhs.print(indentation + 1, end, out);
    }

    public int eval(ContextIR ctx) throws Exception {
        switch (this.op) {
            case sym.PLUS:
                return rhs.eval(ctx);

            case sym.MINUS:
                return -rhs.eval(ctx);
            case sym.NOT:
                return (rhs.eval(ctx) != 0) ? 0 : 1;
            default:
                throw new Exception("Unknow OP");

        }
    }
}
