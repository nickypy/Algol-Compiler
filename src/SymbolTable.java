/*
 * Nick Pagsanjan
 * CS 4110 - Compiler Design
 * SymbolTable.java
 *
 * SymbolTable implementation
 */

import java.util.Hashtable; // for symbol table
import java.util.ArrayList; // for iterable stack

public class SymbolTable {

    private static final int HASHTABLE_SIZE = 47;
    private static final int STACK_SIZE     = 7;
    public static final int VARIABLE_SIZE   = 4;

    private ArrayList<Hashtable<String, Variable>> table;    // List of tables to store per scope
    private ArrayList<Hashtable<String, Variable>> finished; // Storage for tables that fall out of scope

    // keep track of the current scope
    // 0 for global, increases by 1 per new scope
    private int scopeLevel;
    private int currentOffset;

    // class constructor
    public SymbolTable() {
        this.table    = new ArrayList<Hashtable<String, Variable>>(STACK_SIZE);
        this.finished = new ArrayList<Hashtable<String, Variable>>(STACK_SIZE * 5);

        this.table.add(new Hashtable<String, Variable>(HASHTABLE_SIZE));

        scopeLevel    = 0;
        currentOffset = 0;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    // inserts new identifier to the current scope with scope level
    // store scope number as a value for now, change later
    public void insert(Variable v) {
        Variable temp = new Variable(v);

        temp.setOffset(currentOffset);

		if (findCurrentScope(v) == null) {
        	table.get(scopeLevel).put(v.getLexeme(), temp);
            currentOffset -= VARIABLE_SIZE;
		}
    }

    // returns identifier value if in table for current scope, else returns null
    public Variable findCurrentScope(Variable v) {
		String str = v.getLexeme().toLowerCase();
        return findCurrentScope(str);
    }

    // returns identifier value if in table for current scope, else returns null
    public Variable findCurrentScope(String str) {
        return table.get(scopeLevel).get(str.toLowerCase());
    }

    // returns id value if exists in any scope, else returns null
    public Variable findAllScopes(Variable v) {
        // start with current scope, then look outward
		String str = v.getLexeme().toLowerCase();
        return findAllScopes(str);
    }

    // returns id value if exists in any scope, else returns null
    public Variable findAllScopes(String str) {
        // start with current scope, then look outward
        str = str.toLowerCase();
        for (int i=table.size()-1; i >= 0; i--) {
            if (table.get(i).containsKey(str)) {
                return table.get(i).get(str);
            }
        }
        return null;
    }

    // inserts a new Hashtable and increments scope
    public void enterNewScope() {
        this.table.add(new Hashtable<String, Variable>(HASHTABLE_SIZE));
		scopeLevel++;
    }

    // deletes Hashtable for current scope and adds it to finished
    public void leaveCurrentScope() {
        if (scopeLevel > 0) {
            finished.add(table.remove(scopeLevel));
			scopeLevel--;
        }
    }

	// display contents of both symbol tables
	@Override
    public String toString() {
        String s = new String();

        if (!table.isEmpty()) {
            s += "Scope 0: \n";
            s += table.toString() + "\n";
        }

        for (int i=0; i < finished.size(); i++) {
            if (!finished.get(i).isEmpty()){            
            s += "Scope " + (i + 1) + ": \n";
            s += finished.get(i).toString() + "\n";
            }
        }
        return s;
    }
}
