/*
 * Nick Pagsanjan
 * CS 4110
 * Variable.java
 *
 * Child class of token.java which adds variable types
 */

public class Variable extends Token {
    private char type;   // types include i for integer, l for logical, s for string
    private int  offset; // offset assigned by the symbol table

    public Variable(int tokenNumber, String lexeme, char type) {
        super(tokenNumber, lexeme.toLowerCase());
        this.type = type;
    }

    public Variable(int tokenNumber, String lexeme, char type, int offset) {
        this(tokenNumber, lexeme.toLowerCase(), type);
        this.offset = offset;
    }

    public Variable(Variable v) {
        this(v.getTokenNumber(), v.getLexeme(), v.getType(), v.getOffset());
    }

    public Variable(Token t, char type) {
        this(t.getTokenNumber(), t.getLexeme(), type);
    }

    public void setType(char type) {
        this.type = type;
    }

    public char getType() {
        return type;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        String str = new String();

        if (type == 'i') {
            str += "INTEGER";
        } else if (type == 'l') {
            str += "LOGICAL";
        } else if (type == 's') {
            str += "STRING";
        } else {
            str += "INVALID TYPE";
        }

        str += " " + getLexeme() + " " + getOffset() + "\n";

        return str;
    }
}
