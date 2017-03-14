/*
 * Nick Pagsanjan
 * CS 4110
 * RDParser.java
 *
 * Implementation of AlgolW EBNF grammar using recursive descent parsing
 * with Code Generation
 *
 * Grammar shown below:
 *
 *  (1)  program     :  blockst '.'
 *  (2)  statmt      :  decl  |  assstat  |  ifstat  |  blockst  |
 *                      loopst  |  iostat  |  <empty>
 *  (3)  decl        :  TYPE_ID IDENTIFIER_ID
 *  (4)  assstat     :  idref  ASSIGNMENT_ID  expression
 *  (5)  ifstat      :  IF_ID  expression  THEN_ID  statmt
 *  (6)  loopst      :  WHILE_ID  expression  DO_ID  statmt
 *  (7)  blockst     :  BEGIN_ID  { statmt SEMICOLON_ID }  END_ID
 *  (8)  iostat	     :  IO_ID  OPEN_PARENS_ID  idref CLOSING_PARENS_ID |
 *                      IO_ID  OPEN_PARENS_ID  expression  CLOSING_PARENS_ID
 *  (9)  expression  :  term { ADD_OP_ID term }
 *  (10) term        :  relfactor { MULT_OP_ID relfactor }
 *  (11) relfactor   :  factor  [ RELATIONAL_OP_ID factor ]
 *  (12) factor      :  idref  |  LITERAL_ID  |  BOOLEAN_NOT_ID factor  |
 *                      OPEN_PARENS_ID  expression  CLOSING_PARENS_ID
 *  (13) idref       :  IDENTIFIER_ID
 *
 *  Token IDs are defined in Scanner.java
 */

import java.util.ArrayList;

public class RDParser {

    private Token         currentToken;      // current token to begin derivation
    private SymbolTable   symbolTable;       // stores variables and scopes
    private Scanner       scanner;           // scanner for getToken()
    private StringBuilder derivation;        // buffer for derivation
    private StringBuilder errors;            // buffer for error messages
    private StringBuilder verboseDerivation; // verbose derivation, for debugging purposes only
    private boolean       isSuccess;         // returns true if input matches grammar
    private boolean       debug;             // true for debug mode
    private CodeGenerator codeGen;           // CodeGenerator object for writing to file
    private ExpressionRecord exprRecord;     // for storing type and location of a variable

    private int currentOffset;

    public RDParser(Scanner scanner, SymbolTable symbolTable, boolean dbg) {
        this.scanner       = scanner;
        this.symbolTable   = symbolTable;
        this.derivation    = new StringBuilder();
        this.errors        = new StringBuilder();
        this.currentToken  = scanner.getToken();
        this.isSuccess     = true;
        this.codeGen       = new CodeGenerator();
        this.exprRecord    = new ExpressionRecord();
        this.currentOffset = 0;
        this.debug         = dbg;

        if (dbg) {
            verboseDerivation = new StringBuilder();
        } else {
            verboseDerivation = null;
        }

        // begin parse
        program();

        // null is EOF for BufferedReader class
        // write to file happens ONLY when parse is sucessful
        if (currentToken == null || this.isSuccess) {
            codeGen.writeToFile();
        } else {
        	System.out.println(errors.toString());
        }
    }

    public String debug() {
        return this.verboseDerivation.toString();
    }

    public String getDerivation() {
        return this.derivation.toString();
    }

    public boolean isSuccessful() {
        return this.isSuccess;
    }

    public String toString() {
        return this.derivation.toString() + '\n'
            + this.errors.toString() + '\n'
            + ((this.isSuccess) ? "SUCCESS":"FAIL");
    }

    // used for determining type of a literal token
    public char getType(Token t) {
        String s = t.getLexeme();

        char begin = s.charAt(0);
        if (begin == 't' || begin == 'f') {
            return 'l';
        } else if (begin == '"') {
            return 's';
        } else {
            return 'i';
        }
    }

    // sets currentToken to a new value if tokens match
    private void match(Token current, int tokenNum) {
        if (current.getTokenNumber() == tokenNum) {
            currentToken = scanner.getToken();
        } else {
            errors.append(
                "Parser error: \n" +
                "    expected " + tokenNum + " but got " + current.getTokenNumber() + '\n' +
                "    Lexeme: " + current.getLexeme() + '\n' +
                "    Derivation: " + derivation.toString() + "\n");

            currentToken = scanner.getToken();
            isSuccess = false;
        }
    }

