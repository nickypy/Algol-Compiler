/*
 * Nick Pagsanjan
 * CS 4110 - Compiler Design
 * Scanner.java
 * 
 * Scans lexical elements of a source file and returns token number and lexeme
 */


import java.io.BufferedReader;  // allows for use as a read-by-line buffer
import java.io.FileReader;      // BufferedReader's constructor takes in a FileReader object
import java.io.PrintWriter;     // allows writing out to listing file
import java.io.IOException;     // allows for throwing IOExcption
import java.lang.StringBuilder; // buffer for error messages to be written at the end
import java.util.HashSet;       // allows for O(1) keyword lookup
import java.util.ArrayList;     // storage for tokens 

public class Scanner {
    public static final char EOL = '$';

    // some useful states
    public static final int COMMENT_STATE = -2;
    public static final int DEAD_STATE    = -1;
    public static final int WHITESPACE    =  0;

    // token numbers for each token
    public static final int IDENTIFIER_ID     = 1;
    public static final int LITERAL_ID        = 2;  // any literal string, int, etc.
    public static final int TYPE_ID           = 3;  // STRING, INTEGER, keywords etc.
    public static final int ADD_OP_ID         = 4;  // '+' 0R '-'
    public static final int MULT_OP_ID        = 5;  // '*', '/', DIV, REM, ADD
    public static final int RELATIONAL_OP_ID  = 6;  // '=', '!=', '<', '>'
    public static final int BEGIN_ID          = 7;  // BEGIN
    public static final int END_ID            = 8;  // END
    public static final int IF_ID             = 9;  // IF
    public static final int THEN_ID           = 10; // THEN
    public static final int WHILE_ID          = 11; // WHILE
    public static final int DO_ID             = 12; // DO
    public static final int IO_ID             = 13; // READ, WRITE, WRITELN
    public static final int OPEN_PARENS_ID    = 14; // '('
    public static final int CLOSING_PARENS_ID = 15; // ')'
    public static final int SEMICOLON_ID      = 16; // ';'
    public static final int BOOLEAN_NOT_ID    = 17; // '!' 
    public static final int END_OF_PROGRAM_ID = 18; // '.'
    public static final int ASSIGNMENT_ID     = 19; // ':='

    // private member variables
    private BufferedReader buffer;   // buffer for reading file and storing one line at a time
    private ArrayList<Token> tokens; // storage for storing Token objects
    private PrintWriter fileOut;     // object to write out to listing file
    private StringBuilder errors;    // buffer for error messages
    private HashSet<String> keywords;// fast keyword lookup
    private int currentToken;        // for GetToken which returns one token at a time

    // class constructor
    // throws an IOException if file cannot be opened
    public Scanner(BufferedReader buffer) throws IOException {
        this.buffer       = buffer;
        this.tokens       = new ArrayList<Token>();
        this.fileOut      = new PrintWriter("listing_file.txt");
        this.errors       = new StringBuilder();
        this.keywords     = new HashSet<String>();
        this.currentToken = 0;

        // add all keywords to hash set
        keywords.add("begin");
        keywords.add("end");
        keywords.add("if");
        keywords.add("then");
        keywords.add("while");
        keywords.add("do");
        keywords.add("read");
        keywords.add("write");
        keywords.add("writeln");
        keywords.add("div");
        keywords.add("rem");
        keywords.add("add");
        keywords.add("string");
        keywords.add("logical");
        keywords.add("integer");
        keywords.add("comment");
        keywords.add("true");
        keywords.add("false");

        findTokens();
    }

    // print all tokens
    public void printAllTokens() {
        String str = new String();

        for (Token t : tokens) {
            str += t.getLexeme() + " ";
        }

        System.out.println(str);
    }

    // returns respective token number for certain keywords
    private int checkKeyword(String tempToken) {
        if (!keywords.contains(tempToken)) {
            return DEAD_STATE;
        } else {
            if (tempToken.equals("begin")) {
                return BEGIN_ID;
            } else if (tempToken.equals("end")) {
                return END_ID;
            } else if (tempToken.equals("if")) {
                return IF_ID;
            } else if (tempToken.equals("then")) {
                return THEN_ID;
            } else if (tempToken.equals("while")) {
                return WHILE_ID;
            } else if (tempToken.equals("do")) {
                return DO_ID;
            } else if (tempToken.equals("read") || tempToken.equals("write") || tempToken.equals("writeln")) {
                return IO_ID;
            } else if (tempToken.equals("div") || tempToken.equals("rem") || tempToken.equals("add")) {
                return MULT_OP_ID;
            } else if (tempToken.equals("string") || tempToken.equals("integer") || tempToken.equals("logical")) {
                return TYPE_ID;
            } else if (tempToken.equals("true") || tempToken.equals("false")) {
                return LITERAL_ID;
            } else if (tempToken.equals("comment")) {
                return COMMENT_STATE;
            } else {
                return DEAD_STATE;
            }
        }
    }

