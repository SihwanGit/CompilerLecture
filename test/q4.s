let int abc[8]; int i = 0; int sum = 0; in
while (i < 8) {
  abc[i] = i*i - (i + 3)*3 - 3;
  i = i + 1;
}
i = 0;
while (i < 8) {
  if(abc[i] > 0) then
    sum = sum + abc[i];
  i = i + 1;
}
print "Sum of positive numbers";
print sum;
end;