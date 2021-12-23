package gvdecoder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

public class DragControlTest{

public JPanel createButtonPanel(){
JPanel buttonPanel=new JPanel();
DragControl dc1 = new DragControl(Color.gray,12.33,36.55,6000,true,0,"###.00","monkeys");

DragControl dc = new DragControl(Color.red,-10000,10000,6000,true,1,"###","the framerate");
buttonPanel.add(dc);
buttonPanel.add(dc1);
dc.addActionListener(new ActionListener(){
 public void actionPerformed(ActionEvent e){
	DragControl mydc=(DragControl)e.getSource();
	System.out.println("listener gets="+(int)mydc.getValue());
 }
});
return (buttonPanel);
}




public static void main(String[] args){
DragControlTest dct=new DragControlTest();
JFrame frame=new JFrame("test");
JPanel buttons=dct.createButtonPanel();
frame.getContentPane().add(buttons,BorderLayout.CENTER);
frame.pack();
frame.setVisible(true);

}//main
}