package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NArrayDeclare extends NDeclare {
    public NArrayIdentifier name;

    public NArrayDeclare() {

    }

    public NArrayDeclare(NArrayIdentifier name) {
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("ArrayDeclare");
        name.print(indentation + 1, true, out);
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        Vector<Integer> shape = new Vector<>();
        for (NExpression i : this.name.shape) {
            shape.addElement(i.eval(ctx));
        }
        int size = 1;
        for (Integer i : shape) {
            size *= i;
        }
        if (ctx.is_global()) {
            ir.add(new IR(IR.OpCode.DATA_BEGIN, "@&" + this.name.name.name));
            ir.add(new IR(IR.OpCode.DATA_SPACE, new OpName(size * 4), ""));
            ir.add(new IR(IR.OpCode.DATA_END, ""));
            ctx.insert_symbol(this.name.name.name, new VarInfo("@&" + this.name.name.name, true, shape));
        } else {
            ctx.insert_symbol(this.name.name.name, new VarInfo("%&" + ctx.get_id(), true, shape));
            ir.add(new IR(IR.OpCode.MALLOC_IN_STACK,
                    new OpName(ctx.find_symbol(this.name.name.name).name),
                    new OpName(size * 4),
                    ""));
        }
    }
}
