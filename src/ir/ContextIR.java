package ir;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import util.*;

/**
 * 符号表
 */
public class ContextIR implements Cloneable{
    public int id=1;
    public Vector<Map<String, VarInfo>> symbol_table;
    public Vector<Map<String, ConstInfo>> const_table;
    public Vector<Map<String, ConstInfo>> const_assign_table;
    public Stack<String> loop_label;
    public Stack<Vector<Vector<Map<String, VarInfo> > > > loop_continue_symbol_snapshot;
    public Stack<Vector<Vector<Map<String, VarInfo> > > > loop_break_symbol_snapshot;
    public Stack<Map<Pair<Integer, String>, String>> loop_continue_phi_move;
    public Stack<Map<Pair<Integer, String>, String>> loop_break_phi_move;
    public Stack<Vector<String>> loop_var;

    /**
     * 实例初始化块
     */
    {
        symbol_table = new Vector<>();
        symbol_table.addElement(new HashMap<>());  // 加入一张空表作为初始

        const_table = new Vector<>();
        const_table.addElement(new HashMap<>());// 加入一张空表作为初始

        const_assign_table = new Vector<>();
        const_assign_table.addElement(new HashMap<>());// 加入一张空表作为初始

        loop_label = new Stack<>();

        loop_continue_symbol_snapshot = new Stack<>();

        loop_break_symbol_snapshot = new Stack<>();

        loop_continue_phi_move = new Stack<>();

        loop_break_phi_move = new Stack<>();

        loop_var = new Stack<>();

    }
    public ContextIR(){

    }
    public int get_id(){
        return ++id;
    }

    public void insert_symbol(String name, VarInfo value){
        symbol_table.lastElement().put(name, value);
    }
    public void insert_const(String name, ConstInfo value){
        const_table.lastElement().put(name, value);
    }
    public void insert_const_assign(String name, ConstInfo value){
        const_assign_table.lastElement().put(name, value);
    }

    public VarInfo find_symbol(String name) throws Exception {
        for(int i = symbol_table.size()-1;i>=0;i--){
            VarInfo find = symbol_table.get(i).get(name);
            if(find!=null) return find;
        }
        throw new Exception("No such symbol:" + name);
    }
    public ConstInfo find_const(String name) throws Exception {
        for(int i = const_table.size()-1;i>=0;i--){
            ConstInfo find = const_table.get(i).get(name);
            if(find!=null) return find;
        }
        throw new Exception("No such const:" + name);
    }
    public ConstInfo find_const_assign(String name) throws Exception {
        for(int i = const_assign_table.size()-1;i>=0;i--){
            ConstInfo find = const_assign_table.get(i).get(name);
            if(find!=null) return find;
        }
        throw new Exception("No such const:" + name);
    }

    public void create_scope(){
        symbol_table.addElement(new HashMap<>());
        const_table.addElement(new HashMap<>());
        const_assign_table.addElement(new HashMap<>());
    }

    public void end_scope(){
        symbol_table.removeElementAt(symbol_table.size()-1);
        const_table.removeElementAt(const_table.size()-1);
        const_assign_table.removeElementAt(const_assign_table.size()-1);
    }

    public boolean is_global(){
        return symbol_table.size()==1 && const_table.size()==1;
    }

    public boolean in_loop(){
        return !loop_label.empty();
    }

    public ContextIR clone() throws CloneNotSupportedException {
        ContextIR newborn=(ContextIR) super.clone();
        newborn.id=this.id;
        newborn.symbol_table=(Vector<Map<String, VarInfo>>)this.symbol_table.clone();
        newborn.const_table=(Vector<Map<String, ConstInfo>>) this.const_table.clone();
        newborn.const_assign_table=(Vector<Map<String, ConstInfo>>)this.const_assign_table.clone();
        newborn.loop_label=(Stack<String>)this.loop_label.clone();
        newborn.loop_continue_symbol_snapshot=(Stack<Vector<Vector<Map<String, VarInfo> > > >)this.loop_continue_symbol_snapshot.clone();
        newborn.loop_break_symbol_snapshot=(Stack<Vector<Vector<Map<String, VarInfo> > > >)this.loop_break_symbol_snapshot.clone();
        newborn.loop_continue_phi_move=(Stack<Map<Pair<Integer, String>, String>>)this.loop_continue_phi_move.clone();
        newborn.loop_break_phi_move=(Stack<Map<Pair<Integer, String>, String>>)this.loop_break_phi_move.clone();
        newborn.loop_var=(Stack<Vector<String>>)this.loop_var.clone();

        return newborn;
    }

}

