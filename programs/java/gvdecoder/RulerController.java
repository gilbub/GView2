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
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellRenderer;



/*
 Displays Ruler information
 */


class RulerController extends JPanel
                        implements ActionListener {

    public Vector RulerList;
    public RulerTableModel RulerModel;
    static Vector data;

    GView gv;

    boolean DEBUG=true;

    JButton refresh;

    Dimension preferredSize = new Dimension(300,100);

	Vector RulerVector;
	JPanel analysis;

    public JTable table;
    int columnCount=0;
	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

    Font font = new Font("Arial", Font.PLAIN, 10);


    public RulerController(GView gv) {
	this.gv=gv;
    RulerList=new Vector();
    RulerModel = new RulerTableModel();

    table = new JTable(RulerModel);
    setUpColorRenderer(table);
    setUpPointRenderer(table);

	 windowSize=this.getSize();



  	this.setLayout(new BorderLayout()); //unnecessary

	         table.setPreferredScrollableViewportSize(new Dimension(500, 70));

	         //Create the scroll pane and add the table to it.
	         JScrollPane scrollPane = new JScrollPane(table);


	         //Add the scroll pane to this window.
	         this.add(scrollPane, BorderLayout.CENTER);

             refresh=new JButton("refresh");
			 refresh.setActionCommand("refresh");
			 refresh.addActionListener(this);



             JPanel bottombar=new JPanel();
             bottombar.add(refresh);
             this.add(bottombar,BorderLayout.SOUTH);
     }

    public void resetColumnCount(){
	 if (columnCount!=RulerModel.getColumnCount()){
	 columnCount=RulerModel.getColumnCount();
	 RulerModel.fireTableStructureChanged();
	 table.setModel(RulerModel);
     System.out.println("changed the column count ="+columnCount);

	 }
	}


     public void saveRulerList(){
	/*  try{
	    PrintWriter file=new PrintWriter(new FileWriter("RulerInfo.txt"),true);
		 for (int i=0;i<RulerList.size();i++){
		  Vector Rulers=(Vector)RulerList.elementAt(i);
			if (Rulers.size()>0){

		     file.print(((Ruler)Rulers.elementAt(0)).vi.filename+"\t");
		     for (int j=0;j<Rulers.size();j++){
			  Ruler tmp=(Ruler)Rulers.elementAt(j);
			  //file.print(tmp.position+"\t");
			 }
			  file.println("");
		 }

	   }
	  }catch(IOException e){e.printStackTrace();}
      */

	 }
	 public String RulerListing(){

	 	String output="";
        /*
	 	   for (int i=0;i<RulerList.size();i++){
	 		  Vector Cursors=(Vector)RulerList.elementAt(i);
	 			if (Rulers.size()>0){

	 		     output+="# "+((Ruler)Rulers.elementAt(0)).vi.filename+"\t";
	 		     for (int j=0;j<Rulers.size();j++){
	 			  Ruler tmp=(Ruler)Cursors.elementAt(j);
	 			  //output+=tmp.position+"\t";
	 			 }
	 			  output+="\n";
	 		 }

	 	   }*/
	 	 return output;

	 	 }



      public void actionPerformed(ActionEvent e) {
              if (e.getActionCommand().equals("refresh")) {
			   data=null;
			   RulerModel.populateData();
			   RulerModel.fireTableStructureChanged();

			   repaint();

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

    class ColorRenderer extends JLabel
	                        implements TableCellRenderer {
	        Border unselectedBorder = null;
	        Border selectedBorder = null;
	        boolean isBordered = true;

	        public ColorRenderer(boolean isBordered) {
	            super();
	            this.isBordered = isBordered;
	            setOpaque(true); //MUST do this for background to show up.
	        }

	        public Component getTableCellRendererComponent(
	                                JTable table, Object color,
	                                boolean isSelected, boolean hasFocus,
	                                int row, int column) {
	            setBackground((Color)color);
	            if (isBordered) {
	                if (isSelected) {
	                    if (selectedBorder == null) {
	                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
	                                                  table.getSelectionBackground());
	                    }
	                    setBorder(selectedBorder);
	                } else {
	                    if (unselectedBorder == null) {
	                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
	                                                  table.getBackground());
	                    }
	                    setBorder(unselectedBorder);
	                }
	            }
	            return this;
	        }
	    }

	    private void setUpColorRenderer(JTable table) {
	        table.setDefaultRenderer(Color.class,
	                                 new ColorRenderer(true));
	    }





class PointRenderer extends JLabel
	                        implements TableCellRenderer {
	        Border unselectedBorder = null;
	        Border selectedBorder = null;
	        boolean isBordered = true;

	        public PointRenderer(boolean isBordered) {
	            super();

	        }

	        public Component getTableCellRendererComponent(
	                                JTable table, Object point,
	                                boolean isSelected, boolean hasFocus,
	                                int row, int column) {
				  java.awt.Point p=(java.awt.Point)point;
	              this.setText(p.x+","+p.y);
	            return this;
	        }
	    }

	    private void setUpPointRenderer(JTable table) {
	        table.setDefaultRenderer(java.awt.Point.class,
	                                 new PointRenderer(true));
	    }





class RulerTableModel extends AbstractTableModel {



		public void populateData(){
		 System.out.println("populating data");
		 if (data==null){

		 data=new Vector();
		//find all Viewer2 objects.
		 JInternalFrame[] frames=gv.desktop.getAllFrames();
		 for (int i=0;i<frames.length;i++){
			 if (frames[i] instanceof Viewer2){
				Viewer2 tmp=(Viewer2)frames[i];
			     for (int j=0;j<tmp.jp.rulers.size();j++){
		 			data.add((Ruler)tmp.jp.rulers.get(j));
				  }
			  }
			  }
		 }

	    	 }





        public int getColumnCount() {
			return 10;


        }

        public int getRowCount() {
			populateData();
			return data.size();
        }

        public String getColumnName(int col) {
        	    if (col==0) return "file name";
       	  else	if (col==1) return "colour";
       	  else if (col==2) return "x1";
       	  else  if (col==3) return "y1";
       	  else  if  (col==4) return "x2";
       	  else  if (col==5) return  "y2";
       	  else  if (col==6) return "distance";
       	  else if (col==7) return "scaled";
       	  else if (col==8) return "degrees";
       	  else if (col==9) return "mass";
       	  return "";
        }

        public Object getValueAt(int row, int col) {
			System.out.println("getting Ruler values");
		    Object val=null;
		    populateData();
		    Ruler ruler=(Ruler)data.elementAt(row);
		     if (col==0) val=ruler.vi.filename;
		else if (col==1) val= ruler.color;
		else if (col==2) val= new Integer(ruler.firstX);
		else if (col==3) val= new Integer(ruler.firstY);
        else if (col==4) val= new Integer(ruler.lastX);
        else if (col==5) val= new Integer(ruler.lastY);
        else if (col==6) val= new Double(ruler.distance);
        else if (col==7) val= new Double(ruler.scaleddistance);
        else if (col==8) val= new Integer(ruler.degrees);
        else if (col==9) val= new Double(ruler.mass);

		    return val;
		}




        public Class getColumnClass(int c) {
           return getValueAt(0, c).getClass();
        }


        public boolean isCellEditable(int row, int col) {
             return true;
        }


		public void setValueAt(Object value, int row, int col) {
           Ruler ruler=(Ruler)data.elementAt(row);
           int val=((Integer)value).intValue();
           if ((val>=0) && (val<Math.max(ruler.vi.X_dim,ruler.vi.Y_dim))){
    	     if (col==2) ruler.firstX=((Integer)value).intValue();
		     else if (col==3) ruler.firstY=((Integer)value).intValue();
             else if (col==4)ruler.lastX=((Integer)value).intValue();
             else if (col==5) ruler.lastY=((Integer)value).intValue();
             ruler.updateResults();
		 }
		     fireTableCellUpdated(row, col);
		       }


	 }







}