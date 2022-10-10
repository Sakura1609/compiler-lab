package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.lexer.Token;

public class Symbol {
    Token token;
    NonTerminal nonTerminal;

    private Symbol(Token token, NonTerminal nonTerminal) {
        this.token = token;
        this.nonTerminal = nonTerminal;
    }

    public Symbol(Token token) {
        new Symbol(token, null);
    }

    public Symbol(NonTerminal nonTerminal) {
        new Symbol(null, nonTerminal);
    }

    public boolean isToken() {
        return this.token != null;
    }

    public boolean isNonTerminal() {
        return this.nonTerminal != null;
    }
}
