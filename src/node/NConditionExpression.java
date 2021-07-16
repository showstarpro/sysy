package node;

import java.io.PrintStream;

public class NConditionExpression extends NExpression {
    public NExpression value;

    public NConditionExpression() {

    }

    public NConditionExpression(NExpression value) {
        this.value = value;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("ConditionExpression");
        this.value.print(indentation+1, true,out);
    }
}
