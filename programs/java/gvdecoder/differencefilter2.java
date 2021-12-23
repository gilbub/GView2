package gvdecoder;

public class differencefilter2 implements ViewerFilter{

 int [] lastarray;
 int [] tmp;
 public int mult=2;
 public void run(Viewer2 vw,int[] arr){
   int i;
   int val;
   if (lastarray==null){ lastarray=new int[arr.length]; tmp=new int[arr.length];}
    for (i=0;i<arr.length;i++) {tmp[i]=lastarray[i];
                                lastarray[i]=arr[i];}

    for ( i=0;i<arr.length;i++){
     val=arr[i]-tmp[i];
     if (val<0) val=val*-1;
     arr[i]=val*mult;

    }
 }

}
