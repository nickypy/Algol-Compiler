begin

integer a;
a := 1;

while a < 21 do
    begin
	if (a rem 3) != 0 then if (a rem 5) != 0 then write(a);
	if (a rem 3) = 0 then write("fizz");
	if (a rem 5) = 0 then write("buzz");

	writeln("");
	a := a + 1;
    end;
end.
