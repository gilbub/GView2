package gvdecoder;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.GridLayout;
import javax.swing.*;
import java.io.*;
import java.io.Writer.*;
public class NeoGui extends JPanel{

TreeGui treegui;

JPanel top;
JPanel mid;
JPanel bot;
JButton infobutton;
JButton datadir,record,load,exit;
JToggleButton focus;
JTextField datadirfield;
NeoController neo;
JFrame guiframe;
int xdim=-1;
int ydim=-1;
String DataPath=null;

static boolean useSystemLookAndFeel=true;

public NeoGui(NeoController neo){

  this.neo=neo;

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


  treegui=new TreeGui();
  this.add(treegui);

  JPanel inf= new JPanel();
  inf.setLayout(new BoxLayout(inf,BoxLayout.X_AXIS));
  infobutton=new JButton("fps = "+"     "+"; max fps = "+"     "+"; exp (s) = "+"      "+"; T = "+"     ");
  infobutton.addActionListener(new java.awt.event.ActionListener(){
	  public void actionPerformed(java.awt.event.ActionEvent arg){
		  updateinfo();
	  }
  });
  inf.add(infobutton);
  this.add(inf);
  mid=new JPanel();
  mid.setLayout(new BoxLayout(mid,BoxLayout.X_AXIS));

  mid.add(new JLabel("data dir:"));
  datadirfield=new JTextField("");
  mid.add(datadirfield);
  datadir=new JButton("..");
  datadir.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent arg0) {
           choosedatadir();

                  }
      });
  mid.add(datadir);

  this.add(mid);

  bot=new JPanel();
  bot.setLayout(new GridLayout(1,4));

  load=new JButton("load");
  load.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent arg0) {
                load();
                        }
            });


  bot.add(load);

  focus=new JToggleButton("focus");
  focus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent arg0) {
              gen_focus();
                      }
          });

  bot.add(focus);

  record=new JButton("record");
  record.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent arg0) {
            gen_record();
                    }
        });

  bot.add(record);
  this.add(bot);

  exit=new JButton("exit");
  exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent arg0) {
              exit_camera();
                      }
          });

  bot.add(exit);

}

public void updateinfo(){
	double [] infoarray=new double[4];
	neo.GetCameraParameters(infoarray);
	java.text.DecimalFormat df=new java.text.DecimalFormat("#.00");
	java.text.DecimalFormat dfe=new java.text.DecimalFormat("#.0000");

	infobutton.setText("fps = "+df.format(infoarray[0])+"; max fps = "+df.format(infoarray[1])+"; exp (s) = "+dfe.format(infoarray[2])+"; T = "+df.format(infoarray[3]));

}


public String choosefile(boolean directories){
	 JFileChooser chooser = new JFileChooser();
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
      if (directories) chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		 try{
	       return  chooser.getSelectedFile().getCanonicalPath();
	   }
	   catch(Exception e){;}
     }
	return "";
}

public void choosedatadir(){
	datadirfield.setText(choosefile(true));
}

public void addText(java.util.Properties prop, String key, java.util.Vector v, java.util.Vector c, boolean addc){
	String val=prop.getProperty(key);
	if (val!=null){
		v.add((key+" = "+val));
	    if (addc)
	    c.add((key+" = "+val));
	}
}

public void load(){
	String pf=choosefile(false);
    try{
    FileInputStream fstream=new FileInputStream(pf);
	java.util.Properties prop=new java.util.Properties();
	prop.load(fstream);
    fstream.close();
    //java.util.Enumeration pn=prop.propertyNames();
    treegui.commands=new java.util.Vector();
    treegui.htmlPane.setText("");

    java.util.Vector sv=new java.util.Vector();
    sv.add("# Edit next 4 by hand\n");
    addText(prop,"FrameRate",sv,treegui.commands,false);
    addText(prop,"ExposureTime",sv,treegui.commands,false);
    addText(prop,"FrameCount",sv,treegui.commands,false);
    addText(prop,"NumberOfBuffers",sv,treegui.commands,false);
    treegui.commands.add("#FOV loaded");
    sv.add("#FOV loaded");
    addText(prop,"AOITop",sv,treegui.commands,true);
    addText(prop,"AOILeft",sv,treegui.commands,true);
    addText(prop,"AOIWidth",sv,treegui.commands,true);
    addText(prop,"AOIHeight",sv,treegui.commands,true);
    addText(prop,"PixelReadoutRate",sv,treegui.commands,true);
    addText(prop,"ElectronicShutteringMode",sv,treegui.commands,true);
    addText(prop,"CycleMode",sv,treegui.commands,true);
    addText(prop,"PreAmpGainControl",sv,treegui.commands,true);
    addText(prop,"TriggerMode",sv,treegui.commands,true);
    addText(prop,"SaveData",sv,treegui.commands,true);
    addText(prop,"SaveImage",sv,treegui.commands,true);
    addText(prop,"TemperatureControl",sv,treegui.commands,true);
    addText(prop,"SpuriousNoiseFilter",sv,treegui.commands,true);
    addText(prop,"SensorCooling",sv,treegui.commands,true);
    addText(prop,"FanSpeed",sv,treegui.commands,true);
    addText(prop,"OverwriteDirectory",sv,treegui.commands,true);
    addText(prop,"PixelEncoding",sv,treegui.commands,true);
    String tmp="";
    for (int i=0;i<sv.size();i++){
		tmp=tmp.concat((String)sv.elementAt(i)+"\n");
	}
	treegui.htmlPane.setText(tmp);

    }catch(Exception e){e.printStackTrace();}

}

