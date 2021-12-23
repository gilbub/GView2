package gvdecoder;
import java.util.*;
import javax.swing.*;

public class History extends AbstractListModel{

  public Vector historylist;
  public Object getElementAt(int index){return historylist.elementAt(index);}
  public int getSize(){return historylist.size();}
  public void add(String command){historylist.add(command);}

  public History(){
    historylist=new Vector();
  }

  }