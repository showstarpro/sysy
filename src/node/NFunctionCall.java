package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;

import javax.naming.Context;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NFunctionCall extends NExpression {
    public NIdentifier name;
    public NFunctionCallArgList args;

    public NFunctionCall() {

    }

    public NFunctionCall(NIdentifier name, NFunctionCallArgList args) {
        this.args = args;
        this.name = name;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("FunctionCall");
        name.print(indentation+1, false,out);
        args.print(indentation+1, false,out);
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        Vector<OpName> list=new Vector<OpName>();
        OpName dest=new OpName("%"+ctx.get_id());
        for(int i=0;i<this.args.args.size();i++){
            list.addElement(this.args.args.get(i).eval_runtime(ctx,ir));
        }
        for(int i=this.args.args.size()-1;i>=0;i--){
            ir.add(new IR(IR.OpCode.SET_ARG,new OpName(i),list.get(i),""));
        }
        ir.add(new IR(IR.OpCode.CALL,dest,this.name.name));
        return dest;
    }
}
