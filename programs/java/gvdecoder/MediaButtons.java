package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.geom.*;

public class MediaButtons extends JPanel{

ImageViewer v2;

public MediaButtons(ImageViewer v2, boolean Rewind, boolean Stepback, boolean Stop, boolean Play, boolean Stepforward){

JTextField tf;

this.v2=v2;

this.setMaximumSize(new Dimension(200, 16));
this.setMinimumSize(new Dimension(80, 16));
this.setPreferredSize(new Dimension(80,16));
this.setLayout(null);
Insets insets = this.getInsets();

int offset=0;

if (Rewind){
JButton button1 = new JButton(new ImageIcon("gvdecoder/images/Redo16.gif"));
 button1.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
		   Rewind();
			 }
			});
this.add(button1);
button1.setBounds(offset,0,16,16);
offset+=16;
}

if (Stepback){
JButton button2 = new JButton(new ImageIcon("gvdecoder/images/StepBack16.gif"));
this.add(button2);
button2.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
		   BackOneFrame();
			 }
			});
button2.setBounds(offset,0,16,16);
offset+=16;
}

if (Stop){
JButton button3  = new JButton(new ImageIcon("gvdecoder/images/Pause16.gif"));
this.add(button3);
button3.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e){
			 Stop();
			 }
			});
button3.setBounds(offset,0,16,16);
offset+=16;
}

if (Play){
JButton button4 = new JButton(new ImageIcon("gvdecoder/images/Play16.gif"));
button4.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e){
			  Start();
			 }
			});
this.add(button4);
button4.setBounds(offset,0,16,16);
offset+=16;
}

if (Stepforward){
JButton button5 = new JButton(new ImageIcon("gvdecoder/images/StepForward16.gif"));
button5.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e){
		   AdvanceOneFrame();
			 }
			});
this.add(button5);
button5.setBounds(offset,0,16,16);
}
/*
tf=new JTextField(15);
tf.setToolTipText("<html>Enter <b>one</b> or <b>four</b> comma seperated values <br>for frame, scale(float), top x corner, top y corner <br>(e.g. 10,2.3,100,100). Enter 0 to autoscale, 1 number to jump to frame</html>");

tf.addActionListener(new tfListener(this));
this.add(tf);
*/
}

public void Start(){
 v2.Start();
}
public void Stop(){
 v2.Stop();
}
public void Rewind(){
 v2.Rewind();
}

public void AdvanceOneFrame(){
 v2.AdvanceOneFrame();
}

public void BackOneFrame(){
 v2.BackOneFrame();
}
/*
public void Start(){
 v2.frozen=false;
 v2.startAnimation();
}
public void Stop(){
 v2.frozen=true;
 v2.stopAnimation();
}
public void Rewind(){
 v2.JumpToFrame(v2.LastUserSetFrame);
}

public void AdvanceOneFrame(){
 v2.frozen=true;
 v2.stopAnimation();
 v2.AdvanceOneFrame();

}

public void BackOneFrame(){
 v2.frozen=true;
 v2.stopAnimation();
 v2.BackOneFrame();

}
*/
}
/*
class tfListener implements ActionListener{
 MediaButtons cp;

 public tfListener(MediaButtons cp){
  this.cp=cp;

  }

 public void actionPerformed(ActionEvent e){
	 double frame,scale,xoff,yoff;
	 String text=cp.scalefield.getText();

	 String[] vals=text.split(",");
	 if (vals.length==1){
		try{
		frame=java.lang.Integer.valueOf(text);
	    cp.v2.JumpToFrame((int)frame);
	    cp.v2.updateTitle();
	    return;
	    }catch(NumberFormatException ex){
		 cp.scalefield.setText("enter 1 or 4 comma separated values");
		 return;
		}
	 }else
	 if (vals.length!=4){
		cp.scalefield.setText("enter 1 or 4 comma separated values");
		cp.v2.jp.offsetx=0;
		cp.v2.jp.offsety=0;
		cp.v2.setVisualScale(-1.0f);
		return;
	 }
	 try{
		frame=java.lang.Integer.valueOf(vals[0]);
		scale=java.lang.Double.valueOf(vals[1]);
		xoff=java.lang.Double.valueOf(vals[2]);
		yoff=java.lang.Double.valueOf(vals[3]);
		cp.v2.JumpToFrame((int)frame);
		cp.v2.jp.offsetx=-1*(int)(xoff*scale);
		cp.v2.jp.offsety=-1*(int)(yoff*scale);
		cp.v2.setVisualScale((float)scale);
		cp.scalefield.setText("done");
		cp.v2.updateTitle();

	 }catch(NumberFormatException ex){
		 cp.scalefield.setText("not a number...");
		 cp.v2.jp.offsetx=0;
		 cp.v2.jp.offsety=0;
		 cp.v2.setVisualScale(-1.0f);
	 }
 }
}
*/