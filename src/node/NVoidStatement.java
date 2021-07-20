package node;

import ir.ContextIR;
import ir.IR;

import java.io.PrintStream;
import java.util.List;

public class NVoidStatement extends NStatement {
    public NVoidStatement() {

    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Void");
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {

    }
}
