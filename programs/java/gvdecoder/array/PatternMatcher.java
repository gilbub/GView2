package gvdecoder.array;


public class PatternMatcher{
 /**
 with[0,10];v[1,5]<300&&v[5,10]>300;v==300

 **/

 double rangeMax;
 double rangeMin;
 boolean withRange;
 int b;
 int e;
 Conditional[] conditions;
 double returnthreshold;
 boolean returnthresholdIsPercent;
 boolean returnthresholdIsDerivative;
 boolean returnMaxDerivative;
 int rb;
 int re;
 int maxrange;

 public int[] getRange(String str){
	int a,b;
	int[] res=new int[2];
    int m=str.indexOf('[');
	int n=str.indexOf(']');
	int c=str.indexOf(',');
	if ((m!=-1)&&(n>m)&&(c==-1)){
		a=Integer.parseInt(str.substring(m+1,n));
		b=a+1;
	    res[0]=a;
	    res[1]=b;
	    return res;
	}else
	if ((m!=-1)&&(n>m)&&(c>m)&&(c<n)){
	   a=Integer.parseInt(str.substring(m+1,c));
	   b=Integer.parseInt(str.substring(c+1,n));
	   res[0]=a;
	   res[1]=b;
	   return res;
   }
   return null;
}



 public void compile(String regex){
	 maxrange=0;
	 String[] strs=regex.split(";");
	 String withStr,condStr,retStr;

	 if (strs.length==3){
	 	withStr=strs[0];
	 	condStr=strs[1];
	 	retStr=strs[2];
	 	int[] range=getRange(withStr);
	 	this.b=range[0];this.e=range[1];
	    if (e>maxrange) maxrange=e;
	   	withRange=true;
	 }else
	 //if (strs.length==2)
	 {
		condStr=strs[0];
		retStr=strs[1];
	 }

	//process condStr
	String[] conds=condStr.split("&&");
	conditions=new Conditional[conds.length];
	for (int i=0;i<conds.length;i++){
	 String cond=conds[i];
	 Conditional condition=new Conditional();
	 // v [  ,  ] >= 300.3

	 // find v or d?
     if (cond.charAt(0)=='d') condition.isDerivative=true;
      else condition.isDerivative=false;

     // find range
     int[] range=getRange(cond);
     condition.b=range[0];
     condition.e=range[1];
     if (condition.e>maxrange) maxrange=condition.e;
	 // find operator >,<.>=,<=
	 int opindex=-1;
	 String[] ops={">","<",">=","<="};
	 //1 is >,2 is <, 3 is >=, 4 is <=

	 for (int j=3;j>=0;j--){
	   opindex=cond.indexOf(ops[j]);
	   if (opindex>-1){
		   condition.op=j+1; //added one because ops range between 1 and 4.
		   if (j>1) opindex+=2; // shift index t
		    else opindex+=1;
	//	    System.out.println("found="+ops[j]+" is opnum="+condition.op+" at char loc"+opindex+" "+cond.charAt(opindex));
		    int percentindex=cond.indexOf("%");
	        if (percentindex>opindex){
		      condition.isPercent=true;
		      condition.threshold=Double.parseDouble(cond.substring(opindex,percentindex));
	        }else
	          condition.threshold=Double.parseDouble(cond.substring(opindex));
	     break;
	  }//opindex >-1
 	}//j
 	condition.pa=this;
    conditions[i]=condition;
   }
   //process retStr

   if (retStr.charAt(0)=='d') returnthresholdIsDerivative=true;
      else returnthresholdIsDerivative=false;
   int[] range=getRange(retStr);
   rb=range[0];
   re=range[1];
   if (re>maxrange) maxrange=re;
   int percentindex=retStr.indexOf("%");
   int equalindex=retStr.indexOf("==");
   int maxindex=retStr.indexOf("max");
   if (equalindex>-1){
   if (percentindex>-1){
	   returnthresholdIsPercent=true;
	   returnthreshold=Double.parseDouble(retStr.substring(equalindex+2,percentindex));
    }
    else {
	returnthresholdIsPercent=false;
    returnthreshold=Double.parseDouble(retStr.substring(equalindex+2));
    }
   }
   else{
	if (maxindex>-1) returnMaxDerivative=true;
     else returnMaxDerivative=false;
   }
 }

double t_tmp;
boolean crossed;
public double ab_bounds_t(double a, double b, double t){
	crossed=false;
	if ((a<t)&&(b>=t)){
	  t_tmp=(t-a)/(b-a);
	  crossed=true;
	}
	if ((b<t)&&(a>=t)){
	   t_tmp=(t-b)/(a-b);
	   crossed=true;
	}
	if (crossed){
	 if (t_tmp<0) t_tmp*=-1.0;
	 return t_tmp;
   }
   else
   return -1.0;
}

public double find(int start, double[] dat){
  int i,j,k;
  int c;
  boolean pass;
  double time=-1;
  double maxtmp,tmp;
  int maxindex=-1;
  double aa,bb;
  double thr;
  int end=dat.length-maxrange-1; //-1 to give room for derivative
  if (start==0) start=1; // to give room for thresh crossing
  for (i=start;i<end;i++){


	if (withRange){
		rangeMax=Double.MIN_VALUE;
		rangeMin=Double.MAX_VALUE;
		for (j=i+b;j<i+e;j++){
		 if (dat[j]>rangeMax) rangeMax=dat[j];
		 if (dat[j]<rangeMin) rangeMin=dat[j];
	   }
	}
	pass=true;
	for (c=0;c<conditions.length;c++){
		if (!conditions[c].check(dat,i)){
			pass=false;
			break;
		}
	}
	if (pass){
	 if (returnMaxDerivative){
		 maxtmp=Double.MIN_VALUE;
	     for (k=i+rb;k<i+re;k++){
		   	tmp=dat[k+1]-dat[k];
		   	if (tmp>maxtmp) {maxtmp=tmp;maxindex=k;}
		  }
		 time=(double)maxindex;
	  }
	  else{
	     for (k=i+rb;k<i+re;k++){
		  if (returnthresholdIsDerivative){
			  aa=dat[k]-dat[k-1];
			  bb=dat[k+1]-dat[k];
			 }else{
              aa=dat[k];
              bb=dat[k+1];
  		      }
  		  if (returnthresholdIsPercent){
			  thr=(returnthreshold*0.01)*(rangeMax-rangeMin)+rangeMin;
		  } else {
			  thr=returnthreshold;
		  }
		 time=ab_bounds_t(aa,bb,thr);
         if (time>-1){
			 time+=k;
			 break;
		 }
	  }//k
	  }//else
    }//passed
   if (time>-1) break;
  }//i
  return time;
 }

}