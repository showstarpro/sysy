package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import java.util.List;

public class NStatement extends NExpression {
    public NStatement() {
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        this.generate_ir(ctx,ir);
        return new OpName();
    }
}
