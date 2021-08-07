package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import parser.sym;

import java.io.PrintStream;
import java.util.List;

public class NUnaryExpression extends NExpression {
    public int op;
    public NExpression rhs;

    public NUnaryExpression() {

    }

    public NUnaryExpression(int op, NExpression rhs) {
        this.op = op;
        this.rhs = rhs;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.print("UnaryExpression OP: ");
        out.println(this.op);
        this.rhs.print(indentation + 1, end, out);
    }

    public int eval(ContextIR ctx) throws Exception {
        switch (this.op) {
            case sym.PLUS:
                return rhs.eval(ctx);

            case sym.MINUS:
                return -rhs.eval(ctx);
            case sym.NOT:
                return (rhs.eval(ctx) != 0) ? 0 : 1;
            default:
                throw new Exception("Unknow OP");

        }
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        //未考虑optimize
        OpName dest=new OpName("%"+ctx.get_id());
        switch (this.op){
            case sym.PLUS:
                return rhs.eval_runtime(ctx,ir);
            case sym.MINUS:
                ir.add(new IR(IR.OpCode.SUB,dest,new OpName(0),rhs.eval_runtime(ctx,ir),""));
                return dest;
            case sym.NOT:
                ir.add(new IR(IR.OpCode.CMP, new OpName(),new OpName(0),rhs.eval_runtime(ctx,ir),""));
                ir.add(new IR(IR.OpCode.MOVEQ,dest,new OpName(1),new OpName(0),""));
//                ir.add(new IR(IR.OpCode.MOVNE,dest,new OpName(0),new OpName(1),""));
                return dest;
            default:
                throw new Exception("Unknow OP");
//                break;
        }

    }
}
