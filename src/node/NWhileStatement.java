package node;

import java.io.PrintStream;

public class NWhileStatement extends NStatement {
    public NConditionExpression cond;
    public NStatement dostmt;

    public NWhileStatement() {
    }

    public NWhileStatement(NConditionExpression cond, NStatement dostmt) {
        this.cond = cond;
        this.dostmt = dostmt;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println( "WhileStatement" );

        this.printIndentation(indentation + 1, false, out);
        out.println("Cond"); ;
        cond.print(indentation + 2, false, out);

        this.printIndentation(indentation + 1, false, out);
        out.println("Do"); ;
        dostmt.print(indentation + 2, false, out);
    }
}
