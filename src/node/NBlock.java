package node;

import ir.ContextIR;
import ir.IR;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NBlock extends NStatement {
    public Vector<NStatement> statements;

    public NBlock() {
        statements = new Vector<>();
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Block");
        int j = 0;
        for (NStatement i : statements) {
            i.print(indentation + 1, (j + 1) == statements.size(), out);
            j++;
        }
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        ctx.create_scope();
        for (NStatement i : this.statements) {
            i.generate_ir(ctx, ir);
        }
        ctx.end_scope();
    }
}
