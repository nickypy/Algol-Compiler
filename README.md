### Algol Compiler
A compiler for a subset of Algol written in Java. Intermediate code is implemented as MIPS assembly.

### Requirements
JDK8+ and a MIPS assembler.

### Usage
#### Using the provided .jar file
```sh
# to compile source files using jar file
$ java -jar agc.jar file.c
# print SymbolTable and Derivation
```
`verbose` is either true or false, false by default.
#### From source
```sh
# be sure to be in the project directory
$ javac *.java
$ java Main sourcefile.c
# optionally with verbose flag
$ java Main sourcefile.c true
```

#### Command Line SPIM Usage
```shell
$ spim
$ (spim) load "out.s"
$ (spim) run
```

#### Examples
See [examples](https://github.com/nickypy/Algol-Compiler/tree/master/examples) for code examples.

### Some Basic Syntax
#### A Basic Program
Every program requires at least a `BEGIN` and `END` keyword, followed by a `.` which denotes the end of program. Note that the language is not case sensitive. All comments are multiline comments. A comment begins with the keyword `COMMENT` and ends with the first `;`.

```c
begin
    comment
        body of program
        more statements
        multiline comments are fun
    ;
end.
```
#### Declarations and Assignments
```c
begin
    integer a;
    logical b;
    a := 200;
    b := false;
end.
```
Logicals are internally represented as a 0 or a 1.

#### Operators
|Symbol | Operation     | Precedence |
| :---:    | :---:            | :---:    |
|`()`   |  Parenthesis  |  Highest  |
|  `!`  |  Boolean NOT  |  2nd Highest  |
|  `<`  |  Less Than    |  High  |
|  `>`  |  Greater Than |  High  |
|  `=`  |   Equals      |  High  |
| `!=`  |  Not Equal    |  High    |
|  `*`  |  Multiply     |  Medium  |
|  `/`  |  Divide       |  Medium  |
| `and` |  Boolean AND  |  Medium  |
|  `+`  |  Add          |  Low  |
|  `-`  |  Subtract     |  Low  |
| `or`  |  Boolean OR   |  Low  |

Since `and` has the same precedence as `*` and `/`, and `or` has the same precedence as `+` and `-`, expression must make use of `()` to enforce precedence rules.

#### if and while statements
```c
begin
    if condition then
        integer a;

    while condition do
        a := a + 1;
end.

```
Conditions must either be logical values or expressions that evaluate to a logical.

#### Input/Output
```c
begin
    integer a;
    write("no carriage return at the end")
    writeln("with carriage return at the end");
    read(a);
end.
```
The `read()` statement only works with integers.
#### Compound Statements
```c
begin
    if (a != 5 and a < 10) then
    begin
        a := a + 2;
        writeln(a);
    end;
end.
```
Entering a new block uses the `begin` and `end` keywords followed by a `;`.


#### Extended Backus-Naur Form of the Language
```
1.  program     ::=  blockst '.'
2.  statmt      ::=  decl | assstat | ifstat | blockst | loopst | iostat | epsilon
3.  decl        ::=  TYPE_ID IDENTIFIER_ID
4.  assstat     ::=  idref  ASSIGNMENT_ID  expression
5.  ifstat      ::=  IF_ID  expression  THEN_ID  statmt
6.  loopst      ::=  WHILE_ID  expression  DO_ID  statmt
7.  blockst     ::=  BEGIN_ID  { statmt SEMICOLON_ID }  END_ID
8.  iostat	    ::=  IO_ID  OPEN_PARENS_ID  idref CLOSING_PARENS_ID | IO_ID  OPEN_PARENS_ID  expression  CLOSING_PARENS_ID
9.  expression  ::=  term { ADD_OP_ID term }
10. term        ::=  relfactor { MULT_OP_ID relfactor }
11. relfactor   ::=  factor  [ RELATIONAL_OP_ID factor ]
12. factor      ::=  idref | LITERAL_ID | BOOLEAN_NOT_ID factor  | OPEN_PARENS_ID  expression  CLOSING_PARENS_ID
13. idref       ::=  IDENTIFIER_ID
```