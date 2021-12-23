package gvdecoder;

import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;

public class drawableframe extends Frame{

drawable dr;
Vector v;  // there may be a vector that holds this object
int gifnum=0;
boolean TOGGLEGIF=false;
String dir="";
String preamble="";


public drawableframe(drawable dr ,Vector v){
 this.dr=dr;
 this.v=v;
 this.setLayout(new GridBagLayout());
 GridBagConstraints c=new GridBagConstraints();

 c.gridx=0; c.gridy=0; c.gridwidth=5; c.gridheight=1;

 this.add((Component)dr,c);

 spacer sp=new spacer(1,1);
 Button makegif=new Button("gif");
 makegif.addActionListener(new ActionListener(){
 	public void actionPerformed(ActionEvent e){
 	writegif();
 	}
 });

 Button togglegif=new Button("series");
 togglegif.addActionListener(new ActionListener(){
  	public void actionPerformed(ActionEvent e){
  	if (TOGGLEGIF) {TOGGLEGIF=false;}
	else {
  	 setupgif();
	 }
	}
 });



   c.gridx=0; c.gridy=1; c.gridwidth=1; c.gridheight=1;
   c.anchor=GridBagConstraints.WEST;
   c.weightx=1; c.weighty=1;
   c.fill=GridBagConstraints.BOTH;
   this.add(sp,c);


   c.fill=GridBagConstraints.NONE;
    c.weightx=0; c.weighty=0;
    c.gridx=2; c.gridy=1; c.gridwidth=1; c.gridheight=1;
   c.anchor=GridBagConstraints.EAST;


   c.fill=GridBagConstraints.NONE;
   c.weightx=0; c.weighty=0;
   c.gridx=3; c.gridy=1; c.gridwidth=1; c.gridheight=1;
   c.anchor=GridBagConstraints.EAST;


   this.add(togglegif,c);

   c.gridx=4; c.gridy=1; c.gridwidth=1; c.gridheight=1;

   c.anchor=GridBagConstraints.EAST;
   this.add(makegif,c);

  this.addWindowListener(new WindowAdapter(){
     public void windowClosing(WindowEvent e){
       removeMyself();
      dispose();
     }});

 this.pack();
 this.show();


  }

 public void setupgif(){

  try{
  FileDialog f = new FileDialog(this,"save a gif series",FileDialog.SAVE);
  f.show();
  preamble=f.getFile();
  dir=f.getDirectory();
  gifnum=0; TOGGLEGIF=true;
  }catch(Exception e){}
 }




 public void writegif(){
 try{
 	FileDialog f = new FileDialog(this,"Save image as GIF",FileDialog.SAVE);
 	f.show();
 	String filename=f.getFile();
     String directory=f.getDirectory();
    GIFEncoder encode = new GIFEncoder(dr.returnimage());
 	OutputStream output = new BufferedOutputStream(new FileOutputStream(directory+filename));
 	encode.Write(output);
	output.close();
 	}catch(Exception ex){System.out.println("Couldn't write the gif: ");ex.printStackTrace();}

 }


public void removeMyself(){
 v.removeElement(this);
 }

public void draw(){
 dr.draw();
 if (TOGGLEGIF){
   try{
    GIFEncoder encode = new GIFEncoder(dr.returnimage());
	OutputStream output = new BufferedOutputStream(new FileOutputStream(dir+preamble+gifnum+".gif"));
 	gifnum++;
	encode.Write(output);
	output.close();
  }catch(Exception ex){System.out.println("problem writing gif"); ex.printStackTrace();}
 }

}


}