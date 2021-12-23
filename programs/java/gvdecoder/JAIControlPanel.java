package gvdecoder;
//package filters;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class JAIControlPanel extends JPanel{

Viewer2 vw;
JPanel contents;
JList list;
JTextField scalefield;

public JAIControlPanel(Viewer2 vw){

 this.vw=vw;
 contents=new JPanel();
 contents.setLayout(new GridLayout(0,1));

this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
Border lineBorder=BorderFactory.createEtchedBorder();
Border titled = BorderFactory.createTitledBorder(lineBorder,"JAI",TitledBorder.LEFT,TitledBorder.TOP);
	setBorder(titled);



JPanel FilterChooser=new JPanel();
JComboBox filterList = new JComboBox(vw.jp.myfilt.filterNames);
        filterList.setSelectedIndex(4);
        filterList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String filtName = (String)cb.getSelectedItem();
                addFilter(filtName);
            }
        });

FilterChooser.add(new JLabel("filter:"));
FilterChooser.add(filterList);
contents.add(FilterChooser);


list=new JList(vw.jp.myfilt); //list of active filters

list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
JScrollPane listScrollPane = new JScrollPane(list);


this.add(contents);
this.add(listScrollPane);

JPanel FilterControl=new JPanel();
FilterListener filterListener=new FilterListener(this);
JButton RemoveAllFilters=new JButton("clear");
FilterControl.add(RemoveAllFilters);
RemoveAllFilters.addActionListener(filterListener);

JButton RemoveFilter=new JButton("remove");
FilterControl.add(RemoveFilter);
RemoveFilter.addActionListener(filterListener);

this.add(FilterControl);

//JPanel ZoomControl=new JPanel();
//ZoomControl.setLayout(new BoxLayout(ZoomControl,BoxLayout.X_AXIS));
//ZoomControl.add(new JLabel("frame,scale,x,y:"));
//scalefield=new JTextField("");
//scalefield.setToolTipText("<html>Enter <b>one</b> or <b>four</b> comma seperated values <br>for {frame},or {frame, zoom, top x pt, top y pt}<br>(e.g: 10 or 10,2.3,100,100). <br>Enter zoom=-1, to autoscale, just 1 number to jump to frame</html>");
//Dimension d=new Dimension(180,20);
//scalefield.setPreferredSize(d);
//scalefield.setMaximumSize(d);
//scalefield.addActionListener(new ScaleListener(this));
//ZoomControl.add(scalefield);
//this.add(ZoomControl);





}

public void addFilter(String name){
	System.out.println("adding "+ name);vw.jp.myfilt.addFilter(name);
}

public void removeFirstFilter(){
System.out.println("removing "+ vw.jp.myfilt.filters.get(0).toString());
 vw.jp.myfilt.filters.remove(0);

}

public void removeLastFilter(){

 int last=vw.jp.myfilt.filters.size()-1;
 System.out.println("removing "+ vw.jp.myfilt.filters.get(last).toString());
 vw.jp.myfilt.filters.remove(last);

}

}


class FilterListener implements ActionListener{
 JAIControlPanel cp;

 public FilterListener(JAIControlPanel cp){
  this.cp=cp;
  }

 //gets popup events
 public void actionPerformed(ActionEvent e) {
        JButton source = (JButton)(e.getSource());
        String s = "Control Panel Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
		if (source.getText().equals("clear")){
		 cp.vw.jp.myfilt.removeAllFilters();
		}
		if (source.getText().equals("remove")){
		         System.out.println("removing... "+cp.list.getSelectedIndex());
				 cp.vw.jp.myfilt.remove(cp.list.getSelectedIndex());

				}


	}

}
/*
class ScaleListener implements ActionListener{
 JAIControlPanel cp;
 public ScaleListener(JAIControlPanel cp){
  this.cp=cp;
  }

 public void actionPerformed(ActionEvent e){
	 double frame,scale,xoff,yoff;
	 String text=cp.scalefield.getText();
     if (text.equals("r")){ cp.vw.zoomToRoi(); return;}
     else
     if (text.equals("c")){ cp.vw.jp.offsetx=0; cp.vw.jp.offsety=0;cp.vw.setVisualScale(-1.0f); return;}

	 String[] vals=text.split(",");
	 if (vals.length==1){
		try{
		frame=java.lang.Integer.valueOf(text);
		if (frame<0){
		 cp.vw.jp.offsetx=0;
		 cp.vw.jp.offsety=0;
		 cp.vw.setVisualScale(-1.0f);
		 return;
		}
	    cp.vw.JumpToFrame((int)frame);
	    cp.vw.updateTitle();
	    return;
	    }catch(NumberFormatException ex){
		 cp.scalefield.setText("enter 1 or 4 comma separated values");
		 return;
		}
	 }else
	 if (vals.length!=4){
		cp.scalefield.setText("enter 1 or 4 comma separated values");
		cp.vw.jp.offsetx=0;
		cp.vw.jp.offsety=0;
		cp.vw.setVisualScale(-1.0f);
		return;
	 }
	 try{
		frame=java.lang.Integer.valueOf(vals[0]);
		scale=java.lang.Double.valueOf(vals[1]);
		xoff=java.lang.Double.valueOf(vals[2]);
		yoff=java.lang.Double.valueOf(vals[3]);
		if (frame>=0) cp.vw.JumpToFrame((int)frame);
		cp.vw.jp.offsetx=-1*(int)(xoff*scale);
		cp.vw.jp.offsety=-1*(int)(yoff*scale);
		if ((scale>0.1)&&(scale<12.1))
		 cp.vw.setVisualScale((float)scale);
		cp.scalefield.setText("done");
		cp.vw.updateTitle();

	 }catch(NumberFormatException ex){
		 cp.scalefield.setText("not a number...");
		 cp.vw.jp.offsetx=0;
		 cp.vw.jp.offsety=0;
		 cp.vw.setVisualScale(-1.0f);
	 }
 }

}
*/






















