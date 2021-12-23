package gvdecoder;
import javax.swing.*;
import java.awt.*;


public class quickContour extends JInternalFrame {

public ContourDrawer cd;
public quickContour(String filename){
	super("quick",
							 true, //resizable
							 true, //closable
							 true, //maximizable
			  true);//iconifiable

cd=new ContourDrawer(filename);
SetupWindow();
}



public void SetupWindow(){

 JPanel jp=new JPanel();
 jp.add(cd);
 this.getContentPane().add(jp,BorderLayout.CENTER);
 setSize(new Dimension(350,350));
 setLocation(10,10);
}



}