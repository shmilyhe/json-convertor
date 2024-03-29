package io.shmilyhe.convert.ast.expression;

import io.shmilyhe.convert.ast.token.ITokenizer;

public class Expression {
    public static final String TYPE_ID="Identifier";
    public static final String TYPE_BIN="BinaryExpression";
    public static final String TYPE_CALL="CallExpression";
    public static final String TYPE_LIT="Literal";
    public static final String TYPE_ASSIGN="AssignmentExpression";
    public static final String TYPE_UPDATE="UpdateExpression";
    public static final String TYPE_ARRAY="SequenceExpression";
    
    
    protected String type;
    protected int start;
    protected int end;
    protected int line;

    protected boolean minus;

    protected boolean returns;

    public boolean isReturns() {
        return returns;
    }

    public void setReturns(boolean returns) {
        this.returns = returns;
    }

    public boolean isMinus() {
        return minus;
    }

    public boolean isAssignment(){
        return false;
    }

    public Expression setMinus(boolean minus) {
        this.minus = minus;
        return this;
    }

    public int getLine() {
        return line;
    }

    public Expression setLine(int line) {
        this.line = line;
        return this;
    }
    private ITokenizer tokens;

    public ITokenizer getTokens() {
        return tokens;
    }

    public Expression setTokens(ITokenizer tokens) {
        this.tokens = tokens;
        return this;
    }

    public String getType() {
        return type;
    }
    public Expression setType(String type) {
        this.type = type;
        return this;
    }
    public int getStart() {
        return start;
    }
    public Expression setStart(int start) {
        this.start = start;
        return this;
    }
    public int getEnd() {
        return end;
    }
    public Expression setEnd(int end) {
        this.end = end;
        return this;
    }
}
