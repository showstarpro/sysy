package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import java.io.PrintStream;
import java.util.List;

public class NReturnStatement extends NStatement {
    public NExpression value;

    public NReturnStatement() {
    }

    public NReturnStatement(NExpression value) {
        this.value = value;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Return");
        if (value != null) {
            value.print(indentation + 1, true, out);
        }
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        if (this.value != null) {
            ir.add(new IR(IR.OpCode.RET, new OpName(),
                    this.value.eval_runtime(ctx, ir), ""));
        } else {
            ir.add(new IR(IR.OpCode.RET, ""));
        }
    }
}
