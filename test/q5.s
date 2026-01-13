let string ar1[6]; 
int ar2[6]; 
bool ar3[6]; in
ar1[0] = "F";
ar1[1] = "A";
ar1[2] = "G";
ar1[3] = "U";
ar1[4] = "B";
ar1[5] = "E";
let int i = 0; in 
while (i < 6) 
{  ar2[i] = i + 1; 
   ar3[i] = true; 
   i = i + 1; 
 } 
ar3[ar2[ar2[4]-4]] = false; 
end;
ar2[1] = ar2[0] + ar2[1]; 
ar2[2] = ar2[4] - ar2[ar2[ar2[1]]]; 
ar2[3] = ar2[4]/(ar2[4] - ar2[1]); 
ar2[4] = ar2[1]*ar2[3] - (ar2[0]*2);
let int i = 0; int j = 0; in 
        while (i < 5) { 
	    if( ar2[i] == 4) then { print ar1[1]; j = ar2[i];} 
	    print ar1[ar2[i]]; i = i + 1; 
	 } 
	print ar1[j+1]; end;
print ar3[3];
end;