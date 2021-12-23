package gvdecoder.prefs;
/** Holds a number of parameters in a group of local classes, which are accessed
    by the program. Stores them using prefs.**/
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;

import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingUtilities;





 public class PrefManager extends JPanel {
 JTabbedPane tabbedpane;

 PrefsTableModel pm;

 ArrayList allgroups;
 ArrayList allfields;
 int totalfields=0;

     private PrefManager(){

      allgroups=new ArrayList();
      allfields=new ArrayList();

     }

     public void setupGUI(){

	  tabbedpane=new JTabbedPane();
	  for (int i=0;i<allgroups.size();i++){
		  	  pm= new PrefsTableModel();
		  	  pm.index=i;
		  	  JTable table=new JTable(pm);

		  	  JPanel jp=new JPanel();
		  	  jp.setLayout(new BorderLayout()); //unnecessary
			  table.setPreferredScrollableViewportSize(new Dimension(500, 150));
			  	  //Create the scroll pane and add the table to it.
	          JScrollPane scrollPane = new JScrollPane(table);
	          jp.add(scrollPane, BorderLayout.CENTER);
	          String header=(String)((ArrayList)allgroups.get(i)).get(0);
	          tabbedpane.add(header,jp);
	     }
	  this.add(tabbedpane, BorderLayout.CENTER);
	 }





     public String getString(String key){
	 //very temporary for testing purpose
	 String val=null;
	 if (key.equals("CWRULocalMirrorPath")){
		  val="c:\\downloads\\efimov\\mirror";
        }
	 return val;
	 }

     public void register(String header, Object obj){

		ArrayList localgroup=new ArrayList();
		localgroup.add(header);
		try{
			Class cls=obj.getClass();
			Field fieldlist[] = cls.getDeclaredFields();

			 for (int i = 0; i < fieldlist.length; i++) {
			  if (Modifier.isVolatile(fieldlist[i].getModifiers())){
			  NameValue nv=new NameValue(obj,fieldlist[i],header,fieldlist[i].getName(),fieldlist[i].get(obj));
		      localgroup.add(nv);
			  allfields.add(nv);
			  totalfields++;
		      }
			 }

			}catch (Throwable e) {System.err.println(e);}
         allgroups.add(localgroup);

	 }



    public void print(){
	for (int i=0;i<allgroups.size();i++){
		ArrayList al=(ArrayList)allgroups.get(i);
		 System.out.println("\n header :"+(String)al.get(0));
		 for (int j=1;j<al.size();j++){
		  NameValue nv=(NameValue)al.get(j);
		  System.out.println(nv.header+" "+nv.name+" "+nv.value);
		 }
	  }
    }


     static private PrefManager sm_instance;
     static public PrefManager getInstance()
     {
         if ( sm_instance == null ) sm_instance = new PrefManager();
         return sm_instance;
     }


 class PrefsTableModel extends AbstractTableModel {

          int index=0;


          public int getColumnCount() {
  			 return 3;
          }

          public int getRowCount() {
			 ArrayList grp=(ArrayList)allgroups.get(index);
 			 return grp.size()-1;

          }

          public String getColumnName(int col) {
              if (col==0) return "name";
              else if (col==1) return "t/f";
 			  else return "val";
          }

          public Object getValueAt(int row, int col) {
 			 Object o=null;
 			 ArrayList grp=(ArrayList)allgroups.get(index);

 			 NameValue nv=(NameValue)grp.get(row+1);
 			 if (col==0) o=nv.name;
 			 if (nv.typeval==0){
			   if (col==2) o=null;
			   if(col==1)  o=nv.value;
			  }
			  else{
			  if (col==2) o=nv.value;
			  if (col==1) o=null;
		      }
             return o;
  			}



          public boolean isCellEditable(int row, int col) {
              boolean editable=false;
              ArrayList grp=(ArrayList)allgroups.get(index);
              NameValue nv=(NameValue)grp.get(row+1);
              if (nv.typeval==0){
			   // boolean
			   if (col==1) editable=true;
			   else editable =false;
			  }
			  else{
			   if (col==2) editable=true;
			   else editable=false;
			  }
			  return editable;
          }

		public Class getColumnClass(int c) {
			Boolean b=new Boolean(false);
			String s ="";
			if (c==1) return b.getClass();
			else return s.getClass();
		    }

  		public void setValueAt(Object value, int row, int col) {
		   try{
		   ArrayList grp=(ArrayList)allgroups.get(index);
		   NameValue nv=(NameValue)grp.get(row+1);

		   switch(nv.typeval){
			   case 0://boolean
			          try{
						/** I have no idea why this inversion is necissary BUG**/
						boolean b=(((Boolean)nv.value).booleanValue());
						nv.field.setBoolean(nv.obj, !b);
						nv.value=value;
					  }catch(Exception e){System.out.println("error in boolean set"); e.printStackTrace();}
			          break;
			   case 1://int
			   		   try{
					   	nv.field.set(nv.obj, Integer.valueOf((String)value));
					   	nv.value=value;
					   }catch(Exception e){}
			           break;
			   case 2://double
			   		   try{
					   nv.field.set(nv.obj, Double.valueOf((String)value));
					   nv.value=value;
					  }catch(Exception e){}
			   		   break;

			   case 3://string
			   		   nv.field.set(nv.obj,  (String)(value));
			   		   nv.value=value;
			   		   break;
			   default: nv.field.set(nv.obj, nv.value);

		   }

  		     fireTableCellUpdated(row, col);
  		   }catch(Exception e){e.printStackTrace();}

	 }
  	 }


 }

 class NameValue{
	 String header;
	 String name;
	 Object value;
	 Field field;
	 Object obj;
	 int typeval;
	 NameValue(Object obj,Field field, String header, Object name, Object value){
		 this.obj=obj;
		 this.field=field;
		 this.header=header;
		 this.name=(String)name;
		 this.value=value;
		 String tmp=""+field.getType(); //this is a indirect way of doing this, change.
		 if (tmp.equals("boolean")){typeval=0;}
		 else if (tmp.equals("int")){typeval=1;}
		 else if (tmp.equals("double")){typeval=2;}
		 else if (tmp.equals("class java.lang.String")){typeval=3;}
		 else typeval=-1;

		 System.out.println("the registered type is "+tmp +" with a val of "+typeval);
	 }
 }







