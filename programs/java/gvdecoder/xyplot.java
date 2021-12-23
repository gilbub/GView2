package gvdecoder;
import javax.swing.*;

public class xyplot extends JInternalFrame{

 JPanel base;
 GView gv;
 GraphButton graph;
 public xyplot(GView gv, double[] data){
   super("plot",
                 true, //resizable
                 true, //closable
                 true, //maximizable
              true);//iconifiable
   this.gv=gv;
   base=new JPanel();
   graph=new GraphButton(data);
   base.add(graph);
   this.getContentPane().add(base);
   setVisible(true); //necessary as of kestrel
   gv.desktop.add(this);
 }


}