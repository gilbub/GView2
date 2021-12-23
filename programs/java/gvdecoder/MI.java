package gvdecoder;

public class  MI{
/*
What should be done (according to Omichi et al methods section Circ Res 2003

 1) estimate autocorrelation drops to zero after t samples
for each trace pair:
 2) resample Vm, Ca at this auto=0 period
 3) normalize each channel, recast as an integer between 1-100
 for Ca index offset between -250 and 250 ms: (NOTE, not done here...)
  MIMAX=-10000, MIMIN=10000
  4) create val[100][100] 100x100 array, populate with sum of Vm vs Ca
  5) sum the total in each row, divide the value by the number of samples and store in PR[i]
  6) repeat step 5 for each column, store in PC[i].
  7) MI=0
     for i =0 to 100,
      for j = 0 to 100,
        P=val[i][j]/(Number of samples in series)
        MI+=P*log(P / (PR[i]]*PR[j]) )
  8) MI[-250 to 250]=MI (NOTE, only one value of MI is returned for a time shift of 0.


 This routine calculates MI for two traces, where the user has to input the size ofthe grid (ie 100, or 10)
 and the decimation factor for resampling the traces so that the autocorrelation function goes to 0.
 The routine does not time shift values nor does it bootstrap the data with random array (as this is best done
 through scripts)


  */
  public int[][] val;
  public double[] PV;
  public double[] PC;
  public int[] recast(double[] arr, int k){
	 //find max,min
	 double max=Double.MIN_VALUE;
	 double min=Double.MAX_VALUE;
	 double v;
	 for (int i=0;i<arr.length;i++){
	   v=arr[i];
	   if (v>max)max=v;
	   if (v<min)min=v;
	 }
	 //place into integer array so that the vals range between 0 and k
	 int[] res=new int[arr.length];

	 for (int i=0;i<arr.length;i++){
	    res[i]=(int) ( k*(arr[i]-min)/(max-min) );
	  }
	 return res;
  }

  public double[] resample(double [] arr, int d){
	  int newdim=(int)((double)arr.length/d);
	  double [] res=new double[newdim];
	  for (int i=0;i<newdim;i++){
		 if (i*d<arr.length)
		  res[i]=arr[i*d];
	  }
	  return res;
  }



  /**
   Usage: MI_1(double[] Vm time series,
               double[] Ca time series,
               int k binning grid dimension (typically 10)
               int d decimation factor to ensure that autocorrelation is 0)

  **/
  public double MI_1(double[] Vm, double[] Ca, int k, int d){
	  //normalize and recast
	  int[] Vmi=recast(resample(Vm,d),k);
	  int[] Cai=recast(resample(Ca,d),k);
	  return MI_2(Vmi,Cai,k);


  }

 /* this is called from MI_1*/

  public double MI_2(int[] Vm, int[] Ca, int k){
	  val=new int[k][k];
	  PV=new double[k];
	  PC=new double[k];
	  for (int i=0;i<Vm.length;i++){
		 int ii=Vm[i];
		 int jj=Ca[i];
		 if ((ii<k)&&(jj<k))
		 val[ii][jj]+=1;
	  }

	  for (int i=0;i<k;i++){
	   for (int j=0;j<k;j++){
		  PV[i]+=val[i][j];
		  PC[j]+=val[i][j];
	   }
	  }
	  for (int i=0;i<k;i++){
		  PV[i]=PV[i]/Vm.length;
		  PC[i]=PC[i]/Ca.length;
	  }

      double mi=0;
      for (int i=0;i<k;i++){
		for (int j=0;j<k;j++){
		   double P=((double)val[i][j])/Vm.length;
		   if (P>0)
		    mi+=P*Math.log(P/(PV[i]*PC[j]));

		}
	}
    return mi;

  }

  }







