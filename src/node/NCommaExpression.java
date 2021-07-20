package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import javax.naming.Context;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NCommaExpression extends NExpression {
    public Vector<NExpression> values;

    public NCommaExpression() {
        values = new Vector<>();
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("CommaExpression");
        for(NExpression v:values)
        {
            v.print(indentation+1, end,out);
        }   
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        OpName ret=new OpName();
        for(NExpression v: values){
            ret=v.eval_runtime(ctx,ir);
        }
        return ret;
    }
}
