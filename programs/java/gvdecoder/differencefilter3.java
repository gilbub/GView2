package gvdecoder;

public class differencefilter4 implements ViewerFilter{

 int[][] arrays;
 public int mult=10;
 int numbuffers=5;
 int counter=0;
 int subnumber=1;
 public int index(int n){
     int v=0;
	 if (n<0) v=numbuffers+n;
	 if ((v<0)||(v>numbuffers)) return 0;
	 else return v;
 }
 public void run(Viewer2 vw,int[] arr){
   int i;
   int val;
   if (arrays==null){
		   arrays=new int[numbuffers][arr.length];
           counter=0;
   }
   for (i=0;i<arr.length;i++) arrays[counter%numbuffers][i]=arr[i];
   counter++;

    for ( i=0;i<arr.length;i++){
     val=arr[i]-arrays[index( (counter%numbuffers)-subnumber)][i];
     if (val<0) val=val*-1;
     arr[i]=val*mult;

    }
 }

}
