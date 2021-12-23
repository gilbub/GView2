package gvdecoder.array;

public class DataSet{
 public double[] data;
 public String name;

public DataSet(int length, String name){
 data=new double[length];
 this.name=name;
}

public void destroy(){
 data=null;
 name=null;
}

}