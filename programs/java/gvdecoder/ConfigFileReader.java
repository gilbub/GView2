package gvdecoder;

import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class ConfigFileHelper{
 public static Map<String, String> map=new HashMap<String, String>();
 public String filename;

 public ConfigFileHelper(String filename){
	 this.filename=filename;
 }

 public int read(){
	 try{
        BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
        String line = null;
        int c=0;
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


 public void write(){
	 // try{
	   // BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
        Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
		  Map.Entry pair = (Map.Entry)it.next();
		  System.out.println(pair.getKey() + " = " + pair.getValue());
		  it.remove(); // avoids a ConcurrentModificationException
        }
 }

 public void clear(){
  map.clear();
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