    /*
     * recursive descent functions start HERE
     */

    //
    // (1) program : blockst '.'
    private void program() {
        derivation.append("1 ");
        if (debug) {
            verboseDerivation.append("Program Start ");
        }

        codeGen.writeProlog();
        blockst();
        match(currentToken, Scanner.END_OF_PROGRAM_ID);
        codeGen.writePostlog();
    }

    //
    // (2) statmt : decl | assstat | ifstat | blockst | loopst | iostat | <empty>
    private void statmt() {
        derivation.append("2 ");
        if (currentToken.getTokenNumber() == Scanner.TYPE_ID) {
            if (debug) {
                verboseDerivation.append("Decl ");
            }
            decl();
        } else if (currentToken.getTokenNumber() == Scanner.IDENTIFIER_ID) {
            if (debug) {
                verboseDerivation.append("Assgn ");
            }
            assstat();
        } else if (currentToken.getTokenNumber() == Scanner.IF_ID) {
            if (debug) {
                verboseDerivation.append("IfStat ");
            }
            ifstat();
        } else if (currentToken.getTokenNumber() == Scanner.BEGIN_ID) {
            if (debug) {
                verboseDerivation.append("NewBlock ");
            }
            blockst();
        } else if (currentToken.getTokenNumber() == Scanner.WHILE_ID) {
            if (debug) {
                verboseDerivation.append("WhileStat ");
            }
            loopst();
        } else if (currentToken.getTokenNumber() == Scanner.IO_ID) {
            if (debug) {
                verboseDerivation.append("IOStat ");
            }
            iostat();
        }
    }

    //
    // (3) decl : IDENTIFIER_ID LITERAL_ID
    private void decl() {
        derivation.append("3 ");
        if (currentToken.getTokenNumber() == Scanner.TYPE_ID) {
            char type = currentToken.getLexeme().charAt(0);
            match(currentToken, Scanner.TYPE_ID);

            String name = currentToken.getLexeme();
            match(currentToken, Scanner.IDENTIFIER_ID);

            // if not already in Symbol table, add it
            if (symbolTable.findCurrentScope(name) == null){
                symbolTable.insert(new Variable(currentToken.getTokenNumber(), name, type));
            } else {
                errors.append("Token '" + name + "' already declared is this scope");
                isSuccess = false;
            }
        }
    }

    //
    // (4) assstat : idref ASSIGNMENT_ID expression
    private void assstat() {
        derivation.append("4 ");
        Variable temp = symbolTable.findAllScopes(currentToken.getLexeme());

        if (temp == null) {
            errors.append("Variable '" + currentToken.getLexeme() + "' not declared");
        } else {
            char idType   = temp.getType();
            int idLoc     = temp.getOffset();

            idref();
            match(currentToken, Scanner.ASSIGNMENT_ID);
            expression();

            if (idType == exprRecord.getType()) {
                codeGen.writeCode("# assgn statement");
                codeGen.writeCode("lw $t0 " + exprRecord.getLocation() + "($fp)");
                codeGen.writeCode("sw $t0 " + idLoc + "($fp)");
            } else {
                errors.append("Type mismatch for: " + currentToken.getLexeme());
                isSuccess = false;
            }
        }
    }

    //
    // (5) ifstat : IF_ID expression THEN_ID statmt
    private void ifstat() {
        derivation.append("5 ");
        if (currentToken.getTokenNumber() == Scanner.IF_ID) {
            // TODO: cogegen
            match(currentToken, Scanner.IF_ID);
            expression();

            if (exprRecord.getType() != 'l') {
                errors.append("Type mismatch for if statement");
                isSuccess = false;
            }

            String tempLabel = ExpressionRecord.generateLabel();
            // load er
            // gen next label
            // beq $t0 label
            // code gen label
            codeGen.writeCode("# if statement");
            codeGen.writeCode("lw $t0 " + exprRecord.getLocation() + "($fp)");
            codeGen.writeCode("beq $t0 $zero " + tempLabel);

            match(currentToken, Scanner.THEN_ID);
            statmt();
            codeGen.writeCode(tempLabel + ": ");
        }
    }

