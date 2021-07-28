package ir;

import java.util.Vector;

/**
 * 表示一个变量 可以时普通变量也可以是数组
 */
public class VarInfo implements Cloneable{
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

    public VarInfo(String name, boolean is_array, int shape){
        this();
        this.name = name;
        this.is_array = is_array;
        this.shape.addElement(shape);
    }

    public VarInfo clone() throws CloneNotSupportedException {
        VarInfo newborn=(VarInfo) super.clone();
        newborn.name=this.name;
        newborn.is_array=this.is_array;
        newborn.shape=(Vector<Integer>) this.shape.clone();

        return newborn;
    }

}
