package node;

import java.io.PrintStream;

public class NAfterInc extends NStatement {
    public int op;
    public NIdentifier lhs;

    public NAfterInc() {

    }

    public NAfterInc(NIdentifier lhs, int op) {
        this.op = op;
        this.lhs = lhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("AfterInc op:");
        lhs.print(indentation+1, false,out);
    }
}

