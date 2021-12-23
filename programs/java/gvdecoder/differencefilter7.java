package gvdecoder;

public class differencefilter7 implements ViewerFilter{

 int[][] arrays;
 int[] arrtmp;
 public int mult=10;
 int numbuffers=10;
 int counter=0;
 int subnumber=9;
 int smoothdim=2;

 boolean absolute=true;
 public int index(int n){
     int v=0;
	 if (n<0) v=numbuffers+n;
	 else v=n;
	 if ((v<0)||(v>numbuffers)) v=0;

	 return v;
 }

 public int xyi(int y, int x, int xdim){
	 return y*xdim+x;

 }

 public void run(Viewer2 vw,int[] arr){
   int i,x,y,xyi,xx,yy, sum1,sum2,ti;
   int val;
   int xdim=vw.X_dim;
   if (arrays==null){
		   arrays=new int[numbuffers][arr.length];
		   arrtmp=new int[arr.length];
           counter=0;
   }
   for (i=0;i<arr.length;i++) arrays[counter%numbuffers][i]=arr[i];
   counter++;

   int k=index( (counter%numbuffers)-subnumber);
   //System.out.println("counter="+counter+"%numbuffer="+counter%numbuffers+" subnumber="+((counter%numbuffers)-subnumber)+" k="+k);
   for (x=smoothdim;x<vw.X_dim-smoothdim-1;x++){
	   for (y=smoothdim;y<vw.Y_dim-smoothdim-1;y++){
	     sum1=0;sum2=0;
	     for (xx=-smoothdim;xx<=smoothdim;xx++){
			 for (yy=-smoothdim;yy<=smoothdim;yy++){
				 ti=xyi(y+yy,x+xx,xdim);
				 sum1+=arr[ti];
				 sum2+=arrays[k][ti];
	             }
              }
             val=sum2-sum1;
             if (absolute) if (val<0) val=val*-1;
             arrtmp[xyi(y,x,xdim)]=val*mult;
		  }
	  }

	  for ( i=0;i<arr.length;i++)arr[i]=arrtmp[i];
    /*
    for ( i=0;i<arr.length;i++){
     val=arr[i]-arrays[k][i];
     if (absolute) if (val<0) val=val*-1;
     arr[i]=val*mult;

    }
    */
 }

}
