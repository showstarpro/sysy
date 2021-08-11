package assembly;

import ir.IR;
import ir.OpName;

import java.io.PrintStream;
import java.util.*;

public class Asm {
    public static BitSet non_volatile_reg = new BitSet(11);

    static {
        non_volatile_reg.set(4, 11);
    }

    public static void generate_asm(List<IR> irs, PrintStream out) throws Exception {
        out.println("    .macro mov32, reg, val\n" +
                "        movw \\reg, #:lower16:\\val\n" +
                "        movt \\reg, #:upper16:\\val\n" +
                "    .endm");
        ListIterator<IR> function_begin_it = irs.listIterator();
        ListIterator<IR> outter_it = irs.listIterator();
        while (outter_it.hasNext()) {
            IR ir = outter_it.next();
            outter_it.previous();   // 保持
            if (ir.op_code == IR.OpCode.DATA_BEGIN) {
                out.println(".data");
                out.println(".global " + ContextAsm.rename(ir.label));
                out.println(ContextAsm.rename(ir.label) + ":");
            } else if (ir.op_code == IR.OpCode.DATA_WORD) {
                out.println(".word " + ir.dest.value);
            } else if (ir.op_code == IR.OpCode.DATA_SPACE) {
                out.println(".space " + ir.dest.value);
            } else if (ir.op_code == IR.OpCode.FUNCTION_BEGIN) {
                function_begin_it = irs.listIterator(outter_it.nextIndex());
            } else if (ir.op_code == IR.OpCode.FUNCTION_END) {
                generate_function_asm(irs, out, function_begin_it, outter_it);
            }
            outter_it.next();
        }
    }

