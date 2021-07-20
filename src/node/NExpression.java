package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import java.util.List;

public class NExpression extends Node {
    public NExpression() {

    }

    public int eval(ContextIR ctx) throws Exception {
        throw new Exception("can not eval this value at compile time.");

    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception {
        throw new Exception("can not eval this value at run time.");
    }
  
    public CondResult eval_cond_runntime(ContextIR ctx, List<IR> ir) throws Exception{
        CondResult ret = new CondResult();
        OpName dest=new OpName("%"+ctx.get_id());
        ir.add(new IR(IR.OpCode.CMP,dest,this.eval_runtime(ctx,ir),new OpName(0),""));
        ret.then_op=IR.OpCode.JNE;
        ret.else_op=IR.OpCode.JEQ;
        return ret;
    }

}
