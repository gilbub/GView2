package gvdecoder;

public class normalizefilter3 implements ViewerFilter{
/* two arrays
    step 1 (difference):

    maxdiff: max difference for that pixel and a frame N behind over 20 frames

    step 2 (decimation):

    run through, find the maximum index value over a 4 by 4 pixel area. Set other values to 0.
    keep maximum and minimum of remaining


    step 3 (normalization):
    run through, for each non-zero value, multiply by maxvalue / value

  */



 int[][] arrays;
 public double mult=10;
 int numbuffers=10;
 int counter=0;
 int subnumber=9;
 int initialscan=20;
 int decimation=4;
 int i;
 double val;
 double[] maxdiff;
 int[] maxindicesy;
 int[] maxindicesx;

 double globalmaxdif=0;

 boolean absolute=true;
 boolean initialized=false;

 public int index(int n){
     int v=0;
	 if (n<0) v=numbuffers+n;
	 else v=n;
	 if ((v<0)||(v>numbuffers)) v=0;


	 return v;
 }

 public void initialize(Viewer2 vw, int[] arr){
   int framenumber=vw.frameNumber;

   maxdiff=new double[arr.length];
   maxindicesy=new int[(int)(arr.length/(decimation*decimation*1.0))+1];
   maxindicesx=new int[(int)(arr.length/(decimation*decimation*1.0))+1];

   if (arrays==null){
		   arrays=new int[numbuffers][arr.length];
           counter=0;
   }
  counter=0;
  for (int z=framenumber;z<framenumber+initialscan;z++){
	System.out.println("z="+z);
    vw.JumpToFrame(z);
    for (i=0;i<vw.datArray.length;i++) arrays[counter%numbuffers][i]=vw.datArray[i];
    counter++;
    int k=index( (counter%numbuffers)-subnumber);
    if (z-framenumber>subnumber){
    for (i=0;i<vw.datArray.length;i++){
	   val=vw.datArray[i]-arrays[k][i];
       if (val<0) val=val*-1;
       if (val>maxdiff[i]) maxdiff[i]=val;
       if (val>globalmaxdif) globalmaxdif=val;
      }
   }//if
  }//z

  for (int x=0;x<vw.X_dim;x+=decimation){
	  for (int y=0;y<vw.Y_dim;y+=decimation){
		  double maxval=0;
		  int maxx=0;
		  int maxy=0;
		  for (int xx=0;xx<decimation;xx++){
			  for (int yy=0;yy<decimation;yy++){
			   int xi=xx+x;
			   int yi=yy+y;
			   i=yi*vw.X_dim+xi;
			   if (maxdiff[i]>maxval){
				   maxval=maxdiff[i];
				   maxx=xi;
				   maxy=yi;
			   }
		      }
	         }
	       maxindicesy[(int)(y/decimation*(vw.X_dim/decimation)+x/decimation)]=maxy;
	       maxindicesx[(int)(y/decimation*(vw.X_dim/decimation)+x/decimation)]=maxx;


		}
	}


 for (i=0;i<vw.datArray.length;i++){
	  if (maxdiff[i]>=1)
	    maxdiff[i]=globalmaxdif/maxdiff[i];
  }

  initialized=true;



 }



 public void run(Viewer2 vw,int[] arr){
   int i;
   int val;
   if (!initialized) { return;}

   for (i=0;i<arr.length;i++) arrays[counter%numbuffers][i]=arr[i];
   counter++;
   int k=index( (counter%numbuffers)-subnumber);
   System.out.println("counter="+counter+"%numbuffer="+counter%numbuffers+" subnumber="+((counter%numbuffers)-subnumber)+" k="+k);
    for (int x=0;x<vw.X_dim;x+=decimation){
		for (int y=0;y<vw.Y_dim;y+=decimation){
			int my=maxindicesy[(int)(y/decimation*(vw.X_dim/decimation)+x/decimation)];
			int mx=maxindicesx[(int)(y/decimation*(vw.X_dim/decimation)+x/decimation)];
			val=arr[my*vw.X_dim+mx]-arrays[k][my*vw.X_dim+mx];
			if (val<0) val=val*-1;
			for (int xx=0;xx<decimation;xx++){
				for (int yy=0;yy<decimation;yy++){
					arr[(y+yy)*vw.X_dim+(x+xx)]=(int)(val*mult*maxdiff[(y+yy)*vw.X_dim+(x+xx)]);
				}
			}
		}
	}




}

}