public void exit_camera(){
	neo.exit();

}

public boolean gen_record(){
    if (datadirfield.getText()==""){

		choosedatadir();
	}
   DataPath=datadirfield.getText();
	try{

		File fdir=new File(DataPath);
		if (!fdir.exists()) fdir.mkdir();
		 String recname=DataPath+File.separator+"neo_record.txt";

		 PrintWriter f = new PrintWriter(new FileWriter(recname));

		 f.write("# autogenerated camera config file.\n");
		 f.write("# \n");
		 //f.write("OutputFileName = "+DataPath+File.separator+"dat\n");
		 String basedir=fdir.getParent();
		 basedir=basedir.replace("\\","\\\\");
		 f.write("MainDataDirectory ="+basedir+"\\\\\n");
	     f.write("DataSetName = "+fdir.getName()+"\n");
		 f.write(treegui.htmlPane.getText());
		 f.write("\n");
		 f.close();
         if (finddims()){
		      neo.record(recname,xdim,ydim);
		      //update_status();
		      updateinfo();
		      }
         else {treegui.botPane.setText("Error: failure to parse xdim,ydim\n Camera not responding?");}
		}catch(Exception e){e.printStackTrace(); return false;}
      return true;
	}



public boolean gen_focus(){
	if (focus.isSelected()){
	String[] override={"CycleMode","TriggerMode","NumberOfBuffers","SaveData","SaveImage","FrameCount"};
    System.out.println("in gen_focus");
	if (datadirfield.getText()==""){

			choosedatadir();
		}
	    DataPath=datadirfield.getText();
		try{

			File fdir=new File(DataPath);
			if (!fdir.exists()) fdir.mkdir();
			 String recname=DataPath+File.separator+"neo_focus.txt";

			 PrintWriter f = new PrintWriter(new FileWriter(recname));

			 f.write("# autogenerated camera config file.\n");
			 f.write("# \n");
             String tmp=treegui.htmlPane.getText();
             String[] strings=tmp.split("\n");
             for (int i=0;i<strings.length;i++){
			  boolean write=true;
		      for (int j=0;j<override.length;j++){
				  if (strings[i].startsWith(override[j])) write=false;
			  }
			  if (write) f.write(strings[i]+"\n");
		     }
		     f.write("\n#These parameters are automatically set for focusmode\n");
			 f.write("TriggerMode = Software\nCycleMode = Continuous\nNumberOfBuffers = 3\nSaveData = false\nSaveImage = false\n");

			 f.write("\n");

			 f.close();
			 if (finddims()){
              neo.startfocus(recname,xdim,ydim);
              //update_status();
              updateinfo();
		      }
             else {treegui.botPane.setText("Error: failure to parse xdim,ydim\n Camera not responding?");}
			}catch(Exception e){e.printStackTrace(); return false;}

	      return true;
	      }
	      else{
			  neo.stopfocus();
			  return true;
		  }
		}

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


public void update_status(){
	String[] strs =parseCameraStatusFile();
	if (strs!=null){
	 double[] mF=getMinMax(strs,"FrameRate",0);


	 treegui.botPane.setText(strs[5]+"\n"+strs[3]+"\nframe rate =\t"+getDouble(strs,"FrameRate",0)+"\t(max = "+mF[1]+")\nexpose (s) =\t"+getDouble(strs,"ActualExposureTime",0)+"\ntemperature =\t"+getDouble(strs,"SensorTemperature",0)+"\t(set = "+getDouble(strs,"TargetSensorTemperature",0)+")");
   }
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

public int getValue(String txt, String key){
	int loc1=txt.indexOf(key);
    if (loc1>=0){
		int loc2=txt.indexOf("=",loc1);
		String val=txt.substring(loc2+1);
		val=val.trim();
		int res=-1;
		try{
		 res= Integer.parseInt(val);
	 }catch(Exception e){;}
	   return res;
	}
	return -1;
}

public boolean finddims(){
	xdim=-1;
	ydim=-1;
	boolean lookx=true;
	boolean looky=true;
	if (treegui.commands!=null){
		for (int i=0;i<treegui.commands.size();i++){
	       String tmp=(String)treegui.commands.elementAt(i);
	       if (lookx) {
			   xdim=getValue(tmp,"AOIWidth");
			   if (xdim>0) lookx=false;
		   }
		   if (looky){
	       	   ydim=getValue(tmp,"AOIHeight");
	       	   if (ydim>0) looky=false;
		   }
	   }
	   if ((xdim<0)||(ydim<0))	return false;

	  return true;
     }
     System.out.println("commands = null - cant find dims");
     return false;
}


public void showgui() {


        //Create and set up the window.
        guiframe = new JFrame("NeoGui");
       // guiframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        //NeoGui jp=new NeoGui(neo);
        guiframe.add(this);

        //Display the window.
        guiframe.pack();
        guiframe.setVisible(true);
    }




    /*
    public static void main(String[] args) {
	        //Schedule a job for the event dispatch thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                createAndShowGUI();
	            }
	        });
    }
    */

}