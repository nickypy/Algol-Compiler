begin
    integer n;
    integer first;
    integer second;

    n := 0;
    first := 0;
    second := 1;
            
    while n < 20 do
        begin
            integer temp;
            temp = second;
            second = first + second;
            first = temp;

            write(first);
            write(" ");

            n := n + 1;
        end;
    writeln("");
end.