    // checks current line for specific tokens
    private int checkToken(String line, int currentPos) {
        if (line.charAt(currentPos) == '+' || line.charAt(currentPos) == '-') {
            return ADD_OP_ID;
        } else if (line.charAt(currentPos) == '!' && currentPos < line.length() - 1 && line.charAt(currentPos+1) == '=' ) {
            return RELATIONAL_OP_ID;        
        } else if (line.charAt(currentPos) == ':' && currentPos < line.length() - 1 && line.charAt(currentPos+1) == '=' ) {
            return ASSIGNMENT_ID;
        } else if (line.charAt(currentPos) == '*' || line.charAt(currentPos) == '/') {
            return MULT_OP_ID;
        } else if (line.charAt(currentPos) == '=' || line.charAt(currentPos) == '<' || line.charAt(currentPos) == '>') {
            return RELATIONAL_OP_ID;
        } else if (line.charAt(currentPos) == '(') {
            return OPEN_PARENS_ID;
        } else if (line.charAt(currentPos) == ')') {
            return CLOSING_PARENS_ID;
        } else if (line.charAt(currentPos) == ';') {
            return SEMICOLON_ID;
        } else if (line.charAt(currentPos) == '!') {
            return BOOLEAN_NOT_ID;
        } else if (line.charAt(currentPos) == '.') {
            return END_OF_PROGRAM_ID;
        }  else {
            return DEAD_STATE;
        }
    }

    // looks forward one char
    private char peek(String line, int currentPos) {
        // token is completed by whitespace
        if (currentPos < line.length() - 1) {
            return line.charAt(currentPos + 1);
        } else { // (currentPos == line.length() - 1), token is at EOL
            return EOL;
        } 
    }

    // reads file into buffer line by line and finds tokens
    // throws an IOException if BufferedReader cannot read a line
    // returns a Token object containing token number and lexeme
    // based on DFA that defines AlgolW lexemes
    // prints out listing file containing input file with line numbers
    // and error messages
    public void findTokens() throws IOException {
        int lineNumber = 0;
        int stateNumber = 0;

        boolean commentOn = false; // true when we see a comment token
        String line = buffer.readLine();
        while (line != null) {
            line = line.toLowerCase();
            lineNumber += 1;
            fileOut.println(lineNumber + " " + line);

            boolean foundToken = false; // true when token is ready to be stored
            boolean stringOn = false; // true when we see a beginning of string token
            int readingPos = 0; // current position where token starts in string

            for (int currentPos = 0; currentPos < line.length(); currentPos++) {
                if (!commentOn) {
                    if (stringOn) {
                        if (line.charAt(currentPos) == '"' && stateNumber == LITERAL_ID) {
                            foundToken = true;
                            stringOn = false;
                        } else {
                            continue;
                        }
                    } else if (line.charAt(currentPos) == '"' && stateNumber == 0) {
                        stateNumber = LITERAL_ID; 
                        stringOn = true;
                        continue;
                    } else if (Character.isWhitespace(line.charAt(currentPos)) && stateNumber != LITERAL_ID) {
                        stateNumber = 0;
                        readingPos++;
                        continue; // eat whitespace
                    } else if (Character.isLetter(line.charAt(currentPos)) && stateNumber != LITERAL_ID) {
                        if (stateNumber == 0) {
                            stateNumber = IDENTIFIER_ID;
                        }
                        char ch = peek(line, currentPos);
                        if (!Character.isLetter(ch) && !Character.isDigit(ch) || ch == EOL) {
                            foundToken = true;
                        } else {
                            continue;
                        }
                    } else if (Character.isDigit(line.charAt(currentPos))) {
                        //if (stateNumber == 0) {
                        stateNumber = LITERAL_ID;

                        if (line.charAt(currentPos) == '0') { 
                            foundToken = true;
                        } 
                       // } else 
                        if (stateNumber == LITERAL_ID) {
                            char ch = peek(line, currentPos);
                            if (!Character.isDigit(ch) || ch == EOL) {
                                foundToken = true;
                            } else {
                                continue;
                            }
                        }
                    } else {
                        if (stateNumber == 0) { 
                            stateNumber = checkToken(line, currentPos);
                        }
                        if (stateNumber == RELATIONAL_OP_ID || stateNumber == ASSIGNMENT_ID) {
                            if (line.charAt(currentPos) == '!' || line.charAt(currentPos) == ':') currentPos++;
                            foundToken = true;
                        } else if (stateNumber != DEAD_STATE) {
                            foundToken = true;
                        }
                    }

                    if (foundToken && stateNumber > 0) {
                        String tempToken = line.substring(readingPos, currentPos + 1);
                        if (stateNumber == IDENTIFIER_ID) {
                            int test = checkKeyword(tempToken);
                            if (test > 0 || test == COMMENT_STATE) {
                                stateNumber = test;
                            }
                        }

                        if (stateNumber == COMMENT_STATE) {
                            commentOn = true;
                            continue;
                        } 

                        Token temp = new Token(stateNumber, tempToken);
                        tokens.add(temp);
                        readingPos = currentPos + 1;
                        stateNumber = 0;
                    } else if (stateNumber == DEAD_STATE) {
                        errors.append("Token error on line " + lineNumber + '\n');
                    }
                } else if (line.charAt(currentPos) == ';') {
                    commentOn = false;
                    continue;
                }
            }

            if (stringOn) {
                errors.append("Incomplete string error on line " + lineNumber + '\n');
            }
            line = buffer.readLine();
        }
        fileOut.println(errors.toString());
        fileOut.close();
    }

    // returns one token at a time, return null when no more tokens
    public Token getToken() {
        if (tokens.size() > currentToken) {
            return tokens.get(currentToken++);
        } else {
            return null;
        }
    }
}
