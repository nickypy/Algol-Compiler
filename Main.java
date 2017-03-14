/*
 * Nick Pagsanjan
 * CS 4110 - Compiler Design
 * Main.java
 *
 * Main driver for RDParser
 */

import java.io.BufferedReader;  // for Scanner constructor
import java.io.FileReader;      // BufferedReader's constructor takes in a FileReader object
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            String path    = args[0];
            Scanner scan   = new Scanner(new BufferedReader(new FileReader(path)));

            if (args.length > 1) {
                if (Boolean.parseBoolean(args[1]) == true || Integer.parseInt(args[1]) == 1) {
                    System.out.println("Tokens: ");
                    scan.printAllTokens();
                }
            }

            SymbolTable st = new SymbolTable();
            RDParser parse = new RDParser(scan, st, true);

            if (args.length > 1) {
                if (Boolean.parseBoolean(args[1]) == true || Integer.parseInt(args[1]) == 1) {
                    System.out.println("Symbol Table: ");
                    System.out.println(st);
                    System.out.println("Parser -- Leftmost Derivation: ");
                    System.out.println(parse.getDerivation());  
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
