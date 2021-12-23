package gvdecoder;

public class normalizefilter implements ViewerFilter{
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
 int subnumber=6;
 int initialscan=50;
 int decimation=4;
 int i;
 double val;
 double[] maxdiff;
 double[] mindiff;
 double[] norm;
 int[] maxindicesy;
 int[] maxindicesx;
 int[] sort;
 double globalmaxdif=0;

 boolean absolute=true;
 boolean initialized=false;
 Viewer2 vw;

 public int index(int n){
     int v=0;
	 if (n<0) v=numbuffers+n;
	 else v=n;
	 if ((v<0)||(v>numbuffers)) v=0;


	 return v;
 }

 int median(int x0, int x1, int x2){
	int tmp;
	if (x0>x1){tmp=x0;x0=x1;x1=tmp;}
	if (x1>x2){tmp=x1;x1=x2;x2=tmp;}
	if (x0>x1){tmp=x0;x0=x1;x1=tmp;}
	return x1;
 }

 public void initialize(Viewer2 vw, int[] arr){
   sort=new int[3];
   if (subnumber>numbuffers-3) subnumber=numbuffers-3;
   int framenumber=vw.frameNumber;
   this.vw=vw;
   maxdiff=new double[arr.length];
   mindiff=new double[arr.length];
   norm=new double[arr.length];
   for (int i=0;i<maxdiff.length;i++){
   		 mindiff[i]=Double.MAX_VALUE;
   		 maxdiff[i]=Double.MIN_VALUE;
   		 norm[i]=1;
   	}


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
    int k =index( (counter%numbuffers)-subnumber);
    int k1=index( (counter%numbuffers)-subnumber-1);
    int k2=index( (counter%numbuffers)-subnumber-2);
    int p1=index( (counter%numbuffers)-1);
    int p2=index( (counter%numbuffers)-2);

    if (z-framenumber>subnumber){
    for (i=0;i<vw.datArray.length;i++){
	   val=(median(vw.datArray[i],arrays[p1][i],arrays[p2][i])-median(arrays[k][i],arrays[k1][i],arrays[k2][i]));
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

    unsetnormalize();


  initialized=true;



 }

 public void setnormalize(Viewer2 vw ){
	 int framenumber=vw.frameNumber;
	 double[] _mindiff=new double[vw.datArray.length];
	 double[] _maxdiff=new double[vw.datArray.length];

	 for (int i=0;i<maxdiff.length;i++){
		 _mindiff[i]=Double.MAX_VALUE;
		 _maxdiff[i]=Double.MIN_VALUE;
	 }
	 for (int z=framenumber;z<framenumber+initialscan;z++){
		 vw.JumpToFrame(z);
		 //run(vw,vw.datArray);
         for (int i=0;i<vw.datArray.length;i++){
			 if (vw.datArray[i]>_maxdiff[i]) _maxdiff[i]=vw.datArray[i];
			 if (vw.datArray[i]<_mindiff[i]) _mindiff[i]=vw.datArray[i];
		 }
     }
	 vw.JumpToFrame(framenumber);

	 for (int i=0;i<maxdiff.length;i++){

		 norm[i]=10000.0/(_maxdiff[i]-_mindiff[i]);
         mindiff[i]=_mindiff[i];
	 }

	 /*
	 for (i=0;i<norm.length;i++){
	 	  if (maxdiff[i]>=1)
	 	    norm[i]=globalmaxdif/maxdiff[i];
	   }

    */
 }

 public void unsetnormalize(){
	  for (i=0;i<norm.length;i++){
                mindiff[i]=0;
	 	 	    norm[i]=1.0;
	 	   }


 }



 public void run(Viewer2 vw,int[] arr){
   int i,index;
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
					index=(y+yy)*vw.X_dim+(x+xx);
					arr[index]=(int)((val-mindiff[index])*mult*norm[index]);
				}
			}
		}
	}




}

}
