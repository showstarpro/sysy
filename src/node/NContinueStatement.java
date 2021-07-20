package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import util.Pair;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class NContinueStatement extends NStatement {
    public NContinueStatement() {
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Continue");
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        ctx.loop_continue_symbol_snapshot.peek().add(ctx.symbol_table);
        Map<Pair, String> top = ctx.loop_continue_phi_move.peek();
        for (Pair i : top.keySet()) {
            ir.add(new IR(
                    IR.OpCode.MOV,
                    new OpName(top.get(i)),
                    new OpName(ctx.symbol_table.get(i.getFirst()).get(i.getSecond()).name),
                    ""
            ));
        }
        ir.add(new IR(IR.OpCode.JMP, "LOOP_" + ctx.loop_label.peek() + "_CONTINUE"));
    }
}
