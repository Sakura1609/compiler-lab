package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import javax.naming.NameNotFoundException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private ArrayList<String> tokens;
    private ArrayList<Token> tokenList;

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        tokens = new ArrayList<>();
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        try {
            String s = Files.readString(Paths.get(path));
            char[] page = s.toCharArray();
            ArrayList<String> rowTokens = new ArrayList<>();
            for (int i = 0; i < page.length; i++) {
                if (rowTokens == null) {
                    rowTokens = new ArrayList<>();
                }
                if (page[i] == '\r' || page[i] == '\n') {
                    continue;
                } else if (page[i] == '=' || page[i] == ';' || page[i] == '+' || page[i] == ',' || page[i] == '-' ||
                            page[i] == '*' || page[i] == '/' || page[i] == '(' || page[i] == ')') {
                    if (rowTokens.size() != 0) {
                        tokens.add(String.join("", rowTokens));
                    }
                    tokens.add(Character.toString(page[i]));
                    rowTokens = null;
                    continue;
                } else if (page[i] == ' ') {
                    if (rowTokens.size() != 0) {
                        tokens.add(String.join("", rowTokens));
                    }
                    rowTokens = null;
                    continue;
                }
                rowTokens.add(Character.toString(page[i]));
            }
        } catch (IOException e) {
            System.out.println("readError");
        }
//        throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        tokenList = new ArrayList<Token>();
        for (int i = 0; i < tokens.size(); i++) {
            if (!TokenKind.isAllowed(tokens.get(i)) && !Objects.equals(tokens.get(i), ";")) {
                try {
                    Integer.parseInt(tokens.get(i));
                    tokenList.add(Token.normal("IntConst", tokens.get(i)));
                    System.out.println(tokenList.get(tokenList.size()-1));
                } catch (NumberFormatException e) {
                    tokenList.add(Token.normal("id", tokens.get(i)));
                    System.out.println(tokenList.get(tokenList.size()-1));
                    if (!symbolTable.has(tokens.get(i))) {
                        symbolTable.add(tokens.get(i));
                    }
                }
            } else if (Objects.equals(tokens.get(i), ";")) {
                tokenList.add(Token.simple("Semicolon"));
            } else {
                tokenList.add(Token.simple(tokens.get(i)));
                System.out.println(tokenList.get(tokenList.size()-1));
            }
        }
        assert(tokenList.size() != 0);
        tokenList.add(Token.eof());
//        throw new NotImplementedException();
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表`
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return tokenList;
//        throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
