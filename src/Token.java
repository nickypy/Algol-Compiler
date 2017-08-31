/*
 * Nick Pagsanjan
 * CS 4110 - Compiler Design
 * Token.java
 *
 * Interface for token objects: contains token number and lexeme
 */

public class Token {
    private int tokenNumber;
    private String lexeme;

    public Token(int tokenNumber, String lexeme) {
        this.tokenNumber = tokenNumber;
        this.lexeme = lexeme;
    }

    public Token(Token t) {
        this(t.getTokenNumber(), t.getLexeme());
    }

    public int getTokenNumber() {
        return this.tokenNumber;
    }

    public String getLexeme() {
        return this.lexeme;
    }

    public String toString() {
        String str = new String();
        str += "**** \n" + "  Lexeme: " + lexeme + "\n  Token ID: " + tokenNumber;
        return str;
    }
}
