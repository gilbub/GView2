package gvdecoder;

public class differencefilter6 implements ViewerFilter{

 int[][] arrays;
 public int mult=10;
 public int numbuffers=50;
 public int counter=0;
 public int subnumber=9;
 boolean absolute=true;
 public int index(int n){
     int v=0;
	 if (n<0) v=numbuffers+n;
	 else v=n;
	 if ((v<0)||(v>numbuffers)) v=0;


	 return v;
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
   int k=index( (counter%numbuffers)-subnumber);
   System.out.println("counter="+counter+"%numbuffer="+counter%numbuffers+" subnumber="+((counter%numbuffers)-subnumber)+" k="+k);
    for ( i=0;i<arr.length;i++){
     val=arr[i]-arrays[k][i];
     if (absolute) if (val<0) val=val*-1;
     arr[i]=val*mult;

    }
 }

}
