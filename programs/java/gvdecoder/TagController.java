package gvdecoder;
/*
 * Swing version.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;
import java.util.regex.*;
import gvdecoder.trace.*;


/*
 Controls Tags in various navigators....
 */


class TagController extends JPanel
                        implements ActionListener {

    Vector TagList;
    Vector AllTags;
    TagTableModel TagModel;

    GView gv;

    boolean DEBUG=true;

    JButton removeChecked;
    JButton removeAll;
    Dimension preferredSize = new Dimension(300,100);

	Vector TagVector;
	JPanel analysis;
    JTable table;
    int columnCount=0;
	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

    Font font = new Font("Arial", Font.PLAIN, 10);

    public TagController(GView gv) {
	this.gv=gv;
    TagList=new Vector();
    TagModel = new TagTableModel();

    table = new JTable(TagModel);

	 windowSize=this.getSize();



  	this.setLayout(new BorderLayout()); //unnecessary

	         table.setPreferredScrollableViewportSize(new Dimension(500, 70));

	         //Create the scroll pane and add the table to it.
	         JScrollPane scrollPane = new JScrollPane(table);


	         //Add the scroll pane to this window.
	         this.add(scrollPane, BorderLayout.CENTER);

             removeChecked=new JButton("remove");
			 removeChecked.setActionCommand("remove");
			 removeChecked.addActionListener(this);

			 removeAll=new JButton("clear");
			 removeAll.setActionCommand("clear");
			 removeAll.addActionListener(this);

             JPanel bottombar=new JPanel();
             bottombar.add(removeChecked);
             bottombar.add(removeAll);
             this.add(bottombar,BorderLayout.SOUTH);
    }

    public void resetColumnCount(){
	 if (columnCount!=TagModel.getColumnCount()){
	 columnCount=TagModel.getColumnCount();
	 TagModel.fireTableStructureChanged();
	 table.setModel(TagModel);
     System.out.println("changed the column count ="+columnCount);

	 }
	}


   public int[] runDetection(int tracenumber){
     Vector results=new Vector();
     int[] res;
     Vector tagvector=(Vector)TagList.elementAt(tracenumber);
     Trace trace=(Trace)tagvector.elementAt(0);
     //run through each tag and set mode to detect.
     for (int k=1;k<tagvector.size();k++){
		 DetectorTag dt=(DetectorTag)tagvector.elementAt(k);
		  dt.setMode(1);
	  }
     for (int i=1;i<trace.Size();i++){
	 		 boolean trigged=true;
	 		for (int j=1;j<tagvector.size();j++){


	 			trigged=((DetectorTag)tagvector.elementAt(j)).check(i);
	 			if (!trigged) break;
	 		 }
	 		 if (trigged){
	 			 results.addElement(new Integer(i));
	 			 System.out.println("found trigger at "+i);
	 			 i+=10;
	 		}
	   }

    res=new int[results.size()];
    for (int l=0;l<res.length;l++) res[l]=((Integer)results.elementAt(l)).intValue();
	//run through and reset mode to select
	for (int k=1;k<tagvector.size();k++){
			 DetectorTag dt=(DetectorTag)tagvector.elementAt(k);
			  dt.setMode(0);
	  }

	return res;

   }


    public void register(Vector tagvector, Trace referenceTrace){
		if (TagList==null) TagList=new Vector();
		if (AllTags==null) AllTags=new Vector();
		if (!TagList.contains(tagvector)) TagList.add(tagvector);
		for (int i=0;i<tagvector.size();i++){
			DetectorTag td=(DetectorTag)tagvector.elementAt(i);
			if (!AllTags.contains(td)) AllTags.add(td);
		}
	}

	public void remove(DetectorTag td){
	  if (AllTags==null){
		  AllTags.remove(td);
	  }

	}

	 public String TagListing(){
	 	   String output="";

	 	   for (int i=0;i<TagList.size();i++){
	 		  Vector Tags=(Vector)TagList.elementAt(i);
	 			if (Tags.size()>0){

	 		     output+="# "+((DetectorTag)Tags.elementAt(0)).filename+"\t";
	 		     for (int j=0;j<Tags.size();j++){
	 			  DetectorTag tmp=(DetectorTag)Tags.elementAt(j);
	 			  output+=tmp.position+"\t";
	 			 }
	 			  output+="\n";
	 		 }

	 	   }
	 	 return output;

	 	 }



      public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals("remove")) {
            for (int j=0;j<TagList.size();j++){
			Vector Tags=(Vector)TagList.elementAt(j);
			int tmpsize=Tags.size();
			for (int i=0;i<tmpsize;i++){
			  DetectorTag tmp=(DetectorTag)Tags.elementAt(i);
			  if (!tmp.visible.booleanValue()){
				   Tags.remove(tmp);
			       tmpsize-=1;
			       }
			 }
			}

          }
          if (e.getActionCommand().equals("clear")){
			  System.out.println("removing all Tags");
			  TagList.removeAllElements();
		  }
         }

    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {

    }

    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {

    }



    public Dimension getPreferredSize() {
        return preferredSize;
    }

class TagTableModel extends AbstractTableModel {

        DetectorTag[] data;

        public int getColumnCount() {
			 return 9;

        }

        public int getRowCount() {
            if (AllTags!=null) return AllTags.size();
            else return 1;
        }

        public String getColumnName(int col) {
            if (col==0) return "file name";
            else if (col==1) return "position";
            else if (col==2) return "value";
            else if (col==3) return "minval";
            else if (col==4) return "maxval";
            else if (col==5) return "relative";
            else if (col==6) return "basename";
            else if (col==7) return "dif";
            else if (col==8) return "triggered";
            return "";
        }

        public Object getValueAt(int row, int col) {
			Object val=null;
			if (AllTags!=null){
			 DetectorTag td=(DetectorTag)AllTags.elementAt(row);
			 if (col==0) val=td.filename;
			 else if (col==1) val=td.position;
			 else if (col==2) val=td.yVal;
			 else if (col==3) val=td.maxVal;
			 else if (col==4) val=td.minVal;
			 else if (col==5) val=td.relative;
			 else if (col==6) val=td.basename;
			 else if (col==7) val=td.difference;
			 else if (col==8) val=td.AssertedTrigger;
		    }
			 return val;
			}




        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        //public Class getColumnClass(int c) {
        //    return getValueAt(0, c).getClass();
        //}

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
             return true;
        }
        //public Class getColumnClass(int c) {
		//            return getValueAt(0, c).getClass();
		//        }


		public void setValueAt(Object value, int row, int col) {


		                fireTableCellUpdated(row, col);
		            }


	 }



}



