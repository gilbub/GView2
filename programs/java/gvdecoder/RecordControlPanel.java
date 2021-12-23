package gvdecoder;
//package filters;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.CardLayout;
import java.awt.GridBagLayout;


/*
 RecordControlPanel is a control panel that sits in a Viewer2 GUI to control a camera.

 RecordControlPanel is designed to work well with external scripts, with several Swing invokelater methods.
*/
public class RecordControlPanel extends JPanel implements FrameListener, ActionListener{

Viewer2 vw;
CardLayout cardlayout;
JPanel savepanel,exposepanel,messagepanel,cardpanel;
JPanel contents;
JList list;
JTextField pathfield;
JButton choosepathbutton;
JTextField filefield;
JTextField numberframesfield;
JTextField exposefield;
JLabel exposelabel;
JTextField periodfield;
JLabel periodlabel;
JTextField framenumberfield;
JTextField gainfield;
JLabel gainlabel;
JSlider gainslider;
JToggleButton autonamebutton;
JToggleButton triggerbutton;
JButton savebutton;
JToggleButton backgroundbutton;
JToggleButton rescalebutton;
JToggleButton focusbutton;
JButton infobutton;
JTextArea infofield;
JToggleButton infoupdatebutton;
JTabbedPane tabbedPane;
JLabel messagelabel;
public boolean gainfieldsenabled=true;
public boolean exposefieldsenabled=true;
public boolean periodfieldsenabled=true;

static int preferredwidth=235;


JToggleButton Normalize,Background,Custom,Raw;
public HistogramButtonPanel Scalebutton;
Dimension preferredSize = new Dimension(preferredwidth,100);


public void setSaveButtonRed() {
  SwingUtilities.invokeLater(new Runnable() {
    public void run() {
      savebutton.setBackground(Color.RED);
      savebutton.setEnabled(false);
    }
  });
}

public void setInfoField(final String info){
 SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      infofield.setText(info);
	  }
  });
}


public void setTriggerButton(final boolean triggerstate){
 SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      triggerbutton.setSelected(triggerstate);
	  }
  });
}

public void setSaveButtonWhite() {
  SwingUtilities.invokeLater(new Runnable() {
    public void run() {
      savebutton.setBackground(null);
      savebutton.setEnabled(true);
    }
  });
}

public void updatePath(final String newpath, final String newfilename) {
  SwingUtilities.invokeLater(new Runnable(){
      public void run() {
       pathfield.setText(newpath);
       filefield.setText(newfilename);
      }
    }
  );
}


public void updateFrames(final String newframes){
  SwingUtilities.invokeLater(new Runnable(){
      public void run() {
       framenumberfield.setText(newframes);
      }
    }
  );
}

public void updateExposePeriod(final String expose, final String period){
  SwingUtilities.invokeLater(new Runnable(){
      public void run() {
       exposefield.setText(expose);
       periodfield.setText(period);
      }
    }
  );
}

public void updateGainField(final String val){
	 SwingUtilities.invokeLater(new Runnable(){
	      public void run() {
	       gainfield.setText(val);

	      }
	    }
	  );
}

public void updateGainSlider(final int val){
	 SwingUtilities.invokeLater(new Runnable(){
		      public void run() {
		       gainslider.setValue(val);
		      }
		    }
	  );
}


//this isn't thread safe
public  void setEnabled(Component component, boolean enabled) {
    component.setEnabled(enabled);
    if (component instanceof Container) {
        for (Component child : ((Container) component).getComponents()) {
            setEnabled(child, enabled);
        }
    }
}

