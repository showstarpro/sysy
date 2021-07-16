package node;

import java.io.PrintStream;
import java.util.Vector;

public class NArrayDeclareInitValue extends NExpression {
    public boolean is_number;
    public NExpression value;
    public Vector<NArrayDeclareInitValue> value_list;

    public NArrayDeclareInitValue() {
        value_list = new Vector<>();
    }

    public NArrayDeclareInitValue(boolean is_number, NExpression value) {
        this();
        this.is_number = is_number;
        this.value = value;
    }

    @Override
    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("ArrayDeclareInitValue");
        if(is_number){
            value.print(indentation+1, true, out);
        }else{
            for(NArrayDeclareInitValue i: value_list){
                i.print(indentation+1, value_list.indexOf(i)==value_list.size()-1, out);
            }
        }
    }
}
