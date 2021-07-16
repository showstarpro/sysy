package node;

import java.io.PrintStream;
import java.util.Vector;

public class NFunctionCallArgList extends NExpression {
    public Vector<NExpression> args;

    public NFunctionCallArgList() {
        args = new Vector<>();
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("FunctionCallArgList");
        int j=0;
        for(NExpression i:args)
        {
            i.print(indentation+1, (j+1)==args.size(),out);
            j++;
        }
    }
}
