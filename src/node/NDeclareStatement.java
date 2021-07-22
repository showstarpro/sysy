package node;

import ir.ContextIR;
import ir.IR;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NDeclareStatement extends NStatement {
    public Vector<NDeclare> list;
    public int type;

    public NDeclareStatement(int type) {
        this();
        this.type = type;
    }

    public NDeclareStatement() {
        list = new Vector<>();
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end,out);
        out.println("Declare Type： "+ type);
        for(NDeclare i : list){
            i.print(indentation+1, list.indexOf(i)==list.size()-1, out);
        }
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        for(NDeclare i: list){
            i.generate_ir(ctx,ir);
        }
    }
}
