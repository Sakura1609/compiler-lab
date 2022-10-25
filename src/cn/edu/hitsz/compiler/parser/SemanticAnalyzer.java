package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Symbol;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {
    private static int index = 0;
    private Stack<Token> tokens = new Stack<>();
    private SymbolTable memTable = new SymbolTable();
    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
        System.out.println("SemanticAnalyzer Success");
//        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        int length = production.body().size();
//        throw new NotImplementedException();
        switch (production.index()) {
            case 1, 2, 3, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 -> { // P -> S_list and others
                for (int i = 0; i < length; i++) {
                    tokens.pop();
                }
                tokens.push(null);
            }

            case 4 -> { // S -> D id
                memTable.get(tokens.pop().getText()).setType(SourceCodeType.Int);
                tokens.pop();
                tokens.push(null);
            }

            case 5 -> { // D -> int

            }
            // ...
            default -> { //
                // throw new RuntimeException("Unknown production index");
                // 或者任何默认行为
            }
        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        tokens.push(currentToken);
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        memTable = table;
//        throw new NotImplementedException();
    }
}

