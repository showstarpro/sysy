package assembly;

import util.Multimap;
import ir.IR;
import ir.OpName;

import java.io.PrintStream;
import java.util.*;

import static java.lang.Integer.min;

public class ContextAsm {
    private int time = 0;
    public static final int reg_count = 11;

    PrintStream log_out;

    public List<IR> ir;
    public int[] stack_size = {6 * 4, 4, 0, 0};

    public ListIterator<IR> function_begin_it;
    public HashMap<String, Integer> stack_offset_map = new HashMap<>();//java中hashmap和c++的unordered_map一样吗
    public HashMap<IR, Integer> ir_to_time = new HashMap<>();

    public HashMap<String, Integer> var_define_timestamp = new HashMap<>();
    public Multimap<Integer, String> var_define_timestamp_heap = new Multimap<>();

    public HashMap<String, Integer> var_latest_use_timestamp = new HashMap<>();
    public Multimap<Integer, String> var_latest_use_timestamp_heap = new Multimap<>();
    public BitSet savable_reg = new BitSet(11);
    public BitSet used_reg = new BitSet(11);

    public HashMap<String, Integer> var_to_reg = new HashMap<>() {
        {
            put("$arg0", 0);
            put("$arg1", 1);
            put("$arg2", 2);
            put("$arg3", 3);
        }
    };
    public HashMap<Integer, String> reg_to_var;

    public boolean has_function_call = false;

    {
        savable_reg.set(4, 11);
        reg_to_var = new HashMap<>();
    }

    public ContextAsm(List<IR> ir, ListIterator<IR> function_begin_it, PrintStream log_out) {
        this.ir = ir;
        this.function_begin_it = ir.listIterator(function_begin_it.nextIndex());
        this.log_out = log_out;

    }


    static String rename(String name) {
        if (name.length() > 3 && name.charAt(1) == '&' && name.charAt(2) == '^')
            return name.substring(3);
        else if (name.length() > 2 && name.charAt(1) == '^')
            return name.substring(2);
        else if (name.length() > 2 && name.charAt(1) == '&')
            return "__Var__" + (name.length() - 2) + name.substring(2);
        else
            return "__Var__" + (name.length() - 1) + name.substring(1);
    }

    public int resolve_stack_offset(String name) throws Exception {
        if (name.startsWith("$arg")) {
            int id = Integer.parseUnsignedInt(name.substring(4));
            if (id < 4)
                throw new Exception(name + "is not in mem. ");
            else return stack_size[1] + stack_size[2] + stack_size[3] + (id - 4) * 4 + (has_function_call ? 4 : 0);
        } else if (name.equals("$ra"))
            return stack_size[1] + stack_size[2] + stack_size[3];
        int temp_size = 0;
        if(stack_offset_map.get(name)!=null){
            temp_size = stack_offset_map.get(name);
        }
        return temp_size + stack_size[3];
    }

    public void set_ir_timestamp(IR cur) {
        ir_to_time.put(cur, ++time);
    }

    public void F(IR cur, OpName opName) {
        if (opName.type == OpName.Type.Var) {
            if (!var_latest_use_timestamp.containsKey(opName.name)) {
                var_latest_use_timestamp.put(opName.name, ir_to_time.get(cur));
                var_latest_use_timestamp_heap.put(ir_to_time.get(cur), opName.name);
            }
        }
    }

