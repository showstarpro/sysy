package ir;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import util.*;

/**
 * 符号表
 */
public class ContextIR implements Cloneable {
    public int id = 1;
    public Vector<Map<String, VarInfo>> symbol_table;
    public Vector<Map<String, ConstInfo>> const_table;
    public Vector<Map<String, ConstInfo>> const_assign_table;
    public Stack<String> loop_label;
    public Stack<Vector<Vector<Map<String, VarInfo>>>> loop_continue_symbol_snapshot;
    public Stack<Vector<Vector<Map<String, VarInfo>>>> loop_break_symbol_snapshot;
    public Stack<Map<Pair, String>> loop_continue_phi_move;
    public Stack<Map<Pair, String>> loop_break_phi_move;
    public Stack<Vector<String>> loop_var;

    /**
     * 实例初始化块
     */ {
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

    public ContextIR() {

        this.insert_symbol("_sysy_l1", new VarInfo("@&^_sysy_l1", true, 1024));
        this.insert_symbol("_sysy_l2", new VarInfo("@&^_sysy_l2", true, 1024));
        this.insert_symbol("_sysy_h", new VarInfo("@&^_sysy_h", true, 1024));
        this.insert_symbol("_sysy_m", new VarInfo("@&^_sysy_m", true, 1024));
        this.insert_symbol("_sysy_s", new VarInfo("@&^_sysy_s", true, 1024));
        this.insert_symbol("_sysy_us", new VarInfo("@&^_sysy_us", true, 1024));
        this.insert_symbol("_sysy_idx", new VarInfo("@^_sysy_idx", false, new Vector<>()));

    }

    public int get_id() {
        return ++id;
    }

    public void insert_symbol(String name, VarInfo value) {
        symbol_table.lastElement().put(name, value);
    }

    public void insert_const(String name, ConstInfo value) {
        const_table.lastElement().put(name, value);
    }

    public void insert_const_assign(String name, ConstInfo value) {
        const_assign_table.lastElement().put(name, value);
    }

    public VarInfo find_symbol(String name) throws Exception {
        for (int i = symbol_table.size() - 1; i >= 0; i--) {
            VarInfo find = symbol_table.get(i).get(name);
            if (find != null) return find;
        }
        throw new Exception("No such symbol:" + name);
    }

    public ConstInfo find_const(String name) throws Exception {
        for (int i = const_table.size() - 1; i >= 0; i--) {
            ConstInfo find = const_table.get(i).get(name);
            if (find != null) return find;
        }
        throw new Exception("No such const:" + name);
    }

    public ConstInfo find_const_assign(String name) throws Exception {
        for (int i = const_assign_table.size() - 1; i >= 0; i--) {
            ConstInfo find = const_assign_table.get(i).get(name);
            if (find != null) return find;
        }
        throw new Exception("No such const:" + name);
    }

    public void create_scope() {
        symbol_table.addElement(new HashMap<>());
        const_table.addElement(new HashMap<>());
        const_assign_table.addElement(new HashMap<>());
    }

    public void end_scope() {
        symbol_table.removeElementAt(symbol_table.size() - 1);
        const_table.removeElementAt(const_table.size() - 1);
        const_assign_table.removeElementAt(const_assign_table.size() - 1);
    }

    public boolean is_global() {
        return symbol_table.size() == 1 && const_table.size() == 1;
    }

    public boolean in_loop() {
        return !loop_label.empty();
    }

    public ContextIR clone() throws CloneNotSupportedException {
        ContextIR newborn = (ContextIR) super.clone();
        newborn.id = this.id;
//        newborn.symbol_table = (Vector<Map<String, VarInfo>>) this.symbol_table.clone();
        newborn.symbol_table = new Vector<>();
        for (Map<String, VarInfo> i : this.symbol_table) {
            Map<String, VarInfo> tmp = new HashMap<>();
            for(Map.Entry<String, VarInfo> j: i.entrySet()){
                VarInfo tv = j.getValue().clone();
                tmp.put(j.getKey(),tv);
            }
            newborn.symbol_table.add(tmp);
        }

//        newborn.const_table = (Vector<Map<String, ConstInfo>>) this.const_table.clone();
        newborn.const_table = new Vector<>();
        for (Map<String, ConstInfo> i : this.const_table) {
            Map<String, ConstInfo> tmp = new HashMap<>();
            for(Map.Entry<String, ConstInfo> j: i.entrySet()){
                ConstInfo tv = j.getValue().clone();
                tmp.put(j.getKey(),tv);
            }
            newborn.const_table.add(tmp);
        }
//        newborn.const_assign_table = (Vector<Map<String, ConstInfo>>) this.const_assign_table.clone();
        newborn.const_assign_table = new Vector<>();
        for (Map<String, ConstInfo> i : this.const_assign_table) {
            Map<String, ConstInfo> tmp = new HashMap<>();
            for(Map.Entry<String, ConstInfo> j: i.entrySet()){
                ConstInfo tv = j.getValue().clone();
                tmp.put(j.getKey(),tv);
            }
            newborn.const_assign_table.add(tmp);
        }
//        newborn.loop_label = (Stack<String>) this.loop_label.clone();
        newborn.loop_label = new Stack<>();
        for(String s: this.loop_label){
            newborn.loop_label.push(s);
        }
//        newborn.loop_continue_symbol_snapshot = (Stack<Vector<Vector<Map<String, VarInfo>>>>) this.loop_continue_symbol_snapshot.clone();
        newborn.loop_continue_symbol_snapshot = new Stack<>();
        for(Vector<Vector<Map<String, VarInfo>>> i: this.loop_continue_symbol_snapshot){
            Vector<Vector<Map<String, VarInfo>>> tmp1 = new Vector<>();
            for(Vector<Map<String, VarInfo>> j : i){
                Vector<Map<String, VarInfo>> tmp2 = new Vector<>();
                for(Map<String, VarInfo> k: j){
                    Map<String, VarInfo> tmp3 = new HashMap<>();
                    for(Map.Entry<String, VarInfo> l: k.entrySet()){
                        VarInfo tv = l.getValue().clone();
                        tmp3.put(l.getKey(),tv);
                    }
                    tmp2.add(tmp3);
                }
                tmp1.addElement(tmp2);
            }
            newborn.loop_continue_symbol_snapshot.push(tmp1);
        }

//        newborn.loop_break_symbol_snapshot = (Stack<Vector<Vector<Map<String, VarInfo>>>>) this.loop_break_symbol_snapshot.clone();
        newborn.loop_break_symbol_snapshot = new Stack<>();
        for(Vector<Vector<Map<String, VarInfo>>> i: this.loop_break_symbol_snapshot){
            Vector<Vector<Map<String, VarInfo>>> tmp1 = new Vector<>();
            for(Vector<Map<String, VarInfo>> j : i){
                Vector<Map<String, VarInfo>> tmp2 = new Vector<>();
                for(Map<String, VarInfo> k: j){
                    Map<String, VarInfo> tmp3 = new HashMap<>();
                    for(Map.Entry<String, VarInfo> l: k.entrySet()){
                        VarInfo tv = l.getValue().clone();
                        tmp3.put(l.getKey(),tv);
                    }
                    tmp2.add(tmp3);
                }
                tmp1.addElement(tmp2);
            }
            newborn.loop_break_symbol_snapshot.push(tmp1);
        }

//        newborn.loop_continue_phi_move = (Stack<Map<Pair, String>>) this.loop_continue_phi_move.clone();
        newborn.loop_continue_phi_move = new Stack<>();
        for(Map<Pair, String> i: this.loop_continue_phi_move){
            Map<Pair, String> tmp = new HashMap<>();
            for(Map.Entry<Pair,String> j: i.entrySet()){
                Pair tp = (Pair)j.getKey().clone();
                tmp.put(tp, j.getValue());
            }
            newborn.loop_continue_phi_move.push(tmp);
        }


//        newborn.loop_break_phi_move = (Stack<Map<Pair, String>>) this.loop_break_phi_move.clone();
        newborn.loop_break_phi_move = new Stack<>();
        for(Map<Pair, String> i: this.loop_break_phi_move){
            Map<Pair, String> tmp = new HashMap<>();
            for(Map.Entry<Pair,String> j: i.entrySet()){
                Pair tp = (Pair)j.getKey().clone();
                tmp.put(tp, j.getValue());
            }
            newborn.loop_break_phi_move.push(tmp);
        }

//        newborn.loop_var = (Stack<Vector<String>>) this.loop_var.clone();
        newborn.loop_var = new Stack<>();
        for(Vector<String> v: this.loop_var){
            Vector<String> tmp = new Vector<>();
            for(String s : v){
                tmp.addElement(s);
            }
            newborn.loop_var.push(tmp);
        }
        return newborn;
    }


}

