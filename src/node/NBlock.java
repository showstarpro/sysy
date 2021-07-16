package node;

import java.io.PrintStream;
import java.util.Vector;

public class NBlock extends NStatement {
    public Vector<NStatement> statements;

    public NBlock() {
        statements = new Vector<>();
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("Block");
        int j=0;
        for(NStatement i:statements)
        {
            i.print(indentation+1, (j+1)==statements.size(),out);
            j++;
        }
    }
}
