//package filters;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ControlPanel extends JPanel{

Viewer2 vw;
JPanel contents;
JList list;

public ControlPanel(Viewer2 vw){
  
 this.vw=vw;
 contents=new JPanel();
 contents.setLayout(new GridLayout(0,1));
 
this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

ImageReaderListener imageReaderListener=new ImageReaderListener(this);
JButton Background=new JButton("background");
contents.add(Background);
Background.addActionListener(imageReaderListener);


FilterListener filterListener=new FilterListener(this); 
JButton RemoveAllFilters=new JButton("clear");
contents.add(RemoveAllFilters);
RemoveAllFilters.addActionListener(filterListener);

 
JButton RemoveFilter=new JButton("remove");
contents.add(RemoveFilter);
RemoveFilter.addActionListener(filterListener);

JButton AddMedianFilter=new JButton("median");
contents.add(AddMedianFilter);
AddMedianFilter.addActionListener(filterListener);

JButton AddConvolveFilter=new JButton("convolve");
contents.add(AddConvolveFilter);
AddConvolveFilter.addActionListener(filterListener);

JButton AddLookupFilter=new JButton("lookup");
contents.add(AddLookupFilter);
AddLookupFilter.addActionListener(filterListener);

JButton AddSubtractFilter=new JButton("subtract");
contents.add(AddSubtractFilter);
AddSubtractFilter.addActionListener(filterListener);

list=new JList(vw.jp.myfilt);

list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
JScrollPane listScrollPane = new JScrollPane(list);

JScrollPane scroll=new JScrollPane(contents);
this.add(scroll);
this.add(listScrollPane);

 

 
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
 ControlPanel cp;
 
 public FilterListener(ControlPanel cp){
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
		
		if (source.getText().equals("median")){
				 cp.vw.jp.myfilt.addFilter("median");
		}
		if (source.getText().equals("convolve")){
				 cp.vw.jp.myfilt.addFilter("convolve");
		}
		if (source.getText().equals("lookup")){
								 cp.vw.jp.myfilt.addFilter("lookup");
		}
		if (source.getText().equals("subtract")){
										 cp.vw.jp.myfilt.addFilter("subtract");
		}
	}

}

class ImageReaderListener implements ActionListener{
 ControlPanel cp;
 
 public ImageReaderListener(ControlPanel cp){
  this.cp=cp;
  }
  
 //gets popup events
 public void actionPerformed(ActionEvent e) {
        JButton source = (JButton)(e.getSource());
        String s = "Control Panel Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
		if (source.getText().equals("background")){
		 cp.vw.im.FilterOperation(0,0,0,cp.vw.instance);
		}
		 
	}

}
  






















