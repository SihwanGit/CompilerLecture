fun int f(int x, bool y) 
let int result =  0;
in
    if (y) then {
       result = x * x; 
       print result; 
    }
    return result;
end;

fun int g(int x)
let
    int i; bool b = true; 
in 
    i = f(x, b);
    return i;
end;

print g(10);