    public void set_var_latest_use_timestamp(IR cur) {
        if (cur.op_code != IR.OpCode.MALLOC_IN_STACK) {
            F(cur, cur.dest);
            F(cur, cur.op1);
            F(cur, cur.op2);
            F(cur, cur.op3);
        }
        if (cur.op_code == IR.OpCode.SET_ARG && cur.dest.value < 4) {
            String name;
            if (cur.phi_block==null) {
                name = "$arg:" + (cur.dest.value) + ":" + "0";
                IR temp = new IR();
                cur.phi_block = temp;
                ir_to_time.put(temp,0);
            } else {
                name = "$arg:" + (cur.dest.value) + ":" + (ir_to_time.get(cur.phi_block));//不知道对不对
            }

            var_latest_use_timestamp.put(name, ir_to_time.get(cur));//同上
            var_latest_use_timestamp_heap.put(ir_to_time.get(cur.phi_block), name);
        }
        if (cur.op_code == IR.OpCode.IMUL && cur.op1.type == OpName.Type.Var) {
            var_latest_use_timestamp.compute(cur.op1.name, (key, value) -> value + 1);
        }

        if (cur.op_code == IR.OpCode.CALL || cur.op_code == IR.OpCode.IDIV ||
                cur.op_code == IR.OpCode.MOD) {
            for (int i = 0; i < 4; i++) {
                String name = "$arg:" + i + ":" +ir_to_time.get(cur);
                var_latest_use_timestamp.put(name, ir_to_time.get(cur));
                var_latest_use_timestamp_heap.put(ir_to_time.get(cur), name);
            }
        }
    }

    public void set_var_define_timestamp(IR cur) {
        if (cur.op_code != IR.OpCode.MALLOC_IN_STACK &&
                cur.op_code != IR.OpCode.PHI_MOV) {
            if (cur.dest.type == OpName.Type.Var) {
                if (!var_define_timestamp.containsKey(cur.dest.name)) {
                    var_define_timestamp.put(cur.dest.name, ir_to_time.get(cur));
                    var_define_timestamp_heap.put(ir_to_time.get(cur), cur.dest.name);
                }
            }
        } else if (cur.op_code == IR.OpCode.PHI_MOV) {
            if (cur.dest.type == OpName.Type.Var) {
                if (!var_define_timestamp.containsKey(cur.dest.name)) {

                    IR temp;
                    if(cur.phi_block == null )
                    {
                        temp = new IR();
                        cur.phi_block = temp;
                        ir_to_time.put(temp,0);
                    }
                    else {
                        temp = cur.phi_block;
                    }
                    int time = min(ir_to_time.get(temp), ir_to_time.get(cur));
                    var_define_timestamp.put(cur.dest.name, time);
                    var_define_timestamp_heap.put(time, cur.dest.name);
                }

            }
        }
        if (cur.op_code == IR.OpCode.SET_ARG && cur.dest.value < 4) {
            String name;
            if (cur.phi_block==null) {
                name = "$arg:" + (cur.dest.value) + ":" + "0";
                IR temp = new IR();
                cur.phi_block = temp;
                ir_to_time.put(temp,0);
            } else {
                name = "$arg:" + (cur.dest.value) + ":" + (ir_to_time.get(cur.phi_block));//不知道对不对
            }

            var_define_timestamp.put(name, ir_to_time.get(cur));
            var_define_timestamp_heap.put(ir_to_time.get(cur), name);
        }
        if (cur.op_code == IR.OpCode.CALL || cur.op_code == IR.OpCode.IDIV || cur.op_code == IR.OpCode.MOD) {
            for (int i = 0; i < 4; i++) {
                String name = "$arg:" + i +":"+ ir_to_time.get(cur);
                var_define_timestamp.put(name, ir_to_time.get(cur));
                var_define_timestamp_heap.put(ir_to_time.get(cur), name);
            }
        }
    }

    public void expire_old_intervals(int cur_time) {
        for (int i = 0; i < reg_count; i++) {
            if (used_reg.get(i))
                if (!reg_to_var.containsKey(i) ||
                        !var_latest_use_timestamp.containsKey(reg_to_var.get(i)) ||
                        cur_time >= var_latest_use_timestamp.get(reg_to_var.get(i))) {
                    used_reg.set(i, false);
                    log_out.println("# [log]" + cur_time + " expire " + reg_to_var.get(i) + " r" + i);
                    reg_to_var.remove(i);
                }
        }
    }

    public boolean var_in_reg(String name) {
        return var_to_reg.containsKey(name);
    }

    public void get_specified_reg(int reg_id) {
        assert !used_reg.get(reg_id);
        if (savable_reg.get(reg_id)) {
            savable_reg.set(reg_id, false);
            stack_size[1] += 4;
        }
        used_reg.set(reg_id);
    }

