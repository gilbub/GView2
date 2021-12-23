/*
 */

package gvdecoder;

/**

 */
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.JTextArea;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Vector;

public class TreeGui extends JPanel
                      implements TreeSelectionListener {
    Vector commands;



    public JEditorPane htmlPane;
    public JTextArea botPane;
    private JTree tree;

    private URL helpURL;
    private static boolean DEBUG = false;

    int[][] aoivals={
		{2592,2160,1,1,1},
		{2544,2160,1,17,25},
		{2064,2048,57,257,265},
		{1776,1760,201,401,409},
		{1920,1080,537,337,337},
		{1392,1040,561,593,601},
        {528,512,825,1025,1033},
        {240,256,953,1169,1177},
        {144,128,1017,1217,1225},
        {2592,304,929,1,1}
	};

    String [][] params={

		{"PixelEncoding","Mono12", "Mono12Packed", "Mono16", "Mono32"},
		{"TriggerMode","Internal", "Software", "External", "External Start", "External Exposure"},
		{"PixelReadoutRate","280 MHz", "200 MHz", "100 MHz"},
		{"CycleMode","Fixed", "Continuous"},
		{"ElectronicShutteringMode", "Rolling", "Global"},
		{"PreAmpGainControl","Gain 1 (11 bit)","Gain 2 (11 bit)","Gain 3 (11 bit)","Gain 4 (11 bit)","Gain 1 Gain 3 (16 bit)","Gain 1 Gain 4 (16 bit)", "Gain 2 Gain 3 (16 bit)","Gain 2 Gain 4 (16 bit)"},
		{"SensorCooling","false","true"},
		{"SpuriousNoiseFilter","false","true"},
		{"TemperatureControl","-15.00","-20.00","-25.00","-30.00","-35.00","-40.00"},
		{"FanSpeed","Off","Low","On"},
        {"OverwriteDirectory","false","true"}
		};


//{"FrameRate","1","5","10","12.5","15","20"."40","50","75","98","100","150"}





    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";

    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = true;

    private FOV genAOIFOV(String title, int index){
		String[] tmp=new String[4];
		tmp[0]="AOITop=" +aoivals[index][2];
		tmp[1]="AOILeft="+aoivals[index][3];
		tmp[2]="AOIWidth="+aoivals[index][0];
		tmp[3]="AOIHeight="+aoivals[index][1];
		FOV fov=new FOV(title,tmp);
		return fov;
	}


    public TreeGui() {
        super(new GridLayout(1,0));

        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("Camera Parameters");
        //createNodes(top);
        for (int i=0;i<params.length;i++){
			createNode(top,params[i]);
		}

         FOV[] FOVs=new FOV[aoivals.length];
         FOVs[0]=genAOIFOV("full",0);
         FOVs[1]=genAOIFOV("5 MPix",1);
         FOVs[2]=genAOIFOV("4 MPix",2);
         FOVs[3]=genAOIFOV("3 MPix",3);
         FOVs[4]=genAOIFOV("2 MPix",4);
         FOVs[5]=genAOIFOV("1 MPix",5);
         FOVs[6]=genAOIFOV("250 KPix",6);
         FOVs[7]=genAOIFOV("60 KPix",7);
         FOVs[8]=genAOIFOV("18 KPix",8);
         FOVs[9]=genAOIFOV("long",9);




         createNode(top,FOVs);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);
        tree.setEditable(false);
        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(true);
        htmlPane.setText("# Set these parameters directly\nFrameRate = "+1+"\n"+"ExposureTime = "+0.01+"\nFrameCount ="+10+"\nNumberOfBuffers = "+3+"\n\n# Set these parameters from left panel\n");
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(200, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(200);
        splitPane.setPreferredSize(new Dimension(400, 300));


        JSplitPane topbotpane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        botPane=new JTextArea(5,30);
        //botPane.setEditable(false);
        JScrollPane botView = new JScrollPane(botPane);
        botPane.setText("# Camera Status (focus or record to update)\n\n\n\n\n");
        minimumSize = new Dimension(200, 150);
        botView.setMinimumSize(minimumSize);

        topbotpane.setTopComponent(splitPane);
        topbotpane.setBottomComponent(botView);


        //Add the split pane to this panel.
        add(topbotpane);
    }

    public String getUserParams(){
		String txt=htmlPane.getText();
		int locframerate_b=txt.indexOf("FrameRate");
		int locframerate_s=txt.indexOf("=",locframerate_b);
		int locframerate_e=txt.indexOf("\n",locframerate_s);
		String framerate=txt.substring(locframerate_s+1,locframerate_e);
		framerate=framerate.trim();
		double fr;
		try{
			System.out.println("framerate="+framerate);
			fr= java.lang.Double.parseDouble(framerate);
		}catch(Exception e){
			fr=1.0;
		}

		int locexposuretime_b=txt.indexOf("ExposureTime");
		int locexposuretime_s=txt.indexOf("=",locexposuretime_b);
		int locexposuretime_e=txt.indexOf("\n",locexposuretime_s);
		String exposuretime=txt.substring(locexposuretime_s+1,locexposuretime_e);
		exposuretime=exposuretime.trim();
		double ex;
		try{
			System.out.println("exposuretime="+exposuretime);
			ex= java.lang.Double.parseDouble(exposuretime);
		}catch(Exception e){
			ex=0.01;
        }
        int locframes_b=txt.indexOf("FrameCount");
		int locframes_s=txt.indexOf("=",locframes_b);
		int locframes_e=txt.indexOf("\n",locframes_s);
		String frames=txt.substring(locframes_s+1,locframes_e);
		frames=frames.trim();
		int fc;
		try{
			System.out.println("frames="+frames);
			fc= java.lang.Integer.parseInt(frames);
		}catch(Exception e){
			fc=10;
        }


        int locbuffers_b=txt.indexOf("NumberOfBuffers");
		int locbuffers_s=txt.indexOf("=",locbuffers_b);
		int locbuffers_e=txt.indexOf("\n",locbuffers_s);
		String buffers=txt.substring(locbuffers_s+1,locbuffers_e);
		buffers=buffers.trim();
		int bf;
		try{
			System.out.println("buffers="+buffers);
			bf= java.lang.Integer.parseInt(buffers);
		}catch(Exception e){
			bf=3;
        }
        //"# Set first three parameters by hand\n#FrameRate = 1.0\nExposureTime = 0.01\nNumberOfBuffers = 3# Set first three parameters by hand\n")
        return "# Set these parameters directly\nFrameRate = "+fr+"\n"+"ExposureTime = "+ex+"\nFrameCount ="+fc+"\nNumberOfBuffers = "+bf+"\n\n# Set these parameters from left panel\n";

	   }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();

        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            DefaultMutableTreeNode parent=(DefaultMutableTreeNode)node.getParent();
            String keyvalue=(String)parent.getUserObject();
            keyvalue=keyvalue.trim();
            if (keyvalue.contains("=")){
				int p=keyvalue.indexOf("=");
				keyvalue=keyvalue.substring(0,p);
			 }
            //System.out.print(keyvalue+" = "+paramvalue+"\n");
            if (keyvalue.contains("FOV")){
			  FOV fov=(FOV)nodeInfo;
			  parent.setUserObject(keyvalue +"="+fov.name);
			  editcommands(keyvalue,fov);
			}
			else{
			String paramvalue  = (String)nodeInfo;
			parent.setUserObject(keyvalue +"="+paramvalue);
            editcommands(keyvalue,paramvalue);

		}//else
		String commandset=getUserParams();
		if (commands!=null){
						for (int i=0;i<commands.size();i++){
							commandset+=(String)commands.elementAt(i)+"\n";

						}
						htmlPane.setText(commandset);

						//System.out.println("");
			}
	   }//if
      tree.repaint();
    }


   public void editcommands(String keyvalue, FOV fov){
	  if (commands==null){
		 commands=new Vector();
		 commands.add( ("OverwriteDirectory = true") );
		 commands.add( ("#FOV = "+fov.name));
		 for (int j=0;j<fov.AOIs.length;j++){
		  	 commands.add(fov.AOIs[j]);
	      }
		 return;
	   }
	   for (int i=0;i<commands.size();i++){
		  String command=(String)commands.elementAt(i);
		  if (command.contains("#FOV")){
			  for (int k=0;k<5;k++){
			      //System.out.println("removing "+(i)+" = "+(String)commands.elementAt(i));
				  commands.removeElementAt(i);
			    }
			  commands.add(i,"#FOV = "+fov.name);
			  for (int m=1;m<5;m++) commands.add(i+m,fov.AOIs[m-1]);
		      return;
		  }
	   }
        commands.add("#FOV = "+fov.name);
        for (int n=0;n<4;n++){
			commands.add(fov.AOIs[n]);
		}
   }



   public void editcommands(String keyvalue, String paramvalue){

	   if (commands==null){
		 commands=new Vector();
		 commands.add( ("OverwriteDirectory = true") );
		 commands.add( (keyvalue+" = "+paramvalue));

		 return;
	   }
	   for (int i=0;i<commands.size();i++){
		 String command=(String)commands.elementAt(i);
	     if (command.contains(keyvalue)){
			command=keyvalue+" = "+paramvalue;
			commands.removeElementAt(i);
			commands.add(i,command);
			return;
		 }

       }
       commands.add((keyvalue+" = "+paramvalue));

   }


    public void createNode(DefaultMutableTreeNode top, String[] keyvals){
		DefaultMutableTreeNode key = null;
        DefaultMutableTreeNode value = null;
        key=new DefaultMutableTreeNode(keyvals[0]+"                ");
        //key.setUserObject(keyvals[0]);
        for (int i=1;i<keyvals.length;i++){
			value=new DefaultMutableTreeNode(keyvals[i]);
			//value.setUserObject(keyvals[i]);
			key.add(value);
		}
		top.add(key);
	}

     public void createNode(DefaultMutableTreeNode top, FOV[] fovs){
	 		DefaultMutableTreeNode key = null;
	        DefaultMutableTreeNode value = null;
	        DefaultMutableTreeNode svalue = null;
	         key=new DefaultMutableTreeNode("FOV                   ");
	         //key.setUserObject(keyvals[0]);
	         for (int i=0;i<fovs.length;i++){
	 			value=new DefaultMutableTreeNode(fovs[i]);

	 			//value.setUserObject(keyvals[i]);
	 			key.add(value);
	 		}
	 		top.add(key);
	}


   public class FOV{

	   String name;
	   String [] AOIs;
	   public FOV(String name, String[] AOIs){
		 this.name=name;
		 this.AOIs=AOIs;
	   }
	   public String toString(){ return name;}
  }




    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Create and set up the window.
        JFrame frame = new JFrame("TreeDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new TreeGui());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}