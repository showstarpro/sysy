package node;

import ir.*;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NVarDeclareWithInit extends NDeclare {
    public NIdentifier name;
    public NExpression value;
    public boolean is_const;

    public NVarDeclareWithInit() {
    }

    public NVarDeclareWithInit(NIdentifier name, NExpression value, boolean is_const) {
        this.name = name;
        this.value = value;
        this.is_const = is_const;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("DeclareWithInit");
        name.print(indentation + 1, false, out);
        value.print(indentation + 1, true, out);
    }

    @Override
    /**
     * 带初始化变量的声明的IR
     */
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        if (ctx.is_global()) {
            ir.add(new IR(IR.OpCode.DATA_BEGIN, "@" + this.name.name));
            ir.add(new IR(IR.OpCode.DATA_WORD, new OpName(this.value.eval(ctx)), ""));
            ir.add(new IR(IR.OpCode.DATA_END, ""));
            ctx.insert_symbol(this.name.name, new VarInfo("@" + this.name.name, false, new Vector<>()));
            if (this.is_const) {
                ctx.insert_const(this.name.name, new ConstInfo(this.value.eval(ctx)));
            }
        } else {
            ctx.insert_symbol(this.name.name, new VarInfo("%" + ctx.get_id(), false, new Vector<>()));
            new NAssignment(this.name, this.value).generate_ir(ctx, ir);
            if (this.is_const) {
                ctx.insert_const(this.name.name, new ConstInfo(this.value.eval(ctx)));
            }
        }
    }
}
