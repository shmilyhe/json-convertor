package io.shmilyhe.convert.tokenizer;

/**
 * 脚本字符令牌化
 */
public interface ITokenizer {
    char next();
    boolean hasNext();
    String toLineEnd();
    int offset();
    int column();
    int line();
    String tillNext(char c);
    void back(char ch);
    String whitespace();
    String toSymbol();
} 
