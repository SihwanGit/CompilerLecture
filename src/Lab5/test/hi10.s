int i = 1;

fun int f(int x) {
     i = i + x; 
     return i;
}

fun int g(int x) {
     i = i + x*x;
     return i;
}

let
    int i; 
in 
    i = 10; 
    print f(1);
    print i;
    print g(2);
    print i;
end;
