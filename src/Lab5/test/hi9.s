   int i = 0;
   bool b;

   fun bool g(int x, int y) {
       if (x > y) then return true;
       else return false;
   }
   
   fun int f(int x) {
     if (x == 1) then return 1;
     else return x*f(x-1);
   }
   
    i = f(5);
    print i;
    b = g(5,3);
    print b;