    public void move_reg(String name, int reg_dest) {
        assert !used_reg.get(reg_dest);
        int old_reg = var_to_reg.get(name);
        used_reg.set(old_reg, false);
        get_specified_reg(reg_dest);
        reg_to_var.remove(old_reg);
        var_to_reg.compute(name, (key, value) -> reg_dest);
        reg_to_var.put(reg_dest, name);
    }

    public void overflow_var(String name) {
        if (var_to_reg.containsKey(name)) {
            final int reg_id = var_to_reg.get(name);
            used_reg.set(reg_id, false);
            var_to_reg.remove(name);
            reg_to_var.remove(reg_id);
        }
        stack_offset_map.compute(name, (key, value) -> stack_size[2]);
        stack_size[2] += 4;
    }

    public void overflow_reg(int reg_id) {
        if (!used_reg.get(reg_id))
            return;
        overflow_var(reg_to_var.get(reg_id));
    }

    public String select_var_to_overflow(int begin) {
        String var = new String("");
        int end = -1;
        for (Map.Entry<Integer, String> entry : reg_to_var.entrySet()) {
            if (entry.getKey() < begin)
                continue;
            if (var_latest_use_timestamp.get(entry.getValue()) > end) {
                var = entry.getValue();
                end = var_latest_use_timestamp.get(entry.getValue());
            }
        }
        return var;
    }

    public int find_free_reg(int begin) {
        BitSet tmp = (BitSet) savable_reg.clone();//不知道对不对
        tmp.or(used_reg);
        for (int i = begin; i < reg_count; i++) {
            if (!tmp.get(i))
                return i;
        }
        for (int i = begin; i < reg_count; i++)
            if ((!used_reg.get(i)) && savable_reg.get(i)) {
                return i;
            }
        return -1;
    }

    public int get_new_reg(int begin) {
        BitSet tmp = (BitSet) savable_reg.clone();//不知道对不对
        tmp.or(used_reg);
        for (int i = begin; i < reg_count; i++) {
            if (!tmp.get(i)) {
                used_reg.set(i, true);
                return i;
            }
        }
        for (int i = begin; i < reg_count; i++)
            if ((!used_reg.get(i)) && savable_reg.get(i)) {
                used_reg.set(i, true);
                stack_size[1] += 4;
                savable_reg.set(i, false);
                return i;
            }
        int id = begin;
        for (int i = begin; i < reg_count; i++) {
            if (var_latest_use_timestamp.get(reg_to_var.get(i)) > var_latest_use_timestamp.get(reg_to_var.get(id)))
                id = i;
        }
        overflow_reg(id);
        used_reg.set(id);
        return id;
    }

    public void bind_var_to_reg(String name, int reg_id) throws Exception {
        if (name.isEmpty())
            throw new Exception("Var name id empty. ");
        assert used_reg.get(reg_id);
        if (reg_to_var.containsKey(reg_id))
            var_to_reg.remove(reg_to_var.get(reg_id));
        reg_to_var.put(reg_id, name);
        var_to_reg.put(name, reg_id);
    }


    public int get_new_reg_for(String name) throws Exception {
        if (var_to_reg.containsKey(name))
            return var_to_reg.get(name);
        int reg_id = get_new_reg(0);//默认为0
        bind_var_to_reg(name, reg_id);
        return reg_id;
    }

    public int get_specified_reg_for(String name, int reg_id) throws Exception {
        if (var_to_reg.containsKey(name)) {
            if (var_to_reg.get(name) == reg_id)
                return var_to_reg.get(name);
        }
        if (used_reg.get(reg_id))
            overflow_reg(reg_id);
        get_specified_reg(reg_id);
        bind_var_to_reg(name, reg_id);
        return reg_id;
    }

    public void load_imm(String reg, int value, PrintStream out) {
        if (value >= 0 && value < 65536)
            out.println("    MOV " + reg + ", #" + value);
        else
            out.println("    MOV32 " + reg + ",  " + value);
    }