    //
    // (6) loopst : WHILE_ID expression DO_ID statmt
    private void loopst() {
        derivation.append("6 ");
        if (currentToken.getTokenNumber() == Scanner.WHILE_ID) {
            String topWhileLabel = ExpressionRecord.generateLabel();
            String botWhileLabel = ExpressionRecord.generateLabel();

            match(currentToken, Scanner.WHILE_ID);
            codeGen.writeCode("# while statement");
            codeGen.writeCode(topWhileLabel + ": ");
            expression();
            match(currentToken, Scanner.DO_ID);

            if (exprRecord.getType() != 'l') {
                errors.append("Type mismatch in while-loop");
                this.isSuccess = false;
            }
            codeGen.writeCode("lw $t0 " + exprRecord.getLocation() + "($fp)");
            codeGen.writeCode("beq $t0 $zero " + botWhileLabel);
            statmt();
            codeGen.writeCode("j " + topWhileLabel);
            codeGen.writeCode(botWhileLabel + ": ");
        }
    }

    //
    // (7) blockst : BEGIN_ID { statmt SEMICOLON_ID } END_ID
    private void blockst() {
        derivation.append("7 ");
        if (currentToken.getTokenNumber() == Scanner.BEGIN_ID) {
            match(currentToken, Scanner.BEGIN_ID);
            symbolTable.enterNewScope();
            while (currentToken.getTokenNumber() != Scanner.END_ID) {
                statmt();
                match(currentToken, Scanner.SEMICOLON_ID);
            }
            match(currentToken, Scanner.END_ID);
            symbolTable.leaveCurrentScope();
        }
    }

    //
    // (8) iostat : IO_ID OPEN_PARENS_ID idref CLOSING_PARENS_ID |
    //              IO_ID OPEN_PARENS_ID expression CLOSING_PARENS_ID
    private void iostat() {
        derivation.append("8 ");
        if (currentToken.getTokenNumber() == Scanner.IO_ID) {
            String str = currentToken.getLexeme();

            match(currentToken, Scanner.IO_ID);
            match(currentToken, Scanner.OPEN_PARENS_ID);

            if (str.equals("read")) {
                idref();
            } else {
                expression();
            }

            if (str.equals("write")) {
                // writing an int

                if (debug) {
                    verboseDerivation.append("Write ");
                }
                if (exprRecord.getType() == 'i') {
                    codeGen.writeCode("# write statement integer");
                    codeGen.writeCode("lw $a0 " + exprRecord.getLocation() + "($fp)");
                    codeGen.writeCode("li $v0 1");
                    codeGen.writeCode("syscall\n");
                } else if (exprRecord.getType() == 'l') {
                    // writing a logical (bool)
                    // True and False labels should already be declared
                    codeGen.writeCode("# write statement logical");
                    codeGen.writeCode("lw $a0 " + exprRecord.getLocation());
                    codeGen.writeCode("li $v0 1");
                    codeGen.writeCode("syscall\n");
                } else if (exprRecord.getType() == 's') {
                    // writing a string
                    codeGen.writeCode("# write statement string");
                    codeGen.writeCode("la $a0 " + exprRecord.getStrLocation());
                    codeGen.writeCode("li $v0 4");
                    codeGen.writeCode("syscall\n");
                }
            } else if (str.equals("writeln")) {
                // write, but with '\n'at the end
                // "endl" should already be declared in postlog
                //
                if (debug) {
                    verboseDerivation.append("Writeln ");
                }
                if (exprRecord.getType() == 'i') {
                    codeGen.writeCode("# writeln statement integer");
                    codeGen.writeCode("lw $a0 " + exprRecord.getLocation() + "($fp)");
                    codeGen.writeCode("li $v0 1");
                    codeGen.writeCode("syscall\n");
                    codeGen.writeCode("la $a0 endl");
                    codeGen.writeCode("li $v0 4");
                    codeGen.writeCode("syscall\n");
                } else if (exprRecord.getType() == 'l') {
                    // True and False labels should already be declared

                    codeGen.writeCode("# writeln statement logical");
                    codeGen.writeCode("lw $a0 " + exprRecord.getLocation() + "($fp)");
                    codeGen.writeCode("li $v0 1");
                    codeGen.writeCode("syscall\n");
                    codeGen.writeCode("la $a0 endl");
                    codeGen.writeCode("li $v0 4");
                    codeGen.writeCode("syscall\n");

                } else if (exprRecord.getType() == 's') {

                    codeGen.writeCode("# writeln statement string");
                    codeGen.writeCode("la $a0 " + exprRecord.getStrLocation());
                    codeGen.writeCode("li $v0 4");
                    codeGen.writeCode("syscall\n");
                    codeGen.writeCode("la $a0 endl");
                    codeGen.writeCode("li $v0 4");
                    codeGen.writeCode("syscall\n");

                }
            } else if (str.equals("read")) {
                if (exprRecord.getType() == 'i') {
                    if (debug) {
                        verboseDerivation.append("Read ");
                    }

                    codeGen.writeCode("# read statement");
                    codeGen.writeCode("li $v0 5");
                    codeGen.writeCode("syscall");
                    codeGen.writeCode("sw $v0 " + exprRecord.getLocation() + "($fp)");
                } else {
                    errors.append("Read expected an int, but got " + exprRecord.getType());
                    isSuccess = false;
                }
            }

            match(currentToken, Scanner.CLOSING_PARENS_ID);
        }
    }