//this should be
public void enableall(final boolean val){
	 try{
	 SwingUtilities.invokeAndWait(new enabler(this,val));
    }catch(Exception e){e.printStackTrace();}
    //some of the fields may default to disabled.
    if (!val){
		  SwingUtilities.invokeLater(new Runnable(){
			public void run(){
		    messagelabel.setEnabled(true);
		   }
	   }
	  );
	 }
    else if (val){
	 SwingUtilities.invokeLater(new Runnable(){
	 	      public void run() {
			   if (!gainfieldsenabled){
				  gainslider.setEnabled(false);
                  gainfield.setEnabled(false);
                  gainlabel.setEnabled(false);
			     }
			   if (!exposefieldsenabled){
				  exposefield.setEnabled(false);
				  exposelabel.setEnabled(false);
			   }
			   if (!periodfieldsenabled){
				   periodfield.setEnabled(false);
				   periodlabel.setEnabled(false);
			   }

	 	      }
	 	    }
	  );

  }}


public void message(String txt){
 if (txt!=null){
  messagelabel.setText(txt);
  cardlayout.show(cardpanel,"message");
 }
 else{
	 cardlayout.show(cardpanel,"save");
}
}

public void SetFrame(int framenumber){
	Scalebutton.forceRepaint();
}

 public void resetButtons(){
  if (vw.background!=null) Background.setSelected(true);
   else Background.setSelected(false);
  if (vw.normalize!=null) Normalize.setSelected(true);
   else Normalize.setSelected(false);
  repaint();
}

public void jythoncommand(String val, String state){
	 if (vw.ifd.jv!=null) {
        String str="javacommand('"+vw.filename+"','"+val+"','"+state+"')";
        System.out.println("trying to send the following to jython: "+str);
	 	vw.ifd.jv.interp.exec(str);
	 }
}

public void actionPerformed(ActionEvent e){
	Object source=e.getSource();
	String command=e.getActionCommand();
	if (source instanceof JButton) jythoncommand(command,"0");
	if (source instanceof JToggleButton) {
		AbstractButton abstractButton = (AbstractButton) e.getSource();
        boolean selected = abstractButton.getModel().isSelected();
		if (selected) jythoncommand(command,"1");
		else jythoncommand(command,"0");
	}
	if (source instanceof JTextField){
		jythoncommand(command,((JTextField)source).getText());
	}

}

public void setbutton(JButton button,String command){
  button.setActionCommand(command);
  button.addActionListener(this);
}

public void setbutton(JToggleButton button, String command){
  button.setActionCommand(command);
  button.addActionListener(this);
}

public void setfield(JTextField field, String command){
	field.setActionCommand(command);
	field.addActionListener(this);
}

public String tabchanged(String title){
	if (title=="info"){
		jythoncommand("infotab","1");
	}
	 else jythoncommand("infotab","0");
	return title;
}

