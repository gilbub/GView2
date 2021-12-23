package gvdecoder;
/*
 * Swing version.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;
import java.util.regex.*;
import gvdecoder.trace.*;

import gvdecoder.utilities.*;


/*
 Controls open image files and navigator windows
 */


class ImageFileController extends JPanel
                        implements ActionListener, ImageViewer {


    ImageFileManager ImageManager;

    ImageFileTableModel ImageFileModel;

    GView gv;

    boolean DEBUG=true;

    ImageFileRecord[] SelectedRecords;

    JButton openChecked;
    JButton closeChecked;
    JButton playChecked;
    Dimension preferredSize = new Dimension(600,300);

    boolean ALLOW_ROW_SELECTION=true;
    boolean ALLOW_COLUMN_SELECTION=false;

	JPanel analysis;
    JTable table;
    int columnCount=0;
	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

    Font font = new Font("Arial", Font.PLAIN, 10);

    public ImageFileController(String imagepath,GView gv) {
	this.gv=gv;
    ImageManager=new CWRUExplogDecoder(); //send this to some factory, next todo CHANGE
    ImageManager.ReadArrayList(imagepath);


    ImageFileModel = new ImageFileTableModel();

    table = new JTable(ImageFileModel);

	 windowSize=this.getSize();



  	this.setLayout(new BorderLayout()); //unnecessary

	   table.setPreferredScrollableViewportSize(new Dimension(700, 200));

	         //Create the scroll pane and add the table to it.
	         JScrollPane scrollPane = new JScrollPane(table);


	         //Add the scroll pane to this window.
	         this.add(scrollPane, BorderLayout.CENTER);

             openChecked=new JButton("open");
			 openChecked.setActionCommand("open");
			 openChecked.addActionListener(this);

			 closeChecked=new JButton("close");
			 closeChecked.setActionCommand("close");
			 closeChecked.addActionListener(this);

             JPanel toolBar = new MediaButtons(this,true,true,true,true,true);

		     toolBar.setAlignmentY(Component.TOP_ALIGNMENT);


			 DragControl frameSelect=new DragControl(Color.magenta,0,2800,10,true,1,"###","frame");
             frameSelect.addActionListener(new FrameNumberListener(this));

             JPanel topbar=new JPanel();

             topbar.add(openChecked);

             topbar.add(closeChecked);
             topbar.add(toolBar);
             topbar.add(frameSelect);

             this.add(topbar,BorderLayout.SOUTH);

    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
	        if (ALLOW_ROW_SELECTION) { // true by default
	            ListSelectionModel rowSM = table.getSelectionModel();
	            rowSM.addListSelectionListener(new ListSelectionListener() {
	                public void valueChanged(ListSelectionEvent e) {
	                    //Ignore extra messages.
	                    if (e.getValueIsAdjusting()) return;

	                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	                    if (lsm.isSelectionEmpty()) {
	                        System.out.println("No rows are selected.");
	                    } else {
	                        int selectedRow = lsm.getMinSelectionIndex();
	                        int selectedMax = lsm.getMaxSelectionIndex();
	                        System.out.println("Row " + selectedRow+"-"+selectedMax+
	                                            " is now selected.");
	                        SelectedRecords=ImageManager.ReturnSelectedFileNames(selectedRow,selectedMax);
	                        BringSelectedToFront();
	                        //OpenSelectedImageFiles();
	                        for (int i=0;i<SelectedRecords.length;i++){
								System.out.println(SelectedRecords[i].absolutepath);
							}

	                    }
	                }
	            });
	        } else {
	            table.setRowSelectionAllowed(false);
	        }


      TableColumn column=table.getColumnModel().getColumn(9);
       column.setPreferredWidth(400);
       column=table.getColumnModel().getColumn(8);
	   column.setPreferredWidth(15);
	   column=table.getColumnModel().getColumn(1);
	   column.setPreferredWidth(15);
	   column=table.getColumnModel().getColumn(0);
	   column.setPreferredWidth(55);
	   for (int i=2;i<8;i++){
		   column=table.getColumnModel().getColumn(i);
		   column.setPreferredWidth(30);
	   }

   table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    }

    /**ImageViewer Interface**/
    public void Start(){
    Viewer2 vw=null;
   	try{
   	 for (int i=0;i<SelectedRecords.length;i++){
   	  if (SelectedRecords[i].viewerwindow!=null){
   	        vw=(Viewer2) SelectedRecords[i].viewerwindow;
            vw.frozen=false;
            vw.startAnimation();
            vw.findScaleOffset();
       }
       }
       }catch(Exception e){e.printStackTrace();}
	}
	public void Stop(){
	 Viewer2 vw=null;
	    	try{
	    	 for (int i=0;i<SelectedRecords.length;i++){
	    	  if (SelectedRecords[i].viewerwindow!=null){
	    	        vw=(Viewer2) SelectedRecords[i].viewerwindow;
	             vw.frozen=true;
	             vw.stopAnimation();
	        }
	        }
       }catch(Exception e){e.printStackTrace();}
	}
	public void Rewind(){
	 Viewer2 vw=null;
   	try{
   	 for (int i=0;i<SelectedRecords.length;i++){
   	  if (SelectedRecords[i].viewerwindow!=null){
   	        vw=(Viewer2) SelectedRecords[i].viewerwindow;
            vw.JumpToFrame(vw.LastUserSetFrame);

       }
       }
       }catch(Exception e){e.printStackTrace();}

	}

	public void AdvanceOneFrame(){
	  Viewer2 vw=null;
	    	try{
	    	 for (int i=0;i<SelectedRecords.length;i++){
	    	  if (SelectedRecords[i].viewerwindow!=null){
	    	        vw=(Viewer2) SelectedRecords[i].viewerwindow;
	                vw.AdvanceOneFrame();

	        }
	        }
       }catch(Exception e){e.printStackTrace();}

	}

	public void BackOneFrame(){
	  Viewer2 vw=null;
	    	try{
	    	 for (int i=0;i<SelectedRecords.length;i++){
	    	  if (SelectedRecords[i].viewerwindow!=null){
	    	        vw=(Viewer2) SelectedRecords[i].viewerwindow;
	                vw.BackOneFrame();

	        }
	        }
       }catch(Exception e){e.printStackTrace();}

}

    public void BringSelectedToFront(){
     for (int i=0;i<SelectedRecords.length;i++){
	  JInternalFrame iff=SelectedRecords[i].viewerwindow;
	  JInternalFrame nff=SelectedRecords[i].navwindow;
      try{
      if (iff!=null){
		    if (iff.isIcon())
		       iff.setIcon(false);
			 iff.toFront();
			 iff.setSelected(true);
		  }
		if (nff!=null){
			  if (nff.isIcon())
		       nff.setIcon(false);
			 nff.toFront();
			 nff.setSelected(true);
	      }
	   }catch(java.beans.PropertyVetoException e){}
     }
	}

    public void OpenSelectedNavFiles(){


	}


    public void OpenSelectedImageFiles(){

	for (int i=0;i<SelectedRecords.length;i++){
	  if (SelectedRecords[i].viewerwindow==null){
	  File tmp=new File(SelectedRecords[i].absolutepath);
	  Viewer2 tmpv=new Viewer2(gv,15,tmp.getName(),SelectedRecords[i].absolutepath ,ImageManager.GetFileType());
	  SelectedRecords[i].viewerwindow=(JInternalFrame)tmpv;
	  SelectedRecords[i].viewerwindow.setVisible(true);

	  tmpv.JumpToFrame(tmpv.vp.InitialFrame);
	  gv.desktop.add(SelectedRecords[i].viewerwindow);
	  try{
	  SelectedRecords[i].viewerwindow.toFront();
	  SelectedRecords[i].viewerwindow.setSelected(true);
      }catch(java.beans.PropertyVetoException e){}
	  tmpv.JumpToFrame(tmpv.vp.InitialFrame);

	 }
     PlaceViewerWindows();
     BringSelectedToFront();
	}
   }


   public void CloseSelectedImageFiles(){

	try{
	 for (int i=0;i<SelectedRecords.length;i++){
	  if (SelectedRecords[i].viewerwindow!=null){
	     SelectedRecords[i].viewerwindow.setClosed(true);
         SelectedRecords[i].viewerwindow=null;
    }
    }
    }catch(java.beans.PropertyVetoException e){}
   }


   public void PlaySelectedImageFiles(){
    Viewer2 vw=null;
   	try{
   	 for (int i=0;i<SelectedRecords.length;i++){
   	  if (SelectedRecords[i].viewerwindow!=null){
   	        vw=(Viewer2) SelectedRecords[i].viewerwindow;
            vw.frozen=false;
            vw.startAnimation();
            vw.findScaleOffset();
       }
       }
       }catch(Exception e){e.printStackTrace();}
   }

   public void SetFrameNumber(int num){
	  Viewer2 vw=null;
   	try{
	 if (SelectedRecords!=null){
   	 for (int i=0;i<SelectedRecords.length;i++){
   	  if (SelectedRecords[i].viewerwindow!=null){
   	        vw=(Viewer2) SelectedRecords[i].viewerwindow;
            vw.JumpToFrame(num);
            vw.findScaleOffset();
       }
       }
       }
       }catch(Exception e){e.printStackTrace();}
   }




   public void PlaceViewerWindows(){
    if (SelectedRecords.length<10){
	 //dump in 3x3
	 for (int i=0;i<SelectedRecords.length;i++){
	  if (SelectedRecords[i].viewerwindow!=null){
	   if (i<3)
	    SelectedRecords[i].viewerwindow.setLocation(i*200,10);
	   else if (i<6)
	    SelectedRecords[i].viewerwindow.setLocation(i*200-600,210);
	   else
	    SelectedRecords[i].viewerwindow.setLocation(i*200-1200,410);
      }
	 }
	}
	 else
	 {//stagger
	  int offset=100;
	  for (int i=0;i<SelectedRecords.length;i++){
		if (SelectedRecords[i].viewerwindow!=null){
		 SelectedRecords[i].viewerwindow.setLocation(offset,offset+i*20);
		 SelectedRecords[i].viewerwindow.moveToFront();
	 }
    }
   }
  }
    public void resetColumnCount(){
	 if (columnCount!=ImageFileModel.getColumnCount()){
	 columnCount=ImageFileModel.getColumnCount();
	 ImageFileModel.fireTableStructureChanged();
	 table.setModel(ImageFileModel);
     System.out.println("changed the column count ="+columnCount);

	 }
	}


      public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals("open")) {
             OpenSelectedImageFiles();

          }
          else if (e.getActionCommand().equals("close")){
			 CloseSelectedImageFiles();
         }
         else if (e.getActionCommand().equals("play")){
		 			 PlaySelectedImageFiles();
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

class ImageFileTableModel extends AbstractTableModel {

        public Class getColumnClass(int c){
			 return getValueAt(0,c).getClass();
		 }

        public int getColumnCount() {
			return ImageManager.GetRecordNames().length;

        }

        public int getRowCount() {
           return ImageManager.GetRowCount();
        }

        public String getColumnName(int col) {
          String[] s=ImageManager.GetRecordNames();
          return s[col];
        }

        public Object getValueAt(int row, int col) {
			 return ImageManager.GetRecordValue(row,col);
			}





        public boolean isCellEditable(int row, int col) {
             return false;
        }



		public void setValueAt(Object value, int row, int col) {


		                fireTableCellUpdated(row, col);
		            }


	 }



}


class FrameNumberListener implements ActionListener {

ImageFileController pp;

public FrameNumberListener(ImageFileController pp){
 this.pp=pp;
}
    public void actionPerformed(ActionEvent e) {
        DragControl source = (DragControl)e.getSource();

	    int frame = (int)source.getValue();

	       pp.SetFrameNumber(frame);
	    }
        }


