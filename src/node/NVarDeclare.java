package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NVarDeclare extends NDeclare {
    public NIdentifier name;

    public NVarDeclare() {

    }

    public NVarDeclare(NIdentifier name) {
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Declare");
        name.print(indentation + 1, true, out);
    }

    @Override
    /**
     * 变量声明语句的IR
     */
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        if (ctx.is_global()) {
            // 全局变量加入data段
            ir.add(new IR(IR.OpCode.DATA_BEGIN, "@" + this.name.name));
            ir.add(new IR(IR.OpCode.DATA_WORD, new OpName(0), ""));
            ir.add(new IR(IR.OpCode.DATA_END, ""));
            // 插入到符号表
            ctx.insert_symbol(this.name.name, new VarInfo("@" + this.name.name, false, new Vector<>()));
        } else {
            ctx.insert_symbol(this.name.name,
                    new VarInfo("%" + ctx.get_id(), false, new Vector<>()));
        }
    }
}
