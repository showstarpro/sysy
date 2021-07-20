package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import java.io.PrintStream;
import java.util.List;

public class NAssignment extends NStatement {
    public NIdentifier lhs;
    public NExpression rhs;

    public NAssignment() {

    }

    public NAssignment(NIdentifier lhs, NExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("Assignment");
        lhs.print(indentation+1, false,out);
        rhs.print(indentation+1, true,out);
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        this.generate_ir(ctx,ir);
        if(this.lhs instanceof NArrayIdentifier){
            assert ir.get(ir.size()-1).op_code== IR.OpCode.STORE;
            return ir.get(ir.size()-1).op3;
        }
        else{
            assert ir.get(ir.size()-1).dest.type==OpName.Type.Var;
            return ir.get(ir.size()-1).dest;
        }
    }
}
