package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import java.io.PrintStream;
import java.util.List;

public class NEvalStatement extends NStatement {
    public NExpression value;

    public NEvalStatement() {
    }

    public NEvalStatement(NExpression value) {
        this.value = value;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Eval");
        value.print(indentation+1, true, out);
    }

    public int eval(ContextIR ctx) throws Exception {
        return this.value.eval(ctx);
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        return this.value.eval_runtime(ctx,ir);
    }
    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        this.value.eval_runtime(ctx,ir);
    }
}
