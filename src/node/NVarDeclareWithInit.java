package node;

import java.io.PrintStream;

public class NVarDeclareWithInit extends NDeclare {
    public NIdentifier name;
    public NExpression value;
    public boolean is_const;

    public NVarDeclareWithInit() {
    }

    public NVarDeclareWithInit(NIdentifier name, NExpression value, boolean is_const) {
        this.name = name;
        this.value = value;
        this.is_const = is_const;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation ,end,out );
        out.println("DeclareWithInit");
        name.print(indentation+1, false,out );
        value.print(indentation+1, true, out);
    }

}
