let 
   int a[5]; 
   int i = 0; 
   int sum = 0; 
in
while(i < 5) {
   a[i] = i;
   i = i + 1;
}
i = 0;
while (i < 5) {
   sum = sum + a[i];
   i = i + 1;
}
print "Sum";
print sum;
end;