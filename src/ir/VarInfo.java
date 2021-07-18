package ir;

import java.util.Vector;

/**
 * 表示一个变量 可以时普通变量也可以是数组
 */
public class VarInfo {
    public Vector<Integer> shape;
    public boolean is_array;
    public String name;

    public VarInfo() {
        shape = new Vector<>();
    }

    public VarInfo(String name, boolean is_array, Vector<Integer> shape) {
        this();
        this.name = name;
        this.shape = shape;
        this.is_array = is_array;
    }
}