    //
    // (9) expression : term { ADD_OP_ID term }
    private void expression() {
        derivation.append("9 ");
        term();
        while (currentToken.getTokenNumber() == Scanner.ADD_OP_ID) {
            ExpressionRecord left = new ExpressionRecord();
            // set left to be a copy of exprRecord
            left.setType(exprRecord.getType());
            left.setLocation(exprRecord.getLocation());

            char op = currentToken.getLexeme().charAt(0);

            match(currentToken, Scanner.ADD_OP_ID);
            term();

            if (left.getType() == exprRecord.getType()) {
                String operation;

                if (op == '+') {
                    operation = "add";
                } else if (op == '-') {
                    operation = "sub";
                } else {
                    operation = "bor";
                }

                codeGen.writeCode("# expression");
                codeGen.writeCode("lw $t0 " + left.getLocation() + "($fp)");
                codeGen.writeCode("lw $t1 " + exprRecord.getLocation() + "($fp)");
                codeGen.writeCode(operation + " $t0 $t0 $t1");
                codeGen.writeCode("sw $t0 " + currentOffset + "($fp)");
                exprRecord.setType('i');
                exprRecord.setLocation(currentOffset);
                currentOffset -= SymbolTable.VARIABLE_SIZE;

            } else {
                errors.append("Type mismatch");
                isSuccess = false;
            }
        }
    }

    //
    // (10) term : relfactor { MULT_OP_ID relfactor }
    private void term() {
        derivation.append("10 ");
        relfactor();
        while (currentToken.getTokenNumber() == Scanner.MULT_OP_ID) {
            ExpressionRecord left = new ExpressionRecord();
            // set left to be a copy of exprRecord
            left.setType(exprRecord.getType());
            left.setLocation(exprRecord.getLocation());
            String op = currentToken.getLexeme();

            match(currentToken, Scanner.MULT_OP_ID);
            relfactor();

            boolean isRem = false;
            if (left.getType() == exprRecord.getType()) {
                String operation;

                if (op.equals("*")) {
                    operation = "mult";
                } else if (op.equals("/") || op.equals("rem") || op.equals("div")) {
                    operation = "div";

                    if (op.equals("rem")) {
                        isRem = true;
                    }
                } else {
                    operation = "and";
                }

                codeGen.writeCode("# term");
                codeGen.writeCode("lw $t0 " + left.getLocation() + "($fp)");
                codeGen.writeCode("lw $t1 " + exprRecord.getLocation() + "($fp)");
                codeGen.writeCode(operation + " $t0 $t1");
                if (isRem)
                    codeGen.writeCode("mfhi $t0");
                else
                    codeGen.writeCode("mflo $t0");
                codeGen.writeCode("sw $t0 " + currentOffset + "($fp)");
                exprRecord.setType('i');
                exprRecord.setLocation(currentOffset);
                currentOffset -= 4;

            } else {
                errors.append("Type mismatch");
            }
        }
    }

