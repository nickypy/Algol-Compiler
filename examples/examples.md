### Sample Programs
#### FizzBuzz from 1 to 20
```c
begin
    integer a;
    a := 1;

    while a < 21 do
        begin
        if (a rem 3) != 0 then
            if (a rem 5) != 0 then
                write(a);
        if (a rem 3) = 0 then write("fizz");
        if (a rem 5) = 0 then write("buzz");

        writeln("");
        a := a + 1;
        end;
end.
```

##### Output

```
Program Start
1
2
fizz
4
buzz
fizz
7
8
fizz
buzz
11
fizz
13
14
fizzbuzz
16
17
fizz
19
buzz
Program End
```


#### First 10 Fibonacci numbers
```c
begin
    integer n;
    integer first;
    integer second;

    n := 0;
    first := 0;
    second := 1;

    while n < 10 do
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
```
##### Output
```
Program Start
1 1 2 3 5 8 13 21 34 55
Program End
```