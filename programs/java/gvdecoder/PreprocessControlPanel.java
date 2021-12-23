package gvdecoder;
//package filters;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PreprocessControlPanel extends JPanel{

Viewer2 vw;
JPanel contents;
JList list;
JToggleButton Normalize,Background,Custom,Raw;
HistogramButton Scalebutton;
Dimension preferredSize = new Dimension(115,300);

public Dimension getPreferredSize(){
		 return preferredSize;
 }



 public Dimension getMaximumSize(){
		 return preferredSize;
 }


 public void resetButtons(){
  if (vw.background!=null) Background.setSelected(true);
   else Background.setSelected(false);
  if (vw.normalize!=null) Normalize.setSelected(true);
   else Normalize.setSelected(false);
  repaint();
}

public PreprocessControlPanel(Viewer2 vw){

 this.vw=vw;
 if (vw.histdata==null){
  vw.histdata=new double[256];
  for (double i=0;i<256;i++) vw.histdata[(int)i]=i;
 }
 Scalebutton=new HistogramButton(vw,vw.histdata);
// Scalebutton.addActionListener(imageReaderListener);

 contents=new JPanel();
 contents.setLayout(new GridLayout(0,1));

this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

Border lineBorder=BorderFactory.createEtchedBorder();
Border titled = BorderFactory.createTitledBorder(lineBorder,"Preprocess",TitledBorder.LEFT,TitledBorder.TOP);
	setBorder(titled);

ImageReaderListener imageReaderListener=new ImageReaderListener(this);
//JPanel ImageReaderControl=new JPanel();
ButtonGroup bgroup=new ButtonGroup();
Raw=new JToggleButton("raw");
Raw.addActionListener(imageReaderListener);
Raw.setSelected(true);
bgroup.add(Raw);
//ImageReaderControl.add(label);
Background=new JToggleButton("back");
Background.addActionListener(imageReaderListener);
bgroup.add(Background);
//JToggleButton Average=new JToggleButton("average");
//JToggleButton AveSub=new JToggleButton("av. sub");
Normalize=new JToggleButton("normalize");
Normalize.addActionListener(imageReaderListener);
Custom=new JToggleButton("user def");
Custom.addActionListener(imageReaderListener);
bgroup.add(Custom);



//contents.add(AveSub);
bgroup.add(Normalize);
contents.add(Raw);
contents.add(Background);
contents.add(Normalize);
contents.add(Custom);
//contents.add(Scalebutton);


//contents.add(ImageReaderControl);
JPanel DragButtons=new JPanel();
DragButtons.setLayout(new GridLayout(0,1));

DragControl framesPerSecond=new DragControl(Color.magenta,0,100,10,true,1,"###","framerate");
framesPerSecond.addActionListener(new FramesSliderListener(this));
DragButtons.add(framesPerSecond);

DragControl Offset = new DragControl(Color.gray,-128, +128, 0, false,1,"###","offset");
Offset.addActionListener(new OffsetSliderListener(this));
DragButtons.add(Offset);

DragControl Scale = new DragControl(Color.gray, 0, +400, 100, false, 1, "###", "scale");
Scale.addActionListener(new ScaleSliderListener(this));
DragButtons.add(Scale);

JPanel infopanel=new JPanel();
infopanel.setLayout(new GridLayout(0,1));
infopanel.add(Scalebutton);

this.add(DragButtons);
this.add(contents);
this.add(infopanel);


}
}

class FramesSliderListener implements ActionListener {

PreprocessControlPanel pp;

public FramesSliderListener(PreprocessControlPanel pp){
 this.pp=pp;
}
    public void actionPerformed(ActionEvent e) {
        DragControl source = (DragControl)e.getSource();

	    int fps = (int)source.getValue();
	    if (fps == 0) {
	        pp.vw.stopAnimation();
	    } else {
	       pp.vw.setFPS(fps);
	    }
        }
    }



class ScaleSliderListener implements ActionListener {
PreprocessControlPanel pp;

public ScaleSliderListener(PreprocessControlPanel pp){
 this.pp=pp;
 }
    public void actionPerformed(ActionEvent e) {
        DragControl source = (DragControl)e.getSource();

	    pp.vw.UserScale=((double)source.getValue()/100);
		System.out.println("Userscale= "+pp.vw.UserScale+" scale "+pp.vw.scale);
		pp.vw.rescale();
		pp.vw.repaint();
    }
  }


class OffsetSliderListener implements ActionListener {

PreprocessControlPanel pp;

public OffsetSliderListener(PreprocessControlPanel pp){
 this.pp=pp;
 }

	public void actionPerformed(ActionEvent e) {
        DragControl source = (DragControl)e.getSource();

	    pp.vw.UserOffset = (int)source.getValue();
	    pp.vw.rescale();
		pp.vw.repaint();
        }
    }

/*
class ScalebuttonListener implements ActionListener{
	PreprocessControlPanel cp;
	public ScalebuttonListener(PreprocessControlPanel cp){ this.cp=cp;}

 public void actionPerformed(ActionEvent e){


 }
}
*/
class ImageReaderListener implements ActionListener{
 PreprocessControlPanel cp;

 public ImageReaderListener(PreprocessControlPanel cp){
  this.cp=cp;
  }



 //gets popup events
 public void actionPerformed(ActionEvent e) {
        JToggleButton source = (JToggleButton)(e.getSource());
        String s = "Control Panel Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
		if (source.getText().equals("back")){
		 cp.vw.JavaBackground();
		 //cp.resetButtons();

		}else
		if (source.getText().equals("raw")){
			 System.out.println("raw...");
				 cp.vw.JavaRaw();
				 //cp.resetButtons();

		}else
		if (source.getText().equals("normalize")){
				 System.out.println("normalizing...");
				 cp.vw.JavaNormalize();
				 //cp.resetButtons();
		}else
		if (source.getText().equals("user def")){
						 System.out.println("toggle custom");
						 if (cp.vw.viewerfilter!=null){
						 cp.vw.JavaCustomFilter();
					 }
					 else{
						 cp.Custom.setSelected(false);
						 cp.Raw.setSelected(true);
						 cp.vw.JavaRaw();
					 }
						 //cp.resetButtons();
		}

	}

}























