package gvdecoder;

import java.io.*;
import java.util.*;

public class PropertyHelper{

String ConfigFileName="."+File.separator+"gvdecoder"+File.separator+"properties.cfg";
public Properties prp;
public boolean initialized=false;

private static PropertyHelper ref;

private PropertyHelper(){
    initialized=true;
	prp=new Properties();
	try{
	 readProp(ConfigFileName);
	}catch(Exception e){
	  System.out.println("\nConfig file"+ConfigFileName+" doesnt exist or is corrupt.\n\n");
	  e.printStackTrace();
	  initialized=false;
	}
}

public static PropertyHelper getPropertyHelper(){
  if (ref==null) ref=new PropertyHelper();
  return ref;
}

public void saveCFG(){
  saveProp(ConfigFileName);
}

public void saveProp(String filename){
	int session=getIntProperty("SessionNumber",0);
	setProperty("SessionNumber",""+(session+1));
	try{
	FileOutputStream ostream=new FileOutputStream(new File(filename));
	prp.store(ostream,"configuration file for image viewer. do not hand edit");
	ostream.close();
	}catch (Exception e){e.printStackTrace();}
}

public void readProp(String filename) throws IOException{
	FileInputStream istream=new FileInputStream(new File(filename));
	prp.load(istream);
}

public int getIntProperty(String key, int def){
		  return Integer.parseInt(prp.getProperty(key,(""+def)));
}

public int getIntProperty(String key, String def){
	    return Integer.parseInt(prp.getProperty(key,def));
}


public String getStringProperty(String key, String def){
			return (String)prp.getProperty(key,def);
}

public boolean getBooleanProperty(String key, String def){
		   return (prp.getProperty(key,def).equalsIgnoreCase("true"));
}

public boolean getBooleanProperty(String key, boolean def){
	       String tmp="";
	       if (def) tmp="true";
	       else tmp="false";
		   return (prp.getProperty(key,tmp).equalsIgnoreCase("true"));
}


public void setBooleanProperty(String key, boolean def){
	      setProperty(key,def);
}

public void setProperty(String key, boolean value){
	       if (value) prp.setProperty(key,"true");
	       else prp.setProperty(key,"false");
}

public void setProperty(String key, int value){
		   prp.setProperty(key,""+value);
}

public String getProperty(String key){
	 return prp.getProperty(key);
}

public String getProperty(String key, String def){
	 return prp.getProperty(key,def);
}

public void setProperty(String key, String value){
	prp.setProperty(key,value);
}


}