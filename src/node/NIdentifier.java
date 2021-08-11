package node;

import ir.*;

import java.io.PrintStream;
import java.util.List;

public class NIdentifier extends NExpression {
    public String name;

    public NIdentifier() {

    }

    public NIdentifier(String name) {
        this.name = name;
        if(name.equals("starttime"))
            this.name = "_sysy_starttime";
        if(name.equals("stoptime"))
            this.name = "_sysy_stoptime";
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.print("Identifier: ");
        out.println(this.name);
    }

    public int eval(ContextIR ctx) throws Exception {

            ConstInfo v=ctx.find_const(this.name);
            if(v.is_array)
            {
                throw new Exception(this.name+" is a array.");
            }
            else
            {
                return v.value.firstElement();
            }
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception {
        VarInfo v=ctx.find_symbol(this.name);

        if(v.is_array)
        {
            return new OpName(v.name);
        }
        else
        {
            return new OpName(v.name);
        }
    }
}