    //
    // (11) relfactor : factor [ RELATIONAL_OP_ID factor ]
    private void relfactor() {
        derivation.append("11 ");
        factor();
        if (currentToken.getTokenNumber() == Scanner.RELATIONAL_OP_ID) {
            String operator = currentToken.getLexeme();
            ExpressionRecord left = new ExpressionRecord();
            // set left to be a copy of exprRecord
            left.setType(exprRecord.getType());
            left.setLocation(exprRecord.getLocation());

            match(currentToken, Scanner.RELATIONAL_OP_ID);
            factor();

            if (exprRecord.getType() == 'i') {

                codeGen.writeCode("# relfactor");
                codeGen.writeCode("lw $t0 " + left.getLocation() + "($fp)");
                codeGen.writeCode("lw $t1 " + exprRecord.getLocation() + "($fp)");

                if (operator.equals("<")) {
                    codeGen.writeCode("slt $t0 $t0 $t1");
                } else if (operator.equals(">")) {
                    codeGen.writeCode("sgt $t0 $t0 $t1");
                } else if (operator.equals("=")) {
                    codeGen.writeCode("seq $t0 $t1 $t0");
                } else { // operator equals "!="
                    codeGen.writeCode("sne $t0 $t1 $t0");
                }

                codeGen.writeCode("sw $t0 " + currentOffset + "($fp)");
                exprRecord.setType('l');
                exprRecord.setLocation(currentOffset);
                currentOffset -= SymbolTable.VARIABLE_SIZE;
            }
        }
    }

    //
    // (12) factor : idref | LITERAL_ID | BOOLEAN_NOT_ID factor |
    //               OPEN_PARENS_ID expression CLOSING_PARENS_ID
    private void factor() {
        derivation.append("12 ");
        if (currentToken.getTokenNumber() == Scanner.IDENTIFIER_ID) {
            idref();
        // factor -> LITERAL
        } else if (currentToken.getTokenNumber() == Scanner.LITERAL_ID) {

            codeGen.writeCode("# factor -> literal");
            if (getType(currentToken) == 'i') { // integer literal
                String str = currentToken.getLexeme();

                exprRecord.setType('i');
                exprRecord.setLocation(currentOffset);

                currentOffset -= SymbolTable.VARIABLE_SIZE;

                codeGen.writeCode("li $t0 " + str);
                codeGen.writeCode("sw $t0 " + exprRecord.getLocation() + "($fp)");
            } else if (getType(currentToken) == 'l') { // logical literal
                String str = currentToken.getLexeme();

                exprRecord.setType('l');
                exprRecord.setLocation(currentOffset);

                currentOffset -= SymbolTable.VARIABLE_SIZE;

                // 1 or 0 based on what the lexeme was
                codeGen.writeCode("li $t0 " + (str.equals("true") ? 1 : 0));
                codeGen.writeCode("sw $t0 " + exprRecord.getLocation() + "($fp)");
            } else { // String literal
                String tempLocation = ExpressionRecord.generateLabel();
                // strip surrounding " characters
                String lexeme = currentToken.getLexeme().substring(1, currentToken.getLexeme().length() - 1);
                exprRecord.setType('s');
                exprRecord.setLocation(tempLocation);
                codeGen.writeCode(".data ");
                codeGen.writeCode(tempLocation + ": .asciiz \"" + lexeme + "\"");
                codeGen.writeCode(".text ");
            }

            match(currentToken, Scanner.LITERAL_ID);
        } else if (currentToken.getTokenNumber() == Scanner.BOOLEAN_NOT_ID) {
            match(currentToken, Scanner.BOOLEAN_NOT_ID);
            factor();

            codeGen.writeCode("# boolean not");
            codeGen.writeCode("lw $t0 " + exprRecord.getLocation() + "($fp)");
            codeGen.writeCode("not $t0 $t0");
            codeGen.writeCode("sw $t0 " + exprRecord.getLocation() + "($fp)");
        } else if (currentToken.getTokenNumber() == Scanner.OPEN_PARENS_ID) {
            match(currentToken, Scanner.OPEN_PARENS_ID);
            expression();
            match(currentToken, Scanner.CLOSING_PARENS_ID);
        }
    }

    //
    // (13) idref : IDENTIFIER_ID
    private void idref() {
        derivation.append("13 ");
        if (currentToken.getTokenNumber() == Scanner.IDENTIFIER_ID) {
            Variable temp = symbolTable.findAllScopes(currentToken.getLexeme());
            if (temp == null) {
                errors.append("Token error: '" + currentToken.getLexeme() + "' not found.\n");
                isSuccess = false;
            }
            exprRecord.setType(temp.getType());
            exprRecord.setLocation(temp.getOffset());
            match(currentToken, Scanner.IDENTIFIER_ID);
        }
    }
}
