package gvdecoder;
public class MICalc{
public static int MAXNUM=10000;
public static int KMAX=25;
public int[] s;
public int[] q;
public int[] pow2;
public	int numpts,n0,mmax;
public	double[] pop;
public int[] pindex;
public int[] globvalue;

public int bin=-1;
public	int[] globalivalue;
public	double logtwo;
public int taumax = 20;

public MICalc(){
logtwo = 1.0/Math.log(2.0);
s=new int[MAXNUM];
q=new int[MAXNUM];
pow2=new int[KMAX];
pop=new double[MAXNUM];
pindex=new int[MAXNUM];
globvalue=new int[4];

int n0=1;
int numpts=10000;
while ((n0+taumax)<=numpts)  n0 *= 2;
	n0 /= 2;

	/* n0 = numpts - taumax; */
	int j = n0;
	for (int i=0;i<KMAX;i++)  {
		pow2[i] = j;
		j /= 2;
	}
	mmax = ((int)(Math.log(((double)n0))*logtwo+0.1));


}

public void set_sq(double[]iarr, double[]jarr){
	for (int i=0;i<iarr.length;i++){
		s[i]=(int)iarr[i];
		q[i]=(int)jarr[i];

	}

}


public double findisq()
{
	double info;
	int[] kmarray=new int[KMAX];
	double x,y;

	kmarray[0] = 0;
	x = ((double)n0);
	y = ffunct(kmarray,0);
	info = (1.0/x)*y - Math.log(x)*logtwo;

	return info;
}

public double ffunct(int kmarray[],int m)
{
	/* THIS FUNCTION CAN CALL ITSELF RECURSIVELY */
	double value;
	int n,j;
	int[] temparray=new int[KMAX];

	for (j=0;j<=m;j++)  temparray[j] = kmarray[j];

	n = number(temparray,m);

	value = ((double)n);
	if (n<=1)  {
		value = 0.0;
	} else if (n==2)  {
		value = 4.0;
	} else if (m==bin)  {
		/* no substructure */
		value = value*Math.log(value)*logtwo;
	} else {
		/* assume substructure exists */
		value = value*2.0;
		for (j=0;j<=3;j++)  {
			temparray[m+1] = j;
			value += ffunct(temparray,m+1);
		}
	}

	return value;
}


public int number(int karray2[],int m2)
{
	/* THIS FUNCTION IS NOT RECURSIVE */
	int ivalue;
	int los,his,loq,hiq;
    int i,j;

	if (m2>0)  {
		los = 0;loq = 0;
		his = n0; hiq = n0;
		for (i=1;i<=m2;i++)  {
			if (karray2[i]%2==0)  his -= pow2[i];
			else                  los += pow2[i];
			if (karray2[i]<2)     hiq -= pow2[i];
			else                  loq += pow2[i];
		}
		ivalue = 0;
		for (i=los;i<his;i++)  {
			j = q[s[i]];
			if ((j>=loq)&&(j<hiq))  ivalue++;
		}
	} else {
		ivalue = n0;
	}

	return ivalue;
}

}