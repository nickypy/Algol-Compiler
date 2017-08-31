begin
    integer a;
    logical b;

    b := true;
    writeln(b);

    b := false;
    writeln(b);

    if b then a := 5;

    if !b then a := 10;
        

    writeln(a);
    writeln(A);

    integer c;

    c := 0;

    while c < a do
        if !b then
            begin
                c := c + 1;
                write(c);
                write(" ");
            end;;
    writeln(" ");

    a := c + 5 * 13;
    writeln(a);
    writeln(7 - 9/3);

    if true or false then writeln("true or false evaluates to true");

    if ! (false and false) then writeln("not (false and false) = true");

end;
