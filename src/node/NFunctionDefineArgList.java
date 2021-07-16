package node;

import java.io.PrintStream;
import java.util.Vector;

public class NFunctionDefineArgList extends NExpression {
    public Vector<NFunctionDefineArg> list;

    public NFunctionDefineArgList() {
        list = new Vector<>();
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end, out);
        out.println("FunctionDefineArgList");
        for(NFunctionDefineArg i : list){
            i.print(indentation+1, list.indexOf(i)==list.size()-1, out);
        }
    }
}
