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
 Controls cursors in various navigators....
 */


class ROIController extends JPanel
                        implements ActionListener {

    Vector ROIList;
    ROITableModel ROIModel;
    static Vector data;

    GView gv;

    boolean DEBUG=true;

    JButton refresh;

    Dimension preferredSize = new Dimension(300,100);

	Vector ROIVector;
	JPanel analysis;

    JTable table;
    int columnCount=0;
	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

    Font font = new Font("Arial", Font.PLAIN, 10);


    public ROIController(GView gv) {
	this.gv=gv;
    ROIList=new Vector();
    ROIModel = new ROITableModel();

    table = new JTable(ROIModel);
    setUpColorRenderer(table);


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
	 if (columnCount!=ROIModel.getColumnCount()){
	 columnCount=ROIModel.getColumnCount();
	 ROIModel.fireTableStructureChanged();
	 table.setModel(ROIModel);
     System.out.println("changed the column count ="+columnCount);

	 }
	}


     public void saveROIList(){
	/*  try{
	    PrintWriter file=new PrintWriter(new FileWriter("ROIInfo.txt"),true);
		 for (int i=0;i<ROIList.size();i++){
		  Vector ROIs=(Vector)ROIList.elementAt(i);
			if (ROIs.size()>0){

		     file.print(((ROI)ROIs.elementAt(0)).vi.filename+"\t");
		     for (int j=0;j<ROIs.size();j++){
			  ROI tmp=(ROI)ROIs.elementAt(j);
			  //file.print(tmp.position+"\t");
			 }
			  file.println("");
		 }

	   }
	  }catch(IOException e){e.printStackTrace();}
      */

	 }
	 public String ROIListing(){

	 	String output="";
        /*
	 	   for (int i=0;i<ROIList.size();i++){
	 		  Vector Cursors=(Vector)ROIList.elementAt(i);
	 			if (ROIs.size()>0){

	 		     output+="# "+((ROI)ROIs.elementAt(0)).vi.filename+"\t";
	 		     for (int j=0;j<ROIs.size();j++){
	 			  ROI tmp=(ROI)Cursors.elementAt(j);
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
			   ROIModel.populateData();
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


class ROITableModel extends AbstractTableModel {



		public void populateData(){
		 System.out.println("populating data");
		 if (data==null){

		 data=new Vector();
		//find all Viewer2 objects.
		 JInternalFrame[] frames=gv.desktop.getAllFrames();
		 for (int i=0;i<frames.length;i++){
			 if (frames[i] instanceof Viewer2){
				Viewer2 tmp=(Viewer2)frames[i];
			     for (int j=0;j<tmp.jp.rois.size();j++){
		 			data.add((ROI)tmp.jp.rois.get(j));
				  }
			  }
			  }
		 }

	    	 }





        public int getColumnCount() {
			return 5;


        }

        public int getRowCount() {
			populateData();
			return data.size();
        }

        public String getColumnName(int col) {
        	    if (col==0) return "file name";
       	  else	if (col==1) return "colour";
       	  else  if (col==2) return "pixels";
       	  else  if  (col==3) return "value";
       	  else  if (col==4) return  "average";
          return "";
        }

        public Object getValueAt(int row, int col) {
			System.out.println("getting roi values");
		    Object val=null;
		    populateData();
		    ROI roi=(ROI)data.elementAt(row);
		     if (col==0) val=roi.vi.filename;
		else if (col==1) val= roi.color;
		else if (col==2) val= new Integer(roi.findAllPixels(roi.vi.X_dim, roi.vi.Y_dim));
		else if (col==3) val= new Integer(roi.sumAllPixels(roi.vi.X_dim, roi.vi.Y_dim));
        else if (col==4) val= new Double(roi.getAverageVal(roi.vi.X_dim, roi.vi.Y_dim));



		    return val;
		}




        public Class getColumnClass(int c) {
           return getValueAt(0, c).getClass();
        }


        public boolean isCellEditable(int row, int col) {
             return true;
        }


		public void setValueAt(Object value, int row, int col) {


		                fireTableCellUpdated(row, col);
		            }


	 }






}
