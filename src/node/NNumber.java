package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import java.io.PrintStream;
import java.util.List;

public class NNumber extends NExpression {
    public int value;

    public NNumber() {

    }

    public NNumber(String value) {
        this.value = Integer.parseUnsignedInt(value);
    }

    public NNumber(int value) {
        this.value = value;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("Number: "+value);
    }

    public int eval(ContextIR ctx)
    {
        return this.value;
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) {
        return new OpName(this.value);
    }
}