    public static void generate_function_asm(List<IR> irs, PrintStream out,
                                             ListIterator<IR> begin,
                                             ListIterator<IR> end) throws Exception {
        int begin_index = begin.nextIndex();
        int end_index = end.nextIndex();

        assert (irs.get(begin_index).op_code == IR.OpCode.FUNCTION_BEGIN);
        ContextAsm ctx = new ContextAsm(irs, begin, out);

        // 标号
        for (int i = begin_index; i != end_index; i++) {
            ctx.set_ir_timestamp(irs.get(i));
        }

        for (int i = begin_index; i != end_index; i++) {
            ctx.set_var_define_timestamp(irs.get(i));
        }

        for (int i = end_index - 1; i != begin_index; i--) {
            ctx.set_var_latest_use_timestamp(irs.get(i));
        }

        for (int i = begin_index; i != end_index; i++) {
            IR tempir = irs.get(i);
            if (tempir.op_code == IR.OpCode.CALL || tempir.op_code == IR.OpCode.IDIV
                    || tempir.op_code == IR.OpCode.MOD) {
                ctx.has_function_call = true;
                break;
            }
        }
        ctx.stack_size[0] = ctx.has_function_call ? 4 : 0;

        for (int i = begin_index; i != end_index; i++) {
            out.print("# " + ctx.ir_to_time.get(irs.get(i)) + " ");
            irs.get(i).print(out, false);
        }

        for (int i = begin_index; i != end_index; i++) {
            IR ir = irs.get(i);

            ///////////////////////////////////// 计算栈大小 (数组分配)
            if (ir.op_code == IR.OpCode.MALLOC_IN_STACK) {
                ctx.stack_offset_map.put(ir.dest.name, ctx.stack_size[2]);
                ctx.stack_size[2] += ir.op1.value;
            } else if (ir.op_code == IR.OpCode.SET_ARG) {
                ctx.stack_size[3] = Math.max(ctx.stack_size[3], (ir.dest.value - 3) * 4);
            }
        }

        // 寄存器分配
        for (Map.Entry<Integer, String> i : ctx.var_define_timestamp_heap.entries()) {
            int cur_time = i.getKey();
            ctx.expire_old_intervals(cur_time);

            if (ctx.var_in_reg(i.getValue())) {
                continue;
            } else {
                if (i.getValue().startsWith("$arg:")) {
                    String temp_str = i.getValue().substring(5);
                    int cnt = 0;
                    while(Character.isDigit(temp_str.charAt(cnt++)));
                    int reg = Integer.parseUnsignedInt(temp_str.substring(0,cnt-1));
                    if (ctx.used_reg.get(reg)) {
                        String cur_var = ctx.reg_to_var.get(reg);
                        if (ctx.find_free_reg(4) != -1) {
                            ctx.move_reg(cur_var, ctx.find_free_reg(4));
                        } else {
                            String overflow_var = ctx.select_var_to_overflow(4);
                            if (ctx.var_latest_use_timestamp.get(cur_var) >=
                                    ctx.var_latest_use_timestamp.get(overflow_var)) {
                                overflow_var = cur_var;
                            }
                            ctx.overflow_var(overflow_var);
                            if (ctx.used_reg.get(reg)) {
                                ctx.move_reg(cur_var, ctx.find_free_reg(4));
                            }
                        }
                    }
                    ctx.get_specified_reg_for(i.getValue(), reg);
                } else {
                    if (!i.getValue().startsWith("%")) continue;
                    boolean conflict = false;
                    int latest = ctx.var_latest_use_timestamp.get(i.getValue());
                    for (Map.Entry<Integer, String> j : ctx.var_define_timestamp_heap.entries()) {
                        if (j.getKey() < i.getKey()) continue;
                        if (j.getKey() > latest) break;
                        if (j.getValue().startsWith("$arg:")) {
                            conflict = true;
                            break;
                        }
                    }
//                    conflict = true;
                    if (ctx.find_free_reg(conflict ? 4 : 0) != -1) {
                        ctx.get_specified_reg_for(i.getValue(),
                                ctx.find_free_reg(conflict ? 4 : 0));
                    } else {
                        String cur_max = ctx.select_var_to_overflow(conflict ? 4 : 0);
                        if (ctx.var_latest_use_timestamp.get(i.getValue()) <
                                ctx.var_latest_use_timestamp.get(cur_max)) {
                            ctx.get_specified_reg_for(i.getValue(), ctx.var_to_reg.get(cur_max));
                        } else {
                            ctx.overflow_var(i.getValue());
                        }
                    }
                }
            }
        }

        // 翻译
        for (int i = begin_index; i != end_index; i++) {
            IR ir = irs.get(i);
            int[] stack_size = ctx.stack_size;
            out.print("#");
            ir.print(out, false);

            if (ir.op_code == IR.OpCode.FUNCTION_BEGIN) {
                out.println(".text");
                out.println(".global " + ir.label);
                out.println(".type " + ir.label + ", %function");
                out.println(ir.label + ":");
                if (stack_size[0] + stack_size[1] + stack_size[2] + stack_size[3] > 256) {
                    ctx.load("r12",
                            new OpName(stack_size[0] + stack_size[1] + stack_size[2] + stack_size[3]),
                            out);
                    out.println("    SUB sp, sp, r12");
                } else {
                    out.println("    SUB sp, sp, #" +
                            (stack_size[0] + stack_size[1] + stack_size[2] + stack_size[3]));
                }
                if (ctx.has_function_call) ctx.store_to_stack("lr", new OpName("$ra"), out, "STR");


                // 保护现场
                int offset = 4;
                ctx.store_to_stack_offset("r11", stack_size[2] + stack_size[3], out, "STR");
                for (int j = 0; j < 11; j++) {
                    if (non_volatile_reg.get(j) != ctx.savable_reg.get(j)) {
                        ctx.store_to_stack_offset(
                                "r" + j, stack_size[2] + stack_size[3] + offset, out, "STR"
                        );
                        offset += 4;
                    }
                }

            } else if (ir.op_code == IR.OpCode.MOV || ir.op_code == IR.OpCode.PHI_MOV) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                if (ir.op1.type == OpName.Type.Imm) {
                    if (dest_in_reg) {
                        ctx.load_imm("r" + ctx.var_to_reg.get(ir.dest.name), ir.op1.value, out);
                    } else {
                        ctx.load_imm("r12", ir.op1.value, out);
                        ctx.store_to_stack("r12", ir.dest, out, "STR");
                    }
                } else if (ir.op1.type == OpName.Type.Var) {
                    boolean op1_in_reg =
                            ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                    int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 12;
                    int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                    if (!op1_in_reg) {
                        ctx.load("r" + op1, ir.op1, out);
                    }
                    if (dest_in_reg && op1 != dest) {
                        out.println("   MOV r" + dest + ", r" + op1);
                    } else if (!dest_in_reg) {
                        ctx.store_to_stack("r" + op1, ir.dest, out, "STR");
                    }
                }
            } else if (ir.op_code == IR.OpCode.ADD) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                boolean op2_in_reg =
                        ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
                boolean op2_is_imm = ir.op2.type == OpName.Type.Imm
                        && (ir.op2.value >= 0 && ir.op2.value < 256);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 11;
                int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 12;
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;

                if (!op2_in_reg && !op2_is_imm) {
                    ctx.load("r" + op2, ir.op2, out);
                }
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                out.print("    " + "ADD" + " r" + dest + ", r" + op1 + ", ");
                if (op2_is_imm) {
                    out.println("#" + ir.op2.value);
                } else {
                    out.println("r" + op2);
                }
                if (!dest_in_reg) {
                    ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.SUB) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                boolean op2_in_reg =
                        ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
                boolean op2_is_imm = ir.op2.type == OpName.Type.Imm && (ir.op2.value >= 0 && ir.op2.value < 256);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 11;
                int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 12;
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;

                if (!op2_in_reg && !op2_is_imm) {
                    ctx.load("r" + op2, ir.op2, out);
                }
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                out.print("     " + "SUB" + " r" + dest + ", r" + op1 + ", ");
                if (op2_is_imm) {
                    out.println("#" + ir.op2.value);
                } else {
                    out.println("r" + op2);
                }
                if (!dest_in_reg) {
                    ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.IMUL) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                boolean op2_in_reg =
                        ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 11;
                int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 12;
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (!op2_in_reg) {
                    ctx.load("r" + op2, ir.op2, out);
                }
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                if (dest == op1) {
                    out.println("    MUL r12, r" + op1 + ", r" + op2);
                    out.println("    MOV r" + dest + ", r12");
                } else {
                    out.println("    MUL r" + dest + ", r" + op1 + ", r" + op2);
                }
                if (!dest_in_reg) {
                    ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                }

            } else if (ir.op_code == IR.OpCode.SAL) {
                assert (ir.op2.type == OpName.Type.Imm);
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 11;
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                out.println("   " + "LSL" + " r" + dest + ", r" + op1 + ", #" + ir.op2.value);
                if (!dest_in_reg) {
                    ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.SAR) {
                assert (ir.op2.type == OpName.Type.Imm);
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 11;
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                out.println("   " + "ASR" + " r" + dest + ", r" + op1 + ", #" + ir.op2.value);
                if (!dest_in_reg) {
                    ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.IDIV) {
                if (false) {
                    // 优化部分
                } else {
                    ctx.load("r0", ir.op1, out);
                    ctx.load("r1", ir.op2, out);
                    out.println("    BL __aeabi_idiv");
                    if (ctx.var_in_reg(ir.dest.name)) {
                        out.println("   MOV r" + ctx.var_to_reg.get(ir.dest.name) + ", r0");
                    } else {
                        ctx.store_to_stack("r0", ir.dest, out, "STR");
                    }
                }
            } else if (ir.op_code == IR.OpCode.MOD) {
                ctx.load("r0", ir.op1, out);
                ctx.load("r1", ir.op2, out);
                out.println("    BL __aeabi_idivmod");
                if (ctx.var_in_reg(ir.dest.name)) {
                    out.println("    MOV r" + ctx.var_to_reg.get(ir.dest.name) + ", r1");
                } else {
                    ctx.store_to_stack("r1", ir.dest, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.CALL) {
                out.println("   BL " + ir.label);
                if (ir.dest.type == OpName.Type.Var) {
                    if (ctx.var_in_reg(ir.dest.name)) {
                        out.println("    MOV r" + ctx.var_to_reg.get(ir.dest.name) + ", r0");
                    } else {
                        ctx.store_to_stack("r0", ir.dest, out, "STR");
                    }
                }
            } else if (ir.op_code == IR.OpCode.SET_ARG) {
                if (ir.dest.value < 4) {
                    ctx.load("r" + ir.dest.value, ir.op1, out);
                } else {
                    ctx.load("r12", ir.op1, out);
                    ctx.store_to_stack_offset("r12", (ir.dest.value - 4) * 4, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.CMP) {
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                boolean op2_in_reg =
                        ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 12;
                int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 11;
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                if (!op2_in_reg) {
                    ctx.load("r" + op2, ir.op2, out);
                }
                out.println("    CMP r" + op1 + ", r" + op2);
            } else if (ir.op_code == IR.OpCode.JMP) {
                out.println("   " + "B " + ir.label);
            } else if (ir.op_code == IR.OpCode.JEQ) {
                out.println("   " + "BEQ " + ir.label);
            } else if (ir.op_code == IR.OpCode.JNE) {
                out.println("   " + "BNE " + ir.label);
            } else if (ir.op_code == IR.OpCode.JLE) {
                out.println("   " + "BLE " + ir.label);
            } else if (ir.op_code == IR.OpCode.JLT) {
                out.println("   " + "BLT " + ir.label);
            } else if (ir.op_code == IR.OpCode.JGE) {
                out.println("   " + "BGE " + ir.label);
            } else if (ir.op_code == IR.OpCode.JGT) {
                out.println("   " + "BGT " + ir.label);
            } else if (ir.op_code == IR.OpCode.MOVEQ) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (ir.op1.type == OpName.Type.Imm && ir.op1.value >= 0 &&
                        ir.op1.value < 256 && ir.op2.type == OpName.Type.Imm &&
                        ir.op2.value >= 0 && ir.op2.value < 256) {
                    out.println("    " + "MOVEQ" + " r" + dest + ", #" + ir.op1.value);
                    out.println("    " + "MOVNE" + " r" + dest + ", #" + ir.op2.value);
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                }  /* TODO */
            } else if (ir.op_code == IR.OpCode.MOVNE) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (ir.op1.type == OpName.Type.Imm && ir.op1.value >= 0 &&
                        ir.op1.value < 256 && ir.op2.type == OpName.Type.Imm &&
                        ir.op2.value >= 0 && ir.op2.value < 256) {
                    out.println("    " + "MOVNE" + " r" + dest + ", #" + ir.op1.value);
                    out.println("    " + "MOVEQ" + " r" + dest + ", #" + ir.op2.value);
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                }  /* TODO */
            } else if (ir.op_code == IR.OpCode.MOVLE) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (ir.op1.type == OpName.Type.Imm && ir.op1.value >= 0 &&
                        ir.op1.value < 256 && ir.op2.type == OpName.Type.Imm &&
                        ir.op2.value >= 0 && ir.op2.value < 256) {
                    out.println("    " + "MOVLE" + " r" + dest + ", #" + ir.op1.value);
                    out.println("    " + "MOVGT" + " r" + dest + ", #" + ir.op2.value);
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                }  /* TODO */
            } else if (ir.op_code == IR.OpCode.MOVGT) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (ir.op1.type == OpName.Type.Imm && ir.op1.value >= 0 &&
                        ir.op1.value < 256 && ir.op2.type == OpName.Type.Imm &&
                        ir.op2.value >= 0 && ir.op2.value < 256) {
                    out.println("    " + "MOVGT" + " r" + dest + ", #" + ir.op1.value);
                    out.println("    " + "MOVLE" + " r" + dest + ", #" + ir.op2.value);
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                }  /* TODO */
            } else if (ir.op_code == IR.OpCode.MOVLT) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (ir.op1.type == OpName.Type.Imm && ir.op1.value >= 0 &&
                        ir.op1.value < 256 && ir.op2.type == OpName.Type.Imm &&
                        ir.op2.value >= 0 && ir.op2.value < 256) {
                    out.println("    " + "MOVLT" + " r" + dest + ", #" + ir.op1.value);
                    out.println("    " + "MOVGE" + " r" + dest + ", #" + ir.op2.value);
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                }  /* TODO */
            } else if (ir.op_code == IR.OpCode.MOVGE) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (ir.op1.type == OpName.Type.Imm && ir.op1.value >= 0 &&
                        ir.op1.value < 256 && ir.op2.type == OpName.Type.Imm &&
                        ir.op2.value >= 0 && ir.op2.value < 256) {
                    out.println("    " + "MOVGE" + " r" + dest + ", #" + ir.op1.value);
                    out.println("    " + "MOVLT" + " r" + dest + ", #" + ir.op2.value);
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                }  /* TODO */
            } else if (ir.op_code == IR.OpCode.AND) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                boolean op2_in_reg =
                        ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 12;
                int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 11;
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                if (!op2_in_reg) {
                    ctx.load("r" + op2, ir.op2, out);
                }
                out.println("    TST r" + op1 + ", r" + op2);
                out.println("    MOVEQ r" + dest + ", #0");
                out.println("    MOVNE r" + dest + ", #1");
                if (!dest_in_reg) {
                    ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.OR) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                boolean op1_in_reg =
                        ir.op1.type == OpName.Type.Var && ctx.var_in_reg(ir.op1.name);
                boolean op2_in_reg =
                        ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
                int op1 = op1_in_reg ? ctx.var_to_reg.get(ir.op1.name) : 12;
                int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 11;
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                if (!op1_in_reg) {
                    ctx.load("r" + op1, ir.op1, out);
                }
                if (!op2_in_reg) {
                    ctx.load("r" + op2, ir.op2, out);
                }
                out.println("    ORRS r12, r" + op1 + ", r" + op2);
                out.println("    MOVEQ r" + dest + ", #0");
                out.println("    MOVNE r" + dest + ", #1");
                if (!dest_in_reg) {
                    ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                }
            } else if (ir.op_code == IR.OpCode.STORE) {
                // op1 基地址
                boolean op3_in_reg =
                        ir.op3.type == OpName.Type.Var && ctx.var_in_reg(ir.op3.name);
                int op3 = op3_in_reg ? ctx.var_to_reg.get(ir.op3.name) : 11;
                if (ir.op1.type == OpName.Type.Var &&
                        ir.op1.name.startsWith("%&") &&
                        ir.op2.type == OpName.Type.Imm) {
                    if (!op3_in_reg) ctx.load("r" + op3, ir.op3, out);
                    int offset = ctx.resolve_stack_offset(ir.op1.name) + ir.op2.value;
                    ctx.store_to_stack_offset("r" + op3, offset, out, "STR");
                } else {
                    boolean op2_in_reg =
                            ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
//                    int op1 = 11;
//                    int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 12;
//                    if (!op2_in_reg) ctx.load("r" + op2, ir.op2, out);
                    int op1 = 12;
                    int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 11;
                    boolean offset_is_small =
                            ir.op2.type == OpName.Type.Imm && ir.op2.value >= 0 && ir.op2.value < 256;
                    if (!op2_in_reg && !offset_is_small)
                        ctx.load("r" + op2, ir.op2, out);
                    ctx.load("r" + op1, ir.op1, out);
//                    out.println("    ADD r12, r" + op1 + ", r" + op2);
                    if(!offset_is_small)
                        out.println("   ADD r12, r"+op1+", r"+op2);
                    if (!op3_in_reg) ctx.load("r" + op3, ir.op3, out);
//                    out.println("    STR r" + op3 + ", [r12]");
                    out.println("    STR r" + op3 + ", ["
                            + (offset_is_small
                            ? "r" + op1 + ",#" +ir.op2.value
                            : "r12")
                            +"]" );
                }
            } else if (ir.op_code == IR.OpCode.LOAD) {
                boolean dest_in_reg = ctx.var_in_reg(ir.dest.name);
                int dest = dest_in_reg ? ctx.var_to_reg.get(ir.dest.name) : 12;
                // op1 基地址
                if (ir.op1.type == OpName.Type.Var &&
                        ir.op1.name.startsWith("%&") &&
                        ir.op2.type == OpName.Type.Imm) {
                    int offset = ctx.resolve_stack_offset(ir.op1.name) + ir.op2.value;
                    ctx.load_from_stack_offset("r" + dest, offset, out, "LDR");
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                } else {
                    boolean op2_in_reg =
                            ir.op2.type == OpName.Type.Var && ctx.var_in_reg(ir.op2.name);
//                    int op1 = 11;
//                    int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 12;
                    int op1 = 12;
                    int op2 = op2_in_reg ? ctx.var_to_reg.get(ir.op2.name) : 11;
                    boolean offset_is_small =
                            ir.op2.type == OpName.Type.Imm && ir.op2.value >= 0 && ir.op2.value < 256;

//                    if (!op2_in_reg) ctx.load("r" + op2, ir.op2, out);
                    if (!op2_in_reg && !offset_is_small)
                        ctx.load("r" + op2, ir.op2, out);

                    ctx.load("r" + op1, ir.op1, out);
//                    out.println("    ADD r12, r" + op1 + ", r" + op2);
//                    out.println("    LDR r" + dest + ", [r12]");
                    if(!offset_is_small)
                        out.println("    ADD r12, r" + op1 + ", r" + op2);
                    out.println("    LDR r" + dest + ", ["
                            + (offset_is_small
                            ? "r" + op1 + ",#" + ir.op2.value
                            : "r12")
                            + "]");
                    if (!dest_in_reg) {
                        ctx.store_to_stack("r" + dest, ir.dest, out, "STR");
                    }
                }
            } else if (ir.op_code == IR.OpCode.RET) {
                if (ir.op1.type != OpName.Type.Null) {
                    ctx.load("r0", ir.op1, out);
                }
                if (ctx.has_function_call) ctx.load("lr", new OpName("$ra"), out);

                // 恢复现场
                int offset = 4;
                ctx.load_from_stack_offset("r11", stack_size[2] + stack_size[3], out, "LDR");
                for (int j = 0; j < 11; j++) {
                    if (non_volatile_reg.get(j) != ctx.savable_reg.get(j)) {
                        ctx.load_from_stack_offset(
                                "r" + j, stack_size[2] + stack_size[3] + offset, out, "LDR");
                        offset += 4;
                    }
                }

                if (stack_size[0] + stack_size[1] + stack_size[2] + stack_size[3] > 127) {
                    ctx.load("r12",
                            new OpName(stack_size[0] + stack_size[1] + stack_size[2] + stack_size[3]),
                            out);
                    out.println("    ADD sp, sp, r12");
                } else {
                    out.println("    ADD sp, sp, #"
                            + (stack_size[0] + stack_size[1] + stack_size[2] + stack_size[3])
                    );
                }
                out.println("    MOV PC, LR");
            } else if (ir.op_code == IR.OpCode.LABEL) {
                out.println(ir.label + ":");
            }


        }

    }


}


