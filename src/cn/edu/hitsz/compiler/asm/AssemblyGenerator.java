package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {
    private List<Instruction> instr = new ArrayList<>();
    private HashMap<IRValue, Integer> lastUse = new HashMap<>(); // 变量最后一次使用的位置(instr列表中的下标)
    private HashMap<IRVariable, String> regMap = new HashMap<>(); // 各个变量占用寄存器的情况
    List<String> freeRegs = new ArrayList<>(List.of("t0", "t1", "t2", "t3", "t4", "t5", "t6")); // 可用的寄存器
    List<String> asmList = new ArrayList<>(); // 汇编列表


    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        List<IRValue> temp = new ArrayList<>();
        int pos = 0;
        for (Instruction in : originInstructions) {
            temp = in.getOperands();
            switch (temp.size()) {
                case 2 -> {
                    // 二元运算
                    IRValue first = in.getLHS();
                    IRValue second = in.getRHS();
                    IRValue target;
                    if (first.isImmediate() && second.isImmediate()) {
                        // 没有变量
                        IRValue res = IRImmediate.of(((IRImmediate) first).getValue() + ((IRImmediate) second)
                                .getValue());
                        this.instr.add(Instruction.createMov(in.getResult(), res));
                    } else if ((target = first.isImmediate() ? first : second).isImmediate()) {
                        // 一个变量
                        if (in.getKind() == InstructionKind.MUL || (in.getKind() == InstructionKind.SUB &&
                                target == first)) {
                            // 乘法或立即数在左边的减法
                            IRVariable tmp = IRVariable.temp();
                            IRValue rightValue = (target == first ? second : first);
                            this.instr.add(Instruction.createMov(tmp, target));
                            pos++;

                            if (in.getKind() == InstructionKind.MUL) {
                                this.instr.add(Instruction.createMul(in.getResult(), tmp, rightValue));
                            } else {
                                this.instr.add(Instruction.createSub(in.getResult(), tmp, rightValue));
                            }
                            lastUse.put(tmp, pos);
                            lastUse.put(rightValue, pos);

                        } else {
                            if (first.isImmediate()) {
                                this.instr.add(in.swap());
                                lastUse.put(second, pos);
                            } else {
                                lastUse.put(first, pos);
                            }
                        }
                    } else {
                        // 两个变量
                        this.instr.add(in);
                        lastUse.put(first, pos);
                        lastUse.put(second, pos);
                    }
                    lastUse.put(in.getResult(), pos);
                }
                case 1 -> {

                    instr.add(in);
                    if (in.getKind() == InstructionKind.RET) {
                        if (in.getReturnValue().isIRVariable()) {
                            lastUse.put(in.getReturnValue(), pos);
                        }
                        return;
                    } else {
                        if (in.getFrom().isIRVariable()) {
                            lastUse.put(in.getFrom(), pos);
                        }
                        lastUse.put(in.getResult(), pos);
                    }
                }
            }
            pos++;
        }
//        throw new NotImplementedException();
    }

    /**
     * 获取变量所属的寄存器 会尝试分配寄存器
     *
     * @param variable IRVariable
     * @param pos      变量最后一次使用的位置(instr列表中的下标)
     * @return 寄存器名
     */
    String regAlloc(IRVariable variable, int pos) {
        String reg;
        if (regMap.containsKey(variable)) {
            // 已分配寄存器需要检查是否是最后一次使用, 如果是的话释放该寄存器
            reg = regMap.get(variable);
            if (pos == lastUse.get(variable)) {
                freeRegs.add(reg);
            }
            return regMap.get(variable);
        }
        // 不完备的寄存器分配
        if (freeRegs.size() == 0) {
            throw new RuntimeException("Not Enough regs" + pos);
        }
        // 分配空闲寄存器 上面已经检测了变量最后一次使用 这里无需考虑
        reg = freeRegs.remove(0);
        regMap.put(variable, reg);
        return reg;
    }

    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        asmList.add(".text");
        int pos = 0;
        for (Instruction in : instr) {
            switch (in.getOperands().size()) {
                case 2 -> {
                    String iName = in.getKind().name().toLowerCase();
                    String rhs, lhs, res;
                    if (in.getRHS().isImmediate()) {
                        // 判断是否为I型指令
                        rhs = in.getRHS().toString();
                        iName += 'i';
                    } else {
                        rhs = regAlloc((IRVariable) in.getRHS(), pos);
                    }
                    lhs = regAlloc((IRVariable) in.getLHS(), pos);
                    res = regAlloc(in.getResult(), pos);
                    asmList.add(String.format("\t%s %s, %s, %s\t\t#  %s", iName, res, lhs, rhs, in));
                }
                case 1 -> {
                    if (in.getKind() == InstructionKind.MOV) {
                        String asm;
                        String from;
                        if (in.getFrom().isImmediate()) {
                            asm = "\tli %s, %s\t\t#  %s";
                            from = in.getFrom().toString();
                        } else {
                            asm = "\tmv %s, %s\t\t#  %s";
                            from = regAlloc((IRVariable) in.getFrom(), pos);
                        }
                        String res = regAlloc(in.getResult(), pos);
                        asmList.add(String.format(asm, res, from, in));
                    } else {
                        if (in.getReturnValue().isImmediate()) {
                            asmList.add(String.format("\tli a0, %s\t\t#  %s", in.getReturnValue(), in));
                        } else {
                            asmList.add(String.format("\tmv a0, %s\t\t#  %s", regAlloc((IRVariable) in.getReturnValue(), pos), in));
                        }
                    }
                }
            }
            pos++;
        }
//        asmList.add("\n");
        // TODO: 执行寄存器分配与代码生成
//        throw new NotImplementedException();
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        FileUtils.writeLines(path, asmList);
//        throw new NotImplementedException();
    }
}

