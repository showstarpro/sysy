package node;

import java.io.PrintStream;

public class NNumber extends NExpression {
    public int value;

    public NNumber() {

    }

    public NNumber(String value) {
        this.value = Integer.parseInt(value);
    }

    public NNumber(int value) {
        this.value = value;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("Number: "+value);
    }
}
