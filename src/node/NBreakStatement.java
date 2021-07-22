package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;
import util.Pair;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class NBreakStatement extends NStatement {
    public NBreakStatement() {
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Break");
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        Vector<Map<String, VarInfo>> tmp1 = new Vector<>();

//        Vector<Vector<Map<String, VarInfo>>> tmp1 = new Vector<>();
//        for(Vector<Map<String, VarInfo>> j : ctx_do_fake.symbol_table){
//            Vector<Map<String, VarInfo>> tmp2 = new Vector<>();
        for (Map<String, VarInfo> k : ctx.symbol_table) {
            Map<String, VarInfo> tmp3 = new HashMap<>();
            for (Map.Entry<String, VarInfo> l : k.entrySet()) {
                VarInfo tv = l.getValue().clone();
                tmp3.put(l.getKey(), tv);
            }
            tmp1.add(tmp3);
        }
        ctx.loop_break_symbol_snapshot.peek().add(tmp1);
        Map<Pair, String> top = ctx.loop_break_phi_move.peek();
        for (Pair i : top.keySet()) {
            ir.add(new IR(
                    IR.OpCode.MOV,
                    new OpName(top.get(i)),
                    new OpName(ctx.symbol_table.get(i.getFirst()).get(i.getSecond()).name),
                    ""
            ));
        }
        ir.add(new IR(IR.OpCode.JMP, "LOOP_" + ctx.loop_label.peek() + "_END"));
    }
}
