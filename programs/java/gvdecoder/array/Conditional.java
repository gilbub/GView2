package gvdecoder.array;

public class Conditional{
 int b;  //start index
 int e;  //end index
 int op; //1 is >,2 is <, 3 is =>, 4 is <=
 int c; //counter
 double threshold;
 boolean isPercent;
 boolean isDerivative;
 boolean passed;
 PatternMatcher pa;
 double tmpthr;
 double v;
 public Conditional(){

 }
 public Conditional(PatternMatcher pa,int i, int j, int op, double thresh,boolean percent, boolean dvdt){
	 b=i;e=j;this.op=op;threshold=thresh;isPercent=percent;isDerivative=dvdt;
     c=0;
     passed=false;
     this.pa=pa;
    }

 public boolean check(double[] arr, int offset){
  //check if within bounds
  passed=false;
  if (e+offset>arr.length){
	  return passed;
  }
  //check if absolute or percent threshold
  if (isPercent){
	  tmpthr=(threshold*0.01)*(pa.rangeMax-pa.rangeMin)+pa.rangeMin;
  }
  else tmpthr=threshold;
  //run through conditions
  boolean failed=false;
  for (c=b+offset;c<e+offset-1;c++){
	if (isDerivative) v=arr[c+1]-arr[c];
	else v=arr[c];
	switch(op){
	 case 1: failed=(v<=tmpthr);break; //>
	 case 2: failed=(v>=tmpthr);break; //<
	 case 3: failed=(v>tmpthr);break;  //<=
	 case 4: failed=(v<tmpthr);break;  //>=
    }
  if (failed) break;
  }
  passed=!failed;
  return passed;
  }


 }
