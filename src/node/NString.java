package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NString extends NExpression{
    public String string;
    public NString(){

    }

    public NString(String string){
        this.string = string;
    }

    @Override
    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("NString: "+string);
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) {
        return null;
    }
}


