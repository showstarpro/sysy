package node;

import java.io.PrintStream;

public class NIfElseStatement extends NStatement {
    public NExpression cond;
    public NStatement thenstmt, elsestmt;

    public NIfElseStatement() {

    }

    public NIfElseStatement(NExpression cond, NStatement thenstmt, NStatement elsestmt) {
        this.cond = cond;
        this.thenstmt = thenstmt;
        this.elsestmt = elsestmt;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("IfElseStatement");
        this.printIndentation(indentation+1, false, out);
        out.println("Cond");
        cond.print(indentation+2, false, out);

        this.printIndentation(indentation+1, false, out);
        out.println("Then");
        thenstmt.print(indentation+2, false, out);

        this.printIndentation(indentation+1, true, out);
        out.println("Else");
        elsestmt.print(indentation+2, true, out);
    }
}
