package gvdecoder;

import org.python.core.*;

public class arrtest3{

 public double[][] dat;

public arrtest3(int zdim){
 dat=new double[zdim];
 for (int i=0;i<dat.length;i++){
	dat[i]=new double[10];
    for (int j=0;j<10;j++){
		dat[i][j]=j*0.4;
	}
 }
}

public String __getitem__(int[] range){
return get(range);
}

public int __len__(){
	return 4;
}

public String __getitem__(int val){
	if (val>10)
	    throw Py.IndexError("too many dimensions");
	return dat[val];
}

public String __getitem__(PySlice slice){
 String res="requested slice "+slice.start+" to "+slice.stop+".";
 return res;

}

public double[] func(double[] arr){
	double[] res=new double[arr.length];
	for (int i=0;i<res.length;i++){
		res[i]=arr[i]*3;
	}
	return res;

}

public String __getitem__(PyTuple[] tup){
 //int sum=((PyInteger)tup.__getitem__(0)).getValue()+((PyInteger)tup.__getitem__(1)).getValue();
 //String str ="tuple "+tup.__getitem__(0)+ " "+tup.__getitem__(1)+ " = "+sum;
 return "length "+ tup.length;
}

public String __radd__(int arg){
	return  "adding arg to itself";
}

public String __add__(int arg){
	return "adding arg to array";
}

public String get(int[] range){
 String res="requested "+range.length+" items from "+range[0]+" to "+range[range.length-1]+".";
 return res;
}


}