public RecordControlPanel(Viewer2 vw){

 this.vw=vw;
 if (vw.histdata==null){
  vw.histdata=new double[64];
  for (double i=0;i<64;i++) vw.histdata[(int)i]=i;
 }
 Scalebutton=new HistogramButtonPanel(vw,vw.histdata);
// Scalebutton.addActionListener(imageReaderListener);

savepanel=new JPanel();
savepanel.setLayout(new BoxLayout(savepanel,BoxLayout.Y_AXIS));

JPanel topsave=new JPanel();
topsave.setLayout(new BoxLayout(topsave,BoxLayout.X_AXIS));
topsave.add(new JLabel("path:"));

topsave.add(Box.createRigidArea(new Dimension(17,0)));
pathfield=new JTextField("");

topsave.add(pathfield);
setfield(pathfield,"pathtext");
topsave.add(Box.createRigidArea(new Dimension(5,0)));
choosepathbutton=new JButton(new ImageIcon("gvdecoder/images/Open16.gif"));
choosepathbutton.setToolTipText("choose an *existing* directory for saving");
setbutton(choosepathbutton,"choosepath");
topsave.add(choosepathbutton);

JPanel midsave=new JPanel();
midsave.setLayout(new BoxLayout(midsave,BoxLayout.X_AXIS));
midsave.add(new JLabel("name:"));
midsave.add(Box.createRigidArea(new Dimension(10,0)));
filefield=new JTextField("");
setfield(filefield,"filenametext");
midsave.add(filefield);

midsave.add(Box.createRigidArea(new Dimension(5,0)));
autonamebutton=new JToggleButton(new ImageIcon("gvdecoder/images/Edit16.gif"));
autonamebutton.setToolTipText("autonames files - currently only works if you choose a directory first");
midsave.add(autonamebutton);
setbutton(autonamebutton,"autoname");
JPanel botsave=new JPanel();
botsave.setLayout(new BoxLayout(botsave,BoxLayout.X_AXIS));
botsave.add(new JLabel("frames:"));
botsave.add(Box.createRigidArea(new Dimension(2,0)));

framenumberfield=new JTextField("");
framenumberfield.setToolTipText("The number of frames, or '=Xs' or '=Xms' (e.g. '=10s' or '=2500ms') as a shortcut");
setfield(framenumberfield,"totalframes");
botsave.add(framenumberfield);
botsave.add(Box.createRigidArea(new Dimension(10,0)));
triggerbutton=new JToggleButton(new ImageIcon("gvdecoder/images/Export16.gif"));
triggerbutton.setToolTipText("toggle this to make sure both cameras save at the same time");
botsave.add(triggerbutton);
setbutton(triggerbutton,"toggletrigger");
botsave.add(Box.createRigidArea(new Dimension(10,0)));
savebutton=new JButton(new ImageIcon("gvdecoder/images/Movie16.gif"));
savebutton.setToolTipText("start saving - make sure all fields have values first");
botsave.add(savebutton);
setbutton(savebutton,"savenframes");
savepanel.add(topsave);
savepanel.add(midsave);
savepanel.add(botsave);
TitledBorder savetitle=new TitledBorder("save");
savepanel.setBorder(savetitle);
savepanel.setPreferredSize(new Dimension(preferredwidth,100));

messagepanel=new JPanel();
messagepanel.setLayout(new GridBagLayout());
messagelabel=new JLabel("status: OK");
messagepanel.add(messagelabel);


exposepanel=new JPanel();
exposepanel.setLayout(new BoxLayout(exposepanel,BoxLayout.Y_AXIS));
JPanel topexpose=new JPanel();
topexpose.setLayout(new BoxLayout(topexpose,BoxLayout.X_AXIS));
exposelabel=new JLabel("expose(ms):");
topexpose.add(exposelabel);
topexpose.add(Box.createRigidArea(new Dimension(5,0)));
exposefield=new JTextField("");
setfield(exposefield,"exposetime");
topexpose.add(exposefield);
topexpose.add(Box.createRigidArea(new Dimension(5,0)));
periodlabel=new JLabel("period(ms):");
topexpose.add(periodlabel);
topexpose.add(Box.createRigidArea(new Dimension(5,0)));
periodfield=new JTextField("");
periodfield.setToolTipText("time between frames (depends on expose(ms)) - this might be overriden if set too low - check info tab");
setfield(periodfield,"frameperiod");
topexpose.add(periodfield);

JPanel botexpose=new JPanel();
botexpose.setLayout(new BoxLayout(botexpose,BoxLayout.X_AXIS));
//gainslider = new DragControl(Color.gray,0, 100, 0, false,1,"###","gain");
//botexpose.add(gainslider);
gainlabel=new JLabel("gain(%):");
botexpose.add(gainlabel);
botexpose.add(Box.createRigidArea(new Dimension(5,0)));
gainslider=new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
gainslider.setPreferredSize(new Dimension(140,20));
gainslider.addChangeListener(new GainSliderListener(this));
botexpose.add(gainslider);
botexpose.add(Box.createRigidArea(new Dimension(2,0)));
gainfield=new JTextField("");
gainfield.addActionListener(new TextFieldListener(this));
botexpose.add(gainfield);
exposepanel.add(topexpose);
exposepanel.add(botexpose);
exposepanel.setPreferredSize(new Dimension(preferredwidth,70));

TitledBorder exposetitle=new TitledBorder("expose");
exposepanel.setBorder(exposetitle);


tabbedPane=new JTabbedPane();
JPanel infopanel=new JPanel();
infofield=new JTextArea("");
infopanel.setLayout(new BorderLayout());
infopanel.add(infofield,BorderLayout.CENTER);


tabbedPane.add("histogram",Scalebutton);
tabbedPane.add("info",infopanel);

JPanel histpanel=new JPanel();
histpanel.setLayout(new BorderLayout());

histpanel.add(tabbedPane,BorderLayout.CENTER);
backgroundbutton=new JToggleButton("Fr-B");
setbutton(backgroundbutton,"togglebackground");
backgroundbutton.setToolTipText("subtract a dark frame from current image (live only)");
rescalebutton=new JToggleButton("scale");
setbutton(rescalebutton,"scale");
rescalebutton.setToolTipText("Toggle autoscale and live histogram - untoggle for user scale by dragging. May slow refresh rate if on.");
focusbutton=new JToggleButton(new ImageIcon("gvdecoder/images/tv16.png"));
focusbutton.setToolTipText("toggle for live focusing and alignment (untoggle before saving)");
setbutton(focusbutton,"togglefocus");
infobutton=new JButton(new ImageIcon("gvdecoder/images/Intranet.gif"));
setbutton(infobutton,"toggleinfo");
infobutton.setToolTipText("Click to see this camera's extra commands.");





JPanel bothist=new JPanel();
bothist.setLayout(new BoxLayout(bothist,BoxLayout.X_AXIS));
bothist.add(backgroundbutton);
bothist.add(rescalebutton);
bothist.add(focusbutton);
bothist.add(infobutton);
histpanel.add(bothist,BorderLayout.PAGE_END);

histpanel.setPreferredSize(new Dimension(preferredwidth,225));


ChangeListener changeListener=new ChangeListener(){
	public void stateChanged(ChangeEvent evt){
	 JTabbedPane source=(JTabbedPane) evt.getSource();
	 int index=source.getSelectedIndex();
	 String title=source.getTitleAt(index);
	 tabchanged(title);
	}
};

tabbedPane.addChangeListener(changeListener);
cardlayout=new CardLayout();
cardpanel=new JPanel(cardlayout);
cardpanel.add(savepanel,"save");
cardpanel.add(messagepanel,"message");


this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

this.add(cardpanel);
this.add(exposepanel);

this.add(histpanel);
this.add(Box.createHorizontalGlue());
this.add(new JLabel(" . o . "));
this.setPreferredSize(new Dimension(240,400));
this.setMaximumSize(new Dimension(240,400));

}
}

