package gvdecoder;
/*
 * Swing version.
 */
import org.python.core.*;

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
import javax.swing.table.*;



/*
 Displays ROI information
 */


class JythonTable extends JPanel
                        implements ActionListener, HtmlSaver{


    AbstractTableModel TableModel;

    public PyDictionary dict;
    public  PyList list;
    public Matrix ma;
    public Viewer2 vw;
    public double[][] doubles;
    public TimeSeries[] ts;

    public int maz=0; // can only display a 2D array of data, so user must set the frame of interest.

    public PyList columnnames;

	public String title="";

	public JythonTableFrame jtf=null;

    GView gv;

    boolean DEBUG=true;

    JButton refresh;
    JCheckBox resize;
    JComboBox zlist;
    JPanel popuppanel; //=new JPanel();

    Dimension preferredSize = new Dimension(300,100);


	JPanel analysis;

    JTable table;
    int firstselected;
    int lastselected;
    int columnCount=0;
	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

    Font font = new Font("Arial", Font.PLAIN, 10);

    public boolean resize_cells=true;
    public boolean has_zdim=false;

	public int max_html_cols=20;
	public int max_html_rows=20;
	public String saveHtml(){
		int cols=TableModel.getColumnCount();
		int rows=TableModel.getRowCount();
		if (cols>max_html_cols) cols=max_html_cols;
		if (rows>max_html_rows) rows=max_html_rows;
		String res="</pre><table border='1' cellspacing='0'>";
		//headers
		res+="\n<tr>";
		for (int i=0;i<cols;i++){
			res+="<th>"+TableModel.getColumnName(i);
		}
		for (int j=0;j<rows;j++){
		 res+="\n<tr>";
		  for (int k=0;k<cols;k++){
			  res+="<td>"+TableModel.getValueAt(j,k).toString();
		  }
		}
		res+="\n</table><pre>";
	  	return res;
	}

	public void SetupWindow(){
		JPanel editorpanel=new JPanel();
		JPanel bottombar=new JPanel();
		windowSize=this.getSize();

		this.setLayout(new BorderLayout()); //unnecessary

		table.setPreferredScrollableViewportSize(new Dimension(500, 200));

		SelectionListener listener = new SelectionListener(this);
		table.getSelectionModel().addListSelectionListener(listener);
		table.getColumnModel().getSelectionModel().addListSelectionListener(listener);

		 //Create the scroll pane and add the table to it.
		 JScrollPane scrollPane = new JScrollPane(table);


		 //Add the scroll pane to this window.
		 this.add(scrollPane, BorderLayout.CENTER);

		 refresh=new JButton("refresh");
		 refresh.setActionCommand("refresh");
		 refresh.addActionListener(this);

         resize = new JCheckBox("resize cells");
         resize.setActionCommand("resize");
         resize.addActionListener(this);
         resize.setSelected(resize_cells);


		 bottombar.add(refresh);
		 bottombar.add(resize);

         if (has_zdim){
			 if (ma!=null){

				 DefaultComboBoxModel zmodel = new DefaultComboBoxModel() {
				      public int getSize() {return ma.zdim; }
				      public Object getElementAt(int index) { return new Integer(index); }
                 };
				 zlist=new JComboBox(zmodel);
				 zlist.setActionCommand("zlist");
				 zlist.addActionListener(this);
				 //zlist.setPreferredScrollableViewportSize(new Dimension(20,30));
				 //JScrollPane listpane = new JScrollPane(zlist);
				 // popuppanel=new JPanel();
				 // popuppanel.add(zlist);
				 bottombar.add(new JLabel("z="));
				 bottombar.add(zlist);
				 //popup=new JPopupMenu();
				 //popup.add(pp);
				 //scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, pp);
			 }
		 }


		 this.add(bottombar,BorderLayout.SOUTH);

	}

	public void setTitle(String title){
		this.title=title;
		if (jtf!=null){
			jtf.setTitle(title);
		}
	}

	public JythonTable(GView gv, PyDictionary dict) {
			this.gv=gv;
			this.dict=dict;

			TableModel = new JythonDictionaryTableModel();

			table = new JTable(TableModel);
			if (table.getColumnCount()>10){
				resize_cells=false;
				table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
			   }else{
				 table.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS );
			   }
	        SetupWindow();

	     }

	public JythonTable(GView gv, double[][] arr) {
			this.gv=gv;
			this.doubles=arr;

			TableModel = new JythonDoublesTableModel();

			table = new JTable(TableModel);
			if (table.getColumnCount()>10){
				resize_cells=false;
				table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
			   }else{
				 table.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS );
			   }
	        SetupWindow();

	     }


 public JythonTable(GView gv, TimeSeries[] arr) {
 			this.gv=gv;
 			this.ts=arr;

 			TableModel = new JythonTimeSeriesTableModel();

 			table = new JTable(TableModel);
 			if (table.getColumnCount()>10){
 				resize_cells=false;
 				table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
 			   }else{
 				 table.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS );
 			   }
 	        SetupWindow();

 	     }


	public JythonTable(GView gv, Matrix ma) {
			this.gv=gv;
			this.ma=ma;

			has_zdim=true;

			TableModel = new JythonMatrixTableModel();

			table = new JTable(TableModel);
			table.getColumnModel().getColumn(0).setCellRenderer( table.getTableHeader().getDefaultRenderer() );
		    table.getColumnModel().getColumn(0).setPreferredWidth(50);
			if (table.getColumnCount()>10){
				resize_cells=false;
				table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
			   }else{
				 table.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS );
			   }
	        SetupWindow();

	     }



    public JythonTable(GView gv, Viewer2 vw) {
		this.gv=gv;
		this.vw=vw;

		TableModel = new ViewerTableModel();

		table = new JTable(TableModel);
		table.getColumnModel().getColumn(0).setCellRenderer( table.getTableHeader().getDefaultRenderer() );
	    table.getColumnModel().getColumn(0).setPreferredWidth(50);
		if (table.getColumnCount()>10){
			resize_cells=false;
			table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		   }else{
			 table.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS );
		   }
        SetupWindow();

     }

     public JythonTable(GView gv, PyList list){
		 this.gv=gv;
		 this.list=list;
		 TableModel=new JythonListTableModel();
		 table=new JTable(TableModel);
		 if (table.getColumnCount()>10){
		 	 resize_cells=false;
		 	 table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		   } else{
			   table.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS );
		   }
		 SetupWindow();

	 }

    public void resetColumnCount(){
	 if (columnCount!=TableModel.getColumnCount()){
	 columnCount=TableModel.getColumnCount();
	 TableModel.fireTableStructureChanged();
	 table.setModel(TableModel);
     System.out.println("changed the column count ="+columnCount);

	 }
	}


    public void setColumnNames(PyList columnnames){
		this.columnnames=columnnames;
		resetColumnCount();
	}

	public void setMatrixFrame(int n){
	     maz=n;

	     TableModel.fireTableStructureChanged();
	     table.getColumnModel().getColumn(0).setCellRenderer( table.getTableHeader().getDefaultRenderer() );
		 table.getColumnModel().getColumn(0).setPreferredWidth(50);
	}





    public void actionPerformed(ActionEvent e) {
              if (e.getActionCommand().equals("refresh")) {

			   TableModel.fireTableStructureChanged();
			   repaint();
			 }else
			 if (e.getActionCommand().equals("resize")){
				 if (resize.isSelected()){
					  table.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS );
				 }else{
					  table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
				 }
			 }else
			 if (e.getActionCommand().equals("zlist")){
				 if (ma!=null){

					 setMatrixFrame(((Integer)zlist.getSelectedItem()).intValue());
				 }

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


class ViewerTableModel extends AbstractTableModel {
		public int getColumnCount() {
			 return vw.X_dim+1;
		}

		public int getRowCount() {
		  return vw.Y_dim;
		 }

		public String getColumnName(int col) {
		   if (col==0) return "";
		   else return ""+(col-1);
		  }

		public Object getValueAt(int row, int col) {
				if (col==0) return new java.lang.Integer(row);
				else
				return new java.lang.Integer(vw.datArray[(row)*vw.X_dim+(col-1)]);
		}


		public Class getColumnClass(int c) {
		   return getValueAt(0, c).getClass();
		}


		public boolean isCellEditable(int row, int col) {
			 return false;
		}


		public void setValueAt(Object value, int row, int col) {

						fireTableCellUpdated(row, col);
					}

}


class JythonDictionaryTableModel extends AbstractTableModel {
		public int getColumnCount() {
			if (columnnames==null){
			 return dict.keys().__len__();
		    }
			else
			 return (columnnames.__len__());
		}

		public int getRowCount() {
		  PyList keys=(PyList)dict.keys();
		  return ((PyList)dict.get(keys.__getitem__(0))).__len__();
		 }

		public String getColumnName(int col) {
		   if (columnnames==null){
			return ((PyList)dict.keys()).__getitem__(col).toString();
		   } else
		   return columnnames.__getitem__(col).toString();
		  }

		public Object getValueAt(int row, int col) {
			if (columnnames==null){
				return ((PyList)dict.get(dict.keys().__getitem__(col))).__getitem__(row);
				//return ((PyList)(list.__getitem__(col))).__getitem__(row);
			}else{
				return ((PyList)dict.get(columnnames.__getitem__(col))).__getitem__(row);

			}
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


class JythonListTableModel extends AbstractTableModel {
		public int getColumnCount() {
			if (columnnames==null)
			 return list.__len__();
			else
			 return (columnnames.__len__());
		}

		public int getRowCount() {

			 PyList col=(PyList)list.__getitem__(0);
			 return col.__len__();
		}

		public String getColumnName(int col) {
		   return ""+col;
		 }

		public Object getValueAt(int row, int col) {
			if (columnnames==null){
				return ((PyList)(list.__getitem__(col))).__getitem__(row);
			}else{
				int index=((columnnames.__getitem__(col)).__int__()).getValue();
				return ((PyList)(list.__getitem__(index))).__getitem__(row);

			}
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

class JythonDoublesTableModel extends AbstractTableModel {
	public int getColumnCount(){
		return doubles.length+1;
	}

	public int getRowCount(){
		return doubles[0].length;
	}


	public String getColumnName(int col) {
		    if (col==0) return "index";
			else return ""+(col-1);

         }

    public Object getValueAt(int row, int col) {
		    if (col==0) return new Integer(row);
		    else
			 return new Double(doubles[col-1][row]);
		}

   public Class getColumnClass(int c) {
           return getValueAt(0, c).getClass();
        }


   public boolean isCellEditable(int row, int col) {
	         if (col==0) return false;
	         else
              return true;
        }


   public void setValueAt(Object value, int row, int col) {

						doubles[col-1][row]=((Double)value).doubleValue();
		                fireTableCellUpdated(row, col);
		            }


}



class JythonTimeSeriesTableModel extends AbstractTableModel {
	public int getColumnCount(){
		return ts.length+1;
	}

	public int getRowCount(){
		return ts[0].length;
	}


	public String getColumnName(int col) {
		    if (col==0) return "index";
			else return ""+(col-1);

         }

    public Object getValueAt(int row, int col) {
		    if (col==0) return new Integer(row);
		    else
			 return new Double(ts[col-1].arr[row]);
		}

   public Class getColumnClass(int c) {
           return getValueAt(0, c).getClass();
        }


   public boolean isCellEditable(int row, int col) {
	         if (col==0) return false;
	         else
              return true;
        }


   public void setValueAt(Object value, int row, int col) {

						ts[col-1].arr[row]=((Double)value).doubleValue();
		                fireTableCellUpdated(row, col);
		            }


}


class JythonMatrixTableModel extends AbstractTableModel  {

       //column 1 is simply row number


        public int getColumnCount() {
			 return ma.xdim+1;
			}

        public int getRowCount() {
			return ma.ydim;
		}

        public String getColumnName(int col) {
			if (col==0) return "(z"+maz+")";
			else return ""+(col-1);
         }

        public Object getValueAt(int row, int col) {
			if (col==0) return new Integer(row);
			else return new Double(ma.dat.get(maz,row,col-1));
		}




        public Class getColumnClass(int c) {
           return getValueAt(0, c).getClass();
        }


        public boolean isCellEditable(int row, int col) {
             return true;
        }


		public void setValueAt(Object value, int row, int col) {
			            if (col==0) return;
						ma.dat.set(maz,row,col-1,((Double)value).doubleValue());
		                fireTableCellUpdated(row, col-1);
		            }


	 }


class SelectionListener implements ListSelectionListener {
	         JythonTable jythontable;
	         JTable table;

	         // It is necessary to keep the table since it is not possible
	         // to determine the table from the event's source
	         SelectionListener(JythonTable jythontable) {
	             this.jythontable = jythontable;
	             this.table=jythontable.table;

	         }
	         public void valueChanged(ListSelectionEvent e) {
	             // If cell selection is enabled, both row and column change events are fired
	             if (e.getSource() == table.getSelectionModel()
	                   && table.getRowSelectionAllowed()) {
	                 // Column selection changed
	                 int first = e.getFirstIndex();
	                 int last = e.getLastIndex();
	                 jythontable.firstselected=first;
	                 jythontable.lastselected=last;
	             } else if (e.getSource() == table.getColumnModel().getSelectionModel()
	                    && table.getColumnSelectionAllowed() ){
	                 // Row selection changed
	                 int first = e.getFirstIndex();
	                 int last = e.getLastIndex();
	                 jythontable.firstselected=first;
	                 jythontable.lastselected=last;
	             }

	             if (e.getValueIsAdjusting()) {
	                 // The mouse button has not yet been released
	             }

	         }
    }

/*

 class ColumnHeaderListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            JTable table = ((JTableHeader)evt.getSource()).getTable();
            TableColumnModel colModel = table.getColumnModel();

            // The index of the column whose header was clicked
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
            int mColIndex = table.convertColumnIndexToModel(vColIndex);

            // Return if not clicked on any column header
            if (vColIndex == -1) {
                return;
            }

            // Determine if mouse was clicked between column heads
            Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
            if (vColIndex == 0) {
                headerRect.width -= 3;    // Hard-coded constant
            } else {
                headerRect.grow(-3, 0);   // Hard-coded constant
            }
            if (!headerRect.contains(evt.getX(), evt.getY())) {
                // Mouse was clicked between column heads
                // vColIndex is the column head closest to the click

                // vLeftColIndex is the column head to the left of the click
                int vLeftColIndex = vColIndex;
                if (evt.getX() < headerRect.x) {
                    vLeftColIndex--;
                }
            }
            System.out.println(" col head listener "+vColIndex+" "+mColIndex);
            if ((vColIndex==0) && (mColIndex==0)){
				Point	point = ((JComponent) evt.getSource()).getLocationOnScreen();
				showpopup(evt.getComponent(), point.x, point.y);

		     }
        }
    }

*/



}