package node;

import java.io.PrintStream;

public class NIfStatement extends NStatement {
    public NExpression cond;
    public NStatement thenstmt;

    public NIfStatement() {
    }

    public NIfStatement(NExpression cond, NStatement thenstmt) {
        this.cond = cond;
        this.thenstmt = thenstmt;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end,out);
        out.println( "IfStatement");

        this.printIndentation(indentation + 1, false, out);
        out.println("Cond");
        cond.print(indentation + 2, false, out);

        this.printIndentation(indentation + 1, false, out);
        out.println("Then");
        thenstmt.print(indentation + 2, false, out);
    }
}
