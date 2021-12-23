package gvdecoder;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class Canvas extends JPanel{
 ImagePanel ip;

public Canvas(){
 ip=null;
}

public Canvas(ImagePanel ip){
  this.ip=ip;
}

public void paintComponent(Graphics g){
   super.paintComponent(g);
   if (ip!=null){
    ip.drawOverlay((Graphics2D)g,ip.decorations);
   }
 }
}