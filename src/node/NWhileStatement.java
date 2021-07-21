package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;
import util.Pair;

import java.io.PrintStream;
import java.util.*;

public class NWhileStatement extends NStatement {
    public NExpression cond;
    public NStatement dostmt;

    public NWhileStatement() {
    }

    public NWhileStatement(NExpression cond, NStatement dostmt) {
        this.cond = cond;
        this.dostmt = dostmt;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("WhileStatement");

        this.printIndentation(indentation + 1, false, out);
        out.println("Cond");
        cond.print(indentation + 2, false, out);

        this.printIndentation(indentation + 1, false, out);
        out.println("Do");
        dostmt.print(indentation + 2, false, out);
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        ctx.create_scope();
        ctx.loop_label.push(String.valueOf(ctx.get_id()));
        ctx.loop_var.push(new Vector<>());

        // BEFORE
        ContextIR ctx_before = (ContextIR) ctx.clone();
        List<IR> ir_before = new ArrayList<>();

        // COND
        ContextIR ctx_cond = (ContextIR) ctx_before.clone();
        List<IR> ir_cond = new ArrayList<>();
        ir_cond.add(new IR(IR.OpCode.LABEL, "LOOP_" + ctx.loop_label.peek() + "_BEGIN"));

        CondResult cond = this.cond.eval_cond_runntime(ctx_cond, ir_cond);

        // JMP
        List<IR> ir_jmp = new ArrayList<>();
        ir_jmp.add(new IR(cond.else_op, "LOOP_" + ctx.loop_label.peek() + "_END"));

        // DO(fake)
        ContextIR ctx_do_fake = (ContextIR) ctx_cond.clone();
        List<IR> ir_do_fake = new ArrayList<>();
        ctx_do_fake.loop_continue_symbol_snapshot.push(new Vector<>());
        ctx_do_fake.loop_break_symbol_snapshot.push(new Vector<>());
        ctx_do_fake.loop_continue_phi_move.push(new HashMap<>());
        ctx_do_fake.loop_break_phi_move.push(new HashMap<>());
        this.dostmt.generate_ir(ctx_do_fake, ir_do_fake);
        ctx_do_fake.loop_continue_symbol_snapshot.peek().addElement(
                ctx_do_fake.symbol_table);

        // DO
        ContextIR ctx_do = (ContextIR) ctx_cond.clone();
        List<IR> ir_do = new ArrayList<>();
        ctx_do.loop_continue_symbol_snapshot.push(new Vector<>());
        ctx_do.loop_break_symbol_snapshot.push(new Vector<>());
        ctx_do.loop_continue_phi_move.push(new HashMap<>());
        ctx_do.loop_break_phi_move.push(new HashMap<>());
        for (int i = 0; i < ctx_do_fake.symbol_table.size(); i++) {
            for (String symbol : ctx_do_fake.symbol_table.get(i).keySet()) {
                for (int j = 0;
                     j < ctx_do_fake.loop_continue_symbol_snapshot.peek().size(); j++) {
                    if (!ctx_do_fake.symbol_table.get(i).get(symbol).
                            name.equals(ctx_do_fake.loop_continue_symbol_snapshot.peek().
                            get(j).get(i).get(symbol).name)) {
                        ctx_do.loop_continue_phi_move.peek().put(new Pair(i, symbol), "%" + ctx_do.get_id());
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < ctx_cond.symbol_table.size(); i++) {
            for (String symbol : ctx_cond.symbol_table.get(i).keySet()) {
                for (int j = 0;
                     j < ctx_do_fake.loop_break_symbol_snapshot.peek().size(); j++) {
                    if (!ctx_cond.symbol_table.get(i).get(symbol).
                            name.equals(ctx_do_fake.loop_break_symbol_snapshot.peek().
                            get(j).get(i).get(symbol).name)) {
                        ctx_do.loop_break_phi_move.peek().put(
                                new Pair(i, symbol),
                                ctx_cond.symbol_table.get(i).get(symbol).name);
                        break;
                    }
                }
            }
        }
        ir_do.add(new IR(IR.OpCode.LABEL, "LOOP_" + ctx.loop_label.peek() + "_DO"));
        this.dostmt.generate_ir(ctx_do, ir_do);
        for (Map.Entry<Pair, String> i : ctx_do.loop_continue_phi_move.peek().entrySet()) {
            ir_do.add(new IR(IR.OpCode.PHI_MOV, new OpName(i.getValue()),
                    new OpName(ctx_do.symbol_table.get(i.getKey().first).get(i.getKey().second).name), ""));

        }

        for (Map.Entry<Pair, String> i : ctx_do.loop_continue_phi_move.peek().entrySet()) {
            ctx_do.symbol_table.get(i.getKey().getFirst()).get(i.getKey().getSecond()).name
                    = i.getValue();
        }

        for (Map.Entry<Pair, String> i : ctx_do.loop_break_phi_move.peek().entrySet()) {
            ctx_cond.symbol_table.get(i.getKey().getFirst()).get(i.getKey().getSecond()).name
                    = i.getValue();
        }

        ctx_do.loop_continue_symbol_snapshot.pop();
        ctx_do.loop_break_symbol_snapshot.pop();
        ctx_do.loop_continue_phi_move.pop();
        ctx_do.loop_break_phi_move.pop();

        // Continue
        ContextIR ctx_continue = (ContextIR) ctx_do.clone();
        List<IR> ir_continue = new ArrayList<>();
        ir_continue.add(new IR(IR.OpCode.LABEL, "LOOP_" + ctx.loop_label.peek() + "_CONTINUE"));
        ir_cond.clear();
        ir_cond.add(new IR(IR.OpCode.LABEL, "LOOP_" + ctx.loop_label.peek() + "_BEGIN"));

        for (int i = 0; i < ctx_before.symbol_table.size(); i++) {
            for (Map.Entry<String, VarInfo> symbol_before :
                    ctx_before.symbol_table.get(i).entrySet()) {

                VarInfo symbol_continue = ctx_continue.symbol_table.get(i).get(symbol_before.getKey());
                if (!symbol_before.getValue().name.equals(symbol_continue.name)) {
                    String new_name = "%" + ctx_before.get_id();
                    ir_before.add(new IR(IR.OpCode.PHI_MOV, new OpName(new_name),
                            new OpName(symbol_before.getValue().name), ""));
                    ir_before.get(ir_before.size() - 1).phi_block = ir_cond.iterator(); //ir_before.back().phi_block = ir_cond.begin();
                    ir_continue.add(new IR(IR.OpCode.PHI_MOV, new OpName(new_name),
                            new OpName(symbol_continue.name), ""));
                    ctx_before.symbol_table.get(i).get(symbol_before.getKey()).name = new_name;
                    ctx_before.loop_var.peek().addElement(new_name);
                }
            }
        }
        ir_continue.add(new IR(IR.OpCode.JMP,
                "LOOP_" + ctx.loop_label.peek() + "_BEGIN"));
        ir_continue.add(new IR(IR.OpCode.LABEL,
                "LOOP_" + ctx.loop_label.peek() + "_END"));

        //////////////////////////////////////////////////////////////////////

        // COND real
        ctx_cond = ctx_before.clone();
        cond = this.cond.eval_cond_runntime(ctx_cond, ir_cond);

        // JMP real
        ir_jmp.clear();
        ir_jmp.add(new IR(cond.else_op, "LOOP_" + ctx.loop_label.peek() + "_END"));

        // DO (fake) real
        ctx_do_fake = ctx_cond.clone();
        ir_do_fake.clear();
        ctx_do_fake.loop_continue_symbol_snapshot.push(new Vector<>());
        ctx_do_fake.loop_break_symbol_snapshot.push(new Vector<>());
        ctx_do_fake.loop_continue_phi_move.push(new HashMap<>());
        ctx_do_fake.loop_break_phi_move.push(new HashMap<>());
        this.dostmt.generate_ir(ctx_do_fake, ir_do_fake);
        ctx_do_fake.loop_continue_symbol_snapshot.peek().addElement(
                ctx_do_fake.symbol_table);

        // DO real
        ctx_do = ctx_cond.clone();
        ir_do.clear();
        ir_continue.clear();
        ir_continue.add(new IR(IR.OpCode.NOOP, ""));
        List<IR> end = new ArrayList<>();
        end.add(new IR(IR.OpCode.LABEL, "LOOP_" + ctx.loop_label.peek() + "_END"));
        ctx_do.loop_continue_symbol_snapshot.push(new Vector<>());
        ctx_do.loop_break_symbol_snapshot.push(new Vector<>());
        ctx_do.loop_continue_phi_move.push(new HashMap<>());
        ctx_do.loop_break_phi_move.push(new HashMap<>());

        for (int i = 0; i < ctx_do_fake.symbol_table.size(); i++) {
            for (Map.Entry<String, VarInfo> symbol : ctx_do_fake.symbol_table.get(i).entrySet()) {
                for (int j = 0;
                     j < ctx_do_fake.loop_continue_symbol_snapshot.peek().size(); j++) {
                    if (!symbol.getValue().name.equals(
                            ctx_do_fake.loop_continue_symbol_snapshot.peek().get(j).get(i)
                                    .get(symbol.getKey()).name
                    )) {
                        ctx_do.loop_continue_phi_move.peek().put(
                                new Pair(i, symbol.getKey()), "%" + ctx_do.get_id()
                        );
                        break;
                    }
                }

            }
        }

        for (int i = 0; i < ctx_cond.symbol_table.size(); i++) {
            for (Map.Entry<String, VarInfo> symbol : ctx_cond.symbol_table.get(i).entrySet()) {
                for (int j = 0;
                     j < ctx_do_fake.loop_break_symbol_snapshot.peek().size(); j++) {
                    if (!symbol.getValue().name.equals(
                            ctx_do_fake.loop_break_symbol_snapshot.peek().get(j).get(i)
                                    .get(symbol.getKey()).name
                    )) {
                        ctx_do.loop_break_phi_move.peek().put(
                                new Pair(i, symbol.getKey()), symbol.getValue().name
                        );
                        break;
                    }
                }
            }
        }
        ir_do.add(new IR(IR.OpCode.LABEL, "LOOP_" + ctx.loop_label.peek() + "_DO"));
        this.dostmt.generate_ir(ctx_do, ir_do);
        for (Map.Entry<Pair, String> i : ctx_do.loop_continue_phi_move.peek().entrySet()) {
            ir_do.add(new IR(IR.OpCode.PHI_MOV, new OpName(i.getValue()),
                    new OpName(ctx_do.symbol_table.get(i.getKey().getFirst()).
                            get(i.getKey().getSecond()).name), ""));
            ir_do.get(ir_do.size() - 1).phi_block = end.iterator();
        }
        for (Map.Entry<Pair, String> i : ctx_do.loop_continue_phi_move.peek().entrySet()) {
            ctx_do.symbol_table.get(i.getKey().getFirst()).get(i.getKey().getSecond()).name =
                    i.getValue();
        }
        for (Map.Entry<Pair, String> i : ctx_do.loop_break_phi_move.peek().entrySet()) {
            ctx_cond.symbol_table.get(i.getKey().getFirst()).get(i.getKey().getSecond()).name =
                    i.getValue();
        }

        ctx_do.loop_continue_symbol_snapshot.pop();
        ctx_do.loop_break_symbol_snapshot.pop();
        ctx_do.loop_continue_phi_move.pop();
        ctx_do.loop_break_phi_move.pop();

        // CONTINUE real
        ctx_continue = ctx_do.clone();
        ir_continue.add(new IR(IR.OpCode.LABEL,
                "LOOP_" + ctx.loop_label.peek() + "_CONTINUE"));

        for (int i = 0; i < ctx_before.symbol_table.size(); i++) {
            for (Map.Entry<String, VarInfo> symbol_before : ctx_before.symbol_table.get(i).entrySet()) {
                VarInfo symbol_continue =
                        ctx_continue.symbol_table.get(i).get(symbol_before.getKey());
                if (!symbol_before.getValue().name.equals(symbol_continue.name)) {
                    ir_continue.add(new IR(IR.OpCode.PHI_MOV, new OpName(symbol_before.getValue().name),
                            new OpName(symbol_continue.name), ""));
                    ir_continue.get(ir_continue.size() - 1).phi_block = ir_cond.iterator();
                }
            }
        }
        ir_continue.add(new IR(IR.OpCode.JMP,
                "LOOP_" + ctx.loop_label.peek() + "_BEGIN"));

        // 为 continue 块增加假读以延长其生命周期
        // 不是很懂这个意思
        Set<String> writed = new HashSet<>();
        Vector<List<IR>> temp = new Vector<>();
        temp.addElement(ir_cond);
        temp.addElement(ir_jmp);
        temp.addElement(ir_do);
        for (List<IR> irs : temp) {
            for (IR i : irs) {
                if (i.dest.type == OpName.Type.Var) writed.add(i.dest.name);
                if (i.op1.type == OpName.Type.Var) {
                    if (!writed.contains(i.op1.name)) {
                        ir_continue.add(new IR(IR.OpCode.NOOP, new OpName(), i.op1, ""));
                    }
                }
                if (i.op2.type == OpName.Type.Var) {
                    if (!writed.contains(i.op2.name)) {
                        ir_continue.add(new IR(IR.OpCode.NOOP, new OpName(), i.op2, ""));
                    }
                }
                if (i.op3.type == OpName.Type.Var) {
                    if (!writed.contains(i.op3.name)) {
                        ir_continue.add(new IR(IR.OpCode.NOOP, new OpName(), i.op3, ""));
                    }
                }
            }
        }
        ctx = ctx_cond.clone();
        ctx.id = ctx_continue.id;
        ir.addAll(ir_before);
        ir.addAll(ir_cond);
        ir.addAll(ir_jmp);
        ir.addAll(ir_do);
        ir.addAll(ir_continue);
        ir.addAll(end);

        ctx.loop_var.pop();
        ctx.loop_label.pop();
        ctx.end_scope();
    }
}
