package ir;

import java.util.Vector;

/**
 * 表示一个常量 可以时普通变量也可以是数组
 */
public class ConstInfo implements Cloneable{
    public boolean is_array;
    public Vector<Integer> shape;
    public Vector<Integer> value;

    public ConstInfo() {
        this.shape = new Vector<>();
        this.value = new Vector<>();
    }

    public ConstInfo(Vector<Integer> value, boolean is_array, Vector<Integer> shape) {
        this();
        this.value = value;
        this.shape = shape;
        this.is_array = is_array;
    }

    public ConstInfo(int value) {
        this();
        this.value.addElement(value);
        this.is_array = false;
    }

    public ConstInfo clone() throws CloneNotSupportedException {
        ConstInfo newborn=(ConstInfo) super.clone();
        newborn.is_array=this.is_array;
        newborn.shape=(Vector<Integer>) this.shape.clone();
        newborn.value=(Vector<Integer>) this.value.clone();

        return newborn;
    }
}
