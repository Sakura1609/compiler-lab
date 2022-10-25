package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    Stack<IRValue> irStack = new Stack<>();
    ArrayList<Instruction> IL = new ArrayList<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
//        throw new NotImplementedException();
        if ("id".equals(currentToken.getKindId())) {
            irStack.push(IRVariable.named(currentToken.getText()));
        } else if ("IntConst".equals(currentToken.getKindId())) {
            irStack.push(IRImmediate.of(Integer.parseInt(currentToken.getText())));
        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
//        throw new NotImplementedException();
        switch (production.index()) {
            case 6 -> {     // S -> id = E;
                IRValue second = irStack.pop();
                IRValue first = irStack.pop();
                IL.add(Instruction.createMov((IRVariable)first, second));
            }
            case 7 -> {     // S -> return E;
                IL.add(Instruction.createRet(irStack.pop()));
            }
            case 8 -> {     // E -> E + A;
                IRValue second = irStack.pop();
                IRValue first = irStack.pop();
                IRVariable target = IRVariable.temp();
                IL.add(Instruction.createAdd(target, first, second));
                irStack.push(target);
            }
            case 9 -> {     // E -> E - A;
                IRValue second = irStack.pop();
                IRValue first = irStack.pop();
                IRVariable target = IRVariable.temp();
                IL.add(Instruction.createSub(target, first, second));
                irStack.push(target);
            }
            case 11 -> {    // A -> A * B;
                IRValue second = irStack.pop();
                IRValue first = irStack.pop();
                IRVariable target = IRVariable.temp();
                IL.add(Instruction.createMul(target, first, second));
                irStack.push(target);
            }
            case 1, 10, 12, 14, 15 -> {    // E -> A;

            }
            case 2, 3, 4, 5, 13 -> {

            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
//        throw new NotImplementedException();
        System.out.println("IRGenerator Success\n");
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
//        throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        return IL;
//        throw new NotImplementedException();
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

