package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;
import parser.*;

import java.awt.*;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NFunctionDefine extends Node {
    public int return_type;
    public NIdentifier name;
    public NFunctionDefineArgList args;
    public NBlock body;

    public NFunctionDefine() {
    }

    public NFunctionDefine(int return_type, NIdentifier name, NFunctionDefineArgList args, NBlock body) {
        this.args = args;
        this.return_type = return_type;
        this.name = name;
        this.body = body;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("FunctionDefine");

        this.printIndentation(indentation + 1, false, out);
        out.println("Return Type: " + return_type);

        this.printIndentation(indentation + 1, false, out);
        out.println("Name");
        name.print(indentation + 2, true, out);

        this.printIndentation(indentation + 1, false, out);
        out.println("Args");
        args.print(indentation + 2, true, out);

        this.printIndentation(indentation + 1, true, out);
        out.println("Body");
        body.print(indentation + 2, true, out);
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        // new scope
        ctx.create_scope();
        int arg_len = this.args.list.size();
        ir.add(new IR(IR.OpCode.FUNCTION_BEGIN, new OpName(), new OpName(arg_len), this.name.name));

        // args
        for (int i = 0; i < arg_len; i++) {
            NIdentifier identifier = this.args.list.get(i).name;
            if (identifier instanceof NArrayIdentifier) {
                Vector<Integer> shape = new Vector<>();
                for (NExpression j : ((NArrayIdentifier) identifier).shape) {
                    shape.addElement(j.eval(ctx));
                }
                String tmp = "%" + ctx.get_id();
                ir.add(new IR(IR.OpCode.MOV, new OpName(tmp), new OpName("$arg" + i), ""));
                ctx.insert_symbol(((NArrayIdentifier) identifier).name.name, new VarInfo(tmp, true, shape));
                ir.add(new IR(IR.OpCode.INFO, "NOT CONSTEXPR"));

            } else {
                String tmp = "%" + ctx.get_id();
                ir.add(new IR(IR.OpCode.MOV, new OpName(tmp), new OpName("$arg" + i), ""));
                ctx.insert_symbol(this.args.list.get(i).name.name, new VarInfo(tmp, false, new Vector<>()));
            }
        }
        // body
        this.body.generate_ir(ctx, ir);

        // return type
        if (this.return_type == sym.INT) {
            ir.add(new IR(IR.OpCode.RET, new OpName(0),""));
        } else {
            ir.add(new IR(IR.OpCode.RET, ""));
        }
        ir.add(new IR(IR.OpCode.FUNCTION_END, this.name.name));
        ctx.end_scope();
    }
}
