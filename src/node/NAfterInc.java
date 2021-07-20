package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;

import java.io.PrintStream;
import java.util.List;

public class NAfterInc extends NStatement {
    public int op;
    public NIdentifier lhs;

    public NAfterInc() {

    }

    public NAfterInc(NIdentifier lhs, int op) {
        this.op = op;
        this.lhs = lhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("AfterInc op:");
        lhs.print(indentation+1, false,out);
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        VarInfo v=ctx.find_symbol(this.lhs.name);
        NNumber n0=new NNumber(1);
        NBinaryExpression n1=new NBinaryExpression(lhs,this.op,n0);
        NAssignment n2=new NAssignment(lhs,n1);
        n2.eval_runtime(ctx,ir);
        return new OpName(v.name);
    }

    public void generate_ir(ContextIR ctx,List<IR> ir) throws Exception{
        NNumber n0=new NNumber(1);
        NBinaryExpression n1=new NBinaryExpression(lhs,this.op,n0);
        NAssignment n2=new NAssignment(lhs,n1);
        n2.generate_ir(ctx,ir);
    }
}

