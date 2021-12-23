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
 Controls cursors in various navigators....
 */


class CursorController extends JPanel
                        implements ActionListener {

    Vector CursorList;
    CursorTableModel CursorModel;

    GView gv;

    boolean DEBUG=true;

    JButton removeChecked;
    JButton removeAll;
    Dimension preferredSize = new Dimension(300,100);

	Vector CursorVector;
	JPanel analysis;
    JTextField xfield,y1field,y2field,y3field;
    JLabel xanswer,y1answer, y2answer, y3answer;
    JTable table;
    int columnCount=0;
	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

    Font font = new Font("Arial", Font.PLAIN, 10);
    int[] xpair; //used to obtain data from cursors
    int[] y1pair;
	int[] y2pair;
	int[] y3pair;

    public CursorController(GView gv) {
	this.gv=gv;
    CursorList=new Vector();
    CursorModel = new CursorTableModel();

    table = new JTable(CursorModel);

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

             analysis=new JPanel();
             analysis.setLayout(new BoxLayout(analysis, BoxLayout.X_AXIS));
             xfield=new JTextField(5);
             xfield.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                   parseColumnName();
               }
            });
             y1field=new JTextField(5);
             y1field.addActionListener(new ActionListener() {
			               public void actionPerformed(ActionEvent e) {
			                parseColumnName();
			                }
            });
            y2field=new JTextField(5);
			 y2field.addActionListener(new ActionListener() {
						   public void actionPerformed(ActionEvent e) {
							parseColumnName();
				}
            });
            y3field=new JTextField(5);
				 y3field.addActionListener(new ActionListener() {
							   public void actionPerformed(ActionEvent e) {
								parseColumnName();
					  }
            });
             xanswer=new JLabel(" x value ");
             y1answer=new JLabel(" y1 value ");
             y2answer=new JLabel(" y2 value ");
             y3answer=new JLabel(" y3 value ");
             analysis.add(xfield);
             analysis.add(xanswer);
             analysis.add(y1field);
             analysis.add(y1answer);
             analysis.add(y2field);
             analysis.add(y2answer);
             analysis.add(y3field);
             analysis.add(y3answer);
             this.add(analysis,BorderLayout.NORTH);







    }

    public void resetColumnCount(){
	 if (columnCount!=CursorModel.getColumnCount()){
	 columnCount=CursorModel.getColumnCount();
	 CursorModel.fireTableStructureChanged();
	 table.setModel(CursorModel);
     System.out.println("changed the column count ="+columnCount);

	 }
	}

    public void parseColumnName(){
	boolean X_USE_TIME=false;
	int xresult=0;
	double time_multiplier=1.5;

    Pattern p=Pattern.compile(",");

	 try{

	  String xline=xfield.getText();
	  String y1line=y1field.getText();
	  String y2line=y2field.getText();
	  String y3line=y3field.getText();

	  String[] xcols=p.split(xline);
	  String[] y1cols=p.split(y1line);
	  String[] y2cols=p.split(y2line);
	  String[] y3cols=p.split(y3line);
	   xpair=new int[xcols.length];
	   y1pair=new int[y1cols.length];
	   y2pair=new int[y2cols.length];
	   y3pair=new int[y3cols.length];
	  if (xcols.length!=2) {
		  X_USE_TIME=true;
		  xresult=Integer.parseInt(xcols[0].trim());
		  }
	  if (!X_USE_TIME){
	  for (int j=0;j<xcols.length;j++){
	   xpair[j]=Integer.parseInt(xcols[j].trim());
	    System.out.println("y "+j+" = "+xpair[j]);
	   }
	   }//special case
	  for (int j=0;j<y1cols.length;j++){
	   	   y1pair[j]=Integer.parseInt(y1cols[j].trim());
	   	   System.out.println("y "+j+" = "+y1pair[j]);
		}
		for (int j=0;j<y2cols.length;j++){
		   y2pair[j]=Integer.parseInt(y2cols[j].trim());
		   System.out.println("y "+j+" = "+y2pair[j]);
		}
		for (int j=0;j<y3cols.length;j++){
		   y3pair[j]=Integer.parseInt(y3cols[j].trim());
		   System.out.println("y "+j+" = "+y3pair[j]);
		}

	   }catch(NumberFormatException e){System.out.println("not a number");}
     //do math?
     try{

      PrintWriter file=new PrintWriter(new FileWriter("out.gnu"),true);

     int[] pos=new int[8];
     double[][] pts=new double[CursorList.size()][4];
     //run through
     int v_offset=0;//vector size offset...

      for (int j=0;j<CursorList.size();j++){//how many cursor lists are there?
       //for first list, there is no offset
     if (!X_USE_TIME){
     pos[0]=((Integer)CursorModel.getValueAt(j,xpair[0]+1)).intValue();
     pos[1]=((Integer)CursorModel.getValueAt(j,xpair[1]+1)).intValue();
     }
     pos[2]=((Integer)CursorModel.getValueAt(j,y1pair[0]+1)).intValue();
     pos[3]=((Integer)CursorModel.getValueAt(j,y1pair[1]+1)).intValue();
     pos[4]=((Integer)CursorModel.getValueAt(j,y2pair[0]+1)).intValue();
	 pos[5]=((Integer)CursorModel.getValueAt(j,y2pair[1]+1)).intValue();
	 pos[6]=((Integer)CursorModel.getValueAt(j,y3pair[0]+1)).intValue();
     pos[7]=((Integer)CursorModel.getValueAt(j,y3pair[1]+1)).intValue();


     if (X_USE_TIME) xresult+=1;
     else xresult=Math.abs(pos[0]-pos[1]);
     int y1result=Math.abs(pos[2]-pos[3]);
     int y2result=Math.abs(pos[4]-pos[5]);
     int y3result=Math.abs(pos[6]-pos[7]);
     System.out.println("j= "+j+" x= "+xresult+" y= "+y1result);
     file.println(xresult+"\t"+y1result+"\t"+Rounding.toString((double)xresult/1.5,2)+"\t"+Rounding.toString((double)y1result/1.5,2)+"\t"+Rounding.toString((double)y2result/1.5,2)+"\t"+Rounding.toString((double)y3result/1.5,2));
     if (X_USE_TIME)pts[j][0]=(double)xresult;
     else pts[j][0]=(double)xresult/time_multiplier;
     pts[j][1]=(double)y1result/time_multiplier;
     pts[j][2]=(double)y2result/time_multiplier;
     pts[j][3]=(double)y3result/time_multiplier;
     v_offset+=((Vector)CursorList.elementAt(j)).size();
     System.out.println("v_offset = "+v_offset);
      }
     file.println(CursorListing());
     file.close();



     //run through the list
     saveCursorList();


     String[] keys=new String[3];
     keys[0]=y1field.getText();
     keys[1]=y2field.getText();
     keys[2]=y3field.getText();
     InternalPtolemyPlot frame=new InternalPtolemyPlot(pts,CursorList.size(),4,keys);

	 			frame.setVisible(true); //necessary as of kestrel
	 	        gv.desktop.add(frame);
	 	        try {
	 	            frame.setSelected(true);
	 	        } catch (java.beans.PropertyVetoException e){};
        }catch(IOException e){e.printStackTrace();}

     }

     public void saveCursorList(){
	  try{
	    PrintWriter file=new PrintWriter(new FileWriter("cursors.txt"),true);
		 for (int i=0;i<CursorList.size();i++){
		  Vector Cursors=(Vector)CursorList.elementAt(i);
			if (Cursors.size()>0){

		     file.print(((gvdecoder.trace.Cursor)Cursors.elementAt(0)).filename+"\t");
		     for (int j=0;j<Cursors.size();j++){
			  gvdecoder.trace.Cursor tmp=(trace.Cursor)Cursors.elementAt(j);
			  file.print(tmp.position+"\t");
			 }
			  file.println("");
		 }

	   }
	  }catch(IOException e){e.printStackTrace();}


	 }
	 public String CursorListing(){
	 	   String output="";

	 	   for (int i=0;i<CursorList.size();i++){
	 		  Vector Cursors=(Vector)CursorList.elementAt(i);
	 			if (Cursors.size()>0){

	 		     output+="# "+((gvdecoder.trace.Cursor)Cursors.elementAt(0)).filename+"\t";
	 		     for (int j=0;j<Cursors.size();j++){
	 			  gvdecoder.trace.Cursor tmp=(gvdecoder.trace.Cursor)Cursors.elementAt(j);
	 			  output+=tmp.position+"\t";
	 			 }
	 			  output+="\n";
	 		 }

	 	   }
	 	 return output;

	 	 }



      public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals("remove")) {
            for (int j=0;j<CursorList.size();j++){
			Vector cursors=(Vector)CursorList.elementAt(j);
			int tmpsize=cursors.size();
			for (int i=0;i<tmpsize;i++){
			  gvdecoder.trace.Cursor tmp=(gvdecoder.trace.Cursor)cursors.elementAt(i);
			  if (!tmp.visible.booleanValue()){
				   cursors.remove(tmp);
			       tmpsize-=1;
			       }
			 }
			}

          }
          if (e.getActionCommand().equals("clear")){
			  System.out.println("removing all cursors");
			  CursorList.removeAllElements();
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

class CursorTableModel extends AbstractTableModel {

        gvdecoder.trace.Cursor[] data;

        public int getColumnCount() {
			int width=0;
			for (int j=0;j<CursorList.size();j++){
			//find longest list
			Vector cursors=(Vector)CursorList.elementAt(j);
			 if (width<cursors.size()) width=cursors.size();
		 }
		 //System.out.println("getColumnCount called, returns "+width+1);
		return (width+1); //extra column for the filename.

        }

        public int getRowCount() {
			 return CursorList.size();
        }

        public String getColumnName(int col) {
            if (col==0) return "file name";
            else return ""+(col-1);
        }

        public Object getValueAt(int row, int col) {
			Object val=null;
			Vector cursors=(Vector)CursorList.elementAt(row);
			//col=0 return filename, col=1 return cursor 0's position etc
			 if (col==0) val=((gvdecoder.trace.Cursor)cursors.elementAt(0)).filename;
			 else{
			  if ((col-1)>=cursors.size()) val="-";
			  else val=((gvdecoder.trace.Cursor)cursors.elementAt(col-1)).position;
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
        public Class getColumnClass(int c) {
		            return getValueAt(0, c).getClass();
		        }


		public void setValueAt(Object value, int row, int col) {


		                fireTableCellUpdated(row, col);
		            }


	 }



}



