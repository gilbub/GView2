package gvdecoder;
import java.io.*;





public class parser{
String DataPath="c:\\data\\real9";
public String[] parseCameraStatusFile(){
	if (DataPath!=null){

	      File fdir=new File(DataPath+File.separator+"CameraStatus.txt");

          String [] strs = getContents(fdir);

          return strs;

          }
	return null;
 }


public double getDouble(String[] strs, String key, int startingIndex){
	String keyequals=key+" =";
	int loc1,loc2;
	String valstr=null;
	double val=Double.NaN;
	for (int i=startingIndex;i<strs.length;i++){
	      loc1=strs[i].indexOf(keyequals);
	      if (loc1!=-1){
			   loc2=strs[i].indexOf("=");
			   valstr=strs[i].substring(loc2+1);
			   break;
		  }
	    }
	  if (valstr!=null){

		  val=Double.parseDouble(valstr.trim());
       }
	  return val;
	}

public double[] getMinMax(String[] strs, String key, int startingIndex){
	String keyparameter="Parameter "+key;
	int loc1,loc2,loc3,loc4;
	String valstr1=null;
	String valstr2=null;
	double val1=Double.NaN;
	double val2=Double.NaN;
	for (int i=startingIndex;i<strs.length;i++){
	      loc1=strs[i].indexOf(keyparameter);
	      if (loc1!=-1){
			loc2=strs[i].indexOf("min",loc1);
			loc3=strs[i].indexOf("=",loc2);
			loc4=strs[i].indexOf(",",loc3);
			valstr1=strs[i].substring(loc3+1,loc4);
			loc2=strs[i].indexOf("max",loc4);
			loc3=strs[i].indexOf("=",loc2);
			loc4=strs[i].indexOf(",",loc3);
			valstr2=strs[i].substring(loc3+1,loc4);


		   break;
		  }
	  }
	  if ((valstr1!=null)&&(valstr2!=null)){
		  val1=Double.parseDouble(valstr1.trim());
		  val2=Double.parseDouble(valstr2.trim());

	  }
	  double []tmp=new double[2];
	  tmp[0]=val1;
	  tmp[1]=val2;
	  return tmp;

}


public String status(){
	String[] strs =parseCameraStatusFile();
	double[] mF=getMinMax(strs,"FrameRate",0);

	return "\nframe rate = "+getDouble(strs,"FrameRate",0)+"\t(max = "+mF[1]+")\nexposure time = "+getDouble(strs,"ActualExposureTime",0)+"\ntemp = "+getDouble(strs,"SensorTemperature",0);

}

  public String[] getContents(File aFile) {

      java.util.Vector v=new java.util.Vector();
      try{
        // Open the file that is the first
        // command line parameter
        FileInputStream fstream = new FileInputStream(aFile);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        //Read File Line By Line
        while ((strLine = br.readLine()) != null)   {
         v.add(strLine);
        }
        //Close the input stream
        in.close();
          }catch (Exception e){
          e.printStackTrace();
        }

        String [] result =new String[v.size()];
        for (int i=0;i<result.length;i++){
           result[i]=(String)v.elementAt(i);

       }
       return result;
  }
  }