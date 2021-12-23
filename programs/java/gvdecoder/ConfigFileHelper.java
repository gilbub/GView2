package gvdecoder;

import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.Iterator;

public class ConfigFileHelper{
 public static Map<String, String> map=new HashMap<String, String>();
 public String filename;

 public ConfigFileHelper(String filename){
	 this.filename=filename;
 }

 public int read(){
	 int c=0;
	 try{
        BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
        String line = null;

        while ((line = reader.readLine()) != null) {
            if (line.contains("=")) {
                String[] strings = line.split("=");
                map.put(strings[0], strings[1]);
                c++;
            }
        }
        reader.close();
	}catch (IOException e){e.printStackTrace(); return -1;}
   return c;
 }


 public int write(){
	int c=0;
	try{
	    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
        Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
		  Map.Entry pair = (Map.Entry)it.next();
		  writer.write(pair.getKey() + "=" + pair.getValue());
		  writer.newLine();
          c++;
        }
     writer.close();
    }catch(IOException e){
		e.printStackTrace();
	    return -1;
	}
	return c;
 }

 public void clear(){
  map.clear();
 }


 public void put(String key, String value){
	  map.put(key,value);
 }


 public int getInt(String key){
  return Integer.parseInt(map.get(key));
 }

 public double getFloat(String key){
  return Double.parseDouble(map.get(key));

 }

 public String getString(String key){
	 return map.get(key);
}

}