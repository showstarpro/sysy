package node;

import java.io.PrintStream;
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
}