    public void load(String reg, OpName op, PrintStream out) throws Exception {
        if (op.type == OpName.Type.Imm) {
            load_imm(reg, op.value, out);
        } else if (op.type == OpName.Type.Var) {
            if (var_to_reg.containsKey(op.name)) {
                if (Integer.parseUnsignedInt(reg.substring(1)) != var_to_reg.get(op.name))
                    out.println("    MOV " + reg + ", r" + var_to_reg.get(op.name));
            } else {
                if (op.name.charAt(0) == '%') {
                    if (op.name.charAt(1) == '&') {
                        int offset = resolve_stack_offset(op.name);
                        if (offset >= 0 && offset < 256)
                            out.println("    ADD " + reg + ", sp, #" + offset);
                        else {
                            load_imm(reg, resolve_stack_offset(op.name), out);
                            out.println("    ADD " + reg + ", sp, " + reg);
                        }
                    } else {
                        if (var_in_reg(op.name)) {
                            out.println("    MOV " + reg + ", r" + var_to_reg.get(op.name));
                        } else {
                            int offset = resolve_stack_offset(op.name);
                            if (offset > -4096 && offset < 4096) {
                                out.println("    LDR " + reg + ", [sp,#" + offset + "]");
                            } else {
                                out.println("    MOV32 r11, " + offset);
                                out.println("    ADD r11, sp, r11");
                                out.println("    LDR " + reg + ", [r11,#0]");
                            }
                        }
                    }
                } else if (op.name.charAt(0) == '@') {
                    if (op.name.charAt(1) != '&') {
                        out.println("    MOV32 " + reg + "," + rename(op.name));
                        out.println("    LDR " + reg + ", [" + reg + ", #0]");
                    } else
                        out.println("    MOV32 " + reg + ", " + rename(op.name));
                } else if (op.name.charAt(0) == '$') {
                    int offset = resolve_stack_offset(op.name);
                    if (offset > -4096 && offset < 4096) {
                        out.println("    LDR " + reg + ", [sp, #" + offset + "]");
                    } else {
                        out.println("    MOV32 r11, " + offset);
                        out.println("    ADD r11, sp, r11");
                        out.println("    LDR " + reg + ", [r11,#0]");
                    }
                }
            }
        }
    }

    public void store_to_stack_offset(String reg, int offset, PrintStream out, String op) throws Exception {
        if (!(offset > -4096 && offset < 4096)) {
            String tmp_reg = reg.equals("r11") ? "r12" : "r11";
            load_imm(tmp_reg, offset, out);
            out.println("    ADD " + tmp_reg + ", sp, " + tmp_reg);
            out.println("    " + op + " " + reg + ", [" + tmp_reg + ", #0]");
        } else {
            out.println("    " + op + " " + reg + ", [sp, #" + offset + "]");
        }
    }

    public void load_from_stack_offset(String reg, int offset, PrintStream out, String op) throws Exception {
        if (!(offset > -4096 && offset < 4096)) {
            String tmp_reg = reg.equals("r11") ? "r12" : "r11";
            load_imm(tmp_reg, offset, out);
            out.println("    ADD " + tmp_reg + ", sp, " + tmp_reg);
            out.println("    " + op + " " + reg + ", [" + tmp_reg + ", #0]");
        } else {
            out.println("    " + op + " " + reg + ", [sp,#" + offset + "]");
        }
    }

    public void store_to_stack(String reg, OpName op, PrintStream out, String op_code) throws Exception {
        if (op.type != OpName.Type.Var)
            throw new Exception("WTF");//??
        if (op.name.charAt(1) == '&')
            return;
        if (op.name.charAt(0) == '%')
            store_to_stack_offset(reg, resolve_stack_offset(op.name), out, op_code);
        else if (op.name.charAt(0) == '@') {
            out.println("    MOV32 r11, " + rename(op.name));
            out.println("    " + op_code + " " + reg + ", [r11,#0]");
        } else if (op.name.charAt(0) == '$') {
            int offset = resolve_stack_offset(op.name);
            store_to_stack_offset(reg, offset, out, op_code);
        }
    }

}


