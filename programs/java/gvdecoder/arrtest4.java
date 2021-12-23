package gvdecoder;

import org.python.core.*;

public class arrtest4{

 public double[] dat;

public arrtest4(int zdim){
 dat=new double[zdim*10];
 for (int i=0;i<dat.length;i++){
  dat[i]=i*2;
 }
}

public String __getitem__(int[] range){
return get(range);
}

public String __getitem__(PySlice slice){
 String res="requested slice "+slice.start+" to "+slice.stop+".";
 return res;

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