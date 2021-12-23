package gvdecoder;

public class fillfilter implements ViewerFilter{

 int[][] arrays;

 public int mult=10;
 int numbuffers=10;
 int counter=0;
 int subnumber=9;
 boolean absolute=true;
 int xd,yd;
 int ca_binlevel;



 cellularautomata ca;

 int[] lastframe;
 int[] thisframe;
 int[] thresholds;

 Viewer2 vw;

 public void initialize(Viewer2 vw, int caydim, int caxdim){
	 ca=new cellularautomata();
	 ca.setup(caydim,caxdim,5,2,20);//not compiled!
     thresholds=new int[caydim*caxdim];
     ca_binlevel=(int)(vw.X_dim/caxdim);
 }

 public int index(int n){
     int v=0;
	 if (n<0) v=numbuffers+n;
	 else v=n;
	 if ((v<0)||(v>numbuffers)) v=0;
	 return v;
 }




 public void run(Viewer2 tvw,int[] arr){
   int i;
   int val;
   if (arrays==null){
		   arrays=new int[numbuffers][arr.length];
		   lastframe=new int[arr.length];
		   thisframe=new int[arr.length];
           counter=0;
           vw=tvw;
   }

   for (i=0;i<arr.length;i++) {
	   arrays[counter%numbuffers][i]=arr[i];
       lastframe[i]=thisframe[i];
   }

   counter++;
   int k=index( (counter%numbuffers)-subnumber);
   System.out.println("counter="+counter+"%numbuffer="+counter%numbuffers+" subnumber="+((counter%numbuffers)-subnumber)+" k="+k);
    for ( i=0;i<arr.length;i++){
     val=arr[i]-arrays[k][i];
     if (absolute) if (val<0) val=val*-1;
     arr[i]=val*mult;
     thisframe[i]=arr[i];
    }

 }


public void findThresholds(int numberofframes){
	for (int i=0;i<thresholds.length;i++){thresholds[i]=0;}
	int radius=(int)((double)vw.X_dim)/(2*ca.xdim);
	int binlevel=radius*2;
	int fr=vw.frameNumber;
	for (int z=fr;z<fr+numberofframes;z++){
		vw.JumpToFrame(z);
	   for (int y=0;y<ca.ydim;y++){
		   for (int x=0;x<ca.xdim;x++){
			   double sum=0;
			   for (int yy=0;yy<binlevel;yy++){
				   for (int xx=0;xx<binlevel;xx++){
				 	   int index=(y*binlevel+yy)*vw.X_dim+(x*binlevel+xx);
					   sum+=vw.datArray[index];
				   }
			   }
			 thresholds[y*ca.ydim+x]+=(int)(sum/(binlevel*binlevel));

	    }
	}
   }
	for (int i=0;i<thresholds.length;i++) thresholds[i]=(int)((double)thresholds[i])/numberofframes;
}


 public double fractionactive(int cy, int cx, int[] frame, int radius, int threshold){
	 double a=0;
	 double r=0;
	 for (int y=-radius;y<radius;y++){
		 for (int x=-radius;x<radius;x++){
			 int index=(cy+y)*vw.X_dim+cx+x;
			 if ((index>0)&&(index<frame.length)){

	            if (frame[index]>threshold) a++;
	            else r++;
	  		 }
		 }
      }
     return a/(r+a);
 }

 double threshmult=1.0;
 public double fractionactive(int cy, int cx, int[] frame, int radius){
 	 double a=0;
 	 double r=0;
 	 int tindex;
 	 for (int y=-radius;y<radius;y++){
 		 for (int x=-radius;x<radius;x++){
 			 int index=(cy+y)*vw.X_dim+cx+x;
 			 if ((index>0)&&(index<frame.length)){
                int yi=(int)(index/vw.X_dim);
                int xi=(int)(index-(yi*vw.X_dim));
                tindex=(yi/ca_binlevel)*ca.xdim+(xi/ca_binlevel);
                if (tindex>=(ca.xdim*ca.ydim)) tindex=ca.xdim*ca.ydim-1;
 	            if (frame[index]>thresholds[tindex]*threshmult) a++;
 	            else r++;
 	  		 }
 		 }
       }
      return a/(r+a);
  }





 public void setCA(int castate, double thr, int iterates, double iteratesthreshold){
	 int yy,y,xx,x,p,q;
	 int radius=(int)((double)vw.X_dim)/(2*ca.xdim);
	 int binlevel=radius*2;
	 for (y=0;y<ca.ydim;y++){
		 for (x=0;x<ca.xdim;x++){
		   int caindex=y*ca.xdim+x;
		   if ((ca.cells[caindex].state==0)&&(ca.cells[caindex].nextstate==0)){
			  if (fractionactive((y)*binlevel,(x)*binlevel,thisframe,radius)>thr){
              pqloop:
			  for (p=-1;p<2;p++){
				  for (q=-1;q<2;q++){

						if (fractionactive( (y+p)*binlevel,(x+q)*binlevel,lastframe,radius)>thr){
							ca.cells[caindex].state=castate;
							ca.cells[caindex].nextstate=castate;
							p=2;
							q=2;
							break pqloop;
				            }

					}
				}//pq
			   }
			}
	    }
	}
    for (int i=0;i<iterates;i++){ ca.iterate(iteratesthreshold); ca.clean(iteratesthreshold*0.5);}



}




}
