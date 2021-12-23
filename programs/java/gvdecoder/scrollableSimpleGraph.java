package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


class scrollableSimpleGraph extends JPanel{

simpleGraph sg;

public scrollableSimpleGraph(){
        sg=new simpleGraph();
        //Put the drawing area in a scroll pane.
        JScrollPane scroller = new JScrollPane(sg);
        scroller.setPreferredSize(new Dimension(200,200));

        //Layout this demo.
        setLayout(new BorderLayout());
        add(scroller, BorderLayout.CENTER);


}


}