class GainSliderListener implements ChangeListener{
RecordControlPanel rc;
public GainSliderListener(RecordControlPanel rc){
	this.rc=rc;
}
public void stateChanged(ChangeEvent e) {
      JSlider source = (JSlider)e.getSource();
      int gain = (int)source.getValue();
      rc.updateGainField(""+gain);
      if (!source.getValueIsAdjusting()) rc.jythoncommand("gain",""+gain);

}

}

class TextFieldListener implements ActionListener{
RecordControlPanel rc;
public TextFieldListener(RecordControlPanel rc){
	this.rc=rc;
}
public void actionPerformed(ActionEvent e){

  JTextField source = (JTextField)e.getSource();
  if (source==rc.gainfield){
    String text=rc.gainfield.getText();
    rc.updateGainSlider(Integer.parseInt(text));
    rc.jythoncommand("gain",text);
  }

}

}

class enabler implements Runnable{

	Container c;
	boolean val;

	public enabler(Container c, boolean val){
	  this.c=c;
	  this.val=val;
	}

	public  void setEnabled(Component component, boolean enabled) {
	    component.setEnabled(enabled);
	    if (component instanceof Container) {
	        for (Component child : ((Container) component).getComponents()) {
	            setEnabled(child, enabled);
	        }
	    }
   }

	public void run(){
		setEnabled(c,val);
	}
}






















