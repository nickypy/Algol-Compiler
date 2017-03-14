/*
 * Nick Pagsanjan
 * CS 4110
 * CodeGenerator.java
 *
 * Used by RDParser to generate intermediate code,
 * in this case, MIPS assembly
 */

import java.util.ArrayList; // for code gen buffer
import java.io.PrintWriter; // write out to file
import java.io.File;        // for opening files

public class CodeGenerator {

    private static String outfilePath = "out.s"; // output file path

    private ArrayList<String> codeGenOut;     // buffer for writing file at the end

    public CodeGenerator() {
        codeGenOut = new ArrayList<String>();
    }

    public void writeToFile() {
        //  begin write out to file
        File file;
        PrintWriter pw;
		try {
            file = new File(outfilePath);
            pw = new PrintWriter(file);

            for (String s : codeGenOut) {
                pw.println(s);
            }

            pw.close();
        } catch(Exception e) {
			System.out.println("An error occurred while opening the file.");
		}
	}

	public void writeProlog() {
        codeGenOut.add("#Prolog: next 7 lines start the program");
        codeGenOut.add(".text");
        codeGenOut.add(".globl main");
        codeGenOut.add("main:");
        codeGenOut.add("move $fp $sp");
        codeGenOut.add("la $a0 ProgStart");
        codeGenOut.add("li $v0 4");
        codeGenOut.add("syscall\n");
	}


	public void writePostlog() {
        codeGenOut.add("\n#Postlog: next 8 lines will end all programs");
        codeGenOut.add("la $a0 ProgEnd");
        codeGenOut.add("li $v0 4");
        codeGenOut.add("syscall");
        codeGenOut.add("li $v0 10");
        codeGenOut.add("syscall");
        codeGenOut.add(".data");
        codeGenOut.add("ProgStart: .asciiz \"Program Start\\n\"");
        codeGenOut.add("ProgEnd:   .asciiz \"Program End\\n\"");
        codeGenOut.add("True:      .asciiz \"True\"");
        codeGenOut.add("False:     .asciiz \"False\"");
        codeGenOut.add("endl:      .asciiz \"\\n\"");
	}

	public void writeCode(String code) {
		codeGenOut.add(code);
	}
}
