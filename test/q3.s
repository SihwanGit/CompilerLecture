let int a[5]; int i = 0; int max = 0; in
a[0] = 1;
a[1] = 4;
a[2] = 3;
a[3] = 6;
a[4] = 2;
while (i < 5) {
if(a[i] > max) then
max = a[i];
i = i + 1;
}
print "Max";
print max;
end;